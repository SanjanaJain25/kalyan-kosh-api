package com.example.kalyan_kosh_api.service;

import com.example.kalyan_kosh_api.dto.PageResponse;
import com.example.kalyan_kosh_api.dto.RegisterRequest;
import com.example.kalyan_kosh_api.dto.UpdateUserRequest;
import com.example.kalyan_kosh_api.dto.UserResponse;
import com.example.kalyan_kosh_api.entity.Block;
import com.example.kalyan_kosh_api.entity.District;
import com.example.kalyan_kosh_api.entity.Role;
import com.example.kalyan_kosh_api.entity.Sambhag;
import com.example.kalyan_kosh_api.entity.State;
import com.example.kalyan_kosh_api.entity.User;
import com.example.kalyan_kosh_api.repository.BlockRepository;
import com.example.kalyan_kosh_api.repository.DistrictRepository;
import com.example.kalyan_kosh_api.repository.SambhagRepository;
import com.example.kalyan_kosh_api.repository.StateRepository;
import com.example.kalyan_kosh_api.repository.UserRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import com.example.kalyan_kosh_api.entity.DeathCase;
import com.example.kalyan_kosh_api.entity.Receipt;
import com.example.kalyan_kosh_api.repository.ReceiptRepository;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepo;
    private final BlockRepository blockRepo;
    private final DistrictRepository districtRepo;
    private final SambhagRepository sambhagRepo;
    private final StateRepository stateRepo;
    private final PasswordEncoder passwordEncoder;
    private final IdGeneratorService idGeneratorService;
    private final EmailService emailService;
private final ReceiptRepository receiptRepo;


    public UserService(UserRepository userRepo,
                       BlockRepository blockRepo,
                       DistrictRepository districtRepo,
                       SambhagRepository sambhagRepo,
                       StateRepository stateRepo,
                       PasswordEncoder passwordEncoder,
                       IdGeneratorService idGeneratorService,
                       EmailService emailService,
                       ReceiptRepository receiptRepo) {
        this.userRepo = userRepo;
        this.blockRepo = blockRepo;
        this.districtRepo = districtRepo;
        this.sambhagRepo = sambhagRepo;
        this.stateRepo = stateRepo;
        this.passwordEncoder = passwordEncoder;
        this.idGeneratorService = idGeneratorService;
        this.emailService = emailService;
           this.receiptRepo = receiptRepo;
    }

    private String normalizeString(String value) {
    if (value == null) return null;
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
}

