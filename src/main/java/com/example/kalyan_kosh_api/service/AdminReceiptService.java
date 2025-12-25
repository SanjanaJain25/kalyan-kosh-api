package com.example.kalyan_kosh_api.service;

import com.example.kalyan_kosh_api.dto.AdminReceiptResponse;
import com.example.kalyan_kosh_api.entity.Receipt;
import com.example.kalyan_kosh_api.entity.ReceiptStatus;
import com.example.kalyan_kosh_api.repository.ReceiptRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminReceiptService {

    private final ReceiptRepository receiptRepo;

    public AdminReceiptService(ReceiptRepository receiptRepo) {
        this.receiptRepo = receiptRepo;
    }

    public List<AdminReceiptResponse> list(int month, int year) {
        return receiptRepo.findByMonthAndYear(month, year)
                .stream()
                .map(r -> AdminReceiptResponse.from(r))
                .toList();
    }

    public void verify(Long id) {
        Receipt r = receiptRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Receipt not found"));
        r.setStatus(ReceiptStatus.VERIFIED);
        receiptRepo.save(r);
    }

    public void reject(Long id) {
        Receipt r = receiptRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Receipt not found"));
        r.setStatus(ReceiptStatus.REJECTED);
        receiptRepo.save(r);
    }
}
