package com.example.kalyan_kosh_api.controller;

import com.example.kalyan_kosh_api.dto.NonDonorResponse;
import com.example.kalyan_kosh_api.entity.MonthlySahyog;
import com.example.kalyan_kosh_api.service.MonthlySahyogService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/monthly-sahyog")
@CrossOrigin(origins = "*")
public class AdminMonthlySahyogController {

    private final MonthlySahyogService service;

    public AdminMonthlySahyogController(MonthlySahyogService service) {
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

    @PostMapping("/generate")
    public ResponseEntity<MonthlySahyog> generate(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate sahyogDate,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {

        return ResponseEntity.ok(service.generate(resolveDate(sahyogDate, month, year)));
    }

    @GetMapping("/non-donors")
    public ResponseEntity<List<NonDonorResponse>> nonDonors(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate sahyogDate,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {

        return ResponseEntity.ok(service.getNonDonors(resolveDate(sahyogDate, month, year)));
    }

    @GetMapping("/donors")
    public ResponseEntity<?> donors(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate sahyogDate,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {

        return ResponseEntity.ok(service.getDonors(resolveDate(sahyogDate, month, year)));
    }

    @PostMapping("/update-death-cases")
    public ResponseEntity<MonthlySahyog> updateDeaths(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate sahyogDate,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {

        return ResponseEntity.ok(service.updateDeathCases(resolveDate(sahyogDate, month, year)));
    }

    @PostMapping("/freeze")
    public ResponseEntity<MonthlySahyog> freeze(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate sahyogDate,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {

        return ResponseEntity.ok(service.freezeMonth(resolveDate(sahyogDate, month, year)));
    }

    @GetMapping("/non-donors/export")
    public void exportNonDonors(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate sahyogDate,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            HttpServletResponse response) throws Exception {

        LocalDate date = resolveDate(sahyogDate, month, year);

        response.setContentType("text/csv");
        response.setHeader(
                "Content-Disposition",
                "attachment; filename=non_donors_" + date + ".csv"
        );

        service.exportNonDonorsCsv(date, response.getWriter());
    }
}