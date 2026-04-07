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
import com.example.kalyan_kosh_api.service.MonthlySahyogService;
import com.example.kalyan_kosh_api.service.UserService;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/export")
@PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN')")
@CrossOrigin(origins = "*")
public class ExportController {

    private final AdminReceiptService receiptService;
    private final ExportService exportService;
private final MonthlySahyogService monthlySahyogService;
private final UserService userService;

    public ExportController(AdminReceiptService receiptService,
                            ExportService exportService,MonthlySahyogService monthlySahyogService,
UserService userService) {
        this.receiptService = receiptService;
        this.exportService = exportService;
        this.monthlySahyogService = monthlySahyogService;
this.userService = userService;
    }
@PostMapping("/insurance-inquiries/email")
public ResponseEntity<?> exportInsuranceInquiriesAndSendEmail() {
    return ResponseEntity.ok(exportService.exportInsuranceInquiriesAndSendEmail());
}

@GetMapping("/sahyog")
public ResponseEntity<byte[]> exportSahyog(
        @RequestParam int month,
        @RequestParam int year,
        @RequestParam(required = false) String name,
        @RequestParam(required = false) String mobile,
        @RequestParam(required = false) String userId,
        @RequestParam(required = false) String sambhag,
        @RequestParam(required = false) String district,
        @RequestParam(required = false) String block,
        @RequestParam(required = false) String beneficiary
) {
    LocalDate sahyogDate = LocalDate.of(year, month, 1);

    var data = monthlySahyogService.getDonorsForExport(
            sahyogDate, name, mobile, userId, sambhag, district, block, beneficiary
    );

byte[] csvBytes = exportService.exportCsvWithBom(exportService.exportDonorsCsv(data));

return ResponseEntity.ok()
        .header("Content-Disposition", "attachment; filename=sahyog.csv")
        .header("Content-Type", "text/csv; charset=UTF-8")
        .body(csvBytes);
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

    byte[] csvBytes = exportService.exportCsvWithBom(exportService.exportDonorsCsv(data));

    return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=sahyog_by_beneficiary.csv")
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

    byte[] csvBytes = exportService.exportCsvWithBom(exportService.exportNonDonorsCsv(data));

    return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=asahyog_by_beneficiary.csv")
            .header("Content-Type", "text/csv; charset=UTF-8")
            .body(csvBytes);
}

@GetMapping("/asahyog")
public ResponseEntity<byte[]> exportAsahyog(
        @RequestParam int month,
        @RequestParam int year,
        @RequestParam(required = false) String name,
        @RequestParam(required = false) String mobile,
        @RequestParam(required = false) String userId,
        @RequestParam(required = false) String sambhag,
        @RequestParam(required = false) String district,
        @RequestParam(required = false) String block
) {
    LocalDate sahyogDate = LocalDate.of(year, month, 1);

    var data = monthlySahyogService.getNonDonorsForExport(
            sahyogDate, name, mobile, userId, sambhag, district, block
    );

    byte[] csvBytes = exportService.exportCsvWithBom(exportService.exportNonDonorsCsv(data));

    return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=asahyog.csv")
            .header("Content-Type", "text/csv; charset=UTF-8")
            .body(csvBytes);
}

@GetMapping("/pending-profiles")
public ResponseEntity<byte[]> exportPendingProfiles(
        @RequestParam(required = false) String sambhagId,
        @RequestParam(required = false) String districtId,
        @RequestParam(required = false) String blockId,
        @RequestParam(required = false) String name,
        @RequestParam(required = false) String mobile,
        @RequestParam(required = false) String userId
) {
    var data = userService.getPendingProfileUsersForExport(
            sambhagId, districtId, blockId, name, mobile, userId
    );

    byte[] csvBytes = exportService.exportCsvWithBom(exportService.exportPendingProfilesCsv(data));

    return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=pending_profiles.csv")
            .header("Content-Type", "text/csv; charset=UTF-8")
            .body(csvBytes);
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
