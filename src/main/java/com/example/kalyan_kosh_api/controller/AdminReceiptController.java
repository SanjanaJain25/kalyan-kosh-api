package com.example.kalyan_kosh_api.controller;

import com.example.kalyan_kosh_api.dto.AdminReceiptResponse;
import com.example.kalyan_kosh_api.dto.DonorResponse;
import com.example.kalyan_kosh_api.service.AdminReceiptService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/receipts")
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = "*")
public class AdminReceiptController {

    private final AdminReceiptService service;

    public AdminReceiptController(AdminReceiptService service) {
        this.service = service;
    }

    // LIST receipts with filters
    @GetMapping
    public ResponseEntity<List<AdminReceiptResponse>> list(
            @RequestParam int month,
            @RequestParam int year
    ) {
        return ResponseEntity.ok(service.list(month, year));
    }

    @PutMapping("/{id}/verify")
    public ResponseEntity<?> verify(@PathVariable Long id) {
        service.verify(id);
        return ResponseEntity.ok("Receipt verified");
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<?> reject(@PathVariable Long id) {
        service.reject(id);
        return ResponseEntity.ok("Receipt rejected");
    }

    @GetMapping("/donors")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<DonorResponse>> donors(
            @RequestParam int month,
            @RequestParam int year) {

        return ResponseEntity.ok(
                service.getDonors(month, year));
    }

}
