package com.example.kalyan_kosh_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountDetailsDTO {

    private Long id;
    private String bankName;
    private String accountNumber;
    private String ifscCode;
    private String accountHolderName;
}

