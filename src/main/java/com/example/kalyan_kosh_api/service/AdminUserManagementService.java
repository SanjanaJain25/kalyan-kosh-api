package com.example.kalyan_kosh_api.service;

import com.example.kalyan_kosh_api.dto.AdminUserListResponse;
import com.example.kalyan_kosh_api.dto.AdminUserResponse;
import com.example.kalyan_kosh_api.dto.UpdateUserRoleRequest;
import com.example.kalyan_kosh_api.entity.Role;
import com.example.kalyan_kosh_api.entity.User;
import com.example.kalyan_kosh_api.entity.UserStatus;
import com.example.kalyan_kosh_api.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.example.kalyan_kosh_api.repository.ReceiptRepository;
import com.example.kalyan_kosh_api.repository.ManagerAssignmentRepository;
import com.example.kalyan_kosh_api.repository.ManagerQueryRepository;
import java.time.Instant;
import java.util.List;
import com.example.kalyan_kosh_api.repository.ManagerQueryMessageRepository;
import com.example.kalyan_kosh_api.repository.DeleteRequestRepository;
import com.example.kalyan_kosh_api.repository.AuditLogRepository;
import com.example.kalyan_kosh_api.entity.DeleteEntityType;
import com.example.kalyan_kosh_api.entity.ManagerQuery;
import java.util.stream.Collectors;
import java.time.format.DateTimeFormatter;
import com.example.kalyan_kosh_api.dto.BulkPasswordResetResponse;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.web.multipart.MultipartFile;
import com.example.kalyan_kosh_api.dto.BulkLocationUpdateResponse;
import com.example.kalyan_kosh_api.entity.State;
import com.example.kalyan_kosh_api.entity.Sambhag;
import com.example.kalyan_kosh_api.entity.District;
import com.example.kalyan_kosh_api.entity.Block;
import com.example.kalyan_kosh_api.repository.StateRepository;
import com.example.kalyan_kosh_api.repository.SambhagRepository;
import com.example.kalyan_kosh_api.repository.DistrictRepository;
import com.example.kalyan_kosh_api.repository.BlockRepository;

import java.util.LinkedHashMap;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@Service
public class AdminUserManagementService {
    private static final String RESERVED_SUPER_ADMIN_ID = "PMUMS202502";
private static final Role RESERVED_SUPER_ADMIN_ROLE = Role.ROLE_SUPERADMIN;
private final PasswordEncoder passwordEncoder;
private final UserRepository userRepository;
private final ReceiptRepository receiptRepository;
private final ManagerAssignmentRepository managerAssignmentRepository;
private final ManagerQueryRepository managerQueryRepository;
private final ManagerQueryMessageRepository managerQueryMessageRepository;
private final DeleteRequestRepository deleteRequestRepository;
private final AuditLogRepository auditLogRepository;
private final StateRepository stateRepository;
private final SambhagRepository sambhagRepository;
private final DistrictRepository districtRepository;
private final BlockRepository blockRepository;
public AdminUserManagementService(
        PasswordEncoder passwordEncoder,
        UserRepository userRepository,
        ReceiptRepository receiptRepository,
        ManagerAssignmentRepository managerAssignmentRepository,
        ManagerQueryRepository managerQueryRepository,
        ManagerQueryMessageRepository managerQueryMessageRepository,
        DeleteRequestRepository deleteRequestRepository,
        AuditLogRepository auditLogRepository,
        StateRepository stateRepository,
        SambhagRepository sambhagRepository,
        DistrictRepository districtRepository,
        BlockRepository blockRepository
) {
    this.passwordEncoder = passwordEncoder;
    this.userRepository = userRepository;
    this.receiptRepository = receiptRepository;
    this.managerAssignmentRepository = managerAssignmentRepository;
    this.managerQueryRepository = managerQueryRepository;
    this.managerQueryMessageRepository = managerQueryMessageRepository;
    this.deleteRequestRepository = deleteRequestRepository;
    this.auditLogRepository = auditLogRepository;
    this.stateRepository = stateRepository;
    this.sambhagRepository = sambhagRepository;
    this.districtRepository = districtRepository;
    this.blockRepository = blockRepository;
}
private boolean isReservedSuperAdmin(User user) {
    return user != null
            && RESERVED_SUPER_ADMIN_ID.equals(user.getId())
            && RESERVED_SUPER_ADMIN_ROLE.equals(user.getRole());
}

private void validateNotReservedSuperAdmin(User user) {
    if (isReservedSuperAdmin(user)) {
        throw new IllegalArgumentException("Super Admin account cannot be modified.");
    }
}

private String normalize(String value) {
    return (value == null || value.trim().isEmpty()) ? null : value.trim();
}
    /**
     * Get all users with pagination and filters
     */
    public AdminUserListResponse getAllUsers(int page, int size, String userId, String name, String email, 
                                           String mobileNumber, Role role, UserStatus status,
                                           String sambhag, String district, String block) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        // For now, using simple findAll - can be enhanced with specifications for filtering
 Page<User> userPage = userRepository.searchAdminUsers(
        normalize(userId),
        normalize(name),
        normalize(email),
        normalize(mobileNumber),
        role,
        status,
        normalize(sambhag),
        normalize(district),
        normalize(block),
        RESERVED_SUPER_ADMIN_ID,
        RESERVED_SUPER_ADMIN_ROLE,
        pageable
);
     //With superadmin in list   
        // List<AdminUserResponse> users = userPage.getContent().stream()
        //         .filter(user -> filterUser(user, userId, name, email, mobileNumber, role, status, sambhag, district, block))
        //         .map(this::convertToAdminUserResponse)
        //         .collect(Collectors.toList());
//without superadmin in list
// List<AdminUserResponse> users = userPage.getContent().stream()
//         .filter(user -> !isReservedSuperAdmin(user))
//         .filter(user -> filterUser(user, userId, name, email, mobileNumber, role, status, sambhag, district, block))
//         .map(this::convertToAdminUserResponse)
//         .collect(Collectors.toList());
List<AdminUserResponse> users = userPage.getContent().stream()
        .map(this::convertToAdminUserResponse)
        .collect(Collectors.toList());

