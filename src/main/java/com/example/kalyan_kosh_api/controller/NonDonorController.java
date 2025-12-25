package com.example.kalyan_kosh_api.controller;

import com.example.kalyan_kosh_api.entity.User;
import com.example.kalyan_kosh_api.service.NonDonorService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/non-donors")
@PreAuthorize("hasRole('ADMIN')")
public class NonDonorController {

    private final NonDonorService service;

    public NonDonorController(NonDonorService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<User>> list(
            @RequestParam int month,
            @RequestParam int year
    ) {
        return ResponseEntity.ok(service.getNonDonors(month, year));
    }
}
