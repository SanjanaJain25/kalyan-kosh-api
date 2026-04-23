package com.example.kalyan_kosh_api.controller;

import com.example.kalyan_kosh_api.service.SystemSettingService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.example.kalyan_kosh_api.service.storage.FileStorageService;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/settings")
@PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN','SAMBHAG_MANAGER')")
public class AdminSettingsController {

    private final SystemSettingService settingService;

  private final FileStorageService fileStorageService;

public AdminSettingsController(SystemSettingService settingService,
                               FileStorageService fileStorageService) {
    this.settingService = settingService;
    this.fileStorageService = fileStorageService;
}


    @GetMapping("/mobile-otp")
    public Map<String, Boolean> getMobileOtpSetting() {
        return Map.of(
                "mobileOtpEnabled",
                settingService.isMobileOtpEnabled()
        );
    }
@GetMapping("/self-donation-visible")
public Map<String, Boolean> getSelfDonationVisibleSetting() {
    return Map.of(
            "selfDonationVisible",
            settingService.isSelfDonationVisible()
    );
}
@PutMapping("/self-donation-visible")
public Map<String, Object> updateSelfDonationVisible(@RequestBody Map<String, Boolean> req) {
    boolean enabled = req.getOrDefault("enabled", false);

    settingService.updateSelfDonationVisible(enabled);

    return Map.of(
            "success", true,
            "selfDonationVisible", enabled
    );
}
@GetMapping("/self-donation-qr")
public Map<String, String> getSelfDonationQr() {
    return Map.of(
            "qrUrl",
            settingService.getSelfDonationQrUrl()
    );
}
@PostMapping(value = "/self-donation-qr", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public Map<String, Object> uploadSelfDonationQr(@RequestParam("file") MultipartFile file) {
    if (file == null || file.isEmpty()) {
        throw new IllegalArgumentException("QR code file is required");
    }

    String contentType = file.getContentType();
    if (contentType == null || !contentType.startsWith("image/")) {
        throw new IllegalArgumentException("Only image files are allowed for QR code");
    }

    String fileUrl = fileStorageService.storeWithCustomName(file, "self-donation", "qr-code");
    settingService.updateSelfDonationQrUrl(fileUrl);

    return Map.of(
            "success", true,
            "qrUrl", fileUrl
    );
}
    @PutMapping("/mobile-otp")
    public Map<String, Object> updateMobileOtp(@RequestBody Map<String, Boolean> req) {
        boolean enabled = req.getOrDefault("enabled", false);

        settingService.updateMobileOtpSetting(enabled);

        return Map.of(
                "success", true,
                "mobileOtpEnabled", enabled
        );
    }

    @GetMapping("/export-mobile-number")
public Map<String, Boolean> getExportMobileNumberSetting() {
    return Map.of(
            "exportMobileNumberEnabled",
            settingService.isExportMobileNumberEnabled()
    );
}
@PutMapping("/export-mobile-number")
public Map<String, Object> updateExportMobileNumber(@RequestBody Map<String, Boolean> req) {
    boolean enabled = req.getOrDefault("enabled", false);

    settingService.updateExportMobileNumberSetting(enabled);

    return Map.of(
            "success", true,
            "exportMobileNumberEnabled", enabled
    );
}
@GetMapping("/district-manager-export-mobile")
public Map<String, Boolean> getDistrictManagerExportMobileSetting() {
    return Map.of(
            "districtManagerExportMobileEnabled",
            settingService.isDistrictManagerExportMobileEnabled()
    );
}

@PutMapping("/district-manager-export-mobile")
public Map<String, Object> updateDistrictManagerExportMobile(@RequestBody Map<String, Boolean> req) {
    boolean enabled = req.getOrDefault("enabled", false);

    settingService.updateDistrictManagerExportMobileSetting(enabled);

    return Map.of(
            "success", true,
            "districtManagerExportMobileEnabled", enabled
    );
}
@GetMapping("/block-manager-export-mobile")
public Map<String, Boolean> getBlockManagerExportMobileSetting() {
    return Map.of(
            "blockManagerExportMobileEnabled",
            settingService.isBlockManagerExportMobileEnabled()
    );
}

@PutMapping("/block-manager-export-mobile")
public Map<String, Object> updateBlockManagerExportMobile(@RequestBody Map<String, Boolean> req) {
    boolean enabled = req.getOrDefault("enabled", false);

    settingService.updateBlockManagerExportMobileSetting(enabled);

    return Map.of(
            "success", true,
            "blockManagerExportMobileEnabled", enabled
    );
}
@GetMapping("/profile-field-locks")
public Map<String, Boolean> getProfileFieldLocks() {
    return settingService.getProfileFieldLockSettings();
}

@PutMapping("/profile-field-locks")
public Map<String, Object> updateProfileFieldLocks(@RequestBody Map<String, Boolean> req) {
    settingService.updateProfileFieldLockSettings(req);

    return Map.of(
            "success", true,
            "settings", settingService.getProfileFieldLockSettings()
    );
}
}