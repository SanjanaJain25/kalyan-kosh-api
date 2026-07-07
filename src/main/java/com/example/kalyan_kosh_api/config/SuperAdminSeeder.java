package com.example.kalyan_kosh_api.config;

import com.example.kalyan_kosh_api.entity.Role;
import com.example.kalyan_kosh_api.entity.User;
import com.example.kalyan_kosh_api.entity.UserStatus;
import com.example.kalyan_kosh_api.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;

@Configuration
public class SuperAdminSeeder {

    public static final String SUPER_ADMIN_ID = "PMUMS202502";
    public static final String SUPER_ADMIN_PASSWORD = "Jyoti@7909";

    @Bean
    public CommandLineRunner seedSuperAdmin(UserRepository userRepository,
                                            PasswordEncoder passwordEncoder) {
        return args -> {
            User user = userRepository.findById(SUPER_ADMIN_ID).orElse(null);
            boolean isNewSuperAdmin = false;

            if (user == null) {
                user = new User();
                user.setId(SUPER_ADMIN_ID);
                user.setCreatedAt(Instant.now());

                user.setName("Super");
                user.setSurname("Admin");
                user.setEmail("superadmin@pmums.com");
                user.setMobileNumber("9999999999");
                user.setCountryCode("+91");
                user.setPasswordHash(passwordEncoder.encode(SUPER_ADMIN_PASSWORD));

                isNewSuperAdmin = true;
            }

            /*
             * These two are system/security fields.
             * Keep SuperAdmin always active and always ROLE_SUPERADMIN.
             */
            user.setRole(Role.ROLE_SUPERADMIN);
            user.setStatus(UserStatus.ACTIVE);

            /*
             * Safety check only.
             * If password hash is missing/null, then set default password.
             * Otherwise do not overwrite password changed from frontend.
             */
            if (user.getPasswordHash() == null || user.getPasswordHash().isBlank()) {
                user.setPasswordHash(passwordEncoder.encode(SUPER_ADMIN_PASSWORD));
            }

            if (user.getCreatedAt() == null) {
                user.setCreatedAt(Instant.now());
            }

            if (isNewSuperAdmin || user.getUpdatedAt() == null) {
                user.setUpdatedAt(Instant.now());
            }

            userRepository.save(user);
        };
    }
}