        return new AdminUserListResponse(
                users,
                userPage.getNumber(),
                userPage.getTotalPages(),
                userPage.getTotalElements(),
                userPage.getSize(),
                userPage.hasNext(),
                userPage.hasPrevious()
        );
    }

    /**
     * Block a user
     */

@Transactional
public void blockUser(String userId) {
    User user = getUserById(userId);
    validateNotReservedSuperAdmin(user);// superadmin

    user.setStatus(UserStatus.BLOCKED);
    user.setUpdatedAt(Instant.now());
    userRepository.save(user);
}
    /**
     * Unblock a user
     */
  
@Transactional
public void unblockUser(String userId) {
    User user = getUserById(userId);
    validateNotReservedSuperAdmin(user);// superadmin

    user.setStatus(UserStatus.ACTIVE);
    user.setUpdatedAt(Instant.now());
    userRepository.save(user);
}
    /**
     * Soft delete a user
     */

    @Transactional
public void deleteUser(String userId) {
    User user = getUserById(userId);
    validateNotReservedSuperAdmin(user); // superadmin

    user.setStatus(UserStatus.DELETED);
    user.setUpdatedAt(Instant.now());
    userRepository.save(user);
}
@Transactional
public void restoreUser(String userId) {
    User user = getUserById(userId);
     validateNotReservedSuperAdmin(user);
    if (user.getStatus() != UserStatus.DELETED) {
        throw new IllegalArgumentException("Only deleted users can be restored");
    }
    user.setStatus(UserStatus.ACTIVE);
    user.setUpdatedAt(Instant.now());
    userRepository.save(user);
}

  @Transactional
public void permanentDeleteUser(String userId) {
    User user = getUserById(userId);
    validateNotReservedSuperAdmin(user);

    if (user.getStatus() != UserStatus.DELETED) {
        throw new IllegalArgumentException("User must be soft deleted before permanent delete");
    }

    cleanupUserRelationsBeforeHardDelete(user);

    userRepository.delete(user);
    userRepository.flush();
}

