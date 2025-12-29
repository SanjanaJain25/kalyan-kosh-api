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

@RestController
@RequestMapping("/api/admin/export")
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = "*")
public class ExportController {

    private final AdminReceiptService receiptService;
    private final ExportService exportService;

    public ExportController(AdminReceiptService receiptService,
                            ExportService exportService) {
        this.receiptService = receiptService;
        this.exportService = exportService;
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
