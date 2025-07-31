package com.library.dto;

import com.library.entity.Borrower;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Response DTO for borrower information")
public class BorrowerResponseDto {
    
    @Schema(description = "Unique identifier of the borrower", example = "1")
    private Long id;
    @Schema(description = "Full name of the borrower", example = "John Doe")
    private String name;
    @Schema(description = "Email address of the borrower", example = "john.doe@email.com")
    private String email;
    @Schema(description = "Timestamp when the borrower was registered", example = "2025-07-30T09:00:00")
    private LocalDateTime createdAt;
    @Schema(description = "Timestamp when the borrower information was last updated", example = "2025-07-31T10:15:30")
    private LocalDateTime updatedAt;

    public BorrowerResponseDto() {
    }

    public BorrowerResponseDto(Long id, String name, String email, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static BorrowerResponseDto fromEntity(Borrower borrower) {
        return new BorrowerResponseDto(
                borrower.getId(),
                borrower.getName(),
                borrower.getEmail(),
                borrower.getCreatedAt(),
                borrower.getUpdatedAt()
        );
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
}