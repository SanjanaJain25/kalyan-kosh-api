package com.example.kalyan_kosh_api.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;

public class MobileLoginRequest {

    @NotBlank(message = "Mobile number is required")
    @JsonAlias({"mobile", "phone", "userId", "username"})
    private String mobileNumber;

    @NotBlank(message = "Password is required")
    private String password;

    public MobileLoginRequest() {
    }

    public MobileLoginRequest(String mobileNumber, String password) {
        this.mobileNumber = mobileNumber;
        this.password = password;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}