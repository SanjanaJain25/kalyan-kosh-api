package com.example.kalyan_kosh_api.dto;

import com.example.kalyan_kosh_api.entity.Receipt;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AdminReceiptResponse {

    private String regNo;
    private String name;
    private String sambhag;
    private String district;
    private String block;
    private String department;

    private LocalDate paymentDate;
    private double amount;

    public static AdminReceiptResponse from(Receipt r) {
        AdminReceiptResponse dto = new AdminReceiptResponse();
        dto.setRegNo(r.getUser().getUsername());
        dto.setName(r.getUser().getName());
        dto.setDistrict(r.getUser().getDepartmentDistrict());
        dto.setBlock(r.getUser().getDepartmentBlock());
        dto.setDepartment(r.getUser().getDepartment());
        dto.setPaymentDate(r.getPaymentDate());
        dto.setAmount(r.getAmount());
        return dto;
    }
}
