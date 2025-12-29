package com.example.kalyan_kosh_api.controller;

import com.example.kalyan_kosh_api.dto.ResetPasswordRequest;
import com.example.kalyan_kosh_api.dto.SendForgotOtpRequest;
import com.example.kalyan_kosh_api.service.AuthService;
import com.example.kalyan_kosh_api.service.OtpService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/password")
public class ForgotPasswordController {

    private final OtpService otpService;
    private final AuthService authService;

    public ForgotPasswordController(OtpService otpService,
                                    AuthService authService) {
        this.otpService = otpService;
        this.authService = authService;
    }

    // STEP 1: Send OTP
    @PostMapping("/send-otp")
    public ResponseEntity<String> sendOtp(
            @Valid @RequestBody SendForgotOtpRequest request) {

        otpService.sendOtp(request.getMobile());
        return ResponseEntity.ok("OTP sent successfully");
    }

    // STEP 2: Verify OTP + Reset Password
    @PostMapping("/reset")
    public ResponseEntity<String> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        otpService.verifyOtp(request.getMobile(), request.getOtp());
        authService.resetPassword(
                request.getMobile(),
                request.getNewPassword()
        );

        return ResponseEntity.ok("Password reset successfully");
    }
}
