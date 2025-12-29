package com.example.kalyan_kosh_api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdminDashboardSummaryResponse {

    private int month;
    private int year;

    private long totalMembers;
    private long totalDeathCases;

    private long totalDonors;
    private long totalNonDonors;

    private double totalReceivedAmount;
}
