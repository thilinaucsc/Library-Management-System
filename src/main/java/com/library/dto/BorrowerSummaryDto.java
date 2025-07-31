package com.library.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Summary information about the borrower")
public class BorrowerSummaryDto {
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
