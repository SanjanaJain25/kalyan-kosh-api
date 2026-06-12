package com.example.kalyan_kosh_api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class BulkLocationUpdateResponse {
    private int totalRows;
    private int uniqueUserIds;
    private int updatedCount;
    private int skippedCount;
    private int duplicateCount;

    private List<String> duplicateUserIds;
    private List<String> notFoundUsers;
    private List<String> locationErrors;

    private boolean dryRun;
}