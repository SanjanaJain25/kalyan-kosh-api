package com.example.kalyan_kosh_api.dto;

import lombok.Data;

@Data
public class AdminCreateUserRequest {
    private String name;
    private String surname;
    private String fatherName;
    private String email;
    private String countryCode;
    private String mobileNumber;
    private Integer pincode;
    private String gender;
    private String maritalStatus;
    private String homeAddress;

    private String dateOfBirth;
    private String joiningDate;
    private String retirementDate;

    private String schoolOfficeName;
    private String sankulName;
    private String department;
    private String departmentUniqueId;

    private String departmentState;
    private String departmentSambhag;
    private String departmentDistrict;
    private String departmentBlock;

    private String nominee1Name;
    private String nominee1Relation;
    private String nominee2Name;
    private String nominee2Relation;

    private String password;

    // New fields for admin manual create
    private String registrationDateOverride;   // yyyy-MM-dd
    private Boolean createIfMatchFound;        // if true, allow creation even if a match is found
    private String matchedExistingUserId;      // optional, for future explicit handling
    private String supportEntryReference;      // optional remarks/reference
}