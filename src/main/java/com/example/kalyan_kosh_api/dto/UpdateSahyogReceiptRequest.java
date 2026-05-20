package com.example.kalyan_kosh_api.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateSahyogReceiptRequest {

    private Double amount;
    private LocalDate paymentDate;
    private String referenceName;
    private String utrNumber;

    // New field for changing death case / beneficiary
    private Long deathCaseId;
}