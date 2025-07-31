-- Library Management System - Database Migration V1
-- Create borrowers table

CREATE TABLE borrowers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create index on email for faster lookups
CREATE INDEX idx_borrowers_email ON borrowers(email);

-- Create index on created_at for sorting/filtering
CREATE INDEX idx_borrowers_created_at ON borrowers(created_at);

-- Add comments for documentation
COMMENT ON TABLE borrowers IS 'Library borrowers/users who can borrow books';
COMMENT ON COLUMN borrowers.id IS 'Unique identifier for borrower';
COMMENT ON COLUMN borrowers.name IS 'Full name of the borrower';
COMMENT ON COLUMN borrowers.email IS 'Unique email address for the borrower';
COMMENT ON COLUMN borrowers.created_at IS 'Timestamp when borrower was created';
COMMENT ON COLUMN borrowers.updated_at IS 'Timestamp when borrower was last updated';