package com.example.kalyan_kosh_api.controller;

import com.example.kalyan_kosh_api.dto.AdminManualSahyogMoveRequest;
import com.example.kalyan_kosh_api.entity.User;
import com.example.kalyan_kosh_api.repository.UserRepository;
import com.example.kalyan_kosh_api.service.ReceiptService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/manual-sahyog")
@CrossOrigin(origins = "*")
@PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN')")
public class AdminManualSahyogMigrationController {

    private final ReceiptService receiptService;
    private final UserRepository userRepository;

    public AdminManualSahyogMigrationController(
            ReceiptService receiptService,
            UserRepository userRepository
    ) {
        this.receiptService = receiptService;
        this.userRepository = userRepository;
    }

    @PostMapping("/move")
    public ResponseEntity<?> moveAsahyogToSahyog(
            @Valid @RequestBody AdminManualSahyogMoveRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest
    ) {
        User adminUser = userRepository.findById(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Admin user not found"));

        String ipAddress = httpRequest.getRemoteAddr();

        return ResponseEntity.ok(
                receiptService.manualMoveAsahyogToSahyog(request, adminUser, ipAddress)
        );
    }
}