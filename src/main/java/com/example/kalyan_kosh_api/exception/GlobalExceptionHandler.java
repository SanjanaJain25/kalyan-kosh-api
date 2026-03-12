package com.example.kalyan_kosh_api.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global Exception Handler for the entire application
 * Catches all exceptions and returns proper JSON error responses
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ==================== CUSTOM EXCEPTIONS ====================

    /**
     * Handle custom API exceptions
     */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException ex) {
        log.warn("⚠️ API Exception: {} - {}", ex.getErrorCode(), ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .error(true)
                .code(ex.getErrorCode())
                .message(ex.getMessage())
                .status(ex.getStatus().value())
                .timestamp(Instant.now().toString())
                .build();

        return new ResponseEntity<>(error, ex.getStatus());
    }

    // ==================== VALIDATION EXCEPTIONS ====================

    /**
     * Handle validation errors from @Valid
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "Invalid value",
                        (existing, replacement) -> existing
                ));

        log.warn("⚠️ Validation failed: {}", fieldErrors);

        ErrorResponse error = ErrorResponse.builder()
                .error(true)
                .code("VALIDATION_ERROR")
                .message("Validation failed for one or more fields")
                .status(HttpStatus.BAD_REQUEST.value())
                .timestamp(Instant.now().toString())
                .details(fieldErrors)
                .build();

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle constraint violations
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        log.warn("⚠️ Constraint violation: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .error(true)
                .code("CONSTRAINT_VIOLATION")
                .message(ex.getMessage())
                .status(HttpStatus.BAD_REQUEST.value())
                .timestamp(Instant.now().toString())
                .build();

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle missing request parameters
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(MissingServletRequestParameterException ex) {
        log.warn("⚠️ Missing parameter: {}", ex.getParameterName());

        ErrorResponse error = ErrorResponse.builder()
                .error(true)
                .code("MISSING_PARAMETER")
                .message("Required parameter '" + ex.getParameterName() + "' is missing")
                .status(HttpStatus.BAD_REQUEST.value())
                .timestamp(Instant.now().toString())
                .build();

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle type mismatch (e.g., string instead of number)
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = String.format("Parameter '%s' should be of type '%s'",
                ex.getName(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");

        log.warn("⚠️ Type mismatch: {}", message);

        ErrorResponse error = ErrorResponse.builder()
                .error(true)
                .code("TYPE_MISMATCH")
                .message(message)
                .status(HttpStatus.BAD_REQUEST.value())
                .timestamp(Instant.now().toString())
                .build();

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle invalid JSON body
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleInvalidJson(HttpMessageNotReadableException ex) {
        log.warn("⚠️ Invalid JSON: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .error(true)
                .code("INVALID_JSON")
                .message("Invalid JSON format or malformed request body")
                .status(HttpStatus.BAD_REQUEST.value())
                .timestamp(Instant.now().toString())
                .build();

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // ==================== SECURITY EXCEPTIONS ====================

    /**
     * Handle authentication failures
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex) {
        log.warn("🔐 Authentication failed: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .error(true)
                .code("AUTHENTICATION_FAILED")
                .message("Authentication failed: " + ex.getMessage())
                .status(HttpStatus.UNAUTHORIZED.value())
                .timestamp(Instant.now().toString())
                .build();

        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handle bad credentials
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        log.warn("🔐 Bad credentials: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .error(true)
                .code("BAD_CREDENTIALS")
                .message("Invalid username or password")
                .status(HttpStatus.UNAUTHORIZED.value())
                .timestamp(Instant.now().toString())
                .build();

        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handle access denied (403)
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        log.warn("🚫 Access denied: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .error(true)
                .code("ACCESS_DENIED")
                .message("You don't have permission to access this resource")
                .status(HttpStatus.FORBIDDEN.value())
                .timestamp(Instant.now().toString())
                .build();

        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    // ==================== NOT FOUND EXCEPTIONS ====================

    /**
     * Handle entity not found
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFound(EntityNotFoundException ex) {
        log.warn("🔍 Entity not found: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .error(true)
                .code("NOT_FOUND")
                .message(ex.getMessage())
                .status(HttpStatus.NOT_FOUND.value())
                .timestamp(Instant.now().toString())
                .build();

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    /**
     * Handle no handler found (404)
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFound(NoHandlerFoundException ex) {
        log.warn("🔍 Endpoint not found: {} {}", ex.getHttpMethod(), ex.getRequestURL());

        ErrorResponse error = ErrorResponse.builder()
                .error(true)
                .code("ENDPOINT_NOT_FOUND")
                .message("Endpoint not found: " + ex.getHttpMethod() + " " + ex.getRequestURL())
                .status(HttpStatus.NOT_FOUND.value())
                .timestamp(Instant.now().toString())
                .build();

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    /**
     * Handle no resource found
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(NoResourceFoundException ex) {
        log.warn("🔍 Resource not found: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .error(true)
                .code("RESOURCE_NOT_FOUND")
                .message("Resource not found: " + ex.getResourcePath())
                .status(HttpStatus.NOT_FOUND.value())
                .timestamp(Instant.now().toString())
                .build();

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    // ==================== HTTP METHOD/MEDIA TYPE EXCEPTIONS ====================

    /**
     * Handle unsupported HTTP method
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        log.warn("⚠️ Method not supported: {}", ex.getMethod());

        ErrorResponse error = ErrorResponse.builder()
                .error(true)
                .code("METHOD_NOT_ALLOWED")
                .message("HTTP method '" + ex.getMethod() + "' is not supported for this endpoint")
                .status(HttpStatus.METHOD_NOT_ALLOWED.value())
                .timestamp(Instant.now().toString())
                .build();

        return new ResponseEntity<>(error, HttpStatus.METHOD_NOT_ALLOWED);
    }

    /**
     * Handle unsupported media type
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex) {
        log.warn("⚠️ Media type not supported: {}", ex.getContentType());

        ErrorResponse error = ErrorResponse.builder()
                .error(true)
                .code("UNSUPPORTED_MEDIA_TYPE")
                .message("Content type '" + ex.getContentType() + "' is not supported")
                .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value())
                .timestamp(Instant.now().toString())
                .build();

        return new ResponseEntity<>(error, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    // ==================== FILE/IO EXCEPTIONS ====================

    /**
     * Handle file upload size exceeded
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSize(MaxUploadSizeExceededException ex) {
        log.warn("📁 File too large: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .error(true)
                .code("FILE_TOO_LARGE")
                .message("File size exceeds the maximum allowed limit")
                .status(HttpStatus.PAYLOAD_TOO_LARGE.value())
                .timestamp(Instant.now().toString())
                .build();

        return new ResponseEntity<>(error, HttpStatus.PAYLOAD_TOO_LARGE);
    }

    /**
     * Handle file not found
     */
    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleFileNotFound(FileNotFoundException ex) {
        log.warn("📁 File not found: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .error(true)
                .code("FILE_NOT_FOUND")
                .message("File not found: " + ex.getMessage())
                .status(HttpStatus.NOT_FOUND.value())
                .timestamp(Instant.now().toString())
                .build();

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    /**
     * Handle IO exceptions
     */
    @ExceptionHandler(IOException.class)
    public ResponseEntity<ErrorResponse> handleIOException(IOException ex) {
        log.error("📁 IO Error: {}", ex.getMessage(), ex);

        ErrorResponse error = ErrorResponse.builder()
                .error(true)
                .code("IO_ERROR")
                .message("File operation failed: " + ex.getMessage())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .timestamp(Instant.now().toString())
                .build();

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // ==================== DATABASE EXCEPTIONS ====================

    /**
     * Handle data integrity violations (e.g., duplicate entries)
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex) {
        log.error("🗃️ Data integrity violation: {}", ex.getMessage());

        String message = "Data integrity error";
        if (ex.getMessage() != null) {
            if (ex.getMessage().contains("Duplicate")) {
                message = "Duplicate entry found. This record already exists.";
            } else if (ex.getMessage().contains("foreign key")) {
                message = "Cannot perform this operation due to related records.";
            } else if (ex.getMessage().contains("cannot be null")) {
                message = "Required field is missing.";
            }
        }

        ErrorResponse error = ErrorResponse.builder()
                .error(true)
                .code("DATA_INTEGRITY_ERROR")
                .message(message)
                .status(HttpStatus.CONFLICT.value())
                .timestamp(Instant.now().toString())
                .build();

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    // ==================== GENERAL EXCEPTIONS ====================

    /**
     * Handle illegal argument exceptions
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("⚠️ Invalid argument: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .error(true)
                .code("INVALID_ARGUMENT")
                .message(ex.getMessage())
                .status(HttpStatus.BAD_REQUEST.value())
                .timestamp(Instant.now().toString())
                .build();

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle illegal state exceptions
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex) {
        log.warn("⚠️ Invalid state: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .error(true)
                .code("INVALID_STATE")
                .message(ex.getMessage())
                .status(HttpStatus.CONFLICT.value())
                .timestamp(Instant.now().toString())
                .build();

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    /**
     * Handle runtime exceptions
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
        log.error("❌ Runtime error: {}", ex.getMessage(), ex);

        ErrorResponse error = ErrorResponse.builder()
                .error(true)
                .code("RUNTIME_ERROR")
                .message(ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .timestamp(Instant.now().toString())
                .build();

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handle all other exceptions (catch-all)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(Exception ex) {
        log.error("❌ Unexpected error: {}", ex.getMessage(), ex);

        ErrorResponse error = ErrorResponse.builder()
                .error(true)
                .code("INTERNAL_ERROR")
                .message("An unexpected error occurred. Please try again later.")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .timestamp(Instant.now().toString())
                .build();

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

