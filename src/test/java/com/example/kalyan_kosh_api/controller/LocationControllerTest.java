package com.example.kalyan_kosh_api.controller;

import com.example.kalyan_kosh_api.dto.BlockDto;
import com.example.kalyan_kosh_api.dto.DistrictDto;
import com.example.kalyan_kosh_api.dto.DistrictWithCountDto;
import com.example.kalyan_kosh_api.service.LocationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LocationController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for unit tests
public class LocationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LocationService locationService;

    @Test
    public void testGetDistricts() throws Exception {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        List<DistrictDto> districts = Arrays.asList(
                new DistrictDto(id1, "District1"),
                new DistrictDto(id2, "District2")
        );

        when(locationService.getAllDistricts()).thenReturn(districts);

        mockMvc.perform(get("/api/locations/districts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("District1"))
                .andExpect(jsonPath("$[1].name").value("District2"));
    }

    @Test
    public void testGetBlocksByDistrictName() throws Exception {
        UUID blockId1 = UUID.randomUUID();
        UUID blockId2 = UUID.randomUUID();

        List<BlockDto> blocks = Arrays.asList(
                new BlockDto(blockId1, "Block1"),
                new BlockDto(blockId2, "Block2")
        );

        when(locationService.getBlocksForDistrict(anyString())).thenReturn(blocks);

        mockMvc.perform(get("/api/locations/districts/Ashoknagar/blocks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Block1"))
                .andExpect(jsonPath("$[1].name").value("Block2"));
    }

    @Test
    public void testGetBlocksByDistrictId() throws Exception {
        UUID districtId = UUID.randomUUID();
        UUID blockId1 = UUID.randomUUID();
        UUID blockId2 = UUID.randomUUID();

        List<BlockDto> blocks = Arrays.asList(
                new BlockDto(blockId1, "Block1"),
                new BlockDto(blockId2, "Block2")
        );

        when(locationService.getBlocksForDistrict(districtId.toString())).thenReturn(blocks);

        mockMvc.perform(get("/api/locations/districts/" + districtId + "/blocks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Block1"))
                .andExpect(jsonPath("$[1].name").value("Block2"));
    }

    @Test
    public void testGetBlocksNotFound() throws Exception {
        when(locationService.getBlocksForDistrict(anyString())).thenReturn(null);

        mockMvc.perform(get("/api/locations/districts/NonExistent/blocks"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetDistrictsWithCounts() throws Exception {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        List<DistrictWithCountDto> districts = Arrays.asList(
                new DistrictWithCountDto(id1, "District1", 5L),
                new DistrictWithCountDto(id2, "District2", 3L)
        );

        when(locationService.getDistrictsWithCounts()).thenReturn(districts);

        mockMvc.perform(get("/api/locations/districts-with-counts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("District1"))
                .andExpect(jsonPath("$[0].blockCount").value(5))
                .andExpect(jsonPath("$[1].name").value("District2"))
                .andExpect(jsonPath("$[1].blockCount").value(3));
    }
}