private LocalDate parseDate(String value, String fieldName) {
    if (value == null) return null;

    String trimmed = value.trim();
    if (trimmed.isEmpty()) return null;

    try {
        return LocalDate.parse(trimmed);
    } catch (Exception e) {
        throw new IllegalArgumentException("Invalid date format for " + fieldName + ". Use yyyy-MM-dd");
    }
}

    public UserResponse getUserById(String id) {
        User user = userRepo.findByIdWithLocations(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        return toUserResponse(user);
    }

    public List<UserResponse> getAllUsers() {
        List<User> users = userRepo.findAllWithLocations();

        return users.stream()
                .map(this::toUserResponse)
                .toList();
    }
    private String csvSafeExcelText(String value) {
    if (value == null) return "";
    String cleaned = value.replace("\"", "\"\"").replace("\n", " ").replace("\r", " ");
    return "=\"" + cleaned + "\"";
}

   public UserResponse updateUser(String id, UpdateUserRequest req) {
    User user = userRepo.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

    // Update simple fields
    if (req.getName() != null) user.setName(normalizeString(req.getName()));
    if (req.getSurname() != null) user.setSurname(normalizeString(req.getSurname()));
    if (req.getFatherName() != null) user.setFatherName(normalizeString(req.getFatherName()));
    if (req.getEmail() != null) user.setEmail(normalizeString(req.getEmail()));
    if (req.getCountryCode() != null) user.setCountryCode(normalizeString(req.getCountryCode()));
    if (req.getMobileNumber() != null) user.setMobileNumber(normalizeString(req.getMobileNumber()));
    if (req.getPincode() != null) user.setPincode(req.getPincode());
    if (req.getGender() != null) user.setGender(normalizeString(req.getGender()));
    if (req.getMaritalStatus() != null) user.setMaritalStatus(normalizeString(req.getMaritalStatus()));
    if (req.getHomeAddress() != null) user.setHomeAddress(normalizeString(req.getHomeAddress()));

    // Handle date fields
    if (req.getDateOfBirth() != null) {
        user.setDateOfBirth(parseDate(req.getDateOfBirth(), "dateOfBirth"));
    }

    if (req.getJoiningDate() != null) {
        user.setJoiningDate(parseDate(req.getJoiningDate(), "joiningDate"));
    }

    if (req.getRetirementDate() != null) {
        user.setRetirementDate(parseDate(req.getRetirementDate(), "retirementDate"));
    }

    if (req.getSchoolOfficeName() != null) user.setSchoolOfficeName(normalizeString(req.getSchoolOfficeName()));
    if (req.getSankulName() != null) user.setSankulName(normalizeString(req.getSankulName()));
    if (req.getDepartment() != null) user.setDepartment(normalizeString(req.getDepartment()));

    // IMPORTANT FIX:
    // if blank string comes from frontend, convert it to null
    if (req.getDepartmentUniqueId() != null) {
        user.setDepartmentUniqueId(normalizeString(req.getDepartmentUniqueId()));
    }

    // Update location hierarchy
    State state = user.getDepartmentState();
    Sambhag sambhag = user.getDepartmentSambhag();
    District district = user.getDepartmentDistrict();

    // Update state if provided
    if (req.getDepartmentState() != null && !req.getDepartmentState().trim().isEmpty()) {
        state = stateRepo.findByName(req.getDepartmentState().trim())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Invalid state: " + req.getDepartmentState()));
        user.setDepartmentState(state);
    }

    // Update sambhag if provided
    if (req.getDepartmentSambhag() != null && !req.getDepartmentSambhag().trim().isEmpty()) {
        if (state == null && req.getDepartmentState() != null) {
            state = stateRepo.findByName(req.getDepartmentState().trim())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Invalid state: " + req.getDepartmentState()));
        }
        if (state != null) {
            final String stateName = state.getName();
            sambhag = sambhagRepo.findByNameAndState(req.getDepartmentSambhag().trim(), state)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Invalid sambhag: " + req.getDepartmentSambhag() +
                            " for state: " + stateName));
            user.setDepartmentSambhag(sambhag);
        }
    }

    // Update district entity if provided
    if (req.getDepartmentDistrict() != null && !req.getDepartmentDistrict().trim().isEmpty()) {
        if (sambhag == null && req.getDepartmentSambhag() != null && state != null) {
            sambhag = sambhagRepo.findByNameAndState(req.getDepartmentSambhag().trim(), state)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Invalid sambhag: " + req.getDepartmentSambhag()));
        }

        if (sambhag != null) {
            final String sambhagName = sambhag.getName();
            district = districtRepo.findByNameAndSambhag(req.getDepartmentDistrict().trim(), sambhag)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Invalid district: " + req.getDepartmentDistrict() +
                            " for sambhag: " + sambhagName));
        } else {
            district = districtRepo.findByName(req.getDepartmentDistrict().trim())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Invalid district: " + req.getDepartmentDistrict()));
        }
        user.setDepartmentDistrict(district);
    }

    // Update block entity if provided
    if (req.getDepartmentBlock() != null && !req.getDepartmentBlock().trim().isEmpty()) {
        if (district == null && req.getDepartmentDistrict() != null) {
            district = districtRepo.findByName(req.getDepartmentDistrict().trim())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Invalid district: " + req.getDepartmentDistrict()));
        }

        if (district != null) {
            final String districtName = district.getName();
            Block block = blockRepo.findByNameAndDistrict(req.getDepartmentBlock().trim(), district)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Invalid block: " + req.getDepartmentBlock() +
                            " for district: " + districtName));
            user.setDepartmentBlock(block);
        }
    }

    if (req.getNominee1Name() != null) user.setNominee1Name(normalizeString(req.getNominee1Name()));
    if (req.getNominee1Relation() != null) user.setNominee1Relation(normalizeString(req.getNominee1Relation()));
    if (req.getNominee2Name() != null) user.setNominee2Name(normalizeString(req.getNominee2Name()));
    if (req.getNominee2Relation() != null) user.setNominee2Relation(normalizeString(req.getNominee2Relation()));

    user.setUpdatedAt(Instant.now());

    userRepo.save(user);

    return toUserResponse(user);
}

