package com.example.kalyan_kosh_api.dto;

import com.example.kalyan_kosh_api.entity.DeathCaseStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

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

    private String description;
    // userImage is handled as file upload (MultipartFile)

    // Nominee 1 Details
    @NotBlank
    private String nominee1Name;
    // nominee1QrCode is handled as file upload (MultipartFile)

    // Nominee 2 Details
    private String nominee2Name;
    // nominee2QrCode is handled as file upload (MultipartFile)

    // Account Details
    @NotNull
    @Valid
    private AccountDetailsDTO account1;

    private AccountDetailsDTO account2;

    private AccountDetailsDTO account3;

    @NotNull
    private LocalDate caseDate;

    @NotNull
    private DeathCaseStatus status;
}
