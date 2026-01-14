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
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class DeathCaseService {

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
                                     String userId) {

        // Store files with meaningful names (organized by userId)
        String userImagePath = storeFileWithName(userImageFile, userId, "user-images",
                sanitizeName(req.getDeceasedName()) + "_photo");
        String nominee1QrCodePath = storeFileWithName(nominee1QrCodeFile, userId, "qr-codes",
                sanitizeName(req.getNominee1Name()) + "_qr");
        String nominee2QrCodePath = storeFileWithName(nominee2QrCodeFile, userId, "qr-codes",
                sanitizeName(req.getNominee2Name()) + "_qr");

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
                // Account Details
                .account1(mapToAccountDetails(req.getAccount1()))
                .account2(mapToAccountDetails(req.getAccount2()))
                .account3(mapToAccountDetails(req.getAccount3()))
                .caseDate(req.getCaseDate())
                .status(DeathCaseStatus.OPEN)
                .createdBy(userId)
                .build();

        return mapper.map(repository.save(deathCase), DeathCaseResponse.class);
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
            dc.setUserImage(storeFileWithName(userImageFile, userId, "user-images",
                    sanitizeName(req.getDeceasedName()) + "_photo"));
        }
        if (nominee1QrCodeFile != null && !nominee1QrCodeFile.isEmpty()) {
            dc.setNominee1QrCode(storeFileWithName(nominee1QrCodeFile, userId, "qr-codes",
                    sanitizeName(req.getNominee1Name()) + "_qr"));
        }
        if (nominee2QrCodeFile != null && !nominee2QrCodeFile.isEmpty()) {
            dc.setNominee2QrCode(storeFileWithName(nominee2QrCodeFile, userId, "qr-codes",
                    sanitizeName(req.getNominee2Name()) + "_qr"));
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
