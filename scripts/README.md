# Library Management System - Deployment Scripts

This directory contains deployment and management scripts for the Library Management System API.

## Scripts Overview

### 🚀 Deployment Scripts

#### `deploy-dev.sh`
Deploys the application in development mode with H2 database.

**Usage:**
```bash
./scripts/deploy-dev.sh
```

**Features:**
- Builds the project automatically
- Uses H2 in-memory database
- Starts application on port 8080
- Provides easy access to H2 console
- Optimized for development workflow

**Environment:**
- Profile: `dev`
- Database: H2 in-memory
- Port: 8080
- JVM Options: `-Xms256m -Xmx512m`

#### `deploy-prod.sh`
Deploys the application in production mode with PostgreSQL database.

**Usage:**
```bash
# Set required environment variables
export DB_USERNAME="your_db_user"
export DB_PASSWORD="your_db_password"
export DB_HOST="localhost"
export DB_PORT="5432"
export DB_NAME="library_db"

./scripts/deploy-prod.sh
```

**Features:**
- Runs full test suite before deployment
- Validates environment variables
- Graceful shutdown of existing instances
- Creates automatic backups
- Production-optimized JVM settings
- Supports systemd integration

**Environment:**
- Profile: `prod`
- Database: PostgreSQL
- Port: 8080 (configurable via SERVER_PORT)
- JVM Options: `-Xms512m -Xmx1024m -XX:+UseG1GC`

**Required Environment Variables:**
- `DB_USERNAME`: PostgreSQL username
- `DB_PASSWORD`: PostgreSQL password

**Optional Environment Variables:**
- `DB_HOST`: Database host (default: localhost)
- `DB_PORT`: Database port (default: 5432)
- `DB_NAME`: Database name (default: library_db)
- `SERVER_PORT`: Application port (default: 8080)

### 🛑 Management Scripts

#### `stop.sh`
Gracefully stops the running application.

**Usage:**
```bash
./scripts/stop.sh
```

**Features:**
- Attempts graceful shutdown first (SIGTERM)
- Falls back to force kill if needed (SIGKILL)
- Works with both background processes and systemd
- Cleans up PID files

#### `health-check.sh`
Performs comprehensive health checks on the running application.

**Usage:**
```bash
# Check localhost:8080
./scripts/health-check.sh

# Check specific host and port
./scripts/health-check.sh myserver.com 80

# With custom timeout
./scripts/health-check.sh localhost 8080 30
```

**Checks Performed:**
- ✅ Basic connectivity
- ✅ Health endpoint status
- ✅ API endpoints availability
- ✅ Process existence
- ✅ Memory usage
- ✅ Response time
- ✅ Documentation accessibility

**Exit Codes:**
- `0`: All checks passed
- `1`: Some checks failed but success rate ≥ 80%
- `2`: Multiple failures, success rate < 80%

#### `performance-test.sh`
Runs basic performance and load testing.

**Usage:**
```bash
# Basic performance test
./scripts/performance-test.sh

# Custom configuration
./scripts/performance-test.sh localhost 8080 20 200

# Help
./scripts/performance-test.sh --help
```

**Features:**
- Concurrent request testing
- Response time measurement
- Success rate calculation
- System resource monitoring
- Support for Apache Bench (ab) if available
- Fallback to curl-based testing

**Test Scenarios:**
1. Health endpoint performance
2. Books listing performance  
3. Book creation performance
4. System resource monitoring

## Prerequisites

### For All Scripts
- Java 21+ installed and in PATH
- Maven 3.9+ installed
- curl command available

### For Development Deployment
- No additional requirements (uses H2)

### For Production Deployment
- PostgreSQL server running and accessible
- Database user with appropriate permissions
- Environment variables configured

### For Performance Testing (Optional)
- Apache Bench (`ab`) for enhanced load testing
  ```bash
  # Ubuntu/Debian
  sudo apt-get install apache2-utils
  
  # CentOS/RHEL
  sudo yum install httpd-tools
  
  # macOS
  brew install httpd
  ```
- `jq` for JSON parsing (optional but recommended)
  ```bash
  # Ubuntu/Debian
  sudo apt-get install jq
  
  # CentOS/RHEL
  sudo yum install jq
  
  # macOS
  brew install jq
  ```
