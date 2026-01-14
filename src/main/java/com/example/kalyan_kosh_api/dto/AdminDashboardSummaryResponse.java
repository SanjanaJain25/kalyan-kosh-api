package com.example.kalyan_kosh_api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class AdminDashboardSummaryResponse {

    private LocalDate sahyogDate;

    private long totalMembers;
    private long totalDeathCases;

    private long totalDonors;
    private long totalNonDonors;

    private double totalReceivedAmount;
}
