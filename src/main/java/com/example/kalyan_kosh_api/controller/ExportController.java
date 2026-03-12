package com.example.kalyan_kosh_api.controller;

import com.example.kalyan_kosh_api.service.AdminReceiptService;
import com.example.kalyan_kosh_api.service.ExportService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("/api/admin/export")
@PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN')")
@CrossOrigin(origins = "*")
public class ExportController {

    private final AdminReceiptService receiptService;
    private final ExportService exportService;

    public ExportController(AdminReceiptService receiptService,
                            ExportService exportService) {
        this.receiptService = receiptService;
        this.exportService = exportService;
    }
@PostMapping("/insurance-inquiries/email")
public ResponseEntity<?> exportInsuranceInquiriesAndSendEmail() {
    return ResponseEntity.ok(exportService.exportInsuranceInquiriesAndSendEmail());
}

    @GetMapping("/receipts")
    public ResponseEntity<String> export(
            @RequestParam int month,
            @RequestParam int year
    ) {
        var data = receiptService.list(month, year);
        return ResponseEntity.ok(exportService.exportCsv(data));
    }
}
