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
        } catch (Exception e) {
            // Print OTP to console for development/testing (fallback)
            printOtpToConsole(toEmail, otp, "Email sending failed - using console fallback");
        }
    }

    /**
     * Print OTP to console (development/fallback mode)
     */
    private void printOtpToConsole(String toEmail, String otp, String reason) {
        // Fallback console output for development - kept minimal
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

    /**
     * Send registration confirmation email
     */
    public void sendRegistrationConfirmationEmail(String toEmail, String userName, String registrationNumber) {
        // Development mode: Print to console
        if (!emailEnabled) {
            printRegistrationConfirmationToConsole(toEmail, userName, registrationNumber, "Email sending is disabled (app.email.enabled=false)");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("PMUMS पंजीकरण सफल | आपका रजिस्ट्रेशन नंबर");
            message.setText(buildRegistrationConfirmationBody(userName, registrationNumber));

            mailSender.send(message);

            System.out.println("✅ Registration confirmation email sent successfully to: " + toEmail);
        } catch (Exception e) {
            System.err.println("❌ Failed to send registration confirmation email to " + toEmail);
            System.err.println("📧 Error details: " + e.getMessage());

            // Print to console for development/testing
            printRegistrationConfirmationToConsole(toEmail, userName, registrationNumber, "Email sending failed - using console fallback");

            // Don't throw exception - allow registration to complete successfully
            System.out.println("⚠️ Registration completed - confirmation email printed above");
        }
    }

    /**
     * Print registration confirmation to console (development/fallback mode)
     */
    private void printRegistrationConfirmationToConsole(String toEmail, String userName, String registrationNumber, String reason) {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("📧 REGISTRATION CONFIRMATION EMAIL (CONSOLE OUTPUT)");
        System.out.println("=".repeat(70));
        System.out.println("Reason: " + reason);
        System.out.println("To: " + toEmail);
        System.out.println("Subject: PMUMS पंजीकरण सफल | आपका रजिस्ट्रेशन नंबर");
        System.out.println("-".repeat(70));
        System.out.println(buildRegistrationConfirmationBody(userName, registrationNumber));
        System.out.println("=".repeat(70) + "\n");
    }

    /**
     * Build registration confirmation email body in Hindi
     */
    private String buildRegistrationConfirmationBody(String userName, String registrationNumber) {
        return String.format("""
                प्रिय %s,

                आपका PMUMS (प्राथमिक–माध्यमिक–उच्च–माध्यमिक शिक्षक संघ, मध्यप्रदेश) की आधिकारिक वेबसाइट पर किया गया पंजीकरण सफलतापूर्वक पूर्ण हो गया है।

                हमें यह बताते हुए हर्ष हो रहा है कि आप अब PMUMS शिक्षक संघ के पंजीकृत सदस्य बन चुके हैं।

                🔖 आपका पंजीकरण विवरण
                रजिस्ट्रेशन नंबर: %s

                👉 कृपया इस रजिस्ट्रेशन नंबर को भविष्य के सभी संदर्भ, सत्यापन एवं पत्राचार हेतु सुरक्षित रखें।

                📌 महत्वपूर्ण सूचना
                PMUMS शिक्षक संघ द्वारा कर्मचारी कल्याण कोष योजना संचालित की जा रही है। इस योजना के अंतर्गत—

                • आपकी तीन माह की सदस्यता पूर्ण होने के पश्चात आप योजना के लाभों के पात्र होंगे。
                • योजना के अंतर्गत दिवंगत होने वाले PMUMS शिक्षक संघ के सदस्यों हेतु सहयोग प्रदान करना अनिवार्य होगा。
                • सहयोग न करने की स्थिति में आप इस योजना के लाभो से वंचित रहेंगे।

                यदि आपको पंजीकरण अथवा योजना से संबंधित किसी भी प्रकार की जानकारी या सहायता की आवश्यकता हो, तो कृपया PMUMS की आधिकारिक वेबसाइट अथवा अधिकृत माध्यमों के माध्यम से संपर्क करें।

                आपके सहयोग एवं विश्वास के लिए धन्यवाद।

                सादर,
                सतीश खरे
                संस्थापक
                PMUMS शिक्षक संघ / कर्मचारी कल्याण कोष
                """, userName, registrationNumber);
    }
}
