package com.example.kalyan_kosh_api.service;

import com.example.kalyan_kosh_api.dto.BlockDto;
import com.example.kalyan_kosh_api.dto.DistrictDto;
import com.example.kalyan_kosh_api.dto.DistrictWithCountDto;
import com.example.kalyan_kosh_api.entity.Block;
import com.example.kalyan_kosh_api.entity.District;
import com.example.kalyan_kosh_api.repository.BlockRepository;
import com.example.kalyan_kosh_api.repository.DistrictRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class LocationService {
    private final DistrictRepository districtRepository;
    private final BlockRepository blockRepository;

    public LocationService(DistrictRepository districtRepository, BlockRepository blockRepository) {
        this.districtRepository = districtRepository;
        this.blockRepository = blockRepository;
    }

    public List<DistrictDto> getAllDistricts() {
        return districtRepository.findAll().stream()
                .map(d -> new DistrictDto(d.getId(), d.getName()))
                .collect(Collectors.toList());
    }

    public List<BlockDto> getBlocksForDistrict(UUID districtId) {
        return blockRepository.findByDistrictId(districtId).stream()
                .map(b -> new BlockDto(b.getId(), b.getName()))
                .collect(Collectors.toList());
    }

    public List<DistrictWithCountDto> getDistrictsWithCounts() {
        return districtRepository.findAll().stream()
                .map(d -> new DistrictWithCountDto(d.getId(), d.getName(), blockRepository.countByDistrictId(d.getId())))
                .collect(Collectors.toList());
    }
}
