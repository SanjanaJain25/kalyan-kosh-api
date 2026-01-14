package com.example.kalyan_kosh_api.service;

import com.example.kalyan_kosh_api.entity.EmailOtpVerification;
import com.example.kalyan_kosh_api.repository.EmailOtpVerificationRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;

@Service
public class EmailOtpService {

    private final EmailOtpVerificationRepository emailOtpRepo;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public EmailOtpService(EmailOtpVerificationRepository emailOtpRepo,
                           PasswordEncoder passwordEncoder,
                           EmailService emailService) {
        this.emailOtpRepo = emailOtpRepo;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    /**
     * Generate and send OTP to email
     */
    @Transactional
    public void sendEmailOtp(String email) {

        // Generate 6-digit OTP
        String otp = generateOtp();

        // Hash the OTP for secure storage
        String hash = passwordEncoder.encode(otp);

        // Create OTP entity
        EmailOtpVerification otpEntity = EmailOtpVerification.builder()
                .email(email.toLowerCase().trim())
                .otpHash(hash)
                .expiresAt(Instant.now().plusSeconds(300)) // 5 minutes validity
                .verified(false)
                .attempts(0)
                .build();

        // Delete any existing OTP for this email
        emailOtpRepo.deleteByEmail(email.toLowerCase().trim());

        // Save new OTP
        emailOtpRepo.save(otpEntity);

        // Send OTP via email
        emailService.sendOtpEmail(email, otp);
    }

    /**
     * Verify the OTP provided by user
     */
    @Transactional
    public void verifyEmailOtp(String email, String otp) {

        String normalizedEmail = email.toLowerCase().trim();

        // Find non-expired OTP
        EmailOtpVerification savedOtp = emailOtpRepo
                .findByEmailAndExpiresAtAfter(normalizedEmail, Instant.now())
                .orElseThrow(() -> new RuntimeException("OTP expired or not found"));

        // Increment attempts
        savedOtp.setAttempts(savedOtp.getAttempts() + 1);

        // Check max attempts (optional security feature)
        if (savedOtp.getAttempts() > 5) {
            emailOtpRepo.deleteByEmail(normalizedEmail);
            throw new RuntimeException("Too many failed attempts. Please request a new OTP.");
        }

        // Verify OTP
        if (!passwordEncoder.matches(otp, savedOtp.getOtpHash())) {
            emailOtpRepo.save(savedOtp); // Save updated attempts
            throw new RuntimeException("Invalid OTP");
        }

        // Mark as verified
        savedOtp.setVerified(true);
        emailOtpRepo.save(savedOtp);

        // Delete OTP after successful verification (single use)
        emailOtpRepo.deleteByEmail(normalizedEmail);
    }

    /**
     * Generate a random 6-digit OTP
     */
    private String generateOtp() {
        return String.valueOf(
                100000 + new SecureRandom().nextInt(900000)
        );
    }
}

