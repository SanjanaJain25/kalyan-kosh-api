package com.example.kalyan_kosh_api.service;

import com.example.kalyan_kosh_api.entity.OtpVerification;
import com.example.kalyan_kosh_api.repository.OtpVerificationRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.kalyan_kosh_api.repository.UserRepository;
import java.security.SecureRandom;
import java.time.Instant;

@Service
public class OtpService {

    private final OtpVerificationRepository otpRepo;
    private final PasswordEncoder passwordEncoder;
private final SmsService smsService;
private final UserRepository userRepository;
public OtpService(OtpVerificationRepository otpRepo,
                  PasswordEncoder passwordEncoder,
                  SmsService smsService,
                  UserRepository userRepository) {
    this.otpRepo = otpRepo;
    this.passwordEncoder = passwordEncoder;
    this.smsService = smsService;
    this.userRepository = userRepository;
}


   @Transactional
public void sendOtp(String mobile) {
    Instant now = Instant.now();

    // 1. Check mobile exists in registered users
    userRepository.findByMobileNumber(mobile)
            .orElseThrow(() -> new IllegalArgumentException("Mobile number is not registered"));

    // 2. Get existing OTP record if any
    OtpVerification otpEntity = otpRepo.findByMobile(mobile).orElse(null);

    if (otpEntity != null) {
        // 3. Check if blocked
        if (otpEntity.getBlockedUntil() != null && otpEntity.getBlockedUntil().isAfter(now)) {
            throw new IllegalArgumentException("OTP sending is blocked for this mobile number for 72 hours");
        }

        // 4. If already sent 2 times, block for 72 hours
        if (otpEntity.getSendAttempts() >= 2) {
            otpEntity.setBlockedUntil(now.plusSeconds(72 * 60 * 60)); // 72 hours
            otpRepo.save(otpEntity);
            throw new IllegalArgumentException("OTP sending is blocked for this mobile number for 72 hours");
        }
    } else {
        otpEntity = new OtpVerification();
        otpEntity.setMobile(mobile);
        otpEntity.setSendAttempts(0);
        otpEntity.setAttempts(0);
        otpEntity.setVerified(false);
    }

    // 5. Generate and save new OTP
    String otp = generateOtp();
    String hash = passwordEncoder.encode(otp);

    otpEntity.setOtpHash(hash);
    otpEntity.setExpiresAt(now.plusSeconds(300)); // 5 min
    otpEntity.setVerified(false);
    otpEntity.setAttempts(0);
    otpEntity.setLastSentAt(now);
    otpEntity.setSendAttempts(otpEntity.getSendAttempts() + 1);

    otpRepo.save(otpEntity);

    // 6. Send SMS
    smsService.sendOtpSms(mobile, otp);
}

    @Transactional
    public void verifyOtp(String mobile, String otp) {

        OtpVerification savedOtp = otpRepo
                .findByMobileAndExpiresAtAfter(mobile, Instant.now())
                .orElseThrow(() ->
                        new RuntimeException("OTP expired or not found"));

        if (!passwordEncoder.matches(otp, savedOtp.getOtpHash())) {
            throw new RuntimeException("Invalid OTP");
        }

        otpRepo.deleteByMobile(mobile); // single use
    }


    private String generateOtp() {
    return String.valueOf(1000 + new SecureRandom().nextInt(9000));
}
}
