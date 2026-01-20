package com.example.kalyan_kosh_api.controller;

import com.example.kalyan_kosh_api.dto.ResetPasswordRequest;
import com.example.kalyan_kosh_api.dto.SendForgotOtpRequest;
import com.example.kalyan_kosh_api.service.AuthService;
import com.example.kalyan_kosh_api.service.EmailOtpService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<String> sendOtp(
            @Valid @RequestBody SendForgotOtpRequest request) {

        emailOtpService.sendEmailOtp(request.getEmail());
        return ResponseEntity.ok("OTP sent successfully to your email");
    }

    // STEP 2: Verify OTP + Reset Password
    @PostMapping("/reset")
    public ResponseEntity<String> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        emailOtpService.verifyEmailOtp(request.getEmail(), request.getOtp());
        authService.resetPasswordByEmail(
                request.getEmail(),
                request.getNewPassword()
        );

        return ResponseEntity.ok("Password reset successfully");
    }
}
