package com.example.kalyan_kosh_api.service;

import com.example.kalyan_kosh_api.entity.OtpVerification;
import com.example.kalyan_kosh_api.repository.OtpVerificationRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;

@Service
public class OtpService {

    private final OtpVerificationRepository otpRepo;
    private final PasswordEncoder passwordEncoder;

    public OtpService(OtpVerificationRepository otpRepo,
                      PasswordEncoder passwordEncoder) {
        this.otpRepo = otpRepo;
        this.passwordEncoder = passwordEncoder;
    }


    @Transactional
    public void sendOtp(String mobile) {

        String otp = generateOtp();
        String hash = passwordEncoder.encode(otp);

        OtpVerification otpEntity = OtpVerification.builder()
                .mobile(mobile)
                .otpHash(hash)
                .expiresAt(Instant.now().plusSeconds(300)) // 5 min
                .build();

        otpRepo.deleteByMobile(mobile); // remove old OTP
        otpRepo.save(otpEntity);


        // TODO: call Authkey here
        System.out.println("OTP (DEV ONLY): " + otp);
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
        return String.valueOf(
                100000 + new SecureRandom().nextInt(900000)
        );
    }
}
