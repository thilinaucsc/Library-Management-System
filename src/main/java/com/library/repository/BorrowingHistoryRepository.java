package com.library.repository;

import com.library.entity.BorrowingHistory;
import com.library.entity.BorrowingHistory.ActionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BorrowingHistoryRepository extends JpaRepository<BorrowingHistory, Long> {

    // Find all history for a specific book
    List<BorrowingHistory> findByBookIdOrderByActionDateDesc(Long bookId);

    // Find all history for a specific borrower
    List<BorrowingHistory> findByBorrowerIdOrderByActionDateDesc(Long borrowerId);

    // Find paginated history for a specific borrower
    Page<BorrowingHistory> findByBorrowerIdOrderByActionDateDesc(Long borrowerId, Pageable pageable);

    // Find all history for a specific book with pagination
    Page<BorrowingHistory> findByBookIdOrderByActionDateDesc(Long bookId, Pageable pageable);

    // Find all borrowing records (not returns) for a specific borrower
    List<BorrowingHistory> findByBorrowerIdAndActionTypeOrderByActionDateDesc(Long borrowerId, ActionType actionType);

    // Find all borrowing records for a specific book
    List<BorrowingHistory> findByBookIdAndActionTypeOrderByActionDateDesc(Long bookId, ActionType actionType);

    // Find currently borrowed books (books that were borrowed but not yet returned)
    @Query("SELECT bh FROM BorrowingHistory bh WHERE bh.borrower.id = :borrowerId AND bh.actionType = 'BORROWED' " +
           "AND NOT EXISTS (SELECT 1 FROM BorrowingHistory bh2 WHERE bh2.book.id = bh.book.id " +
           "AND bh2.borrower.id = bh.borrower.id AND bh2.actionType = 'RETURNED' AND bh2.actionDate > bh.actionDate)")
    List<BorrowingHistory> findCurrentlyBorrowedBooksByBorrower(@Param("borrowerId") Long borrowerId);

    // Find overdue books for a specific borrower
    @Query("SELECT bh FROM BorrowingHistory bh WHERE bh.borrower.id = :borrowerId AND bh.actionType = 'BORROWED' " +
           "AND bh.dueDate < :currentDate " +
           "AND NOT EXISTS (SELECT 1 FROM BorrowingHistory bh2 WHERE bh2.book.id = bh.book.id " +
           "AND bh2.borrower.id = bh.borrower.id AND bh2.actionType = 'RETURNED' AND bh2.actionDate > bh.actionDate)")
    List<BorrowingHistory> findOverdueBorrowingsByBorrower(@Param("borrowerId") Long borrowerId, 
                                                           @Param("currentDate") LocalDateTime currentDate);

    // Find all overdue books
    @Query("SELECT bh FROM BorrowingHistory bh WHERE bh.actionType = 'BORROWED' " +
           "AND bh.dueDate < :currentDate " +
           "AND NOT EXISTS (SELECT 1 FROM BorrowingHistory bh2 WHERE bh2.book.id = bh.book.id " +
           "AND bh2.borrower.id = bh.borrower.id AND bh2.actionType = 'RETURNED' AND bh2.actionDate > bh.actionDate)")
    List<BorrowingHistory> findAllOverdueBorrowings(@Param("currentDate") LocalDateTime currentDate);

    // Find the most recent borrowing record for a specific book
    Optional<BorrowingHistory> findFirstByBookIdOrderByActionDateDesc(Long bookId);

    // Find borrowing history within a date range
    List<BorrowingHistory> findByActionDateBetweenOrderByActionDateDesc(LocalDateTime startDate, LocalDateTime endDate);

    // Find borrowing history for a specific borrower within a date range
    List<BorrowingHistory> findByBorrowerIdAndActionDateBetweenOrderByActionDateDesc(Long borrowerId, 
                                                                                     LocalDateTime startDate, 
                                                                                     LocalDateTime endDate);

    // Find borrowing history for a specific book within a date range
    List<BorrowingHistory> findByBookIdAndActionDateBetweenOrderByActionDateDesc(Long bookId, 
                                                                                 LocalDateTime startDate, 
                                                                                 LocalDateTime endDate);

    // Count total borrowings by a borrower
    @Query("SELECT COUNT(bh) FROM BorrowingHistory bh WHERE bh.borrower.id = :borrowerId AND bh.actionType = 'BORROWED'")
    long countBorrowingsByBorrower(@Param("borrowerId") Long borrowerId);

    // Count how many times a book has been borrowed
    @Query("SELECT COUNT(bh) FROM BorrowingHistory bh WHERE bh.book.id = :bookId AND bh.actionType = 'BORROWED'")
    long countBorrowingsForBook(@Param("bookId") Long bookId);

    // Find most popular books (most borrowed)
    @Query("SELECT bh.book.id, COUNT(bh) as borrowCount FROM BorrowingHistory bh " +
           "WHERE bh.actionType = 'BORROWED' GROUP BY bh.book.id ORDER BY borrowCount DESC")
    List<Object[]> findMostPopularBooks(Pageable pageable);

    // Find most active borrowers
    @Query("SELECT bh.borrower.id, COUNT(bh) as borrowCount FROM BorrowingHistory bh " +
           "WHERE bh.actionType = 'BORROWED' GROUP BY bh.borrower.id ORDER BY borrowCount DESC")
    List<Object[]> findMostActiveBorrowers(Pageable pageable);
}