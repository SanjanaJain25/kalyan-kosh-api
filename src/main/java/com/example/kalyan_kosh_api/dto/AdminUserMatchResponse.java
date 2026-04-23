package com.example.kalyan_kosh_api.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminUserMatchResponse {
    private boolean matchFound;
    private String message;

    private String matchedBy; // DEPARTMENT_UNIQUE_ID / MOBILE_NUMBER / EMAIL
    private String existingUserId;
    private String name;
    private String surname;
    private String mobileNumber;
    private String email;
    private String departmentUniqueId;
    private String status;
    private Instant createdAt;
}