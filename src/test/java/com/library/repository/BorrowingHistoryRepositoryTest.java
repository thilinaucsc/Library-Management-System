package com.library.repository;

import com.library.entity.Book;
import com.library.entity.Borrower;
import com.library.entity.BorrowingHistory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class BorrowingHistoryRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BorrowingHistoryRepository borrowingHistoryRepository;

    private Book testBook;
    private Borrower testBorrower;
    private BorrowingHistory borrowingHistory;
    private BorrowingHistory returnHistory;

    @BeforeEach
    void setUp() {
        // Create test borrower
        testBorrower = new Borrower("John Doe", "john.doe@email.com");
        entityManager.persistAndFlush(testBorrower);

        // Create test book
        testBook = new Book("9780131103627", "Effective Java", "Joshua Bloch");
        entityManager.persistAndFlush(testBook);

        // Create borrowing history
        borrowingHistory = new BorrowingHistory(testBook, testBorrower, BorrowingHistory.ActionType.BORROWED);
        borrowingHistory.setActionDate(LocalDateTime.now().minusDays(5));
        borrowingHistory.setDueDate(LocalDateTime.now().plusDays(9));
        entityManager.persistAndFlush(borrowingHistory);

        // Create return history
        returnHistory = new BorrowingHistory(testBook, testBorrower, BorrowingHistory.ActionType.RETURNED);
        returnHistory.setActionDate(LocalDateTime.now().minusDays(1));
        entityManager.persistAndFlush(returnHistory);
    }

    @Test
    void findByBookIdOrderByActionDateDesc_ShouldReturnHistoryForBook() {
        List<BorrowingHistory> history = borrowingHistoryRepository.findByBookIdOrderByActionDateDesc(testBook.getId());

        assertThat(history).hasSize(2);
        assertThat(history.get(0).getActionType()).isEqualTo(BorrowingHistory.ActionType.RETURNED);
        assertThat(history.get(1).getActionType()).isEqualTo(BorrowingHistory.ActionType.BORROWED);
    }

    @Test
    void findByBorrowerIdOrderByActionDateDesc_ShouldReturnHistoryForBorrower() {
        List<BorrowingHistory> history = borrowingHistoryRepository.findByBorrowerIdOrderByActionDateDesc(testBorrower.getId());

        assertThat(history).hasSize(2);
        assertThat(history.get(0).getActionType()).isEqualTo(BorrowingHistory.ActionType.RETURNED);
        assertThat(history.get(1).getActionType()).isEqualTo(BorrowingHistory.ActionType.BORROWED);
    }

    @Test
    void findByBorrowerIdAndActionTypeOrderByActionDateDesc_ShouldReturnFilteredHistory() {
        List<BorrowingHistory> borrowings = borrowingHistoryRepository.findByBorrowerIdAndActionTypeOrderByActionDateDesc(
                testBorrower.getId(), BorrowingHistory.ActionType.BORROWED);

        assertThat(borrowings).hasSize(1);
        assertThat(borrowings.get(0).getActionType()).isEqualTo(BorrowingHistory.ActionType.BORROWED);
    }

    @Test
    void findCurrentlyBorrowedBooksByBorrower_ShouldReturnEmptyWhenBookReturned() {
        List<BorrowingHistory> currentBorrowings = borrowingHistoryRepository.findCurrentlyBorrowedBooksByBorrower(testBorrower.getId());

        // Since the book was returned, there should be no current borrowings
        assertThat(currentBorrowings).isEmpty();
    }

    @Test
    void findCurrentlyBorrowedBooksByBorrower_ShouldReturnBorrowingWhenNotReturned() {
        // Create another book and borrow it without returning
        Book anotherBook = new Book("9780134685991", "Effective Java 3rd Edition", "Joshua Bloch");
        entityManager.persistAndFlush(anotherBook);

        BorrowingHistory currentBorrowing = new BorrowingHistory(anotherBook, testBorrower, BorrowingHistory.ActionType.BORROWED);
        currentBorrowing.setActionDate(LocalDateTime.now().minusDays(3));
        currentBorrowing.setDueDate(LocalDateTime.now().plusDays(11));
        entityManager.persistAndFlush(currentBorrowing);

        List<BorrowingHistory> currentBorrowings = borrowingHistoryRepository.findCurrentlyBorrowedBooksByBorrower(testBorrower.getId());

        assertThat(currentBorrowings).hasSize(1);
        assertThat(currentBorrowings.get(0).getBook().getId()).isEqualTo(anotherBook.getId());
    }

    @Test
    void findOverdueBorrowingsByBorrower_ShouldReturnOverdueBooks() {
        // Create an overdue borrowing
        Book overdueBook = new Book("9780596009205", "Head First Design Patterns", "Eric Freeman");
        entityManager.persistAndFlush(overdueBook);

        BorrowingHistory overdueBorrowing = new BorrowingHistory(overdueBook, testBorrower, BorrowingHistory.ActionType.BORROWED);
        overdueBorrowing.setActionDate(LocalDateTime.now().minusDays(20));
        overdueBorrowing.setDueDate(LocalDateTime.now().minusDays(6)); // Overdue by 6 days
        entityManager.persistAndFlush(overdueBorrowing);

        List<BorrowingHistory> overdueBooks = borrowingHistoryRepository.findOverdueBorrowingsByBorrower(
                testBorrower.getId(), LocalDateTime.now());

        assertThat(overdueBooks).hasSize(1);
        assertThat(overdueBooks.get(0).getBook().getId()).isEqualTo(overdueBook.getId());
        assertThat(overdueBooks.get(0).isOverdue()).isTrue();
    }

    @Test
    void findAllOverdueBorrowings_ShouldReturnAllOverdueBooks() {
        // Create another borrower with an overdue book
        Borrower anotherBorrower = new Borrower("Jane Smith", "jane.smith@email.com");
        entityManager.persistAndFlush(anotherBorrower);

        Book overdueBook = new Book("9780596009205", "Head First Design Patterns", "Eric Freeman");
        entityManager.persistAndFlush(overdueBook);

        BorrowingHistory overdueBorrowing = new BorrowingHistory(overdueBook, anotherBorrower, BorrowingHistory.ActionType.BORROWED);
        overdueBorrowing.setActionDate(LocalDateTime.now().minusDays(20));
        overdueBorrowing.setDueDate(LocalDateTime.now().minusDays(6)); // Overdue by 6 days
        entityManager.persistAndFlush(overdueBorrowing);

        List<BorrowingHistory> allOverdueBooks = borrowingHistoryRepository.findAllOverdueBorrowings(LocalDateTime.now());

        assertThat(allOverdueBooks).hasSize(1);
        assertThat(allOverdueBooks.get(0).getBorrower().getId()).isEqualTo(anotherBorrower.getId());
    }

    @Test
    void findFirstByBookIdOrderByActionDateDesc_ShouldReturnMostRecentHistory() {
        Optional<BorrowingHistory> mostRecent = borrowingHistoryRepository.findFirstByBookIdOrderByActionDateDesc(testBook.getId());

        assertThat(mostRecent).isPresent();
        assertThat(mostRecent.get().getActionType()).isEqualTo(BorrowingHistory.ActionType.RETURNED);
    }

    @Test
    void findByActionDateBetweenOrderByActionDateDesc_ShouldReturnHistoryInDateRange() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(10);
        LocalDateTime endDate = LocalDateTime.now();

        List<BorrowingHistory> historyInRange = borrowingHistoryRepository.findByActionDateBetweenOrderByActionDateDesc(startDate, endDate);

        assertThat(historyInRange).hasSize(2);
    }

    @Test
    void findByBorrowerIdAndActionDateBetweenOrderByActionDateDesc_ShouldReturnFilteredHistory() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(10);
        LocalDateTime endDate = LocalDateTime.now();

        List<BorrowingHistory> historyInRange = borrowingHistoryRepository.findByBorrowerIdAndActionDateBetweenOrderByActionDateDesc(
                testBorrower.getId(), startDate, endDate);

        assertThat(historyInRange).hasSize(2);
        assertThat(historyInRange.get(0).getBorrower().getId()).isEqualTo(testBorrower.getId());
    }

    @Test
    void countBorrowingsByBorrower_ShouldReturnCorrectCount() {
        long count = borrowingHistoryRepository.countBorrowingsByBorrower(testBorrower.getId());

        assertThat(count).isEqualTo(1); // Only one BORROWED action
    }

    @Test
    void countBorrowingsForBook_ShouldReturnCorrectCount() {
        long count = borrowingHistoryRepository.countBorrowingsForBook(testBook.getId());

        assertThat(count).isEqualTo(1); // Only one BORROWED action
    }

    @Test
    void findMostPopularBooks_ShouldReturnBooksOrderedByBorrowCount() {
        // Create another book and borrow it multiple times
        Book popularBook = new Book("9780134685991", "Effective Java 3rd Edition", "Joshua Bloch");
        entityManager.persistAndFlush(popularBook);

        Borrower anotherBorrower = new Borrower("Jane Smith", "jane.smith@email.com");
        entityManager.persistAndFlush(anotherBorrower);

        // Add multiple borrowing records for the popular book
        BorrowingHistory borrowing1 = new BorrowingHistory(popularBook, testBorrower, BorrowingHistory.ActionType.BORROWED);
        borrowing1.setActionDate(LocalDateTime.now().minusDays(10));
        entityManager.persistAndFlush(borrowing1);

        BorrowingHistory borrowing2 = new BorrowingHistory(popularBook, anotherBorrower, BorrowingHistory.ActionType.BORROWED);
        borrowing2.setActionDate(LocalDateTime.now().minusDays(8));
        entityManager.persistAndFlush(borrowing2);

        List<Object[]> popularBooks = borrowingHistoryRepository.findMostPopularBooks(PageRequest.of(0, 10));

        assertThat(popularBooks).isNotEmpty();
        // The popularBook should be first with 2 borrowings
        Object[] firstResult = popularBooks.get(0);
        assertThat(firstResult[0]).isEqualTo(popularBook.getId());
        assertThat(firstResult[1]).isEqualTo(2L);
    }

    @Test
    void findMostActiveBorrowers_ShouldReturnBorrowersOrderedByBorrowCount() {
        // Create books and additional borrowings for testBorrower
        Book book2 = new Book("9780134685991", "Effective Java 3rd Edition", "Joshua Bloch");
        entityManager.persistAndFlush(book2);

        Book book3 = new Book("9781491950371", "Learning Java", "Patrick Niemeyer");
        entityManager.persistAndFlush(book3);

        BorrowingHistory borrowing1 = new BorrowingHistory(book2, testBorrower, BorrowingHistory.ActionType.BORROWED);
        borrowing1.setActionDate(LocalDateTime.now().minusDays(10));
        entityManager.persistAndFlush(borrowing1);

        BorrowingHistory borrowing2 = new BorrowingHistory(book3, testBorrower, BorrowingHistory.ActionType.BORROWED);
        borrowing2.setActionDate(LocalDateTime.now().minusDays(8));
        entityManager.persistAndFlush(borrowing2);

        List<Object[]> activeBorrowers = borrowingHistoryRepository.findMostActiveBorrowers(PageRequest.of(0, 10));

        assertThat(activeBorrowers).isNotEmpty();
        // testBorrower should be first with 3 borrowings (1 from setUp + 2 new)
        Object[] firstResult = activeBorrowers.get(0);
        assertThat(firstResult[0]).isEqualTo(testBorrower.getId());
        assertThat(firstResult[1]).isEqualTo(3L);
    }

    @Test
    void save_ShouldPersistBorrowingHistory() {
        Book newBook = new Book("9781491950371", "Learning Java", "Patrick Niemeyer");
        entityManager.persistAndFlush(newBook);

        BorrowingHistory newHistory = new BorrowingHistory(newBook, testBorrower, BorrowingHistory.ActionType.BORROWED);
        
        BorrowingHistory saved = borrowingHistoryRepository.save(newHistory);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getBook().getId()).isEqualTo(newBook.getId());
        assertThat(saved.getBorrower().getId()).isEqualTo(testBorrower.getId());
        assertThat(saved.getActionType()).isEqualTo(BorrowingHistory.ActionType.BORROWED);
        assertThat(saved.getActionDate()).isNotNull();
        assertThat(saved.getDueDate()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void borrowingHistory_BusinessMethods_ShouldWorkCorrectly() {
        // Test overdue functionality
        BorrowingHistory overdueHistory = new BorrowingHistory(testBook, testBorrower, BorrowingHistory.ActionType.BORROWED);
        overdueHistory.setDueDate(LocalDateTime.now().minusDays(1)); // 1 day overdue

        assertThat(overdueHistory.isOverdue()).isTrue();
        assertThat(overdueHistory.getDaysUntilDue()).isNegative();

        // Test non-overdue functionality
        BorrowingHistory notOverdueHistory = new BorrowingHistory(testBook, testBorrower, BorrowingHistory.ActionType.BORROWED);
        notOverdueHistory.setDueDate(LocalDateTime.now().plusDays(5)); // 5 days until due

        assertThat(notOverdueHistory.isOverdue()).isFalse();
        assertThat(notOverdueHistory.getDaysUntilDue()).isBetween(4L, 5L); // Account for timing differences

        // Test return history (should not be overdue)
        BorrowingHistory returnedHistory = new BorrowingHistory(testBook, testBorrower, BorrowingHistory.ActionType.RETURNED);

        assertThat(returnedHistory.isOverdue()).isFalse();
        assertThat(returnedHistory.getDaysUntilDue()).isEqualTo(0L);
    }
}