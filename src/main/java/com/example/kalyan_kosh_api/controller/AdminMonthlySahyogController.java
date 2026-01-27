package com.example.kalyan_kosh_api.controller;

import com.example.kalyan_kosh_api.dto.DonorResponse;
import com.example.kalyan_kosh_api.dto.PageResponse;
import com.example.kalyan_kosh_api.dto.UserResponse;
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

    /**
     * ✅ FAST PAGINATED: Get non-donors with 250 records per page
     * Usage: GET /api/admin/monthly-sahyog/non-donors?month=1&year=2026&page=0&size=250
     */
    @GetMapping("/non-donors")
    public ResponseEntity<PageResponse<UserResponse>> nonDonorsPaginated(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate sahyogDate,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "250") int size) {

        return ResponseEntity.ok(service.getNonDonorsPaginated(resolveDate(sahyogDate, month, year), page, size));
    }

    /**
     * ✅ FAST PAGINATED: Get donors with 250 records per page
     * Usage: GET /api/admin/monthly-sahyog/donors?month=1&year=2026&page=0&size=250
     */
    @GetMapping("/donors")
    public ResponseEntity<PageResponse<DonorResponse>> donorsPaginated(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate sahyogDate,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "250") int size) {

        return ResponseEntity.ok(service.getDonorsPaginated(resolveDate(sahyogDate, month, year), page, size));
    }

    /**
     * ✅ Search donors by full name (name + surname) and/or mobile number and/or userId
     * Usage: GET /api/admin/monthly-sahyog/donors/search?month=1&year=2026&name=राम शर्मा&mobile=9876&userId=PMUMS
     */
    @GetMapping("/donors/search")
    public ResponseEntity<PageResponse<DonorResponse>> searchDonors(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate sahyogDate,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String mobile,
            @RequestParam(required = false) String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "250") int size) {

        return ResponseEntity.ok(service.searchDonors(resolveDate(sahyogDate, month, year), name, mobile, userId, page, size));
    }

    /**
     * ✅ Search non-donors by full name (name + surname) and/or mobile number and/or userId
     * Usage: GET /api/admin/monthly-sahyog/non-donors/search?month=1&year=2026&name=राम शर्मा&mobile=9876&userId=PMUMS
     */
    @GetMapping("/non-donors/search")
    public ResponseEntity<PageResponse<UserResponse>> searchNonDonors(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate sahyogDate,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String mobile,
            @RequestParam(required = false) String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "250") int size) {

        return ResponseEntity.ok(service.searchNonDonors(resolveDate(sahyogDate, month, year), name, mobile, userId, page, size));
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