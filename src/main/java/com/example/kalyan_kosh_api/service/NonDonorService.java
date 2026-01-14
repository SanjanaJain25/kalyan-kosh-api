package com.example.kalyan_kosh_api.service;

import com.example.kalyan_kosh_api.dto.PageResponse;
import com.example.kalyan_kosh_api.dto.UserResponse;
import com.example.kalyan_kosh_api.entity.User;
import com.example.kalyan_kosh_api.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NonDonorService {

    private final UserRepository userRepo;

    public NonDonorService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    /**
     * ✅ OPTIMIZED: Get non-donors using single SQL query
     * Much faster than loading all users + all donors into memory
     */
    public List<User> getNonDonors(int month, int year) {
        System.out.println("📋 Fetching non-donors for " + month + "/" + year + " (Optimized Query)");
        List<User> nonDonors = userRepo.findNonDonors(month, year);
        System.out.println("✅ Found " + nonDonors.size() + " non-donors");
        return nonDonors;
    }

    /**
     * ✅ OPTIMIZED + PAGINATED: Get non-donors with pagination
     * Best for large datasets (60,000+ users)
     */
    public PageResponse<UserResponse> getNonDonorsPaginated(int month, int year, int page, int size) {
        System.out.println("📋 Fetching non-donors (paginated) for " + month + "/" + year);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
        Page<User> userPage = userRepo.findNonDonorsPaginated(month, year, pageable);

        List<UserResponse> userResponses = userPage.getContent().stream()
                .map(this::toUserResponse)
                .collect(Collectors.toList());

        System.out.println("✅ Page " + page + ": " + userResponses.size() +
                           " non-donors (Total: " + userPage.getTotalElements() + ")");

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

    private UserResponse toUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setSurname(user.getSurname());
        response.setFatherName(user.getFatherName());
        response.setEmail(user.getEmail());
        response.setMobileNumber(user.getMobileNumber());
        response.setGender(user.getGender());
        response.setMaritalStatus(user.getMaritalStatus());
        response.setDateOfBirth(user.getDateOfBirth());
        response.setHomeAddress(user.getHomeAddress());
        response.setPincode(user.getPincode());
        response.setDepartment(user.getDepartment());
        response.setSchoolOfficeName(user.getSchoolOfficeName());
        response.setDepartmentUniqueId(user.getDepartmentUniqueId());
        response.setSankulName(user.getSankulName());
        response.setJoiningDate(user.getJoiningDate());
        response.setRetirementDate(user.getRetirementDate());
        response.setNominee1Name(user.getNominee1Name());
        response.setNominee1Relation(user.getNominee1Relation());
        response.setNominee2Name(user.getNominee2Name());
        response.setNominee2Relation(user.getNominee2Relation());
        response.setRole(user.getRole());
        response.setCreatedAt(user.getCreatedAt());

        // Location names
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

        return response;
    }
}
