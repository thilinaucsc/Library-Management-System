package com.library.service;

import com.library.entity.Book;
import com.library.entity.Borrower;
import com.library.repository.BookRepository;
import com.library.repository.BorrowerRepository;
import com.library.service.impl.BookServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BorrowerRepository borrowerRepository;

    @InjectMocks
    private BookServiceImpl bookService;

    private Book testBook;
    private Borrower testBorrower;

    @BeforeEach
    void setUp() {
        testBook = new Book("9780131103627", "Effective Java", "Joshua Bloch");
        testBook.setId(1L);
        
        testBorrower = new Borrower("John Doe", "john.doe@email.com");
        testBorrower.setId(1L);
    }

    @Test
    void addBook_ShouldCreateBook_WhenValidData() {
        // Arrange
        String isbn = "978-0-13-110362-7";
        String title = "Effective Java";
        String author = "Joshua Bloch";
        String normalizedIsbn = "9780131103627";
        
        when(bookRepository.findByIsbn(normalizedIsbn)).thenReturn(Collections.emptyList());
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);

        // Act
        Book result = bookService.addBook(isbn, title, author);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getIsbn()).isEqualTo(normalizedIsbn);
        assertThat(result.getTitle()).isEqualTo(title);
        assertThat(result.getAuthor()).isEqualTo(author);
        verify(bookRepository).findByIsbn(normalizedIsbn);
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void addBook_ShouldCreateBook_WhenSameIsbnWithSameTitleAuthor() {
        // Arrange
        String isbn = "978-0-13-110362-7";
        String title = "Effective Java";
        String author = "Joshua Bloch";
        String normalizedIsbn = "9780131103627";
        
        Book existingBook = new Book(normalizedIsbn, title, author);
        when(bookRepository.findByIsbn(normalizedIsbn)).thenReturn(Arrays.asList(existingBook));
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);

        // Act
        Book result = bookService.addBook(isbn, title, author);

        // Assert
        assertThat(result).isNotNull();
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void addBook_ShouldThrowException_WhenSameIsbnWithDifferentTitle() {
        // Arrange
        String isbn = "978-0-13-110362-7";
        String title = "Different Title";
        String author = "Joshua Bloch";
        String normalizedIsbn = "9780131103627";
        
        Book existingBook = new Book(normalizedIsbn, "Effective Java", author);
        when(bookRepository.findByIsbn(normalizedIsbn)).thenReturn(Arrays.asList(existingBook));

        // Act & Assert
        assertThatThrownBy(() -> bookService.addBook(isbn, title, author))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ISBN " + normalizedIsbn + " already exists with different title/author");

        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void addBook_ShouldThrowException_WhenIsbnIsNull() {
        // Act & Assert
        assertThatThrownBy(() -> bookService.addBook(null, "Title", "Author"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("ISBN cannot be null or empty");
    }

    @Test
    void addBook_ShouldThrowException_WhenTitleIsNull() {
        // Act & Assert
        assertThatThrownBy(() -> bookService.addBook("978-0-13-110362-7", null, "Author"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Title cannot be null or empty");
    }

    @Test
    void addBook_ShouldThrowException_WhenAuthorIsNull() {
        // Act & Assert
        assertThatThrownBy(() -> bookService.addBook("978-0-13-110362-7", "Title", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Author cannot be null or empty");
    }

    @Test
    void addBook_ShouldThrowException_WhenTitleTooLong() {
        // Arrange
        String longTitle = "A".repeat(501);

        // Act & Assert
        assertThatThrownBy(() -> bookService.addBook("978-0-13-110362-7", longTitle, "Author"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Title cannot exceed 500 characters");
    }

    @Test
    void addBook_ShouldThrowException_WhenAuthorTooLong() {
        // Arrange
        String longAuthor = "A".repeat(201);

        // Act & Assert
        assertThatThrownBy(() -> bookService.addBook("978-0-13-110362-7", "Title", longAuthor))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Author cannot exceed 200 characters");
    }

    @Test
    void findBookById_ShouldReturnBook_WhenExists() {
        // Arrange
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));

        // Act
        Optional<Book> result = bookService.findBookById(1L);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testBook);
        verify(bookRepository).findById(1L);
    }

    @Test
    void findBookById_ShouldReturnEmpty_WhenNotExists() {
        // Arrange
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        Optional<Book> result = bookService.findBookById(99L);

        // Assert
        assertThat(result).isEmpty();
        verify(bookRepository).findById(99L);
    }

    @Test
    void findBookById_ShouldThrowException_WhenIdIsNull() {
        // Act & Assert
        assertThatThrownBy(() -> bookService.findBookById(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Book ID cannot be null");
    }

    @Test
    void findBooksByIsbn_ShouldReturnBooks() {
        // Arrange
        String isbn = "978-0-13-110362-7";
        String normalizedIsbn = "9780131103627";
        List<Book> expected = Arrays.asList(testBook);
        when(bookRepository.findByIsbn(normalizedIsbn)).thenReturn(expected);

        // Act
        List<Book> result = bookService.findBooksByIsbn(isbn);

        // Assert
        assertThat(result).isEqualTo(expected);
        verify(bookRepository).findByIsbn(normalizedIsbn);
    }

    @Test
    void searchBooksByTitle_ShouldReturnMatchingBooks() {
        // Arrange
        String titlePattern = "Effective";
        List<Book> expected = Arrays.asList(testBook);
        when(bookRepository.findByTitleContainingIgnoreCase(titlePattern)).thenReturn(expected);

        // Act
        List<Book> result = bookService.searchBooksByTitle(titlePattern);

        // Assert
        assertThat(result).isEqualTo(expected);
        verify(bookRepository).findByTitleContainingIgnoreCase(titlePattern);
    }

    @Test
    void searchBooksByAuthor_ShouldReturnMatchingBooks() {
        // Arrange
        String authorPattern = "Bloch";
        List<Book> expected = Arrays.asList(testBook);
        when(bookRepository.findByAuthorContainingIgnoreCase(authorPattern)).thenReturn(expected);

        // Act
        List<Book> result = bookService.searchBooksByAuthor(authorPattern);

        // Assert
        assertThat(result).isEqualTo(expected);
        verify(bookRepository).findByAuthorContainingIgnoreCase(authorPattern);
    }

    @Test
    void getAllBooks_ShouldReturnAllBooks() {
        // Arrange
        List<Book> expected = Arrays.asList(testBook);
        when(bookRepository.findAll()).thenReturn(expected);

        // Act
        List<Book> result = bookService.getAllBooks();

        // Assert
        assertThat(result).isEqualTo(expected);
        verify(bookRepository).findAll();
    }

    @Test
    void getAvailableBooks_ShouldReturnAvailableBooks() {
        // Arrange
        List<Book> expected = Arrays.asList(testBook);
        when(bookRepository.findAvailableBooks()).thenReturn(expected);

        // Act
        List<Book> result = bookService.getAvailableBooks();

        // Assert
        assertThat(result).isEqualTo(expected);
        verify(bookRepository).findAvailableBooks();
    }

    @Test
    void getBorrowedBooks_ShouldReturnBorrowedBooks() {
        // Arrange
        List<Book> expected = Arrays.asList(testBook);
        when(bookRepository.findBorrowedBooks()).thenReturn(expected);

        // Act
        List<Book> result = bookService.getBorrowedBooks();

        // Assert
        assertThat(result).isEqualTo(expected);
        verify(bookRepository).findBorrowedBooks();
    }

    @Test
    void getAvailableBooksByIsbn_ShouldReturnAvailableBooks() {
        // Arrange
        String isbn = "978-0-13-110362-7";
        String normalizedIsbn = "9780131103627";
        List<Book> expected = Arrays.asList(testBook);
        when(bookRepository.findAvailableBooksByIsbn(normalizedIsbn)).thenReturn(expected);

        // Act
        List<Book> result = bookService.getAvailableBooksByIsbn(isbn);

        // Assert
        assertThat(result).isEqualTo(expected);
        verify(bookRepository).findAvailableBooksByIsbn(normalizedIsbn);
    }

    @Test
    void getBooksByBorrowerId_ShouldReturnBorrowerBooks() {
        // Arrange
        List<Book> expected = Arrays.asList(testBook);
        when(bookRepository.findBooksByBorrowerId(1L)).thenReturn(expected);

        // Act
        List<Book> result = bookService.getBooksByBorrowerId(1L);

        // Assert
        assertThat(result).isEqualTo(expected);
        verify(bookRepository).findBooksByBorrowerId(1L);
    }

    @Test
    void borrowBook_ShouldBorrowBook_WhenValidData() {
        // Arrange
        String isbn = "978-0-13-110362-7";
        String normalizedIsbn = "9780131103627";
        Long borrowerId = 1L;
        
        when(borrowerRepository.findById(borrowerId)).thenReturn(Optional.of(testBorrower));
        when(bookRepository.findFirstAvailableBookByIsbn(normalizedIsbn)).thenReturn(Optional.of(testBook));
        when(bookRepository.save(testBook)).thenReturn(testBook);

        // Act
        Book result = bookService.borrowBook(isbn, borrowerId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getBorrower()).isEqualTo(testBorrower);
        assertThat(result.isAvailable()).isFalse();
        verify(borrowerRepository).findById(borrowerId);
        verify(bookRepository).findFirstAvailableBookByIsbn(normalizedIsbn);
        verify(bookRepository).save(testBook);
    }

    @Test
    void borrowBook_ShouldThrowException_WhenBorrowerNotFound() {
        // Arrange
        String isbn = "978-0-13-110362-7";
        Long borrowerId = 99L;
        
        when(borrowerRepository.findById(borrowerId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> bookService.borrowBook(isbn, borrowerId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Borrower not found with ID: 99");

        verify(bookRepository, never()).findFirstAvailableBookByIsbn(anyString());
    }

    @Test
    void borrowBook_ShouldThrowException_WhenNoAvailableBook() {
        // Arrange
        String isbn = "978-0-13-110362-7";
        String normalizedIsbn = "9780131103627";
        Long borrowerId = 1L;
        
        when(borrowerRepository.findById(borrowerId)).thenReturn(Optional.of(testBorrower));
        when(bookRepository.findFirstAvailableBookByIsbn(normalizedIsbn)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> bookService.borrowBook(isbn, borrowerId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No available books found with ISBN: " + normalizedIsbn);

        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void borrowBook_ShouldThrowException_WhenIsbnIsNull() {
        // Act & Assert
        assertThatThrownBy(() -> bookService.borrowBook(null, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("ISBN cannot be null or empty");
    }

    @Test
    void borrowBook_ShouldThrowException_WhenBorrowerIdIsNull() {
        // Act & Assert
        assertThatThrownBy(() -> bookService.borrowBook("978-0-13-110362-7", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Borrower ID cannot be null");
    }

    @Test
    void returnBook_ShouldReturnBook_WhenValidData() {
        // Arrange
        testBook.borrowBy(testBorrower);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(bookRepository.save(testBook)).thenReturn(testBook);

        // Act
        Book result = bookService.returnBook(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.isAvailable()).isTrue();
        assertThat(result.getBorrower()).isNull();
        verify(bookRepository).findById(1L);
        verify(bookRepository).save(testBook);
    }

    @Test
    void returnBook_ShouldThrowException_WhenBookNotFound() {
        // Arrange
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> bookService.returnBook(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Book not found with ID: 99");
    }

    @Test
    void returnBook_ShouldThrowException_WhenBookNotBorrowed() {
        // Arrange
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));

        // Act & Assert
        assertThatThrownBy(() -> bookService.returnBook(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Book with ID 1 is not currently borrowed");

        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void returnBook_ShouldThrowException_WhenBookIdIsNull() {
        // Act & Assert
        assertThatThrownBy(() -> bookService.returnBook(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Book ID cannot be null");
    }

    @Test
    void updateBook_ShouldUpdateTitle_WhenValidTitleProvided() {
        // Arrange
        String newTitle = "Effective Java 3rd Edition";
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(bookRepository.findInconsistentBooksByIsbn(testBook.getIsbn(), newTitle, testBook.getAuthor()))
                .thenReturn(Collections.emptyList());
        when(bookRepository.save(testBook)).thenReturn(testBook);

        // Act
        Book result = bookService.updateBook(1L, newTitle, null);

        // Assert
        assertThat(result.getTitle()).isEqualTo(newTitle);
        verify(bookRepository).save(testBook);
    }

    @Test
    void updateBook_ShouldUpdateAuthor_WhenValidAuthorProvided() {
        // Arrange
        String newAuthor = "Joshua Bloch Jr.";
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(bookRepository.findInconsistentBooksByIsbn(testBook.getIsbn(), testBook.getTitle(), newAuthor))
                .thenReturn(Collections.emptyList());
        when(bookRepository.save(testBook)).thenReturn(testBook);

        // Act
        Book result = bookService.updateBook(1L, null, newAuthor);

        // Assert
        assertThat(result.getAuthor()).isEqualTo(newAuthor);
        verify(bookRepository).save(testBook);
    }

    @Test
    void updateBook_ShouldNotUpdate_WhenNoChanges() {
        // Arrange
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));

        // Act
        Book result = bookService.updateBook(1L, testBook.getTitle(), testBook.getAuthor());

        // Assert
        assertThat(result).isEqualTo(testBook);
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void updateBook_ShouldThrowException_WhenBookNotFound() {
        // Arrange
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> bookService.updateBook(99L, "New Title", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Book not found with ID: 99");
    }

    @Test
    void updateBook_ShouldThrowException_WhenTitleUpdateWouldCreateInconsistency() {
        // Arrange
        String newTitle = "Different Title";
        Book inconsistentBook = new Book(testBook.getIsbn(), "Another Title", testBook.getAuthor());
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(bookRepository.findInconsistentBooksByIsbn(testBook.getIsbn(), newTitle, testBook.getAuthor()))
                .thenReturn(Arrays.asList(inconsistentBook));

        // Act & Assert
        assertThatThrownBy(() -> bookService.updateBook(1L, newTitle, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot update title. Other books with ISBN");

        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void deleteBook_ShouldDeleteBook_WhenBookIsAvailable() {
        // Arrange
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));

        // Act
        bookService.deleteBook(1L);

        // Assert
        verify(bookRepository).delete(testBook);
    }

    @Test
    void deleteBook_ShouldThrowException_WhenBookNotFound() {
        // Arrange
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> bookService.deleteBook(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Book not found with ID: 99");

        verify(bookRepository, never()).delete(any(Book.class));
    }

    @Test
    void deleteBook_ShouldThrowException_WhenBookIsBorrowed() {
        // Arrange
        testBook.borrowBy(testBorrower);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));

        // Act & Assert
        assertThatThrownBy(() -> bookService.deleteBook(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot delete borrowed book with ID 1. Book must be returned first.");

        verify(bookRepository, never()).delete(any(Book.class));
    }

    @Test
    void existsById_ShouldReturnTrue_WhenBookExists() {
        // Arrange
        when(bookRepository.existsById(1L)).thenReturn(true);

        // Act
        boolean result = bookService.existsById(1L);

        // Assert
        assertThat(result).isTrue();
        verify(bookRepository).existsById(1L);
    }

    @Test
    void existsById_ShouldReturnFalse_WhenBookNotExists() {
        // Arrange
        when(bookRepository.existsById(99L)).thenReturn(false);

        // Act
        boolean result = bookService.existsById(99L);

        // Assert
        assertThat(result).isFalse();
        verify(bookRepository).existsById(99L);
    }

    @Test
    void existsById_ShouldReturnFalse_WhenIdIsNull() {
        // Act
        boolean result = bookService.existsById(null);

        // Assert
        assertThat(result).isFalse();
        verify(bookRepository, never()).existsById(anyLong());
    }

    @Test
    void existsByIsbn_ShouldReturnTrue_WhenIsbnExists() {
        // Arrange
        String isbn = "978-0-13-110362-7";
        String normalizedIsbn = "9780131103627";
        when(bookRepository.existsByIsbn(normalizedIsbn)).thenReturn(true);

        // Act
        boolean result = bookService.existsByIsbn(isbn);

        // Assert
        assertThat(result).isTrue();
        verify(bookRepository).existsByIsbn(normalizedIsbn);
    }

    @Test
    void isBookAvailable_ShouldReturnTrue_WhenBookIsAvailable() {
        // Arrange
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));

        // Act
        boolean result = bookService.isBookAvailable(1L);

        // Assert
        assertThat(result).isTrue();
        verify(bookRepository).findById(1L);
    }

    @Test
    void isBookAvailable_ShouldReturnFalse_WhenBookIsBorrowed() {
        // Arrange
        testBook.borrowBy(testBorrower);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));

        // Act
        boolean result = bookService.isBookAvailable(1L);

        // Assert
        assertThat(result).isFalse();
        verify(bookRepository).findById(1L);
    }

    @Test
    void isBookAvailable_ShouldReturnFalse_WhenBookNotExists() {
        // Arrange
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        boolean result = bookService.isBookAvailable(99L);

        // Assert
        assertThat(result).isFalse();
        verify(bookRepository).findById(99L);
    }

    @Test
    void getBookCountByIsbn_ShouldReturnCount() {
        // Arrange
        String isbn = "978-0-13-110362-7";
        String normalizedIsbn = "9780131103627";
        when(bookRepository.countByIsbn(normalizedIsbn)).thenReturn(3L);

        // Act
        long result = bookService.getBookCountByIsbn(isbn);

        // Assert
        assertThat(result).isEqualTo(3L);
        verify(bookRepository).countByIsbn(normalizedIsbn);
    }

    @Test
    void getAvailableBookCountByIsbn_ShouldReturnCount() {
        // Arrange
        String isbn = "978-0-13-110362-7";
        String normalizedIsbn = "9780131103627";
        when(bookRepository.countAvailableBooksByIsbn(normalizedIsbn)).thenReturn(2L);

        // Act
        long result = bookService.getAvailableBookCountByIsbn(isbn);

        // Assert
        assertThat(result).isEqualTo(2L);
        verify(bookRepository).countAvailableBooksByIsbn(normalizedIsbn);
    }

    @Test
    void findFirstAvailableBookByIsbn_ShouldReturnBook_WhenAvailable() {
        // Arrange
        String isbn = "978-0-13-110362-7";
        String normalizedIsbn = "9780131103627";
        when(bookRepository.findFirstAvailableBookByIsbn(normalizedIsbn)).thenReturn(Optional.of(testBook));

        // Act
        Optional<Book> result = bookService.findFirstAvailableBookByIsbn(isbn);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testBook);
        verify(bookRepository).findFirstAvailableBookByIsbn(normalizedIsbn);
    }

    @Test
    void validateBookData_ShouldPass_WhenValidData() {
        // Act & Assert (should not throw)
        bookService.validateBookData("978-0-13-110362-7", "Effective Java", "Joshua Bloch");
    }

    @Test
    void validateBookData_ShouldThrowException_WhenInvalidIsbn() {
        // Act & Assert
        assertThatThrownBy(() -> bookService.validateBookData("invalid-isbn", "Title", "Author"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid ISBN format: invalid-isbn");
    }

    @Test
    void validateBookData_ShouldThrowException_WhenInvalidTitle() {
        // Act & Assert
        assertThatThrownBy(() -> bookService.validateBookData("978-0-13-110362-7", "", "Author"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Title cannot be null or empty");
    }

    @Test
    void validateBookData_ShouldThrowException_WhenInvalidAuthor() {
        // Act & Assert
        assertThatThrownBy(() -> bookService.validateBookData("978-0-13-110362-7", "Title", ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Author cannot be null or empty");
    }

    @Test
    void findInconsistentBooksByIsbn_ShouldReturnInconsistentBooks() {
        // Arrange
        String isbn = "978-0-13-110362-7";
        String normalizedIsbn = "9780131103627";
        String expectedTitle = "Expected Title";
        String expectedAuthor = "Expected Author";
        List<Book> expected = Arrays.asList(testBook);
        
        when(bookRepository.findInconsistentBooksByIsbn(normalizedIsbn, expectedTitle, expectedAuthor))
                .thenReturn(expected);

        // Act
        List<Book> result = bookService.findInconsistentBooksByIsbn(isbn, expectedTitle, expectedAuthor);

        // Assert
        assertThat(result).isEqualTo(expected);
        verify(bookRepository).findInconsistentBooksByIsbn(normalizedIsbn, expectedTitle, expectedAuthor);
    }

    @Test
    void findInconsistentBooksByIsbn_ShouldThrowException_WhenIsbnIsNull() {
        // Act & Assert
        assertThatThrownBy(() -> bookService.findInconsistentBooksByIsbn(null, "Title", "Author"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("ISBN cannot be null or empty");
    }
}