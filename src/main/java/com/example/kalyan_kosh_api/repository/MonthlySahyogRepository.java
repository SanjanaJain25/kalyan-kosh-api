package com.example.kalyan_kosh_api.repository;

import com.example.kalyan_kosh_api.entity.MonthlySahyog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MonthlySahyogRepository
        extends JpaRepository<MonthlySahyog, Long> {

    Optional<MonthlySahyog> findByMonthAndYear(int month, int year);
}
