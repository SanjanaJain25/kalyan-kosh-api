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
        LoginResponse loginResponse = authService.authenticateAndGetLoginResponse(req.getUsername(), req.getPassword());
        return ResponseEntity.ok(loginResponse);
    }


    // 2️⃣ VERIFY OTP + REGISTER USER
    @PostMapping("/register")
    public ResponseEntity<String> verifyOtpAndRegister(
            @Valid @RequestBody RegisterRequest request) {

//        // verify OTP
//        otpService.verifyOtp(
//                request.getOtp().getMobile(),
//                request.getOtp().getOtp()
//        );

        // register user
        User user = authService.registerAfterOtp(request);

        return ResponseEntity.ok(
                "User registered successfully with username: "
                        + user.getUsername()
        );
    }
}
