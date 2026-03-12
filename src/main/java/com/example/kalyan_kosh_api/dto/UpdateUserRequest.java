package com.example.kalyan_kosh_api.dto;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private String name;
    private String surname;
    private String fatherName;          // Added father name
    private String email;
    private String countryCode;
    private String mobileNumber;
    private String gender;
    private String maritalStatus;
    private String homeAddress;
    private Integer pincode;            // Added pincode
    private String dateOfBirth;         // Added date of birth
    private String joiningDate;         // Added joining date
    private String retirementDate;      // Added retirement date
    private String schoolOfficeName;    // पदस्थ स्कूल/कार्यालय का नाम
    private String sankulName;          // संकुल का नाम
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
}
