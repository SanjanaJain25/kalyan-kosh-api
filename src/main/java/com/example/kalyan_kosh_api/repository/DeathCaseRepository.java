package com.example.kalyan_kosh_api.repository;

import com.example.kalyan_kosh_api.entity.DeathCase;
import com.example.kalyan_kosh_api.entity.DeathCaseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.kalyan_kosh_api.dto.BeneficiaryOptionDto;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DeathCaseRepository extends JpaRepository<DeathCase, Long> {

    // Count death cases by date range
    long countByCaseDateBetween(LocalDate startDate, LocalDate endDate);

    // Find death cases by status
    List<DeathCase> findByStatus(DeathCaseStatus status);

    // Find death cases by status ordered by case date descending
    List<DeathCase> findByStatusOrderByCaseDateDesc(DeathCaseStatus status);
// Find death cases by status ordered by creation date ascending (for round-robin assignment)
    List<DeathCase> findByStatusOrderByIdAsc(DeathCaseStatus status);
    @Query(
        value = """
                SELECT qr_code
                FROM death_case_nominee1_qr_codes
                WHERE death_case_id = :deathCaseId
                  AND qr_code IS NOT NULL
                  AND TRIM(qr_code) <> ''
                ORDER BY qr_order ASC
                """,
        nativeQuery = true
)
List<String> findNominee1QrCodesByDeathCaseId(@Param("deathCaseId") Long deathCaseId);


@Query(
        value = """
                SELECT qr_code
                FROM death_case_nominee2_qr_codes
                WHERE death_case_id = :deathCaseId
                  AND qr_code IS NOT NULL
                  AND TRIM(qr_code) <> ''
                ORDER BY qr_order ASC
                """,
        nativeQuery = true
)
List<String> findNominee2QrCodesByDeathCaseId(@Param("deathCaseId") Long deathCaseId);

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
