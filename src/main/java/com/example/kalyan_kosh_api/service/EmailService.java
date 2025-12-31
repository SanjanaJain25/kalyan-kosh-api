package com.example.kalyan_kosh_api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@kalyankosh.com}")
    private String fromEmail;

    @Value("${app.email.enabled:false}")
    private boolean emailEnabled;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Send OTP email
     * If email is disabled or fails, prints OTP to console (development mode)
     */
    public void sendOtpEmail(String toEmail, String otp) {
        // Development mode: Skip actual email sending
        if (!emailEnabled) {
            printOtpToConsole(toEmail, otp, "Email sending is disabled (app.email.enabled=false)");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Kalyan Kosh - Email Verification OTP");
            message.setText(buildOtpEmailBody(otp));

            mailSender.send(message);

            System.out.println("✅ OTP email sent successfully to: " + toEmail);
        } catch (Exception e) {
            System.err.println("❌ Failed to send email to " + toEmail);
            System.err.println("📧 Error details: " + e.getMessage());

            // Print OTP to console for development/testing
            printOtpToConsole(toEmail, otp, "Email sending failed - using console fallback");

            // Don't throw exception - allow registration to continue with console OTP
            System.out.println("⚠️ Registration will continue - OTP printed above");
        }
    }

    /**
     * Print OTP to console (development/fallback mode)
     */
    private void printOtpToConsole(String toEmail, String otp, String reason) {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("📧 EMAIL OTP (CONSOLE OUTPUT)");
        System.out.println("=".repeat(70));
        System.out.println("Reason: " + reason);
        System.out.println("To: " + toEmail);
        System.out.println("Subject: Kalyan Kosh - Email Verification OTP");
        System.out.println("-".repeat(70));
        System.out.println("OTP CODE: " + otp);
        System.out.println("-".repeat(70));
        System.out.println("⏰ Valid for: 5 minutes");
        System.out.println("=".repeat(70) + "\n");
    }

    /**
     * Build email body with OTP
     */
    private String buildOtpEmailBody(String otp) {
        return """
                Dear User,
                
                Your OTP for email verification is: %s
                
                This OTP is valid for 5 minutes only.
                
                If you did not request this OTP, please ignore this email.
                
                Regards,
                PMUIMS Kalyan Kosh Team
                """.formatted(otp);
    }
}

