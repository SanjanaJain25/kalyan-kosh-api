package com.example.kalyan_kosh_api.service;

import com.example.kalyan_kosh_api.dto.AccountDetailsDTO;
import com.example.kalyan_kosh_api.dto.CreateDeathCaseRequest;
import com.example.kalyan_kosh_api.dto.UpdateDeathCaseRequest;
import com.example.kalyan_kosh_api.dto.DeathCaseResponse;
import com.example.kalyan_kosh_api.entity.AccountDetails;
import com.example.kalyan_kosh_api.entity.DeathCase;
import com.example.kalyan_kosh_api.entity.DeathCaseStatus;
import com.example.kalyan_kosh_api.repository.DeathCaseRepository;
import com.example.kalyan_kosh_api.service.storage.FileStorageService;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class DeathCaseService {

    private static final Logger log = LoggerFactory.getLogger(DeathCaseService.class);

    private final DeathCaseRepository repository;
    private final ModelMapper mapper;
    private final FileStorageService fileStorageService;

    public DeathCaseService(DeathCaseRepository repository, ModelMapper mapper, FileStorageService fileStorageService) {
        this.repository = repository;
        this.mapper = mapper;
        this.fileStorageService = fileStorageService;
    }

    public DeathCaseResponse create(CreateDeathCaseRequest req,
                                     MultipartFile userImageFile,
                                     MultipartFile nominee1QrCodeFile,
                                     MultipartFile nominee2QrCodeFile,
                                     MultipartFile certificate1File,
                                     String userId) {

        log.info("Creating death case - DeceasedName: {}, EmployeeCode: {}, UserId: {}",
                 req.getDeceasedName(), req.getEmployeeCode(), userId);

        try {
            // Store files with meaningful names (organized by userId)
            String userImagePath = storeFileWithName(userImageFile, userId, "death-cases",
                    sanitizeName(req.getDeceasedName()) + "_user");
            String nominee1QrCodePath = storeFileWithName(nominee1QrCodeFile, userId, "death-cases",
                    sanitizeName(req.getNominee1Name()) + "_qr");
            String nominee2QrCodePath = storeFileWithName(nominee2QrCodeFile, userId, "death-cases",
                    sanitizeName(req.getNominee2Name()) + "_qr");
            String certificate1Path = storeFileWithName(certificate1File, userId, "death-cases",
                    sanitizeName(req.getDeceasedName()) + "_cert1");

            log.info("Files stored - UserImage: {}, Nominee1QR: {}, Nominee2QR: {}, Cert1: {}",
                     userImagePath, nominee1QrCodePath, nominee2QrCodePath, certificate1Path);

            DeathCase deathCase = DeathCase.builder()
                    .deceasedName(req.getDeceasedName())
                    .employeeCode(req.getEmployeeCode())
                    .department(req.getDepartment())
                    .district(req.getDistrict())
                    .description(req.getDescription())
                    .userImage(userImagePath)
                    // Nominee Details
                    .nominee1Name(req.getNominee1Name())
                    .nominee1QrCode(nominee1QrCodePath)
                    .nominee2Name(req.getNominee2Name())
                    .nominee2QrCode(nominee2QrCodePath)
                    // Certificate Details
                    .certificate1(certificate1Path)
                    // Account Details
                    .account1(mapToAccountDetails(req.getAccount1()))
                    .account2(mapToAccountDetails(req.getAccount2()))
                    .account3(mapToAccountDetails(req.getAccount3()))
                    .caseDate(req.getCaseDate())
                    .status(DeathCaseStatus.OPEN)
                    .createdBy(userId)
                    .build();

            DeathCase savedDeathCase = repository.save(deathCase);
            log.info("Death case created successfully with ID: {}", savedDeathCase.getId());

            return mapper.map(savedDeathCase, DeathCaseResponse.class);
        } catch (Exception e) {
            log.error("Failed to create death case - DeceasedName: {}, Error: {}",
                      req.getDeceasedName(), e.getMessage(), e);
            throw new RuntimeException("Failed to create death case: " + e.getMessage(), e);
        }
    }

    public List<DeathCaseResponse> getAll() {
        return repository.findAll()
                .stream()
                .map(dc -> mapper.map(dc, DeathCaseResponse.class))
                .toList();
    }

    public DeathCaseResponse getById(Long id) {
        DeathCase dc = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Death case not found"));
        return mapper.map(dc, DeathCaseResponse.class);
    }

    public DeathCaseResponse update(
            Long id,
            UpdateDeathCaseRequest req,
            MultipartFile userImageFile,
            MultipartFile nominee1QrCodeFile,
            MultipartFile nominee2QrCodeFile,
            MultipartFile certificate1File,
            String userId) {

        DeathCase dc = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Death case not found"));

        dc.setDeceasedName(req.getDeceasedName());
        dc.setEmployeeCode(req.getEmployeeCode());
        dc.setDepartment(req.getDepartment());
        dc.setDistrict(req.getDistrict());
        dc.setDescription(req.getDescription());

        // Nominee Details
        dc.setNominee1Name(req.getNominee1Name());
        dc.setNominee2Name(req.getNominee2Name());

        // Update images only if new files are provided (with meaningful names)
        if (userImageFile != null && !userImageFile.isEmpty()) {
            dc.setUserImage(storeFileWithName(userImageFile, userId, "death-cases",
                    sanitizeName(req.getDeceasedName()) + "_user"));
        }
        if (nominee1QrCodeFile != null && !nominee1QrCodeFile.isEmpty()) {
            dc.setNominee1QrCode(storeFileWithName(nominee1QrCodeFile, userId, "death-cases",
                    sanitizeName(req.getNominee1Name()) + "_qr"));
        }
        if (nominee2QrCodeFile != null && !nominee2QrCodeFile.isEmpty()) {
            dc.setNominee2QrCode(storeFileWithName(nominee2QrCodeFile, userId, "death-cases",
                    sanitizeName(req.getNominee2Name()) + "_qr"));
        }
        if (certificate1File != null && !certificate1File.isEmpty()) {
            dc.setCertificate1(storeFileWithName(certificate1File, userId, "death-cases",
                    sanitizeName(req.getDeceasedName()) + "_cert1"));
        }

        // Account Details
        dc.setAccount1(mapToAccountDetails(req.getAccount1()));
        dc.setAccount2(mapToAccountDetails(req.getAccount2()));
        dc.setAccount3(mapToAccountDetails(req.getAccount3()));
        dc.setCaseDate(req.getCaseDate());
        dc.setStatus(req.getStatus());
        dc.setUpdatedBy(userId);

        return mapper.map(repository.save(dc), DeathCaseResponse.class);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Death case not found");
        }
        repository.deleteById(id);
    }

