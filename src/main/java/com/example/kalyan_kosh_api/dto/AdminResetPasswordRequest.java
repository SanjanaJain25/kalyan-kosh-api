package com.example.kalyan_kosh_api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminResetPasswordRequest {

    @NotBlank
    private String newPassword;

    @NotBlank
    private String confirmPassword;
}