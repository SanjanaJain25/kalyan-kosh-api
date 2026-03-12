package com.example.kalyan_kosh_api.controller;

import com.example.kalyan_kosh_api.service.SystemSettingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/public")
public class PublicSettingsController {

    private final SystemSettingService settingService;

    public PublicSettingsController(SystemSettingService settingService) {
        this.settingService = settingService;
    }

    @GetMapping("/self-donation-settings")
    public Map<String, Object> getSelfDonationSettings() {
        return Map.of(
                "selfDonationVisible", settingService.isSelfDonationVisible(),
                "qrUrl", settingService.getSelfDonationQrUrl()
        );
    }
}