private void cleanupUserRelationsBeforeHardDelete(User user) {
    String userId = user.getId();

    // 1. Delete receipts uploaded by this user
    receiptRepository.deleteByUser(user);

    // 2. Delete query messages sent by this user
    managerQueryMessageRepository.deleteBySender(user);

    // 3. Delete manager queries related to this user
    List<ManagerQuery> relatedQueries = managerQueryRepository.findAllRelatedToUser(user);
    if (relatedQueries != null && !relatedQueries.isEmpty()) {
        managerQueryMessageRepository.deleteByQueryIn(relatedQueries);
        managerQueryRepository.deleteAll(relatedQueries);
    }

    // 4. Delete manager assignments
    managerAssignmentRepository.deleteByManager(user);
    managerAssignmentRepository.deleteByAssignedBy(user);

    // 5. Delete delete-request references
    deleteRequestRepository.deleteUserRelatedRequests(DeleteEntityType.USER.name(), userId);

    // 6. Clear audit log FK reference
    auditLogRepository.clearPerformedBy(userId);

    // 7. Clear deleted_by reference from other users
    userRepository.clearDeletedByReference(userId);
}

    /**
     * Update user role
     */
    @Transactional
    public void updateUserRole(String userId, UpdateUserRoleRequest request) {
        User user = getUserById(userId);
         validateNotReservedSuperAdmin(user);

    if (request.getRole() == Role.ROLE_SUPERADMIN) {
        throw new IllegalArgumentException("Super Admin role cannot be assigned manually.");
    }

        user.setRole(request.getRole());
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);
    }

    // Reset user password
@Transactional
public void resetUserPassword(String userId, String newPassword) {
    User user = getUserById(userId);
    validateNotReservedSuperAdmin(user);

    if (user.getStatus() == UserStatus.DELETED) {
        throw new IllegalArgumentException("Cannot reset password for deleted user");
    }

    if (newPassword == null || newPassword.trim().isEmpty()) {
        throw new IllegalArgumentException("New password is required");
    }

    String cleanPassword = newPassword.trim();

    if (cleanPassword.length() < 6) {
        throw new IllegalArgumentException("Password must be at least 6 characters");
    }

    user.setPasswordHash(passwordEncoder.encode(cleanPassword));
    user.setUpdatedAt(Instant.now());
    userRepository.save(user);
}

