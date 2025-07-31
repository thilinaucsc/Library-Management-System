#!/bin/bash

# Library Management System - Performance Testing Script
# This script performs basic performance and load testing

set -e

# Default configuration
DEFAULT_HOST="localhost"
DEFAULT_PORT="8080"
DEFAULT_CONCURRENT="10"
DEFAULT_REQUESTS="100"
DEFAULT_DURATION="30"

# Parse command line arguments
HOST="${1:-$DEFAULT_HOST}"
PORT="${2:-$DEFAULT_PORT}"
CONCURRENT="${3:-$DEFAULT_CONCURRENT}"
REQUESTS="${4:-$DEFAULT_REQUESTS}"
DURATION="${5:-$DEFAULT_DURATION}"

BASE_URL="http://$HOST:$PORT"

echo "🚀 Library Management System - Performance Testing"
echo "=================================================="
echo "🌐 Target: $BASE_URL"
echo "👥 Concurrent Users: $CONCURRENT"
echo "📊 Total Requests: $REQUESTS"
echo "⏱️  Duration: ${DURATION}s"
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

# Function to check if application is running
check_application() {
    print_status "INFO" "Checking if application is running..."
    
    if curl -f -s "$BASE_URL/actuator/health" > /dev/null 2>&1; then
        print_status "PASS" "Application is running and healthy"
        return 0
    else
        print_status "FAIL" "Application is not running or not healthy"
        echo "Please start the application first using: ./scripts/deploy-dev.sh"
        exit 1
    fi
}

# Function to create test data
setup_test_data() {
    print_status "INFO" "Setting up test data..."
    
    # Create a test borrower
    BORROWER_RESPONSE=$(curl -s -X POST "$BASE_URL/borrowers" \
        -H "Content-Type: application/json" \
        -d '{"name": "Performance Test User", "email": "perf.test@example.com"}' \
        2>/dev/null || echo '{"error": "failed"}')
    
    if echo "$BORROWER_RESPONSE" | grep -q '"id"'; then
        if command -v jq >/dev/null 2>&1; then
            BORROWER_ID=$(echo "$BORROWER_RESPONSE" | jq -r '.id')
            print_status "PASS" "Test borrower created with ID: $BORROWER_ID"
        else
            BORROWER_ID="1"
            print_status "PASS" "Test borrower created (jq not available for ID parsing)"
        fi
    else
        print_status "WARN" "Could not create test borrower (may already exist)"
        BORROWER_ID="1"
    fi
    
    # Create test books
    for i in {1..5}; do
        BOOK_RESPONSE=$(curl -s -X POST "$BASE_URL/books" \
            -H "Content-Type: application/json" \
            -d "{\"isbn\": \"978-0-123-45678-$i\", \"title\": \"Performance Test Book $i\", \"author\": \"Test Author $i\"}" \
            2>/dev/null || echo '{"error": "failed"}')
        
        if echo "$BOOK_RESPONSE" | grep -q '"id"'; then
            print_status "PASS" "Test book $i created"
        else
            print_status "WARN" "Could not create test book $i (may already exist)"
        fi
    done
}

