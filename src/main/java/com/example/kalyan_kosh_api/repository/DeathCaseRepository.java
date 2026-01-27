package com.example.kalyan_kosh_api.repository;

import com.example.kalyan_kosh_api.entity.DeathCase;
import com.example.kalyan_kosh_api.entity.DeathCaseStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface DeathCaseRepository extends JpaRepository<DeathCase, Long> {

    // Count death cases by date range
    long countByCaseDateBetween(LocalDate startDate, LocalDate endDate);

    // Find death cases by status
    List<DeathCase> findByStatus(DeathCaseStatus status);

    // Find death cases by status ordered by case date descending
    List<DeathCase> findByStatusOrderByCaseDateDesc(DeathCaseStatus status);
}
