-- Library Management System - Database Initialization Script
-- This script sets up the PostgreSQL database for the application

-- Ensure the database exists (this is usually handled by Docker)
-- CREATE DATABASE library_db;

-- Connect to the library database
\c library_db;

-- Create extensions if needed
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Grant necessary permissions to the application user
GRANT ALL PRIVILEGES ON DATABASE library_db TO library_user;
GRANT ALL PRIVILEGES ON SCHEMA public TO library_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO library_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO library_user;

-- Set default privileges for future objects
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO library_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO library_user;

-- Optional: Create some initial data for testing
-- Note: The application will create the tables automatically via JPA/Hibernate

-- You can uncomment the following lines to insert sample data:
/*
-- Insert sample borrowers
INSERT INTO borrowers (name, email, created_at, updated_at) VALUES 
    ('John Doe', 'john.doe@example.com', NOW(), NOW()),
    ('Jane Smith', 'jane.smith@example.com', NOW(), NOW()),
    ('Bob Johnson', 'bob.johnson@example.com', NOW(), NOW())
ON CONFLICT (email) DO NOTHING;

-- Insert sample books
INSERT INTO books (isbn, title, author, created_at, updated_at) VALUES 
    ('978-0-321-35668-0', 'Effective Java', 'Joshua Bloch', NOW(), NOW()),
    ('978-0-13-468599-1', 'The Clean Coder', 'Robert C. Martin', NOW(), NOW()),
    ('978-0-201-63361-0', 'Design Patterns', 'Gang of Four', NOW(), NOW()),
    ('978-0-596-52068-7', 'Head First Design Patterns', 'Eric Freeman', NOW(), NOW()),
    ('978-0-321-12742-6', 'Refactoring: Improving the Design of Existing Code', 'Martin Fowler', NOW(), NOW())
ON CONFLICT (isbn) DO NOTHING;
*/

-- Create indexes for better performance (if not created by JPA)
-- These will be created automatically by Hibernate, but you can customize them here
/*
CREATE INDEX IF NOT EXISTS idx_borrowers_email ON borrowers(email);
CREATE INDEX IF NOT EXISTS idx_books_isbn ON books(isbn);
CREATE INDEX IF NOT EXISTS idx_books_borrower_id ON books(borrower_id);
CREATE INDEX IF NOT EXISTS idx_books_created_at ON books(created_at);
CREATE INDEX IF NOT EXISTS idx_borrowers_created_at ON borrowers(created_at);
*/

-- Log successful initialization
\echo 'Database initialization completed successfully!'