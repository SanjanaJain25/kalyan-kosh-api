package com.example.kalyan_kosh_api.service;

import com.example.kalyan_kosh_api.entity.EmailOtpVerification;
import com.example.kalyan_kosh_api.repository.EmailOtpVerificationRepository;
import com.example.kalyan_kosh_api.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;

@Service
public class EmailOtpService {

    private static final Logger log = LoggerFactory.getLogger(EmailOtpService.class);

    private final EmailOtpVerificationRepository emailOtpRepo;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final UserRepository userRepository;

 public EmailOtpService(
        EmailOtpVerificationRepository emailOtpRepo,
        PasswordEncoder passwordEncoder,
        EmailService emailService,
        UserRepository userRepository
) {
    this.emailOtpRepo = emailOtpRepo;
    this.passwordEncoder = passwordEncoder;
    this.emailService = emailService;
    this.userRepository = userRepository;
}

    /**
     * Generate and send OTP to email for forgot password
     * First checks if email exists in database
     */
    @Transactional
    public void sendEmailOtp(String email) {
 String normalizedEmail = email.toLowerCase().trim();

if (userRepository.findByEmail(normalizedEmail).isEmpty()) {
    throw new IllegalArgumentException("Email does not exist. Please check or register first.");
}

log.info("Sending OTP for forgot password to email: {}", normalizedEmail);

String otp = generateOtp();
String hash = passwordEncoder.encode(otp);

EmailOtpVerification otpEntity = EmailOtpVerification.builder()
        .email(normalizedEmail)
        .otpHash(hash)
        .expiresAt(Instant.now().plusSeconds(600))
        .verified(false)
        .attempts(0)
        .build();

emailOtpRepo.deleteByEmail(normalizedEmail);
emailOtpRepo.save(otpEntity);

emailService.sendOtpEmail(normalizedEmail, otp);
    }

    /**
     * Verify the OTP provided by user
     */
    @Transactional
    public void verifyEmailOtp(String email, String otp) {

    String normalizedEmail = email.toLowerCase().trim();

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
 return String.valueOf(1000 + new SecureRandom().nextInt(9000));
}
@Transactional
public void sendRegistrationEmailOtp(String email) {
    String normalizedEmail = email.toLowerCase().trim();

    log.info("Sending registration OTP to email: {}", normalizedEmail);

    String otp = generateOtp();
    String hash = passwordEncoder.encode(otp);

    EmailOtpVerification otpEntity = EmailOtpVerification.builder()
            .email(normalizedEmail)
            .otpHash(hash)
            .expiresAt(Instant.now().plusSeconds(600))
            .verified(false)
            .attempts(0)
            .build();

    emailOtpRepo.deleteByEmail(normalizedEmail);
    emailOtpRepo.save(otpEntity);

    emailService.sendOtpEmail(normalizedEmail, otp);
}
}

