package com.example.kalyan_kosh_api.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Standard error response format for all API errors
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    /**
     * Indicates if this is an error response
     */
    private boolean error;

    /**
     * Error code for identifying the type of error
     * Examples: VALIDATION_ERROR, NOT_FOUND, UNAUTHORIZED, etc.
     */
    private String code;

    /**
     * Human-readable error message
     */
    private String message;

    /**
     * HTTP status code
     */
    private int status;

    /**
     * Timestamp when the error occurred
     */
    private String timestamp;

    /**
     * Additional details (e.g., field-level validation errors)
     */
    private Map<String, String> details;

    /**
     * Path of the request that caused the error (optional)
     */
    private String path;
}

