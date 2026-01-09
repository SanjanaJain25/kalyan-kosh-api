package com.example.kalyan_kosh_api.controller;

import com.example.kalyan_kosh_api.dto.NonDonorResponse;
import com.example.kalyan_kosh_api.entity.MonthlySahyog;
import com.example.kalyan_kosh_api.service.MonthlySahyogService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*
 * NOTE: This controller is commented out because it depends on month/year fields
 * that were removed from the Receipt entity. If you need monthly tracking functionality,
 * you'll need to either:
 * 1. Add back month/year fields to Receipt entity, OR
 * 2. Refactor to extract month/year from paymentDate field
 */

@RestController
@RequestMapping("/api/admin/monthly-sahyog")
//@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@CrossOrigin(origins = "*")
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

    @GetMapping("/non-donors")
    public ResponseEntity<List<NonDonorResponse>> nonDonors(
            @RequestParam int month,
            @RequestParam int year) {

        return ResponseEntity.ok(
                service.getNonDonors(month, year));
    }

    @GetMapping("/donors")
    public ResponseEntity<?> donors(
            @RequestParam int month,
            @RequestParam int year) {

        return ResponseEntity.ok(
                service.getDonors(month, year)
        );
    }

    @PostMapping("/update-death-cases")
    public ResponseEntity<MonthlySahyog> updateDeaths(
            @RequestParam int month,
            @RequestParam int year) {

        return ResponseEntity.ok(
                service.updateDeathCases(month, year));
    }

    @PostMapping("/freeze")
    public ResponseEntity<MonthlySahyog> freeze(
            @RequestParam int month,
            @RequestParam int year) {

        return ResponseEntity.ok(
                service.freezeMonth(month, year));
    }

    @GetMapping("/non-donors/export")
    public void exportNonDonors(
            @RequestParam int month,
            @RequestParam int year,
            HttpServletResponse response) throws Exception {

        response.setContentType("text/csv");
        response.setHeader(
                "Content-Disposition",
                "attachment; filename=non_donors_" + month + "_" + year + ".csv"
        );

        service.exportNonDonorsCsv(month, year, response.getWriter());
    }
}