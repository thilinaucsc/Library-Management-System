#!/bin/bash

# Library Management System - Docker Deployment Script
# This script manages Docker-based deployment of the application

set -e

# Default configuration
DEFAULT_ENVIRONMENT="dev"
DEFAULT_COMPOSE_FILE="docker-compose.yml"

# Parse command line arguments
ENVIRONMENT="${1:-$DEFAULT_ENVIRONMENT}"
COMPOSE_FILE="${2:-$DEFAULT_COMPOSE_FILE}"

echo "🐳 Library Management System - Docker Deployment"
echo "================================================"
echo "🎯 Environment: $ENVIRONMENT"
echo "📄 Compose File: $COMPOSE_FILE"
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
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
            echo -e "${BLUE}ℹ️  INFO${NC} - $message"
            ;;
    esac
}

# Function to check prerequisites
check_prerequisites() {
    print_status "INFO" "Checking prerequisites..."
    
    # Check Docker
    if ! command -v docker &> /dev/null; then
        print_status "FAIL" "Docker is not installed"
        echo "Please install Docker: https://docs.docker.com/get-docker/"
        exit 1
    fi
    
    # Check Docker Compose
    if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
        print_status "FAIL" "Docker Compose is not installed"
        echo "Please install Docker Compose: https://docs.docker.com/compose/install/"
        exit 1
    fi
    
    # Check if Docker daemon is running
    if ! docker info &> /dev/null; then
        print_status "FAIL" "Docker daemon is not running"
        echo "Please start Docker daemon"
        exit 1
    fi
    
    print_status "PASS" "All prerequisites satisfied"
}

# Function to check if compose file exists
check_compose_file() {
    if [ ! -f "$COMPOSE_FILE" ]; then
        print_status "FAIL" "Docker Compose file not found: $COMPOSE_FILE"
        exit 1
    fi
    print_status "PASS" "Docker Compose file found"
}

# Function to build the application
build_application() {
    print_status "INFO" "Building application..."
    
    # Navigate to project directory
    SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
    PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
    cd "$PROJECT_DIR"
    
    # Build Docker image
    if docker-compose -f "$COMPOSE_FILE" build app; then
        print_status "PASS" "Application build successful"
    elif docker compose -f "$COMPOSE_FILE" build app; then
        print_status "PASS" "Application build successful"
    else
        print_status "FAIL" "Application build failed"
        exit 1
    fi
}

# Function to start services
start_services() {
    print_status "INFO" "Starting services..."
    
    local compose_cmd="docker-compose"
    if ! command -v docker-compose &> /dev/null; then
        compose_cmd="docker compose"
    fi
    
    # Determine which services to start based on environment
    local services=""
    case $ENVIRONMENT in
        "dev"|"development")
            services="database app"
            ;;
        "prod"|"production")
            services="database app"
            ;;
        "admin")
            services="database app pgadmin"
            ;;
        "all")
            services=""  # Start all services
            ;;
        *)
            print_status "WARN" "Unknown environment: $ENVIRONMENT, starting all services"
            services=""
            ;;
    esac
    
    # Start services
    if [ -z "$services" ]; then
        $compose_cmd -f "$COMPOSE_FILE" up -d
    else
        $compose_cmd -f "$COMPOSE_FILE" up -d $services
    fi
    
    if [ $? -eq 0 ]; then
        print_status "PASS" "Services started successfully"
    else
        print_status "FAIL" "Failed to start services"
        exit 1
    fi
}

# Function to wait for services to be healthy
wait_for_services() {
    print_status "INFO" "Waiting for services to be healthy..."
    
    local max_attempts=60
    local attempt=0
    
    while [ $attempt -lt $max_attempts ]; do
        attempt=$((attempt + 1))
        
        # Check database health
        if docker-compose -f "$COMPOSE_FILE" ps database | grep -q "healthy" 2>/dev/null || \
           docker compose -f "$COMPOSE_FILE" ps database | grep -q "healthy" 2>/dev/null; then
            print_status "PASS" "Database is healthy"
            break
        elif [ $attempt -eq $max_attempts ]; then
            print_status "FAIL" "Database failed to become healthy"
            show_logs
            exit 1
        fi
        
        echo "⏳ Waiting for database... ($attempt/$max_attempts)"
        sleep 5
    done
    
    # Wait a bit more for application
    attempt=0
    while [ $attempt -lt $max_attempts ]; do
        attempt=$((attempt + 1))
        
        # Check application health
        if docker-compose -f "$COMPOSE_FILE" ps app | grep -q "healthy" 2>/dev/null || \
           docker compose -f "$COMPOSE_FILE" ps app | grep -q "healthy" 2>/dev/null; then
            print_status "PASS" "Application is healthy"
            return 0
        elif curl -f -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
            print_status "PASS" "Application is responding"
            return 0
        elif [ $attempt -eq $max_attempts ]; then
            print_status "FAIL" "Application failed to become healthy"
            show_logs
            exit 1
        fi
        
        echo "⏳ Waiting for application... ($attempt/$max_attempts)"
        sleep 5
    done
}

