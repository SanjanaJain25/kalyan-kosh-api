package com.example.kalyan_kosh_api.dto;

import lombok.Data;

/**
 * DTO for registration request - fields match your User entity.
 * Note: dateOfBirth is accepted as ISO string (yyyy-MM-dd) and parsed in service.
 */
@Data
public class RegisterRequest {
    private String name;
    private String surname;
    private String countryCode;
    private String phoneNumber;
    private String mobileNumber;
    private String email;
    private String gender;
    private String maritalStatus;
    private String username;
    private String password;
    private String homeAddress;
    private String dateOfBirth;        // "1999-09-11"
    private String schoolOfficeName;
    private String department;
    private String departmentUniqueId;
    private String departmentState;      // NEW: "Madhya Pradesh"
    private String departmentSambhag;    // NEW: "Chambal"
    private String departmentDistrict;
    private String departmentBlock;
    private String nominee1Name;
    private String nominee1Relation;
    private String nominee2Name;
    private String nominee2Relation;
    private boolean acceptedTerms;
}
