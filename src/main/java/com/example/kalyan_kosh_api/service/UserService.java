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
        User user = userRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        return toUserResponse(user);
    }

    public List<UserResponse> getAllUsers() {
        return userRepo.findAll()
                .stream()
                .map(this::toUserResponse)
                .toList();
    }

    public UserResponse updateUser(String id, UpdateUserRequest req) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Update simple fields
        if (req.getName() != null) user.setName(req.getName());
        if (req.getSurname() != null) user.setSurname(req.getSurname());
        if (req.getEmail() != null) user.setEmail(req.getEmail());
        if (req.getPhoneNumber() != null) user.setPhoneNumber(req.getPhoneNumber());
        if (req.getCountryCode() != null) user.setCountryCode(req.getCountryCode());
        if (req.getMobileNumber() != null) user.setMobileNumber(req.getMobileNumber());
        if (req.getGender() != null) user.setGender(req.getGender());
        if (req.getMaritalStatus() != null) user.setMaritalStatus(req.getMaritalStatus());
        if (req.getHomeAddress() != null) user.setHomeAddress(req.getHomeAddress());
        if (req.getSchoolOfficeName() != null) user.setSchoolOfficeName(req.getSchoolOfficeName());
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
        UserResponse response = new UserResponse();

        response.setId(user.getId());
        response.setName(user.getName());
        response.setSurname(user.getSurname());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setMobileNumber(user.getMobileNumber());
        response.setGender(user.getGender());
        response.setMaritalStatus(user.getMaritalStatus());
        response.setHomeAddress(user.getHomeAddress());
        response.setSchoolOfficeName(user.getSchoolOfficeName());
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
        response.setAcceptedTerms(user.isAcceptedTerms());
        response.setRole(user.getRole());
        response.setCreatedAt(user.getCreatedAt());

        return response;
    }
}
