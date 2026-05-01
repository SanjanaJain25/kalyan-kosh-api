package com.example.kalyan_kosh_api.service;

import com.example.kalyan_kosh_api.dto.AdminReportUserRowResponse;
import com.example.kalyan_kosh_api.dto.manager.ManagerAreaScope;
import com.example.kalyan_kosh_api.entity.Block;
import com.example.kalyan_kosh_api.entity.District;
import com.example.kalyan_kosh_api.entity.Sambhag;
import com.example.kalyan_kosh_api.entity.State;
import com.example.kalyan_kosh_api.entity.User;
import com.example.kalyan_kosh_api.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

@Service
public class AdminReportService {

    private final UserRepository userRepository;
    private final ManagerScopeService managerScopeService;

    public AdminReportService(
            UserRepository userRepository,
            ManagerScopeService managerScopeService
    ) {
        this.userRepository = userRepository;
        this.managerScopeService = managerScopeService;
    }

    public Page<AdminReportUserRowResponse> getUsersByJoiningDate(
            User currentUser,
            LocalDate fromDate,
            LocalDate toDate,
            String sambhagId,
            String districtId,
            String blockId,
            String search,
            Pageable pageable
    ) {
        ManagerAreaScope scope = managerScopeService.buildAreaScope(currentUser);

        Page<User> users = userRepository.findUsersForJoiningDateReport(
                fromDate,
                toDate,
                blankToNull(sambhagId),
                blankToNull(districtId),
                blankToNull(blockId),
                blankToNull(search),
                scope.isUnrestricted(),
                safeScopeList(scope.getSambhagIds()),
                safeScopeList(scope.getDistrictIds()),
                safeScopeList(scope.getBlockIds()),
                pageable
        );

        return users.map(this::toRow);
    }

    public Page<AdminReportUserRowResponse> getUsersByRetirementDate(
            User currentUser,
            LocalDate fromDate,
            LocalDate toDate,
            String sambhagId,
            String districtId,
            String blockId,
            String search,
            Pageable pageable
    ) {
        ManagerAreaScope scope = managerScopeService.buildAreaScope(currentUser);

        Page<User> users = userRepository.findUsersForRetirementDateReport(
                fromDate,
                toDate,
                blankToNull(sambhagId),
                blankToNull(districtId),
                blankToNull(blockId),
                blankToNull(search),
                scope.isUnrestricted(),
                safeScopeList(scope.getSambhagIds()),
                safeScopeList(scope.getDistrictIds()),
                safeScopeList(scope.getBlockIds()),
                pageable
        );

        return users.map(this::toRow);
    }

    public Page<AdminReportUserRowResponse> getNoLoginThreeMonthsUsers(
            User currentUser,
            String sambhagId,
            String districtId,
            String blockId,
            String search,
            Pageable pageable
    ) {
        ManagerAreaScope scope = managerScopeService.buildAreaScope(currentUser);

        Instant cutoff = Instant.now().minus(90, ChronoUnit.DAYS);

        Page<User> users = userRepository.findUsersNotLoggedInSinceReport(
                cutoff,
                blankToNull(sambhagId),
                blankToNull(districtId),
                blankToNull(blockId),
                blankToNull(search),
                scope.isUnrestricted(),
                safeScopeList(scope.getSambhagIds()),
                safeScopeList(scope.getDistrictIds()),
                safeScopeList(scope.getBlockIds()),
                pageable
        );

        return users.map(this::toRow);
    }

    public Page<AdminReportUserRowResponse> getNoSahyogTwoMonthsUsers(
            User currentUser,
            String sambhagId,
            String districtId,
            String blockId,
            String search,
            Pageable pageable
    ) {
        ManagerAreaScope scope = managerScopeService.buildAreaScope(currentUser);

        LocalDate cutoffDate = LocalDate.now().minusMonths(2);

        Page<User> users = userRepository.findUsersNotContributedSinceReport(
                cutoffDate,
                blankToNull(sambhagId),
                blankToNull(districtId),
                blankToNull(blockId),
                blankToNull(search),
                scope.isUnrestricted(),
                safeScopeList(scope.getSambhagIds()),
                safeScopeList(scope.getDistrictIds()),
                safeScopeList(scope.getBlockIds()),
                pageable
        );

        return users.map(this::toRow);
    }

    private AdminReportUserRowResponse toRow(User user) {
        return AdminReportUserRowResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .surname(user.getSurname())
                .fullName(buildFullName(user.getName(), user.getSurname()))
                .fatherName(user.getFatherName())
                .email(user.getEmail())
                .mobileNumber(user.getMobileNumber())
                .department(user.getDepartment())
                .departmentUniqueId(user.getDepartmentUniqueId())
                .schoolOfficeName(user.getSchoolOfficeName())
                .sankulName(user.getSankulName())
                .departmentState(getStateName(user.getDepartmentState()))
                .departmentSambhag(getSambhagName(user.getDepartmentSambhag()))
                .departmentDistrict(getDistrictName(user.getDepartmentDistrict()))
                .departmentBlock(getBlockName(user.getDepartmentBlock()))
                .joiningDate(user.getJoiningDate())
                .retirementDate(user.getRetirementDate())
                .dateOfBirth(user.getDateOfBirth())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .role(user.getRole() != null ? user.getRole().name() : null)
                .status(user.getStatus() != null ? user.getStatus().name() : null)
                .build();
    }

    private String buildFullName(String name, String surname) {
        String first = name == null ? "" : name.trim();
        String last = surname == null ? "" : surname.trim();

        String fullName = (first + " " + last).trim();
        return fullName.isEmpty() ? null : fullName;
    }

    private String getStateName(State state) {
        return state != null ? state.getName() : null;
    }

    private String getSambhagName(Sambhag sambhag) {
        return sambhag != null ? sambhag.getName() : null;
    }

    private String getDistrictName(District district) {
        return district != null ? district.getName() : null;
    }

    private String getBlockName(Block block) {
        return block != null ? block.getName() : null;
    }

    private String blankToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    private List<String> safeScopeList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return Collections.singletonList("__NO_SCOPE__");
        }
        return values;
    }
}