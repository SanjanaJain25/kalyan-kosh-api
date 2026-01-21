package com.example.kalyan_kosh_api.controller;

import com.example.kalyan_kosh_api.dto.ResetPasswordRequest;
import com.example.kalyan_kosh_api.dto.SendForgotOtpRequest;
import com.example.kalyan_kosh_api.service.AuthService;
import com.example.kalyan_kosh_api.service.EmailOtpService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth/password")
@CrossOrigin(origins = "*")
public class ForgotPasswordController {

    private final EmailOtpService emailOtpService;
    private final AuthService authService;

    public ForgotPasswordController(EmailOtpService emailOtpService,
                                    AuthService authService) {
        this.emailOtpService = emailOtpService;
        this.authService = authService;
    }

    // STEP 1: Send OTP to email
    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(
            @Valid @RequestBody SendForgotOtpRequest request) {
        try {
            emailOtpService.sendEmailOtp(request.getEmail());
            return ResponseEntity.ok(createSuccessResponse("OTP sent successfully to your email"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse("VALIDATION_ERROR", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("EMAIL_ERROR", "Failed to send OTP: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("UNKNOWN_ERROR", "An unexpected error occurred: " + e.getMessage()));
        }
    }

    // STEP 2: Verify OTP + Reset Password
    @PostMapping("/reset")
    public ResponseEntity<?> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        try {
            // First verify OTP
            emailOtpService.verifyEmailOtp(request.getEmail(), request.getOtp());

            // Then reset password
            authService.resetPasswordByEmail(
                    request.getEmail(),
                    request.getNewPassword()
            );

            return ResponseEntity.ok(createSuccessResponse("Password reset successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse("VALIDATION_ERROR", e.getMessage()));
        } catch (RuntimeException e) {
            String message = e.getMessage();
            if (message != null && message.contains("OTP")) {
                return ResponseEntity.badRequest().body(createErrorResponse("OTP_ERROR", message));
            } else if (message != null && message.contains("User not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("USER_NOT_FOUND", message));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("RESET_ERROR", "Failed to reset password: " + message));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("UNKNOWN_ERROR", "An unexpected error occurred: " + e.getMessage()));
        }
    }

    // Helper method to create error response
    private Map<String, Object> createErrorResponse(String errorCode, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("errorCode", errorCode);
        response.put("message", message);
        return response;
    }

    // Helper method to create success response
    private Map<String, Object> createSuccessResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        return response;
    }
}