- `bc` for calculations (usually pre-installed)

## Directory Structure

After running the scripts, the following directories will be created:

```
├── scripts/           # Deployment scripts
├── logs/             # Application logs
│   ├── app-dev.log   # Development logs
│   └── app-prod.log  # Production logs
├── backups/          # JAR backups (production)
└── target/           # Maven build artifacts
```

## Usage Examples

### Complete Development Workflow
```bash
# 1. Deploy in development mode
./scripts/deploy-dev.sh

# 2. Check health
./scripts/health-check.sh

# 3. Run performance tests
./scripts/performance-test.sh

# 4. Stop application
./scripts/stop.sh
```

### Production Deployment Workflow
```bash
# 1. Set environment variables
export DB_USERNAME="library_user"
export DB_PASSWORD="secure_password"
export DB_HOST="db.example.com"
export DB_NAME="library_production"

# 2. Deploy in production mode
./scripts/deploy-prod.sh

# 3. Verify deployment
./scripts/health-check.sh

# 4. Run load tests (optional)
./scripts/performance-test.sh localhost 8080 50 1000
```

### Monitoring and Maintenance
```bash
# Check application status
./scripts/health-check.sh

# View logs
tail -f logs/app-prod.log

# Stop for maintenance
./scripts/stop.sh

# Restart after maintenance
./scripts/deploy-prod.sh
```

## Systemd Integration (Optional)

For production environments, you can integrate with systemd:

1. Create service file `/etc/systemd/system/library-management.service`:
```ini
[Unit]
Description=Library Management System API
After=network.target

[Service]
Type=simple
User=library
Group=library
WorkingDirectory=/opt/library-management
ExecStart=/usr/bin/java -jar target/library-management-0.0.1-SNAPSHOT.jar
ExecStop=/bin/kill -TERM $MAINPID
Restart=on-failure
RestartSec=10
Environment=SPRING_PROFILES_ACTIVE=prod
Environment=DB_USERNAME=library_user
Environment=DB_PASSWORD=secure_password

[Install]
WantedBy=multi-user.target
```

2. Enable and start the service:
```bash
sudo systemctl daemon-reload
sudo systemctl enable library-management
sudo systemctl start library-management
```

The deployment scripts will automatically detect and use systemd if available.

## Troubleshooting

### Common Issues

1. **Port already in use**
   ```bash
   # Find process using port 8080
   sudo lsof -i :8080
   
   # Kill the process or use a different port
   export SERVER_PORT=8081
   ```

2. **Database connection failed**
   ```bash
   # Check PostgreSQL is running
   sudo systemctl status postgresql
   
   # Test connection manually
   psql -h $DB_HOST -p $DB_PORT -U $DB_USERNAME -d $DB_NAME
   ```

3. **Out of memory errors**
   ```bash
   # Increase JVM memory in the script
   export JAVA_OPTS="-Xms512m -Xmx2048m"
   ```

4. **Permission denied**
   ```bash
   # Make scripts executable
   chmod +x scripts/*.sh
   
   # Check file ownership
   ls -la scripts/
   ```

### Log Files

- **Development**: `logs/app-dev.log`
- **Production**: `logs/app-prod.log`
- **System logs**: `sudo journalctl -u library-management -f`

### Health Monitoring

The health check script provides detailed diagnostics:
```bash
# Verbose health check
./scripts/health-check.sh localhost 8080 30
```

### Performance Monitoring

Monitor application performance:
```bash
# Check JVM metrics
curl http://localhost:8080/actuator/metrics/jvm.memory.used

# Check custom health indicators
curl http://localhost:8080/actuator/health

# Database statistics
curl http://localhost:8080/actuator/health | jq '.components.library'
```

## Security Considerations

1. **Environment Variables**: Never commit passwords to version control
2. **File Permissions**: Ensure scripts have appropriate permissions
3. **Network Security**: Configure firewalls for production deployments
4. **Database Security**: Use strong passwords and limit database user permissions
5. **Log Security**: Ensure log files don't contain sensitive information

## Support

For issues with the deployment scripts:
1. Check the application logs
2. Run health checks for diagnostics
3. Verify all prerequisites are installed
4. Check environment variable configuration
5. Review the troubleshooting section above