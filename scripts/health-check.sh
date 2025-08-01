#!/bin/bash

# Library Management System - Health Check Script
# This script performs comprehensive health checks on the running application

set -e

# Default configuration
DEFAULT_HOST="localhost"
DEFAULT_PORT="8080"
DEFAULT_TIMEOUT="10"

# Parse command line arguments
HOST="${1:-$DEFAULT_HOST}"
PORT="${2:-$DEFAULT_PORT}"
TIMEOUT="${3:-$DEFAULT_TIMEOUT}"

BASE_URL="http://$HOST:$PORT"

echo "🏥 Library Management System - Health Check"
echo "==========================================="
echo "🌐 Target: $BASE_URL"
echo "⏰ Timeout: ${TIMEOUT}s"
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
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

# Initialize counters
TOTAL_CHECKS=0
PASSED_CHECKS=0
FAILED_CHECKS=0

# Function to run a check
run_check() {
    local check_name=$1
    local check_command=$2
    local expected_result=${3:-0}
    
    TOTAL_CHECKS=$((TOTAL_CHECKS + 1))
    echo ""
    print_status "INFO" "Running: $check_name"
    
    if eval "$check_command" > /dev/null 2>&1; then
        local result=$?
        if [ $result -eq $expected_result ]; then
            print_status "PASS" "$check_name"
            PASSED_CHECKS=$((PASSED_CHECKS + 1))
            return 0
        fi
    fi
    
    print_status "FAIL" "$check_name"
    FAILED_CHECKS=$((FAILED_CHECKS + 1))
    return 1
}

# Function to check HTTP endpoint
check_endpoint() {
    local endpoint=$1
    local expected_status=${2:-200}
    local description=$3
    
    local url="$BASE_URL$endpoint"
    local response=$(curl -s -w "%{http_code}" -o /dev/null --connect-timeout $TIMEOUT "$url" 2>/dev/null || echo "000")
    
    TOTAL_CHECKS=$((TOTAL_CHECKS + 1))
    
    if [ "$response" = "$expected_status" ]; then
        print_status "PASS" "$description ($url) - HTTP $response"
        PASSED_CHECKS=$((PASSED_CHECKS + 1))
        return 0
    else
        print_status "FAIL" "$description ($url) - HTTP $response (expected $expected_status)"
        FAILED_CHECKS=$((FAILED_CHECKS + 1))
        return 1
    fi
}

# Function to check JSON response contains expected value
check_json_response() {
    local endpoint=$1
    local json_path=$2
    local expected_value=$3
    local description=$4
    
    local url="$BASE_URL$endpoint"
    local response=$(curl -s --connect-timeout $TIMEOUT "$url" 2>/dev/null || echo "{}")
    
    TOTAL_CHECKS=$((TOTAL_CHECKS + 1))
    
    if command -v jq >/dev/null 2>&1; then
        local actual_value=$(echo "$response" | jq -r "$json_path" 2>/dev/null || echo "null")
        if [ "$actual_value" = "$expected_value" ]; then
            print_status "PASS" "$description - $json_path: $actual_value"
            PASSED_CHECKS=$((PASSED_CHECKS + 1))
            return 0
        else
            print_status "FAIL" "$description - $json_path: $actual_value (expected $expected_value)"
            FAILED_CHECKS=$((FAILED_CHECKS + 1))
            return 1
        fi
    else
        # Fallback without jq
        if echo "$response" | grep -q "\"$expected_value\""; then
            print_status "PASS" "$description (contains expected value)"
            PASSED_CHECKS=$((PASSED_CHECKS + 1))
            return 0
        else
            print_status "FAIL" "$description (missing expected value)"
            FAILED_CHECKS=$((FAILED_CHECKS + 1))
            return 1
        fi
    fi
}

echo "🔍 Starting health checks..."

# Basic connectivity
check_endpoint "/actuator/health" "200" "Basic health endpoint"

# Application health status
if command -v jq >/dev/null 2>&1; then
    check_json_response "/actuator/health" ".status" "UP" "Application health status"
else
    print_status "WARN" "jq not available, skipping detailed JSON checks"
fi

# Individual actuator endpoints
check_endpoint "/actuator/info" "200" "Application info endpoint"
check_endpoint "/actuator/metrics" "200" "Metrics endpoint"

