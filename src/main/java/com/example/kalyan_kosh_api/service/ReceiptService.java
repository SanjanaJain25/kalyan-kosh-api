package com.example.kalyan_kosh_api.service;

import com.example.kalyan_kosh_api.dto.ReceiptResponse;
import com.example.kalyan_kosh_api.dto.UploadReceiptRequest;
import com.example.kalyan_kosh_api.entity.*;
import com.example.kalyan_kosh_api.repository.DeathCaseRepository;
import com.example.kalyan_kosh_api.repository.ReceiptRepository;
import com.example.kalyan_kosh_api.repository.UserRepository;
import com.example.kalyan_kosh_api.service.storage.FileStorageService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;

@Service
public class ReceiptService {

    private final ReceiptRepository receiptRepo;
    private final UserRepository userRepo;
    private final DeathCaseRepository deathCaseRepo;
    private final ModelMapper mapper;
    private final FileStorageService fileStorageService;

    public ReceiptService(
            ReceiptRepository receiptRepo,
            UserRepository userRepo,
            DeathCaseRepository deathCaseRepo,
            ModelMapper mapper,
            FileStorageService fileStorageService
    ) {
        this.receiptRepo = receiptRepo;
        this.userRepo = userRepo;
        this.deathCaseRepo = deathCaseRepo;
        this.mapper = mapper;
        this.fileStorageService = fileStorageService;
    }


    // UPLOAD RECEIPT
    public ReceiptResponse upload(
            UploadReceiptRequest req,
            MultipartFile file,
            String userId
    ) {

        // Validate user
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        // Validate death case
        DeathCase deathCase = deathCaseRepo.findById(req.getDeathCaseId())
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

        // Upload file to S3: receipts/{deathCaseId}/{userId}/filename
        String subdirectory = "receipts/" + req.getDeathCaseId() + "/" + userId;
        String customName = "receipt_" + System.currentTimeMillis();
        String fileUrl = fileStorageService.storeWithCustomName(file, subdirectory, customName);

        Receipt receipt = Receipt.builder()
                .user(user)
                .deathCase(deathCase)
                .amount(req.getAmount())
                .paymentDate(req.getPaymentDate())
                .comment(req.getComment())

                // Store S3 URL instead of file data
                .fileUrl(fileUrl)
                .fileName(file.getOriginalFilename())
                .fileType(contentType)
                .fileSize(file.getSize())

                .status(ReceiptStatus.UPLOADED)
                .uploadedAt(currentDateTime)
                .build();

        Receipt saved = receiptRepo.save(receipt);

        System.out.println("✅ File uploaded to S3: " + fileUrl);

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
