package com.example.kalyan_kosh_api.controller;

import com.example.kalyan_kosh_api.dto.CreateDeathCaseRequest;
import com.example.kalyan_kosh_api.dto.UpdateDeathCaseRequest;
import com.example.kalyan_kosh_api.dto.DeathCaseResponse;
import com.example.kalyan_kosh_api.service.DeathCaseService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/death-cases")
//@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@CrossOrigin(origins = "*")
public class DeathCaseController {

    private final DeathCaseService service;

    public DeathCaseController(DeathCaseService service) {
        this.service = service;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DeathCaseResponse> create(
            @Valid @RequestPart("data") CreateDeathCaseRequest req,
            @RequestPart(value = "userImage", required = false) MultipartFile userImage,
            @RequestPart(value = "nominee1QrCode", required = false) MultipartFile nominee1QrCode,
            @RequestPart(value = "nominee2QrCode", required = false) MultipartFile nominee2QrCode,
            Authentication authentication) {

        return ResponseEntity.ok(
                service.create(req, userImage, nominee1QrCode, nominee2QrCode, authentication.getName())
        );
    }

    @GetMapping
    public ResponseEntity<List<DeathCaseResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeathCaseResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DeathCaseResponse> update(
            @PathVariable Long id,
            @Valid @RequestPart("data") UpdateDeathCaseRequest req,
            @RequestPart(value = "userImage", required = false) MultipartFile userImage,
            @RequestPart(value = "nominee1QrCode", required = false) MultipartFile nominee1QrCode,
            @RequestPart(value = "nominee2QrCode", required = false) MultipartFile nominee2QrCode,
            Authentication authentication) {

        return ResponseEntity.ok(
                service.update(id, req, userImage, nominee1QrCode, nominee2QrCode, authentication.getName())
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
