package com.example.kalyan_kosh_api.repository;

import com.example.kalyan_kosh_api.entity.InsuranceInquiry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InsuranceInquiryRepository extends JpaRepository<InsuranceInquiry, Long> {
    List<InsuranceInquiry> findAllByOrderByCreatedAtDesc();
}