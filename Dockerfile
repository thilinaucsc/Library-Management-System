# Library Management System - Docker Configuration
# Multi-stage build for optimized production image

# Build stage
FROM maven:3.9.6-openjdk-21-slim AS build

# Set working directory
WORKDIR /app

# Copy Maven configuration files
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests -B

# Runtime stage
FROM openjdk:21-jre-slim

# Install curl for health checks
RUN apt-get update && \
    apt-get install -y curl && \
    rm -rf /var/lib/apt/lists/*

# Create non-root user for security
RUN groupadd -r library && useradd -r -g library library

# Set working directory
WORKDIR /app

# Copy the JAR file from build stage
COPY --from=build /app/target/library-management-*.jar app.jar

# Create logs directory and set permissions
RUN mkdir -p /app/logs && \
    chown -R library:library /app

# Switch to non-root user
USER library

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Environment variables with defaults
ENV SPRING_PROFILES_ACTIVE=prod
ENV SERVER_PORT=8080
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]