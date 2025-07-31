# Library Management System - Postman Collection

## Overview
This directory contains a comprehensive Postman collection for testing the Library Management System API. The collection includes all endpoints, error scenarios, and automated test scripts.

## Files
- `Library-Management-System.postman_collection.json` - Complete API collection
- `Library-Management-Development.postman_environment.json` - Development environment variables
- `README.md` - This documentation file

## Quick Start

### 1. Import Collection and Environment
1. Open Postman
2. Click **Import** button
3. Import both files:
   - `Library-Management-System.postman_collection.json`
   - `Library-Management-Development.postman_environment.json`

### 2. Set Environment
1. Select **Library Management - Development** environment from the dropdown
2. Verify the `baseUrl` is set to `http://localhost:8080`

### 3. Start the Application
Ensure the Library Management System is running:
```bash
cd "/path/to/Library Management System"
./mvnw spring-boot:run
```

### 4. Run Tests
- Use **Run Collection** for automated testing
- Or execute requests individually

## Collection Structure

### 📁 Borrower Management
- **Register New Borrower** - Create borrower accounts
- **Register Borrower - Duplicate Email Error** - Test email uniqueness
- **Register Borrower - Invalid Email Error** - Test email validation

### 📁 Book Management  
- **Register New Book** - Add books to library
- **Register Book - ISBN Consistency Error** - Test ISBN validation
- **Get All Books** - Retrieve library catalog

### 📁 Borrowing Operations
- **Borrow a Book** - Borrow available books
- **Borrow Book - Already Borrowed Error** - Test double borrowing
- **Borrow Book - Book Not Found Error** - Test invalid book ID
- **Borrow Book - Borrower Not Found Error** - Test invalid borrower ID

### 📁 Return Operations
- **Return a Book** - Return borrowed books
- **Return Book - Not Borrowed Error** - Test returning available books
- **Return Book - Book Not Found Error** - Test invalid book ID

### 📁 System Health & Info
- **Health Check** - Application health status
- **Application Info** - Build and system information
- **API Documentation** - Swagger UI access

## Environment Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `baseUrl` | API base URL | `http://localhost:8080` |
| `borrowerId` | Auto-set from borrower creation | `1` |
| `bookId` | Auto-set from book creation | `1` |
| `availableBookId` | Auto-set from book listing | `5` |
| `borrowedBookId` | Auto-set from borrowing operation | `3` |
| `testUserEmail` | Email for test user | `test.user@example.com` |
| `testUserName` | Name for test user | `Test User` |

## Automated Tests

Each request includes automated test scripts that verify:
- ✅ Correct HTTP status codes
- ✅ Response structure and data types  
- ✅ Business rule enforcement
- ✅ Error message format
- ✅ Response time performance (< 5000ms)
- ✅ Content-Type headers

### Test Reports
Run the collection to generate test reports showing:
- Pass/fail status for each test
- Response times
- Error details
- Coverage statistics

## Sample Data

The system includes pre-loaded test data:

### Borrowers (5 total)
- John Doe - john.doe@example.com
- Jane Smith - jane.smith@example.com  
- Bob Johnson - bob.johnson@example.com
- Alice Brown - alice.brown@example.com
- Charlie Wilson - charlie.wilson@example.com

### Books (13 total)
Popular programming books including:
- Effective Java - Joshua Bloch
- Clean Code - Robert C. Martin
- Design Patterns - Gang of Four
- The Pragmatic Programmer
- Head First Java
- And more...

## API Endpoints Summary

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/borrowers` | Register new borrower |
| POST | `/books` | Register new book |
| GET | `/books` | Get all books |
| POST | `/books/{bookId}/borrow` | Borrow a book |
| POST | `/books/{bookId}/return` | Return a book |
| GET | `/actuator/health` | Health check |
| GET | `/actuator/info` | Application info |
| GET | `/swagger-ui/index.html` | API documentation |

## Error Codes

| Code | Description | When It Occurs |
|------|-------------|----------------|
| 200 | OK | Successful operations |
| 201 | Created | Resource created successfully |
| 400 | Bad Request | Invalid input data, validation errors |
| 404 | Not Found | Resource doesn't exist |
| 409 | Conflict | Business rule violation (duplicate email, book already borrowed, etc.) |

## Testing Workflow

### Recommended Test Sequence:
1. **Health Check** - Verify system is running
2. **Register New Borrower** - Create test user (auto-sets `borrowerId`)
3. **Register New Book** - Add test book (auto-sets `bookId`)
4. **Get All Books** - View catalog (auto-sets `availableBookId`)
5. **Borrow a Book** - Test borrowing workflow (auto-sets `borrowedBookId`)
6. **Return a Book** - Test return workflow
7. **Error Scenarios** - Test all validation and business rules

### Advanced Testing:
- Run error scenario tests to validate proper error handling
- Use collection runner for automated regression testing
- Monitor response times and performance
- Verify data consistency across operations

## Troubleshooting

### Common Issues:

**"Connection refused" errors:**
- Ensure the application is running on localhost:8080
- Check the `baseUrl` environment variable

**404 errors on specific book/borrower IDs:**
- Run "Get All Books" to refresh available IDs
- Check that entities exist in the database
- Verify the correct environment variables are set

**409 Conflict errors:**
- These are expected for duplicate data tests
- Check business rules (email uniqueness, book availability)
- Verify test sequence is correct

**Test failures:**
- Check application logs for detailed error messages
- Verify database is accessible (H2 console at `/h2-console`)
- Ensure all migrations have run successfully

## Integration with CI/CD

This collection can be integrated into automated testing pipelines:

```bash
# Run collection with Newman (Postman CLI)
npm install -g newman
newman run Library-Management-System.postman_collection.json \
  -e Library-Management-Development.postman_environment.json \
  --reporters cli,json
```

## Additional Resources

- **API Documentation**: http://localhost:8080/swagger-ui/index.html
- **Health Monitoring**: http://localhost:8080/actuator/health
- **H2 Database Console**: http://localhost:8080/h2-console
- **Application Metrics**: http://localhost:8080/actuator/metrics

## Support

For issues with the API or this Postman collection:
1. Check the application logs
2. Verify the system is healthy via `/actuator/health`
3. Review the API documentation at `/swagger-ui/index.html`
4. Check the project's main README.md for setup instructions

---

**Generated for Library Management System API v1.0.0**  
*Last updated: 2025-07-31*