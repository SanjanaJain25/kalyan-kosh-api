package com.example.kalyan_kosh_api.service;

import com.example.kalyan_kosh_api.dto.UpdateUserRequest;
import com.example.kalyan_kosh_api.dto.UserResponse;
import com.example.kalyan_kosh_api.entity.Block;
import com.example.kalyan_kosh_api.entity.District;
import com.example.kalyan_kosh_api.entity.Sambhag;
import com.example.kalyan_kosh_api.entity.State;
import com.example.kalyan_kosh_api.entity.User;
import com.example.kalyan_kosh_api.repository.BlockRepository;
import com.example.kalyan_kosh_api.repository.DistrictRepository;
import com.example.kalyan_kosh_api.repository.SambhagRepository;
import com.example.kalyan_kosh_api.repository.StateRepository;
import com.example.kalyan_kosh_api.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;  // Added missing import
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepo;
    private final BlockRepository blockRepo;
    private final DistrictRepository districtRepo;
    private final SambhagRepository sambhagRepo;
    private final StateRepository stateRepo;

    public UserService(UserRepository userRepo,
                       BlockRepository blockRepo,
                       DistrictRepository districtRepo,
                       SambhagRepository sambhagRepo,
                       StateRepository stateRepo) {
        this.userRepo = userRepo;
        this.blockRepo = blockRepo;
        this.districtRepo = districtRepo;
        this.sambhagRepo = sambhagRepo;
        this.stateRepo = stateRepo;
    }

    public UserResponse getUserById(String id) {
        System.out.println("🔍 Fetching user with ID: " + id + " WITH location relationships");

        User user = userRepo.findByIdWithLocations(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        System.out.println("✅ User loaded: " + user.getName());
        System.out.println("   State: " + (user.getDepartmentState() != null ? user.getDepartmentState().getName() : "NULL"));
        System.out.println("   Sambhag: " + (user.getDepartmentSambhag() != null ? user.getDepartmentSambhag().getName() : "NULL"));
        System.out.println("   District: " + (user.getDepartmentDistrict() != null ? user.getDepartmentDistrict().getName() : "NULL"));
        System.out.println("   Block: " + (user.getDepartmentBlock() != null ? user.getDepartmentBlock().getName() : "NULL"));

        return toUserResponse(user);
    }

    public List<UserResponse> getAllUsers() {
        System.out.println("📋 Fetching ALL users WITH location relationships");

        List<User> users = userRepo.findAllWithLocations();

        System.out.println("✅ Loaded " + users.size() + " users");

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
        if (req.getPhoneNumber() != null) user.setPhoneNumber(req.getPhoneNumber());
        if (req.getCountryCode() != null) user.setCountryCode(req.getCountryCode());
        if (req.getMobileNumber() != null) user.setMobileNumber(req.getMobileNumber());
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

        userRepo.save(user);

        return toUserResponse(user);
    }

    /**
     * Convert User entity to UserResponse DTO
     */
    private UserResponse toUserResponse(User user) {
        System.out.println("🔄 Converting User to UserResponse: " + user.getName());

        UserResponse response = new UserResponse();

        response.setId(user.getId());
        response.setName(user.getName());
        response.setSurname(user.getSurname());
        response.setFatherName(user.getFatherName());  // Added father name
        // Removed username - no longer exists
        response.setEmail(user.getEmail());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setMobileNumber(user.getMobileNumber());
        response.setGender(user.getGender());
        response.setMaritalStatus(user.getMaritalStatus());
        response.setHomeAddress(user.getHomeAddress());
        response.setDateOfBirth(user.getDateOfBirth());      // Added date of birth
        response.setJoiningDate(user.getJoiningDate());      // Added joining date
        response.setRetirementDate(user.getRetirementDate()); // Added retirement date
        response.setSchoolOfficeName(user.getSchoolOfficeName()); // पदस्थ स्कूल/कार्यालय का नाम
        response.setSankulName(user.getSankulName());        // संकुल का नाम
        response.setDepartment(user.getDepartment());
        response.setDepartmentUniqueId(user.getDepartmentUniqueId());

        // Convert entity relationships to string names with detailed logging
        System.out.println("📍 Converting location entities:");

        if (user.getDepartmentState() != null) {
            String stateName = user.getDepartmentState().getName();
            response.setDepartmentState(stateName);
            System.out.println("   ✅ State: " + stateName);
        } else {
            System.out.println("   ⚠️  State: NULL");
        }

        if (user.getDepartmentSambhag() != null) {
            String sambhagName = user.getDepartmentSambhag().getName();
            response.setDepartmentSambhag(sambhagName);
            System.out.println("   ✅ Sambhag: " + sambhagName);
        } else {
            System.out.println("   ⚠️  Sambhag: NULL");
        }

        if (user.getDepartmentDistrict() != null) {
            String districtName = user.getDepartmentDistrict().getName();
            response.setDepartmentDistrict(districtName);
            System.out.println("   ✅ District: " + districtName);
        } else {
            System.out.println("   ⚠️  District: NULL");
        }

        if (user.getDepartmentBlock() != null) {
            String blockName = user.getDepartmentBlock().getName();
            response.setDepartmentBlock(blockName);
            System.out.println("   ✅ Block: " + blockName);
        } else {
            System.out.println("   ⚠️  Block: NULL");
        }

        response.setNominee1Name(user.getNominee1Name());
        response.setNominee1Relation(user.getNominee1Relation());
        response.setNominee2Name(user.getNominee2Name());
        response.setNominee2Relation(user.getNominee2Relation());
        response.setAcceptedTerms(user.isAcceptedTerms());
        response.setRole(user.getRole());
        response.setCreatedAt(user.getCreatedAt());

        System.out.println("✅ UserResponse created successfully");

        return response;
    }
}
