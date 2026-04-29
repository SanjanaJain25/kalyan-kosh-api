
package com.example.kalyan_kosh_api.service;

import com.example.kalyan_kosh_api.dto.ReceiptResponse;
import com.example.kalyan_kosh_api.dto.UploadReceiptRequest;
import com.example.kalyan_kosh_api.entity.*;
import com.example.kalyan_kosh_api.repository.ReceiptRepository;
import com.example.kalyan_kosh_api.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.kalyan_kosh_api.repository.DeathCaseRepository;
import com.example.kalyan_kosh_api.dto.AdminManualSahyogMoveRequest;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Service
public class ReceiptService {

    private static final Logger log = LoggerFactory.getLogger(ReceiptService.class);

    private final ReceiptRepository receiptRepo;
    private final UserRepository userRepo;
    private final ModelMapper mapper;
    private final PoolAssignmentService poolAssignmentService;
private final DeathCaseRepository deathCaseRepo;
private final AuditLogService auditLogService;
private final EmailService emailService;
   public ReceiptService(
        ReceiptRepository receiptRepo,
        UserRepository userRepo,
        ModelMapper mapper,
        PoolAssignmentService poolAssignmentService,
        DeathCaseRepository deathCaseRepo,
        AuditLogService auditLogService,
        EmailService emailService
) {
    this.receiptRepo = receiptRepo;
    this.userRepo = userRepo;
    this.mapper = mapper;
    this.poolAssignmentService = poolAssignmentService;
    this.deathCaseRepo = deathCaseRepo;
    this.auditLogService = auditLogService;
    this.emailService = emailService;
}

@Transactional
public ReceiptResponse manualMoveAsahyogToSahyog(
        AdminManualSahyogMoveRequest req,
        User adminUser,
        String ipAddress
) {
    if (adminUser.getRole() != Role.ROLE_ADMIN && adminUser.getRole() != Role.ROLE_SUPERADMIN) {
        throw new IllegalArgumentException("Only Admin/Super Admin can manually move user to Sahyog");
    }

    User targetUser = userRepo.findById(req.getUserId())
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + req.getUserId()));

    if (targetUser.getStatus() != UserStatus.ACTIVE) {
        throw new IllegalArgumentException("Only active users can be moved to Sahyog");
    }

    DeathCase deathCase = deathCaseRepo.findById(req.getDeathCaseId())
            .orElseThrow(() -> new IllegalArgumentException("Death case not found: " + req.getDeathCaseId()));

    Receipt receipt = Receipt.builder()
            .user(targetUser)
            .deathCase(deathCase)
            .amount(req.getAmount())
            .paymentDate(req.getPaymentDate())
            .referenceName(
                    req.getReferenceName() != null && !req.getReferenceName().isBlank()
                            ? req.getReferenceName().trim()
                            : "Manual Admin Entry"
            )
            .utrNumber(
                    req.getUtrNumber() != null && !req.getUtrNumber().isBlank()
                            ? req.getUtrNumber().trim()
                            : "MANUAL-" + targetUser.getId() + "-" + System.currentTimeMillis()
            )
            .status(ReceiptStatus.VERIFIED)
            .uploadedAt(Instant.now())
            .build();

    Receipt saved = receiptRepo.save(receipt);

    auditLogService.saveLog(
            DeleteEntityType.RECEIPT,
            String.valueOf(saved.getId()),
            AuditActionType.CREATE,
            null,
            "Manual Asahyog to Sahyog move. User=" + targetUser.getId()
                    + ", DeathCase=" + deathCase.getId()
                    + ", Amount=" + req.getAmount()
                    + ", PaymentDate=" + req.getPaymentDate(),
            adminUser,
            req.getRemarks() != null ? req.getRemarks() : "Manual move from Asahyog to Sahyog",
            ipAddress
    );

    ReceiptResponse resp = mapper.map(saved, ReceiptResponse.class);
    resp.setDeathCaseId(saved.getDeathCase().getId());
    resp.setDeceasedName(saved.getDeathCase().getDeceasedName());

    return resp;
}

    @Transactional
public ReceiptResponse upload(UploadReceiptRequest req, String loggedInUserId) {

    String targetUserId = req.getUserId();

    /*
     * If frontend sends selected userId, use that.
     * Otherwise fallback to logged-in user.
     */
    if (targetUserId == null || targetUserId.trim().isEmpty()) {
        targetUserId = loggedInUserId;
    }

    return createReceiptForTargetUser(req, targetUserId);
}
@Transactional
public ReceiptResponse publicUpload(UploadReceiptRequest req) {

    String targetUserId = req.getUserId();

    if (targetUserId == null || targetUserId.trim().isEmpty()) {
        if (req.getMobileNumber() == null || req.getMobileNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("User ID or mobile number is required");
        }

        User userByMobile = userRepo.findByMobileNumber(req.getMobileNumber().trim())
                .orElseThrow(() -> new IllegalArgumentException("User not found with this mobile number"));

        targetUserId = userByMobile.getId();
    }

    return createReceiptForTargetUser(req, targetUserId);
}
private ReceiptResponse createReceiptForTargetUser(UploadReceiptRequest req, String targetUserId) {

    User user = userRepo.findById(targetUserId)
            .orElseThrow(() -> new IllegalStateException("User not found: " + targetUserId));

    if (user.getStatus() != UserStatus.ACTIVE) {
        throw new IllegalStateException("Only active users can upload UTR.");
    }

    if (user.getAssignedDeathCase() == null) {
        poolAssignmentService.assignPoolToNewUser(user);
        userRepo.save(user);
    }

    if (user.getAssignedDeathCase() == null) {
        throw new IllegalStateException("No active death case pool available. Please contact admin.");
    }

    DeathCase assignedCase = user.getAssignedDeathCase();

    if (assignedCase.getStatus() != DeathCaseStatus.OPEN) {
        throw new IllegalStateException("Assigned pool is not active. Please contact admin.");
    }

    if (req.getUtrNumber() == null || req.getUtrNumber().trim().isEmpty()) {
        throw new IllegalArgumentException("UTR number is required");
    }

    LocalDate paymentDate = LocalDate.now();
    Instant now = Instant.now();

    log.info("Creating receipt for targetUser: {}, assignedDeathCaseId: {}, amount: {}",
            targetUserId, assignedCase.getId(), req.getAmount());

    Receipt receipt = Receipt.builder()
            .user(user)
            .deathCase(assignedCase)
            .amount(req.getAmount())
            .paymentDate(paymentDate)
            .referenceName(req.getReferenceName())
            .utrNumber(req.getUtrNumber().trim())
            .status(ReceiptStatus.UPLOADED)
            .uploadedAt(now)
            .build();

    Receipt saved = receiptRepo.save(receipt);

    try {
        emailService.sendReceiptUploadConfirmationEmail(user, saved);
    } catch (Exception emailError) {
        log.error("Receipt upload email failed for receipt ID: {}", saved.getId(), emailError);
    }

    ReceiptResponse resp = mapper.map(saved, ReceiptResponse.class);
    resp.setDeathCaseId(saved.getDeathCase().getId());
    resp.setDeceasedName(saved.getDeathCase().getDeceasedName());

    return resp;
}

    public List<ReceiptResponse> getMyReceipts(String userId) {

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));

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