package com.library.service;

import com.library.entity.Book;
import com.library.entity.Borrower;
import com.library.entity.BorrowingHistory;
import com.library.repository.BorrowingHistoryRepository;
import com.library.service.impl.BorrowingHistoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BorrowingHistoryServiceTest {

    @Mock
    private BorrowingHistoryRepository borrowingHistoryRepository;

    @InjectMocks
    private BorrowingHistoryServiceImpl borrowingHistoryService;

    private Book testBook;
    private Borrower testBorrower;
    private BorrowingHistory borrowingHistory;
    private BorrowingHistory returnHistory;

    @BeforeEach
    void setUp() {
        testBook = new Book("9780131103627", "Effective Java", "Joshua Bloch");
        testBook.setId(1L);

        testBorrower = new Borrower("John Doe", "john.doe@email.com");
        testBorrower.setId(1L);

        borrowingHistory = new BorrowingHistory(testBook, testBorrower, BorrowingHistory.ActionType.BORROWED);
        borrowingHistory.setId(1L);
        borrowingHistory.setActionDate(LocalDateTime.now().minusDays(5));
        borrowingHistory.setDueDate(LocalDateTime.now().plusDays(9));

        returnHistory = new BorrowingHistory(testBook, testBorrower, BorrowingHistory.ActionType.RETURNED);
        returnHistory.setId(2L);
        returnHistory.setActionDate(LocalDateTime.now().minusDays(1));
    }

    @Test
    void getHistoryByBookId_ShouldReturnHistory_WhenValidBookId() {
        // Arrange
        Long bookId = 1L;
        List<BorrowingHistory> expectedHistory = Arrays.asList(returnHistory, borrowingHistory);
        when(borrowingHistoryRepository.findByBookIdOrderByActionDateDesc(bookId)).thenReturn(expectedHistory);

        // Act
        List<BorrowingHistory> result = borrowingHistoryService.getHistoryByBookId(bookId);

        // Assert
        assertThat(result).isEqualTo(expectedHistory);
        verify(borrowingHistoryRepository).findByBookIdOrderByActionDateDesc(bookId);
    }

    @Test
    void getHistoryByBookId_ShouldThrowException_WhenBookIdIsNull() {
        // Act & Assert
        assertThatThrownBy(() -> borrowingHistoryService.getHistoryByBookId(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Book ID cannot be null");
    }

    @Test
    void getHistoryByBorrowerId_ShouldReturnHistory_WhenValidBorrowerId() {
        // Arrange
        Long borrowerId = 1L;
        List<BorrowingHistory> expectedHistory = Arrays.asList(returnHistory, borrowingHistory);
        when(borrowingHistoryRepository.findByBorrowerIdOrderByActionDateDesc(borrowerId)).thenReturn(expectedHistory);

        // Act
        List<BorrowingHistory> result = borrowingHistoryService.getHistoryByBorrowerId(borrowerId);

        // Assert
        assertThat(result).isEqualTo(expectedHistory);
        verify(borrowingHistoryRepository).findByBorrowerIdOrderByActionDateDesc(borrowerId);
    }

    @Test
    void getHistoryByBorrowerId_ShouldThrowException_WhenBorrowerIdIsNull() {
        // Act & Assert
        assertThatThrownBy(() -> borrowingHistoryService.getHistoryByBorrowerId(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Borrower ID cannot be null");
    }

    @Test
    void getHistoryByBorrowerIdPaginated_ShouldReturnPagedHistory_WhenValidParameters() {
        // Arrange
        Long borrowerId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        List<BorrowingHistory> historyList = Arrays.asList(returnHistory, borrowingHistory);
        Page<BorrowingHistory> expectedPage = new PageImpl<>(historyList, pageable, historyList.size());
        when(borrowingHistoryRepository.findByBorrowerIdOrderByActionDateDesc(borrowerId, pageable)).thenReturn(expectedPage);

        // Act
        Page<BorrowingHistory> result = borrowingHistoryService.getHistoryByBorrowerId(borrowerId, pageable);

        // Assert
        assertThat(result).isEqualTo(expectedPage);
        verify(borrowingHistoryRepository).findByBorrowerIdOrderByActionDateDesc(borrowerId, pageable);
    }

    @Test
    void getHistoryByBorrowerIdPaginated_ShouldThrowException_WhenBorrowerIdIsNull() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act & Assert
        assertThatThrownBy(() -> borrowingHistoryService.getHistoryByBorrowerId(null, pageable))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Borrower ID cannot be null");
    }

    @Test
    void getHistoryByBorrowerIdPaginated_ShouldThrowException_WhenPageableIsNull() {
        // Act & Assert
        assertThatThrownBy(() -> borrowingHistoryService.getHistoryByBorrowerId(1L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Pageable cannot be null");
    }

    @Test
    void getCurrentlyBorrowedBooksByBorrower_ShouldReturnCurrentBorrowings_WhenValidBorrowerId() {
        // Arrange
        Long borrowerId = 1L;
        List<BorrowingHistory> currentBorrowings = Collections.singletonList(borrowingHistory);
        when(borrowingHistoryRepository.findCurrentlyBorrowedBooksByBorrower(borrowerId)).thenReturn(currentBorrowings);

        // Act
        List<BorrowingHistory> result = borrowingHistoryService.getCurrentlyBorrowedBooksByBorrower(borrowerId);

        // Assert
        assertThat(result).isEqualTo(currentBorrowings);
        verify(borrowingHistoryRepository).findCurrentlyBorrowedBooksByBorrower(borrowerId);
    }

    @Test
    void getCurrentlyBorrowedBooksByBorrower_ShouldThrowException_WhenBorrowerIdIsNull() {
        // Act & Assert
        assertThatThrownBy(() -> borrowingHistoryService.getCurrentlyBorrowedBooksByBorrower(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Borrower ID cannot be null");
    }

    @Test
    void getOverdueBooksByBorrower_ShouldReturnOverdueBooks_WhenValidBorrowerId() {
        // Arrange
        Long borrowerId = 1L;
        BorrowingHistory overdueHistory = new BorrowingHistory(testBook, testBorrower, BorrowingHistory.ActionType.BORROWED);
        overdueHistory.setDueDate(LocalDateTime.now().minusDays(1)); // Overdue
        List<BorrowingHistory> overdueBooks = Collections.singletonList(overdueHistory);
        
        when(borrowingHistoryRepository.findOverdueBorrowingsByBorrower(eq(borrowerId), any(LocalDateTime.class)))
                .thenReturn(overdueBooks);

        // Act
        List<BorrowingHistory> result = borrowingHistoryService.getOverdueBooksByBorrower(borrowerId);

        // Assert
        assertThat(result).isEqualTo(overdueBooks);
        verify(borrowingHistoryRepository).findOverdueBorrowingsByBorrower(eq(borrowerId), any(LocalDateTime.class));
    }

    @Test
    void getOverdueBooksByBorrower_ShouldThrowException_WhenBorrowerIdIsNull() {
        // Act & Assert
        assertThatThrownBy(() -> borrowingHistoryService.getOverdueBooksByBorrower(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Borrower ID cannot be null");
    }

    @Test
    void getAllOverdueBooks_ShouldReturnAllOverdueBooks() {
        // Arrange
        BorrowingHistory overdueHistory = new BorrowingHistory(testBook, testBorrower, BorrowingHistory.ActionType.BORROWED);
        overdueHistory.setDueDate(LocalDateTime.now().minusDays(1)); // Overdue
        List<BorrowingHistory> overdueBooks = Collections.singletonList(overdueHistory);
        
        when(borrowingHistoryRepository.findAllOverdueBorrowings(any(LocalDateTime.class))).thenReturn(overdueBooks);

        // Act
        List<BorrowingHistory> result = borrowingHistoryService.getAllOverdueBooks();

        // Assert
        assertThat(result).isEqualTo(overdueBooks);
        verify(borrowingHistoryRepository).findAllOverdueBorrowings(any(LocalDateTime.class));
    }

    @Test
    void getMostRecentHistoryForBook_ShouldReturnMostRecentHistory_WhenValidBookId() {
        // Arrange
        Long bookId = 1L;
        when(borrowingHistoryRepository.findFirstByBookIdOrderByActionDateDesc(bookId))
                .thenReturn(Optional.of(returnHistory));

        // Act
        Optional<BorrowingHistory> result = borrowingHistoryService.getMostRecentHistoryForBook(bookId);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(returnHistory);
        verify(borrowingHistoryRepository).findFirstByBookIdOrderByActionDateDesc(bookId);
    }

    @Test
    void getMostRecentHistoryForBook_ShouldThrowException_WhenBookIdIsNull() {
        // Act & Assert
        assertThatThrownBy(() -> borrowingHistoryService.getMostRecentHistoryForBook(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Book ID cannot be null");
    }

    @Test
    void getHistoryByDateRange_ShouldReturnHistory_WhenValidDateRange() {
        // Arrange
        LocalDateTime startDate = LocalDateTime.now().minusDays(10);
        LocalDateTime endDate = LocalDateTime.now();
        List<BorrowingHistory> expectedHistory = Arrays.asList(returnHistory, borrowingHistory);
        
        when(borrowingHistoryRepository.findByActionDateBetweenOrderByActionDateDesc(startDate, endDate))
                .thenReturn(expectedHistory);

        // Act
        List<BorrowingHistory> result = borrowingHistoryService.getHistoryByDateRange(startDate, endDate);

        // Assert
        assertThat(result).isEqualTo(expectedHistory);
        verify(borrowingHistoryRepository).findByActionDateBetweenOrderByActionDateDesc(startDate, endDate);
    }

    @Test
    void getHistoryByDateRange_ShouldThrowException_WhenStartDateIsNull() {
        // Act & Assert
        assertThatThrownBy(() -> borrowingHistoryService.getHistoryByDateRange(null, LocalDateTime.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Start date cannot be null");
    }

    @Test
    void getHistoryByDateRange_ShouldThrowException_WhenEndDateIsNull() {
        // Act & Assert
        assertThatThrownBy(() -> borrowingHistoryService.getHistoryByDateRange(LocalDateTime.now(), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("End date cannot be null");
    }

    @Test
    void getHistoryByDateRange_ShouldThrowException_WhenStartDateIsAfterEndDate() {
        // Arrange
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = LocalDateTime.now().minusDays(1);

        // Act & Assert
        assertThatThrownBy(() -> borrowingHistoryService.getHistoryByDateRange(startDate, endDate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Start date cannot be after end date");
    }

    @Test
    void getTotalBorrowingsByBorrower_ShouldReturnCount_WhenValidBorrowerId() {
        // Arrange
        Long borrowerId = 1L;
        long expectedCount = 5L;
        when(borrowingHistoryRepository.countBorrowingsByBorrower(borrowerId)).thenReturn(expectedCount);

        // Act
        long result = borrowingHistoryService.getTotalBorrowingsByBorrower(borrowerId);

        // Assert
        assertThat(result).isEqualTo(expectedCount);
        verify(borrowingHistoryRepository).countBorrowingsByBorrower(borrowerId);
    }

    @Test
    void getTotalBorrowingsByBorrower_ShouldThrowException_WhenBorrowerIdIsNull() {
        // Act & Assert
        assertThatThrownBy(() -> borrowingHistoryService.getTotalBorrowingsByBorrower(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Borrower ID cannot be null");
    }

    @Test
    void getTotalBorrowingsForBook_ShouldReturnCount_WhenValidBookId() {
        // Arrange
        Long bookId = 1L;
        long expectedCount = 3L;
        when(borrowingHistoryRepository.countBorrowingsForBook(bookId)).thenReturn(expectedCount);

        // Act
        long result = borrowingHistoryService.getTotalBorrowingsForBook(bookId);

        // Assert
        assertThat(result).isEqualTo(expectedCount);
        verify(borrowingHistoryRepository).countBorrowingsForBook(bookId);
    }

    @Test
    void getTotalBorrowingsForBook_ShouldThrowException_WhenBookIdIsNull() {
        // Act & Assert
        assertThatThrownBy(() -> borrowingHistoryService.getTotalBorrowingsForBook(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Book ID cannot be null");
    }

    @Test
    void getMostPopularBooks_ShouldReturnPopularBooks_WhenValidLimit() {
        // Arrange
        int limit = 10;
        List<Object[]> expectedBooks = Arrays.asList(
                new Object[]{1L, 5L},
                new Object[]{2L, 3L}
        );
        when(borrowingHistoryRepository.findMostPopularBooks(any(PageRequest.class))).thenReturn(expectedBooks);

        // Act
        List<Object[]> result = borrowingHistoryService.getMostPopularBooks(limit);

        // Assert
        assertThat(result).isEqualTo(expectedBooks);
        verify(borrowingHistoryRepository).findMostPopularBooks(any(PageRequest.class));
    }

    @Test
    void getMostPopularBooks_ShouldThrowException_WhenLimitIsZero() {
        // Act & Assert
        assertThatThrownBy(() -> borrowingHistoryService.getMostPopularBooks(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Limit must be positive");
    }

    @Test
    void getMostPopularBooks_ShouldThrowException_WhenLimitIsNegative() {
        // Act & Assert
        assertThatThrownBy(() -> borrowingHistoryService.getMostPopularBooks(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Limit must be positive");
    }

    @Test
    void getMostActiveBorrowers_ShouldReturnActiveBorrowers_WhenValidLimit() {
        // Arrange
        int limit = 10;
        List<Object[]> expectedBorrowers = Arrays.asList(
                new Object[]{1L, 8L},
                new Object[]{2L, 5L}
        );
        when(borrowingHistoryRepository.findMostActiveBorrowers(any(PageRequest.class))).thenReturn(expectedBorrowers);

        // Act
        List<Object[]> result = borrowingHistoryService.getMostActiveBorrowers(limit);

        // Assert
        assertThat(result).isEqualTo(expectedBorrowers);
        verify(borrowingHistoryRepository).findMostActiveBorrowers(any(PageRequest.class));
    }

    @Test
    void getMostActiveBorrowers_ShouldThrowException_WhenLimitIsZero() {
        // Act & Assert
        assertThatThrownBy(() -> borrowingHistoryService.getMostActiveBorrowers(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Limit must be positive");
    }

    @Test
    void hasOverdueBooks_ShouldReturnTrue_WhenBorrowerHasOverdueBooks() {
        // Arrange
        Long borrowerId = 1L;
        BorrowingHistory overdueHistory = new BorrowingHistory(testBook, testBorrower, BorrowingHistory.ActionType.BORROWED);
        List<BorrowingHistory> overdueBooks = Collections.singletonList(overdueHistory);
        
        when(borrowingHistoryRepository.findOverdueBorrowingsByBorrower(eq(borrowerId), any(LocalDateTime.class)))
                .thenReturn(overdueBooks);

        // Act
        boolean result = borrowingHistoryService.hasOverdueBooks(borrowerId);

        // Assert
        assertThat(result).isTrue();
        verify(borrowingHistoryRepository).findOverdueBorrowingsByBorrower(eq(borrowerId), any(LocalDateTime.class));
    }

    @Test
    void hasOverdueBooks_ShouldReturnFalse_WhenBorrowerHasNoOverdueBooks() {
        // Arrange
        Long borrowerId = 1L;
        when(borrowingHistoryRepository.findOverdueBorrowingsByBorrower(eq(borrowerId), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        // Act
        boolean result = borrowingHistoryService.hasOverdueBooks(borrowerId);

        // Assert
        assertThat(result).isFalse();
        verify(borrowingHistoryRepository).findOverdueBorrowingsByBorrower(eq(borrowerId), any(LocalDateTime.class));
    }

    @Test
    void hasOverdueBooks_ShouldThrowException_WhenBorrowerIdIsNull() {
        // Act & Assert
        assertThatThrownBy(() -> borrowingHistoryService.hasOverdueBooks(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Borrower ID cannot be null");
    }

    @Test
    void getCurrentBorrowingCount_ShouldReturnCount_WhenValidBorrowerId() {
        // Arrange
        Long borrowerId = 1L;
        List<BorrowingHistory> currentBorrowings = Arrays.asList(borrowingHistory, borrowingHistory);
        when(borrowingHistoryRepository.findCurrentlyBorrowedBooksByBorrower(borrowerId)).thenReturn(currentBorrowings);

        // Act
        long result = borrowingHistoryService.getCurrentBorrowingCount(borrowerId);

        // Assert
        assertThat(result).isEqualTo(2L);
        verify(borrowingHistoryRepository).findCurrentlyBorrowedBooksByBorrower(borrowerId);
    }

    @Test
    void getCurrentBorrowingCount_ShouldThrowException_WhenBorrowerIdIsNull() {
        // Act & Assert
        assertThatThrownBy(() -> borrowingHistoryService.getCurrentBorrowingCount(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Borrower ID cannot be null");
    }
}