package com.example.kalyan_kosh_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicSahyogListResponse {

    private String registrationNumber;
    private String name;
    private String department;
    private String state;
    private String sambhag;
    private String district;
    private String block;
    private String schoolName;
    private String beneficiary;
    private LocalDateTime receiptUploadDate;
}