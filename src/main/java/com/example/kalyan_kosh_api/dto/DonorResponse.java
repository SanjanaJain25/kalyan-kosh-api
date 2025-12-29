package com.example.kalyan_kosh_api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class DonorResponse {

    private String userId;
    private String username;
    private String name;

    private String sambhag;
    private String district;
    private String block;
    private String department;

    private LocalDate paymentDate;
    private double amount;
}
