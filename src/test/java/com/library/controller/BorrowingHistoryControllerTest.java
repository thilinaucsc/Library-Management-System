package com.library.controller;

import com.library.entity.Book;
import com.library.entity.Borrower;
import com.library.entity.BorrowingHistory;
import com.library.service.BorrowingHistoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BorrowingHistoryController.class)
class BorrowingHistoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BorrowingHistoryService borrowingHistoryService;

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
        borrowingHistory.setDueDate(LocalDateTime.now().plusDays(9)); // Not overdue

        returnHistory = new BorrowingHistory(testBook, testBorrower, BorrowingHistory.ActionType.RETURNED);
        returnHistory.setId(2L);
        returnHistory.setActionDate(LocalDateTime.now().minusDays(1));
    }

    @Test
    void getBorrowerHistory_ShouldReturnHistory_WhenValidBorrowerId() throws Exception {
        // Arrange
        Long borrowerId = 1L;
        List<BorrowingHistory> history = Arrays.asList(returnHistory, borrowingHistory);
        when(borrowingHistoryService.getHistoryByBorrowerId(borrowerId)).thenReturn(history);

        // Act & Assert
        mockMvc.perform(get("/api/history/borrowers/{borrowerId}", borrowerId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(2))) // Return history first (most recent)
                .andExpect(jsonPath("$[0].actionType", is("RETURNED")))
                .andExpect(jsonPath("$[0].book.id", is(1)))
                .andExpect(jsonPath("$[0].book.title", is("Effective Java")))
                .andExpect(jsonPath("$[0].borrower.id", is(1)))
                .andExpect(jsonPath("$[0].borrower.name", is("John Doe")))
                .andExpect(jsonPath("$[1].id", is(1))) // Borrowing history second
                .andExpect(jsonPath("$[1].actionType", is("BORROWED")))
                .andExpect(jsonPath("$[1].overdue", is(false)));
    }

    @Test
    void getBorrowerHistory_ShouldReturnEmptyList_WhenNoBorrowingHistory() throws Exception {
        // Arrange
        Long borrowerId = 1L;
        when(borrowingHistoryService.getHistoryByBorrowerId(borrowerId)).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/history/borrowers/{borrowerId}", borrowerId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getBorrowerHistoryPaginated_ShouldReturnPagedHistory_WhenValidParameters() throws Exception {
        // Arrange
        Long borrowerId = 1L;
        List<BorrowingHistory> historyList = Arrays.asList(returnHistory, borrowingHistory);
        Page<BorrowingHistory> historyPage = new PageImpl<>(historyList, PageRequest.of(0, 20), historyList.size());
        when(borrowingHistoryService.getHistoryByBorrowerId(eq(borrowerId), any(PageRequest.class))).thenReturn(historyPage);

        // Act & Assert
        mockMvc.perform(get("/api/history/borrowers/{borrowerId}/paginated", borrowerId)
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements", is(2)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.size", is(20)))
                .andExpect(jsonPath("$.number", is(0)));
    }

    @Test
    void getBookHistory_ShouldReturnHistory_WhenValidBookId() throws Exception {
        // Arrange
        Long bookId = 1L;
        List<BorrowingHistory> history = Arrays.asList(returnHistory, borrowingHistory);
        when(borrowingHistoryService.getHistoryByBookId(bookId)).thenReturn(history);

        // Act & Assert
        mockMvc.perform(get("/api/history/books/{bookId}", bookId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(2))) // Return history first
                .andExpect(jsonPath("$[0].actionType", is("RETURNED")))
                .andExpect(jsonPath("$[1].id", is(1))) // Borrowing history second
                .andExpect(jsonPath("$[1].actionType", is("BORROWED")));
    }

    @Test
    void getCurrentlyBorrowedBooks_ShouldReturnCurrentBorrowings_WhenValidBorrowerId() throws Exception {
        // Arrange
        Long borrowerId = 1L;
        List<BorrowingHistory> currentBorrowings = Collections.singletonList(borrowingHistory);
        when(borrowingHistoryService.getCurrentlyBorrowedBooksByBorrower(borrowerId)).thenReturn(currentBorrowings);

        // Act & Assert
        mockMvc.perform(get("/api/history/borrowers/{borrowerId}/current", borrowerId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].actionType", is("BORROWED")))
                .andExpect(jsonPath("$[0].book.title", is("Effective Java")));
    }

    @Test
    void getOverdueBooks_ShouldReturnOverdueBooks_WhenValidBorrowerId() throws Exception {
        // Arrange
        Long borrowerId = 1L;
        BorrowingHistory overdueHistory = new BorrowingHistory(testBook, testBorrower, BorrowingHistory.ActionType.BORROWED);
        overdueHistory.setId(3L);
        overdueHistory.setActionDate(LocalDateTime.now().minusDays(20));
        overdueHistory.setDueDate(LocalDateTime.now().minusDays(6)); // Overdue
        List<BorrowingHistory> overdueBooks = Collections.singletonList(overdueHistory);
        when(borrowingHistoryService.getOverdueBooksByBorrower(borrowerId)).thenReturn(overdueBooks);

        // Act & Assert
        mockMvc.perform(get("/api/history/borrowers/{borrowerId}/overdue", borrowerId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(3)))
                .andExpect(jsonPath("$[0].actionType", is("BORROWED")))
                .andExpect(jsonPath("$[0].overdue", is(true)));
    }

    @Test
    void getAllOverdueBooks_ShouldReturnAllOverdueBooks() throws Exception {
        // Arrange
        BorrowingHistory overdueHistory = new BorrowingHistory(testBook, testBorrower, BorrowingHistory.ActionType.BORROWED);
        overdueHistory.setId(3L);
        overdueHistory.setActionDate(LocalDateTime.now().minusDays(20));
        overdueHistory.setDueDate(LocalDateTime.now().minusDays(6)); // Overdue
        List<BorrowingHistory> overdueBooks = Collections.singletonList(overdueHistory);
        when(borrowingHistoryService.getAllOverdueBooks()).thenReturn(overdueBooks);

        // Act & Assert
        mockMvc.perform(get("/api/history/overdue"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(3)))
                .andExpect(jsonPath("$[0].actionType", is("BORROWED")))
                .andExpect(jsonPath("$[0].overdue", is(true)));
    }

    @Test
    void getHistoryByDateRange_ShouldReturnHistory_WhenValidDateRange() throws Exception {
        // Arrange
        List<BorrowingHistory> history = Arrays.asList(returnHistory, borrowingHistory);
        when(borrowingHistoryService.getHistoryByDateRange(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(history);

        // Act & Assert
        mockMvc.perform(get("/api/history/date-range")
                .param("startDate", "2023-07-01T00:00:00")
                .param("endDate", "2023-07-31T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(2)))
                .andExpect(jsonPath("$[1].id", is(1)));
    }

    @Test
    void getMostPopularBooks_ShouldReturnPopularBooks_WhenValidLimit() throws Exception {
        // Arrange
        List<Object[]> popularBooks = Arrays.asList(
                new Object[]{1L, 5L},
                new Object[]{2L, 3L}
        );
        when(borrowingHistoryService.getMostPopularBooks(10)).thenReturn(popularBooks);

        // Act & Assert
        mockMvc.perform(get("/api/history/statistics/popular-books")
                .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0][0]", is(1))) // Book ID
                .andExpect(jsonPath("$[0][1]", is(5))) // Borrow count
                .andExpect(jsonPath("$[1][0]", is(2))) // Book ID
                .andExpect(jsonPath("$[1][1]", is(3))); // Borrow count
    }

    @Test
    void getMostActiveBorrowers_ShouldReturnActiveBorrowers_WhenValidLimit() throws Exception {
        // Arrange
        List<Object[]> activeBorrowers = Arrays.asList(
                new Object[]{1L, 8L},
                new Object[]{2L, 5L}
        );
        when(borrowingHistoryService.getMostActiveBorrowers(10)).thenReturn(activeBorrowers);

        // Act & Assert
        mockMvc.perform(get("/api/history/statistics/active-borrowers")
                .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0][0]", is(1))) // Borrower ID
                .andExpect(jsonPath("$[0][1]", is(8))) // Borrow count
                .andExpect(jsonPath("$[1][0]", is(2))) // Borrower ID
                .andExpect(jsonPath("$[1][1]", is(5))); // Borrow count
    }

    @Test
    void getBorrowerStatistics_ShouldReturnStatistics_WhenValidBorrowerId() throws Exception {
        // Arrange
        Long borrowerId = 1L;
        when(borrowingHistoryService.getTotalBorrowingsByBorrower(borrowerId)).thenReturn(25L);
        when(borrowingHistoryService.getCurrentBorrowingCount(borrowerId)).thenReturn(3L);
        when(borrowingHistoryService.hasOverdueBooks(borrowerId)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/api/history/borrowers/{borrowerId}/statistics", borrowerId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.borrowerId", is(1)))
                .andExpect(jsonPath("$.totalBorrowings", is(25)))
                .andExpect(jsonPath("$.currentBorrowings", is(3)))
                .andExpect(jsonPath("$.hasOverdueBooks", is(false)));
    }

    @Test
    void getBorrowerStatistics_ShouldReturnStatisticsWithOverdue_WhenBorrowerHasOverdueBooks() throws Exception {
        // Arrange
        Long borrowerId = 1L;
        when(borrowingHistoryService.getTotalBorrowingsByBorrower(borrowerId)).thenReturn(15L);
        when(borrowingHistoryService.getCurrentBorrowingCount(borrowerId)).thenReturn(2L);
        when(borrowingHistoryService.hasOverdueBooks(borrowerId)).thenReturn(true);

        // Act & Assert
        mockMvc.perform(get("/api/history/borrowers/{borrowerId}/statistics", borrowerId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.borrowerId", is(1)))
                .andExpect(jsonPath("$.totalBorrowings", is(15)))
                .andExpect(jsonPath("$.currentBorrowings", is(2)))
                .andExpect(jsonPath("$.hasOverdueBooks", is(true)));
    }

    @Test
    void getBorrowerHistoryPaginated_ShouldUseDefaultPagination_WhenNoParametersProvided() throws Exception {
        // Arrange
        Long borrowerId = 1L;
        List<BorrowingHistory> historyList = Collections.singletonList(borrowingHistory);
        Page<BorrowingHistory> historyPage = new PageImpl<>(historyList, PageRequest.of(0, 20), historyList.size());
        when(borrowingHistoryService.getHistoryByBorrowerId(eq(borrowerId), any(PageRequest.class))).thenReturn(historyPage);

        // Act & Assert
        mockMvc.perform(get("/api/history/borrowers/{borrowerId}/paginated", borrowerId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.size", is(20))) // Default size
                .andExpect(jsonPath("$.number", is(0))); // Default page
    }

    @Test
    void getMostPopularBooks_ShouldUseDefaultLimit_WhenNoLimitProvided() throws Exception {
        // Arrange
        List<Object[]> popularBooks = Collections.singletonList(new Object[]{1L, 5L});
        when(borrowingHistoryService.getMostPopularBooks(10)).thenReturn(popularBooks); // Default limit is 10

        // Act & Assert
        mockMvc.perform(get("/api/history/statistics/popular-books"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getMostActiveBorrowers_ShouldUseDefaultLimit_WhenNoLimitProvided() throws Exception {
        // Arrange
        List<Object[]> activeBorrowers = Collections.singletonList(new Object[]{1L, 8L});
        when(borrowingHistoryService.getMostActiveBorrowers(10)).thenReturn(activeBorrowers); // Default limit is 10

        // Act & Assert
        mockMvc.perform(get("/api/history/statistics/active-borrowers"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)));
    }
}