package com.library.dto;

import com.library.entity.BorrowingHistory;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Borrowing history response containing borrowing/returning event details")
public class BorrowingHistoryResponseDto {

    @Schema(description = "Unique identifier of the borrowing history record", example = "1")
    private Long id;

    @Schema(description = "Book information")
    private BookSummaryDto book;

    @Schema(description = "Borrower information")
    private BorrowerSummaryDto borrower;

    @Schema(description = "Type of action performed", example = "BORROWED", allowableValues = {"BORROWED", "RETURNED"})
    private String actionType;

    @Schema(description = "Date and time when the action was performed", example = "2023-07-30T14:30:00")
    private LocalDateTime actionDate;

    @Schema(description = "Due date for borrowed books (null for returned books)", example = "2023-08-13T14:30:00")
    private LocalDateTime dueDate;

    @Schema(description = "Indicates if the book is overdue (only applicable for BORROWED records)", example = "false")
    private Boolean overdue;

    @Schema(description = "Number of days until due date (negative if overdue)", example = "5")
    private Long daysUntilDue;

    @Schema(description = "Record creation timestamp", example = "2023-07-30T14:30:00")
    private LocalDateTime createdAt;

    // Nested DTOs for summary information
    @Schema(description = "Book summary information")
    public static class BookSummaryDto {
        @Schema(description = "Book ID", example = "1")
        private Long id;

        @Schema(description = "ISBN", example = "9781234567890")
        private String isbn;

        @Schema(description = "Book title", example = "The Great Gatsby")
        private String title;

        @Schema(description = "Book author", example = "F. Scott Fitzgerald")
        private String author;

        public BookSummaryDto() {}

        public BookSummaryDto(Long id, String isbn, String title, String author) {
            this.id = id;
            this.isbn = isbn;
            this.title = title;
            this.author = author;
        }

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getIsbn() { return isbn; }
        public void setIsbn(String isbn) { this.isbn = isbn; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getAuthor() { return author; }
        public void setAuthor(String author) { this.author = author; }
    }

    @Schema(description = "Borrower summary information")
    public static class BorrowerSummaryDto {
        @Schema(description = "Borrower ID", example = "1")
        private Long id;

        @Schema(description = "Borrower name", example = "John Doe")
        private String name;

        @Schema(description = "Borrower email", example = "john.doe@example.com")
        private String email;

        public BorrowerSummaryDto() {}

        public BorrowerSummaryDto(Long id, String name, String email) {
            this.id = id;
            this.name = name;
            this.email = email;
        }

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    public BorrowingHistoryResponseDto() {}

    public static BorrowingHistoryResponseDto fromEntity(BorrowingHistory history) {
        BorrowingHistoryResponseDto dto = new BorrowingHistoryResponseDto();
        dto.setId(history.getId());
        dto.setActionType(history.getActionType().name());
        dto.setActionDate(history.getActionDate());
        dto.setDueDate(history.getDueDate());
        dto.setCreatedAt(history.getCreatedAt());

        // Set book information
        if (history.getBook() != null) {
            dto.setBook(new BookSummaryDto(
                    history.getBook().getId(),
                    history.getBook().getIsbn(),
                    history.getBook().getTitle(),
                    history.getBook().getAuthor()
            ));
        }

        // Set borrower information
        if (history.getBorrower() != null) {
            dto.setBorrower(new BorrowerSummaryDto(
                    history.getBorrower().getId(),
                    history.getBorrower().getName(),
                    history.getBorrower().getEmail()
            ));
        }

        // Calculate overdue status and days until due
        if (history.getActionType() == BorrowingHistory.ActionType.BORROWED) {
            dto.setOverdue(history.isOverdue());
            dto.setDaysUntilDue(history.getDaysUntilDue());
        } else {
            dto.setOverdue(false);
            dto.setDaysUntilDue(0L);
        }

        return dto;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BookSummaryDto getBook() {
        return book;
    }

    public void setBook(BookSummaryDto book) {
        this.book = book;
    }

    public BorrowerSummaryDto getBorrower() {
        return borrower;
    }

    public void setBorrower(BorrowerSummaryDto borrower) {
        this.borrower = borrower;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public LocalDateTime getActionDate() {
        return actionDate;
    }

    public void setActionDate(LocalDateTime actionDate) {
        this.actionDate = actionDate;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public Boolean getOverdue() {
        return overdue;
    }

    public void setOverdue(Boolean overdue) {
        this.overdue = overdue;
    }

    public Long getDaysUntilDue() {
        return daysUntilDue;
    }

    public void setDaysUntilDue(Long daysUntilDue) {
        this.daysUntilDue = daysUntilDue;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}