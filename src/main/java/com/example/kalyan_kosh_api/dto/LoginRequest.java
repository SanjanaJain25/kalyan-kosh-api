package com.example.kalyan_kosh_api.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;

public class LoginRequest {
    @NotBlank(message = "User ID is required")
    @JsonProperty("userId")
    @JsonAlias({"username", "email"})  // Accept "username" or "email" as aliases for "userId"
    private String userId;

    @NotBlank(message = "Password is required")
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

    // Alias setter for 'username' field from frontend
    public void setUsername(String username) {
        this.userId = username;
    }

    // Alias setter for 'email' field from frontend
    public void setEmail(String email) {
        this.userId = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}


