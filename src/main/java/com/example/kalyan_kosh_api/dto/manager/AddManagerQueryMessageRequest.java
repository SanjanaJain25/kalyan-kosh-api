package com.example.kalyan_kosh_api.dto.manager;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddManagerQueryMessageRequest {

    @NotBlank(message = "Message is required")
    private String message;
}