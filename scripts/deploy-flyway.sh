#!/bin/bash

# Library Management System - Flyway Deployment Script
# This script deploys the application using Flyway migrations instead of JPA auto-generation

set -e

echo "🗄️  Library Management System - Flyway Migration Deployment"
echo "=========================================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

print_status() {
    local status=$1
    local message=$2
    case $status in
        "PASS")
            echo -e "${GREEN}✅ PASS${NC} - $message"
            ;;
        "FAIL")
            echo -e "${RED}❌ FAIL${NC} - $message"
            ;;
        "WARN")
            echo -e "${YELLOW}⚠️  WARN${NC} - $message"
            ;;
        "INFO")
            echo -e "ℹ️  INFO - $message"
            ;;
    esac
}

# Check if PostgreSQL is required
print_status "INFO" "This deployment uses Flyway migrations and requires PostgreSQL"
print_status "WARN" "Make sure PostgreSQL is running and environment variables are set:"
echo "  - DB_USERNAME: PostgreSQL username"
echo "  - DB_PASSWORD: PostgreSQL password"
echo "  - DB_HOST: Database host (default: localhost)"
echo "  - DB_PORT: Database port (default: 5432)"
echo "  - DB_NAME: Database name (default: library_db)"
echo ""

# Check required environment variables
if [ -z "$DB_USERNAME" ]; then
    print_status "FAIL" "DB_USERNAME environment variable is required"
    exit 1
fi

if [ -z "$DB_PASSWORD" ]; then
    print_status "FAIL" "DB_PASSWORD environment variable is required"
    exit 1
fi

# Set defaults for optional variables
export DB_HOST="${DB_HOST:-localhost}"
export DB_PORT="${DB_PORT:-5432}"
export DB_NAME="${DB_NAME:-library_db}"
export SERVER_PORT="${SERVER_PORT:-8080}"

print_status "PASS" "Environment variables validated"

# Check if Java 21 is available
if ! command -v java &> /dev/null; then
    print_status "FAIL" "Java is not installed or not in PATH"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | grep -oP 'version "([0-9]+)' | grep -oP '[0-9]+' | head -1)
if [ "$JAVA_VERSION" -lt 21 ]; then
    print_status "FAIL" "Java 21 or higher is required. Current version: $JAVA_VERSION"
    exit 1
fi

print_status "PASS" "Java $JAVA_VERSION detected"

# Navigate to project directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
cd "$PROJECT_DIR"

print_status "INFO" "Working directory: $PROJECT_DIR"

# Test database connectivity
print_status "INFO" "Testing database connectivity..."
if command -v psql &> /dev/null; then
    if PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USERNAME" -d "$DB_NAME" -c "SELECT 1;" > /dev/null 2>&1; then
        print_status "PASS" "Database connection successful"
    else
        print_status "FAIL" "Cannot connect to database"
        print_status "INFO" "Connection details: $DB_USERNAME@$DB_HOST:$DB_PORT/$DB_NAME"
        exit 1
    fi
else
    print_status "WARN" "psql not available, skipping database connectivity test"
fi

# Build the project
print_status "INFO" "Building project..."
./mvnw clean package -DskipTests -Dspring.profiles.active=flyway

if [ $? -ne 0 ]; then
    print_status "FAIL" "Build failed"
    exit 1
fi

print_status "PASS" "Build successful"

# Check if application is already running
APP_PID=$(pgrep -f "library-management.*jar" || true)
if [ ! -z "$APP_PID" ]; then
    print_status "WARN" "Application is already running (PID: $APP_PID)"
    print_status "INFO" "Stopping existing application..."
    kill -TERM "$APP_PID"
    sleep 5
    
    if kill -0 "$APP_PID" 2>/dev/null; then
        print_status "WARN" "Force killing application..."
        kill -KILL "$APP_PID"
    fi
    print_status "PASS" "Previous application stopped"
fi

# Create logs directory
mkdir -p logs

# Start the application with Flyway profile
print_status "INFO" "Starting Library Management System (Flyway Mode)..."
JAR_FILE="target/library-management-0.0.1-SNAPSHOT.jar"

if [ ! -f "$JAR_FILE" ]; then
    print_status "FAIL" "JAR file not found: $JAR_FILE"
    exit 1
fi

# Set environment variables and JVM options
export SPRING_PROFILES_ACTIVE=flyway
export JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -Dspring.profiles.active=flyway"

print_status "INFO" "Configuration:"
echo "  🎯 Environment: Flyway Migration"
echo "  🌐 Port: $SERVER_PORT"
echo "  💾 Profile: $SPRING_PROFILES_ACTIVE"
echo "  🗄️  Database: $DB_HOST:$DB_PORT/$DB_NAME"

# Start application in background
nohup java $JAVA_OPTS \
    -Dspring.datasource.url="jdbc:postgresql://$DB_HOST:$DB_PORT/$DB_NAME" \
    -Dspring.datasource.username="$DB_USERNAME" \
    -Dspring.datasource.password="$DB_PASSWORD" \
    -jar "$JAR_FILE" > logs/app-flyway.log 2>&1 &

APP_PID=$!
echo $APP_PID > library-management-flyway.pid

print_status "PASS" "Application started with PID: $APP_PID"

echo ""
print_status "INFO" "Service URLs:"
echo "  📊 Application: http://localhost:$SERVER_PORT"
echo "  📚 API Documentation: http://localhost:$SERVER_PORT/swagger-ui/index.html"
echo "  🏥 Health Check: http://localhost:$SERVER_PORT/actuator/health"
echo "  🗄️  Flyway Info: http://localhost:$SERVER_PORT/actuator/flyway"

# Wait for application to start
print_status "INFO" "Waiting for application to start..."
for i in {1..60}; do
    if curl -f -s "http://localhost:$SERVER_PORT/actuator/health" > /dev/null 2>&1; then
        print_status "PASS" "Application is ready and healthy!"
        
        # Check Flyway migrations
        print_status "INFO" "Checking Flyway migration status..."
        FLYWAY_RESPONSE=$(curl -s "http://localhost:$SERVER_PORT/actuator/flyway" 2>/dev/null || echo '{}')
        
        if echo "$FLYWAY_RESPONSE" | grep -q "migrations" 2>/dev/null; then
            print_status "PASS" "Flyway migrations completed successfully"
            
            # Show migration details if jq is available
            if command -v jq >/dev/null 2>&1; then
                MIGRATION_COUNT=$(echo "$FLYWAY_RESPONSE" | jq '.contexts.application.flywayBeans.flyway.migrations | length' 2>/dev/null || echo "N/A")
                print_status "INFO" "Applied migrations: $MIGRATION_COUNT"
            fi
        else
            print_status "WARN" "Could not verify Flyway migration status"
        fi
        
        echo ""
        print_status "PASS" "🎉 Flyway deployment successful!"
        echo ""
        echo "📋 Management commands:"
        echo "  📊 Status: ps aux | grep library-management"
        echo "  📋 Logs: tail -f logs/app-flyway.log"
        echo "  🗄️  Flyway: curl http://localhost:$SERVER_PORT/actuator/flyway"
        echo "  🛑 Stop: kill $APP_PID"
        echo "  🏥 Health: curl http://localhost:$SERVER_PORT/actuator/health"
        
        exit 0
    fi
    echo "⏱️  Waiting... ($i/60)"
    sleep 3
done

print_status "WARN" "Application may still be starting. Check logs:"
echo "📋 Logs: tail -f logs/app-flyway.log"
echo "🛑 Stop: kill $APP_PID"
echo "🗂️  PID file: library-management-flyway.pid"