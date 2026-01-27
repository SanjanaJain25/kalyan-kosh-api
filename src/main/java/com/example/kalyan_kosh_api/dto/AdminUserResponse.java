package com.example.kalyan_kosh_api.dto;

import com.example.kalyan_kosh_api.entity.Role;
import com.example.kalyan_kosh_api.entity.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

/**
 * DTO for Admin User List Response with all details needed for admin panel
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserResponse {
    private String id;
    private String name;
    private String surname;
    private String fatherName;
    private String email;
    private String mobileNumber;
    private LocalDate dateOfBirth;
    
    // Location details
    private String departmentState;
    private String departmentSambhag;
    private String departmentDistrict;
    private String departmentBlock;
    
    // Professional details
    private String department;
    private String departmentUniqueId;
    private String schoolOfficeName;
    private String sankulName;
    
    // System fields
    private Role role;
    private UserStatus status;
    private Instant createdAt;
    private Instant updatedAt;
    
    // Additional fields for admin
    private String homeAddress;
    private Integer pincode;
    private LocalDate joiningDate;
    private LocalDate retirementDate;
}