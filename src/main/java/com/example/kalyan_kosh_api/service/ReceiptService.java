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

import java.io.IOException;
import java.time.Instant;
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


    // UPLOAD RECEIPT
    public ReceiptResponse upload(
            UploadReceiptRequest req,
            MultipartFile file,
            String username
    ) {

        // Validate user
        User user = userRepo.findById(username)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        // Validate death case
        DeathCase deathCase = deathCaseRepo.findById(31L)
                .orElseThrow(() -> new IllegalStateException("Death case not found"));

        // Validate file size (10MB limit)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("File size exceeds 10MB limit");
        }

        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null ||
            (!contentType.startsWith("image/") && !contentType.equals("application/pdf"))) {
            throw new IllegalArgumentException("Only images and PDFs are allowed");
        }

        // Save current date and time
        Instant currentDateTime = Instant.now();

        try {
            // ✅ Convert file to byte array for database storage
            byte[] fileData = file.getBytes();

            Receipt receipt = Receipt.builder()
                    .user(user)
                    .deathCase(deathCase)
                    .amount(req.getAmount())
                    .paymentDate(req.getPaymentDate())
                    .comment(req.getComment())

                    // ✅ Store file in database
                    .fileData(fileData)
                    .fileName(file.getOriginalFilename())
                    .fileType(contentType)
                    .fileSize(file.getSize())

                    .status(ReceiptStatus.UPLOADED)
                    .uploadedAt(currentDateTime)
                    .build();

            Receipt saved = receiptRepo.save(receipt);

            System.out.println("✅ File saved to database: " + file.getOriginalFilename() +
                             " (" + file.getSize() + " bytes)");

            return mapper.map(saved, ReceiptResponse.class);

        } catch (IOException e) {
            throw new RuntimeException("Failed to read file data: " + e.getMessage(), e);
        }
    }


    // USER RECEIPT HISTORY
    public List<ReceiptResponse> getMyReceipts(String username) {

        User user = userRepo.findByUsername(username)
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
