package com.example.kalyan_kosh_api.controller;

import com.example.kalyan_kosh_api.dto.SendEmailOtpRequest;
import com.example.kalyan_kosh_api.dto.VerifyEmailOtpRequest;
import com.example.kalyan_kosh_api.service.EmailOtpService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/email-otp")
@CrossOrigin(
    origins = {"https://pmums.com", "https://www.pmums.com", "http://localhost:3000", "http://localhost:5173"},
    allowedHeaders = "*",
    methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS, RequestMethod.PUT, RequestMethod.DELETE},
    allowCredentials = "true",
    maxAge = 3600
)
public class EmailOtpController {

    private final EmailOtpService emailOtpService;

    public EmailOtpController(EmailOtpService emailOtpService) {
        this.emailOtpService = emailOtpService;
    }

    /**
     * Handle CORS preflight OPTIONS request
     */
    @RequestMapping(method = RequestMethod.OPTIONS)
    public ResponseEntity<Void> handleOptions() {
        return ResponseEntity.ok().build();
    }

    /**
     * Send OTP to email
     * POST /api/auth/email-otp/send
     */
    @PostMapping("/send")
    public ResponseEntity<String> sendEmailOtp(
            @Valid @RequestBody SendEmailOtpRequest request) {

        emailOtpService.sendEmailOtp(request.getEmail());

        return ResponseEntity.ok(
                "OTP sent successfully to " + request.getEmail()
        );
    }

    /**
     * Verify OTP from email
     * POST /api/auth/email-otp/verify
     */
    @PostMapping("/verify")
    public ResponseEntity<String> verifyEmailOtp(
            @Valid @RequestBody VerifyEmailOtpRequest request) {

        emailOtpService.verifyEmailOtp(
                request.getEmail(),
                request.getOtp()
        );

        return ResponseEntity.ok(
                "Email verified successfully"
        );
    }
}

