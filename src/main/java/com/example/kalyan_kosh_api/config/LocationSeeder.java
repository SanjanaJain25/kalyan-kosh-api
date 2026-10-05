package com.example.kalyan_kosh_api.config;

import com.example.kalyan_kosh_api.entity.Block;
import com.example.kalyan_kosh_api.entity.District;
import com.example.kalyan_kosh_api.entity.Sambhag;
import com.example.kalyan_kosh_api.entity.State;
import com.example.kalyan_kosh_api.repository.BlockRepository;
import com.example.kalyan_kosh_api.repository.DistrictRepository;
import com.example.kalyan_kosh_api.repository.SambhagRepository;
import com.example.kalyan_kosh_api.repository.StateRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;

@Component
public class LocationSeeder {

    private static final Logger log = LoggerFactory.getLogger(LocationSeeder.class);

    private final StateRepository stateRepo;
    private final SambhagRepository sambhagRepo;
    private final DistrictRepository districtRepo;
    private final BlockRepository blockRepo;
    private final ObjectMapper objectMapper;

    public LocationSeeder(
            StateRepository stateRepo,
            SambhagRepository sambhagRepo,
            DistrictRepository districtRepo,
            BlockRepository blockRepo,
            ObjectMapper objectMapper
    ) {
        this.stateRepo = stateRepo;
        this.sambhagRepo = sambhagRepo;
        this.districtRepo = districtRepo;
        this.blockRepo = blockRepo;
        this.objectMapper = objectMapper;
    }

    public void seedIfMissing() {
        if (stateRepo.count() > 0) {
            return;
        }

        try (InputStream inputStream = new ClassPathResource(
                "data/madhya_pradesh_district_blocks.json"
        ).getInputStream()) {
            JsonNode root = objectMapper.readTree(inputStream);
            JsonNode mpNode = root.get("Madhya Pradesh");

            if (mpNode == null || !mpNode.isObject()) {
                throw new IllegalStateException("Madhya Pradesh location data is missing from seed JSON.");
            }

            State state = new State();
            state.setName("Madhya Pradesh");
            state.setCode("MP");
            state = stateRepo.save(state);

            Iterator<Map.Entry<String, JsonNode>> divisionFields = mpNode.fields();

            while (divisionFields.hasNext()) {
                Map.Entry<String, JsonNode> divisionEntry = divisionFields.next();
                String sambhagName = divisionEntry.getKey().trim();
                JsonNode districtsNode = divisionEntry.getValue();

                Sambhag sambhag = new Sambhag();
                sambhag.setName(sambhagName);
                sambhag.setState(state);
                sambhag = sambhagRepo.save(sambhag);

                Iterator<Map.Entry<String, JsonNode>> districtFields = districtsNode.fields();

                while (districtFields.hasNext()) {
                    Map.Entry<String, JsonNode> districtEntry = districtFields.next();
                    String districtName = districtEntry.getKey().trim();
                    JsonNode blocksArray = districtEntry.getValue();

                    District district = new District();
                    district.setName(districtName);
                    district.setSambhag(sambhag);
                    district = districtRepo.save(district);

                    for (JsonNode blockNameNode : blocksArray) {
                        Block block = new Block();
                        block.setName(blockNameNode.asText().trim());
                        block.setDistrict(district);
                        blockRepo.save(block);
                    }
                }
            }

            log.info("Location seed data created successfully.");
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to seed location data.", ex);
        }
    }
}
