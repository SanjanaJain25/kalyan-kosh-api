package com.example.kalyan_kosh_api.service;

import com.example.kalyan_kosh_api.dto.AdminReceiptResponse;
import com.example.kalyan_kosh_api.dto.DonorResponse;
import com.example.kalyan_kosh_api.entity.Receipt;
import com.example.kalyan_kosh_api.entity.ReceiptStatus;
import com.example.kalyan_kosh_api.entity.User;
import com.example.kalyan_kosh_api.repository.ReceiptRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminReceiptService {

    private final ReceiptRepository receiptRepo;

    public AdminReceiptService(ReceiptRepository receiptRepo) {
        this.receiptRepo = receiptRepo;
    }

    public List<AdminReceiptResponse> list(int month, int year) {
//        return receiptRepo.findByMonthAndYear(month, year)
//                .stream()
//                .map(r -> AdminReceiptResponse.from(r))
//                .toList();
        return List.of();
    }
    
    /**
     * Get all receipts without any date filters with pagination support
     */
    public Page<AdminReceiptResponse> getAllReceipts(Pageable pageable) {
        return receiptRepo.findAll(pageable)
                .map(AdminReceiptResponse::from);
    }
    
    /**
     * Get all receipts for export (without pagination)
     */
    public List<AdminReceiptResponse> getAllReceiptsForExport() {
        return receiptRepo.findAll()
                .stream()
                .map(AdminReceiptResponse::from)
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

    public List<DonorResponse> getDonors(int month, int year) {
return List.of();
//        return receiptRepo.findVerifiedReceipts(month, year)
//                .stream()
//                .map(r -> {
//                    User u = r.getUser();
//
//                    String sambhagName = u.getDepartmentSambhag() != null
//                            ? u.getDepartmentSambhag().getName() : null;
//                    String districtName = u.getDepartmentDistrict() != null
//                            ? u.getDepartmentDistrict().getName() : null;
//                    String blockName = u.getDepartmentBlock() != null
//                            ? u.getDepartmentBlock().getName() : null;
//
//                    return new DonorResponse(
//                            u.getId(),
//                            u.getUsername(),
//                            u.getName(),
//                            sambhagName,   // sambhag
//                            districtName,  // district
//                            blockName,     // block
//                            u.getDepartment(),
//                            null,
//                            0
//                    );
//                })
//                .toList();
    }

}
