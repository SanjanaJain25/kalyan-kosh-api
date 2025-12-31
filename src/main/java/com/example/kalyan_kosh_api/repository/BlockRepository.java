package com.example.kalyan_kosh_api.repository;

import com.example.kalyan_kosh_api.entity.Block;
import com.example.kalyan_kosh_api.entity.District;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BlockRepository extends JpaRepository<Block, UUID> {
    List<Block> findByDistrictId(UUID districtId);
    long countByDistrictId(UUID districtId);

    // Find block by name
    Optional<Block> findByName(String name);

    // Find block by name and district
    Optional<Block> findByNameAndDistrict(String name, District district);
}
