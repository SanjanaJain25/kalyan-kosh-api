package com.example.kalyan_kosh_api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SendForgotOtpRequest {

    @NotBlank
    private String mobile;
}
