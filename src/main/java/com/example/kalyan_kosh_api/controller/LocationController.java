package com.example.kalyan_kosh_api.controller;

import com.example.kalyan_kosh_api.dto.BlockDto;
import com.example.kalyan_kosh_api.dto.DistrictDto;
import com.example.kalyan_kosh_api.dto.DistrictWithCountDto;
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

    @GetMapping("/districts")
    public List<DistrictDto> getDistricts() { return service.getAllDistricts(); }

    @GetMapping("/districts/{districtId}/blocks")
    public ResponseEntity<List<BlockDto>> getBlocks(@PathVariable String districtId) {
        List<BlockDto> blocks = service.getBlocksForDistrict(districtId);
        if (blocks == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(blocks);
    }

    @GetMapping("/districts-with-counts")
    public List<DistrictWithCountDto> getDistrictsWithCounts() { return service.getDistrictsWithCounts(); }
}
