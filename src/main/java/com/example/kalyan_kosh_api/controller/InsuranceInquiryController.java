package com.example.kalyan_kosh_api.controller;

import com.example.kalyan_kosh_api.dto.InsuranceInquiryRequest;
import com.example.kalyan_kosh_api.service.InsuranceInquiryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/public")
public class InsuranceInquiryController {

    private final InsuranceInquiryService insuranceInquiryService;

    public InsuranceInquiryController(InsuranceInquiryService insuranceInquiryService) {
        this.insuranceInquiryService = insuranceInquiryService;
    }

    @PostMapping("/insurance-inquiries")
    public ResponseEntity<Map<String, Object>> createInsuranceInquiry(
            @Valid @RequestBody InsuranceInquiryRequest request
    ) {
        return ResponseEntity.ok(insuranceInquiryService.saveInquiry(request));
    }
}