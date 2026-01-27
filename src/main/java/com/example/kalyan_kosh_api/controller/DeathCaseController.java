package com.example.kalyan_kosh_api.controller;

import com.example.kalyan_kosh_api.dto.CreateDeathCaseRequest;
import com.example.kalyan_kosh_api.dto.UpdateDeathCaseRequest;
import com.example.kalyan_kosh_api.dto.DeathCaseResponse;
import com.example.kalyan_kosh_api.service.DeathCaseService;
import com.example.kalyan_kosh_api.service.ExportService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/death-cases")
//@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@CrossOrigin(origins = "*")
public class DeathCaseController {

    private final DeathCaseService service;
    private final ExportService exportService;

    public DeathCaseController(DeathCaseService service, ExportService exportService) {
        this.service = service;
        this.exportService = exportService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DeathCaseResponse> create(
            @Valid @RequestPart("data") CreateDeathCaseRequest req,
            @RequestPart(value = "userImage", required = false) MultipartFile userImage,
            @RequestPart(value = "nominee1QrCode", required = false) MultipartFile nominee1QrCode,
            @RequestPart(value = "nominee2QrCode", required = false) MultipartFile nominee2QrCode,
            Authentication authentication) {

        return ResponseEntity.ok(
                service.create(req, userImage, nominee1QrCode, nominee2QrCode, authentication.getName())
        );
    }

    @GetMapping
    public ResponseEntity<List<DeathCaseResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeathCaseResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DeathCaseResponse> update(
            @PathVariable Long id,
            @Valid @RequestPart("data") UpdateDeathCaseRequest req,
            @RequestPart(value = "userImage", required = false) MultipartFile userImage,
            @RequestPart(value = "nominee1QrCode", required = false) MultipartFile nominee1QrCode,
            @RequestPart(value = "nominee2QrCode", required = false) MultipartFile nominee2QrCode,
            Authentication authentication) {

        return ResponseEntity.ok(
                service.update(id, req, userImage, nominee1QrCode, nominee2QrCode, authentication.getName())
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Hide a death case (Admin only)
     * The case will not be shown on the public/home page but remains in database
     */
    @PutMapping("/{id}/hide")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DeathCaseResponse> hide(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(service.hide(id, authentication.getName()));
    }

    /**
     * Deactivate/Hide a death case (Admin only) - PUT method
     * Alias for hide endpoint
     */
    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DeathCaseResponse> deactivate(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(service.hide(id, authentication.getName()));
    }

    /**
     * Show/Unhide a death case (Admin only)
     * The case will be visible on the public/home page again
     */
    @PutMapping("/{id}/show")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DeathCaseResponse> show(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(service.show(id, authentication.getName()));
    }

    /**
     * Activate/Show a death case (Admin only) - PUT method
     * Alias for show endpoint
     */
    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DeathCaseResponse> activate(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(service.show(id, authentication.getName()));
    }

    /**
     * Get only visible (non-hidden) death cases for public/home page
     * Returns only OPEN death cases (not CLOSED or HIDDEN)
     */
    @GetMapping("/public")
    public ResponseEntity<List<DeathCaseResponse>> getPublicCases() {
        return ResponseEntity.ok(service.getVisibleCases());
    }
    
    /**
     * Export all death cases to Excel file (Admin only)
     */
    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    public void exportDeathCases(HttpServletResponse response) throws IOException {
        List<DeathCaseResponse> deathCases = service.getAll();
        
        // Generate filename with current timestamp
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = "death_cases_" + timestamp + ".xlsx";
        
        // Set response headers for Excel file download
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
        
        // Generate Excel file and write to response
        byte[] excelData = exportService.exportDeathCasesExcel(deathCases);
        response.getOutputStream().write(excelData);
        response.getOutputStream().flush();
    }
}
