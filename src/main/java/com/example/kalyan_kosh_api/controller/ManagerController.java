package com.example.kalyan_kosh_api.controller;

import com.example.kalyan_kosh_api.entity.Role;
import com.example.kalyan_kosh_api.entity.User;
import com.example.kalyan_kosh_api.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/manager")
@CrossOrigin(origins = "*")
public class ManagerController {

    private final UserRepository userRepo;

    public ManagerController(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @PostMapping("/reports")
    public ResponseEntity<?> managerReports(@RequestBody Map<String, String> body) {

        String userId = body.get("userId");  // Changed from username to userId

        User user = userRepo.findById(userId).orElse(null);  // Changed from findByUsername to findById

        if (user == null) {
            return ResponseEntity.status(404).body("User not found");
        }

        // ROLE CHECK → Only MANAGER or ADMIN
        if (user.getRole() != Role.ROLE_MANAGER && user.getRole() != Role.ROLE_ADMIN) {
            return ResponseEntity.status(403).body("Access denied: Only manager & admin allowed");
        }

        return ResponseEntity.ok("Manager report accessed");
    }
}

