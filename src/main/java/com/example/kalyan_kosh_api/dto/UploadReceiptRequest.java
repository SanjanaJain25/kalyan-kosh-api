package com.example.kalyan_kosh_api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UploadReceiptRequest {

    @NotNull
    private Integer month;

    @NotNull
    private Integer year;

    @NotNull
    private Long deathCaseId;

    @NotNull
    @Positive
    private Double amount;

    @NotNull
    private LocalDate paymentDate;

    // optional
    private String transactionId;
}
