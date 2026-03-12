package com.example.kalyan_kosh_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class InsuranceInquiryRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "District is required")
    private String district;

    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Mobile number must be 10 digits")
    private String mobileNumber;
}