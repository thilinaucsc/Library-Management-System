package com.library.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request DTO for book registration")
public class BookRequestDto {
    
    @Schema(description = "International Standard Book Number", example = "978-0-123456-78-9", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "ISBN is required")
    @Size(max = 20, message = "ISBN cannot exceed 20 characters")
    private String isbn;
    
    @Schema(description = "Title of the book", example = "The Great Gatsby", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 500, message = "Title must be between 1 and 500 characters")
    private String title;
    
    @Schema(description = "Author of the book", example = "F. Scott Fitzgerald", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Author is required")
    @Size(min = 1, max = 200, message = "Author must be between 1 and 200 characters")
    private String author;

    public BookRequestDto() {
    }

    public BookRequestDto(String isbn, String title, String author) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
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
}