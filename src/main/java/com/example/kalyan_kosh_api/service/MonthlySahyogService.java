package com.example.kalyan_kosh_api.service;

import com.example.kalyan_kosh_api.dto.NonDonorResponse;
import com.example.kalyan_kosh_api.entity.MonthlySahyog;
import com.example.kalyan_kosh_api.entity.Role;
import com.example.kalyan_kosh_api.entity.SahyogStatus;
import com.example.kalyan_kosh_api.repository.DeathCaseRepository;
import com.example.kalyan_kosh_api.repository.MonthlySahyogRepository;
import com.example.kalyan_kosh_api.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class MonthlySahyogService {

    private final MonthlySahyogRepository sahyogRepo;
    private final UserRepository userRepo;

    private final DeathCaseRepository deathCaseRepo;


    public MonthlySahyogService(
            MonthlySahyogRepository sahyogRepo,
            UserRepository userRepo,
            DeathCaseRepository deathCaseRepo) {

        this.sahyogRepo = sahyogRepo;
        this.userRepo = userRepo;
        this.deathCaseRepo = deathCaseRepo;
    }


    public MonthlySahyog generate(int month, int year) {

        // avoid duplicate generation
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
                .totalDeathCases(0)   // Phase-2 me add hoga
                .expectedAmount(0)
                .receivedAmount(0)
                .status(SahyogStatus.OPEN)
                .generatedAt(Instant.now())
                .build();

        return sahyogRepo.save(sahyog);
    }

    public MonthlySahyog calculateExpected(int month, int year) {

        MonthlySahyog sahyog = sahyogRepo.findByMonthAndYear(month, year)
                .orElseThrow(() -> new IllegalStateException(
                        "Monthly Sahyog not generated"));

        if (sahyog.getStatus() == SahyogStatus.FROZEN) {
            throw new IllegalStateException(
                    "Month is frozen. Calculation not allowed.");
        }

        int totalMembers = sahyog.getTotalMembers();
        int deathCases = (int)deathCaseRepo.countByCaseMonthAndCaseYear(month, year);

        double expected =
                totalMembers * deathCases * 500;

        sahyog.setTotalDeathCases(deathCases);
        sahyog.setExpectedAmount(expected);

        return sahyogRepo.save(sahyog);
    }

    public List<NonDonorResponse> getNonDonors(int month, int year) {

        MonthlySahyog sahyog = sahyogRepo.findByMonthAndYear(month, year)
                .orElseThrow(() ->
                        new IllegalStateException("Monthly Sahyog not generated"));

        int deathCases = sahyog.getTotalDeathCases();
        double expectedPerUser = deathCases * 500;

        return userRepo.findAll().stream()
                .filter(u -> u.getRole() == Role.ROLE_USER)
                .map(user -> {

                    double paidAmount = 0; // ⚠️ receipt logic later

                    String status;
                    if (paidAmount == 0) {
                        status = "UNPAID";
                    } else if (paidAmount < expectedPerUser) {
                        status = "PARTIAL";
                    } else {
                        status = "PAID";
                    }

                    return new NonDonorResponse(
                            user.getId(),
                            user.getUsername(),
                            expectedPerUser,
                            paidAmount,
                            status
                    );
                })
                .filter(r -> !"PAID".equals(r.getStatus()))
                .toList();
    }


}
