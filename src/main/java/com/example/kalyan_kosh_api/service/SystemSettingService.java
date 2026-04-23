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

    // Profile field lock settings
    getOrCreateSetting("profile_lock_full_name", "false");
    getOrCreateSetting("profile_lock_date_of_birth", "false");
    getOrCreateSetting("profile_lock_mobile_number", "false");
    getOrCreateSetting("profile_lock_email", "false");
    getOrCreateSetting("profile_lock_department_unique_id", "false");
    getOrCreateSetting("home_notice_html",
        "👉 वर्तमान सहयोग प्रक्रिया समाप्त हो चुकी है।<br /><br />" +
        "आगामी सहयोग से संबंधित सभी अपडेट एवं जानकारी के लिए कृपया WhatsApp चैनल/ग्रुप को अवश्य फॉलो करें।<br /><br />" +
        "<a href=\"https://www.whatsapp.com/channel/0029Vaw20ci5K3zZkmv3jV1g\" target=\"_blank\" rel=\"noopener noreferrer\">WhatsApp चैनल देखें</a>");

getOrCreateSetting("statistics_content_html",
        "दिवंगत साथी<br />" +
        "स्व. श्री रेवाराम अलोने जी (जिला धार)<br />" +
        "सदस्यता क्रमांक: PMUMS 202411574<br />" +
        "सदस्यता दिनांक: 18/09/2025<br />" +
        "मृत्यु दिनांक: 20/12/2025<br />" +
        "स्व. श्री महेंद्र सिंह मुवेल जी (जिला धार)<br />" +
        "सदस्यता क्रमांक: PMUMS 20248814<br />" +
        "सदस्यता दिनांक: 12/09/2025<br />" +
        "मृत्यु दिनांक: 25/12/2025<br /><br />" +
        "<b>⚠️ सहयोग हेतु महत्वपूर्ण निर्देश</b><br /><br />" +
        "1.कृपया अपने लॉगिन में दिए गए निर्धारित परिवार के QR कोड पर ही सहयोग करें।<br />" +
        "2.व्हाट्सएप ग्रुप या अन्य किसी माध्यम से प्राप्त QR कोड पर सहयोग न करें।<br />" +
        "3.सहयोग केवल स्वयं / पति / पत्नी / पुत्र / पुत्री / नामांकित (Nominee) के खाते से ही करें। " +
        "यदि इनमें से कोई UPI का उपयोग नहीं करता है, तो कृपया किसी एक की UPI ID अवश्य बनवा लें।<br />" +
        "4.यह योजना “सहयोग के बदले सहयोग” सिद्धांत पर आधारित है। " +
        "यदि कोई सदस्य सहयोग नहीं करता है और भविष्य में उसके साथ कोई अप्रिय घटना होती है, " +
        "तो उसे योजना का लाभ (सहयोग) प्रदान नहीं किया जाएगा।<br />" +
        "5.सभी सदस्य अपनी प्रोफाइल अनिवार्य अपडेट कर लें। किसी भी समस्या के समाधान हेतु व्हाट्सएप नंबर.... पर मेसेज करें.<br />" +
        "<b>WhatsApp helpline 6262565803</b>");
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
public boolean isProfileLockFullNameEnabled() {
    return Boolean.parseBoolean(
            getOrCreateSetting("profile_lock_full_name", "false").getSettingValue()
    );
}

public void updateProfileLockFullName(boolean enabled) {
    SystemSetting setting = getOrCreateSetting("profile_lock_full_name", "false");
    setting.setSettingValue(String.valueOf(enabled));
    setting.setUpdatedAt(Instant.now());
    repo.save(setting);
}

public boolean isProfileLockDateOfBirthEnabled() {
    return Boolean.parseBoolean(
            getOrCreateSetting("profile_lock_date_of_birth", "false").getSettingValue()
    );
}

