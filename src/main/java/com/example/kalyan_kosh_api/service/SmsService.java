package com.example.kalyan_kosh_api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;

@Service
public class SmsService {

    private static final Logger log = LoggerFactory.getLogger(SmsService.class);

    private final RestTemplate restTemplate = new RestTemplate();

    private final String baseUrl = "http://voice.roundsms.co/api/sendmsg.php";
    private final String user = "SSC_SMS";
    private final String pass = "123456";
    private final String sender = "PMUMSS";
    private final String priority = "ndnd";
    private final String stype = "normal";

   public String sendOtpSms(String phone, String otp) {

    String text = "Dear User Your OTP is " + otp +
            " for registration. Valid for 10 minutes. " +
            "Team PRATHMIK MADHYAMIK UCHHATAR MADHYAMIK SHIKSHAK SANGH";

    String url = UriComponentsBuilder.fromHttpUrl(baseUrl)
            .queryParam("user", user)
            .queryParam("pass", pass)
            .queryParam("sender", sender)
            .queryParam("phone", phone)
            .queryParam("text", text)
            .queryParam("priority", priority)
            .queryParam("stype", stype)
            .encode(StandardCharsets.UTF_8)
            .toUriString();

    log.info("📲 RoundSMS URL (masked): {}", url.replace(pass, "****"));

    String response = restTemplate.getForObject(url, String.class);
    log.info("📩 RoundSMS response for {} => {}", phone, response);

    if (response == null || response.trim().isEmpty()) {
        throw new RuntimeException("SMS gateway returned empty response");
    }

    String r = response.trim();

    // ✅ RoundSMS success pattern => "S.12345" OR "S.123 S.456"
    boolean success = r.matches("(?i)^(s\\.[0-9]+)(\\s+s\\.[0-9]+)*$");

    if (!success) {
        // anything else is failure
        throw new RuntimeException("SMS gateway failed: " + response);
    }

    return response;
}
}