package com.example.kalyan_kosh_api.controller;

import com.example.kalyan_kosh_api.dto.ResetPasswordRequest;
import com.example.kalyan_kosh_api.dto.SendForgotOtpRequest;
import com.example.kalyan_kosh_api.service.AuthService;
import com.example.kalyan_kosh_api.service.EmailOtpService;
import com.example.kalyan_kosh_api.service.SystemSettingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.kalyan_kosh_api.service.OtpService;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth/password")
@CrossOrigin(origins = "*")
public class ForgotPasswordController {

    private final EmailOtpService emailOtpService;
    private final AuthService authService;
    private final SystemSettingService systemSettingService;
    private final OtpService otpService;

   public ForgotPasswordController(EmailOtpService emailOtpService,
                                OtpService otpService,
                                AuthService authService,
                                SystemSettingService systemSettingService) {
    this.emailOtpService = emailOtpService;
    this.otpService = otpService;
    this.authService = authService;
    this.systemSettingService = systemSettingService;
}

    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@Valid @RequestBody SendForgotOtpRequest request) {
        try {
           boolean mobileOtpEnabled = systemSettingService.isMobileOtpEnabled();

if (mobileOtpEnabled) {
    if (request.getMobileNumber() == null || request.getMobileNumber().trim().isEmpty()) {
        return ResponseEntity.badRequest().body(createErrorResponse(
                "VALIDATION_ERROR",
                "Mobile number is required"
        ));
    }

    otpService.sendOtp(request.getMobileNumber().trim());
    return ResponseEntity.ok(createSuccessResponse("OTP sent successfully to your mobile number"));
} else {
    if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
        return ResponseEntity.badRequest().body(createErrorResponse(
                "VALIDATION_ERROR",
                "Email is required"
        ));
    }

    emailOtpService.sendEmailOtp(request.getEmail().trim());
    return ResponseEntity.ok(createSuccessResponse("OTP sent successfully to your email"));
}
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

@GetMapping("/recovery-mode")
public ResponseEntity<?> getRecoveryMode() {
    Map<String, Object> response = new HashMap<>();
    response.put("success", true);
    response.put("mobileOtpEnabled", systemSettingService.isMobileOtpEnabled());
    return ResponseEntity.ok(response);
}

    @PostMapping("/reset")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
           boolean mobileOtpEnabled = systemSettingService.isMobileOtpEnabled();

if (mobileOtpEnabled) {
    if (request.getMobileNumber() == null || request.getMobileNumber().trim().isEmpty()) {
        return ResponseEntity.badRequest().body(createErrorResponse(
                "VALIDATION_ERROR",
                "Mobile number is required"
        ));
    }

    otpService.verifyOtp(request.getMobileNumber().trim(), request.getOtp());
    authService.resetPassword(request.getMobileNumber().trim(), request.getNewPassword());
    return ResponseEntity.ok(createSuccessResponse("Password reset successfully using mobile OTP"));
} else {
    if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
        return ResponseEntity.badRequest().body(createErrorResponse(
                "VALIDATION_ERROR",
                "Email is required"
        ));
    }

    emailOtpService.verifyEmailOtp(request.getEmail().trim(), request.getOtp());
    authService.resetPasswordByEmail(request.getEmail().trim(), request.getNewPassword());
    return ResponseEntity.ok(createSuccessResponse("Password reset successfully using email OTP"));
}
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

    private Map<String, Object> createErrorResponse(String errorCode, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("errorCode", errorCode);
        response.put("message", message);
        return response;
    }

    private Map<String, Object> createSuccessResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        return response;
    }
}