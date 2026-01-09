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
                        new ClassPathResource("data/madhya_pradesh_district_blocks.json");

                InputStream is = resource.getInputStream();
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(is);

                // Get "Madhya Pradesh" node
                JsonNode mpNode = root.get("Madhya Pradesh");

                // Create State
                State state = new State();
                state.setName("Madhya Pradesh");
                state.setCode("MP");
                state = stateRepo.save(state);
                System.out.println("✅ Created State: Madhya Pradesh");

                // Iterate through divisions (Bhopal, Chambal, Gwalior, etc.)
                Iterator<Map.Entry<String, JsonNode>> divisionFields = mpNode.fields();

                while (divisionFields.hasNext()) {
                    Map.Entry<String, JsonNode> divisionEntry = divisionFields.next();
                    String sambhagName = divisionEntry.getKey().trim(); // Division name
                    JsonNode districtsNode = divisionEntry.getValue();

                    // Create Sambhag (Division)
                    Sambhag sambhag = new Sambhag();
                    sambhag.setName(sambhagName);
                    sambhag.setState(state);
                    sambhag = sambhagRepo.save(sambhag);
                    System.out.println("  ✅ Created Sambhag: " + sambhagName);

                    // Iterate through districts in this division
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