@Transactional
public BulkLocationUpdateResponse bulkUpdateUserLocationsFromExcel(
        MultipartFile file,
        boolean dryRun
) {
    if (file == null || file.isEmpty()) {
        throw new IllegalArgumentException("Excel file is required");
    }

    Map<String, ExcelLocationRow> excelRows = new LinkedHashMap<>();
    List<String> duplicateUserIds = new ArrayList<>();
    List<String> notFoundUsers = new ArrayList<>();
    List<String> locationErrors = new ArrayList<>();

    try (InputStream inputStream = file.getInputStream();
         Workbook workbook = WorkbookFactory.create(inputStream)) {

        DataFormatter formatter = new DataFormatter();
        Sheet sheet = workbook.getSheetAt(0);

        if (sheet == null || sheet.getPhysicalNumberOfRows() == 0) {
            throw new IllegalArgumentException("Excel sheet is empty");
        }

        Row headerRow = sheet.getRow(0);

        if (headerRow == null) {
            throw new IllegalArgumentException("Header row not found in Excel");
        }

        Map<String, Integer> columnMap = new HashMap<>();

        for (Cell cell : headerRow) {
            String header = formatter.formatCellValue(cell)
                    .trim()
                    .replace(" ", "")
                    .toLowerCase();

            columnMap.put(header, cell.getColumnIndex());
        }

        int userIdCol = getRequiredExcelColumn(columnMap, "userid");
        int stateCol = getRequiredExcelColumn(columnMap, "state");
        int sambhagCol = getRequiredExcelColumn(columnMap, "sambhag");
        int districtCol = getRequiredExcelColumn(columnMap, "district");
        int blockCol = getRequiredExcelColumn(columnMap, "block");

        for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);

            if (row == null) {
                continue;
            }

            String userId = getExcelCellValue(formatter, row, userIdCol);
            String stateName = getExcelCellValue(formatter, row, stateCol);
            String sambhagName = getExcelCellValue(formatter, row, sambhagCol);
            String districtName = getExcelCellValue(formatter, row, districtCol);
            String blockName = getExcelCellValue(formatter, row, blockCol);

            if (userId == null || userId.isBlank()) {
                continue;
            }

            if (excelRows.containsKey(userId)) {
                duplicateUserIds.add(userId);
                continue;
            }

            excelRows.put(userId, new ExcelLocationRow(
                    userId,
                    stateName,
                    sambhagName,
                    districtName,
                    blockName,
                    rowIndex + 1
            ));
        }

    } catch (IllegalArgumentException e) {
        throw e;
    } catch (Exception e) {
        throw new IllegalArgumentException("Failed to read Excel file: " + e.getMessage());
    }

    if (excelRows.isEmpty()) {
        throw new IllegalArgumentException("No valid UserId rows found in Excel");
    }

    List<User> existingUsers = userRepository.findAllById(excelRows.keySet());

    Map<String, User> userMap = new HashMap<>();

    for (User user : existingUsers) {
        userMap.put(user.getId(), user);
    }

    List<User> usersToUpdate = new ArrayList<>();
    Instant now = Instant.now();

    for (ExcelLocationRow excelRow : excelRows.values()) {
        User user = userMap.get(excelRow.userId());

        if (user == null) {
            notFoundUsers.add("Row " + excelRow.rowNumber() + ": User not found - " + excelRow.userId());
            continue;
        }

        if (isReservedSuperAdmin(user)) {
            notFoundUsers.add("Row " + excelRow.rowNumber() + ": Reserved super admin skipped - " + excelRow.userId());
            continue;
        }

        if (user.getStatus() == UserStatus.DELETED) {
            notFoundUsers.add("Row " + excelRow.rowNumber() + ": Deleted user skipped - " + excelRow.userId());
            continue;
        }

        if (isBlank(excelRow.stateName())
                || isBlank(excelRow.sambhagName())
                || isBlank(excelRow.districtName())
                || isBlank(excelRow.blockName())) {

            locationErrors.add("Row " + excelRow.rowNumber() + ": Location value missing for user - " + excelRow.userId());
            continue;
        }

        State state = stateRepository.findByNameIgnoreCase(excelRow.stateName())
                .orElse(null);

        if (state == null) {
            locationErrors.add("Row " + excelRow.rowNumber() + ": State not found - " + excelRow.stateName());
            continue;
        }

        Sambhag sambhag = sambhagRepository.findByNameIgnoreCaseAndState(excelRow.sambhagName(), state)
                .orElse(null);

        if (sambhag == null) {
            locationErrors.add("Row " + excelRow.rowNumber() + ": Sambhag not found - " + excelRow.sambhagName());
            continue;
        }

        District district = districtRepository.findByNameIgnoreCaseAndSambhag(excelRow.districtName(), sambhag)
                .orElse(null);

        if (district == null) {
            locationErrors.add("Row " + excelRow.rowNumber() + ": District not found - " + excelRow.districtName());
            continue;
        }

        Block block = blockRepository.findByNameIgnoreCaseAndDistrict(excelRow.blockName(), district)
                .orElse(null);

        if (block == null) {
            locationErrors.add("Row " + excelRow.rowNumber() + ": Block not found - " + excelRow.blockName());
            continue;
        }

        user.setDepartmentState(state);
        user.setDepartmentSambhag(sambhag);
        user.setDepartmentDistrict(district);
        user.setDepartmentBlock(block);
        user.setUpdatedAt(now);

        usersToUpdate.add(user);
    }

    if (!dryRun && !usersToUpdate.isEmpty()) {
        userRepository.saveAll(usersToUpdate);
    }

    int skippedCount = duplicateUserIds.size() + notFoundUsers.size() + locationErrors.size();

    return new BulkLocationUpdateResponse(
            excelRows.size() + duplicateUserIds.size(),
            excelRows.size(),
            dryRun ? 0 : usersToUpdate.size(),
            skippedCount,
            duplicateUserIds.size(),
            duplicateUserIds,
            notFoundUsers,
            locationErrors,
            dryRun
    );
}
@Transactional
public BulkPasswordResetResponse bulkResetPasswordsFromExcel(
        MultipartFile file,
        String defaultPassword
) {
    if (file == null || file.isEmpty()) {
        throw new IllegalArgumentException("Excel file is required");
    }

    if (defaultPassword == null || defaultPassword.trim().isEmpty()) {
        throw new IllegalArgumentException("Default password is required");
    }

    String cleanPassword = defaultPassword.trim();

    if (cleanPassword.length() < 6) {
        throw new IllegalArgumentException("Password must be at least 6 characters");
    }

    Set<String> registrationNumbers = new LinkedHashSet<>();

    try (InputStream inputStream = file.getInputStream();
         Workbook workbook = WorkbookFactory.create(inputStream)) {

        DataFormatter formatter = new DataFormatter();

        for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
            Sheet sheet = workbook.getSheetAt(sheetIndex);

            if (sheet == null || sheet.getPhysicalNumberOfRows() == 0) {
                continue;
            }

            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                continue;
            }

            int registrationColumnIndex = -1;

            for (Cell cell : headerRow) {
                String header = formatter.formatCellValue(cell).trim();

                if ("Registration No.".equalsIgnoreCase(header)
                        || "Registration No".equalsIgnoreCase(header)
                        || "Registration Number".equalsIgnoreCase(header)
                        || "RegistrationNumber".equalsIgnoreCase(header)
                        || "Reg No".equalsIgnoreCase(header)
                        || "Reg. No.".equalsIgnoreCase(header)) {
                    registrationColumnIndex = cell.getColumnIndex();
                    break;
                }
            }

            if (registrationColumnIndex == -1) {
                continue;
            }

            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    continue;
                }

                Cell cell = row.getCell(registrationColumnIndex);
                String registrationNo = formatter.formatCellValue(cell).trim();

                if (!registrationNo.isEmpty()) {
                    registrationNumbers.add(registrationNo);
                }
            }
        }

    } catch (Exception e) {
        throw new IllegalArgumentException("Failed to read Excel file: " + e.getMessage());
    }

    if (registrationNumbers.isEmpty()) {
        throw new IllegalArgumentException("No registration numbers found in Excel. Please check Registration No. column.");
    }

    List<User> users = userRepository.findAllById(registrationNumbers);

    Map<String, User> userMap = new HashMap<>();
    for (User user : users) {
        userMap.put(user.getId(), user);
    }

  List<String> notFoundRegistrationNumbers = new ArrayList<>();
