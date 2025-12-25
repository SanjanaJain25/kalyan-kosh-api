package com.example.kalyan_kosh_api.repository;

import com.example.kalyan_kosh_api.entity.Block;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BlockRepository extends JpaRepository<Block, UUID> {
    List<Block> findByDistrictId(UUID districtId);
    long countByDistrictId(UUID districtId);
}
