package com.example.kalyan_kosh_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AdminManualSahyogMoveRequest {

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotNull(message = "Death case is required")
    private Long deathCaseId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private Double amount;

    @NotNull(message = "Payment date is required")
    private LocalDate paymentDate;

    private String referenceName;

    private String utrNumber;

    private String remarks;
}