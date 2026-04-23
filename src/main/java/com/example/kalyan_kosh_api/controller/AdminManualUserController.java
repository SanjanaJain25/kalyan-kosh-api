package com.example.kalyan_kosh_api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.kalyan_kosh_api.dto.AdminCreateUserRequest;
import com.example.kalyan_kosh_api.dto.AdminUserMatchResponse;
import com.example.kalyan_kosh_api.dto.UserResponse;
import com.example.kalyan_kosh_api.service.UserService;

@RestController
@RequestMapping("/api/admin/users")
public class AdminManualUserController {

    private final UserService userService;

    public AdminManualUserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/manual-create/check")
    public ResponseEntity<AdminUserMatchResponse> checkManualCreateMatch(
            @RequestBody AdminCreateUserRequest req) {
        return ResponseEntity.ok(userService.checkExistingUserForManualCreate(req));
    }

    @PostMapping("/manual-create")
    public ResponseEntity<UserResponse> manualCreateUser(
            @RequestBody AdminCreateUserRequest req) {
        return ResponseEntity.ok(userService.adminCreateUser(req));
    }
}