package com.example.kalyan_kosh_api.dto;

import com.example.kalyan_kosh_api.entity.Role;
import lombok.Data;

@Data
public class UserResponse {
    private Long id;
    private String name;
    private String surname;
    private String username;
    private String email;
    private String phoneNumber;
    private String mobileNumber;
    private String gender;
    private String maritalStatus;
    private String homeAddress;
    private String schoolOfficeName;
    private String department;
    private String departmentUniqueId;
    private String departmentDistrict;
    private String departmentBlock;
    private String nominee1Name;
    private String nominee1Relation;
    private String nominee2Name;
    private String nominee2Relation;
    private boolean acceptedTerms;
    private Role role;
}
