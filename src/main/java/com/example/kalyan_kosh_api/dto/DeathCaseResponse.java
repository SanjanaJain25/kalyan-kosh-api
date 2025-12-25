package com.example.kalyan_kosh_api.dto;

import com.example.kalyan_kosh_api.entity.DeathCaseStatus;
import lombok.Data;

import java.time.Instant;

@Data
public class DeathCaseResponse {

    private Long id;
    private String deceasedName;
    private String employeeCode;
    private String department;
    private String district;

    private String nomineeName;
    private String nomineeAccountNumber;
    private String nomineeIfsc;

    private int caseMonth;
    private int caseYear;

    private DeathCaseStatus status;

    private String createdBy;
    private String updatedBy;

    private Instant createdAt;
    private Instant updatedAt;
}
