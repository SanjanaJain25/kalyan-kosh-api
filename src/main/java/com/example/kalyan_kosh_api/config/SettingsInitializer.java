package com.example.kalyan_kosh_api.config;

import com.example.kalyan_kosh_api.entity.SystemSetting;
import com.example.kalyan_kosh_api.repository.SystemSettingRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class SettingsInitializer {

    private final SystemSettingRepository repository;

    public SettingsInitializer(SystemSettingRepository repository) {
        this.repository = repository;
    }

    @PostConstruct
    public void init() {
        repository.findBySettingKey("mobile_otp_enabled")
                .orElseGet(() -> {
                    SystemSetting setting = new SystemSetting();
                    setting.setSettingKey("mobile_otp_enabled");
                    setting.setSettingValue("false");
                    setting.setUpdatedAt(Instant.now());
                    return repository.save(setting);
                });
                    repository.findBySettingKey("export_mobile_number_enabled")
            .orElseGet(() -> {
                SystemSetting setting = new SystemSetting();
                setting.setSettingKey("export_mobile_number_enabled");
                setting.setSettingValue("false");
                setting.setUpdatedAt(Instant.now());
                return repository.save(setting);
            });
            repository.findBySettingKey("self_donation_visible")
        .orElseGet(() -> {
            SystemSetting setting = new SystemSetting();
            setting.setSettingKey("self_donation_visible");
            setting.setSettingValue("false");
            setting.setUpdatedAt(Instant.now());
            return repository.save(setting);
        });

repository.findBySettingKey("self_donation_qr_url")
        .orElseGet(() -> {
            SystemSetting setting = new SystemSetting();
            setting.setSettingKey("self_donation_qr_url");
            setting.setSettingValue("");
            setting.setUpdatedAt(Instant.now());
            return repository.save(setting);
        });
    }
}