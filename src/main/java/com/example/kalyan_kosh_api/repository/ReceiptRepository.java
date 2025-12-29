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

    @Query("""
select COALESCE(SUM(r.amount), 0)
from Receipt r
where r.user.id = :userId
and r.month = :month
and r.year = :year
and r.status = 'VERIFIED'
""")
    double sumPaidAmount(String userId, int month, int year);

    @Query("""
select r
from Receipt r
where r.month = :month
and r.year = :year
and r.status = com.example.kalyan_kosh_api.entity.ReceiptStatus.VERIFIED
""")
    List<Receipt> findVerifiedReceipts(int month, int year);

    @Query("""
select count(distinct r.user.id)
from Receipt r
where r.month = :month
and r.year = :year
and r.status = 'VERIFIED'
""")
    long countDonors(int month, int year);


    @Query("""
select coalesce(sum(r.amount), 0)
from Receipt r
where r.month = :month
and r.year = :year
and r.status = 'VERIFIED'
""")
    double sumVerifiedAmount(int month, int year);

}
