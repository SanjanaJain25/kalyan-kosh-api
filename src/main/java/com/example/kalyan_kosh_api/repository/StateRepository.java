package com.example.kalyan_kosh_api.repository;

import com.example.kalyan_kosh_api.entity.State;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StateRepository extends JpaRepository<State, UUID> {
    Optional<State> findByName(String name);
    Optional<State> findByCode(String code);
}

