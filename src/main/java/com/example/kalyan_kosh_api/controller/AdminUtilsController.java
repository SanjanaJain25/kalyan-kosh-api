package com.example.kalyan_kosh_api.controller;

import com.example.kalyan_kosh_api.entity.Block;
import com.example.kalyan_kosh_api.entity.District;
import com.example.kalyan_kosh_api.entity.Sambhag;
import com.example.kalyan_kosh_api.entity.State;
import com.example.kalyan_kosh_api.entity.User;
import com.example.kalyan_kosh_api.repository.BlockRepository;
import com.example.kalyan_kosh_api.repository.DistrictRepository;
import com.example.kalyan_kosh_api.repository.SambhagRepository;
import com.example.kalyan_kosh_api.repository.StateRepository;
import com.example.kalyan_kosh_api.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ADMIN UTILITY CONTROLLER
 * Use this to fix existing users who have NULL location data
 */
@RestController
@RequestMapping("/api/admin/utils")
@CrossOrigin(origins = "*")
public class AdminUtilsController {

    private final UserRepository userRepo;
    private final StateRepository stateRepo;
    private final SambhagRepository sambhagRepo;
    private final DistrictRepository districtRepo;
    private final BlockRepository blockRepo;

    public AdminUtilsController(UserRepository userRepo,
                                StateRepository stateRepo,
                                SambhagRepository sambhagRepo,
                                DistrictRepository districtRepo,
                                BlockRepository blockRepo) {
        this.userRepo = userRepo;
        this.stateRepo = stateRepo;
        this.sambhagRepo = sambhagRepo;
        this.districtRepo = districtRepo;
        this.blockRepo = blockRepo;
    }

    /**
     * Fix all users who have NULL location data
     * Sets them to: मध्य प्रदेश → इंदौर संभाग → इंदौर → इंदौर
     *
     * Call: POST http://localhost:8080/api/admin/utils/fix-null-locations
     */
    @PostMapping("/fix-null-locations")
    public ResponseEntity<Map<String, Object>> fixNullLocations() {
        Map<String, Object> result = new HashMap<>();
        int fixedCount = 0;

        try {
            // Get default location hierarchy - automatically picks first available from database
            State state = stateRepo.findAll().stream()
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No state found in database!"));

            Sambhag sambhag = sambhagRepo.findAll().stream()
                    .filter(s -> s.getState().getId().equals(state.getId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No sambhag found for state: " + state.getName()));

            District district = districtRepo.findAll().stream()
                    .filter(d -> d.getSambhag().getId().equals(sambhag.getId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No district found for sambhag: " + sambhag.getName()));

            Block block = blockRepo.findAll().stream()
                    .filter(b -> b.getDistrict().getId().equals(district.getId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No block found for district: " + district.getName()));

            // Find all users with NULL location data
            List<User> allUsers = userRepo.findAll();

            for (User user : allUsers) {
                boolean needsUpdate = false;

                if (user.getDepartmentState() == null) {
                    user.setDepartmentState(state);
                    needsUpdate = true;
                }

                if (user.getDepartmentSambhag() == null) {
                    user.setDepartmentSambhag(sambhag);
                    needsUpdate = true;
                }

                if (user.getDepartmentDistrict() == null) {
                    user.setDepartmentDistrict(district);
                    needsUpdate = true;
                }

                if (user.getDepartmentBlock() == null) {
                    user.setDepartmentBlock(block);
                    needsUpdate = true;
                }

                if (needsUpdate) {
                    userRepo.save(user);
                    fixedCount++;
                }
            }

            result.put("success", true);
            result.put("message", "Successfully fixed NULL locations");
            result.put("fixedCount", fixedCount);
            result.put("defaultLocation", Map.of(
                    "state", state.getName(),
                    "sambhag", sambhag.getName(),
                    "district", district.getName(),
                    "block", block.getName()
            ));

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Error: " + e.getMessage());
            result.put("fixedCount", fixedCount);
            result.put("errorCount", 0);

            return ResponseEntity.status(500).body(result);
        }
    }

    /**
     * Check how many users have NULL location data
     *
     * Call: GET http://localhost:8080/api/admin/utils/check-null-locations
     */
    @GetMapping("/check-null-locations")
    public ResponseEntity<Map<String, Object>> checkNullLocations() {
        List<User> allUsers = userRepo.findAll();

        int totalUsers = allUsers.size();
        int nullStateCount = 0;
        int nullSambhagCount = 0;
        int nullDistrictCount = 0;
        int nullBlockCount = 0;

        for (User user : allUsers) {
            if (user.getDepartmentState() == null) nullStateCount++;
            if (user.getDepartmentSambhag() == null) nullSambhagCount++;
            if (user.getDepartmentDistrict() == null) nullDistrictCount++;
            if (user.getDepartmentBlock() == null) nullBlockCount++;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalUsers", totalUsers);
        result.put("usersWithNullState", nullStateCount);
        result.put("usersWithNullSambhag", nullSambhagCount);
        result.put("usersWithNullDistrict", nullDistrictCount);
        result.put("usersWithNullBlock", nullBlockCount);
        result.put("needsFix", nullStateCount > 0 || nullSambhagCount > 0 ||
                               nullDistrictCount > 0 || nullBlockCount > 0);

        return ResponseEntity.ok(result);
    }
}

