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
public ResponseEntity<String> exportSahyog(
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

    return ResponseEntity.ok(exportService.exportDonorsCsv(data));
}

@GetMapping("/asahyog")
public ResponseEntity<String> exportAsahyog(
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

    return ResponseEntity.ok(exportService.exportNonDonorsCsv(data));
}

@GetMapping("/pending-profiles")
public ResponseEntity<String> exportPendingProfiles(
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

    return ResponseEntity.ok(exportService.exportPendingProfilesCsv(data));
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
