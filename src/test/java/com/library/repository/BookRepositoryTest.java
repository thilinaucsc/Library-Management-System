package com.library.repository;

import com.library.entity.Book;
import com.library.entity.Borrower;
import org.junit.jupiter.api.BeforeEach;  
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class BookRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BookRepository bookRepository;

    private Book testBook1;
    private Book testBook2;
    private Book testBook3;
    private Borrower testBorrower;

    @BeforeEach
    void setUp() {
        testBorrower = new Borrower("John Doe", "john.doe@email.com");
        entityManager.persistAndFlush(testBorrower);

        testBook1 = new Book("978-0-13-110362-7", "Effective Java", "Joshua Bloch");
        testBook2 = new Book("978-0-13-110362-7", "Effective Java", "Joshua Bloch");
        testBook3 = new Book("978-0-321-35668-0", "Clean Code", "Robert C. Martin");

        // Borrow one book
        testBook2.borrowBy(testBorrower);

        entityManager.persistAndFlush(testBook1);
        entityManager.persistAndFlush(testBook2);
        entityManager.persistAndFlush(testBook3);
    }

    @Test
    void findByIsbn_ShouldReturnAllBooksWithSameIsbn() {
        // Act
        List<Book> results = bookRepository.findByIsbn("978-0-13-110362-7");

        // Assert
        assertThat(results).hasSize(2);
        assertThat(results).allMatch(book -> book.getIsbn().equals("978-0-13-110362-7"));
        assertThat(results).allMatch(book -> book.getTitle().equals("Effective Java"));
    }

    @Test
    void findByTitleContainingIgnoreCase_ShouldReturnMatchingBooks() {
        // Act
        List<Book> results = bookRepository.findByTitleContainingIgnoreCase("effective");

        // Assert
        assertThat(results).hasSize(2);
        assertThat(results).allMatch(book -> book.getTitle().contains("Effective"));
    }

    @Test
    void findByAuthorContainingIgnoreCase_ShouldReturnMatchingBooks() {
        // Act
        List<Book> results = bookRepository.findByAuthorContainingIgnoreCase("bloch");

        // Assert
        assertThat(results).hasSize(2);
        assertThat(results).allMatch(book -> book.getAuthor().contains("Bloch"));
    }

    @Test
    void findAvailableBooks_ShouldReturnOnlyUnborrowedBooks() {
        // Act
        List<Book> results = bookRepository.findAvailableBooks();

        // Assert
        assertThat(results).hasSize(2);
        assertThat(results).allMatch(Book::isAvailable);
        assertThat(results).extracting(Book::getTitle)
                .containsExactlyInAnyOrder("Effective Java", "Clean Code");
    }

    @Test
    void findBorrowedBooks_ShouldReturnOnlyBorrowedBooks() {
        // Act
        List<Book> results = bookRepository.findBorrowedBooks();

        // Assert
        assertThat(results).hasSize(1);
        assertThat(results).allMatch(book -> !book.isAvailable());
        assertThat(results.get(0).getTitle()).isEqualTo("Effective Java");
        assertThat(results.get(0).getBorrower()).isEqualTo(testBorrower);
    }

    @Test
    void findBooksByBorrowerId_ShouldReturnBooksForSpecificBorrower() {
        // Act
        List<Book> results = bookRepository.findBooksByBorrowerId(testBorrower.getId());

        // Assert
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getBorrower()).isEqualTo(testBorrower);
        assertThat(results.get(0).getTitle()).isEqualTo("Effective Java");
    }

    @Test
    void findAvailableBooksByIsbn_ShouldReturnOnlyAvailableBooksWithIsbn() {
        // Act
        List<Book> results = bookRepository.findAvailableBooksByIsbn("978-0-13-110362-7");

        // Assert
        assertThat(results).hasSize(1);
        assertThat(results.get(0).isAvailable()).isTrue();
        assertThat(results.get(0).getIsbn()).isEqualTo("978-0-13-110362-7");
    }

    @Test
    void existsByIsbn_ShouldReturnTrue_WhenIsbnExists() {
        // Act
        boolean exists = bookRepository.existsByIsbn("978-0-13-110362-7");

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    void existsByIsbn_ShouldReturnFalse_WhenIsbnDoesNotExist() {
        // Act
        boolean exists = bookRepository.existsByIsbn("978-0-00-000000-0");

        // Assert
        assertThat(exists).isFalse();
    }

    @Test
    void findFirstAvailableBookByIsbn_ShouldReturnAvailableBook() {
        // Act
        Optional<Book> result = bookRepository.findFirstAvailableBookByIsbn("978-0-13-110362-7");

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().isAvailable()).isTrue();
        assertThat(result.get().getIsbn()).isEqualTo("978-0-13-110362-7");
    }

    @Test
    void findFirstAvailableBookByIsbn_ShouldReturnEmpty_WhenNoAvailableBooks() {
        // Arrange - borrow all books with this ISBN
        testBook1.borrowBy(testBorrower);
        entityManager.persistAndFlush(testBook1);

        // Act
        Optional<Book> result = bookRepository.findFirstAvailableBookByIsbn("978-0-13-110362-7");

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void countByIsbn_ShouldReturnTotalCountForIsbn() {
        // Act
        long count = bookRepository.countByIsbn("978-0-13-110362-7");

        // Assert
        assertThat(count).isEqualTo(2);
    }

    @Test
    void countAvailableBooksByIsbn_ShouldReturnAvailableCountForIsbn() {
        // Act
        long count = bookRepository.countAvailableBooksByIsbn("978-0-13-110362-7");

        // Assert
        assertThat(count).isEqualTo(1);
    }

    @Test
    void findInconsistentBooksByIsbn_ShouldReturnEmpty_WhenBooksAreConsistent() {
        // Act
        List<Book> results = bookRepository.findInconsistentBooksByIsbn(
                "978-0-13-110362-7", "Effective Java", "Joshua Bloch"
        );

        // Assert
        assertThat(results).isEmpty();
    }

    @Test
    void findInconsistentBooksByIsbn_ShouldReturnBooks_WhenInconsistentDataExists() {
        // Arrange - create a book with same ISBN but different title
        Book inconsistentBook = new Book("978-0-13-110362-7", "Different Title", "Joshua Bloch");
        entityManager.persistAndFlush(inconsistentBook);

        // Act
        List<Book> results = bookRepository.findInconsistentBooksByIsbn(
                "978-0-13-110362-7", "Effective Java", "Joshua Bloch"
        );

        // Assert
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("Different Title");
    }

    @Test
    void save_ShouldPersistBook() {
        // Arrange
        Book newBook = new Book("978-0-123-45678-9", "Test Book", "Test Author");

        // Act
        Book saved = bookRepository.save(newBook);

        // Assert
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getIsbn()).isEqualTo("978-0-123-45678-9");
        assertThat(saved.getTitle()).isEqualTo("Test Book");
        assertThat(saved.getAuthor()).isEqualTo("Test Author");
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(saved.isAvailable()).isTrue();
    }
}