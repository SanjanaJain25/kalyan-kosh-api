package com.example.kalyan_kosh_api.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@Builder
public class AdminReportUserRowResponse {

    private String id;

    private String name;
    private String surname;
    private String fullName;
    private String fatherName;

    private String email;
    private String mobileNumber;

    private String department;
    private String departmentUniqueId;
    private String schoolOfficeName;
    private String sankulName;

    private String departmentState;
    private String departmentSambhag;
    private String departmentDistrict;
    private String departmentBlock;

    private LocalDate joiningDate;
    private LocalDate retirementDate;
    private LocalDate dateOfBirth;

    private Instant createdAt;
    private Instant updatedAt;
    private Instant lastLoginAt;

    private String role;
    private String status;
}