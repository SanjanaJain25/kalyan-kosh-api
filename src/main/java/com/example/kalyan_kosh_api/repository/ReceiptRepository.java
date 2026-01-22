package com.example.kalyan_kosh_api.repository;

import com.example.kalyan_kosh_api.entity.Receipt;
import com.example.kalyan_kosh_api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public interface ReceiptRepository extends JpaRepository<Receipt, Long> {

    List<Receipt> findByUserOrderByUploadedAtDesc(User user);

    @Query("""
        SELECT COUNT(DISTINCT r.user.id)
        FROM Receipt r
        WHERE r.paymentDate BETWEEN :startDate AND :endDate
          AND r.status = 'VERIFIED'
    """)
    long countDonorsByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("""
        SELECT COALESCE(SUM(r.amount), 0)
        FROM Receipt r
        WHERE r.paymentDate BETWEEN :startDate AND :endDate
          AND r.status = 'VERIFIED'
    """)
    double sumVerifiedAmountByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // ✅ Get all donor user IDs in a single query
    @Query("""
        SELECT DISTINCT r.user.id
        FROM Receipt r
        WHERE r.paymentDate BETWEEN :startDate AND :endDate
          AND r.amount > 0
    """)
    Set<String> findDonorUserIdsByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // ✅ SUPER FAST PAGINATED: Get donors with pagination directly from DB
    @Query(value = """
        SELECT u.id, u.department_unique_id, u.name, u.surname, u.department,
               s.name as state_name, sa.name as sambhag_name, d.name as district_name, 
               b.name as block_name, u.school_office_name,
               dc.deceased_name as beneficiary, MAX(r.uploaded_at) as receipt_date
        FROM receipt r
        JOIN users u ON r.user_id = u.id
        LEFT JOIN state s ON u.department_state_id = s.id
        LEFT JOIN sambhag sa ON u.department_sambhag_id = sa.id
        LEFT JOIN district d ON u.department_district_id = d.id
        LEFT JOIN block b ON u.department_block_id = b.id
        LEFT JOIN death_case dc ON r.death_case_id = dc.id
        WHERE r.payment_date BETWEEN :startDate AND :endDate AND r.amount > 0
        GROUP BY u.id, u.department_unique_id, u.name, u.surname, u.department,
                 s.name, sa.name, d.name, b.name, u.school_office_name, dc.deceased_name
        ORDER BY MAX(r.uploaded_at) DESC
        """,
        countQuery = """
        SELECT COUNT(DISTINCT user_id)
        FROM receipt
        WHERE payment_date BETWEEN :startDate AND :endDate AND amount > 0
        """,
        nativeQuery = true)
    org.springframework.data.domain.Page<Object[]> findDonorsPaginatedNative(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            org.springframework.data.domain.Pageable pageable
    );
}
