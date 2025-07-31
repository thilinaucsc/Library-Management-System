package com.library.repository;

import com.library.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    /**
     * Find books by ISBN
     * @param isbn the ISBN to search for
     * @return list of books with the given ISBN
     */
    List<Book> findByIsbn(String isbn);

    /**
     * Find books by title containing the given string (case-insensitive)
     * @param title the title pattern to search for
     * @return list of books matching the title pattern
     */
    List<Book> findByTitleContainingIgnoreCase(String title);

    /**
     * Find books by author containing the given string (case-insensitive)
     * @param author the author pattern to search for
     * @return list of books matching the author pattern
     */
    List<Book> findByAuthorContainingIgnoreCase(String author);

    /**
     * Find all available books (not currently borrowed)
     * @return list of available books
     */
    @Query("SELECT b FROM Book b WHERE b.borrower IS NULL")
    List<Book> findAvailableBooks();

    /**
     * Find all borrowed books
     * @return list of borrowed books
     */
    @Query("SELECT b FROM Book b WHERE b.borrower IS NOT NULL")
    List<Book> findBorrowedBooks();

    /**
     * Find books borrowed by a specific borrower
     * @param borrowerId the ID of the borrower
     * @return list of books borrowed by the specified borrower
     */
    @Query("SELECT b FROM Book b WHERE b.borrower.id = :borrowerId")
    List<Book> findBooksByBorrowerId(@Param("borrowerId") Long borrowerId);

    /**
     * Find available books by ISBN
     * @param isbn the ISBN to search for
     * @return list of available books with the given ISBN
     */
    @Query("SELECT b FROM Book b WHERE b.isbn = :isbn AND b.borrower IS NULL")
    List<Book> findAvailableBooksByIsbn(@Param("isbn") String isbn);

    /**
     * Check if any book with the given ISBN exists
     * @param isbn the ISBN to check
     * @return true if any book exists with this ISBN
     */
    boolean existsByIsbn(String isbn);

    /**
     * Find first available book by ISBN for borrowing
     * @param isbn the ISBN to search for
     * @return Optional containing the first available book with the given ISBN
     */
    @Query("SELECT b FROM Book b WHERE b.isbn = :isbn AND b.borrower IS NULL ORDER BY b.id ASC")
    Optional<Book> findFirstAvailableBookByIsbn(@Param("isbn") String isbn);

    /**
     * Count total books by ISBN
     * @param isbn the ISBN to count
     * @return total number of books with the given ISBN
     */
    long countByIsbn(String isbn);

    /**
     * Count available books by ISBN
     * @param isbn the ISBN to count
     * @return number of available books with the given ISBN
     */
    @Query("SELECT COUNT(b) FROM Book b WHERE b.isbn = :isbn AND b.borrower IS NULL")
    long countAvailableBooksByIsbn(@Param("isbn") String isbn);

    /**
     * Validate ISBN consistency - find books with same ISBN but different title/author
     * @param isbn the ISBN to validate
     * @param title the expected title
     * @param author the expected author
     * @return list of books with same ISBN but different title or author
     */
    @Query("SELECT b FROM Book b WHERE b.isbn = :isbn AND (b.title != :title OR b.author != :author)")
    List<Book> findInconsistentBooksByIsbn(@Param("isbn") String isbn, @Param("title") String title, @Param("author") String author);
}