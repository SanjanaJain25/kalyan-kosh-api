package com.example.kalyan_kosh_api.controller;

import com.example.kalyan_kosh_api.dto.manager.ManagerAreaScope;
import com.example.kalyan_kosh_api.service.ExportService;
import com.example.kalyan_kosh_api.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@RestController
@RequestMapping("/api/live-export")
@CrossOrigin(origins = "*")
public class LiveExportController {

    private final UserService userService;
    private final ExportService exportService;

    @Value("${reports.live.pending-profile-key}")
    private String pendingProfileLiveReportKey;

    public LiveExportController(
            UserService userService,
            ExportService exportService
    ) {
        this.userService = userService;
        this.exportService = exportService;
    }

    @GetMapping(value = "/pending-profiles.csv", produces = "text/csv; charset=UTF-8")
    public ResponseEntity<byte[]> livePendingProfilesCsv(
            @RequestParam String key,
            @RequestParam(required = false) String sambhagId,
            @RequestParam(required = false) String districtId,
            @RequestParam(required = false) String blockId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String mobile,
            @RequestParam(required = false) String userId
    ) {
        if (!pendingProfileLiveReportKey.equals(key)) {
            return ResponseEntity.status(401)
                    .header("Content-Type", "text/plain; charset=UTF-8")
                    .body("Invalid live report key".getBytes());
        }

        /*
         * IMPORTANT:
         * userService.getPendingProfileUsersForExport(...) returns List<UserResponse>,
         * which is the type expected by exportService.exportPendingProfilesCsv(...).
         */
        ManagerAreaScope scope = new ManagerAreaScope(
                true,
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList()
        );

        var data = userService.getPendingProfileUsersForExport(
                sambhagId,
                districtId,
                blockId,
                name,
                mobile,
                userId,
                scope
        );

        boolean includeMobile = true;

        byte[] csvBytes = exportService.exportCsvWithBom(
                exportService.exportPendingProfilesCsv(data, includeMobile)
        );

        return ResponseEntity.ok()
                .header("Content-Disposition", "inline; filename=live_pending_profiles.csv")
                .header("Content-Type", "text/csv; charset=UTF-8")
                .header("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0")
                .header("Pragma", "no-cache")
                .body(csvBytes);
    }
}