List<User> usersToUpdate = new ArrayList<>();

String encodedDefaultPassword = passwordEncoder.encode(cleanPassword);
Instant now = Instant.now();

for (String registrationNo : registrationNumbers) {
    User user = userMap.get(registrationNo);

    if (user == null) {
        notFoundRegistrationNumbers.add(registrationNo);
        continue;
    }

    if (isReservedSuperAdmin(user)) {
        notFoundRegistrationNumbers.add(registrationNo + " - SUPERADMIN_SKIPPED");
        continue;
    }

    if (user.getStatus() == UserStatus.DELETED) {
        notFoundRegistrationNumbers.add(registrationNo + " - DELETED_USER_SKIPPED");
        continue;
    }

    user.setPasswordHash(encodedDefaultPassword);
    user.setUpdatedAt(now);
    usersToUpdate.add(user);
}

userRepository.saveAll(usersToUpdate);
    return new BulkPasswordResetResponse(
            registrationNumbers.size(),
            usersToUpdate.size(),
            notFoundRegistrationNumbers.size(),
            notFoundRegistrationNumbers
    );
}

@Transactional
public void resetManagerDashboardPassword(String userId, String newPassword) {
    User user = getUserById(userId);
    validateNotReservedSuperAdmin(user);

    if (user.getStatus() == UserStatus.DELETED) {
        throw new IllegalArgumentException("Cannot reset Manager Dashboard password for deleted user");
    }

    if (user.getRole() == Role.ROLE_USER) {
        throw new IllegalArgumentException("Manager Dashboard password can be set only for manager/admin roles");
    }

    if (newPassword == null || newPassword.trim().isEmpty()) {
        throw new IllegalArgumentException("New Manager Dashboard password is required");
    }

    String cleanPassword = newPassword.trim();

    if (cleanPassword.length() < 6) {
        throw new IllegalArgumentException("Password must be at least 6 characters");
    }

    user.setManagerDashboardPasswordHash(passwordEncoder.encode(cleanPassword));
    user.setUpdatedAt(Instant.now());
    userRepository.save(user);
}
    /**
     * Get user by ID
     */
    public AdminUserResponse getUserByIdResponse(String userId) {
        User user = getUserById(userId);

    if (isReservedSuperAdmin(user)) {
        throw new RuntimeException("User not found with ID: " + userId);
    }
        return convertToAdminUserResponse(user);
    }

    private User getUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
    }

    // private boolean filterUser(User user, String userId, String name, String email, String mobileNumber, 
    //                          Role role, UserStatus status, String sambhag, String district, String block) {
        
    //     if (userId != null && !userId.isEmpty()) {
    //         if (!user.getId().toLowerCase().contains(userId.toLowerCase())) {
    //             return false;
    //         }
    //     }
        
    //     if (name != null && !name.isEmpty()) {
    //         String fullName = (user.getName() + " " + (user.getSurname() != null ? user.getSurname() : "")).toLowerCase();
    //         if (!fullName.contains(name.toLowerCase())) {
    //             return false;
    //         }
    //     }
        
    //     if (email != null && !email.isEmpty() && user.getEmail() != null) {
    //         if (!user.getEmail().toLowerCase().contains(email.toLowerCase())) {
    //             return false;
    //         }
    //     }
        
    //     if (mobileNumber != null && !mobileNumber.isEmpty() && user.getMobileNumber() != null) {
    //         if (!user.getMobileNumber().contains(mobileNumber)) {
    //             return false;
    //         }
    //     }
        
    //     if (role != null && !role.equals(user.getRole())) {
    //         return false;
    //     }
        
    //     if (status != null && !status.equals(user.getStatus())) {
    //         return false;
    //     }
        
    //     if (sambhag != null && !sambhag.isEmpty()) {
    //         if (user.getDepartmentSambhag() == null || 
    //             !user.getDepartmentSambhag().getName().toLowerCase().contains(sambhag.toLowerCase())) {
    //             return false;
    //         }
    //     }
        
    //     if (district != null && !district.isEmpty()) {
    //         if (user.getDepartmentDistrict() == null || 
    //             !user.getDepartmentDistrict().getName().toLowerCase().contains(district.toLowerCase())) {
    //             return false;
    //         }
    //     }
        
    //     if (block != null && !block.isEmpty()) {
    //         if (user.getDepartmentBlock() == null || 
    //             !user.getDepartmentBlock().getName().toLowerCase().contains(block.toLowerCase())) {
    //             return false;
    //         }
    //     }
        
    //     return true;
    // }
