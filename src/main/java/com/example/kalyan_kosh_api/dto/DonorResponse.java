package com.example.kalyan_kosh_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DonorResponse {

    private Long receiptId;                 // Required for edit/delete action
private Long deathCaseId;
    private String registrationNumber;      // पंजीकरण संख्या
    private String name;                    // नाम
    private String department;              // विभाग
    private String state;                   // राज्य
    private String sambhag;                 // संभाग
    private String district;                // जिला
    private String block;                   // ब्लॉक
    private String schoolName;              // स्कूल का नाम
    private String beneficiary;             // लाभार्थी
  private LocalDateTime receiptUploadDate;     // रसीद अपलोड दिनांक

    private Double amount;                  // सहयोग राशि
    private LocalDate paymentDate;          // भुगतान दिनांक
    private String referenceName;           // Reference Name
    private String utrNumber;               // UTR Number
}