private String generateTimestampedFileName(String baseName) {
    return baseName + "_" +
            java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"))
            + ".csv";
}
private String csvSafe(String value) {
    return value != null ? value.replace(",", " ").replace("\n", " ").replace("\r", " ") : "";
}
public void exportUsersCsv(
        String sambhagId,
        String districtId,
        String blockId,
        String name,
        String mobile,
        String userId,
        boolean includeMobile,
        java.io.PrintWriter writer) {

    String cleanName = (name != null && name.trim().isEmpty()) ? null : name;
    String cleanMobile = (mobile != null && mobile.trim().isEmpty()) ? null : mobile;
    String cleanUserId = (userId != null && userId.trim().isEmpty()) ? null : userId;

    List<User> users = userRepo.findAllUsersForExport(
            sambhagId, districtId, blockId, cleanName, cleanMobile, cleanUserId
    );
writer.write("\uFEFF");
if (includeMobile) {
    writer.println("UserId,Name,Surname,Mobile,Department,State,Sambhag,District,Block,SchoolOfficeName,CreatedAt");
} else {
    writer.println("UserId,Name,Surname,Department,State,Sambhag,District,Block,SchoolOfficeName,CreatedAt");
}
    for (User user : users) {
    UserResponse u = toUserResponse(user);

    if (includeMobile) {
        writer.printf("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s%n",
                csvSafe(u.getId()),
                csvSafe(u.getName()),
                csvSafe(u.getSurname()),
               csvSafeExcelText(u.getMobileNumber()),
                csvSafe(u.getDepartment()),
                csvSafe(u.getDepartmentState()),
                csvSafe(u.getDepartmentSambhag()),
                csvSafe(u.getDepartmentDistrict()),
                csvSafe(u.getDepartmentBlock()),
                csvSafe(u.getSchoolOfficeName()),
                u.getCreatedAt() != null ? u.getCreatedAt().toString() : ""
        );
    } else {
        writer.printf("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s%n",
                csvSafe(u.getId()),
                csvSafe(u.getName()),
                csvSafe(u.getSurname()),
                csvSafe(u.getDepartment()),
                csvSafe(u.getDepartmentState()),
                csvSafe(u.getDepartmentSambhag()),
                csvSafe(u.getDepartmentDistrict()),
                csvSafe(u.getDepartmentBlock()),
                csvSafe(u.getSchoolOfficeName()),
                u.getCreatedAt() != null ? u.getCreatedAt().toString() : ""
        );
    }
}

    writer.flush();
}
public void exportPendingProfilesCsv(
        String sambhagId,
        String districtId,
        String blockId,
        String name,
        String mobile,
        String userId,
        boolean includeMobile,
        java.io.PrintWriter writer) {

    List<UserResponse> users = getPendingProfileUsersForExport(
            sambhagId, districtId, blockId, name, mobile, userId
    );
writer.write("\uFEFF");
if (includeMobile) {
    writer.println("UserId,Name,Surname,Mobile,Department,State,Sambhag,District,Block,SchoolOfficeName,CreatedAt");
} else {
    writer.println("UserId,Name,Surname,Department,State,Sambhag,District,Block,SchoolOfficeName,CreatedAt");
}
for (UserResponse u : users) {
    if (includeMobile) {
        writer.printf("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s%n",
                csvSafe(u.getId()),
                csvSafe(u.getName()),
                csvSafe(u.getSurname()),
               csvSafeExcelText(u.getMobileNumber()),
                csvSafe(u.getDepartment()),
                csvSafe(u.getDepartmentState()),
                csvSafe(u.getDepartmentSambhag()),
                csvSafe(u.getDepartmentDistrict()),
                csvSafe(u.getDepartmentBlock()),
                csvSafe(u.getSchoolOfficeName()),
                u.getCreatedAt() != null ? u.getCreatedAt().toString() : ""
        );
    } else {
        writer.printf("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s%n",
                csvSafe(u.getId()),
                csvSafe(u.getName()),
                csvSafe(u.getSurname()),
                csvSafe(u.getDepartment()),
                csvSafe(u.getDepartmentState()),
                csvSafe(u.getDepartmentSambhag()),
                csvSafe(u.getDepartmentDistrict()),
                csvSafe(u.getDepartmentBlock()),
                csvSafe(u.getSchoolOfficeName()),
                u.getCreatedAt() != null ? u.getCreatedAt().toString() : ""
        );
    }
}

    writer.flush();
}

