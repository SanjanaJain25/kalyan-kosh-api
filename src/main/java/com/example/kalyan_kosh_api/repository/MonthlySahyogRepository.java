package com.example.kalyan_kosh_api.repository;

import com.example.kalyan_kosh_api.entity.MonthlySahyog;
import com.example.kalyan_kosh_api.entity.SahyogStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface MonthlySahyogRepository
        extends JpaRepository<MonthlySahyog, Long> {

    Optional<MonthlySahyog> findBySahyogDate(LocalDate sahyogDate);

    Optional<MonthlySahyog> findBySahyogDateAndStatus(LocalDate sahyogDate, SahyogStatus status);
}
