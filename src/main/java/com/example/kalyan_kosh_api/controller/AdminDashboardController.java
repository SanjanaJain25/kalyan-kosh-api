package com.example.kalyan_kosh_api.controller;

import com.example.kalyan_kosh_api.dto.AdminDashboardSummaryResponse;
import com.example.kalyan_kosh_api.service.MonthlySahyogService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/dashboard")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminDashboardController {

    private final MonthlySahyogService service;

    public AdminDashboardController(MonthlySahyogService service) {
        this.service = service;
    }

    @GetMapping("/summary")
    public ResponseEntity<AdminDashboardSummaryResponse> summary(
            @RequestParam int month,
            @RequestParam int year) {

        return ResponseEntity.ok(
                service.getDashboardSummary(month, year));
    }
}
