package com.example.kalyan_kosh_api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReAuthRequest {

    @NotBlank(message = "Password is required")
    private String password;
}