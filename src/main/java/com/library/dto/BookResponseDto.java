package com.library.dto;

import com.library.entity.Book;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Response DTO for book information with borrowing details")
public class BookResponseDto {
    
    @Schema(description = "Unique identifier of the book copy", example = "1")
    private Long id;
    @Schema(description = "International Standard Book Number", example = "978-0-123456-78-9")
    private String isbn;
    @Schema(description = "Title of the book", example = "The Great Gatsby")
    private String title;
    @Schema(description = "Author of the book", example = "F. Scott Fitzgerald")
    private String author;
    @Schema(description = "Whether the book is currently available for borrowing", example = "true")
    private boolean available;
    @Schema(description = "Information about the current borrower (null if book is available)")
    private BorrowerSummaryDto borrower;
    @Schema(description = "Timestamp when the book was borrowed (null if book is available)", example = "2025-07-31T10:15:30")
    private LocalDateTime borrowedAt;
    @Schema(description = "Timestamp when the book was registered", example = "2025-07-30T09:00:00")
    private LocalDateTime createdAt;
    @Schema(description = "Timestamp when the book information was last updated", example = "2025-07-31T10:15:30")
    private LocalDateTime updatedAt;

    public BookResponseDto() {
    }

    public BookResponseDto(Long id, String isbn, String title, String author, boolean available,
                          BorrowerSummaryDto borrower, LocalDateTime borrowedAt, 
                          LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.available = available;
        this.borrower = borrower;
        this.borrowedAt = borrowedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static BookResponseDto fromEntity(Book book) {
        BorrowerSummaryDto borrowerDto = null;
        if (book.getBorrower() != null) {
            borrowerDto = new BorrowerSummaryDto(
                    book.getBorrower().getId(),
                    book.getBorrower().getName(),
                    book.getBorrower().getEmail()
            );
        }

        return new BookResponseDto(
                book.getId(),
                book.getIsbn(),
                book.getTitle(),
                book.getAuthor(),
                book.isAvailable(),
                borrowerDto,
                book.getBorrowedAt(),
                book.getCreatedAt(),
                book.getUpdatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public BorrowerSummaryDto getBorrower() {
        return borrower;
    }

    public void setBorrower(BorrowerSummaryDto borrower) {
        this.borrower = borrower;
    }

    public LocalDateTime getBorrowedAt() {
        return borrowedAt;
    }

    public void setBorrowedAt(LocalDateTime borrowedAt) {
        this.borrowedAt = borrowedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Schema(description = "Summary information about the borrower")
    public static class BorrowerSummaryDto {
        @Schema(description = "Unique identifier of the borrower", example = "1")
        private Long id;
        @Schema(description = "Full name of the borrower", example = "John Doe")
        private String name;
        @Schema(description = "Email address of the borrower", example = "john.doe@email.com")
        private String email;

        public BorrowerSummaryDto() {
        }

        public BorrowerSummaryDto(Long id, String name, String email) {
            this.id = id;
            this.name = name;
            this.email = email;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }
}