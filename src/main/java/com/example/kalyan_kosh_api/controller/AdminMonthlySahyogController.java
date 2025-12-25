package com.example.kalyan_kosh_api.controller;

import com.example.kalyan_kosh_api.dto.NonDonorResponse;
import com.example.kalyan_kosh_api.entity.MonthlySahyog;
import com.example.kalyan_kosh_api.service.MonthlySahyogService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/monthly-sahyog")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminMonthlySahyogController {

    private final MonthlySahyogService service;

    public AdminMonthlySahyogController(MonthlySahyogService service) {
        this.service = service;
    }

    @PostMapping("/generate")
    public ResponseEntity<MonthlySahyog> generate(
            @RequestParam int month,
            @RequestParam int year) {

        return ResponseEntity.ok(service.generate(month, year));
    }

    @PostMapping("/calculate")
    public ResponseEntity<MonthlySahyog> calculate(
            @RequestParam int month,
            @RequestParam int year) {

        return ResponseEntity.ok(
                service.calculateExpected(month, year));
    }

    @GetMapping("/non-donors")
    public ResponseEntity<List<NonDonorResponse>> nonDonors(
            @RequestParam int month,
            @RequestParam int year) {

        return ResponseEntity.ok(
                service.getNonDonors(month, year));
    }


}
