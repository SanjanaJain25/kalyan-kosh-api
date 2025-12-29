package com.example.kalyan_kosh_api.controller;

import com.example.kalyan_kosh_api.dto.OtpRegisterRequest;
import com.example.kalyan_kosh_api.dto.SendOtpRequest;
import com.example.kalyan_kosh_api.entity.User;
import com.example.kalyan_kosh_api.service.AuthService;
import com.example.kalyan_kosh_api.service.OtpService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/otp")
public class OtpAuthController {

    private final OtpService otpService;
    private final AuthService authService;

    public OtpAuthController(OtpService otpService,
                             AuthService authService) {
        this.otpService = otpService;
        this.authService = authService;
    }

    // 1️⃣ SEND OTP
    @PostMapping("/send")
    public ResponseEntity<String> sendOtp(
            @Valid @RequestBody SendOtpRequest request) {

        otpService.sendOtp(request.getMobile());
        return ResponseEntity.ok("OTP sent successfully");
    }

    // 2️⃣ VERIFY OTP + REGISTER USER
    @PostMapping("/verify")
    public ResponseEntity<String> verifyOtpAndRegister(
            @Valid @RequestBody OtpRegisterRequest request) {

        // verify OTP
        otpService.verifyOtp(
                request.getOtp().getMobile(),
                request.getOtp().getOtp()
        );

        // register user
        User user = authService.registerAfterOtp(
                request.getUser()
        );

        return ResponseEntity.ok(
                "User registered successfully with username: "
                        + user.getUsername()
        );
    }
}
