package com.example.kalyan_kosh_api.service;

import com.example.kalyan_kosh_api.dto.ReceiptResponse;
import com.example.kalyan_kosh_api.dto.UploadReceiptRequest;
import com.example.kalyan_kosh_api.entity.*;
import com.example.kalyan_kosh_api.repository.DeathCaseRepository;
import com.example.kalyan_kosh_api.repository.ReceiptRepository;
import com.example.kalyan_kosh_api.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Service
public class ReceiptService {

    private static final Logger log = LoggerFactory.getLogger(ReceiptService.class);

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


    // UPLOAD RECEIPT (without file - just payment details)
    public ReceiptResponse upload(UploadReceiptRequest req, String userId) {

        log.info("Creating receipt for user: {}, deathCaseId: {}, amount: {}",
                 userId, req.getDeathCaseId(), req.getAmount());

        // Validate user
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found: " + userId));

        // Validate death case
        DeathCase deathCase = deathCaseRepo.findById(req.getDeathCaseId())
                .orElseThrow(() -> new IllegalStateException("Death case not found: " + req.getDeathCaseId()));

        // Validate UTR number is provided
        if (req.getUtrNumber() == null || req.getUtrNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("UTR number is required");
        }

        // Auto-set payment date to current date
        LocalDate paymentDate = LocalDate.now();
        Instant currentDateTime = Instant.now();

        Receipt receipt = Receipt.builder()
                .user(user)
                .deathCase(deathCase)
                .amount(req.getAmount())
                .paymentDate(paymentDate)  // Auto-set to current date
                .referenceName(req.getReferenceName())
                .utrNumber(req.getUtrNumber())
                .status(ReceiptStatus.UPLOADED)
                .uploadedAt(currentDateTime)
                .build();

        Receipt saved = receiptRepo.save(receipt);
        log.info("Receipt created successfully with ID: {}, paymentDate: {}", saved.getId(), paymentDate);

        return mapper.map(saved, ReceiptResponse.class);
    }


    // USER RECEIPT HISTORY
    public List<ReceiptResponse> getMyReceipts(String userId) {

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        return receiptRepo.findByUserOrderByUploadedAtDesc(user)
                .stream()
                .map(receipt -> {
                    ReceiptResponse resp =
                            mapper.map(receipt, ReceiptResponse.class);
                    resp.setDeathCaseId(receipt.getDeathCase().getId());
                    resp.setDeceasedName(
                            receipt.getDeathCase().getDeceasedName());
                    return resp;
                })
                .toList();
    }
}
