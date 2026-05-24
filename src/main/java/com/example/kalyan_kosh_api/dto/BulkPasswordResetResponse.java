package com.example.kalyan_kosh_api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class BulkPasswordResetResponse {
    private int totalRegistrationNumbers;
    private int updatedCount;
    private int notFoundCount;
    private List<String> notFoundRegistrationNumbers;
}