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

    public MonthlySahyog generate(int month, int year) {

        sahyogRepo.findByMonthAndYear(month, year)
                .ifPresent(ms -> {
                    throw new IllegalStateException(
                            "Monthly Sahyog already generated");
                });

        int totalMembers = (int) userRepo.count();

        MonthlySahyog sahyog = MonthlySahyog.builder()
                .month(month)
                .year(year)
                .totalMembers(totalMembers)
                .totalDeathCases(0)
                .receivedAmount(0)
                .status(SahyogStatus.OPEN)
                .generatedAt(Instant.now())
                .build();

        return sahyogRepo.save(sahyog);
    }


    public MonthlySahyog updateDeathCases(int month, int year) {

        MonthlySahyog sahyog = sahyogRepo.findByMonthAndYear(month, year)
                .orElseThrow(() ->
                        new IllegalStateException("Monthly Sahyog not generated"));

        if (sahyog.getStatus() == SahyogStatus.FROZEN) {
            throw new IllegalStateException(
                    "Month is frozen. Update not allowed.");
        }

        int deathCases =
                (int) deathCaseRepo.countByCaseMonthAndCaseYear(month, year);

        sahyog.setTotalDeathCases(deathCases);

        return sahyogRepo.save(sahyog);
    }


    public List<NonDonorResponse> getNonDonors(int month, int year) {

        sahyogRepo.findByMonthAndYear(month, year)
                .orElseThrow(() ->
                        new IllegalStateException("Monthly Sahyog not generated"));

        return userRepo.findAll().stream()
                .filter(u -> u.getRole() == Role.ROLE_USER)
                .map(user -> {

                    double paidAmount =
                            receiptRepo.sumPaidAmount(
                                    user.getId(), month, year);

                    String status =
                            paidAmount > 0 ? "DONOR" : "NON_DONOR";


                    return new NonDonorResponse(
                            user.getId(),
                            user.getUsername(),
                            paidAmount,
                            status
                    );
                })
                .filter(r -> "NON_DONOR".equals(r.getStatus()))
                .toList();
    }

    public void exportNonDonorsCsv(
            int month,
            int year,
            PrintWriter writer) {

        List<NonDonorResponse> nonDonors = getNonDonors(month, year);


        writer.println("UserId,Username,Month,Year,PaidAmount,Status");

        for (NonDonorResponse r : nonDonors) {
            writer.printf(
                    "%d,%s,%d,%d,%.2f,%s%n",
                    r.getUserId(),
                    r.getUsername(),
                    month,
                    year,
                    r.getPaidAmount(),
                    r.getStatus()
            );
        }

        writer.flush();
    }

    public List<NonDonorResponse> getDonors(int month, int year) {

        List<Long> nonDonorUserIds = getNonDonors(month, year)
                .stream()
                .map(NonDonorResponse::getUserId)
                .toList();

        return userRepo.findAll()
                .stream()
                .filter(u -> u.getRole() == Role.ROLE_USER)
                .filter(u -> !nonDonorUserIds.contains(u.getId()))
                .map(u -> new NonDonorResponse(
                        u.getId(),
                        u.getUsername(),
                        receiptRepo.sumPaidAmount(u.getId(), month, year),
                        "DONOR"
                ))
                .toList();
    }



    public AdminDashboardSummaryResponse getDashboardSummary(
            int month,
            int year) {

        long totalMembers = userRepo.count();

        long totalDeathCases =
                deathCaseRepo.countByCaseMonthAndCaseYear(month, year);

        long totalDonors =
                receiptRepo.countDonors(month, year);

        long totalNonDonors =
                totalMembers - totalDonors;

        double totalReceivedAmount =
                receiptRepo.sumVerifiedAmount(month, year);

        return new AdminDashboardSummaryResponse(
                month,
                year,
                totalMembers,
                totalDeathCases,
                totalDonors,
                totalNonDonors,
                totalReceivedAmount
        );
    }

    public MonthlySahyog freezeMonth(int month, int year) {

        MonthlySahyog sahyog = getOpenMonth(month, year);

        sahyog.setStatus(SahyogStatus.FROZEN);
        sahyog.setFrozenAt(Instant.now());

        return sahyogRepo.save(sahyog);
    }

    private MonthlySahyog getOpenMonth(int month, int year) {

        MonthlySahyog sahyog = sahyogRepo.findByMonthAndYear(month, year)
                .orElseThrow(() ->
                        new IllegalStateException("Monthly Sahyog not generated"));

        if (sahyog.getStatus() == SahyogStatus.FROZEN) {
            throw new IllegalStateException(
                    "Month is frozen. Operation not allowed.");
        }

        return sahyog;
    }



}
