package com.example.kalyan_kosh_api.config;

import com.example.kalyan_kosh_api.service.SystemSettingService;
import org.springframework.stereotype.Component;

@Component
public class SettingsInitializer {

    private final SystemSettingService systemSettingService;

    public SettingsInitializer(SystemSettingService systemSettingService) {
        this.systemSettingService = systemSettingService;
    }

    public void initializeDefaults() {
        systemSettingService.initializeDefaultSettings();
    }
}
