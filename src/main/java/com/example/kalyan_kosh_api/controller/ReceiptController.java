package com.example.kalyan_kosh_api.controller;

import com.example.kalyan_kosh_api.dto.ReceiptResponse;
import com.example.kalyan_kosh_api.dto.UploadReceiptRequest;
import com.example.kalyan_kosh_api.service.ReceiptService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/receipts")
@PreAuthorize("hasRole('USER')")
public class ReceiptController {

    private final ReceiptService service;

    public ReceiptController(ReceiptService service) {
        this.service = service;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ReceiptResponse> upload(
            @Valid @RequestPart("data") UploadReceiptRequest req,
            @RequestPart("file") MultipartFile file,
            Authentication authentication
    ) {
        String username = authentication.getName();
        return ResponseEntity.ok(service.upload(req, file, username));
    }

    @GetMapping("/my")
    public ResponseEntity<?> myReceipts(Authentication authentication) {

        String username = authentication.getName();
        return ResponseEntity.ok(service.getMyReceipts(username));
    }

}
