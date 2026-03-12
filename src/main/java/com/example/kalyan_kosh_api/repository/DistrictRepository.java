package com.example.kalyan_kosh_api.repository;

import com.example.kalyan_kosh_api.entity.District;
import com.example.kalyan_kosh_api.entity.Sambhag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DistrictRepository extends JpaRepository<District, UUID> {
    Optional<District> findByName(String name);
    Optional<District> findByNameAndSambhag(String name, Sambhag sambhag);
    List<District> findBySambhag(Sambhag sambhag);
    List<District> findBySambhagId(UUID sambhagId);
}
