package com.example.kalyan_kosh_api.controller;

import com.example.kalyan_kosh_api.dto.LoginRequest;
import com.example.kalyan_kosh_api.dto.LoginResponse;
import com.example.kalyan_kosh_api.dto.RegisterRequest;
import com.example.kalyan_kosh_api.entity.User;
import com.example.kalyan_kosh_api.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest req) {
        // Using email instead of username for authentication
        LoginResponse loginResponse = authService.authenticateAndGetLoginResponse(req.getEmail(), req.getPassword());
        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/register")
    public ResponseEntity<String> verifyOtpAndRegister(
            @Valid @RequestBody RegisterRequest request) {

        System.out.println("========================================");
        System.out.println("🔍 REGISTRATION REQUEST RECEIVED");
        System.out.println("========================================");
        System.out.println("📧 Email: " + request.getEmail());
        System.out.println("👤 Name: " + request.getName() + " " + request.getSurname());
        System.out.println("👨 Father Name: " + request.getFatherName());
        System.out.println("📱 Mobile: " + request.getMobileNumber());
        System.out.println("🏫 School: " + request.getSchoolOfficeName());
        System.out.println("🏢 Department: " + request.getDepartment());
        System.out.println("📍 State: " + request.getDepartmentState());
        System.out.println("📍 Sambhag: " + request.getDepartmentSambhag());
        System.out.println("📍 District: " + request.getDepartmentDistrict());
        System.out.println("📍 Block: " + request.getDepartmentBlock());
        System.out.println("========================================");

        try {
            System.out.println("➡️ Calling authService.registerAfterOtp()...");

            // register user
            User user = authService.registerAfterOtp(request);

            System.out.println("✅ User registered successfully!");
            System.out.println("🆔 User ID: " + user.getId());
            System.out.println("========================================");

            return ResponseEntity.ok(
                    "User registered successfully with ID: " + user.getId()
            );
        } catch (Exception e) {
            System.err.println("========================================");
            System.err.println("❌ REGISTRATION ERROR!");
            System.err.println("========================================");
            System.err.println("Error Type: " + e.getClass().getName());
            System.err.println("Error Message: " + e.getMessage());
            System.err.println("Stack Trace:");
            e.printStackTrace();
            System.err.println("========================================");
            throw e;
        }
    }
}
