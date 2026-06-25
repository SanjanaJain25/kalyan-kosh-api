package com.example.kalyan_kosh_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicMemberListResponse {

    private String id;
    private String registrationNumber;

    private String name;
    private String surname;

    private String department;

    private String departmentState;
    private String departmentSambhag;
    private String departmentDistrict;
    private String departmentBlock;

    // Alias fields for existing frontend pages
    private String state;
    private String sambhag;
    private String district;
    private String block;

    private String schoolOfficeName;
    private String schoolName;

    private Instant createdAt;

    // Required only because Our Member page shows QR/UTR columns conditionally
    private Boolean utrUploaded;
    private String allocatedQrCode;
    private String latestUtrNumber;
}