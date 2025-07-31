package com.library.service.impl;

import com.library.entity.Book;
import com.library.entity.Borrower;
import com.library.repository.BookRepository;
import com.library.repository.BorrowerRepository;
import com.library.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@Transactional
public class BookServiceImpl implements BookService {

    private static final Pattern ISBN_PATTERN = Pattern.compile(
            "^(?:ISBN(?:-1[03])?:? )?(?=[0-9X]{10}$|(?=(?:[0-9]+[- ]){3})[- 0-9X]{13}$|97[89][0-9]{10}$|(?=(?:[0-9]+[- ]){4})[- 0-9]{17}$)(?:97[89][- ]?)?[0-9]{1,5}[- ]?[0-9]+[- ]?[0-9]+[- ]?[0-9X]$"
    );

    private final BookRepository bookRepository;
    private final BorrowerRepository borrowerRepository;

    @Autowired
    public BookServiceImpl(BookRepository bookRepository, BorrowerRepository borrowerRepository) {
        this.bookRepository = bookRepository;
        this.borrowerRepository = borrowerRepository;
    }

    @Override
    public Book addBook(String isbn, String title, String author) {
        validateBookData(isbn, title, author);
        
        String normalizedIsbn = normalizeIsbn(isbn);
        String trimmedTitle = title.trim();
        String trimmedAuthor = author.trim();

        // Check for data consistency if ISBN already exists
        List<Book> existingBooks = bookRepository.findByIsbn(normalizedIsbn);
        if (!existingBooks.isEmpty()) {
            Book firstBook = existingBooks.get(0);
            if (!firstBook.getTitle().equals(trimmedTitle) || !firstBook.getAuthor().equals(trimmedAuthor)) {
                throw new IllegalStateException(
                        String.format("ISBN %s already exists with different title/author. Expected: '%s' by '%s', but found: '%s' by '%s'",
                                normalizedIsbn, trimmedTitle, trimmedAuthor, firstBook.getTitle(), firstBook.getAuthor())
                );
            }
        }

        Book book = new Book(normalizedIsbn, trimmedTitle, trimmedAuthor);
        return bookRepository.save(book);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Book> findBookById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Book ID cannot be null");
        }
        return bookRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Book> findBooksByIsbn(String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) {
            throw new IllegalArgumentException("ISBN cannot be null or empty");
        }
        return bookRepository.findByIsbn(normalizeIsbn(isbn));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Book> searchBooksByTitle(String titlePattern) {
        if (titlePattern == null || titlePattern.trim().isEmpty()) {
            throw new IllegalArgumentException("Title pattern cannot be null or empty");
        }
        return bookRepository.findByTitleContainingIgnoreCase(titlePattern.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Book> searchBooksByAuthor(String authorPattern) {
        if (authorPattern == null || authorPattern.trim().isEmpty()) {
            throw new IllegalArgumentException("Author pattern cannot be null or empty");
        }
        return bookRepository.findByAuthorContainingIgnoreCase(authorPattern.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Book> getAvailableBooks() {
        return bookRepository.findAvailableBooks();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Book> getBorrowedBooks() {
        return bookRepository.findBorrowedBooks();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Book> getAvailableBooksByIsbn(String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) {
            throw new IllegalArgumentException("ISBN cannot be null or empty");
        }
        return bookRepository.findAvailableBooksByIsbn(normalizeIsbn(isbn));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Book> getBooksByBorrowerId(Long borrowerId) {
        if (borrowerId == null) {
            throw new IllegalArgumentException("Borrower ID cannot be null");
        }
        return bookRepository.findBooksByBorrowerId(borrowerId);
    }

    @Override
    public Book borrowBook(String isbn, Long borrowerId) {
        if (isbn == null || isbn.trim().isEmpty()) {
            throw new IllegalArgumentException("ISBN cannot be null or empty");
        }
        if (borrowerId == null) {
            throw new IllegalArgumentException("Borrower ID cannot be null");
        }

        String normalizedIsbn = normalizeIsbn(isbn);
        
        // Verify borrower exists
        Borrower borrower = borrowerRepository.findById(borrowerId)
                .orElseThrow(() -> new IllegalArgumentException("Borrower not found with ID: " + borrowerId));

        // Find first available book with the ISBN
        Optional<Book> availableBook = bookRepository.findFirstAvailableBookByIsbn(normalizedIsbn);
        if (availableBook.isEmpty()) {
            throw new IllegalStateException("No available books found with ISBN: " + normalizedIsbn);
        }

        Book book = availableBook.get();
        book.borrowBy(borrower);
        return bookRepository.save(book);
    }

    @Override
    public Book borrowBookById(Long bookId, Long borrowerId) {
        if (bookId == null) {
            throw new IllegalArgumentException("Book ID cannot be null");
        }
        if (borrowerId == null) {
            throw new IllegalArgumentException("Borrower ID cannot be null");
        }

        // Verify borrower exists
        Borrower borrower = borrowerRepository.findById(borrowerId)
                .orElseThrow(() -> new IllegalArgumentException("Borrower not found with ID: " + borrowerId));

        // Find the specific book
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found with ID: " + bookId));

        if (!book.isAvailable()) {
            throw new IllegalStateException("Book with ID " + bookId + " is not available for borrowing");
        }

        book.borrowBy(borrower);
        return bookRepository.save(book);
    }

    @Override
    public Book returnBook(Long bookId) {
        if (bookId == null) {
            throw new IllegalArgumentException("Book ID cannot be null");
        }

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found with ID: " + bookId));

        if (book.isAvailable()) {
            throw new IllegalStateException("Book with ID " + bookId + " is not currently borrowed");
        }

        book.returnBook();
        return bookRepository.save(book);
    }

    @Override
    public Book updateBook(Long bookId, String title, String author) {
        if (bookId == null) {
            throw new IllegalArgumentException("Book ID cannot be null");
        }

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found with ID: " + bookId));

        boolean updated = false;

        if (title != null && !title.trim().isEmpty()) {
            validateTitle(title);
            String trimmedTitle = title.trim();
            if (!trimmedTitle.equals(book.getTitle())) {
                // Check for ISBN consistency
                List<Book> inconsistentBooks = bookRepository.findInconsistentBooksByIsbn(
                        book.getIsbn(), trimmedTitle, book.getAuthor()
                );
                if (!inconsistentBooks.isEmpty()) {
                    throw new IllegalStateException(
                            "Cannot update title. Other books with ISBN " + book.getIsbn() + 
                            " have different title. All books with same ISBN must have identical title and author."
                    );
                }
                book.setTitle(trimmedTitle);
                updated = true;
            }
        }

        if (author != null && !author.trim().isEmpty()) {
            validateAuthor(author);
            String trimmedAuthor = author.trim();
            if (!trimmedAuthor.equals(book.getAuthor())) {
                // Check for ISBN consistency
                List<Book> inconsistentBooks = bookRepository.findInconsistentBooksByIsbn(
                        book.getIsbn(), book.getTitle(), trimmedAuthor
                );
                if (!inconsistentBooks.isEmpty()) {
                    throw new IllegalStateException(
                            "Cannot update author. Other books with ISBN " + book.getIsbn() + 
                            " have different author. All books with same ISBN must have identical title and author."
                    );
                }
                book.setAuthor(trimmedAuthor);
                updated = true;
            }
        }

        if (updated) {
            return bookRepository.save(book);
        }

        return book;
    }

    @Override
    public void deleteBook(Long bookId) {
        if (bookId == null) {
            throw new IllegalArgumentException("Book ID cannot be null");
        }

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found with ID: " + bookId));

        if (!book.isAvailable()) {
            throw new IllegalStateException(
                    "Cannot delete borrowed book with ID " + bookId + ". Book must be returned first."
            );
        }

        bookRepository.delete(book);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long bookId) {
        if (bookId == null) {
            return false;
        }
        return bookRepository.existsById(bookId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByIsbn(String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) {
            return false;
        }
        return bookRepository.existsByIsbn(normalizeIsbn(isbn));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isBookAvailable(Long bookId) {
        if (bookId == null) {
            return false;
        }
        return bookRepository.findById(bookId)
                .map(Book::isAvailable)
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public long getBookCountByIsbn(String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) {
            throw new IllegalArgumentException("ISBN cannot be null or empty");
        }
        return bookRepository.countByIsbn(normalizeIsbn(isbn));
    }

    @Override
    @Transactional(readOnly = true)
    public long getAvailableBookCountByIsbn(String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) {
            throw new IllegalArgumentException("ISBN cannot be null or empty");
        }
        return bookRepository.countAvailableBooksByIsbn(normalizeIsbn(isbn));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Book> findFirstAvailableBookByIsbn(String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) {
            throw new IllegalArgumentException("ISBN cannot be null or empty");
        }
        return bookRepository.findFirstAvailableBookByIsbn(normalizeIsbn(isbn));
    }

    @Override
    public void validateBookData(String isbn, String title, String author) {
        validateIsbn(isbn);
        validateTitle(title);
        validateAuthor(author);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Book> findInconsistentBooksByIsbn(String isbn, String expectedTitle, String expectedAuthor) {
        if (isbn == null || isbn.trim().isEmpty()) {
            throw new IllegalArgumentException("ISBN cannot be null or empty");
        }
        if (expectedTitle == null || expectedTitle.trim().isEmpty()) {
            throw new IllegalArgumentException("Expected title cannot be null or empty");
        }
        if (expectedAuthor == null || expectedAuthor.trim().isEmpty()) {
            throw new IllegalArgumentException("Expected author cannot be null or empty");
        }
        
        return bookRepository.findInconsistentBooksByIsbn(
                normalizeIsbn(isbn), expectedTitle.trim(), expectedAuthor.trim()
        );
    }

    private void validateIsbn(String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) {
            throw new IllegalArgumentException("ISBN cannot be null or empty");
        }
        
        String normalizedIsbn = normalizeIsbn(isbn);
        if (!ISBN_PATTERN.matcher(normalizedIsbn).matches()) {
            throw new IllegalArgumentException("Invalid ISBN format: " + isbn);
        }
    }

    private void validateTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be null or empty");
        }
        
        String trimmedTitle = title.trim();
        if (trimmedTitle.length() < 1) {
            throw new IllegalArgumentException("Title must be at least 1 character long");
        }
        
        if (trimmedTitle.length() > 500) {
            throw new IllegalArgumentException("Title cannot exceed 500 characters");
        }
    }

    private void validateAuthor(String author) {
        if (author == null || author.trim().isEmpty()) {
            throw new IllegalArgumentException("Author cannot be null or empty");
        }
        
        String trimmedAuthor = author.trim();
        if (trimmedAuthor.length() < 1) {
            throw new IllegalArgumentException("Author must be at least 1 character long");
        }
        
        if (trimmedAuthor.length() > 200) {
            throw new IllegalArgumentException("Author cannot exceed 200 characters");
        }
    }

    private String normalizeIsbn(String isbn) {
        if (isbn == null) {
            return null;
        }
        // Remove all non-digit and non-X characters, then convert to uppercase
        return isbn.replaceAll("[^0-9X]", "").toUpperCase();
    }
}