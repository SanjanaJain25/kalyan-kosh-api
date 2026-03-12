package com.example.kalyan_kosh_api.controller;

import com.example.kalyan_kosh_api.dto.AdminDashboardSummaryResponse;
import com.example.kalyan_kosh_api.service.MonthlySahyogService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/dashboard")
@CrossOrigin(origins = "*")
public class AdminDashboardController {

    private final MonthlySahyogService service;

    public AdminDashboardController(MonthlySahyogService service) {
        this.service = service;
    }

    // Helper method to convert month/year to LocalDate (first day of month)
    private LocalDate resolveDate(LocalDate sahyogDate, Integer month, Integer year) {
        if (sahyogDate != null) {
            return sahyogDate;
        }
        if (month != null && year != null) {
            return LocalDate.of(year, month, 1);
        }
        throw new IllegalArgumentException("Either 'sahyogDate' or both 'month' and 'year' must be provided");
    }

    @GetMapping("/summary")
    public ResponseEntity<AdminDashboardSummaryResponse> summary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate sahyogDate,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {

        return ResponseEntity.ok(service.getDashboardSummary(resolveDate(sahyogDate, month, year)));
    }
}
