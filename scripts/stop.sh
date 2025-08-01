#!/bin/bash

# Library Management System - Stop Script
# This script gracefully stops the running application

set -e

echo "🛑 Stopping Library Management System"
echo "====================================="

# Find application process
APP_PID=$(pgrep -f "library-management.*jar" || true)

if [ -z "$APP_PID" ]; then
    # Check if PID file exists
    if [ -f "library-management.pid" ]; then
        STORED_PID=$(cat library-management.pid)
        if kill -0 "$STORED_PID" 2>/dev/null; then
            APP_PID="$STORED_PID"
        else
            echo "⚠️  Stale PID file found, removing..."
            rm -f library-management.pid
        fi
    fi
fi

# Check systemd service
if command -v systemctl &> /dev/null && systemctl is-active --quiet library-management 2>/dev/null; then
    echo "📋 Found systemd service, stopping..."
    sudo systemctl stop library-management
    echo "✅ Systemd service stopped"
    exit 0
fi

if [ -z "$APP_PID" ]; then
    echo "ℹ️  No running Library Management System found"
    exit 0
fi

echo "🔍 Found application running with PID: $APP_PID"

# Graceful shutdown
echo "🤝 Attempting graceful shutdown..."
kill -TERM "$APP_PID"

# Wait for graceful shutdown
for i in {1..30}; do
    if ! kill -0 "$APP_PID" 2>/dev/null; then
        echo "✅ Application stopped gracefully"
        rm -f library-management.pid
        exit 0
    fi
    echo "⏳ Waiting for graceful shutdown... ($i/30)"
    sleep 2
done

# Force kill if still running
echo "⚠️  Graceful shutdown timeout, force killing..."
if kill -0 "$APP_PID" 2>/dev/null; then
    kill -KILL "$APP_PID"
    sleep 2
    
    if kill -0 "$APP_PID" 2>/dev/null; then
        echo "❌ Failed to stop application"
        exit 1
    else
        echo "🔪 Application force stopped"
        rm -f library-management.pid
    fi
else
    echo "✅ Application stopped"
    rm -f library-management.pid
fi