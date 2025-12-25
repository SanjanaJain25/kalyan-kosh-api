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




    List<Receipt> findByMonthAndYear(int month, int year);


    @Query("""
        SELECT COALESCE(SUM(r.amount), 0)
        FROM Receipt r
        WHERE r.month = :month
          AND r.year = :year
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
        WHERE r.month = :month
          AND r.year = :year
          AND r.status = 'VERIFIED'
    """)
    List<User> findDonors(
            @Param("month") int month,
            @Param("year") int year
    );
}
