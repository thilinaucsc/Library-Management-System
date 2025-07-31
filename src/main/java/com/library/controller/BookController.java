package com.library.controller;

import com.library.dto.BookRequestDto;
import com.library.dto.BookResponseDto;
import com.library.dto.BorrowRequestDto;
import com.library.entity.Book;
import com.library.exception.GlobalExceptionHandler;
import com.library.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/books")
@Tag(name = "Book Management", description = "API endpoints for managing library books, including borrowing and returning")
public class BookController {

    private final BookService bookService;

    @Autowired
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping
    @Operation(
        summary = "Add a new book",
        description = "Creates a new book in the library system. Multiple copies of the same book (same ISBN) can exist with different IDs. Books with same ISBN must have identical title and author."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Book created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BookResponseDto.class),
                examples = @ExampleObject(
                    name = "Successful book creation",
                    value = """
                        {
                          "id": 1,
                          "isbn": "978-0-13-110362-7",
                          "title": "Effective Java",
                          "author": "Joshua Bloch",
                          "available": true,
                          "borrower": null
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request data or ISBN consistency violation",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "Validation error",
                        value = """
                            {
                              "code": "VALIDATION_FAILED",
                              "message": "Request validation failed",
                              "timestamp": "2025-07-31T10:30:00",
                              "fieldErrors": {
                                "isbn": "ISBN is required",
                                "title": "Title is required"
                              }
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "ISBN consistency violation",
                        value = """
                            {
                              "code": "INVALID_REQUEST",
                              "message": "ISBN 978-0-13-110362-7 already exists with different title or author",
                              "timestamp": "2025-07-31T10:30:00"
                            }
                            """
                    )
                }
            )
        )
    })
    public ResponseEntity<BookResponseDto> addBook(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Book details to be added",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BookRequestDto.class),
                examples = @ExampleObject(
                    name = "Book creation example",
                    value = """
                        {
                          "isbn": "978-0-13-110362-7",
                          "title": "Effective Java",
                          "author": "Joshua Bloch"
                        }
                        """
                )
            )
        )
        @Valid @RequestBody BookRequestDto request) {
        Book book = bookService.addBook(request.getIsbn(), request.getTitle(), request.getAuthor());
        BookResponseDto response = BookResponseDto.fromEntity(book);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(
        summary = "Get all books",
        description = "Retrieves all books in the library system with their current availability status and borrower information if borrowed."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Books retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = BookResponseDto.class)),
                examples = @ExampleObject(
                    name = "Books list example",
                    value = """
                        [
                          {
                            "id": 1,
                            "isbn": "978-0-13-110362-7",
                            "title": "Effective Java",
                            "author": "Joshua Bloch",
                            "available": true,
                            "borrower": null
                          },
                          {
                            "id": 2,
                            "isbn": "978-0-13-110362-7",
                            "title": "Effective Java",
                            "author": "Joshua Bloch",
                            "available": false,
                            "borrower": {
                              "id": 1,
                              "name": "John Doe",
                              "email": "john.doe@email.com"
                            }
                          }
                        ]
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)
            )
        )
    })
    public ResponseEntity<List<BookResponseDto>> getAllBooks() {
        List<Book> books = bookService.getAllBooks();
        List<BookResponseDto> response = books.stream()
                .map(BookResponseDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{bookId}/borrow")
    @Operation(
        summary = "Borrow a book",
        description = "Allows a borrower to borrow an available book by providing the book ID and borrower ID. Only one borrower can borrow a specific book copy at a time."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Book borrowed successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BookResponseDto.class),
                examples = @ExampleObject(
                    name = "Successful book borrowing",
                    value = """
                        {
                          "id": 1,
                          "isbn": "978-0-13-110362-7",
                          "title": "Effective Java",
                          "author": "Joshua Bloch",
                          "available": false,
                          "borrower": {
                            "id": 1,
                            "name": "John Doe",
                            "email": "john.doe@email.com"
                          }
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request data, book not found, or borrower not found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "Book not found",
                        value = """
                            {
                              "code": "INVALID_REQUEST",
                              "message": "Book not found with ID: 999",
                              "timestamp": "2025-07-31T10:30:00"
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "Borrower not found",
                        value = """
                            {
                              "code": "INVALID_REQUEST",
                              "message": "Borrower not found with ID: 999",
                              "timestamp": "2025-07-31T10:30:00"
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "Validation error",
                        value = """
                            {
                              "code": "VALIDATION_FAILED",
                              "message": "Request validation failed",
                              "timestamp": "2025-07-31T10:30:00",
                              "fieldErrors": {
                                "borrowerId": "Borrower ID is required"
                              }
                            }
                            """
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Business rule violation - no available copies",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class),
                examples = @ExampleObject(
                    name = "No available copies",
                    value = """
                        {
                          "code": "BUSINESS_RULE_VIOLATION",
                          "message": "No available copies found for ISBN: 978-0-13-110362-7",
                          "timestamp": "2025-07-31T10:30:00"
                        }
                        """
                )
            )
        )
    })
    public ResponseEntity<BookResponseDto> borrowBook(
            @Parameter(description = "ID of the book to borrow", example = "1")
            @PathVariable Long bookId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Borrowing request with borrower ID",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = BorrowRequestDto.class),
                    examples = @ExampleObject(
                        name = "Borrow request example",
                        value = """
                            {
                              "borrowerId": 1
                            }
                            """
                    )
                )
            )
            @Valid @RequestBody BorrowRequestDto request) {
        
        // Borrow the specific book by ID
        Book borrowedBook = bookService.borrowBookById(bookId, request.getBorrowerId());
        BookResponseDto response = BookResponseDto.fromEntity(borrowedBook);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{bookId}/return")
    @Operation(
        summary = "Return a book",
        description = "Processes the return of a borrowed book by book ID. The book must be currently borrowed to be returned."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Book returned successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BookResponseDto.class),
                examples = @ExampleObject(
                    name = "Successful book return",
                    value = """
                        {
                          "id": 1,
                          "isbn": "978-0-13-110362-7",
                          "title": "Effective Java",
                          "author": "Joshua Bloch",
                          "available": true,
                          "borrower": null
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Book not found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Book not found",
                    value = """
                        {
                          "code": "INVALID_REQUEST",
                          "message": "Book not found with ID: 999",
                          "timestamp": "2025-07-31T10:30:00"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Business rule violation - book not currently borrowed",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Book not borrowed",
                    value = """
                        {
                          "code": "BUSINESS_RULE_VIOLATION",
                          "message": "Book with ID 1 is not currently borrowed",
                          "timestamp": "2025-07-31T10:30:00"
                        }
                        """
                )
            )
        )
    })
    public ResponseEntity<BookResponseDto> returnBook(
            @Parameter(description = "ID of the book to return", example = "1")
            @PathVariable Long bookId) {
        Book returnedBook = bookService.returnBook(bookId);
        BookResponseDto response = BookResponseDto.fromEntity(returnedBook);
        return ResponseEntity.ok(response);
    }
}