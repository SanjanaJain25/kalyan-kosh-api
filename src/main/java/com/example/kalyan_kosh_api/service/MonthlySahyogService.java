package com.example.kalyan_kosh_api.service;

import com.example.kalyan_kosh_api.dto.AdminDashboardSummaryResponse;
import com.example.kalyan_kosh_api.dto.DonorResponse;
import com.example.kalyan_kosh_api.dto.PageResponse;
import com.example.kalyan_kosh_api.dto.UserResponse;
import com.example.kalyan_kosh_api.entity.MonthlySahyog;
import com.example.kalyan_kosh_api.entity.Receipt;
import com.example.kalyan_kosh_api.entity.Role;
import com.example.kalyan_kosh_api.entity.SahyogStatus;
import com.example.kalyan_kosh_api.entity.User;
import com.example.kalyan_kosh_api.repository.DeathCaseRepository;
import com.example.kalyan_kosh_api.repository.MonthlySahyogRepository;
import com.example.kalyan_kosh_api.repository.ReceiptRepository;
import com.example.kalyan_kosh_api.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class MonthlySahyogService {

    private final MonthlySahyogRepository sahyogRepo;
    private final UserRepository userRepo;
    private final DeathCaseRepository deathCaseRepo;
    private final ReceiptRepository receiptRepo;

    public MonthlySahyogService(
            MonthlySahyogRepository sahyogRepo,
            UserRepository userRepo,
            DeathCaseRepository deathCaseRepo,
            ReceiptRepository receiptRepo) {

        this.sahyogRepo = sahyogRepo;
        this.userRepo = userRepo;
        this.deathCaseRepo = deathCaseRepo;
        this.receiptRepo = receiptRepo;
    }

    public MonthlySahyog generate(LocalDate sahyogDate) {

        sahyogRepo.findBySahyogDate(sahyogDate)
                .ifPresent(ms -> {
                    throw new IllegalStateException(
                            "Monthly Sahyog already generated for this date");
                });

        int totalMembers = (int) userRepo.count();

        MonthlySahyog sahyog = MonthlySahyog.builder()
                .sahyogDate(sahyogDate)
                .totalMembers(totalMembers)
                .totalDeathCases(0)
                .receivedAmount(0)
                .status(SahyogStatus.OPEN)
                .generatedAt(Instant.now())
                .build();

        return sahyogRepo.save(sahyog);
    }


    public MonthlySahyog updateDeathCases(LocalDate sahyogDate) {

        MonthlySahyog sahyog = sahyogRepo.findBySahyogDate(sahyogDate)
                .orElseThrow(() ->
                        new IllegalStateException("Monthly Sahyog not generated"));

        if (sahyog.getStatus() == SahyogStatus.FROZEN) {
            throw new IllegalStateException(
                    "Month is frozen. Update not allowed.");
        }

        // Count death cases for the date range (entire month)
        LocalDate startDate = sahyogDate.withDayOfMonth(1);
        LocalDate endDate = sahyogDate.withDayOfMonth(sahyogDate.lengthOfMonth());

        int deathCases = (int) deathCaseRepo.countByCaseDateBetween(startDate, endDate);

        sahyog.setTotalDeathCases(deathCases);

        return sahyogRepo.save(sahyog);
    }


    public List<UserResponse> getNonDonors(LocalDate sahyogDate) {

        LocalDate startDate = sahyogDate.withDayOfMonth(1);
        LocalDate endDate = sahyogDate.withDayOfMonth(sahyogDate.lengthOfMonth());

        // ✅ Single query to get all donor user IDs (instead of N queries)
        Set<String> donorUserIds = receiptRepo.findDonorUserIdsByDateRange(startDate, endDate);

        // ✅ Filter non-donors efficiently with O(1) Set lookup
        return userRepo.findByRole(Role.ROLE_USER).stream()
                .filter(user -> !donorUserIds.contains(user.getId()))
                .map(this::toUserResponse)
                .toList();
    }

    /**
     * ✅ FAST PAGINATED: Get non-donors with pagination using database-level query
     */
    public PageResponse<UserResponse> getNonDonorsPaginated(LocalDate sahyogDate, int page, int size) {
        int month = sahyogDate.getMonthValue();
        int year = sahyogDate.getYear();

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<User> userPage = userRepo.findNonDonorsPaginated(month, year, pageable);

        List<UserResponse> userResponses = userPage.getContent().stream()
                .map(this::toUserResponse)
                .toList();

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

    public void exportNonDonorsCsv(LocalDate sahyogDate, PrintWriter writer) {

        List<UserResponse> nonDonors = getNonDonors(sahyogDate);

        writer.println("UserId,Name,Surname,Email,Status");

        for (UserResponse r : nonDonors) {
            writer.printf(
                    "%s,%s,%s,%s,%s%n",
                    r.getId(),
                    r.getName() != null ? r.getName() : "",
                    r.getSurname() != null ? r.getSurname() : "",
                    r.getEmail() != null ? r.getEmail() : "",
                    "NON_DONOR"
            );
        }

        writer.flush();
    }

    /**
     * ✅ SUPER FAST PAGINATED: Get donors with 250 records per page
     */
    public PageResponse<DonorResponse> getDonorsPaginated(LocalDate sahyogDate, int page, int size) {
        LocalDate startDate = sahyogDate.withDayOfMonth(1);
        LocalDate endDate = sahyogDate.withDayOfMonth(sahyogDate.lengthOfMonth());

        Pageable pageable = PageRequest.of(page, size);
        Page<Object[]> donorPage = receiptRepo.findDonorsPaginatedNative(startDate, endDate, pageable);

        List<DonorResponse> donors = donorPage.getContent().stream()
                .map(row -> DonorResponse.builder()
                        .registrationNumber((String) row[0])  // user_id (पंजीकरण संख्या)
                        .name(row[2] + (row[3] != null ? " " + row[3] : ""))  // name + surname
                        .department((String) row[4])
                        .state((String) row[5])
                        .sambhag((String) row[6])
                        .district((String) row[7])
                        .block((String) row[8])
                        .schoolName((String) row[9])
                        .beneficiary((String) row[10])
                        .receiptUploadDate(row[11] != null ? ((java.sql.Timestamp) row[11]).toInstant() : null)
                        .build())
                .toList();

        return new PageResponse<>(
                donors,
                donorPage.getNumber(),
                donorPage.getSize(),
                donorPage.getTotalElements(),
                donorPage.getTotalPages(),
                donorPage.isLast(),
                donorPage.isFirst()
        );
    }

    /**
     * @deprecated Use getDonorsPaginated instead for better performance
     */
    @Deprecated
    public List<DonorResponse> getDonors(LocalDate sahyogDate) {
        return getDonorsPaginated(sahyogDate, 0, 250).getContent();
    }

    /**
     * ✅ Search donors by full name (name + surname) and/or mobile number and/or userId
     */
    public PageResponse<DonorResponse> searchDonors(LocalDate sahyogDate, String name, String mobile, String userId, int page, int size) {
        LocalDate startDate = sahyogDate.withDayOfMonth(1);
        LocalDate endDate = sahyogDate.withDayOfMonth(sahyogDate.lengthOfMonth());

        // Clean input - null if empty
        String cleanName = (name != null && !name.trim().isEmpty()) ? name.trim() : null;
        String cleanMobile = (mobile != null && !mobile.trim().isEmpty()) ? mobile.trim() : null;
        String cleanUserId = (userId != null && !userId.trim().isEmpty()) ? userId.trim() : null;

        Pageable pageable = PageRequest.of(page, size);
        Page<Object[]> donorPage = receiptRepo.searchDonorsNative(startDate, endDate, cleanName, cleanMobile, cleanUserId, pageable);

        List<DonorResponse> donors = donorPage.getContent().stream()
                .map(row -> DonorResponse.builder()
                        .registrationNumber((String) row[0])
                        .name(row[2] + (row[3] != null ? " " + row[3] : ""))
                        .department((String) row[4])
                        .state((String) row[5])
                        .sambhag((String) row[6])
                        .district((String) row[7])
                        .block((String) row[8])
                        .schoolName((String) row[9])
                        .beneficiary((String) row[10])
                        .receiptUploadDate(row[11] != null ? ((java.sql.Timestamp) row[11]).toInstant() : null)
                        .build())
                .toList();

        return new PageResponse<>(
                donors,
                donorPage.getNumber(),
                donorPage.getSize(),
                donorPage.getTotalElements(),
                donorPage.getTotalPages(),
                donorPage.isLast(),
                donorPage.isFirst()
        );
    }

    /**
     * ✅ Search non-donors by full name (name + surname) and/or mobile number and/or userId
     */
    public PageResponse<UserResponse> searchNonDonors(LocalDate sahyogDate, String name, String mobile, String userId, int page, int size) {
        int month = sahyogDate.getMonthValue();
        int year = sahyogDate.getYear();

        // Clean input - null if empty
        String cleanName = (name != null && !name.trim().isEmpty()) ? name.trim() : null;
        String cleanMobile = (mobile != null && !mobile.trim().isEmpty()) ? mobile.trim() : null;
        String cleanUserId = (userId != null && !userId.trim().isEmpty()) ? userId.trim() : null;

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<User> userPage = userRepo.searchNonDonorsPaginated(month, year, cleanName, cleanMobile, cleanUserId, pageable);

        List<UserResponse> userResponses = userPage.getContent().stream()
                .map(this::toUserResponse)
                .toList();

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
     * Convert Receipt to DonorResponse with all required fields:
     * पंजीकरण संख्या | नाम | विभाग | राज्य | संभाग | जिला | ब्लॉक | स्कूल का नाम | लाभार्थी | रसीद अपलोड दिनांक
     */
    private DonorResponse toDonorResponse(Receipt receipt) {
        User user = receipt.getUser();

        return DonorResponse.builder()
                .registrationNumber(user.getDepartmentUniqueId())  // पंजीकरण संख्या
                .name(user.getName() + (user.getSurname() != null ? " " + user.getSurname() : ""))  // नाम
                .department(user.getDepartment())  // विभाग
                .state(user.getDepartmentState() != null ? user.getDepartmentState().getName() : null)  // राज्य
                .sambhag(user.getDepartmentSambhag() != null ? user.getDepartmentSambhag().getName() : null)  // संभाग
                .district(user.getDepartmentDistrict() != null ? user.getDepartmentDistrict().getName() : null)  // जिला
                .block(user.getDepartmentBlock() != null ? user.getDepartmentBlock().getName() : null)  // ब्लॉक
                .schoolName(user.getSchoolOfficeName())  // स्कूल का नाम
                .beneficiary(receipt.getDeathCase() != null ? receipt.getDeathCase().getDeceasedName() : null)  // लाभार्थी
                .receiptUploadDate(receipt.getUploadedAt())  // रसीद अपलोड दिनांक
                .build();
    }


    public AdminDashboardSummaryResponse getDashboardSummary(LocalDate sahyogDate) {

        LocalDate startDate = sahyogDate.withDayOfMonth(1);
        LocalDate endDate = sahyogDate.withDayOfMonth(sahyogDate.lengthOfMonth());

        long totalMembers = userRepo.count();

        long totalDeathCases = deathCaseRepo.countByCaseDateBetween(startDate, endDate);

        long totalDonors = receiptRepo.countDonorsByDateRange(startDate, endDate);

        long totalNonDonors = totalMembers - totalDonors;

        double totalReceivedAmount = receiptRepo.sumVerifiedAmountByDateRange(startDate, endDate);

        return new AdminDashboardSummaryResponse(
                sahyogDate,
                totalMembers,
                totalDeathCases,
                totalDonors,
                totalNonDonors,
                totalReceivedAmount
        );
    }

    public MonthlySahyog freezeMonth(LocalDate sahyogDate) {

        MonthlySahyog sahyog = getOpenSahyog(sahyogDate);

        sahyog.setStatus(SahyogStatus.FROZEN);
        sahyog.setFrozenAt(Instant.now());

        return sahyogRepo.save(sahyog);
    }

    private MonthlySahyog getOpenSahyog(LocalDate sahyogDate) {

        MonthlySahyog sahyog = sahyogRepo.findBySahyogDate(sahyogDate)
                .orElseThrow(() ->
                        new IllegalStateException("Monthly Sahyog not generated"));

        if (sahyog.getStatus() == SahyogStatus.FROZEN) {
            throw new IllegalStateException(
                    "Month is frozen. Operation not allowed.");
        }

        return sahyog;
    }

    private UserResponse toUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setSurname(user.getSurname());
        response.setFatherName(user.getFatherName());
        response.setEmail(user.getEmail());
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
