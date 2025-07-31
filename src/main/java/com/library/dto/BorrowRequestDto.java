package com.library.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request DTO for borrowing a book")
public class BorrowRequestDto {
    
    @Schema(description = "Unique identifier of the borrower who wants to borrow the book", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Borrower ID is required")
    private Long borrowerId;

    public BorrowRequestDto() {
    }

    public BorrowRequestDto(Long borrowerId) {
        this.borrowerId = borrowerId;
    }

    public Long getBorrowerId() {
        return borrowerId;
    }

    public void setBorrowerId(Long borrowerId) {
        this.borrowerId = borrowerId;
    }
}