private int getRequiredExcelColumn(Map<String, Integer> columnMap, String columnName) {
    Integer index = columnMap.get(columnName.toLowerCase());

    if (index == null) {
        throw new IllegalArgumentException("Required Excel column missing: " + columnName);
    }

    return index;
}

private String getExcelCellValue(DataFormatter formatter, Row row, int columnIndex) {
    Cell cell = row.getCell(columnIndex);

    if (cell == null) {
        return "";
    }

    return formatter.formatCellValue(cell).trim();
}

private boolean isBlank(String value) {
    return value == null || value.trim().isEmpty();
}

private record ExcelLocationRow(
        String userId,
        String stateName,
        String sambhagName,
        String districtName,
        String blockName,
        int rowNumber
) {}

    private AdminUserResponse convertToAdminUserResponse(User user) {
        AdminUserResponse response = new AdminUserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setSurname(user.getSurname());
        response.setFatherName(user.getFatherName());
        response.setEmail(user.getEmail());
        response.setMobileNumber(user.getMobileNumber());
        response.setDateOfBirth(user.getDateOfBirth());
        
        // Location details
        response.setDepartmentState(user.getDepartmentState() != null ? user.getDepartmentState().getName() : null);
        response.setDepartmentSambhag(user.getDepartmentSambhag() != null ? user.getDepartmentSambhag().getName() : null);
        response.setDepartmentDistrict(user.getDepartmentDistrict() != null ? user.getDepartmentDistrict().getName() : null);
        response.setDepartmentBlock(user.getDepartmentBlock() != null ? user.getDepartmentBlock().getName() : null);
        // Personal details
response.setGender(user.getGender());
response.setMaritalStatus(user.getMaritalStatus());

// Nominee details
response.setNominee1Name(user.getNominee1Name());
response.setNominee1Relation(user.getNominee1Relation());
response.setNominee2Name(user.getNominee2Name());
response.setNominee2Relation(user.getNominee2Relation());
        // Professional details
        response.setDepartment(user.getDepartment());
        response.setDepartmentUniqueId(user.getDepartmentUniqueId());
        response.setSchoolOfficeName(user.getSchoolOfficeName());
        response.setSankulName(user.getSankulName());
        
        // System fields
        response.setRole(user.getRole());
        response.setStatus(user.getStatus() != null ? user.getStatus() : UserStatus.ACTIVE);
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        response.setLastLoginAt(user.getLastLoginAt());
        // Additional fields
        response.setHomeAddress(user.getHomeAddress());
        response.setPincode(user.getPincode());
        response.setJoiningDate(user.getJoiningDate());
        response.setRetirementDate(user.getRetirementDate());
        
        return response;
    }
}