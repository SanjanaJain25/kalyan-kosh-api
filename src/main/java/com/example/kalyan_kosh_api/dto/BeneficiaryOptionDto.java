package com.example.kalyan_kosh_api.dto;

public class BeneficiaryOptionDto {
    private Long id;
    private String name;

    public BeneficiaryOptionDto(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}