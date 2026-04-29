package com.example.kalyan_kosh_api.controller;

import com.example.kalyan_kosh_api.dto.ReceiptResponse;
import com.example.kalyan_kosh_api.dto.UploadReceiptRequest;
import com.example.kalyan_kosh_api.entity.Receipt;
import com.example.kalyan_kosh_api.repository.ReceiptRepository;
import com.example.kalyan_kosh_api.service.ReceiptService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// @RestController
// @RequestMapping("/api/receipts")
// @CrossOrigin(origins = "*")
// public class ReceiptController {

//     private final ReceiptService service;
//     private final ReceiptRepository receiptRepo;

//     public ReceiptController(ReceiptService service, ReceiptRepository receiptRepo) {
//         this.service = service;
//         this.receiptRepo = receiptRepo;
//     }

//     /**
//      * Create a new receipt (payment record)
//      * POST /api/receipts
//      * Body: { deathCaseId, amount, paymentDate, referenceName, utrNumber }
//      */
//     @PostMapping
//     public ResponseEntity<ReceiptResponse> upload(
//             @Valid @RequestBody UploadReceiptRequest req,
//             Authentication authentication
//     ) {
//         return ResponseEntity.ok(
//                 service.upload(req, authentication.getName())
//         );
//     }

//     /**
//      * Get user's receipt history
//      * GET /api/receipts/my
//      */
//     @GetMapping("/my")
//     public ResponseEntity<?> myReceipts(Authentication authentication) {
//         String userId = authentication.getName();
//         return ResponseEntity.ok(service.getMyReceipts(userId));
//     }

//     /**
//      * Get receipt details by ID
//      * GET /api/receipts/{id}
//      */
//     @GetMapping("/{id}")
//     public ResponseEntity<?> getReceipt(
//             @PathVariable Long id,
//             Authentication authentication
//     ) {
//         Receipt receipt = receiptRepo.findById(id)
//                 .orElseThrow(() -> new RuntimeException("Receipt not found"));

//         // Security: Verify user owns this receipt or is admin
//         String userId = authentication.getName();
//         boolean isAdmin = authentication.getAuthorities().stream()
//                 .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

//         if (!isAdmin && !receipt.getUser().getId().equals(userId)) {
//             throw new RuntimeException("Access denied");
//         }

//         return ResponseEntity.ok(Map.of(
//                 "id", receipt.getId(),
//                 "amount", receipt.getAmount(),
//                 "paymentDate", receipt.getPaymentDate(),
//                 "referenceName", receipt.getReferenceName() != null ? receipt.getReferenceName() : "",
//                 "utrNumber", receipt.getUtrNumber() != null ? receipt.getUtrNumber() : "",
//                 "status", receipt.getStatus(),
//                 "uploadedAt", receipt.getUploadedAt()
//         ));
//     }

// }

@RestController
@RequestMapping("/api/receipts")
public class ReceiptController {

    private final ReceiptService service;
    private final ReceiptRepository receiptRepo;

    public ReceiptController(ReceiptService service, ReceiptRepository receiptRepo) {
        this.service = service;
        this.receiptRepo = receiptRepo;
    }

    /**
     * Create a new receipt (payment record)
     * POST /api/receipts
     * Body: { amount, paymentDate(auto), referenceName, utrNumber }
     *
     *  deathCase is auto-attached from user's assigned pool
     */
    @PostMapping
public ResponseEntity<ReceiptResponse> upload(
        @Valid @RequestBody UploadReceiptRequest req,
        Authentication authentication
) {
    return ResponseEntity.ok(service.upload(req, authentication.getName()));
}
@PostMapping("/public-upload")
public ResponseEntity<ReceiptResponse> publicUpload(
        @Valid @RequestBody UploadReceiptRequest req
) {
    return ResponseEntity.ok(service.publicUpload(req));
}
    @GetMapping("/my")
    public ResponseEntity<?> myReceipts(Authentication authentication) {
        return ResponseEntity.ok(service.getMyReceipts(authentication.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getReceipt(@PathVariable Long id, Authentication authentication) {
        Receipt receipt = receiptRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Receipt not found"));

        String userId = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !receipt.getUser().getId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }

        return ResponseEntity.ok(Map.of(
                "id", receipt.getId(),
                "amount", receipt.getAmount(),
                "paymentDate", receipt.getPaymentDate(),
                "referenceName", receipt.getReferenceName() != null ? receipt.getReferenceName() : "",
                "utrNumber", receipt.getUtrNumber() != null ? receipt.getUtrNumber() : "",
                "status", receipt.getStatus(),
                "uploadedAt", receipt.getUploadedAt(),
                "deathCaseId", receipt.getDeathCase().getId() //  Include associated death case ID for reference
        ));
    }
}