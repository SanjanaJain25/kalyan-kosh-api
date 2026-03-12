package com.example.kalyan_kosh_api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@Profile("authkey")
//@Profile("authkey")

public class AuthkeySmsService {

    private static final String AUTHKEY_URL = "https://api.authkey.io/request";

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${authkey.api-key}")
    private String authkey;

    @Value("${authkey.sender}")
    private String sender;

    public void sendOtp(String mobile, String otp) {

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("authkey", authkey);
        body.add("mobiles", mobile); // 91XXXXXXXXXX
        body.add("message",
                "Your OTP is " + otp + ". Valid for 5 minutes. – " + sender);
        body.add("sender", sender);
        body.add("route", "4");
        body.add("country", "91");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request =
                new HttpEntity<>(body, headers);

        restTemplate.postForObject(AUTHKEY_URL, request, String.class);
    }
}
