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

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminUserManagementService {

    private final UserRepository userRepository;

    public AdminUserManagementService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Get all users with pagination and filters
     */
    public AdminUserListResponse getAllUsers(int page, int size, String name, String email, 
                                           String mobileNumber, Role role, UserStatus status,
                                           String sambhag, String district, String block) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        // For now, using simple findAll - can be enhanced with specifications for filtering
        Page<User> userPage = userRepository.findAllWithLocations(pageable);
        
        List<AdminUserResponse> users = userPage.getContent().stream()
                .filter(user -> filterUser(user, name, email, mobileNumber, role, status, sambhag, district, block))
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
        user.setStatus(UserStatus.DELETED);
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);
    }

    /**
     * Update user role
     */
    @Transactional
    public void updateUserRole(String userId, UpdateUserRoleRequest request) {
        User user = getUserById(userId);
        user.setRole(request.getRole());
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);
    }

    /**
     * Get user by ID
     */
    public AdminUserResponse getUserByIdResponse(String userId) {
        User user = getUserById(userId);
        return convertToAdminUserResponse(user);
    }

    private User getUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
    }

    private boolean filterUser(User user, String name, String email, String mobileNumber, 
                             Role role, UserStatus status, String sambhag, String district, String block) {
        
        if (name != null && !name.isEmpty()) {
            String fullName = (user.getName() + " " + (user.getSurname() != null ? user.getSurname() : "")).toLowerCase();
            if (!fullName.contains(name.toLowerCase())) {
                return false;
            }
        }
        
        if (email != null && !email.isEmpty() && user.getEmail() != null) {
            if (!user.getEmail().toLowerCase().contains(email.toLowerCase())) {
                return false;
            }
        }
        
        if (mobileNumber != null && !mobileNumber.isEmpty() && user.getMobileNumber() != null) {
            if (!user.getMobileNumber().contains(mobileNumber)) {
                return false;
            }
        }
        
        if (role != null && !role.equals(user.getRole())) {
            return false;
        }
        
        if (status != null && !status.equals(user.getStatus())) {
            return false;
        }
        
        if (sambhag != null && !sambhag.isEmpty()) {
            if (user.getDepartmentSambhag() == null || 
                !user.getDepartmentSambhag().getName().toLowerCase().contains(sambhag.toLowerCase())) {
                return false;
            }
        }
        
        if (district != null && !district.isEmpty()) {
            if (user.getDepartmentDistrict() == null || 
                !user.getDepartmentDistrict().getName().toLowerCase().contains(district.toLowerCase())) {
                return false;
            }
        }
        
        if (block != null && !block.isEmpty()) {
            if (user.getDepartmentBlock() == null || 
                !user.getDepartmentBlock().getName().toLowerCase().contains(block.toLowerCase())) {
                return false;
            }
        }
        
        return true;
    }

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