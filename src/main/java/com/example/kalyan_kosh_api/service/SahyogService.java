package com.example.kalyan_kosh_api.service;

import com.example.kalyan_kosh_api.entity.ReceiptStatus;
import com.example.kalyan_kosh_api.repository.ReceiptRepository;
import org.springframework.stereotype.Service;

@Service
public class SahyogService {

    private final ReceiptRepository receiptRepo;

    public SahyogService(ReceiptRepository receiptRepo) {
        this.receiptRepo = receiptRepo;
    }

    public double calculateTotal(int month, int year) {
        Double total = receiptRepo
                .sumAmountByMonthAndYearAndStatus(month, year, ReceiptStatus.VERIFIED);
        return total == null ? 0 : total;
    }
}
