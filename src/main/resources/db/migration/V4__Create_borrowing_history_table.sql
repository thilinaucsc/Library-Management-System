-- Create borrowing_history table to track all borrowing and returning events
CREATE TABLE borrowing_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    book_id BIGINT NOT NULL,
    borrower_id BIGINT NOT NULL,
    action_type VARCHAR(20) NOT NULL,
    action_date TIMESTAMP NOT NULL,
    due_date TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key constraints
    CONSTRAINT fk_borrowing_history_book 
        FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
    CONSTRAINT fk_borrowing_history_borrower 
        FOREIGN KEY (borrower_id) REFERENCES borrowers(id) ON DELETE CASCADE,
    
    -- Check constraint for action_type
    CONSTRAINT chk_action_type CHECK (action_type IN ('BORROWED', 'RETURNED'))
);

-- Create indexes for better query performance
CREATE INDEX idx_borrowing_history_book_id ON borrowing_history(book_id);
CREATE INDEX idx_borrowing_history_borrower_id ON borrowing_history(borrower_id);
CREATE INDEX idx_borrowing_history_action_date ON borrowing_history(action_date);
CREATE INDEX idx_borrowing_history_action_type ON borrowing_history(action_type);
CREATE INDEX idx_borrowing_history_due_date ON borrowing_history(due_date);

-- Composite indexes for common queries
CREATE INDEX idx_borrowing_history_borrower_action ON borrowing_history(borrower_id, action_type, action_date);
CREATE INDEX idx_borrowing_history_book_action ON borrowing_history(book_id, action_type, action_date);