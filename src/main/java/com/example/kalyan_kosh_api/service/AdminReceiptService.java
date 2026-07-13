package com.example.kalyan_kosh_api.service;

import com.example.kalyan_kosh_api.dto.AdminReceiptResponse;
import com.example.kalyan_kosh_api.dto.DonorResponse;
import com.example.kalyan_kosh_api.entity.Receipt;
import com.example.kalyan_kosh_api.entity.ReceiptStatus;
import com.example.kalyan_kosh_api.repository.ReceiptRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class AdminReceiptService {

    private final ReceiptRepository receiptRepo;

    public AdminReceiptService(ReceiptRepository receiptRepo) {
        this.receiptRepo = receiptRepo;
    }

    public List<AdminReceiptResponse> list(int month, int year) {
        return List.of();
    }

    /**
     * Returns paginated receipt records.
     *
     * When utrNumber is empty or null, all receipts are returned.
     *
     * When utrNumber contains a value, only matching receipt
     * records are returned.
     */
    public Page<AdminReceiptResponse> getAllReceipts(
            String utrNumber,
            Pageable pageable
    ) {

        String normalizedUtrNumber =
                normalizeUtrSearchValue(utrNumber);

        return receiptRepo
                .searchAdminReceipts(
                        normalizedUtrNumber,
                        pageable
                )
                .map(AdminReceiptResponse::from);
    }

    /**
     * Returns all receipt records for Excel export.
     *
     * When a UTR number is supplied, only filtered receipt
     * records are exported.
     */
    public List<AdminReceiptResponse> getAllReceiptsForExport(
            String utrNumber
    ) {

        String normalizedUtrNumber =
                normalizeUtrSearchValue(utrNumber);

        return receiptRepo
                .searchAdminReceipts(
                        normalizedUtrNumber,
                        Pageable.unpaged()
                )
                .getContent()
                .stream()
                .map(AdminReceiptResponse::from)
                .toList();
    }

    /**
     * Removes unnecessary spaces and makes the UTR
     * case-insensitive.
     *
     * Empty values are converted to null so that the repository
     * returns every receipt.
     */
    private String normalizeUtrSearchValue(String utrNumber) {

        if (utrNumber == null) {
            return null;
        }

        String normalizedUtrNumber =
                utrNumber.trim().toUpperCase(Locale.ROOT);

        if (normalizedUtrNumber.isEmpty()) {
            return null;
        }

        return normalizedUtrNumber;
    }

    public void verify(Long id) {

        Receipt receipt = receiptRepo
                .findById(id)
                .orElseThrow(
                        () -> new RuntimeException("Receipt not found")
                );

        receipt.setStatus(ReceiptStatus.VERIFIED);

        receiptRepo.save(receipt);
    }

    public void reject(Long id) {

        Receipt receipt = receiptRepo
                .findById(id)
                .orElseThrow(
                        () -> new RuntimeException("Receipt not found")
                );

        receipt.setStatus(ReceiptStatus.REJECTED);

        receiptRepo.save(receipt);
    }

    public List<DonorResponse> getDonors(
            int month,
            int year
    ) {
        return List.of();
    }
}