package com.example.kalyan_kosh_api.controller;

import com.example.kalyan_kosh_api.dto.AdminReceiptResponse;
import com.example.kalyan_kosh_api.dto.DonorResponse;
import com.example.kalyan_kosh_api.service.AdminReceiptService;
import com.example.kalyan_kosh_api.service.ExportService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/admin/receipts")
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = "*")
public class  AdminReceiptController {

    private final AdminReceiptService service;
    private final ExportService exportService;

    public AdminReceiptController(AdminReceiptService service, ExportService exportService) {
        this.service = service;
        this.exportService = exportService;
    }

    // LIST receipts with filters
    @GetMapping
    public ResponseEntity<List<AdminReceiptResponse>> list(
            @RequestParam int month,
            @RequestParam int year
    ) {
        return ResponseEntity.ok(service.list(month, year));
    }
    
    // GET ALL receipts without any filters with pagination
    @GetMapping("/all")
    public ResponseEntity<Page<AdminReceiptResponse>> getAllReceipts(
            @PageableDefault(size = 20, sort = "paymentDate", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(service.getAllReceipts(pageable));
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
    
    /**
     * Export all receipts to Excel file (Admin only)
     */
    @GetMapping("/export")
    public void exportReceipts(HttpServletResponse response) throws IOException {
        // Get all receipts without pagination for export
        List<AdminReceiptResponse> receipts = service.getAllReceiptsForExport();
        
        // Generate filename with current timestamp
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = "receipts_" + timestamp + ".xlsx";
        
        // Set response headers for Excel file download
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
        
        // Generate Excel file and write to response
        byte[] excelData = exportService.exportReceiptsExcel(receipts);
        response.getOutputStream().write(excelData);
        response.getOutputStream().flush();
    }

}
