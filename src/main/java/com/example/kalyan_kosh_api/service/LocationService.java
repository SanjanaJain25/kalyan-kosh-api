package com.example.kalyan_kosh_api.service;

import com.example.kalyan_kosh_api.dto.BlockDto;
import com.example.kalyan_kosh_api.dto.DistrictDto;
import com.example.kalyan_kosh_api.dto.DistrictWithCountDto;
import com.example.kalyan_kosh_api.dto.SambhagDto;
import com.example.kalyan_kosh_api.dto.StateDto;
import com.example.kalyan_kosh_api.entity.District;
import com.example.kalyan_kosh_api.entity.Sambhag;
import com.example.kalyan_kosh_api.entity.State;
import com.example.kalyan_kosh_api.repository.BlockRepository;
import com.example.kalyan_kosh_api.repository.DistrictRepository;
import com.example.kalyan_kosh_api.repository.SambhagRepository;
import com.example.kalyan_kosh_api.repository.StateRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class LocationService {
    private final StateRepository stateRepository;
    private final SambhagRepository sambhagRepository;
    private final DistrictRepository districtRepository;
    private final BlockRepository blockRepository;

    public LocationService(StateRepository stateRepository,
                          SambhagRepository sambhagRepository,
                          DistrictRepository districtRepository,
                          BlockRepository blockRepository) {
        this.stateRepository = stateRepository;
        this.sambhagRepository = sambhagRepository;
        this.districtRepository = districtRepository;
        this.blockRepository = blockRepository;
    }

    // ========== STATE METHODS ==========
    public List<StateDto> getAllStates() {
        return stateRepository.findAll().stream()
                .map(s -> new StateDto(s.getId().toString(), s.getName(), s.getCode()))
                .collect(Collectors.toList());
    }

    // ========== SAMBHAG METHODS ==========
    public List<SambhagDto> getSambhagsByState(String stateIdOrName) {
        State state = findStateByIdOrName(stateIdOrName);
        if (state == null) return null;

        return sambhagRepository.findByState(state).stream()
                .map(sb -> new SambhagDto(
                        sb.getId().toString(),
                        sb.getName(),
                        state.getId().toString(),
                        state.getName()
                ))
                .collect(Collectors.toList());
    }

    // ========== DISTRICT METHODS ==========
    public List<DistrictDto> getDistrictsBySambhag(String sambhagIdOrName) {
        Sambhag sambhag = findSambhagByIdOrName(sambhagIdOrName);
        if (sambhag == null) return null;

        return districtRepository.findBySambhag(sambhag).stream()
                .map(d -> new DistrictDto(d.getId(), d.getName()))
                .collect(Collectors.toList());
    }

    public List<DistrictDto> getAllDistricts() {
        return districtRepository.findAll().stream()
                .map(d -> new DistrictDto(d.getId(), d.getName()))
                .collect(Collectors.toList());
    }

    public List<DistrictWithCountDto> getDistrictsWithCounts() {
        return districtRepository.findAll().stream()
                .map(d -> new DistrictWithCountDto(d.getId(), d.getName(), blockRepository.countByDistrictId(d.getId())))
                .collect(Collectors.toList());
    }

    // ========== BLOCK METHODS ==========
    public List<BlockDto> getBlocksForDistrict(UUID districtId) {
        return blockRepository.findByDistrictId(districtId).stream()
                .map(b -> new BlockDto(b.getId(), b.getName()))
                .collect(Collectors.toList());
    }

    // Accept either a UUID string or a district name; resolve to UUID and return blocks
    public List<BlockDto> getBlocksForDistrict(String districtIdOrName) {
        // Try parse UUID first
        try {
            UUID id = UUID.fromString(districtIdOrName);
            return getBlocksForDistrict(id);
        } catch (IllegalArgumentException ex) {
            // not a UUID, try by name
            District d = districtRepository.findByName(districtIdOrName).orElse(null);
            if (d == null) return null; // indicate not found
            return getBlocksForDistrict(d.getId());
        }
    }

    // ========== HIERARCHY METHOD ==========
    public Map<String, Object> getCompleteHierarchy() {
        Map<String, Object> result = new LinkedHashMap<>();

        List<State> states = stateRepository.findAll();
        List<Map<String, Object>> stateList = new ArrayList<>();

        for (State state : states) {
            Map<String, Object> stateMap = new LinkedHashMap<>();
            stateMap.put("id", state.getId().toString());
            stateMap.put("name", state.getName());
            stateMap.put("code", state.getCode());

            List<Map<String, Object>> sambhagList = new ArrayList<>();
            List<Sambhag> sambhags = sambhagRepository.findByState(state);

            for (Sambhag sambhag : sambhags) {
                Map<String, Object> sambhagMap = new LinkedHashMap<>();
                sambhagMap.put("id", sambhag.getId().toString());
                sambhagMap.put("name", sambhag.getName());

                List<Map<String, Object>> districtList = new ArrayList<>();
                List<District> districts = districtRepository.findBySambhag(sambhag);

                for (District district : districts) {
                    Map<String, Object> districtMap = new LinkedHashMap<>();
                    districtMap.put("id", district.getId().toString());
                    districtMap.put("name", district.getName());

                    List<Map<String, Object>> blockList = blockRepository.findByDistrictId(district.getId())
                            .stream()
                            .map(b -> {
                                Map<String, Object> blockMap = new LinkedHashMap<>();
                                blockMap.put("id", b.getId().toString());
                                blockMap.put("name", b.getName());
                                return blockMap;
                            })
                            .collect(Collectors.toList());

                    districtMap.put("blocks", blockList);
                    districtList.add(districtMap);
                }

                sambhagMap.put("districts", districtList);
                sambhagList.add(sambhagMap);
            }

            stateMap.put("sambhags", sambhagList);
            stateList.add(stateMap);
        }

        result.put("states", stateList);
        return result;
    }

    // ========== HELPER METHODS ==========
    private State findStateByIdOrName(String idOrName) {
        try {
            UUID id = UUID.fromString(idOrName);
            return stateRepository.findById(id).orElse(null);
        } catch (IllegalArgumentException ex) {
            return stateRepository.findByName(idOrName).orElse(null);
        }
    }

    private Sambhag findSambhagByIdOrName(String idOrName) {
        try {
            UUID id = UUID.fromString(idOrName);
            return sambhagRepository.findById(id).orElse(null);
        } catch (IllegalArgumentException ex) {
            return sambhagRepository.findByName(idOrName).orElse(null);
        }
    }
}

