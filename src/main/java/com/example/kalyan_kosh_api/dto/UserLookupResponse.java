package com.example.kalyan_kosh_api.dto;

import com.example.kalyan_kosh_api.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLookupResponse {

    private String id;
    private String registrationNumber;

    private String name;
    private String surname;
    private String fullName;

    private String mobileNumber;

    private String department;
    private String departmentUniqueId;

    private String departmentState;
    private String departmentSambhag;
    private String departmentDistrict;
    private String departmentBlock;

    private String schoolOfficeName;

    private Role role;

    private Long assignedDeathCaseId;
    private String assignedDeathCaseName;
    private String allocatedQrCode;
    private List<String> nominee1QrCodes;
    private List<String> nominee2QrCodes;
    private Boolean utrUploaded;
    private Long latestReceiptId;
    private String latestUtrNumber;
    private Instant utrUploadedAt;

    public UserLookupResponse(
            String id,
            String name,
            String surname,
            String mobileNumber,
            String department,
            String departmentUniqueId,
            String departmentState,
            String departmentSambhag,
            String departmentDistrict,
            String departmentBlock,
            String schoolOfficeName,
            Role role
    ) {
        this.id = id;
        this.registrationNumber = id;
        this.name = name;
        this.surname = surname;
        this.fullName = ((name != null ? name : "") +
                (surname != null && !surname.isBlank() ? " " + surname : "")).trim();
        this.mobileNumber = mobileNumber;
        this.department = department;
        this.departmentUniqueId = departmentUniqueId;
        this.departmentState = departmentState;
        this.departmentSambhag = departmentSambhag;
        this.departmentDistrict = departmentDistrict;
        this.departmentBlock = departmentBlock;
        this.schoolOfficeName = schoolOfficeName;
        this.role = role;
    }
}