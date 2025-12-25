package com.example.kalyan_kosh_api.repository;

import com.example.kalyan_kosh_api.entity.District;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface DistrictRepository extends JpaRepository<District, UUID> {
    District findByName(String name);
}
