package com.example.kalyan_kosh_api.controller;

import com.example.kalyan_kosh_api.entity.User;
import com.example.kalyan_kosh_api.repository.UserRepository;
import com.example.kalyan_kosh_api.service.PoolAssignmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.example.kalyan_kosh_api.entity.DeathCaseStatus;

@RestController
@RequestMapping("/api/pools")
public class PoolController {

    private final UserRepository userRepo;
    private final PoolAssignmentService poolAssignmentService;

    public PoolController(UserRepository userRepo, PoolAssignmentService poolAssignmentService) {
        this.userRepo = userRepo;
        this.poolAssignmentService = poolAssignmentService;
    }

    // ✅ User gets only his assigned pool death case
@PreAuthorize("hasAnyRole('USER','SAMBHAG_MANAGER','DISTRICT_MANAGER','BLOCK_MANAGER','ADMIN','SUPERADMIN')")
@GetMapping("/my")
public ResponseEntity<?> myPool(Authentication auth) {

    User u = userRepo.findById(auth.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));

    if (u.getAssignedDeathCase() == null ||
        u.getAssignedDeathCase().getStatus() != DeathCaseStatus.OPEN) {
        poolAssignmentService.assignPoolToNewUser(u);
        userRepo.save(u);
    }

    return ResponseEntity.ok(u.getAssignedDeathCase());
}

    // ✅ Admin tool - rebalance users
    @PostMapping("/rebalance")
    @PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN')")
    public ResponseEntity<String> rebalance(@RequestParam(defaultValue = "false") boolean overwrite) {
        poolAssignmentService.rebalanceAllUsersAcrossPools(overwrite);
        return ResponseEntity.ok("Rebalance completed");
    }
}