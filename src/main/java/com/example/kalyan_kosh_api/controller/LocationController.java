package com.example.kalyan_kosh_api.controller;

import com.example.kalyan_kosh_api.dto.BlockDto;
import com.example.kalyan_kosh_api.dto.DistrictDto;
import com.example.kalyan_kosh_api.dto.DistrictWithCountDto;
import com.example.kalyan_kosh_api.service.LocationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/locations")
@CrossOrigin(origins = "*") // temporary for frontend testing
public class LocationController {
    private final LocationService service;

    public LocationController(LocationService service) { this.service = service; }

    @GetMapping("/districts")
    public List<DistrictDto> getDistricts() { return service.getAllDistricts(); }

    @GetMapping("/districts/{districtId}/blocks")
    public List<BlockDto> getBlocks(@PathVariable UUID districtId) { return service.getBlocksForDistrict(districtId); }

    @GetMapping("/districts-with-counts")
    public List<DistrictWithCountDto> getDistrictsWithCounts() { return service.getDistrictsWithCounts(); }
}
