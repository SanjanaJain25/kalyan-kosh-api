package com.example.kalyan_kosh_api.controller;

import com.example.kalyan_kosh_api.dto.DonorResponse;
import com.example.kalyan_kosh_api.dto.PageResponse;
import com.example.kalyan_kosh_api.dto.UserResponse;
import com.example.kalyan_kosh_api.entity.MonthlySahyog;
import com.example.kalyan_kosh_api.service.MonthlySahyogService;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/monthly-sahyog")
@CrossOrigin(origins = "*")
public class AdminMonthlySahyogController {

    private static final Logger log = LoggerFactory.getLogger(AdminMonthlySahyogController.class);

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
    public ResponseEntity<?> generate(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate sahyogDate,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        try {
            log.info("📊 Generating monthly sahyog for month={}, year={}, sahyogDate={}", month, year, sahyogDate);
            MonthlySahyog result = service.generate(resolveDate(sahyogDate, month, year));
            log.info("✅ Monthly sahyog generated successfully");
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            log.warn("⚠️ Invalid parameters for generate: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse("INVALID_PARAMS", e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Error generating monthly sahyog: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(createErrorResponse("GENERATION_ERROR", "Failed to generate monthly sahyog: " + e.getMessage()));
        }
    }

    /**
     * ✅ FAST PAGINATED: Get non-donors with 250 records per page
     * Usage: GET /api/admin/monthly-sahyog/non-donors?month=1&year=2026&page=0&size=250
     */
    @GetMapping("/non-donors")
    public ResponseEntity<?> nonDonorsPaginated(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate sahyogDate,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "250") int size) {
        try {
            log.info("📋 Fetching non-donors: month={}, year={}, page={}, size={}", month, year, page, size);
            LocalDate date = resolveDate(sahyogDate, month, year);
            PageResponse<UserResponse> result = service.getNonDonorsPaginated(date, page, size);
            log.info("✅ Non-donors fetched: {} records, page {}/{}", result.getContent().size(), page, result.getTotalPages());
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            log.warn("⚠️ Invalid parameters for non-donors: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse("INVALID_PARAMS", e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Error fetching non-donors: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(createErrorResponse("FETCH_ERROR", "Failed to fetch non-donors: " + e.getMessage()));
        }
    }

    /**
     * ✅ FAST PAGINATED: Get donors with 250 records per page
     * Usage: GET /api/admin/monthly-sahyog/donors?month=1&year=2026&page=0&size=250
     */
    @GetMapping("/donors")
    public ResponseEntity<?> donorsPaginated(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate sahyogDate,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "250") int size) {
        try {
            log.info("💰 Fetching donors: month={}, year={}, page={}, size={}", month, year, page, size);
            LocalDate date = resolveDate(sahyogDate, month, year);
            PageResponse<DonorResponse> result = service.getDonorsPaginated(date, page, size);
            log.info("✅ Donors fetched: {} records, page {}/{}", result.getContent().size(), page, result.getTotalPages());
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            log.warn("⚠️ Invalid parameters for donors: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse("INVALID_PARAMS", e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Error fetching donors: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(createErrorResponse("FETCH_ERROR", "Failed to fetch donors: " + e.getMessage()));
        }
    }

    /**
     * ✅ Search donors by full name (name + surname) and/or mobile number and/or userId
     * Usage: GET /api/admin/monthly-sahyog/donors/search?month=1&year=2026&name=राम शर्मा&mobile=9876&userId=PMUMS
     */
@GetMapping("/donors/search")
public ResponseEntity<?> searchDonors(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate sahyogDate,
        @RequestParam(required = false) Integer month,
        @RequestParam(required = false) Integer year,
        @RequestParam(required = false) String name,
        @RequestParam(required = false) String mobile,
        @RequestParam(required = false) String userId,
        @RequestParam(required = false) String sambhag,
        @RequestParam(required = false) String district,
        @RequestParam(required = false) String block,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "250") int size) {
    try {
        log.info("🔍 Searching donors: month={}, year={}, name={}, mobile={}, userId={}, sambhag={}, district={}, block={}, page={}, size={}",
                month, year, name, mobile, userId, sambhag, district, block, page, size);

        LocalDate date = resolveDate(sahyogDate, month, year);

        PageResponse<DonorResponse> result =
                service.searchDonors(date, name, mobile, userId, sambhag, district, block, page, size);

        log.info("✅ Donor search completed: {} records found", result.getContent().size());
        return ResponseEntity.ok(result);
    } catch (IllegalArgumentException e) {
        log.warn("⚠️ Invalid parameters for donor search: {}", e.getMessage());
        return ResponseEntity.badRequest().body(createErrorResponse("INVALID_PARAMS", e.getMessage()));
    } catch (Exception e) {
        log.error("❌ Error searching donors: {}", e.getMessage(), e);
        return ResponseEntity.internalServerError().body(createErrorResponse("SEARCH_ERROR", "Failed to search donors: " + e.getMessage()));
    }
}

@GetMapping("/donors/beneficiaries-all")
public ResponseEntity<?> getAllBeneficiaries() {
    try {
        return ResponseEntity.ok(service.getAllBeneficiaryOptions());
    } catch (Exception e) {
        return ResponseEntity.internalServerError()
                .body(createErrorResponse("FETCH_ERROR", "Failed to fetch beneficiaries: " + e.getMessage()));
    }
}

@GetMapping("/non-donors/beneficiaries-all")
public ResponseEntity<?> getAllBeneficiariesForNonDonors() {
    try {
        return ResponseEntity.ok(service.getAllBeneficiaryOptions());
    } catch (Exception e) {
        return ResponseEntity.internalServerError()
                .body(createErrorResponse("FETCH_ERROR", "Failed to fetch beneficiaries: " + e.getMessage()));
    }
}
@GetMapping("/donors/search-by-beneficiary")
public ResponseEntity<?> searchDonorsByBeneficiary(
        @RequestParam(required = false) Long beneficiaryId,
        @RequestParam(required = false) String name,
        @RequestParam(required = false) String mobile,
        @RequestParam(required = false) String userId,
        @RequestParam(required = false) String sambhag,
        @RequestParam(required = false) String district,
        @RequestParam(required = false) String block,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {
    try {
        PageResponse<DonorResponse> result = service.searchDonorsByBeneficiary(
                beneficiaryId, name, mobile, userId, sambhag, district, block, page, size
        );
        return ResponseEntity.ok(result);
    } catch (Exception e) {
        return ResponseEntity.internalServerError()
                .body(createErrorResponse("SEARCH_ERROR", "Failed to search donors: " + e.getMessage()));
    }
}

@GetMapping("/non-donors/search-by-beneficiary")
public ResponseEntity<?> searchNonDonorsByBeneficiary(
        @RequestParam(required = false) Long beneficiaryId,
        @RequestParam(required = false) String name,
        @RequestParam(required = false) String mobile,
        @RequestParam(required = false) String userId,
        @RequestParam(required = false) String sambhag,
        @RequestParam(required = false) String district,
        @RequestParam(required = false) String block,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {
    try {
        PageResponse<UserResponse> result = service.searchNonDonorsByBeneficiary(
                beneficiaryId, name, mobile, userId, sambhag, district, block, page, size
        );
        return ResponseEntity.ok(result);
    } catch (Exception e) {
        return ResponseEntity.internalServerError()
                .body(createErrorResponse("SEARCH_ERROR", "Failed to search non-donors: " + e.getMessage()));
    }
}
@GetMapping("/donors/beneficiaries")
public ResponseEntity<?> getDistinctBeneficiaries(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate sahyogDate,
        @RequestParam(required = false) Integer month,
        @RequestParam(required = false) Integer year) {
    try {
        LocalDate date = resolveDate(sahyogDate, month, year);
        return ResponseEntity.ok(service.getDistinctBeneficiaries(date));
    } catch (IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(createErrorResponse("INVALID_PARAMS", e.getMessage()));
    } catch (Exception e) {
        return ResponseEntity.internalServerError().body(createErrorResponse("FETCH_ERROR", "Failed to fetch beneficiaries: " + e.getMessage()));
    }
}
    /**
     * ✅ Search non-donors by full name (name + surname) and/or mobile number and/or userId
     * Usage: GET /api/admin/monthly-sahyog/non-donors/search?month=1&year=2026&name=राम शर्मा&mobile=9876&userId=PMUMS
     */
   @GetMapping("/non-donors/search")
public ResponseEntity<?> searchNonDonors(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate sahyogDate,
        @RequestParam(required = false) Integer month,
        @RequestParam(required = false) Integer year,
        @RequestParam(required = false) String name,
        @RequestParam(required = false) String mobile,
        @RequestParam(required = false) String userId,
        @RequestParam(required = false) String sambhag,
        @RequestParam(required = false) String district,
        @RequestParam(required = false) String block,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "250") int size) {
    try {
        log.info("🔍 Searching non-donors: month={}, year={}, name={}, mobile={}, userId={}, sambhag={}, district={}, block={}, page={}, size={}",
                 month, year, name, mobile, userId, sambhag, district, block, page, size);
        LocalDate date = resolveDate(sahyogDate, month, year);
        PageResponse<UserResponse> result =
                service.searchNonDonors(date, name, mobile, userId, sambhag, district, block, page, size);
        log.info("✅ Non-donor search completed: {} records found", result.getContent().size());
        return ResponseEntity.ok(result);
    } catch (IllegalArgumentException e) {
        log.warn("⚠️ Invalid parameters for non-donor search: {}", e.getMessage());
        return ResponseEntity.badRequest().body(createErrorResponse("INVALID_PARAMS", e.getMessage()));
    } catch (Exception e) {
        log.error("❌ Error searching non-donors: {}", e.getMessage(), e);
        return ResponseEntity.internalServerError().body(createErrorResponse("SEARCH_ERROR", "Failed to search non-donors: " + e.getMessage()));
    }
}

    @PostMapping("/update-death-cases")
    public ResponseEntity<?> updateDeaths(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate sahyogDate,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        try {
            log.info("🔄 Updating death cases for month={}, year={}", month, year);
            MonthlySahyog result = service.updateDeathCases(resolveDate(sahyogDate, month, year));
            log.info("✅ Death cases updated successfully");
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            log.warn("⚠️ Invalid parameters for update death cases: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse("INVALID_PARAMS", e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Error updating death cases: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(createErrorResponse("UPDATE_ERROR", "Failed to update death cases: " + e.getMessage()));
        }
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

    /**
     * Helper method to create error response
     */
    private Map<String, Object> createErrorResponse(String code, String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", true);
        error.put("code", code);
        error.put("message", message);
        error.put("timestamp", java.time.Instant.now().toString());
        return error;
    }
}

