package com.example.kalyan_kosh_api.config;

import com.example.kalyan_kosh_api.entity.Block;
import com.example.kalyan_kosh_api.entity.District;
import com.example.kalyan_kosh_api.repository.BlockRepository;
import com.example.kalyan_kosh_api.repository.DistrictRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Configuration
public class LocationSeeder {

    @Bean
    public CommandLineRunner seedData(
            DistrictRepository districtRepo,
            BlockRepository blockRepo
    ) {
        return args -> {

            // ✅ Seed ONLY if no district data exists
            if (districtRepo.count() > 0) {
                System.out.println("ℹ District & Block data already exists. Skipping seeding.");
                return;
            }

            System.out.println("🚀 Seeding district & block master data...");

            try {
                ClassPathResource resource =
                        new ClassPathResource("data/mp_district_block_data.json");

                InputStream is = resource.getInputStream();
                ObjectMapper mapper = new ObjectMapper();

                // Map<districtName, List<blockNames>>
                Map<String, List<String>> map =
                        mapper.readValue(is,
                                new TypeReference<Map<String, List<String>>>() {});

                for (Map.Entry<String, List<String>> entry : map.entrySet()) {

                    String districtName = entry.getKey().trim();
                    List<String> blocks = entry.getValue();

                    District district = new District();
                    district.setName(districtName);
                    district = districtRepo.save(district);

                    if (blocks != null) {
                        for (String blockName : blocks) {
                            Block block = new Block();
                            block.setName(blockName.trim());
                            block.setDistrict(district);
                            blockRepo.save(block);
                        }
                    }
                }

                System.out.println("✅ District & Block master data seeded successfully.");

            } catch (Exception ex) {
                System.err.println("❌ Failed to seed district/block data");
                ex.printStackTrace();
            }
        };
    }
}
