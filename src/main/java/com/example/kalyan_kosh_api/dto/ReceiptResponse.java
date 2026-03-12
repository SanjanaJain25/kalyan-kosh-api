package com.example.kalyan_kosh_api.dto;

import com.example.kalyan_kosh_api.entity.ReceiptStatus;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;

@Data
public class ReceiptResponse {

    private Long id;

    private double amount;
    private LocalDate paymentDate;

    // Reference name for the payment
    private String referenceName;

    // UTR (Unique Transaction Reference) number
    private String utrNumber;

    private ReceiptStatus status;

    private Long deathCaseId;
    private String deceasedName;


    private Instant uploadedAt;
}
