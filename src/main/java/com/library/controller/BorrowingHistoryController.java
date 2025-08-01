package com.library.controller;

import com.library.dto.BorrowingHistoryResponseDto;
import com.library.entity.BorrowingHistory;
import com.library.service.BorrowingHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/history")
@Tag(name = "Borrowing History", description = "Operations related to borrowing history management")
public class BorrowingHistoryController {

    private final BorrowingHistoryService borrowingHistoryService;

    @Autowired
    public BorrowingHistoryController(BorrowingHistoryService borrowingHistoryService) {
        this.borrowingHistoryService = borrowingHistoryService;
    }

    @GetMapping("/borrowers/{borrowerId}")
    @Operation(summary = "Get borrowing history for a borrower", 
               description = "Retrieve all borrowing history records for a specific borrower")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved borrowing history",
                    content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = BorrowingHistoryResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid borrower ID provided"),
            @ApiResponse(responseCode = "404", description = "Borrower not found")
    })
    public ResponseEntity<List<BorrowingHistoryResponseDto>> getBorrowerHistory(
            @Parameter(description = "ID of the borrower", required = true, example = "1")
            @PathVariable Long borrowerId) {
        
        List<BorrowingHistory> history = borrowingHistoryService.getHistoryByBorrowerId(borrowerId);
        List<BorrowingHistoryResponseDto> response = history.stream()
                .map(BorrowingHistoryResponseDto::fromEntity)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/borrowers/{borrowerId}/paginated")
    @Operation(summary = "Get paginated borrowing history for a borrower", 
               description = "Retrieve paginated borrowing history records for a specific borrower")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved paginated borrowing history"),
            @ApiResponse(responseCode = "400", description = "Invalid borrower ID or pagination parameters provided")
    })
    public ResponseEntity<Page<BorrowingHistoryResponseDto>> getBorrowerHistoryPaginated(
            @Parameter(description = "ID of the borrower", required = true, example = "1")
            @PathVariable Long borrowerId,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of records per page", example = "20")
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<BorrowingHistory> historyPage = borrowingHistoryService.getHistoryByBorrowerId(borrowerId, pageable);
        Page<BorrowingHistoryResponseDto> response = historyPage.map(BorrowingHistoryResponseDto::fromEntity);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/books/{bookId}")
    @Operation(summary = "Get borrowing history for a book", 
               description = "Retrieve all borrowing history records for a specific book")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved borrowing history",
                    content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = BorrowingHistoryResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid book ID provided"),
            @ApiResponse(responseCode = "404", description = "Book not found")
    })
    public ResponseEntity<List<BorrowingHistoryResponseDto>> getBookHistory(
            @Parameter(description = "ID of the book", required = true, example = "1")
            @PathVariable Long bookId) {
        
        List<BorrowingHistory> history = borrowingHistoryService.getHistoryByBookId(bookId);
        List<BorrowingHistoryResponseDto> response = history.stream()
                .map(BorrowingHistoryResponseDto::fromEntity)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/borrowers/{borrowerId}/current")
    @Operation(summary = "Get currently borrowed books by a borrower", 
               description = "Retrieve all books currently borrowed by a specific borrower")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved currently borrowed books",
                    content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = BorrowingHistoryResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid borrower ID provided")
    })
    public ResponseEntity<List<BorrowingHistoryResponseDto>> getCurrentlyBorrowedBooks(
            @Parameter(description = "ID of the borrower", required = true, example = "1")
            @PathVariable Long borrowerId) {
        
        List<BorrowingHistory> currentBorrowings = borrowingHistoryService.getCurrentlyBorrowedBooksByBorrower(borrowerId);
        List<BorrowingHistoryResponseDto> response = currentBorrowings.stream()
                .map(BorrowingHistoryResponseDto::fromEntity)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/borrowers/{borrowerId}/overdue")
    @Operation(summary = "Get overdue books for a borrower", 
               description = "Retrieve all overdue books for a specific borrower")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved overdue books",
                    content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = BorrowingHistoryResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid borrower ID provided")
    })
    public ResponseEntity<List<BorrowingHistoryResponseDto>> getOverdueBooks(
            @Parameter(description = "ID of the borrower", required = true, example = "1")
            @PathVariable Long borrowerId) {
        
        List<BorrowingHistory> overdueBooks = borrowingHistoryService.getOverdueBooksByBorrower(borrowerId);
        List<BorrowingHistoryResponseDto> response = overdueBooks.stream()
                .map(BorrowingHistoryResponseDto::fromEntity)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/overdue")
    @Operation(summary = "Get all overdue books", 
               description = "Retrieve all overdue books in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved all overdue books",
                    content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = BorrowingHistoryResponseDto.class)))
    })
    public ResponseEntity<List<BorrowingHistoryResponseDto>> getAllOverdueBooks() {
        List<BorrowingHistory> overdueBooks = borrowingHistoryService.getAllOverdueBooks();
        List<BorrowingHistoryResponseDto> response = overdueBooks.stream()
                .map(BorrowingHistoryResponseDto::fromEntity)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/date-range")
    @Operation(summary = "Get borrowing history by date range", 
               description = "Retrieve borrowing history records within a specific date range")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved borrowing history",
                    content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = BorrowingHistoryResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid date range provided")
    })
    public ResponseEntity<List<BorrowingHistoryResponseDto>> getHistoryByDateRange(
            @Parameter(description = "Start date (ISO format)", required = true, example = "2023-07-01T00:00:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date (ISO format)", required = true, example = "2023-07-31T23:59:59")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        List<BorrowingHistory> history = borrowingHistoryService.getHistoryByDateRange(startDate, endDate);
        List<BorrowingHistoryResponseDto> response = history.stream()
                .map(BorrowingHistoryResponseDto::fromEntity)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/statistics/popular-books")
    @Operation(summary = "Get most popular books", 
               description = "Retrieve the most borrowed books in order of popularity")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved popular books statistics"),
            @ApiResponse(responseCode = "400", description = "Invalid limit parameter")
    })
    public ResponseEntity<List<Object[]>> getMostPopularBooks(
            @Parameter(description = "Maximum number of books to return", example = "10")
            @RequestParam(defaultValue = "10") int limit) {
        
        List<Object[]> popularBooks = borrowingHistoryService.getMostPopularBooks(limit);
        return ResponseEntity.ok(popularBooks);
    }

    @GetMapping("/statistics/active-borrowers")
    @Operation(summary = "Get most active borrowers", 
               description = "Retrieve the most active borrowers in order of borrowing activity")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved active borrowers statistics"),
            @ApiResponse(responseCode = "400", description = "Invalid limit parameter")
    })
    public ResponseEntity<List<Object[]>> getMostActiveBorrowers(
            @Parameter(description = "Maximum number of borrowers to return", example = "10")
            @RequestParam(defaultValue = "10") int limit) {
        
        List<Object[]> activeBorrowers = borrowingHistoryService.getMostActiveBorrowers(limit);
        return ResponseEntity.ok(activeBorrowers);
    }

    @GetMapping("/borrowers/{borrowerId}/statistics")
    @Operation(summary = "Get borrower statistics", 
               description = "Retrieve borrowing statistics for a specific borrower")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved borrower statistics"),
            @ApiResponse(responseCode = "400", description = "Invalid borrower ID provided")
    })
    public ResponseEntity<BorrowerStatisticsDto> getBorrowerStatistics(
            @Parameter(description = "ID of the borrower", required = true, example = "1")
            @PathVariable Long borrowerId) {
        
        long totalBorrowings = borrowingHistoryService.getTotalBorrowingsByBorrower(borrowerId);
        long currentBorrowings = borrowingHistoryService.getCurrentBorrowingCount(borrowerId);
        boolean hasOverdue = borrowingHistoryService.hasOverdueBooks(borrowerId);
        
        BorrowerStatisticsDto statistics = new BorrowerStatisticsDto(
                borrowerId, totalBorrowings, currentBorrowings, hasOverdue);
        
        return ResponseEntity.ok(statistics);
    }

    @Schema(description = "Borrower statistics summary")
    public static class BorrowerStatisticsDto {
        @Schema(description = "Borrower ID", example = "1")
        private Long borrowerId;

        @Schema(description = "Total number of books borrowed by this borrower", example = "25")
        private Long totalBorrowings;

        @Schema(description = "Number of books currently borrowed", example = "3")
        private Long currentBorrowings;

        @Schema(description = "Whether the borrower has any overdue books", example = "false")
        private Boolean hasOverdueBooks;

        public BorrowerStatisticsDto() {}

        public BorrowerStatisticsDto(Long borrowerId, Long totalBorrowings, Long currentBorrowings, Boolean hasOverdueBooks) {
            this.borrowerId = borrowerId;
            this.totalBorrowings = totalBorrowings;
            this.currentBorrowings = currentBorrowings;
            this.hasOverdueBooks = hasOverdueBooks;
        }

        // Getters and setters
        public Long getBorrowerId() { return borrowerId; }
        public void setBorrowerId(Long borrowerId) { this.borrowerId = borrowerId; }
        public Long getTotalBorrowings() { return totalBorrowings; }
        public void setTotalBorrowings(Long totalBorrowings) { this.totalBorrowings = totalBorrowings; }
        public Long getCurrentBorrowings() { return currentBorrowings; }
        public void setCurrentBorrowings(Long currentBorrowings) { this.currentBorrowings = currentBorrowings; }
        public Boolean getHasOverdueBooks() { return hasOverdueBooks; }
        public void setHasOverdueBooks(Boolean hasOverdueBooks) { this.hasOverdueBooks = hasOverdueBooks; }
    }
}