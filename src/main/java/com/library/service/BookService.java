package com.library.service;

import com.library.entity.Book;
import com.library.entity.Borrower;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing books in the library system.
 * Provides business logic operations for book management, borrowing, and returning.
 */
public interface BookService {

    /**
     * Add a new book to the library system.
     * Validates ISBN format and checks for data consistency if ISBN already exists.
     * 
     * @param isbn the book's ISBN (must be valid format)
     * @param title the book's title
     * @param author the book's author
     * @return the created book with assigned ID
     * @throws IllegalArgumentException if ISBN, title, or author is invalid
     * @throws IllegalStateException if ISBN exists with different title/author
     */
    Book addBook(String isbn, String title, String author);

    /**
     * Find a book by its unique ID.
     * 
     * @param id the book's ID
     * @return Optional containing the book if found, empty otherwise
     */
    Optional<Book> findBookById(Long id);

    /**
     * Find all books with a specific ISBN.
     * 
     * @param isbn the ISBN to search for
     * @return list of books with the specified ISBN
     */
    List<Book> findBooksByIsbn(String isbn);

    /**
     * Search for books by title (case-insensitive partial match).
     * 
     * @param titlePattern the title pattern to search for
     * @return list of books matching the title pattern
     */
    List<Book> searchBooksByTitle(String titlePattern);

    /**
     * Search for books by author (case-insensitive partial match).
     * 
     * @param authorPattern the author pattern to search for
     * @return list of books matching the author pattern
     */
    List<Book> searchBooksByAuthor(String authorPattern);

    /**
     * Get all books in the system.
     * 
     * @return list of all books
     */
    List<Book> getAllBooks();

    /**
     * Get all available (not borrowed) books.
     * 
     * @return list of available books
     */
    List<Book> getAvailableBooks();

    /**
     * Get all currently borrowed books.
     * 
     * @return list of borrowed books
     */
    List<Book> getBorrowedBooks();

    /**
     * Get available books with a specific ISBN.
     * 
     * @param isbn the ISBN to search for
     * @return list of available books with the specified ISBN
     */
    List<Book> getAvailableBooksByIsbn(String isbn);

    /**
     * Get books currently borrowed by a specific borrower.
     * 
     * @param borrowerId the borrower's ID
     * @return list of books borrowed by the specified borrower
     */
    List<Book> getBooksByBorrowerId(Long borrowerId);

    /**
     * Borrow a book for a specific borrower.
     * Finds the first available book with the given ISBN and assigns it to the borrower.
     * 
     * @param isbn the ISBN of the book to borrow
     * @param borrowerId the ID of the borrower
     * @return the borrowed book
     * @throws IllegalArgumentException if borrower doesn't exist or ISBN is invalid
     * @throws IllegalStateException if no available books with the ISBN exist
     */
    Book borrowBook(String isbn, Long borrowerId);

    /**
     * Borrow a specific book by its ID for a specific borrower.
     * 
     * @param bookId the ID of the specific book to borrow
     * @param borrowerId the ID of the borrower
     * @return the borrowed book
     * @throws IllegalArgumentException if book or borrower doesn't exist
     * @throws IllegalStateException if book is not available
     */
    Book borrowBookById(Long bookId, Long borrowerId);

    /**
     * Return a borrowed book.
     * Updates the book's status to available and clears borrower information.
     * 
     * @param bookId the ID of the book to return
     * @return the returned book
     * @throws IllegalArgumentException if book doesn't exist
     * @throws IllegalStateException if book is not currently borrowed
     */
    Book returnBook(Long bookId);

    /**
     * Update book information.
     * ISBN cannot be changed once set to maintain data integrity.
     * 
     * @param bookId the book's ID
     * @param title the new title (optional, pass null to keep current)
     * @param author the new author (optional, pass null to keep current)
     * @return the updated book
     * @throws IllegalArgumentException if book doesn't exist or data is invalid
     */
    Book updateBook(Long bookId, String title, String author);

    /**
     * Remove a book from the system.
     * Only allows deletion if the book is not currently borrowed.
     * 
     * @param bookId the book's ID
     * @throws IllegalArgumentException if book doesn't exist
     * @throws IllegalStateException if book is currently borrowed
     */
    void deleteBook(Long bookId);

    /**
     * Check if a book exists by ID.
     * 
     * @param bookId the book's ID
     * @return true if book exists, false otherwise
     */
    boolean existsById(Long bookId);

    /**
     * Check if any books exist with the specified ISBN.
     * 
     * @param isbn the ISBN to check
     * @return true if books with ISBN exist, false otherwise
     */
    boolean existsByIsbn(String isbn);

    /**
     * Check if a specific book is available (not borrowed).
     * 
     * @param bookId the book's ID
     * @return true if book exists and is available, false otherwise
     */
    boolean isBookAvailable(Long bookId);

    /**
     * Get the total count of books with a specific ISBN.
     * 
     * @param isbn the ISBN to count
     * @return total number of books with the specified ISBN
     */
    long getBookCountByIsbn(String isbn);

    /**
     * Get the count of available books with a specific ISBN.
     * 
     * @param isbn the ISBN to count
     * @return number of available books with the specified ISBN
     */
    long getAvailableBookCountByIsbn(String isbn);

    /**
     * Find the first available book with a specific ISBN.
     * Useful for borrowing operations.
     * 
     * @param isbn the ISBN to search for
     * @return Optional containing the first available book, empty if none available
     */
    Optional<Book> findFirstAvailableBookByIsbn(String isbn);

    /**
     * Validate book data before saving or updating.
     * 
     * @param isbn the book's ISBN
     * @param title the book's title
     * @param author the book's author
     * @throws IllegalArgumentException if data is invalid
     */
    void validateBookData(String isbn, String title, String author);

    /**
     * Find books with inconsistent data for the same ISBN.
     * Used for data integrity validation.
     * 
     * @param isbn the ISBN to check
     * @param expectedTitle the expected title for this ISBN
     * @param expectedAuthor the expected author for this ISBN
     * @return list of books with inconsistent data
     */
    List<Book> findInconsistentBooksByIsbn(String isbn, String expectedTitle, String expectedAuthor);
}