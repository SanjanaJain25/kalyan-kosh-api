package com.example.kalyan_kosh_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicBinUserResponse {

    private String id;
    private String registrationNumber;
    private String name;
    private String surname;
    private String department;
    private String departmentState;
    private String departmentSambhag;
    private String departmentDistrict;
    private String departmentBlock;
    private String schoolOfficeName;
    private Long sahyogCount;
    private Double totalSahyog;
}
