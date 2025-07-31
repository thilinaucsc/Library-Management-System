package com.library.repository;

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
class BorrowerRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BorrowerRepository borrowerRepository;

    private Borrower testBorrower1;
    private Borrower testBorrower2;

    @BeforeEach
    void setUp() {
        testBorrower1 = new Borrower("John Doe", "john.doe@email.com");
        testBorrower2 = new Borrower("Jane Smith", "jane.smith@email.com");

        entityManager.persistAndFlush(testBorrower1);
        entityManager.persistAndFlush(testBorrower2);
    }

    @Test
    void findByEmail_ShouldReturnBorrower_WhenEmailExists() {
        // Act
        Optional<Borrower> result = borrowerRepository.findByEmail("john.doe@email.com");

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("John Doe");
        assertThat(result.get().getEmail()).isEqualTo("john.doe@email.com");
    }

    @Test
    void findByEmail_ShouldReturnEmpty_WhenEmailDoesNotExist() {
        // Act
        Optional<Borrower> result = borrowerRepository.findByEmail("nonexistent@email.com");

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void existsByEmail_ShouldReturnTrue_WhenEmailExists() {
        // Act
        boolean exists = borrowerRepository.existsByEmail("john.doe@email.com");

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    void existsByEmail_ShouldReturnFalse_WhenEmailDoesNotExist() {
        // Act
        boolean exists = borrowerRepository.existsByEmail("nonexistent@email.com");

        // Assert
        assertThat(exists).isFalse();
    }

    @Test
    void findByNameContainingIgnoreCase_ShouldReturnMatchingBorrowers() {
        // Act
        List<Borrower> results = borrowerRepository.findByNameContainingIgnoreCase("john");

        // Assert
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("John Doe");
    }

    @Test
    void findByNameContainingIgnoreCase_ShouldBeCaseInsensitive() {
        // Act
        List<Borrower> results = borrowerRepository.findByNameContainingIgnoreCase("JANE");

        // Assert
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Jane Smith");
    }

    @Test
    void findByNameContainingIgnoreCase_ShouldReturnEmpty_WhenNoMatch() {
        // Act
        List<Borrower> results = borrowerRepository.findByNameContainingIgnoreCase("NonExistent");

        // Assert
        assertThat(results).isEmpty();
    }

    @Test
    void findBorrowersWithoutBooks_ShouldReturnAllBorrowers_WhenNoBooksAreBorrowed() {
        // Act
        List<Borrower> results = borrowerRepository.findBorrowersWithoutBooks();

        // Assert
        assertThat(results).hasSize(2);
        assertThat(results).extracting(Borrower::getName)
                .containsExactlyInAnyOrder("John Doe", "Jane Smith");
    }

    @Test
    void save_ShouldPersistBorrower() {
        // Arrange
        Borrower newBorrower = new Borrower("Test User", "test@email.com");

        // Act
        Borrower saved = borrowerRepository.save(newBorrower);

        // Assert
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Test User");
        assertThat(saved.getEmail()).isEqualTo("test@email.com");
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void findAll_ShouldReturnAllBorrowers() {
        // Act
        List<Borrower> results = borrowerRepository.findAll();

        // Assert
        assertThat(results).hasSize(2);
        assertThat(results).extracting(Borrower::getName)
                .containsExactlyInAnyOrder("John Doe", "Jane Smith");
    }

    @Test
    void deleteById_ShouldRemoveBorrower() {
        // Arrange
        Long borrowerId = testBorrower1.getId();

        // Act
        borrowerRepository.deleteById(borrowerId);

        // Assert
        Optional<Borrower> result = borrowerRepository.findById(borrowerId);
        assertThat(result).isEmpty();
    }
}