# API endpoints
check_endpoint "/books" "200" "Books listing endpoint"
check_endpoint "/swagger-ui/index.html" "200" "Swagger UI documentation"

# API documentation
check_endpoint "/v3/api-docs" "200" "OpenAPI specification"

# Test invalid endpoints return proper errors
check_endpoint "/nonexistent" "404" "404 handling for invalid endpoints"

# Test database connectivity (if custom health indicator is available)
if curl -s --connect-timeout $TIMEOUT "$BASE_URL/actuator/health" | grep -q "library"; then
    print_status "PASS" "Custom health indicator detected"
    PASSED_CHECKS=$((PASSED_CHECKS + 1))
else
    print_status "INFO" "Custom health indicator not detected (optional)"
fi

# Process check
TOTAL_CHECKS=$((TOTAL_CHECKS + 1))
if pgrep -f "library-management.*jar" > /dev/null; then
    APP_PID=$(pgrep -f "library-management.*jar")
    print_status "PASS" "Application process running (PID: $APP_PID)"
    PASSED_CHECKS=$((PASSED_CHECKS + 1))
else
    print_status "FAIL" "Application process not found"
    FAILED_CHECKS=$((FAILED_CHECKS + 1))
fi

# Memory usage check
TOTAL_CHECKS=$((TOTAL_CHECKS + 1))
if command -v ps >/dev/null 2>&1 && pgrep -f "library-management.*jar" > /dev/null; then
    APP_PID=$(pgrep -f "library-management.*jar")
    MEMORY_MB=$(ps -p $APP_PID -o rss= | awk '{print int($1/1024)}' 2>/dev/null || echo "0")
    if [ "$MEMORY_MB" -gt 0 ] && [ "$MEMORY_MB" -lt 2048 ]; then
        print_status "PASS" "Memory usage within limits (${MEMORY_MB}MB)"
        PASSED_CHECKS=$((PASSED_CHECKS + 1))
    elif [ "$MEMORY_MB" -ge 2048 ]; then
        print_status "WARN" "High memory usage (${MEMORY_MB}MB)"
        PASSED_CHECKS=$((PASSED_CHECKS + 1))
    else
        print_status "FAIL" "Could not determine memory usage"
        FAILED_CHECKS=$((FAILED_CHECKS + 1))
    fi
else
    print_status "FAIL" "Could not check memory usage"
    FAILED_CHECKS=$((FAILED_CHECKS + 1))
fi

# Performance check (response time)
TOTAL_CHECKS=$((TOTAL_CHECKS + 1))
RESPONSE_TIME=$(curl -s -w "%{time_total}" -o /dev/null --connect-timeout $TIMEOUT "$BASE_URL/actuator/health" 2>/dev/null || echo "999")
RESPONSE_TIME_MS=$(echo "$RESPONSE_TIME * 1000" | bc 2>/dev/null || echo "999")

if [ "${RESPONSE_TIME_MS%.*}" -lt 1000 ]; then
    print_status "PASS" "Response time acceptable (${RESPONSE_TIME}s)"
    PASSED_CHECKS=$((PASSED_CHECKS + 1))
elif [ "${RESPONSE_TIME_MS%.*}" -lt 5000 ]; then
    print_status "WARN" "Response time slow (${RESPONSE_TIME}s)"
    PASSED_CHECKS=$((PASSED_CHECKS + 1))
else
    print_status "FAIL" "Response time too slow (${RESPONSE_TIME}s)"
    FAILED_CHECKS=$((FAILED_CHECKS + 1))
fi

# Summary
echo ""
echo "📊 Health Check Summary"
echo "======================"
echo "Total Checks: $TOTAL_CHECKS"
echo "Passed: $PASSED_CHECKS"
echo "Failed: $FAILED_CHECKS"

SUCCESS_RATE=$((PASSED_CHECKS * 100 / TOTAL_CHECKS))
echo "Success Rate: $SUCCESS_RATE%"

if [ $FAILED_CHECKS -eq 0 ]; then
    print_status "PASS" "All health checks passed! Application is healthy. 🎉"
    exit 0
elif [ $SUCCESS_RATE -ge 80 ]; then
    print_status "WARN" "Most health checks passed, but some issues detected."
    exit 1
else
    print_status "FAIL" "Multiple health checks failed. Application may not be functioning properly."
    exit 2
fi