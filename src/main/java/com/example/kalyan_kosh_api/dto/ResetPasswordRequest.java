package com.example.kalyan_kosh_api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResetPasswordRequest {

    @NotBlank
    private String mobile;

    @NotBlank
    private String otp;

    @NotBlank
    private String newPassword;
}
