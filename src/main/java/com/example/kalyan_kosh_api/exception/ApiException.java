package com.example.kalyan_kosh_api.exception;

import org.springframework.http.HttpStatus;

/**
 * Custom API Exception for throwing specific errors with error codes
 * Use this exception throughout the application for consistent error handling
 */
public class ApiException extends RuntimeException {

    private final String errorCode;
    private final HttpStatus status;

    public ApiException(String message) {
        super(message);
        this.errorCode = "API_ERROR";
        this.status = HttpStatus.INTERNAL_SERVER_ERROR;
    }

    public ApiException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.status = HttpStatus.BAD_REQUEST;
    }

    public ApiException(String errorCode, String message, HttpStatus status) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public HttpStatus getStatus() {
        return status;
    }

    // ==================== FACTORY METHODS ====================

    /**
     * Create a NOT_FOUND exception
     */
    public static ApiException notFound(String message) {
        return new ApiException("NOT_FOUND", message, HttpStatus.NOT_FOUND);
    }

    /**
     * Create a NOT_FOUND exception for a specific entity
     */
    public static ApiException notFound(String entity, String id) {
        return new ApiException("NOT_FOUND", entity + " not found with id: " + id, HttpStatus.NOT_FOUND);
    }

    /**
     * Create a BAD_REQUEST exception
     */
    public static ApiException badRequest(String message) {
        return new ApiException("BAD_REQUEST", message, HttpStatus.BAD_REQUEST);
    }

    /**
     * Create an UNAUTHORIZED exception
     */
    public static ApiException unauthorized(String message) {
        return new ApiException("UNAUTHORIZED", message, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Create a FORBIDDEN exception
     */
    public static ApiException forbidden(String message) {
        return new ApiException("FORBIDDEN", message, HttpStatus.FORBIDDEN);
    }

    /**
     * Create a CONFLICT exception (e.g., duplicate entry)
     */
    public static ApiException conflict(String message) {
        return new ApiException("CONFLICT", message, HttpStatus.CONFLICT);
    }

    /**
     * Create a VALIDATION_ERROR exception
     */
    public static ApiException validationError(String message) {
        return new ApiException("VALIDATION_ERROR", message, HttpStatus.BAD_REQUEST);
    }

    /**
     * Create an INTERNAL_ERROR exception
     */
    public static ApiException internalError(String message) {
        return new ApiException("INTERNAL_ERROR", message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

