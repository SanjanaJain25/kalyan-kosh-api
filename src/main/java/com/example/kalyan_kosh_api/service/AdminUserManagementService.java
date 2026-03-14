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
import java.util.stream.Collectors;

@Service
public class AdminUserManagementService {
    private static final String RESERVED_SUPER_ADMIN_ID = "PMUMS202502";
private static final Role RESERVED_SUPER_ADMIN_ROLE = Role.ROLE_SUPERADMIN;
private final PasswordEncoder passwordEncoder;
private final UserRepository userRepository;
private final ReceiptRepository receiptRepository;
private final ManagerAssignmentRepository managerAssignmentRepository;
private final ManagerQueryRepository managerQueryRepository;

 public AdminUserManagementService(
        PasswordEncoder passwordEncoder,
        UserRepository userRepository,
        ReceiptRepository receiptRepository,
        ManagerAssignmentRepository managerAssignmentRepository,
        ManagerQueryRepository managerQueryRepository) {
    this.passwordEncoder = passwordEncoder;
    this.userRepository = userRepository;
    this.receiptRepository = receiptRepository;
    this.managerAssignmentRepository = managerAssignmentRepository;
    this.managerQueryRepository = managerQueryRepository;
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

    long receiptCount = receiptRepository.countByUser(user);
    if (receiptCount > 0) {
        throw new IllegalArgumentException("Cannot permanently delete user because receipt records exist.");
    }

    // Delete manager assignments first
    managerAssignmentRepository.deleteByManager(user);
    managerAssignmentRepository.deleteByAssignedBy(user);

    // Delete manager queries first
    managerQueryRepository.deleteByCreatedBy(user);
    managerQueryRepository.deleteByAssignedTo(user);
    managerQueryRepository.deleteByRelatedUser(user);
    managerQueryRepository.deleteByResolvedBy(user);

    // Now delete user
    userRepository.delete(user);
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

    user.setPasswordHash(passwordEncoder.encode(newPassword)); // ✅ correct field in your project
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
        
        // Additional fields
        response.setHomeAddress(user.getHomeAddress());
        response.setPincode(user.getPincode());
        response.setJoiningDate(user.getJoiningDate());
        response.setRetirementDate(user.getRetirementDate());
        
        return response;
    }
}