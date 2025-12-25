package com.example.kalyan_kosh_api.repository;

import com.example.kalyan_kosh_api.entity.DeathCase;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeathCaseRepository extends JpaRepository<DeathCase,Long> {
    long countByCaseMonthAndCaseYear(int caseMonth, int caseYear);
}
