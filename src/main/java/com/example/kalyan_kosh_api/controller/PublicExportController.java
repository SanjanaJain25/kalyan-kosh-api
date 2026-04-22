package com.example.kalyan_kosh_api.controller;

import com.example.kalyan_kosh_api.service.ExportService;
import com.example.kalyan_kosh_api.service.MonthlySahyogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/export")
@CrossOrigin(origins = "*")
public class PublicExportController {

    private final MonthlySahyogService monthlySahyogService;
    private final ExportService exportService;

    public PublicExportController(
            MonthlySahyogService monthlySahyogService,
            ExportService exportService
    ) {
        this.monthlySahyogService = monthlySahyogService;
        this.exportService = exportService;
    }

    @GetMapping("/sahyog/by-beneficiary")
    public ResponseEntity<byte[]> exportSahyogByBeneficiary(
            @RequestParam(required = false) Long beneficiaryId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String mobile,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String sambhag,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) String block
    ) {
        var data = monthlySahyogService.getDonorsForExportByBeneficiary(
                beneficiaryId, name, mobile, userId, sambhag, district, block
        );

        byte[] csvBytes = exportService.exportCsvWithBom(
                exportService.exportDonorsCsv(data)
        );

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=sahyog_list.csv")
                .header("Content-Type", "text/csv; charset=UTF-8")
                .body(csvBytes);
    }

    @GetMapping("/asahyog/by-beneficiary")
    public ResponseEntity<byte[]> exportAsahyogByBeneficiary(
            @RequestParam(required = false) Long beneficiaryId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String mobile,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String sambhag,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) String block
    ) {
        var data = monthlySahyogService.getNonDonorsForExportByBeneficiary(
                beneficiaryId, name, mobile, userId, sambhag, district, block
        );

        byte[] csvBytes = exportService.exportCsvWithBom(
                exportService.exportNonDonorsCsv(data, false)
        );

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=asahyog_list.csv")
                .header("Content-Type", "text/csv; charset=UTF-8")
                .body(csvBytes);
    }
}