package com.example.kalyan_kosh_api.repository;

import com.example.kalyan_kosh_api.entity.DeathCase;
import com.example.kalyan_kosh_api.entity.DeathCaseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.kalyan_kosh_api.dto.BeneficiaryOptionDto;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.Query;

public interface DeathCaseRepository extends JpaRepository<DeathCase, Long> {

    // Count death cases by date range
    long countByCaseDateBetween(LocalDate startDate, LocalDate endDate);

    // Find death cases by status
    List<DeathCase> findByStatus(DeathCaseStatus status);

    // Find death cases by status ordered by case date descending
    List<DeathCase> findByStatusOrderByCaseDateDesc(DeathCaseStatus status);
// Find death cases by status ordered by creation date ascending (for round-robin assignment)
    List<DeathCase> findByStatusOrderByIdAsc(DeathCaseStatus status);

    @Query("""
    SELECT DISTINCT d.deceasedName
    FROM DeathCase d
    WHERE d.deceasedName IS NOT NULL
      AND TRIM(d.deceasedName) <> ''
    ORDER BY d.deceasedName
""")
List<String> findDistinctBeneficiaryNames();

@Query("""
    SELECT new com.example.kalyan_kosh_api.dto.BeneficiaryOptionDto(dc.id, dc.deceasedName)
    FROM DeathCase dc
    WHERE dc.deceasedName IS NOT NULL
      AND TRIM(dc.deceasedName) <> ''
    ORDER BY dc.deceasedName
""")
List<BeneficiaryOptionDto> findAllBeneficiaryOptions();
}
