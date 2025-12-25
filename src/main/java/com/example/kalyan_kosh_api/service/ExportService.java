package com.example.kalyan_kosh_api.service;

import com.example.kalyan_kosh_api.dto.AdminReceiptResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExportService {

    public String exportCsv(List<AdminReceiptResponse> data) {

        StringBuilder sb = new StringBuilder();
        sb.append("RegNo,Name,District,Block,Department,PaymentDate,Amount\n");

        for (AdminReceiptResponse r : data) {
            sb.append(r.getRegNo()).append(",")
                    .append(r.getName()).append(",")
                    .append(r.getDistrict()).append(",")
                    .append(r.getBlock()).append(",")
                    .append(r.getDepartment()).append(",")
                    .append(r.getPaymentDate()).append(",")
                    .append(r.getAmount()).append("\n");
        }
        return sb.toString();
    }
}
