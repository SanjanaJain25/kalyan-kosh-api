package com.example.kalyan_kosh_api.repository;

import com.example.kalyan_kosh_api.entity.MonthlySahyog;
import com.example.kalyan_kosh_api.entity.SahyogStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface MonthlySahyogRepository
        extends JpaRepository<MonthlySahyog, Long> {

    Optional<MonthlySahyog> findByMonthAndYear(int month, int year);

    Optional<MonthlySahyog> findByMonthAndYearAndStatus(
            int month,
            int year,
            SahyogStatus status
    );
}
