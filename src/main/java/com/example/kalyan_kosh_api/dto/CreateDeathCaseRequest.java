package com.example.kalyan_kosh_api.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateDeathCaseRequest {

    @NotBlank
    private String deceasedName;

    @NotBlank
    private String employeeCode;

    private String department;
    private String district;

    @NotBlank
    private String nomineeName;

    @NotBlank
    private String nomineeAccountNumber;

    @NotBlank
    private String nomineeIfsc;

    @Min(1) @Max(12)
    private int caseMonth;

    @Min(2020)
    private int caseYear;
}
