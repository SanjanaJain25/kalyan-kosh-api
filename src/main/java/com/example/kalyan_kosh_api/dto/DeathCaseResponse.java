package com.example.kalyan_kosh_api.dto;

import com.example.kalyan_kosh_api.entity.DeathCaseStatus;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;

@Data
public class DeathCaseResponse {

    private Long id;
    private String deceasedName;
    private String employeeCode;
    private String department;
    private String district;

    private String description;
    private String userImage;

    // Nominee 1 Details
    private String nominee1Name;
    private String nominee1QrCode;

    // Nominee 2 Details
    private String nominee2Name;
    private String nominee2QrCode;

    // Account Details
    private AccountDetailsDTO account1;
    private AccountDetailsDTO account2;
    private AccountDetailsDTO account3;

    private LocalDate caseDate;

    private DeathCaseStatus status;

    private String createdBy;
    private String updatedBy;

    private Instant createdAt;
    private Instant updatedAt;
}
