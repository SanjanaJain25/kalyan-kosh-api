package com.example.kalyan_kosh_api.dto;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private String name;
    private String surname;
    private String email;
    private String phoneNumber;
    private String countryCode;
    private String mobileNumber;
    private String gender;
    private String maritalStatus;
    private String homeAddress;
    private String schoolOfficeName;
    private String department;
    private String departmentUniqueId;
    private String departmentState;      // NEW
    private String departmentSambhag;    // NEW
    private String departmentDistrict;
    private String departmentBlock;
    private String nominee1Name;
    private String nominee1Relation;
    private String nominee2Name;
    private String nominee2Relation;
}
