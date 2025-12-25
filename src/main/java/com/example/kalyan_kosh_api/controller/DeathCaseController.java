package com.example.kalyan_kosh_api.controller;

import com.example.kalyan_kosh_api.dto.CreateDeathCaseRequest;
import com.example.kalyan_kosh_api.dto.UpdateDeathCaseRequest;
import com.example.kalyan_kosh_api.dto.DeathCaseResponse;
import com.example.kalyan_kosh_api.service.DeathCaseService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/death-cases")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class DeathCaseController {

    private final DeathCaseService service;

    public DeathCaseController(DeathCaseService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<DeathCaseResponse> create(
            @Valid @RequestBody CreateDeathCaseRequest req,
            Authentication authentication) {

        return ResponseEntity.ok(
                service.create(req, authentication.getName())
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

    @PutMapping("/{id}")
    public ResponseEntity<DeathCaseResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDeathCaseRequest req,
            Authentication authentication) {

        return ResponseEntity.ok(
                service.update(id, req, authentication.getName())
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
