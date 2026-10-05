package com.example.kalyan_kosh_api.config;

import com.example.kalyan_kosh_api.entity.Role;
import com.example.kalyan_kosh_api.entity.User;
import com.example.kalyan_kosh_api.entity.UserStatus;
import com.example.kalyan_kosh_api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class SuperAdminSeeder {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String superAdminId;
    private final String superAdminPassword;
    private final String superAdminEmail;
    private final String superAdminMobile;

    public SuperAdminSeeder(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.seed.super-admin.id:PMUMS202502}") String superAdminId,
            @Value("${app.seed.super-admin.password:Jyoti@7909}") String superAdminPassword,
            @Value("${app.seed.super-admin.email:superadmin@pmums.com}") String superAdminEmail,
            @Value("${app.seed.super-admin.mobile:9999999999}") String superAdminMobile
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.superAdminId = superAdminId;
        this.superAdminPassword = superAdminPassword;
        this.superAdminEmail = superAdminEmail;
        this.superAdminMobile = superAdminMobile;
    }

    public void seedIfMissing() {
        User user = userRepository.findById(superAdminId).orElse(null);
        boolean isNewSuperAdmin = false;

        if (user == null) {
            user = new User();
            user.setId(superAdminId);
            user.setCreatedAt(Instant.now());
            user.setName("Super");
            user.setSurname("Admin");
            user.setEmail(superAdminEmail);
            user.setMobileNumber(superAdminMobile);
            user.setCountryCode("+91");
            user.setPasswordHash(passwordEncoder.encode(superAdminPassword));
            isNewSuperAdmin = true;
        }

        user.setRole(Role.ROLE_SUPERADMIN);
        user.setStatus(UserStatus.ACTIVE);

        if (user.getPasswordHash() == null || user.getPasswordHash().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(superAdminPassword));
        }

        if (user.getCreatedAt() == null) {
            user.setCreatedAt(Instant.now());
        }

        if (isNewSuperAdmin || user.getUpdatedAt() == null) {
            user.setUpdatedAt(Instant.now());
        }

        userRepository.save(user);
    }
}
