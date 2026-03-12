package com.example.kalyan_kosh_api.controller;

import com.example.kalyan_kosh_api.dto.BlockDto;
import com.example.kalyan_kosh_api.dto.DistrictDto;
import com.example.kalyan_kosh_api.dto.DistrictWithCountDto;
import com.example.kalyan_kosh_api.dto.SambhagDto;
import com.example.kalyan_kosh_api.dto.StateDto;
import com.example.kalyan_kosh_api.service.LocationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
@CrossOrigin(origins = "*") // temporary for frontend testing
public class LocationController {
    private final LocationService service;

    public LocationController(LocationService service) { this.service = service; }

    // Get all states
    @GetMapping("/states")
    public List<StateDto> getStates() {
        return service.getAllStates();
    }

    // Get sambhags by state ID
    @GetMapping("/states/{stateId}/sambhags")
    public ResponseEntity<List<SambhagDto>> getSambhagsByState(@PathVariable String stateId) {
        List<SambhagDto> sambhags = service.getSambhagsByState(stateId);
        if (sambhags == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(sambhags);
    }

    // Get districts by sambhag ID
    @GetMapping("/sambhags/{sambhagId}/districts")
    public ResponseEntity<List<DistrictDto>> getDistrictsBySambhag(@PathVariable String sambhagId) {
        List<DistrictDto> districts = service.getDistrictsBySambhag(sambhagId);
        if (districts == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(districts);
    }

    // Get all districts (existing)
    @GetMapping("/districts")
    public List<DistrictDto> getDistricts() {
        return service.getAllDistricts();
    }

    // Get blocks by district ID (existing)
    @GetMapping("/districts/{districtId}/blocks")
    public ResponseEntity<List<BlockDto>> getBlocks(@PathVariable String districtId) {
        List<BlockDto> blocks = service.getBlocksForDistrict(districtId);
        if (blocks == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(blocks);
    }

    // Get districts with user counts (existing)
    @GetMapping("/districts-with-counts")
    public List<DistrictWithCountDto> getDistrictsWithCounts() {
        return service.getDistrictsWithCounts();
    }

    // NEW: Get complete hierarchy
    @GetMapping("/hierarchy")
    public ResponseEntity<?> getCompleteHierarchy() {
        return ResponseEntity.ok(service.getCompleteHierarchy());
    }
}
