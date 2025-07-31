package com.library.controller;

import com.library.dto.BorrowerRequestDto;
import com.library.dto.BorrowerResponseDto;
import com.library.entity.Borrower;
import com.library.exception.GlobalExceptionHandler;
import com.library.service.BorrowerService;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/borrowers")
@Tag(name = "Borrower Management", description = "API endpoints for managing library borrowers")
public class BorrowerController {

    private final BorrowerService borrowerService;

    @Autowired
    public BorrowerController(BorrowerService borrowerService) {
        this.borrowerService = borrowerService;
    }

    @PostMapping
    @Operation(
        summary = "Register a new borrower",
        description = "Creates a new borrower in the library system with unique email validation"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Borrower created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BorrowerResponseDto.class),
                examples = @ExampleObject(
                    name = "Successful registration",
                    value = """
                        {
                          "id": 1,
                          "name": "John Doe",
                          "email": "john.doe@email.com",
                          "registrationDate": "2025-07-31T10:30:00"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request data or email already exists",
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
                                "email": "Email must be valid",
                                "name": "Name is required"
                              }
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "Email already exists",
                        value = """
                            {
                              "code": "INVALID_REQUEST",
                              "message": "Email already exists: john.doe@email.com",
                              "timestamp": "2025-07-31T10:30:00"
                            }
                            """
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Internal server error",
                    value = """
                        {
                          "code": "INTERNAL_SERVER_ERROR",
                          "message": "An unexpected error occurred",
                          "timestamp": "2025-07-31T10:30:00"
                        }
                        """
                )
            )
        )
    })
    public ResponseEntity<BorrowerResponseDto> registerBorrower(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Borrower registration details",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BorrowerRequestDto.class),
                examples = @ExampleObject(
                    name = "Borrower registration example",
                    value = """
                        {
                          "name": "John Doe",
                          "email": "john.doe@email.com"
                        }
                        """
                )
            )
        )
        @Valid @RequestBody BorrowerRequestDto request) {
        Borrower borrower = borrowerService.registerBorrower(request.getName(), request.getEmail());
        BorrowerResponseDto response = BorrowerResponseDto.fromEntity(borrower);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}