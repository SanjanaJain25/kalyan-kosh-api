package com.example.kalyan_kosh_api.service;

import com.example.kalyan_kosh_api.dto.AdminDashboardSummaryResponse;
import com.example.kalyan_kosh_api.dto.NonDonorResponse;
import com.example.kalyan_kosh_api.entity.MonthlySahyog;
import com.example.kalyan_kosh_api.entity.Role;
import com.example.kalyan_kosh_api.entity.SahyogStatus;
import com.example.kalyan_kosh_api.repository.DeathCaseRepository;
import com.example.kalyan_kosh_api.repository.MonthlySahyogRepository;
import com.example.kalyan_kosh_api.repository.ReceiptRepository;
import com.example.kalyan_kosh_api.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

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


    public List<NonDonorResponse> getNonDonors(LocalDate sahyogDate) {

        LocalDate startDate = sahyogDate.withDayOfMonth(1);
        LocalDate endDate = sahyogDate.withDayOfMonth(sahyogDate.lengthOfMonth());

        return userRepo.findAll().stream()
                .filter(u -> u.getRole() == Role.ROLE_USER)
                .map(user -> {

                    double paidAmount = receiptRepo.sumPaidAmountByDateRange(
                            user.getId(), startDate, endDate);

                    String status = paidAmount > 0 ? "DONOR" : "NON_DONOR";

                    return new NonDonorResponse(
                            user.getId(),
                            user.getId(),
                            paidAmount,
                            status
                    );
                })
                .filter(r -> "NON_DONOR".equals(r.getStatus()))
                .toList();
    }

    public void exportNonDonorsCsv(LocalDate sahyogDate, PrintWriter writer) {

        List<NonDonorResponse> nonDonors = getNonDonors(sahyogDate);

        writer.println("UserId,SahyogDate,PaidAmount,Status");

        for (NonDonorResponse r : nonDonors) {
            writer.printf(
                    "%s,%s,%.2f,%s%n",
                    r.getUserId(),
                    sahyogDate,
                    r.getPaidAmount(),
                    r.getStatus()
            );
        }

        writer.flush();
    }

    public List<NonDonorResponse> getDonors(LocalDate sahyogDate) {

        List<String> nonDonorUserIds = getNonDonors(sahyogDate)
                .stream()
                .map(NonDonorResponse::getUserId)
                .toList();

        LocalDate startDate = sahyogDate.withDayOfMonth(1);
        LocalDate endDate = sahyogDate.withDayOfMonth(sahyogDate.lengthOfMonth());

        return userRepo.findAll()
                .stream()
                .filter(u -> u.getRole() == Role.ROLE_USER)
                .filter(u -> !nonDonorUserIds.contains(u.getId()))
                .map(u -> new NonDonorResponse(
                        u.getId(),
                        u.getId(),
                        receiptRepo.sumPaidAmountByDateRange(u.getId(), startDate, endDate),
                        "DONOR"
                ))
                .toList();
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
}