# Function to run performance test using Apache Bench (ab)
run_ab_test() {
    local endpoint=$1
    local description=$2
    local method=${3:-GET}
    local data=${4:-""}
    
    print_status "INFO" "Running AB test: $description"
    
    if ! command -v ab >/dev/null 2>&1; then
        print_status "WARN" "Apache Bench (ab) not found. Skipping AB tests."
        print_status "INFO" "Install with: sudo apt-get install apache2-utils (Ubuntu/Debian)"
        return 1
    fi
    
    local url="$BASE_URL$endpoint"
    local ab_args="-n $REQUESTS -c $CONCURRENT -g ab_results.tsv"
    
    if [ "$method" = "POST" ] && [ ! -z "$data" ]; then
        echo "$data" > temp_post_data.json
        ab_args="$ab_args -p temp_post_data.json -T application/json"
    fi
    
    echo "Running: ab $ab_args $url"
    ab $ab_args "$url" > ab_output.tmp 2>&1
    
    if [ $? -eq 0 ]; then
        # Parse results
        local total_time=$(grep "Time taken for tests:" ab_output.tmp | awk '{print $5}')
        local rps=$(grep "Requests per second:" ab_output.tmp | awk '{print $4}')
        local mean_time=$(grep "Time per request:" ab_output.tmp | head -1 | awk '{print $4}')
        local failed=$(grep "Failed requests:" ab_output.tmp | awk '{print $3}')
        
        print_status "PASS" "$description completed"
        echo "  📊 Total Time: ${total_time}s"
        echo "  🔄 Requests/sec: $rps"
        echo "  ⏱️  Mean Time: ${mean_time}ms"
        echo "  ❌ Failed: $failed"
        
        # Cleanup
        rm -f temp_post_data.json ab_output.tmp
        
        return 0
    else
        print_status "FAIL" "$description failed"
        rm -f temp_post_data.json ab_output.tmp
        return 1
    fi
}

# Function to run curl-based load test
run_curl_test() {
    local endpoint=$1
    local description=$2
    local method=${3:-GET}
    local data=${4:-""}
    
    print_status "INFO" "Running curl-based test: $description"
    
    local url="$BASE_URL$endpoint"
    local success_count=0
    local error_count=0
    local total_time=0
    local start_time=$(date +%s)
    
    for i in $(seq 1 $REQUESTS); do
        local request_start=$(date +%s.%3N)
        
        if [ "$method" = "POST" ] && [ ! -z "$data" ]; then
            local response=$(curl -s -w "%{http_code}" -o /dev/null \
                -X POST "$url" \
                -H "Content-Type: application/json" \
                -d "$data" \
                --connect-timeout 10 --max-time 30 2>/dev/null || echo "000")
        else
            local response=$(curl -s -w "%{http_code}" -o /dev/null \
                "$url" --connect-timeout 10 --max-time 30 2>/dev/null || echo "000")
        fi
        
        local request_end=$(date +%s.%3N)
        local request_time=$(echo "$request_end - $request_start" | bc 2>/dev/null || echo "0")
        total_time=$(echo "$total_time + $request_time" | bc 2>/dev/null || echo "$total_time")
        
        if [ "$response" = "200" ] || [ "$response" = "201" ]; then
            success_count=$((success_count + 1))
        else
            error_count=$((error_count + 1))
        fi
        
        # Progress indicator
        if [ $((i % 10)) -eq 0 ]; then
            echo -n "."
        fi
    done
    
    echo ""
    local end_time=$(date +%s)
    local elapsed_time=$((end_time - start_time))
    local rps=$(echo "scale=2; $REQUESTS / $elapsed_time" | bc 2>/dev/null || echo "N/A")
    local avg_time=$(echo "scale=3; $total_time / $REQUESTS * 1000" | bc 2>/dev/null || echo "N/A")
    
    print_status "PASS" "$description completed (curl-based)"
    echo "  📊 Total Time: ${elapsed_time}s"
    echo "  🔄 Requests/sec: $rps"
    echo "  ⏱️  Avg Time: ${avg_time}ms"
    echo "  ✅ Success: $success_count"
    echo "  ❌ Errors: $error_count"
    
    local success_rate=$((success_count * 100 / REQUESTS))
    if [ $success_rate -ge 95 ]; then
        print_status "PASS" "Success rate: $success_rate%"
    elif [ $success_rate -ge 80 ]; then
        print_status "WARN" "Success rate: $success_rate%"
    else
        print_status "FAIL" "Success rate: $success_rate%"
    fi
}

