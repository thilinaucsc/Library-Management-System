#!/bin/bash

# Library Management System - Development Deployment Script
# This script builds and runs the application in development mode

set -e  # Exit on any error

echo "🚀 Starting Library Management System - Development Deployment"
echo "============================================================"

# Check if Java 21 is available
if ! command -v java &> /dev/null; then
    echo "❌ Java is not installed or not in PATH"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | grep -oP 'version "([0-9]+)' | grep -oP '[0-9]+' | head -1)
if [ "$JAVA_VERSION" -lt 21 ]; then
    echo "❌ Java 21 or higher is required. Current version: $JAVA_VERSION"
    exit 1
fi

echo "✅ Java $JAVA_VERSION detected"

# Navigate to project directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
cd "$PROJECT_DIR"

echo "📂 Working directory: $PROJECT_DIR"

# Clean and build the project
echo "🔧 Building project..."
./mvnw clean package -DskipTests -Dspring.profiles.active=dev

if [ $? -ne 0 ]; then
    echo "❌ Build failed"
    exit 1
fi

echo "✅ Build successful"

# Check if application is already running
APP_PID=$(pgrep -f "library-management.*jar" || true)
if [ ! -z "$APP_PID" ]; then
    echo "⚠️  Application is already running (PID: $APP_PID)"
    echo "🛑 Stopping existing application..."
    kill -TERM "$APP_PID"
    sleep 5
    
    # Force kill if still running
    if kill -0 "$APP_PID" 2>/dev/null; then
        echo "🔪 Force killing application..."
        kill -KILL "$APP_PID"
    fi
    echo "✅ Previous application stopped"
fi

# Start the application
echo "🚀 Starting Library Management System (Development Mode)..."
JAR_FILE="target/library-management-0.0.1-SNAPSHOT.jar"

if [ ! -f "$JAR_FILE" ]; then
    echo "❌ JAR file not found: $JAR_FILE"
    exit 1
fi

# Set development environment variables
export SPRING_PROFILES_ACTIVE=dev
export SERVER_PORT=8080
export JAVA_OPTS="-Xms256m -Xmx512m -Dspring.profiles.active=dev"

echo "🎯 Environment: Development"
echo "🌐 Port: $SERVER_PORT"
echo "💾 Profile: $SPRING_PROFILES_ACTIVE"

# Start application in background
nohup java $JAVA_OPTS -jar "$JAR_FILE" > logs/app-dev.log 2>&1 &
APP_PID=$!

echo "✅ Application started with PID: $APP_PID"
echo "📊 Application will be available at: http://localhost:$SERVER_PORT"
echo "📚 API Documentation: http://localhost:$SERVER_PORT/swagger-ui/index.html"
echo "🏥 Health Check: http://localhost:$SERVER_PORT/actuator/health"
echo "🗄️  H2 Console: http://localhost:$SERVER_PORT/h2-console"

# Wait for application to start
echo "⏳ Waiting for application to start..."
for i in {1..30}; do
    if curl -f -s "http://localhost:$SERVER_PORT/actuator/health" > /dev/null 2>&1; then
        echo "✅ Application is ready!"
        echo "📋 Logs: tail -f logs/app-dev.log"
        echo "🛑 Stop: kill $APP_PID"
        exit 0
    fi
    echo "⏱️  Waiting... ($i/30)"
    sleep 2
done

echo "⚠️  Application may still be starting. Check logs: tail -f logs/app-dev.log"
echo "🛑 To stop: kill $APP_PID"