public List<UserResponse> getPendingProfileUsersForExport(
        String sambhagId,
        String districtId,
        String blockId,
        String name,
        String mobile,
        String userId) {

    String cleanName = (name != null && name.trim().isEmpty()) ? null : name;
    String cleanMobile = (mobile != null && mobile.trim().isEmpty()) ? null : mobile;
    String cleanUserId = (userId != null && userId.trim().isEmpty()) ? null : userId;

    List<User> users = userRepo.findPendingProfileUsersForExport(
            sambhagId,
            districtId,
            blockId,
            cleanName,
            cleanMobile,
            cleanUserId
    );

    return users.stream()
            .map(this::toUserResponse)
            .collect(Collectors.toList());
}


/**
 * Filtered + Paginated method to get only pending-profile users
 * Pending profile = missing important profile/location fields
 */
public PageResponse<UserResponse> getPendingProfileUsersFiltered(
        String sambhagId,
        String districtId,
        String blockId,
        String name,
        String mobile,
        String userId,
        int page,
        int size) {

    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

    // Clean up empty strings to null for proper query handling
    String cleanName = (name != null && name.trim().isEmpty()) ? null : name;
    String cleanMobile = (mobile != null && mobile.trim().isEmpty()) ? null : mobile;
    String cleanUserId = (userId != null && userId.trim().isEmpty()) ? null : userId;

    Page<User> userPage = userRepo.findPendingProfileUsersWithFilters(
            sambhagId, districtId, blockId, cleanName, cleanMobile, cleanUserId, pageable);

    List<UserResponse> userResponses = userPage.getContent().stream()
            .map(this::toUserResponse)
            .collect(Collectors.toList());

    return new PageResponse<>(
            userResponses,
            userPage.getNumber(),
            userPage.getSize(),
            userPage.getTotalElements(),
            userPage.getTotalPages(),
            userPage.isLast(),
            userPage.isFirst()
    );
}

    /**
     * Update user password
     * Validates current password before updating to new password
     */
    @Transactional
    public void updatePassword(String userId, String currentPassword, String newPassword) {

        // Find user
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        // Check if new password is same as current
        if (passwordEncoder.matches(newPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("New password cannot be the same as current password");
        }

        // Update password
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(Instant.now());
        userRepo.save(user);
    }

    /**
     * Register a new user
     */
    @Transactional
    public UserResponse register(RegisterRequest req) {
        // Check if user with this email already exists
        if (userRepo.findByEmail(req.getEmail()).isPresent()) {
            throw new IllegalArgumentException("User with this email already exists");
        }

        User u = new User();

        // Set basic fields
        u.setName(req.getName());
        u.setSurname(req.getSurname());
        u.setFatherName(req.getFatherName());
        u.setEmail(req.getEmail());
        u.setMobileNumber(req.getMobileNumber());
        u.setCountryCode(req.getCountryCode());
        u.setPincode(req.getPincode());
        u.setGender(req.getGender());
        u.setMaritalStatus(req.getMaritalStatus());
        u.setHomeAddress(req.getHomeAddress());

        // Set school/office fields
        u.setSchoolOfficeName(req.getSchoolOfficeName());
        u.setSankulName(req.getSankulName());
        u.setDepartment(req.getDepartment());

        // Set department unique ID only if provided
        if (req.getDepartmentUniqueId() != null && !req.getDepartmentUniqueId().trim().isEmpty()) {
            u.setDepartmentUniqueId(req.getDepartmentUniqueId().trim());
        } else {
            u.setDepartmentUniqueId(null);
        }

        // Set location entities (State, Sambhag, District, Block)
        if (req.getDepartmentState() != null && !req.getDepartmentState().isEmpty()) {
            State state = stateRepo.findByName(req.getDepartmentState())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid state: " + req.getDepartmentState()));
            u.setDepartmentState(state);

            if (req.getDepartmentSambhag() != null && !req.getDepartmentSambhag().isEmpty()) {
                Sambhag sambhag = sambhagRepo.findByNameAndState(req.getDepartmentSambhag(), state)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid sambhag: " + req.getDepartmentSambhag()));
                u.setDepartmentSambhag(sambhag);

                if (req.getDepartmentDistrict() != null && !req.getDepartmentDistrict().isEmpty()) {
                    District district = districtRepo.findByNameAndSambhag(req.getDepartmentDistrict(), sambhag)
                            .orElseThrow(() -> new IllegalArgumentException("Invalid district: " + req.getDepartmentDistrict()));
                    u.setDepartmentDistrict(district);

                    if (req.getDepartmentBlock() != null && !req.getDepartmentBlock().isEmpty()) {
                        Block block = blockRepo.findByNameAndDistrict(req.getDepartmentBlock(), district)
                                .orElseThrow(() -> new IllegalArgumentException("Invalid block: " + req.getDepartmentBlock()));
                        u.setDepartmentBlock(block);
                    }
                }
            }
        }

        // Set nominee information
        u.setNominee1Name(req.getNominee1Name());
        u.setNominee1Relation(req.getNominee1Relation());
        u.setNominee2Name(req.getNominee2Name());
        u.setNominee2Relation(req.getNominee2Relation());

        // Parse dates
        if (req.getDateOfBirth() != null && !req.getDateOfBirth().isEmpty()) {
            try {
                u.setDateOfBirth(LocalDate.parse(req.getDateOfBirth()));
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid date format for dateOfBirth. Use yyyy-MM-dd");
            }
        }

        if (req.getJoiningDate() != null && !req.getJoiningDate().isEmpty()) {
            try {
                u.setJoiningDate(LocalDate.parse(req.getJoiningDate()));
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid date format for joiningDate. Use yyyy-MM-dd");
            }
        }

        if (req.getRetirementDate() != null && !req.getRetirementDate().isEmpty()) {
            try {
                u.setRetirementDate(LocalDate.parse(req.getRetirementDate()));
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid date format for retirementDate. Use yyyy-MM-dd");
            }
        }

        // Encode password and set role
        u.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        u.setRole(Role.ROLE_USER);

        // Generate user ID
        String userId = idGeneratorService.generateNextUserId();
        u.setId(userId);

        // Set timestamps manually (createdAt should only be set once during creation)
        u.setCreatedAt(Instant.now());
        u.setUpdatedAt(Instant.now());

        // Save user
        User savedUser = userRepo.save(u);

        // Send registration confirmation email
        // Only use name field for greeting (surname is separate)
        String fullName = savedUser.getName();
        if (savedUser.getSurname() != null && !savedUser.getSurname().trim().isEmpty()) {
            // Only append surname if it's not already part of the name
            if (!fullName.toLowerCase().contains(savedUser.getSurname().toLowerCase().trim())) {
                fullName = fullName + " " + savedUser.getSurname().trim();
            }
        }
        emailService.sendRegistrationConfirmationEmail(savedUser.getEmail(), fullName.trim(), savedUser.getId());

        return toUserResponse(savedUser);
    }

    /**
     * Convert User entity to UserResponse DTO
     */
    private UserResponse toUserResponse(User user) {
        UserResponse response = new UserResponse();

        response.setId(user.getId());
        response.setName(user.getName());
        response.setSurname(user.getSurname());
        response.setFatherName(user.getFatherName());
        response.setEmail(user.getEmail());
        response.setMobileNumber(user.getMobileNumber());
        response.setPincode(user.getPincode());
        response.setGender(user.getGender());
        response.setMaritalStatus(user.getMaritalStatus());
        response.setHomeAddress(user.getHomeAddress());
        response.setDateOfBirth(user.getDateOfBirth());
        response.setJoiningDate(user.getJoiningDate());
        response.setRetirementDate(user.getRetirementDate());
        response.setSchoolOfficeName(user.getSchoolOfficeName());
        response.setSankulName(user.getSankulName());
        response.setDepartment(user.getDepartment());
        response.setDepartmentUniqueId(user.getDepartmentUniqueId());

        // Convert entity relationships to string names
        if (user.getDepartmentState() != null) {
            response.setDepartmentState(user.getDepartmentState().getName());
        }

        if (user.getDepartmentSambhag() != null) {
            response.setDepartmentSambhag(user.getDepartmentSambhag().getName());
        }

        if (user.getDepartmentDistrict() != null) {
            response.setDepartmentDistrict(user.getDepartmentDistrict().getName());
        }

        if (user.getDepartmentBlock() != null) {
            response.setDepartmentBlock(user.getDepartmentBlock().getName());
        }

        response.setNominee1Name(user.getNominee1Name());
        response.setNominee1Relation(user.getNominee1Relation());
        response.setNominee2Name(user.getNominee2Name());
        response.setNominee2Relation(user.getNominee2Relation());
        response.setRole(user.getRole());
        response.setCreatedAt(user.getCreatedAt());
if (user.getAssignedDeathCase() != null) {
    DeathCase deathCase = user.getAssignedDeathCase();

    response.setAssignedDeathCaseId(deathCase.getId());
    response.setAssignedDeathCaseName(deathCase.getDeceasedName());

    String allocatedQrCode = deathCase.getNominee1QrCode();
    if (allocatedQrCode == null || allocatedQrCode.isBlank()) {
        allocatedQrCode = deathCase.getNominee2QrCode();
    }

    Optional<Receipt> latestReceiptOpt =
            receiptRepo.findTopByUserIdAndDeathCaseIdOrderByUploadedAtDesc(
                    user.getId(),
                    deathCase.getId()
            );

    if (latestReceiptOpt.isPresent()) {
        Receipt latestReceipt = latestReceiptOpt.get();

        response.setUtrUploaded(true);
        response.setLatestReceiptId(latestReceipt.getId());
        response.setLatestUtrNumber(latestReceipt.getUtrNumber());
        response.setUtrUploadedAt(latestReceipt.getUploadedAt());

        // If UTR already uploaded, QR should not be shown
        response.setAllocatedQrCode(null);
    } else {
        response.setUtrUploaded(false);
        response.setAllocatedQrCode(allocatedQrCode);
    }
} else {
    response.setAssignedDeathCaseId(null);
    response.setAssignedDeathCaseName(null);
    response.setAllocatedQrCode(null);
    response.setUtrUploaded(false);
    response.setLatestReceiptId(null);
    response.setLatestUtrNumber(null);
    response.setUtrUploadedAt(null);
}
        return response;
    }


    /**
     * Paginated method to get users - 20 users per page, sorted by insertion order (createdAt ASC)
     * Optimized for large datasets (60000+ users)
     */
    public PageResponse<UserResponse> getAllUsersPaginated(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<User> userPage = userRepo
                .findAllWithLocations(pageable);

        List<UserResponse> userResponses = userPage.getContent().stream()
                .map(this::toUserResponse)
                .collect(Collectors.toList());


        return new PageResponse<>(
                userResponses,
                userPage.getNumber(),
                userPage.getSize(),
                userPage.getTotalElements(),
                userPage.getTotalPages(),
                userPage.isLast(),
                userPage.isFirst()
        );
    }

    /**
     * Filtered + Paginated method to get users
     * Filters: Sambhag, District, Block, Name (searches in name & surname), Mobile, UserId
     * 20 users per page, sorted by insertion order (createdAt DESC - newest first)
     */
    public PageResponse<UserResponse> getAllUsersFiltered(
            String sambhagId,
            String districtId,
            String blockId,
            String name,
            String mobile,
            String userId,
            int page,
            int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        // Clean up empty strings to null for proper query handling
        String cleanName = (name != null && name.trim().isEmpty()) ? null : name;
        String cleanMobile = (mobile != null && mobile.trim().isEmpty()) ? null : mobile;
        String cleanUserId = (userId != null && userId.trim().isEmpty()) ? null : userId;

        Page<User> userPage = userRepo.findAllWithFilters(
                sambhagId, districtId, blockId, cleanName, cleanMobile, cleanUserId, pageable);

        List<UserResponse> userResponses = userPage.getContent().stream()
                .map(this::toUserResponse)
                .collect(Collectors.toList());


        return new PageResponse<>(
                userResponses,
                userPage.getNumber(),
                userPage.getSize(),
                userPage.getTotalElements(),
                userPage.getTotalPages(),
                userPage.isLast(),
                userPage.isFirst()
        );
    }
}
