#!/bin/bash

# Library Management System - Production Deployment Script
# This script builds and runs the application in production mode

set -e  # Exit on any error

echo "🚀 Starting Library Management System - Production Deployment"
echo "============================================================="

# Check if running as root (not recommended for production)
if [ "$EUID" -eq 0 ]; then
    echo "⚠️  Warning: Running as root is not recommended for production"
    read -p "Continue anyway? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
fi

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

# Validate environment variables
echo "🔍 Checking production environment variables..."

# Database configuration
if [ -z "$DB_HOST" ]; then
    echo "⚠️  DB_HOST not set, using default: localhost"
    export DB_HOST="localhost"
fi

if [ -z "$DB_PORT" ]; then
    echo "⚠️  DB_PORT not set, using default: 5432"
    export DB_PORT="5432"
fi

if [ -z "$DB_NAME" ]; then
    echo "⚠️  DB_NAME not set, using default: library_db"
    export DB_NAME="library_db"
fi

if [ -z "$DB_USERNAME" ]; then
    echo "❌ DB_USERNAME environment variable is required for production"
    exit 1
fi

if [ -z "$DB_PASSWORD" ]; then
    echo "❌ DB_PASSWORD environment variable is required for production"
    exit 1
fi

# Application configuration
if [ -z "$SERVER_PORT" ]; then
    export SERVER_PORT="8080"
fi

echo "✅ Environment variables validated"

# Run tests before deployment
echo "🧪 Running tests..."
./mvnw test -Dspring.profiles.active=test

if [ $? -ne 0 ]; then
    echo "❌ Tests failed. Deployment aborted."
    exit 1
fi

echo "✅ All tests passed"

# Build the project
echo "🔧 Building project for production..."
./mvnw clean package -DskipTests -Dspring.profiles.active=prod

if [ $? -ne 0 ]; then
    echo "❌ Build failed"
    exit 1
fi

echo "✅ Build successful"

# Create logs directory if it doesn't exist
mkdir -p logs

# Check if application is already running
APP_PID=$(pgrep -f "library-management.*jar" || true)
if [ ! -z "$APP_PID" ]; then
    echo "⚠️  Application is already running (PID: $APP_PID)"
    echo "🛑 Stopping existing application gracefully..."
    kill -TERM "$APP_PID"
    
    # Wait for graceful shutdown
    for i in {1..30}; do
        if ! kill -0 "$APP_PID" 2>/dev/null; then
            echo "✅ Application stopped gracefully"
            break
        fi
        echo "⏳ Waiting for graceful shutdown... ($i/30)"
        sleep 2
    done
    
    # Force kill if still running
    if kill -0 "$APP_PID" 2>/dev/null; then
        echo "🔪 Force killing application..."
        kill -KILL "$APP_PID"
        sleep 2
    fi
fi

# Backup current JAR if it exists
JAR_FILE="target/library-management-0.0.1-SNAPSHOT.jar"
if [ -f "$JAR_FILE" ]; then
    BACKUP_NAME="library-management-backup-$(date +%Y%m%d-%H%M%S).jar"
    echo "📦 Creating backup: $BACKUP_NAME"
    cp "$JAR_FILE" "backups/$BACKUP_NAME" 2>/dev/null || true
fi

if [ ! -f "$JAR_FILE" ]; then
    echo "❌ JAR file not found: $JAR_FILE"
    exit 1
fi

# Set production environment variables and JVM options
export SPRING_PROFILES_ACTIVE=prod
export JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+UseStringDeduplication -Dspring.profiles.active=prod -Dserver.port=$SERVER_PORT"

# Database connection string
export DB_URL="jdbc:postgresql://$DB_HOST:$DB_PORT/$DB_NAME"

echo "🎯 Environment: Production"
echo "🌐 Port: $SERVER_PORT"
echo "💾 Profile: $SPRING_PROFILES_ACTIVE"
echo "🗄️  Database: $DB_HOST:$DB_PORT/$DB_NAME"

# Start application
echo "🚀 Starting Library Management System (Production Mode)..."

# Use systemd-style service management if available, otherwise background process
if command -v systemctl &> /dev/null && [ -f "/etc/systemd/system/library-management.service" ]; then
    echo "📋 Using systemd service management"
    sudo systemctl start library-management
    echo "✅ Application started via systemd"
    echo "📊 Status: sudo systemctl status library-management"
    echo "📋 Logs: sudo journalctl -u library-management -f"
else
    # Start as background process
    nohup java $JAVA_OPTS \
        -Dspring.datasource.url="$DB_URL" \
        -Dspring.datasource.username="$DB_USERNAME" \
        -Dspring.datasource.password="$DB_PASSWORD" \
        -jar "$JAR_FILE" > logs/app-prod.log 2>&1 &
    
    APP_PID=$!
    echo "✅ Application started with PID: $APP_PID"
    
    # Save PID for management
    echo $APP_PID > library-management.pid
fi

echo "📊 Application endpoints:"
echo "  🌐 Base URL: http://localhost:$SERVER_PORT"
echo "  📚 API Docs: http://localhost:$SERVER_PORT/swagger-ui/index.html"
echo "  🏥 Health: http://localhost:$SERVER_PORT/actuator/health"
echo "  📈 Metrics: http://localhost:$SERVER_PORT/actuator/metrics"

# Wait for application to start
echo "⏳ Waiting for application to start..."
for i in {1..60}; do
    if curl -f -s "http://localhost:$SERVER_PORT/actuator/health" > /dev/null 2>&1; then
        echo "✅ Application is ready and healthy!"
        
        # Test database connectivity
        HEALTH_RESPONSE=$(curl -s "http://localhost:$SERVER_PORT/actuator/health")
        if echo "$HEALTH_RESPONSE" | grep -q '"status":"UP"'; then
            echo "✅ Health check passed"
            echo "🎉 Production deployment successful!"
            exit 0
        else
            echo "⚠️  Health check failed. Response: $HEALTH_RESPONSE"
        fi
        break
    fi
    echo "⏱️  Waiting... ($i/60)"
    sleep 3
done

echo "⚠️  Application may still be starting. Check logs:"
if [ -f "library-management.pid" ]; then
    echo "📋 Logs: tail -f logs/app-prod.log"
    echo "🛑 Stop: kill $(cat library-management.pid)"
else
    echo "📋 Logs: sudo journalctl -u library-management -f"
    echo "🛑 Stop: sudo systemctl stop library-management"
fi