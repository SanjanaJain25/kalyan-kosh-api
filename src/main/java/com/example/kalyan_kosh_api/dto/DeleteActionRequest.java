package com.example.kalyan_kosh_api.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeleteActionRequest {
    private String reason;
    private String rejectionReason;
    private String requestedFromDashboard;
}