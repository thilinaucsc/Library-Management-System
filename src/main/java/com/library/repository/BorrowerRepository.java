package com.library.repository;

import com.library.entity.Borrower;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BorrowerRepository extends JpaRepository<Borrower, Long> {

    /**
     * Find borrower by email address
     * @param email the email address to search for
     * @return Optional containing the borrower if found
     */
    Optional<Borrower> findByEmail(String email);

    /**
     * Check if borrower exists by email
     * @param email the email address to check
     * @return true if borrower exists with this email
     */
    boolean existsByEmail(String email);

    /**
     * Find borrowers by name containing the given string (case-insensitive)
     * @param name the name pattern to search for
     * @return list of borrowers matching the name pattern
     */
    List<Borrower> findByNameContainingIgnoreCase(String name);

    /**
     * Find borrowers who have borrowed books
     * @return list of borrowers who currently have borrowed books
     */
    @Query("SELECT DISTINCT b FROM Borrower b JOIN b.borrowedBooks")
    List<Borrower> findBorrowersWithBooks();

    /**
     * Find borrowers who have not borrowed any books
     * @return list of borrowers with no borrowed books
     */
    @Query("SELECT b FROM Borrower b WHERE b.borrowedBooks IS EMPTY")
    List<Borrower> findBorrowersWithoutBooks();

    /**
     * Count total number of books borrowed by a specific borrower
     * @param borrowerId the ID of the borrower
     * @return count of books currently borrowed by the borrower
     */
    @Query("SELECT COUNT(book) FROM Book book WHERE book.borrower.id = :borrowerId")
    long countBooksBorrowedByBorrower(@Param("borrowerId") Long borrowerId);
}