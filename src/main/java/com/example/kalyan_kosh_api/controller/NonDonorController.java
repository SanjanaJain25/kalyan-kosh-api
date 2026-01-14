package com.example.kalyan_kosh_api.controller;

import com.example.kalyan_kosh_api.dto.PageResponse;
import com.example.kalyan_kosh_api.dto.UserResponse;
import com.example.kalyan_kosh_api.entity.User;
import com.example.kalyan_kosh_api.service.NonDonorService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/non-donors")
//@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = "*")
public class NonDonorController {

    private final NonDonorService service;

    public NonDonorController(NonDonorService service) {
        this.service = service;
    }

    /**
     * Get all non-donors (without pagination)
     */
    @GetMapping
    public ResponseEntity<List<User>> list(
            @RequestParam int month,
            @RequestParam int year
    ) {
        return ResponseEntity.ok(service.getNonDonors(month, year));
    }

    /**
     * Get non-donors with pagination - 20 records per page by default
     *
     * Usage: GET /api/admin/non-donors/paginated?month=1&year=2026&page=0&size=20
     */
    @GetMapping("/paginated")
    public ResponseEntity<PageResponse<UserResponse>> listPaginated(
            @RequestParam int month,
            @RequestParam int year,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(service.getNonDonorsPaginated(month, year, page, size));
    }
}
