package com.example.kalyan_kosh_api.dto;

import jakarta.validation.constraints.*;

public class LoginRequest {
    @NotBlank
    private String userId;

    @NotBlank
    private String password;

    // Default constructor
    public LoginRequest() {}

    // Constructor with parameters
    public LoginRequest(String userId, String password) {
        this.userId = userId;
        this.password = password;
    }

    // Getters and setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}


