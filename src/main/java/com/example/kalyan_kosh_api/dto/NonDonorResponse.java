package com.example.kalyan_kosh_api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NonDonorResponse {

    private Long userId;
    private String username;
    private double paidAmount;
    private String status; // UNPAID / PARTIAL / PAID
}
