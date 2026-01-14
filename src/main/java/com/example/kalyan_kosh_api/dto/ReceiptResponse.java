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
    private String comment;

    private ReceiptStatus status;

    private Long deathCaseId;
    private String deceasedName;

    // S3 file info
    private String fileUrl;
    private String fileName;
    private String fileType;
    private Long fileSize;

    private Instant uploadedAt;
}
