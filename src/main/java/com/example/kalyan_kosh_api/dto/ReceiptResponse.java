package com.example.kalyan_kosh_api.dto;

import com.example.kalyan_kosh_api.entity.ReceiptStatus;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;

@Data
public class ReceiptResponse {

    private Long id;

    private int month;
    private int year;

    private double amount;
    private LocalDate paymentDate;
    private String transactionId;

    private ReceiptStatus status;

    private Long deathCaseId;
    private String deceasedName;

    private String filePath;
    private Instant uploadedAt;
}
