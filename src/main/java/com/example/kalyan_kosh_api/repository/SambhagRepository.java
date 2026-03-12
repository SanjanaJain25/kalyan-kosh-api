package com.example.kalyan_kosh_api.repository;

import com.example.kalyan_kosh_api.entity.Sambhag;
import com.example.kalyan_kosh_api.entity.State;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SambhagRepository extends JpaRepository<Sambhag, UUID> {
    Optional<Sambhag> findByName(String name);
    Optional<Sambhag> findByNameAndState(String name, State state);
    List<Sambhag> findByState(State state);
    List<Sambhag> findByStateId(UUID stateId);
}

