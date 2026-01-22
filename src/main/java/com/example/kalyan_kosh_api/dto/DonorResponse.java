package com.example.kalyan_kosh_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO for Donor list response
 * Fields:
 * - पंजीकरण संख्या (Registration Number / Department Unique ID)
 * - नाम (Name)
 * - विभाग (Department)
 * - राज्य (State)
 * - संभाग (Sambhag)
 * - जिला (District)
 * - ब्लॉक (Block)
 * - स्कूल का नाम (School Name)
 * - लाभार्थी (Beneficiary - Death Case person name)
 * - रसीद अपलोड दिनांक (Receipt Upload Date)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DonorResponse {

    private String registrationNumber;      // पंजीकरण संख्या (departmentUniqueId)
    private String name;                    // नाम
    private String department;              // विभाग
    private String state;                   // राज्य
    private String sambhag;                 // संभाग
    private String district;                // जिला
    private String block;                   // ब्लॉक
    private String schoolName;              // स्कूल का नाम
    private String beneficiary;             // लाभार्थी (deceased person name from DeathCase)
    private Instant receiptUploadDate;      // रसीद अपलोड दिनांक
}

