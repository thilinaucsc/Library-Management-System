package com.library.service;

import com.library.entity.BorrowingHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BorrowingHistoryService {

    /**
     * Get all borrowing history for a specific book
     */
    List<BorrowingHistory> getHistoryByBookId(Long bookId);

    /**
     * Get all borrowing history for a specific borrower
     */
    List<BorrowingHistory> getHistoryByBorrowerId(Long borrowerId);

    /**
     * Get paginated borrowing history for a specific borrower
     */
    Page<BorrowingHistory> getHistoryByBorrowerId(Long borrowerId, Pageable pageable);

    /**
     * Get paginated borrowing history for a specific book
     */
    Page<BorrowingHistory> getHistoryByBookId(Long bookId, Pageable pageable);

    /**
     * Get currently borrowed books by a borrower
     */
    List<BorrowingHistory> getCurrentlyBorrowedBooksByBorrower(Long borrowerId);

    /**
     * Get overdue books for a specific borrower
     */
    List<BorrowingHistory> getOverdueBooksByBorrower(Long borrowerId);

    /**
     * Get all overdue books in the system
     */
    List<BorrowingHistory> getAllOverdueBooks();

    /**
     * Get the most recent borrowing record for a book
     */
    Optional<BorrowingHistory> getMostRecentHistoryForBook(Long bookId);

    /**
     * Get borrowing history within a date range
     */
    List<BorrowingHistory> getHistoryByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Get borrowing history for a borrower within a date range
     */
    List<BorrowingHistory> getHistoryByBorrowerAndDateRange(Long borrowerId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Get borrowing history for a book within a date range
     */
    List<BorrowingHistory> getHistoryByBookAndDateRange(Long bookId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Get total number of times a borrower has borrowed books
     */
    long getTotalBorrowingsByBorrower(Long borrowerId);

    /**
     * Get total number of times a book has been borrowed
     */
    long getTotalBorrowingsForBook(Long bookId);

    /**
     * Get most popular books (most borrowed)
     */
    List<Object[]> getMostPopularBooks(int limit);

    /**
     * Get most active borrowers
     */
    List<Object[]> getMostActiveBorrowers(int limit);

    /**
     * Check if a borrower has any overdue books
     */
    boolean hasOverdueBooks(Long borrowerId);

    /**
     * Get the number of books currently borrowed by a borrower
     */
    long getCurrentBorrowingCount(Long borrowerId);
}