package com.example.kalyan_kosh_api.repository;

import com.example.kalyan_kosh_api.entity.Receipt;
import com.example.kalyan_kosh_api.entity.ReceiptStatus;
import com.example.kalyan_kosh_api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReceiptRepository extends JpaRepository<Receipt, Long> {

    List<Receipt> findByUserOrderByUploadedAtDesc(User user);

    // ✅ Updated queries to use MONTH() and YEAR() functions on paymentDate

    @Query("""
        SELECT r FROM Receipt r
        WHERE MONTH(r.paymentDate) = :month
          AND YEAR(r.paymentDate) = :year
    """)
    List<Receipt> findByMonthAndYear(@Param("month") int month, @Param("year") int year);

    @Query("""
        SELECT COALESCE(SUM(r.amount), 0)
        FROM Receipt r
        WHERE MONTH(r.paymentDate) = :month
          AND YEAR(r.paymentDate) = :year
          AND r.status = :status
    """)
    double sumAmountByMonthAndYearAndStatus(
            @Param("month") int month,
            @Param("year") int year,
            @Param("status") ReceiptStatus status
    );

    @Query("""
        SELECT DISTINCT r.user
        FROM Receipt r
        WHERE MONTH(r.paymentDate) = :month
          AND YEAR(r.paymentDate) = :year
          AND r.status = 'VERIFIED'
    """)
    List<User> findDonors(
            @Param("month") int month,
            @Param("year") int year
    );

    @Query("""
        SELECT COALESCE(SUM(r.amount), 0)
        FROM Receipt r
        WHERE r.user.id = :userId
          AND MONTH(r.paymentDate) = :month
          AND YEAR(r.paymentDate) = :year
          AND r.status = 'VERIFIED'
    """)
    double sumPaidAmount(@Param("userId") String userId, @Param("month") int month, @Param("year") int year);

    @Query("""
        SELECT r
        FROM Receipt r
        WHERE MONTH(r.paymentDate) = :month
          AND YEAR(r.paymentDate) = :year
          AND r.status = com.example.kalyan_kosh_api.entity.ReceiptStatus.VERIFIED
    """)
    List<Receipt> findVerifiedReceipts(@Param("month") int month, @Param("year") int year);

    @Query("""
        SELECT COUNT(DISTINCT r.user.id)
        FROM Receipt r
        WHERE MONTH(r.paymentDate) = :month
          AND YEAR(r.paymentDate) = :year
          AND r.status = 'VERIFIED'
    """)
    long countDonors(@Param("month") int month, @Param("year") int year);

    @Query("""
        SELECT COALESCE(SUM(r.amount), 0)
        FROM Receipt r
        WHERE MONTH(r.paymentDate) = :month
          AND YEAR(r.paymentDate) = :year
          AND r.status = 'VERIFIED'
    """)
    double sumVerifiedAmount(@Param("month") int month, @Param("year") int year);
}
