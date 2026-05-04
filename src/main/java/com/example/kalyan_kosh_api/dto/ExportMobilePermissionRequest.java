package com.example.kalyan_kosh_api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExportMobilePermissionRequest {

    @NotBlank(message = "User ID is required")
    private String userId;

    private String remarks;
}