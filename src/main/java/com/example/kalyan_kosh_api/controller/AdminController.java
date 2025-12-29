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
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private final UserRepository userRepo;

    public AdminController(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @PostMapping("/dashboard")
    public ResponseEntity<?> adminDashboard(@RequestBody Map<String, String> body) {

        String username = body.get("username");

        User user = userRepo.findByUsername(username).orElse(null);

        if (user == null) {
            return ResponseEntity.status(404).body("User not found");
        }

        // ROLE CHECK → Only ADMIN allowed
        if (user.getRole() != Role.ROLE_ADMIN) {
            return ResponseEntity.status(403).body("Access denied: Admin only");
        }

        return ResponseEntity.ok("Welcome Admin: " + user.getName());
    }
}

