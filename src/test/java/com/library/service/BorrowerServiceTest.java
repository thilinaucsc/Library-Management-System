package com.library.service;

import com.library.entity.Book;
import com.library.entity.Borrower;
import com.library.repository.BookRepository;
import com.library.repository.BorrowerRepository;
import com.library.service.impl.BorrowerServiceImpl;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BorrowerServiceTest {

    @Mock
    private BorrowerRepository borrowerRepository;

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BorrowerServiceImpl borrowerService;

    private Borrower testBorrower;
    private Book testBook;

    @BeforeEach
    void setUp() {
        testBorrower = new Borrower("John Doe", "john.doe@email.com");
        testBorrower.setId(1L);
        
        testBook = new Book("978-0-13-110362-7", "Effective Java", "Joshua Bloch");
        testBook.setId(1L);
    }

    @Test
    void registerBorrower_ShouldCreateBorrower_WhenValidData() {
        // Arrange
        String name = "Jane Smith";
        String email = "jane.smith@email.com";
        Borrower expectedBorrower = new Borrower(name, email);
        expectedBorrower.setId(2L);

        when(borrowerRepository.existsByEmail(email.toLowerCase())).thenReturn(false);
        when(borrowerRepository.save(any(Borrower.class))).thenReturn(expectedBorrower);

        // Act
        Borrower result = borrowerService.registerBorrower(name, email);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(name);
        assertThat(result.getEmail()).isEqualTo(email.toLowerCase());
        verify(borrowerRepository).existsByEmail(email.toLowerCase());
        verify(borrowerRepository).save(any(Borrower.class));
    }

    @Test
    void registerBorrower_ShouldThrowException_WhenEmailAlreadyExists() {
        // Arrange
        String name = "Jane Smith";
        String email = "john.doe@email.com";

        when(borrowerRepository.existsByEmail(email.toLowerCase())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> borrowerService.registerBorrower(name, email))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Email already exists: " + email.toLowerCase());

        verify(borrowerRepository).existsByEmail(email.toLowerCase());
        verify(borrowerRepository, never()).save(any(Borrower.class));
    }

    @Test
    void registerBorrower_ShouldThrowException_WhenNameIsNull() {
        // Act & Assert
        assertThatThrownBy(() -> borrowerService.registerBorrower(null, "test@email.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Name cannot be null or empty");
    }

    @Test
    void registerBorrower_ShouldThrowException_WhenNameIsEmpty() {
        // Act & Assert
        assertThatThrownBy(() -> borrowerService.registerBorrower("", "test@email.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Name cannot be null or empty");
    }

    @Test
    void registerBorrower_ShouldThrowException_WhenNameTooShort() {
        // Act & Assert
        assertThatThrownBy(() -> borrowerService.registerBorrower("A", "test@email.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Name must be at least 2 characters long");
    }

    @Test
    void registerBorrower_ShouldThrowException_WhenNameTooLong() {
        // Arrange
        String longName = "A".repeat(101);

        // Act & Assert
        assertThatThrownBy(() -> borrowerService.registerBorrower(longName, "test@email.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Name cannot exceed 100 characters");
    }

    @Test
    void registerBorrower_ShouldThrowException_WhenNameHasInvalidCharacters() {
        // Act & Assert
        assertThatThrownBy(() -> borrowerService.registerBorrower("John123", "test@email.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Name can only contain letters, spaces, hyphens, and apostrophes");
    }

    @Test
    void registerBorrower_ShouldThrowException_WhenEmailIsNull() {
        // Act & Assert
        assertThatThrownBy(() -> borrowerService.registerBorrower("John Doe", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email cannot be null or empty");
    }

    @Test
    void registerBorrower_ShouldThrowException_WhenEmailIsInvalid() {
        // Act & Assert
        assertThatThrownBy(() -> borrowerService.registerBorrower("John Doe", "invalidEmail"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email format is invalid: invalidEmail");
    }

    @Test
    void registerBorrower_ShouldThrowException_WhenEmailTooLong() {
        // Arrange
        String longEmail = "a".repeat(142) + "@email.com"; // 142 + 10 = 152 characters (exceeds 150)

        // Act & Assert
        assertThatThrownBy(() -> borrowerService.registerBorrower("John Doe", longEmail))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email cannot exceed 150 characters");
    }

    @Test
    void findBorrowerById_ShouldReturnBorrower_WhenExists() {
        // Arrange
        when(borrowerRepository.findById(1L)).thenReturn(Optional.of(testBorrower));

        // Act
        Optional<Borrower> result = borrowerService.findBorrowerById(1L);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testBorrower);
        verify(borrowerRepository).findById(1L);
    }

    @Test
    void findBorrowerById_ShouldReturnEmpty_WhenNotExists() {
        // Arrange
        when(borrowerRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        Optional<Borrower> result = borrowerService.findBorrowerById(99L);

        // Assert
        assertThat(result).isEmpty();
        verify(borrowerRepository).findById(99L);
    }

    @Test
    void findBorrowerById_ShouldThrowException_WhenIdIsNull() {
        // Act & Assert
        assertThatThrownBy(() -> borrowerService.findBorrowerById(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Borrower ID cannot be null");
    }

    @Test
    void findBorrowerByEmail_ShouldReturnBorrower_WhenExists() {
        // Arrange
        String email = "john.doe@email.com";
        when(borrowerRepository.findByEmail(email)).thenReturn(Optional.of(testBorrower));

        // Act
        Optional<Borrower> result = borrowerService.findBorrowerByEmail(email);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testBorrower);
        verify(borrowerRepository).findByEmail(email);
    }

    @Test
    void findBorrowerByEmail_ShouldNormalizeEmail() {
        // Arrange
        String email = "JOHN.DOE@EMAIL.COM";
        String normalizedEmail = "john.doe@email.com";
        when(borrowerRepository.findByEmail(normalizedEmail)).thenReturn(Optional.of(testBorrower));

        // Act
        Optional<Borrower> result = borrowerService.findBorrowerByEmail(email);

        // Assert
        assertThat(result).isPresent();
        verify(borrowerRepository).findByEmail(normalizedEmail);
    }

    @Test
    void findBorrowerByEmail_ShouldThrowException_WhenEmailIsNull() {
        // Act & Assert
        assertThatThrownBy(() -> borrowerService.findBorrowerByEmail(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email cannot be null or empty");
    }

    @Test
    void searchBorrowersByName_ShouldReturnMatchingBorrowers() {
        // Arrange
        String namePattern = "John";
        List<Borrower> expected = Arrays.asList(testBorrower);
        when(borrowerRepository.findByNameContainingIgnoreCase(namePattern)).thenReturn(expected);

        // Act
        List<Borrower> result = borrowerService.searchBorrowersByName(namePattern);

        // Assert
        assertThat(result).isEqualTo(expected);
        verify(borrowerRepository).findByNameContainingIgnoreCase(namePattern);
    }

    @Test
    void searchBorrowersByName_ShouldThrowException_WhenPatternIsNull() {
        // Act & Assert
        assertThatThrownBy(() -> borrowerService.searchBorrowersByName(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Name pattern cannot be null or empty");
    }

    @Test
    void getAllBorrowers_ShouldReturnAllBorrowers() {
        // Arrange
        List<Borrower> expected = Arrays.asList(testBorrower);
        when(borrowerRepository.findAll()).thenReturn(expected);

        // Act
        List<Borrower> result = borrowerService.getAllBorrowers();

        // Assert
        assertThat(result).isEqualTo(expected);
        verify(borrowerRepository).findAll();
    }

    @Test
    void getBorrowersWithBooks_ShouldReturnBorrowersWithBooks() {
        // Arrange
        List<Borrower> expected = Arrays.asList(testBorrower);
        when(borrowerRepository.findBorrowersWithBooks()).thenReturn(expected);

        // Act
        List<Borrower> result = borrowerService.getBorrowersWithBooks();

        // Assert
        assertThat(result).isEqualTo(expected);
        verify(borrowerRepository).findBorrowersWithBooks();
    }

    @Test
    void getBorrowersWithoutBooks_ShouldReturnBorrowersWithoutBooks() {
        // Arrange
        List<Borrower> expected = Arrays.asList(testBorrower);
        when(borrowerRepository.findBorrowersWithoutBooks()).thenReturn(expected);

        // Act
        List<Borrower> result = borrowerService.getBorrowersWithoutBooks();

        // Assert
        assertThat(result).isEqualTo(expected);
        verify(borrowerRepository).findBorrowersWithoutBooks();
    }

    @Test
    void updateBorrower_ShouldUpdateName_WhenValidNameProvided() {
        // Arrange
        String newName = "Jane Doe";
        when(borrowerRepository.findById(1L)).thenReturn(Optional.of(testBorrower));
        when(borrowerRepository.save(testBorrower)).thenReturn(testBorrower);

        // Act
        Borrower result = borrowerService.updateBorrower(1L, newName, null);

        // Assert
        assertThat(result.getName()).isEqualTo(newName);
        verify(borrowerRepository).save(testBorrower);
    }

    @Test
    void updateBorrower_ShouldUpdateEmail_WhenValidEmailProvided() {
        // Arrange
        String newEmail = "jane.doe@email.com";
        when(borrowerRepository.findById(1L)).thenReturn(Optional.of(testBorrower));
        when(borrowerRepository.existsByEmail(newEmail)).thenReturn(false);
        when(borrowerRepository.save(testBorrower)).thenReturn(testBorrower);

        // Act
        Borrower result = borrowerService.updateBorrower(1L, null, newEmail);

        // Assert
        assertThat(result.getEmail()).isEqualTo(newEmail);
        verify(borrowerRepository).existsByEmail(newEmail);
        verify(borrowerRepository).save(testBorrower);
    }

    @Test
    void updateBorrower_ShouldNotUpdate_WhenNoChanges() {
        // Arrange
        when(borrowerRepository.findById(1L)).thenReturn(Optional.of(testBorrower));

        // Act
        Borrower result = borrowerService.updateBorrower(1L, testBorrower.getName(), testBorrower.getEmail());

        // Assert
        assertThat(result).isEqualTo(testBorrower);
        verify(borrowerRepository, never()).save(any(Borrower.class));
    }

    @Test
    void updateBorrower_ShouldThrowException_WhenBorrowerNotFound() {
        // Arrange
        when(borrowerRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> borrowerService.updateBorrower(99L, "New Name", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Borrower not found with ID: 99");
    }

    @Test
    void updateBorrower_ShouldThrowException_WhenEmailAlreadyExists() {
        // Arrange
        String newEmail = "existing@email.com";
        when(borrowerRepository.findById(1L)).thenReturn(Optional.of(testBorrower));
        when(borrowerRepository.existsByEmail(newEmail)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> borrowerService.updateBorrower(1L, null, newEmail))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Email already exists: " + newEmail);
    }

    @Test
    void deleteBorrower_ShouldDeleteBorrower_WhenNoBorrowedBooks() {
        // Arrange
        when(borrowerRepository.existsById(1L)).thenReturn(true);
        when(bookRepository.findBooksByBorrowerId(1L)).thenReturn(Collections.emptyList());

        // Act
        borrowerService.deleteBorrower(1L);

        // Assert
        verify(borrowerRepository).deleteById(1L);
    }

    @Test
    void deleteBorrower_ShouldThrowException_WhenBorrowerNotFound() {
        // Arrange
        when(borrowerRepository.existsById(99L)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> borrowerService.deleteBorrower(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Borrower not found with ID: 99");

        verify(borrowerRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteBorrower_ShouldThrowException_WhenBorrowerHasBorrowedBooks() {
        // Arrange
        when(borrowerRepository.existsById(1L)).thenReturn(true);
        when(bookRepository.findBooksByBorrowerId(1L)).thenReturn(Arrays.asList(testBook));

        // Act & Assert
        assertThatThrownBy(() -> borrowerService.deleteBorrower(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot delete borrower with ID 1. They have 1 borrowed books.");

        verify(borrowerRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteBorrower_ShouldThrowException_WhenIdIsNull() {
        // Act & Assert
        assertThatThrownBy(() -> borrowerService.deleteBorrower(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Borrower ID cannot be null");
    }

    @Test
    void existsById_ShouldReturnTrue_WhenBorrowerExists() {
        // Arrange
        when(borrowerRepository.existsById(1L)).thenReturn(true);

        // Act
        boolean result = borrowerService.existsById(1L);

        // Assert
        assertThat(result).isTrue();
        verify(borrowerRepository).existsById(1L);
    }

    @Test
    void existsById_ShouldReturnFalse_WhenBorrowerNotExists() {
        // Arrange
        when(borrowerRepository.existsById(99L)).thenReturn(false);

        // Act
        boolean result = borrowerService.existsById(99L);

        // Assert
        assertThat(result).isFalse();
        verify(borrowerRepository).existsById(99L);
    }

    @Test
    void existsById_ShouldReturnFalse_WhenIdIsNull() {
        // Act
        boolean result = borrowerService.existsById(null);

        // Assert
        assertThat(result).isFalse();
        verify(borrowerRepository, never()).existsById(anyLong());
    }

    @Test
    void existsByEmail_ShouldReturnTrue_WhenEmailExists() {
        // Arrange
        String email = "john.doe@email.com";
        when(borrowerRepository.existsByEmail(email)).thenReturn(true);

        // Act
        boolean result = borrowerService.existsByEmail(email);

        // Assert
        assertThat(result).isTrue();
        verify(borrowerRepository).existsByEmail(email);
    }

    @Test
    void existsByEmail_ShouldNormalizeEmail() {
        // Arrange
        String email = "JOHN.DOE@EMAIL.COM";
        String normalizedEmail = "john.doe@email.com";
        when(borrowerRepository.existsByEmail(normalizedEmail)).thenReturn(true);

        // Act
        boolean result = borrowerService.existsByEmail(email);

        // Assert
        assertThat(result).isTrue();
        verify(borrowerRepository).existsByEmail(normalizedEmail);
    }

    @Test
    void existsByEmail_ShouldReturnFalse_WhenEmailIsNull() {
        // Act
        boolean result = borrowerService.existsByEmail(null);

        // Assert
        assertThat(result).isFalse();
        verify(borrowerRepository, never()).existsByEmail(anyString());
    }

    @Test
    void getBorrowedBookCount_ShouldReturnCount() {
        // Arrange
        when(bookRepository.findBooksByBorrowerId(1L)).thenReturn(Arrays.asList(testBook));

        // Act
        long result = borrowerService.getBorrowedBookCount(1L);

        // Assert
        assertThat(result).isEqualTo(1);
        verify(bookRepository).findBooksByBorrowerId(1L);
    }

    @Test
    void getBorrowedBookCount_ShouldThrowException_WhenIdIsNull() {
        // Act & Assert
        assertThatThrownBy(() -> borrowerService.getBorrowedBookCount(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Borrower ID cannot be null");
    }

    @Test
    void validateBorrowerData_ShouldPass_WhenValidData() {
        // Act & Assert (should not throw)
        borrowerService.validateBorrowerData("John Doe", "john.doe@email.com");
    }

    @Test
    void validateBorrowerData_ShouldThrowException_WhenInvalidName() {
        // Act & Assert
        assertThatThrownBy(() -> borrowerService.validateBorrowerData("", "john.doe@email.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Name cannot be null or empty");
    }

    @Test
    void validateBorrowerData_ShouldThrowException_WhenInvalidEmail() {
        // Act & Assert
        assertThatThrownBy(() -> borrowerService.validateBorrowerData("John Doe", "invalid-email"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email format is invalid: invalid-email");
    }
}