# Function to show service status
show_status() {
    print_status "INFO" "Service status:"
    
    local compose_cmd="docker-compose"
    if ! command -v docker-compose &> /dev/null; then
        compose_cmd="docker compose"
    fi
    
    $compose_cmd -f "$COMPOSE_FILE" ps
}

# Function to show logs
show_logs() {
    print_status "INFO" "Recent logs:"
    
    local compose_cmd="docker-compose"
    if ! command -v docker-compose &> /dev/null; then
        compose_cmd="docker compose"
    fi
    
    $compose_cmd -f "$COMPOSE_FILE" logs --tail=50
}

# Function to run health checks
run_health_checks() {
    print_status "INFO" "Running health checks..."
    
    # Check if application responds
    if curl -f -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
        print_status "PASS" "Application health check passed"
    else
        print_status "FAIL" "Application health check failed"
        return 1
    fi
    
    # Check if API endpoints work
    if curl -f -s http://localhost:8080/books > /dev/null 2>&1; then
        print_status "PASS" "API endpoints responding"
    else
        print_status "FAIL" "API endpoints not responding"
        return 1
    fi
    
    # Check database connectivity through application
    local health_response=$(curl -s http://localhost:8080/actuator/health 2>/dev/null || echo '{}')
    if echo "$health_response" | grep -q '"status":"UP"'; then
        print_status "PASS" "Database connectivity confirmed"
    else
        print_status "WARN" "Database connectivity uncertain"
    fi
    
    return 0
}

# Function to display service URLs
show_service_urls() {
    print_status "INFO" "Service URLs:"
    echo "  🌐 Application: http://localhost:8080"
    echo "  📚 API Documentation: http://localhost:8080/swagger-ui/index.html"
    echo "  🏥 Health Check: http://localhost:8080/actuator/health"
    echo "  📈 Metrics: http://localhost:8080/actuator/metrics"
    echo "  🗄️  Database: localhost:5432 (library_db/library_user)"
    
    # Check if pgAdmin is running
    if docker ps --format "table {{.Names}}" | grep -q "library-pgadmin"; then
        echo "  🔧 pgAdmin: http://localhost:5050 (admin@library.local/admin_password)"
    fi
}

# Function to cleanup (stop and remove containers)
cleanup() {
    print_status "INFO" "Cleaning up..."
    
    local compose_cmd="docker-compose"
    if ! command -v docker-compose &> /dev/null; then
        compose_cmd="docker compose"
    fi
    
    $compose_cmd -f "$COMPOSE_FILE" down -v
    print_status "PASS" "Cleanup completed"
}

# Main deployment function
deploy() {
    echo "🔍 Pre-deployment checks..."
    check_prerequisites
    check_compose_file
    
    echo ""
    echo "🔨 Building application..."
    build_application
    
    echo ""
    echo "🚀 Starting services..."
    start_services
    
    echo ""
    echo "⏳ Waiting for services..."
    wait_for_services
    
    echo ""
    echo "📊 Service status..."
    show_status
    
    echo ""
    echo "🏥 Running health checks..."
    if run_health_checks; then
        echo ""
        echo "🎉 Deployment successful!"
        show_service_urls
        
        echo ""
        echo "📋 Management commands:"
        echo "  📊 Status: docker-compose -f $COMPOSE_FILE ps"
        echo "  📋 Logs: docker-compose -f $COMPOSE_FILE logs -f"
        echo "  🛑 Stop: docker-compose -f $COMPOSE_FILE down"
        echo "  🔄 Restart: docker-compose -f $COMPOSE_FILE restart"
        echo "  🧹 Cleanup: docker-compose -f $COMPOSE_FILE down -v"
    else
        echo ""
        print_status "WARN" "Deployment completed with health check warnings"
        show_logs
    fi
}

# Handle command line arguments
case "$ENVIRONMENT" in
    "help"|"--help"|"-h")
        echo "Usage: $0 [environment] [compose-file]"
        echo ""
        echo "Environments:"
        echo "  dev/development  - Start database and application"
        echo "  prod/production  - Start database and application (production mode)"
        echo "  admin           - Start database, application, and pgAdmin"
        echo "  all             - Start all services"
        echo ""
        echo "Examples:"
        echo "  $0                          # Deploy in dev environment"
        echo "  $0 prod                     # Deploy in production environment"
        echo "  $0 admin                    # Deploy with pgAdmin"
        echo "  $0 dev docker-compose.yml  # Use specific compose file"
        echo ""
        echo "Other commands:"
        echo "  $0 stop                     # Stop all services"
        echo "  $0 cleanup                  # Stop and remove all containers and volumes"
        echo "  $0 logs                     # Show logs"
        echo "  $0 status                   # Show service status"
        exit 0
        ;;
    "stop")
        echo "🛑 Stopping services..."
        local compose_cmd="docker-compose"
        if ! command -v docker-compose &> /dev/null; then
            compose_cmd="docker compose"
        fi
        $compose_cmd -f "$COMPOSE_FILE" down
        print_status "PASS" "Services stopped"
        exit 0
        ;;
    "cleanup")
        cleanup
        exit 0
        ;;
    "logs")
        show_logs
        exit 0
        ;;
    "status")
        show_status
        exit 0
        ;;
    *)
        deploy
        ;;
esac