package com.example.kalyan_kosh_api.service;

import com.example.kalyan_kosh_api.entity.SystemSetting;
import com.example.kalyan_kosh_api.repository.SystemSettingRepository;
import org.springframework.stereotype.Service;
import com.example.kalyan_kosh_api.entity.Role;
import java.time.Instant;

@Service
public class SystemSettingService {

    private final SystemSettingRepository repo;

    private SystemSetting getOrCreateSetting(String key, String defaultValue) {
    return repo.findBySettingKey(key).orElseGet(() -> {
        SystemSetting setting = new SystemSetting();
        setting.setSettingKey(key);
        setting.setSettingValue(defaultValue);
        setting.setUpdatedAt(Instant.now());
        return repo.save(setting);
    });
}
@jakarta.annotation.PostConstruct
public void initializeDefaultSettings() {
    getOrCreateSetting("mobile_otp_enabled", "false");
    getOrCreateSetting("export_mobile_number_enabled", "true");
    getOrCreateSetting("self_donation_visible", "false");
    getOrCreateSetting("self_donation_qr_url", "");
    getOrCreateSetting("district_manager_export_mobile_enabled", "false");
    getOrCreateSetting("block_manager_export_mobile_enabled", "false");
}

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
public boolean isDistrictManagerExportMobileEnabled() {
    return Boolean.parseBoolean(
            getOrCreateSetting("district_manager_export_mobile_enabled", "false").getSettingValue()
    );
}

public void updateDistrictManagerExportMobileSetting(boolean enabled) {
    SystemSetting setting = getOrCreateSetting("district_manager_export_mobile_enabled", "false");
    setting.setSettingValue(String.valueOf(enabled));
    setting.setUpdatedAt(Instant.now());
    repo.save(setting);
}

public boolean isBlockManagerExportMobileEnabled() {
    return Boolean.parseBoolean(
            getOrCreateSetting("block_manager_export_mobile_enabled", "false").getSettingValue()
    );
}

public void updateBlockManagerExportMobileSetting(boolean enabled) {
    SystemSetting setting = getOrCreateSetting("block_manager_export_mobile_enabled", "false");
    setting.setSettingValue(String.valueOf(enabled));
    setting.setUpdatedAt(Instant.now());
    repo.save(setting);
}
public boolean canExportMobileNumber(Role role) {
    if (role == null) return false;

    return switch (role) {
        case ROLE_SUPERADMIN, ROLE_ADMIN, ROLE_SAMBHAG_MANAGER -> true;
        case ROLE_DISTRICT_MANAGER -> isDistrictManagerExportMobileEnabled();
        case ROLE_BLOCK_MANAGER -> isBlockManagerExportMobileEnabled();
        case ROLE_USER -> false;
    };
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