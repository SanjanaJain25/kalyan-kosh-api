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
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;

@Configuration
public class LocationSeeder {

    @Bean
    public CommandLineRunner seedData(
            StateRepository stateRepo,
            SambhagRepository sambhagRepo,
            DistrictRepository districtRepo,
            BlockRepository blockRepo
    ) {
        return args -> {

            // ✅ Seed ONLY if no state data exists
            if (stateRepo.count() > 0) {
                System.out.println("ℹ Location hierarchy data already exists. Skipping seeding.");
                return;
            }

            System.out.println("🚀 Seeding State → Sambhag → District → Block hierarchy...");

            try {
                ClassPathResource resource =
                        new ClassPathResource("data/mp_state_sambhag_district_block_data.json");

                InputStream is = resource.getInputStream();
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(is);

                // Get state node
                JsonNode stateNode = root.get("state");
                String stateName = stateNode.get("name").asText();
                String stateCode = stateNode.get("code").asText();

                // Create State
                State state = new State();
                state.setName(stateName);
                state.setCode(stateCode);
                state = stateRepo.save(state);
                System.out.println("✅ Created State: " + stateName);

                // Get sambhags array
                JsonNode sambhagsArray = stateNode.get("sambhags");

                for (JsonNode sambhagNode : sambhagsArray) {
                    String sambhagName = sambhagNode.get("name").asText();

                    // Create Sambhag
                    Sambhag sambhag = new Sambhag();
                    sambhag.setName(sambhagName);
                    sambhag.setState(state);
                    sambhag = sambhagRepo.save(sambhag);
                    System.out.println("  ✅ Created Sambhag: " + sambhagName);

                    // Get districts object
                    JsonNode districtsNode = sambhagNode.get("districts");
                    Iterator<Map.Entry<String, JsonNode>> districtFields = districtsNode.fields();

                    while (districtFields.hasNext()) {
                        Map.Entry<String, JsonNode> districtEntry = districtFields.next();
                        String districtName = districtEntry.getKey();
                        JsonNode blocksArray = districtEntry.getValue();

                        // Create District
                        District district = new District();
                        district.setName(districtName);
                        district.setSambhag(sambhag);
                        district = districtRepo.save(district);

                        // Create Blocks
                        int blockCount = 0;
                        for (JsonNode blockNameNode : blocksArray) {
                            String blockName = blockNameNode.asText();

                            Block block = new Block();
                            block.setName(blockName);
                            block.setDistrict(district);
                            blockRepo.save(block);
                            blockCount++;
                        }
                        System.out.println("    ✅ Created District: " + districtName + " with " + blockCount + " blocks");
                    }
                }

                System.out.println("✅ Location hierarchy seeded successfully!");
                System.out.println("   State: 1, Sambhags: " + sambhagRepo.count() +
                                   ", Districts: " + districtRepo.count() +
                                   ", Blocks: " + blockRepo.count());

            } catch (Exception ex) {
                System.err.println("❌ Failed to seed location hierarchy");
                ex.printStackTrace();
            }
        };
    }
}
