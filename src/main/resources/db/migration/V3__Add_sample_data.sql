-- Library Management System - Database Migration V3
-- Add sample data for testing and demonstration

-- Insert sample borrowers (H2 compatible - simple INSERT)
INSERT INTO borrowers (name, email, created_at, updated_at) VALUES 
    ('John Doe', 'john.doe@example.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Jane Smith', 'jane.smith@example.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Bob Johnson', 'bob.johnson@example.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Alice Brown', 'alice.brown@example.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Charlie Wilson', 'charlie.wilson@example.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert sample books (H2 compatible)
INSERT INTO books (isbn, title, author, created_at, updated_at) VALUES 
    ('978-0-321-35668-0', 'Effective Java', 'Joshua Bloch', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('978-0-13-468599-1', 'The Clean Coder: A Code of Conduct for Professional Programmers', 'Robert C. Martin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('978-0-201-63361-0', 'Design Patterns: Elements of Reusable Object-Oriented Software', 'Erich Gamma, Richard Helm, Ralph Johnson, John Vlissides', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('978-0-596-52068-7', 'Head First Design Patterns', 'Eric Freeman, Elisabeth Robson', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('978-0-321-12742-6', 'Refactoring: Improving the Design of Existing Code', 'Martin Fowler', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('978-0-13-110362-7', 'The Pragmatic Programmer: From Journeyman to Master', 'Andrew Hunt, David Thomas', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('978-0-596-00736-6', 'Head First Java', 'Kathy Sierra, Bert Bates', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('978-0-321-33336-1', 'Java Concurrency in Practice', 'Brian Goetz', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('978-0-13-235088-4', 'Clean Code: A Handbook of Agile Software Craftsmanship', 'Robert C. Martin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('978-0-201-31012-9', 'The Mythical Man-Month: Essays on Software Engineering', 'Frederick P. Brooks Jr.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    
    -- Add multiple copies of popular books
    ('978-0-321-35668-0', 'Effective Java', 'Joshua Bloch', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('978-0-13-468599-1', 'The Clean Coder: A Code of Conduct for Professional Programmers', 'Robert C. Martin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('978-0-201-63361-0', 'Design Patterns: Elements of Reusable Object-Oriented Software', 'Erich Gamma, Richard Helm, Ralph Johnson, John Vlissides', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Note: We don't set any books as borrowed initially
-- This allows for a clean starting state where all books are available