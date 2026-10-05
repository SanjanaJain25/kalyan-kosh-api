package com.example.kalyan_kosh_api.service;

import com.example.kalyan_kosh_api.dto.PageResponse;
import com.example.kalyan_kosh_api.dto.PublicBinUserResponse;
import com.example.kalyan_kosh_api.entity.User;
import com.example.kalyan_kosh_api.repository.ReceiptRepository;
import com.example.kalyan_kosh_api.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class PublicBinUserService {

    private static final int MAX_PAGE_SIZE = 200;

    private final UserRepository userRepository;
    private final ReceiptRepository receiptRepository;

    public PublicBinUserService(
            UserRepository userRepository,
            ReceiptRepository receiptRepository) {
        this.userRepository = userRepository;
        this.receiptRepository = receiptRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<PublicBinUserResponse> getPublicBinUsers(
            String sambhagId,
            String districtId,
            String blockId,
            String name,
            String userId,
            int page,
            int size) {

        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);

        UUID sambhagUuid = parseUuidOrNull(sambhagId, "Invalid sambhagId");
        UUID districtUuid = parseUuidOrNull(districtId, "Invalid districtId");
        UUID blockUuid = parseUuidOrNull(blockId, "Invalid blockId");
        String cleanName = clean(name);
        String cleanUserId = clean(userId);

        Pageable pageable = PageRequest.of(
                safePage,
                safeSize,
                Sort.by(Sort.Direction.DESC, "deletedAt")
                        .and(Sort.by(Sort.Direction.DESC, "updatedAt"))
        );

        Page<User> userPage = userRepository.searchPublicBinUsers(
                sambhagUuid,
                districtUuid,
                blockUuid,
                cleanName,
                cleanUserId,
                pageable
        );

        List<String> userIds = userPage.getContent().stream()
                .map(User::getId)
                .toList();

        Map<String, SahyogSummary> sahyogByUser = loadVerifiedSahyogSummary(userIds);

        List<PublicBinUserResponse> content = userPage.getContent().stream()
                .map(user -> toResponse(
                        user,
                        sahyogByUser.getOrDefault(user.getId(), SahyogSummary.EMPTY)
                ))
                .toList();

        return new PageResponse<>(
                content,
                userPage.getNumber(),
                userPage.getSize(),
                userPage.getTotalElements(),
                userPage.getTotalPages(),
                userPage.isLast(),
                userPage.isFirst()
        );
    }

    private Map<String, SahyogSummary> loadVerifiedSahyogSummary(List<String> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, SahyogSummary> summaries = new HashMap<>();
        for (Object[] row : receiptRepository.sumVerifiedSahyogByUserIds(userIds)) {
            if (row == null || row.length < 2 || row[0] == null) {
                continue;
            }

            String id = String.valueOf(row[0]);
            double amount = row[1] instanceof Number
                    ? ((Number) row[1]).doubleValue()
                    : 0.0;
            long count = row.length > 2 && row[2] instanceof Number
                    ? ((Number) row[2]).longValue()
                    : 0L;
            summaries.put(id, new SahyogSummary(amount, count));
        }
        return summaries;
    }

    private PublicBinUserResponse toResponse(User user, SahyogSummary sahyog) {
        return PublicBinUserResponse.builder()
                .id(user.getId())
                .registrationNumber(user.getId())
                .name(user.getName())
                .surname(user.getSurname())
                .department(user.getDepartment())
                .departmentState(user.getDepartmentState() != null ? user.getDepartmentState().getName() : null)
                .departmentSambhag(user.getDepartmentSambhag() != null ? user.getDepartmentSambhag().getName() : null)
                .departmentDistrict(user.getDepartmentDistrict() != null ? user.getDepartmentDistrict().getName() : null)
                .departmentBlock(user.getDepartmentBlock() != null ? user.getDepartmentBlock().getName() : null)
                .schoolOfficeName(user.getSchoolOfficeName())
                .sahyogCount(sahyog.count())
                .totalSahyog(sahyog.total())
                .build();
    }

    private record SahyogSummary(double total, long count) {
        private static final SahyogSummary EMPTY = new SahyogSummary(0.0, 0L);
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }
        String cleaned = value.trim();
        return cleaned.isEmpty() ? null : cleaned;
    }

    private UUID parseUuidOrNull(String value, String errorMessage) {
        String cleaned = clean(value);
        if (cleaned == null) {
            return null;
        }
        try {
            return UUID.fromString(cleaned);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(errorMessage);
        }
    }


}
