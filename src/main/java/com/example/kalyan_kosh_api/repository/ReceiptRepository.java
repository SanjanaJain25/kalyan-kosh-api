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
}
