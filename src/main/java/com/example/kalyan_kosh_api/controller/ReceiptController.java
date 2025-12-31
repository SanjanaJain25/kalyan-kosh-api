package com.example.kalyan_kosh_api.controller;

import com.example.kalyan_kosh_api.dto.ReceiptResponse;
import com.example.kalyan_kosh_api.dto.UploadReceiptRequest;
import com.example.kalyan_kosh_api.entity.Receipt;
import com.example.kalyan_kosh_api.repository.ReceiptRepository;
import com.example.kalyan_kosh_api.service.ReceiptService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/receipts")
@PreAuthorize("hasRole('USER')")
@CrossOrigin(origins = "*")
public class ReceiptController {

    private final ReceiptService service;
    private final ReceiptRepository receiptRepo;

    public ReceiptController(ReceiptService service, ReceiptRepository receiptRepo) {
        this.service = service;
        this.receiptRepo = receiptRepo;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ReceiptResponse> upload(
            @RequestPart("data") String data,
            @RequestPart("file") MultipartFile file,
            Authentication authentication
    ) throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        UploadReceiptRequest req = mapper.readValue(data, UploadReceiptRequest.class);

        return ResponseEntity.ok(
                service.upload(req, file, authentication.getName()) // authentication.getName() now returns userId
        );
    }



    @GetMapping("/my")
    public ResponseEntity<?> myReceipts(Authentication authentication) {

        String userId = authentication.getName();
        return ResponseEntity.ok(service.getMyReceipts(userId));
    }

    /**
     * Download receipt file from database
     * GET /api/receipts/{id}/download
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadReceipt(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Receipt receipt = receiptRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Receipt not found"));

        // Security: Verify user owns this receipt
        String userId = authentication.getName();
        if (!receipt.getUser().getId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(receipt.getFileType()));
        headers.setContentDisposition(
            ContentDisposition.builder("attachment")
                .filename(receipt.getFileName())
                .build()
        );

        return ResponseEntity.ok()
                .headers(headers)
                .body(receipt.getFileData());
    }

    /**
     * View receipt file in browser
     * GET /api/receipts/{id}/view
     */
    @GetMapping("/{id}/view")
    public ResponseEntity<byte[]> viewReceipt(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Receipt receipt = receiptRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Receipt not found"));

        // Security: Verify user owns this receipt
        String userId = authentication.getName();
        if (!receipt.getUser().getId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(receipt.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .body(receipt.getFileData());
    }

}
