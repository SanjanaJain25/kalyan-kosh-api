package com.example.kalyan_kosh_api.controller;

import com.example.kalyan_kosh_api.dto.ReAuthRequest;
import com.example.kalyan_kosh_api.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/security")
@CrossOrigin(origins = "*")
public class SecurityReAuthController {

    private final AuthService authService;

    public SecurityReAuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/reauth")
    public ResponseEntity<?> reAuthenticate(
            @Valid @RequestBody ReAuthRequest request,
            Authentication authentication
    ) {
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalArgumentException("Unauthenticated user");
        }

        authService.verifyCurrentUserPassword(
                authentication.getName(),
                request.getPassword()
        );

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Re-authentication successful"
        ));
    }
}