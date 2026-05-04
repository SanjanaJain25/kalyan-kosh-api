package com.example.kalyan_kosh_api.controller;

import com.example.kalyan_kosh_api.service.SystemSettingService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/security")
@PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN')")
public class AdminSecurityController {

    private final SystemSettingService systemSettingService;

    public AdminSecurityController(SystemSettingService systemSettingService) {
        this.systemSettingService = systemSettingService;
    }

    @PostMapping("/logout-all-users")
    public Map<String, Object> logoutAllUsers() {
        systemSettingService.forceLogoutAllUsers();

        return Map.of(
                "success", true,
                "message", "All users have been logged out successfully.",
                "logoutTime", Instant.now().toString()
        );
    }

    @GetMapping("/session-status")
    public Map<String, Object> sessionStatus() {
        return Map.of(
                "valid", true,
                "message", "Session is valid"
        );
    }
}