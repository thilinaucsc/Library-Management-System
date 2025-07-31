-- Library Management System - Database Migration V2
-- Create books table

CREATE TABLE books (
    id BIGSERIAL PRIMARY KEY,
    isbn VARCHAR(20) NOT NULL,
    title VARCHAR(500) NOT NULL,
    author VARCHAR(200) NOT NULL,
    borrower_id BIGINT,
    borrowed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key constraint
    CONSTRAINT fk_books_borrower FOREIGN KEY (borrower_id) REFERENCES borrowers(id) ON DELETE SET NULL
);

-- Create indexes for better performance
CREATE INDEX idx_books_isbn ON books(isbn);
CREATE INDEX idx_books_borrower_id ON books(borrower_id);
CREATE INDEX idx_books_created_at ON books(created_at);
CREATE INDEX idx_books_title ON books(title);
CREATE INDEX idx_books_author ON books(author);

-- Create regular indexes (H2 compatible)
-- Note: Partial indexes are PostgreSQL-specific and not supported by H2
CREATE INDEX idx_books_available ON books(borrower_id);
CREATE INDEX idx_books_borrowed ON books(borrower_id, borrowed_at);

-- Add comments for documentation
COMMENT ON TABLE books IS 'Individual book copies in the library';
COMMENT ON COLUMN books.id IS 'Unique identifier for this book copy';
COMMENT ON COLUMN books.isbn IS 'International Standard Book Number';
COMMENT ON COLUMN books.title IS 'Title of the book';
COMMENT ON COLUMN books.author IS 'Author of the book';
COMMENT ON COLUMN books.borrower_id IS 'ID of borrower who has this book (NULL if available)';
COMMENT ON COLUMN books.borrowed_at IS 'Timestamp when book was borrowed';
COMMENT ON COLUMN books.created_at IS 'Timestamp when book was added to library';
COMMENT ON COLUMN books.updated_at IS 'Timestamp when book record was last updated';