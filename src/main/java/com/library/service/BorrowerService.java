package com.library.service;

import com.library.entity.Borrower;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing borrowers in the library system.
 * Provides business logic operations for borrower registration, lookup, and management.
 */
public interface BorrowerService {

    /**
     * Register a new borrower in the system.
     * Validates that the email is unique before creating the borrower.
     * 
     * @param name the borrower's full name
     * @param email the borrower's email address (must be unique)
     * @return the created borrower with assigned ID
     * @throws IllegalArgumentException if name or email is invalid
     * @throws IllegalStateException if email already exists
     */
    Borrower registerBorrower(String name, String email);

    /**
     * Find a borrower by their unique ID.
     * 
     * @param id the borrower's ID
     * @return Optional containing the borrower if found, empty otherwise
     */
    Optional<Borrower> findBorrowerById(Long id);

    /**
     * Find a borrower by their email address.
     * 
     * @param email the borrower's email address
     * @return Optional containing the borrower if found, empty otherwise
     */
    Optional<Borrower> findBorrowerByEmail(String email);

    /**
     * Search for borrowers by name (case-insensitive partial match).
     * 
     * @param namePattern the name pattern to search for
     * @return list of borrowers matching the name pattern
     */
    List<Borrower> searchBorrowersByName(String namePattern);

    /**
     * Get all borrowers in the system.
     * 
     * @return list of all borrowers
     */
    List<Borrower> getAllBorrowers();

    /**
     * Get borrowers who currently have borrowed books.
     * 
     * @return list of borrowers with active book loans
     */
    List<Borrower> getBorrowersWithBooks();

    /**
     * Get borrowers who have not borrowed any books.
     * 
     * @return list of borrowers without any book loans
     */
    List<Borrower> getBorrowersWithoutBooks();

    /**
     * Update borrower information.
     * Email uniqueness is validated if email is being changed.
     * 
     * @param id the borrower's ID
     * @param name the new name (optional, pass null to keep current)
     * @param email the new email (optional, pass null to keep current)
     * @return the updated borrower
     * @throws IllegalArgumentException if borrower doesn't exist or data is invalid
     * @throws IllegalStateException if new email already exists
     */
    Borrower updateBorrower(Long id, String name, String email);

    /**
     * Delete a borrower from the system.
     * Only allows deletion if the borrower has no currently borrowed books.
     * 
     * @param id the borrower's ID
     * @throws IllegalArgumentException if borrower doesn't exist
     * @throws IllegalStateException if borrower has borrowed books
     */
    void deleteBorrower(Long id);

    /**
     * Check if a borrower exists by ID.
     * 
     * @param id the borrower's ID
     * @return true if borrower exists, false otherwise
     */
    boolean existsById(Long id);

    /**
     * Check if an email is already registered.
     * 
     * @param email the email to check
     * @return true if email exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Get the count of books currently borrowed by a specific borrower.
     * 
     * @param borrowerId the borrower's ID
     * @return number of books currently borrowed
     */
    long getBorrowedBookCount(Long borrowerId);

    /**
     * Validate borrower data before saving or updating.
     * 
     * @param name the borrower's name
     * @param email the borrower's email
     * @throws IllegalArgumentException if data is invalid
     */
    void validateBorrowerData(String name, String email);
}