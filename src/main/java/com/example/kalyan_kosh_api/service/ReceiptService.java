package com.example.kalyan_kosh_api.service;

import com.example.kalyan_kosh_api.dto.ReceiptResponse;
import com.example.kalyan_kosh_api.dto.UploadReceiptRequest;
import com.example.kalyan_kosh_api.entity.*;
import com.example.kalyan_kosh_api.repository.DeathCaseRepository;
import com.example.kalyan_kosh_api.repository.ReceiptRepository;
import com.example.kalyan_kosh_api.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class ReceiptService {

    private final ReceiptRepository receiptRepo;
    private final UserRepository userRepo;
    private final DeathCaseRepository deathCaseRepo;
    private final ModelMapper mapper;

    public ReceiptService(
            ReceiptRepository receiptRepo,
            UserRepository userRepo,
            DeathCaseRepository deathCaseRepo,
            ModelMapper mapper
    ) {
        this.receiptRepo = receiptRepo;
        this.userRepo = userRepo;
        this.deathCaseRepo = deathCaseRepo;
        this.mapper = mapper;
    }

    public ReceiptResponse upload(
            UploadReceiptRequest req,
            MultipartFile file,
            String username
    ) {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        DeathCase deathCase = deathCaseRepo.findById(req.getDeathCaseId())
                .orElseThrow(() -> new RuntimeException("Death case not found"));

        // 🔹 file storage (simple version)
        String filePath = "uploads/" + System.currentTimeMillis() + "_" + file.getOriginalFilename();

        Receipt receipt = Receipt.builder()
                .user(user)
                .deathCase(deathCase)
                .month(req.getMonth())
                .year(req.getYear())
                .amount(req.getAmount())
                .paymentDate(req.getPaymentDate())
                .transactionId(req.getTransactionId())
                .filePath(filePath)
                .status(ReceiptStatus.UPLOADED)
                .build();

        Receipt saved = receiptRepo.save(receipt);
        return mapper.map(saved, ReceiptResponse.class);
    }
    public List<ReceiptResponse> getMyReceipts(String username) {

        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return receiptRepo.findByUserOrderByUploadedAtDesc(user)
                .stream()
                .map(receipt -> {
                    ReceiptResponse resp = mapper.map(receipt, ReceiptResponse.class);
                    resp.setDeathCaseId(receipt.getDeathCase().getId());
                    resp.setDeceasedName(receipt.getDeathCase().getDeceasedName());
                    return resp;
                })
                .toList();
    }
}
