# Library Management System API

A comprehensive RESTful API for managing a library system that handles books and borrowers, including borrowing and returning processes.

## Table of Contents
- [Overview](#overview)
- [Technology Stack](#technology-stack)
- [Prerequisites](#prerequisites)
- [Installation & Setup](#installation--setup)
- [Configuration](#configuration)
- [API Documentation](#api-documentation)
- [Running the Application](#running-the-application)
- [Testing](#testing)
- [Monitoring & Health Checks](#monitoring--health-checks)
- [Production Deployment](#production-deployment)
- [API Examples](#api-examples)
- [Architecture](#architecture)
- [Design Decisions](#design-decisions)

## Overview

This Library Management System provides a robust backend API for:
- Managing borrowers with unique email validation
- Managing books with ISBN consistency checks
- Tracking book borrowing and returns
- Supporting multiple copies of the same book
- Comprehensive validation and error handling

## Technology Stack

- **Java**: 21 (LTS)
- **Framework**: Spring Boot 3.5.4
- **Build Tool**: Maven 3.9.10
- **Database**: H2 (development), PostgreSQL (production)
- **ORM**: Spring Data JPA with Hibernate
- **Documentation**: SpringDoc OpenAPI 3 (Swagger)
- **Testing**: JUnit 5, Mockito, Spring Boot Test
- **Monitoring**: Spring Boot Actuator, Micrometer

## Prerequisites

- Java 21 or higher
- Maven 3.6 or higher
- PostgreSQL 12+ (for production)
- Git

## Installation & Setup

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd "Library Management System"
   ```

2. **Build the project**
   ```bash
   ./mvnw clean compile
   ```

3. **Run tests**
   ```bash
   ./mvnw test
   ```

4. **Start the application**
   ```bash
   ./mvnw spring-boot:run
   ```

The application will start on `http://localhost:8080`

## Configuration

### Environment Profiles

The application supports three profiles:

#### Development (default)
- H2 in-memory database
- SQL logging enabled
- H2 console available at `/h2-console`
- Detailed logging and debugging

#### Test
- Isolated H2 database for testing
- Optimized connection pooling
- Minimal logging

#### Production
- PostgreSQL database
- Connection pooling with HikariCP
- Comprehensive monitoring and metrics
- Optimized performance settings

### Environment Variables

For production deployment, set these environment variables:

```bash
# Database Configuration
DB_URL=jdbc:postgresql://localhost:5432/library_db
DB_USERNAME=library_user
DB_PASSWORD=library_password

# Server Configuration
SERVER_PORT=8080
MANAGEMENT_PORT=8081

# Spring Profile
SPRING_PROFILES_ACTIVE=prod
```

## API Documentation

### Interactive API Documentation
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI Spec**: `http://localhost:8080/v3/api-docs`

### API Endpoints
____________________________________________________________________
| Method | Endpoint                 | Description                  |
|--------|--------------------------|------------------------------|
| POST   | `/borrowers`             | Register a new borrower      |
| POST   | `/books`                 | Register a new book          |
| GET    | `/books`                 | Get all books in the library |
| POST   | `/books/{bookId}/borrow` | Borrow a book                |
| POST   | `/books/{bookId}/return` | Return a book                |
____________________________________________________________________

### Data Models

#### Borrower
```json
{
  "name": "John Doe",
  "email": "john.doe@example.com"
}
```

#### Book
```json
{
  "isbn": "978-0-123456-78-9",
  "title": "Sample Book Title",
  "author": "Author Name"
}
```

#### Borrow Request
```json
{
  "borrowerId": 1
}
```

## Running the Application

### Development Mode
```bash
./mvnw spring-boot:run
```
Access at: `http://localhost:8080`

### Production Mode
```bash
export SPRING_PROFILES_ACTIVE=prod
./mvnw spring-boot:run
```

### Using JAR File
```bash
./mvnw clean package
java -jar target/LibraryManagement-0.0.1-SNAPSHOT.jar
```

## Testing

### Run All Tests
```bash
./mvnw test
```

### Test Coverage
The project includes comprehensive testing:
- **Unit Tests**: 162 total tests
- **Integration Tests**: 8 end-to-end scenarios
- **Repository Tests**: 28 data layer tests
- **Service Tests**: 93 business logic tests
- **Controller Tests**: 33 API layer tests

### Test Types
- **Unit Tests**: Isolated component testing with mocks
- **Integration Tests**: Full application context testing
- **Repository Tests**: Database interaction testing
- **Controller Tests**: REST API endpoint testing

## Monitoring & Health Checks

### Health Endpoints
- **Application Health**: `GET /actuator/health`
- **Detailed Health**: `GET /actuator/health/library`
- **System Info**: `GET /actuator/info`
- **Metrics**: `GET /actuator/metrics`

### Custom Health Indicators
The application includes a custom health indicator that monitors:
- Database connectivity
- Total books and borrowers count
- Available vs borrowed books ratio

### Monitoring in Production
- Prometheus metrics available at `/actuator/prometheus`
- Custom metrics for API response times
- Database connection pool monitoring
- JVM and system metrics

## Production Deployment

### Database Setup
1. **Install PostgreSQL**
2. **Create database and user**
   ```sql
   CREATE DATABASE library_db;
   CREATE USER library_user WITH PASSWORD 'library_password';
   GRANT ALL PRIVILEGES ON DATABASE library_db TO library_user;
   ```

### Deployment Steps
1. **Package the application**
   ```bash
   ./mvnw clean package -Pprod
   ```

2. **Set environment variables**
3. **Run with production profile**
   ```bash
   java -jar -Dspring.profiles.active=prod target/LibraryManagement-0.0.1-SNAPSHOT.jar
   ```

### Production Considerations
- Configure reverse proxy (nginx/Apache)
- Set up SSL/TLS certificates
- Configure log rotation
- Set up monitoring and alerting
- Database backup strategy
- Load balancing for high availability

## API Examples

### Register a Borrower
```bash
curl -X POST http://localhost:8080/borrowers \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john.doe@example.com"
  }'
```

### Register a Book
```bash
curl -X POST http://localhost:8080/books \
  -H "Content-Type: application/json" \
  -d '{
    "isbn": "978-0-123456-78-9",
    "title": "The Great Book",
    "author": "Jane Author"
  }'
```

### Get All Books
```bash
curl -X GET http://localhost:8080/books
```

### Borrow a Book
```bash
curl -X POST http://localhost:8080/books/1/borrow \
  -H "Content-Type: application/json" \
  -d '{
    "borrowerId": 1
  }'
```

### Return a Book
```bash
curl -X POST http://localhost:8080/books/1/return \
  -H "Content-Type: application/json" \
  -d '{}'
```

### Error Response Example
```json
{
  "code": "BOOK_ALREADY_BORROWED",
  "message": "Book with ID 1 is already borrowed by another user",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

## Business Rules

1. **Email Uniqueness**: Each borrower must have a unique email address
2. **ISBN Consistency**: Books with the same ISBN must have identical title and author
3. **Book Availability**: Only available books can be borrowed
4. **Single Borrower**: Each book copy can only be borrowed by one borrower at a time
5. **Multiple Copies**: Multiple copies of the same book (same ISBN) are supported

## Error Handling

The API provides comprehensive error handling with appropriate HTTP status codes:

- **200 OK**: Successful operations
- **201 Created**: Resource created successfully
- **400 Bad Request**: Invalid input data or validation errors
- **404 Not Found**: Resource not found
- **409 Conflict**: Business rule violations

## Troubleshooting

### Common Issues

1. **Port Already in Use**
   ```bash
   # Change port in application.yml or use environment variable
   export SERVER_PORT=8081
   ```

2. **Database Connection Issues**
   - Verify PostgreSQL is running
   - Check connection parameters
   - Ensure database and user exist

3. **H2 Console Not Accessible**
   - Ensure development profile is active
   - Check `spring.h2.console.enabled=true`

### Logs Location
- Development: Console output
- Production: `logs/library-management.log`

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For support and questions:
- Check the API documentation at `/swagger-ui.html`
- Review the health status at `/actuator/health`
- Check application logs for detailed error information

## Architecture

### System Architecture

The Library Management System follows a **layered architecture pattern** with clear separation of concerns:

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   REST API      │    │   Service       │    │   Data Access   │
│   Controllers   │───▶│   Layer         │───▶│   Repository    │
│                 │    │   (Business     │    │   Layer         │
│                 │    │    Logic)       │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                                        │
                                                        ▼
                                               ┌─────────────────┐
                                               │   Database      │
                                               │   (H2/          │
                                               │   PostgreSQL)   │
                                               └─────────────────┘
```

### Layer Responsibilities

#### 1. Presentation Layer (Controllers)
- **Package**: `com.library.controller`
- **Responsibilities**:
  - REST endpoint definitions
  - Request/response mapping (DTOs)
  - Input validation using Bean Validation
  - HTTP status code handling
  - Exception handling delegation

#### 2. Service Layer (Business Logic)
- **Package**: `com.library.service` & `com.library.service.impl`
- **Responsibilities**:
  - Core business rules enforcement
  - Transaction management with `@Transactional`
  - Data transformation and validation
  - Cross-cutting concerns

#### 3. Data Access Layer (Repositories)
- **Package**: `com.library.repository`
- **Responsibilities**:
  - Database operations using Spring Data JPA
  - Entity mapping and relationships
  - Custom queries with JPQL
  - Data persistence

#### 4. Domain Layer (Entities & DTOs)
- **Packages**: `com.library.entity`, `com.library.dto`
- **Responsibilities**:
  - Core business entities with JPA annotations
  - Data validation rules
  - Entity relationships and constraints
  - Request/Response data transfer objects

### Key Components

#### Configuration Components
- **OpenApiConfig**: API documentation configuration
- **ProductionConfig**: Production-specific configurations
- **LibraryHealthIndicator**: Custom health checks

#### Exception Handling
- **GlobalExceptionHandler**: Centralized exception handling
- **Consistent Error Responses**: Standardized error format across all endpoints

#### Monitoring & Observability
- **Spring Boot Actuator**: Health checks, metrics, and monitoring
- **Custom Health Indicators**: Application-specific health checks
- **Comprehensive Logging**: Structured logging with different levels per environment

## Design Decisions

### 1. **Layered Architecture Pattern**
**Decision**: Implement a clean layered architecture with clear separation of concerns.
**Rationale**: 
- Promotes maintainability and testability
- Enables independent development and testing of each layer
- Follows SOLID principles and dependency inversion
- Supports future scalability and modifications

### 2. **Spring Boot Framework**
**Decision**: Use Spring Boot 3.5.4 as the primary framework.
**Rationale**:
- Provides comprehensive ecosystem for enterprise applications
- Auto-configuration reduces boilerplate code
- Built-in support for REST APIs, security, and data access
- Excellent testing support and extensive documentation

### 3. **JPA/Hibernate for Data Access**
**Decision**: Use Spring Data JPA with Hibernate as the ORM.
**Rationale**:
- Simplifies database operations with repository pattern
- Automatic query generation for common operations
- Support for custom queries with JPQL
- Database-agnostic approach for future migrations

### 4. **H2 for Development, PostgreSQL for Production**
**Decision**: Use H2 in-memory database for development/testing, PostgreSQL for production.
**Rationale**:
- H2 enables fast development and testing cycles
- PostgreSQL provides production-grade performance and features
- Environment-specific configurations support smooth transitions

### 5. **DTO Pattern for API Layer**
**Decision**: Use separate DTOs for request/response instead of exposing entities directly.
**Rationale**:
- Prevents over-fetching and under-fetching of data
- Provides API versioning flexibility
- Enables input validation at the API boundary
- Decouples internal model from external API contract

### 6. **Global Exception Handling**
**Decision**: Implement centralized exception handling with `@RestControllerAdvice`.
**Rationale**:
- Ensures consistent error response format
- Reduces code duplication across controllers
- Provides proper HTTP status code mapping
- Enables comprehensive error logging

### 7. **Bean Validation for Input Validation**
**Decision**: Use Jakarta Bean Validation for request validation.
**Rationale**:
- Declarative validation with annotations
- Consistent validation across the application
- Automatic error message generation
- Integration with Spring Boot's error handling

### 8. **Custom Business Logic in Service Layer**
**Decision**: Implement all business rules in the service layer, not in controllers or repositories.
**Rationale**:
- Single responsibility principle compliance
- Enables easy unit testing with mocks
- Supports transaction management
- Promotes code reusability

### 9. **Comprehensive Testing Strategy**
**Decision**: Implement unit tests, integration tests, and controller tests with high coverage (80%+).
**Rationale**:
- Ensures code quality and reliability
- Enables confident refactoring and feature additions
- Documents expected behavior through tests
- Supports continuous integration practices

### 10. **Environment-Specific Configuration**
**Decision**: Use Spring profiles (dev, test, prod) for environment-specific configurations.
**Rationale**:
- Supports different database configurations per environment
- Enables environment-specific logging levels
- Facilitates smooth deployment across environments
- Maintains security by separating production credentials

### 11. **Actuator for Monitoring**
**Decision**: Integrate Spring Boot Actuator with custom health indicators.
**Rationale**:
- Provides out-of-the-box monitoring endpoints
- Enables health checks for external monitoring systems
- Supports application metrics and diagnostics
- Facilitates production troubleshooting

### 12. **OpenAPI/Swagger Documentation**
**Decision**: Use SpringDoc OpenAPI for automatic API documentation generation.
**Rationale**:
- Generates interactive API documentation
- Keeps documentation in sync with code
- Provides testing interface for developers
- Supports API contract validation

These architectural and design decisions ensure the Library Management System is:
- **Maintainable**: Clear separation of concerns and well-defined boundaries
- **Testable**: Comprehensive test coverage with proper mocking strategies
- **Scalable**: Layered architecture supports horizontal and vertical scaling
- **Secure**: Input validation, proper error handling, and production-ready configurations
- **Observable**: Comprehensive logging, health checks, and monitoring capabilities