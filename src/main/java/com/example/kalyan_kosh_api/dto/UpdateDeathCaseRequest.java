package com.example.kalyan_kosh_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateDeathCaseRequest {

    @NotBlank
    private String deceasedName;

    @NotBlank
    private String employeeCode;

    @NotBlank
    private String department;

    @NotBlank
    private String district;

    @NotBlank
    private String nomineeName;

    @NotBlank
    private String nomineeAccountNumber;

    @NotBlank
    private String nomineeIfsc;

    @NotNull
    private Integer caseMonth;

    @NotNull
    private Integer caseYear;
}
