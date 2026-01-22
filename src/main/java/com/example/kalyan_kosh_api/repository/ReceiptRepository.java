package com.example.kalyan_kosh_api.repository;

import com.example.kalyan_kosh_api.entity.Receipt;
import com.example.kalyan_kosh_api.entity.ReceiptStatus;
import com.example.kalyan_kosh_api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ReceiptRepository extends JpaRepository<Receipt, Long> {

    List<Receipt> findByUserOrderByUploadedAtDesc(User user);

    // Find receipts by date range
    List<Receipt> findByPaymentDateBetween(LocalDate startDate, LocalDate endDate);

    @Query("""
        SELECT COALESCE(SUM(r.amount), 0)
        FROM Receipt r
        WHERE r.paymentDate BETWEEN :startDate AND :endDate
          AND r.status = :status
    """)
    double sumAmountByDateRangeAndStatus(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("status") ReceiptStatus status
    );

    @Query("""
        SELECT DISTINCT r.user
        FROM Receipt r
        WHERE r.paymentDate BETWEEN :startDate AND :endDate
          AND r.status = 'VERIFIED'
    """)
    List<User> findDonorsByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("""
        SELECT COALESCE(SUM(r.amount), 0)
        FROM Receipt r
        WHERE r.user.id = :userId
          AND r.paymentDate BETWEEN :startDate AND :endDate
    """)
    double sumPaidAmountByDateRange(
            @Param("userId") String userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("""
        SELECT r
        FROM Receipt r
        WHERE r.paymentDate BETWEEN :startDate AND :endDate
          AND r.status = com.example.kalyan_kosh_api.entity.ReceiptStatus.VERIFIED
    """)
    List<Receipt> findVerifiedReceiptsByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

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

    // ✅ Optimized: Get all donor user IDs with their total paid amounts in a single query
    @Query("""
        SELECT r.user.id, COALESCE(SUM(r.amount), 0)
        FROM Receipt r
        WHERE r.paymentDate BETWEEN :startDate AND :endDate
        GROUP BY r.user.id
        HAVING SUM(r.amount) > 0
    """)
    List<Object[]> findDonorUserIdsWithAmounts(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // ✅ Optimized: Get all donor user IDs in a single query
    @Query("""
        SELECT DISTINCT r.user.id
        FROM Receipt r
        WHERE r.paymentDate BETWEEN :startDate AND :endDate
          AND r.amount > 0
    """)
    java.util.Set<String> findDonorUserIdsByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // ✅ Fetch receipts with user and death case data for donor list
    @Query("""
        SELECT r FROM Receipt r
        JOIN FETCH r.user u
        LEFT JOIN FETCH u.departmentState
        LEFT JOIN FETCH u.departmentSambhag
        LEFT JOIN FETCH u.departmentDistrict
        LEFT JOIN FETCH u.departmentBlock
        JOIN FETCH r.deathCase dc
        WHERE r.paymentDate BETWEEN :startDate AND :endDate
          AND r.amount > 0
        ORDER BY r.uploadedAt DESC
    """)
    List<Receipt> findReceiptsWithUserAndDeathCaseByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