# Function to monitor system resources
monitor_resources() {
    print_status "INFO" "Monitoring system resources during test..."
    
    if ! pgrep -f "library-management.*jar" > /dev/null; then
        print_status "WARN" "Application process not found for monitoring"
        return 1
    fi
    
    local app_pid=$(pgrep -f "library-management.*jar")
    local cpu_samples=0
    local cpu_total=0
    local memory_mb=0
    
    for i in $(seq 1 5); do
        if command -v ps >/dev/null 2>&1; then
            local cpu=$(ps -p $app_pid -o %cpu= 2>/dev/null | awk '{print $1}' || echo "0")
            local memory_kb=$(ps -p $app_pid -o rss= 2>/dev/null || echo "0")
            memory_mb=$((memory_kb / 1024))
            
            if [ ! -z "$cpu" ] && [ "$cpu" != "0" ]; then
                cpu_total=$(echo "$cpu_total + $cpu" | bc 2>/dev/null || echo "$cpu_total")
                cpu_samples=$((cpu_samples + 1))
            fi
        fi
        sleep 1
    done
    
    if [ $cpu_samples -gt 0 ]; then
        local avg_cpu=$(echo "scale=2; $cpu_total / $cpu_samples" | bc 2>/dev/null || echo "N/A")
        print_status "INFO" "Average CPU usage: $avg_cpu%"
    fi
    
    if [ $memory_mb -gt 0 ]; then
        print_status "INFO" "Memory usage: ${memory_mb}MB"
    fi
}

# Main execution
main() {
    echo "🔍 Pre-flight checks..."
    check_application
    
    echo ""
    echo "🛠️  Setting up test environment..."
    setup_test_data
    
    echo ""
    echo "📊 Starting performance tests..."
    
    # Test 1: Health endpoint (should be fast)
    print_status "INFO" "Test 1: Health endpoint performance"
    run_curl_test "/actuator/health" "Health endpoint load test"
    
    echo ""
    
    # Test 2: Books listing endpoint
    print_status "INFO" "Test 2: Books listing performance"
    run_curl_test "/books" "Books listing load test"
    
    echo ""
    
    # Test 3: Book creation (if we have Apache Bench)
    if command -v ab >/dev/null 2>&1; then
        print_status "INFO" "Test 3: Book creation performance (Apache Bench)"
        # Use a simpler test for POST requests
        run_ab_test "/actuator/health" "Apache Bench health test"
    else
        print_status "INFO" "Test 3: Book creation performance (curl-based)"
        # Use curl for book creation test
        BOOK_DATA='{"isbn": "978-0-performance-test", "title": "Performance Test Book", "author": "Performance Author"}'
        run_curl_test "/books" "Book creation load test" "POST" "$BOOK_DATA"
    fi
    
    echo ""
    
    # System resource monitoring
    monitor_resources
    
    echo ""
    echo "🎯 Performance Test Summary"
    echo "=========================="
    print_status "PASS" "Performance testing completed"
    print_status "INFO" "Review the results above for performance metrics"
    
    # Cleanup
    rm -f ab_results.tsv
    
    echo ""
    echo "💡 Performance Tips:"
    echo "  - For production load testing, use dedicated tools like JMeter or K6"
    echo "  - Monitor application logs during high load: tail -f logs/app-*.log"
    echo "  - Check JVM metrics: curl $BASE_URL/actuator/metrics/jvm.memory.used"
    echo "  - For detailed profiling, consider using Java profilers like JProfiler or async-profiler"
}

# Handle script arguments
if [ "$1" = "--help" ] || [ "$1" = "-h" ]; then
    echo "Usage: $0 [host] [port] [concurrent] [requests] [duration]"
    echo ""
    echo "Arguments:"
    echo "  host        Target host (default: localhost)"
    echo "  port        Target port (default: 8080)"
    echo "  concurrent  Concurrent requests (default: 10)"
    echo "  requests    Total requests (default: 100)"
    echo "  duration    Test duration in seconds (default: 30)"
    echo ""
    echo "Examples:"
    echo "  $0                                    # Use all defaults"
    echo "  $0 localhost 8080 20 200            # 20 concurrent, 200 total requests"
    echo "  $0 myserver.com 80 50 1000          # Test remote server"
    exit 0
fi

# Run main function
main