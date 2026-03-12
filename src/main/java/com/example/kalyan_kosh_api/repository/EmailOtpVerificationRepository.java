package com.example.kalyan_kosh_api.repository;

import com.example.kalyan_kosh_api.entity.EmailOtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface EmailOtpVerificationRepository extends JpaRepository<EmailOtpVerification, Long> {

    Optional<EmailOtpVerification> findByEmailAndExpiresAtAfter(String email, Instant now);

    void deleteByEmail(String email);
}

