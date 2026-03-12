package com.example.kalyan_kosh_api.service;

import com.example.kalyan_kosh_api.dto.InsuranceInquiryRequest;
import com.example.kalyan_kosh_api.entity.InsuranceInquiry;
import com.example.kalyan_kosh_api.repository.InsuranceInquiryRepository;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class InsuranceInquiryService {

    private final InsuranceInquiryRepository insuranceInquiryRepository;

    public InsuranceInquiryService(InsuranceInquiryRepository insuranceInquiryRepository) {
        this.insuranceInquiryRepository = insuranceInquiryRepository;
    }

    public Map<String, Object> saveInquiry(InsuranceInquiryRequest request) {
        InsuranceInquiry inquiry = new InsuranceInquiry();
        inquiry.setName(request.getName().trim());
        inquiry.setDistrict(request.getDistrict().trim());
        inquiry.setMobileNumber(request.getMobileNumber().trim());

        InsuranceInquiry saved = insuranceInquiryRepository.save(inquiry);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "हमारी टीम जल्द ही आपसे सम्पर्क करेगी।");
        response.put("id", saved.getId());

        return response;
    }
}