public void updateProfileLockDateOfBirth(boolean enabled) {
    SystemSetting setting = getOrCreateSetting("profile_lock_date_of_birth", "false");
    setting.setSettingValue(String.valueOf(enabled));
    setting.setUpdatedAt(Instant.now());
    repo.save(setting);
}

public boolean isProfileLockMobileNumberEnabled() {
    return Boolean.parseBoolean(
            getOrCreateSetting("profile_lock_mobile_number", "false").getSettingValue()
    );
}

public void updateProfileLockMobileNumber(boolean enabled) {
    SystemSetting setting = getOrCreateSetting("profile_lock_mobile_number", "false");
    setting.setSettingValue(String.valueOf(enabled));
    setting.setUpdatedAt(Instant.now());
    repo.save(setting);
}

public boolean isProfileLockEmailEnabled() {
    return Boolean.parseBoolean(
            getOrCreateSetting("profile_lock_email", "false").getSettingValue()
    );
}

public void updateProfileLockEmail(boolean enabled) {
    SystemSetting setting = getOrCreateSetting("profile_lock_email", "false");
    setting.setSettingValue(String.valueOf(enabled));
    setting.setUpdatedAt(Instant.now());
    repo.save(setting);
}

public boolean isProfileLockDepartmentUniqueIdEnabled() {
    return Boolean.parseBoolean(
            getOrCreateSetting("profile_lock_department_unique_id", "false").getSettingValue()
    );
}

public void updateProfileLockDepartmentUniqueId(boolean enabled) {
    SystemSetting setting = getOrCreateSetting("profile_lock_department_unique_id", "false");
    setting.setSettingValue(String.valueOf(enabled));
    setting.setUpdatedAt(Instant.now());
    repo.save(setting);
}

public java.util.Map<String, Boolean> getProfileFieldLockSettings() {
    java.util.Map<String, Boolean> settings = new java.util.HashMap<>();
    settings.put("fullName", isProfileLockFullNameEnabled());
    settings.put("dateOfBirth", isProfileLockDateOfBirthEnabled());
    settings.put("mobileNumber", isProfileLockMobileNumberEnabled());
    settings.put("email", isProfileLockEmailEnabled());
    settings.put("departmentUniqueId", isProfileLockDepartmentUniqueIdEnabled());
    return settings;
}

public void updateProfileFieldLockSettings(java.util.Map<String, Boolean> req) {
    updateProfileLockFullName(req.getOrDefault("fullName", false));
    updateProfileLockDateOfBirth(req.getOrDefault("dateOfBirth", false));
    updateProfileLockMobileNumber(req.getOrDefault("mobileNumber", false));
    updateProfileLockEmail(req.getOrDefault("email", false));
    updateProfileLockDepartmentUniqueId(req.getOrDefault("departmentUniqueId", false));
}
public String getHomeNoticeHtml() {
    return getOrCreateSetting("home_notice_html", "").getSettingValue();
}

public void updateHomeNoticeHtml(String value) {
    SystemSetting setting = getOrCreateSetting("home_notice_html", "");
    setting.setSettingValue(value != null ? value : "");
    setting.setUpdatedAt(Instant.now());
    repo.save(setting);
}

public String getStatisticsContentHtml() {
    return getOrCreateSetting("statistics_content_html", "").getSettingValue();
}

public void updateStatisticsContentHtml(String value) {
    SystemSetting setting = getOrCreateSetting("statistics_content_html", "");
    setting.setSettingValue(value != null ? value : "");
    setting.setUpdatedAt(Instant.now());
    repo.save(setting);
}

public java.util.Map<String, String> getHomeDisplayContentSettings() {
    java.util.Map<String, String> settings = new java.util.HashMap<>();
    settings.put("homeNoticeHtml", getHomeNoticeHtml());
    settings.put("statisticsContentHtml", getStatisticsContentHtml());
    return settings;
}

public void updateHomeDisplayContentSettings(java.util.Map<String, String> req) {
    updateHomeNoticeHtml(req.get("homeNoticeHtml"));
    updateStatisticsContentHtml(req.get("statisticsContentHtml"));
}
}