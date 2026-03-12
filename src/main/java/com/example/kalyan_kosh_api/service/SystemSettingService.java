package com.example.kalyan_kosh_api.service;

import com.example.kalyan_kosh_api.entity.SystemSetting;
import com.example.kalyan_kosh_api.repository.SystemSettingRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class SystemSettingService {

    private final SystemSettingRepository repo;

    public SystemSettingService(SystemSettingRepository repo) {
        this.repo = repo;
    }

    public boolean isMobileOtpEnabled() {
        return repo.findBySettingKey("mobile_otp_enabled")
                .map(s -> Boolean.parseBoolean(s.getSettingValue()))
                .orElse(false);
    }

    public void updateMobileOtpSetting(boolean enabled) {
        SystemSetting setting = repo.findBySettingKey("mobile_otp_enabled")
                .orElse(new SystemSetting());

        setting.setSettingKey("mobile_otp_enabled");
        setting.setSettingValue(String.valueOf(enabled));
        setting.setUpdatedAt(Instant.now());

        repo.save(setting);
    }

    public boolean isExportMobileNumberEnabled() {
    return repo.findBySettingKey("export_mobile_number_enabled")
            .map(s -> Boolean.parseBoolean(s.getSettingValue()))
            .orElse(false);
}

public void updateExportMobileNumberSetting(boolean enabled) {
    SystemSetting setting = repo.findBySettingKey("export_mobile_number_enabled")
            .orElse(new SystemSetting());

    setting.setSettingKey("export_mobile_number_enabled");
    setting.setSettingValue(String.valueOf(enabled));
    setting.setUpdatedAt(Instant.now());

    repo.save(setting);
}

public boolean isSelfDonationVisible() {
    return repo.findBySettingKey("self_donation_visible")
            .map(s -> Boolean.parseBoolean(s.getSettingValue()))
            .orElse(false);
}

public void updateSelfDonationVisible(boolean enabled) {
    SystemSetting setting = repo.findBySettingKey("self_donation_visible")
            .orElse(new SystemSetting());

    setting.setSettingKey("self_donation_visible");
    setting.setSettingValue(String.valueOf(enabled));
    setting.setUpdatedAt(Instant.now());

    repo.save(setting);
}

public String getSelfDonationQrUrl() {
    return repo.findBySettingKey("self_donation_qr_url")
            .map(SystemSetting::getSettingValue)
            .orElse("");
}

public void updateSelfDonationQrUrl(String qrUrl) {
    SystemSetting setting = repo.findBySettingKey("self_donation_qr_url")
            .orElse(new SystemSetting());

    setting.setSettingKey("self_donation_qr_url");
    setting.setSettingValue(qrUrl != null ? qrUrl : "");
    setting.setUpdatedAt(Instant.now());

    repo.save(setting);
}
}