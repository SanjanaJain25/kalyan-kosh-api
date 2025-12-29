package com.example.kalyan_kosh_api.controller;

import com.example.kalyan_kosh_api.dto.ReceiptResponse;
import com.example.kalyan_kosh_api.dto.UploadReceiptRequest;
import com.example.kalyan_kosh_api.service.ReceiptService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
@CrossOrigin(origins = "*")
public class ReceiptController {

    private final ReceiptService service;

    public ReceiptController(ReceiptService service) {
        this.service = service;
    }

//    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ResponseEntity<ReceiptResponse> upload(
//            @Valid @RequestPart("data") UploadReceiptRequest req,
//            @RequestPart("file") MultipartFile file,
//            Authentication authentication
//    ) {
//        String username = authentication.getName();
//        return ResponseEntity.ok(service.upload(req, file, username));
//    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ReceiptResponse> upload(
            @RequestPart("data") String data,
            @RequestPart("file") MultipartFile file,
            Authentication authentication
    ) throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        UploadReceiptRequest req =
                mapper.readValue(data, UploadReceiptRequest.class);

        return ResponseEntity.ok(
                service.upload(req, file, authentication.getName())
        );
    }



    @GetMapping("/my")
    public ResponseEntity<?> myReceipts(Authentication authentication) {

        String username = authentication.getName();
        return ResponseEntity.ok(service.getMyReceipts(username));
    }

}
