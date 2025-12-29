package com.example.kalyan_kosh_api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OtpRegisterRequest {

    @Valid
    @NotNull
    private VerifyOtpRequest otp;

    @Valid
    @NotNull
    private RegisterRequest user;
}