/**
     * Hide a death case - it won't appear on public/home page (sets to CLOSED)
     */
    public DeathCaseResponse hide(Long id, String userId) {
        DeathCase dc = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Death case not found"));

        dc.setStatus(DeathCaseStatus.CLOSED);
        dc.setUpdatedBy(userId);

        return mapper.map(repository.save(dc), DeathCaseResponse.class);
    }

    /**
     * Show/Unhide a death case - it will appear on public/home page again (sets to OPEN)
     */
    public DeathCaseResponse show(Long id, String userId) {
        DeathCase dc = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Death case not found"));

        dc.setStatus(DeathCaseStatus.OPEN);
        dc.setUpdatedBy(userId);

        return mapper.map(repository.save(dc), DeathCaseResponse.class);
    }

    /**
     * Get only visible (OPEN) death cases for public/home page
     * Excludes CLOSED and HIDDEN cases
     */
    public List<DeathCaseResponse> getVisibleCases() {
        return repository.findByStatus(DeathCaseStatus.OPEN)
                .stream()
                .map(dc -> mapper.map(dc, DeathCaseResponse.class))
                .toList();
    }

    private String storeFileWithName(MultipartFile file, String userId, String folderType, String customName) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        // Store in folder: {folderType}/{userId}/{customName}_{timestamp}.{ext}
        String subdirectory = folderType + "/" + userId;
        return fileStorageService.storeWithCustomName(file, subdirectory, customName);
    }

    private String sanitizeName(String name) {
        if (name == null || name.isEmpty()) {
            return "unknown";
        }
        // Remove special characters and replace spaces with underscores
        return name.replaceAll("[^a-zA-Z0-9\\s]", "")
                   .replaceAll("\\s+", "_")
                   .toLowerCase();
    }

    private AccountDetails mapToAccountDetails(AccountDetailsDTO dto) {
        if (dto == null) {
            return null;
        }
        return AccountDetails.builder()
                .bankName(dto.getBankName())
                .accountNumber(dto.getAccountNumber())
                .ifscCode(dto.getIfscCode())
                .accountHolderName(dto.getAccountHolderName())
                .build();
    }
}
