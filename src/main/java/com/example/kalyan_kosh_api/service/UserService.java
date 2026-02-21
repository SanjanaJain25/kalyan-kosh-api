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

    public UserService(UserRepository userRepo,
                       BlockRepository blockRepo,
                       DistrictRepository districtRepo,
                       SambhagRepository sambhagRepo,
                       StateRepository stateRepo,
                       PasswordEncoder passwordEncoder,
                       IdGeneratorService idGeneratorService,
                       EmailService emailService) {
        this.userRepo = userRepo;
        this.blockRepo = blockRepo;
        this.districtRepo = districtRepo;
        this.sambhagRepo = sambhagRepo;
        this.stateRepo = stateRepo;
        this.passwordEncoder = passwordEncoder;
        this.idGeneratorService = idGeneratorService;
        this.emailService = emailService;
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

    public UserResponse updateUser(String id, UpdateUserRequest req) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Update simple fields
        if (req.getName() != null) user.setName(req.getName());
        if (req.getSurname() != null) user.setSurname(req.getSurname());
        if (req.getFatherName() != null) user.setFatherName(req.getFatherName());  // Added father name
        if (req.getEmail() != null) user.setEmail(req.getEmail());
        if (req.getCountryCode() != null) user.setCountryCode(req.getCountryCode());
        if (req.getMobileNumber() != null) user.setMobileNumber(req.getMobileNumber());
        if (req.getPincode() != null) user.setPincode(req.getPincode());
        if (req.getGender() != null) user.setGender(req.getGender());
        if (req.getMaritalStatus() != null) user.setMaritalStatus(req.getMaritalStatus());
        if (req.getHomeAddress() != null) user.setHomeAddress(req.getHomeAddress());

        // Handle date fields
        if (req.getDateOfBirth() != null && !req.getDateOfBirth().isEmpty()) {
            try {
                user.setDateOfBirth(LocalDate.parse(req.getDateOfBirth()));
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid date format for dateOfBirth. Use yyyy-MM-dd");
            }
        }

        if (req.getJoiningDate() != null && !req.getJoiningDate().isEmpty()) {
            try {
                user.setJoiningDate(LocalDate.parse(req.getJoiningDate()));
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid date format for joiningDate. Use yyyy-MM-dd");
            }
        }

        if (req.getRetirementDate() != null && !req.getRetirementDate().isEmpty()) {
            try {
                user.setRetirementDate(LocalDate.parse(req.getRetirementDate()));
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid date format for retirementDate. Use yyyy-MM-dd");
            }
        }

        if (req.getSchoolOfficeName() != null) user.setSchoolOfficeName(req.getSchoolOfficeName());
        if (req.getSankulName() != null) user.setSankulName(req.getSankulName());
        if (req.getDepartment() != null) user.setDepartment(req.getDepartment());
        if (req.getDepartmentUniqueId() != null) user.setDepartmentUniqueId(req.getDepartmentUniqueId());

        // Update location hierarchy
        State state = user.getDepartmentState();
        Sambhag sambhag = user.getDepartmentSambhag();
        District district = user.getDepartmentDistrict();

        // Update state if provided
        if (req.getDepartmentState() != null && !req.getDepartmentState().isEmpty()) {
            state = stateRepo.findByName(req.getDepartmentState())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Invalid state: " + req.getDepartmentState()));
            user.setDepartmentState(state);
        }

        // Update sambhag if provided
        if (req.getDepartmentSambhag() != null && !req.getDepartmentSambhag().isEmpty()) {
            if (state == null && req.getDepartmentState() != null) {
                state = stateRepo.findByName(req.getDepartmentState())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Invalid state: " + req.getDepartmentState()));
            }
            if (state != null) {
                final String stateName = state.getName();
                sambhag = sambhagRepo.findByNameAndState(req.getDepartmentSambhag(), state)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Invalid sambhag: " + req.getDepartmentSambhag() +
                                " for state: " + stateName));
                user.setDepartmentSambhag(sambhag);
            }
        }

        // Update district entity if provided
        if (req.getDepartmentDistrict() != null && !req.getDepartmentDistrict().isEmpty()) {
            if (sambhag == null && req.getDepartmentSambhag() != null && state != null) {
                sambhag = sambhagRepo.findByNameAndState(req.getDepartmentSambhag(), state)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Invalid sambhag: " + req.getDepartmentSambhag()));
            }

            if (sambhag != null) {
                final String sambhagName = sambhag.getName();
                district = districtRepo.findByNameAndSambhag(req.getDepartmentDistrict(), sambhag)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Invalid district: " + req.getDepartmentDistrict() +
                                " for sambhag: " + sambhagName));
            } else {
                district = districtRepo.findByName(req.getDepartmentDistrict())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Invalid district: " + req.getDepartmentDistrict()));
            }
            user.setDepartmentDistrict(district);
        }

        // Update block entity if provided
        if (req.getDepartmentBlock() != null && !req.getDepartmentBlock().isEmpty()) {
            if (district == null && req.getDepartmentDistrict() != null) {
                district = districtRepo.findByName(req.getDepartmentDistrict())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Invalid district: " + req.getDepartmentDistrict()));
            }

            if (district != null) {
                final String districtName = district.getName();
                Block block = blockRepo.findByNameAndDistrict(req.getDepartmentBlock(), district)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Invalid block: " + req.getDepartmentBlock() +
                                " for district: " + districtName));
                user.setDepartmentBlock(block);
            }
        }

        if (req.getNominee1Name() != null) user.setNominee1Name(req.getNominee1Name());
        if (req.getNominee1Relation() != null) user.setNominee1Relation(req.getNominee1Relation());
        if (req.getNominee2Name() != null) user.setNominee2Name(req.getNominee2Name());
        if (req.getNominee2Relation() != null) user.setNominee2Relation(req.getNominee2Relation());

        // Set updatedAt timestamp (createdAt should never be modified during update)
        user.setUpdatedAt(Instant.now());

        userRepo.save(user);

        return toUserResponse(user);
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
        String fullName = savedUser.getName() + (savedUser.getSurname() != null ? " " + savedUser.getSurname() : "");
        emailService.sendRegistrationConfirmationEmail(savedUser.getEmail(), fullName, savedUser.getId());

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
