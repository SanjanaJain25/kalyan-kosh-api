package com.example.kalyan_kosh_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLookupResponse {

    private String id;

    private String name;
    private String surname;

    private String mobileNumber;

    private Long assignedDeathCaseId;
    private String assignedDeathCaseName;

    private String allocatedQrCode;
    private List<String> nominee1QrCodes;
    private List<String> nominee2QrCodes;

    private Boolean utrUploaded;
    private String latestUtrNumber;
}