package com.example.kalyan_kosh_api.repository;

import com.example.kalyan_kosh_api.entity.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface    OtpVerificationRepository
        extends JpaRepository<OtpVerification, Long> {

    Optional<OtpVerification> findByMobileAndExpiresAtAfter(
            String mobile,
            Instant now
    );

    void deleteByMobile(String mobile);
}
