package com.example.kalyan_kosh_api.demo;

import com.example.kalyan_kosh_api.entity.DeathCase;
import com.example.kalyan_kosh_api.entity.DeathCaseStatus;
import com.example.kalyan_kosh_api.repository.DeathCaseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;

/**
 * This test class creates demo death cases in the database for testing purposes.
 * Run this test to populate the database with sample data.
 *
 * To run this test:
 * mvn test -Dtest=DemoDeathCaseCreator
 *
 * Or in IDE: Right-click on class and "Run DemoDeathCaseCreator"
 */
@SpringBootTest
@ActiveProfiles("test") // You can create application-test.properties if needed
@DisplayName("Demo Death Case Creator")
class DemoDeathCaseCreator {

    @Autowired
    private DeathCaseRepository deathCaseRepository;

    @Test
    @DisplayName("🎯 CREATE DEMO DEATH CASES FOR TESTING")
    void createDemoDeathCases() {
        System.out.println("🚀 Creating demo death cases...");
        System.out.println("═══════════════════════════════════");

        // Create demo death cases
        List<DeathCase> demoDeathCases = List.of(
                createDemoDeathCase(
                        "राम कुमार शर्मा",
                        "EMP001",
                        "Education Department",
                        "Bhopal",
                        "सुनीता शर्मा",
                        "123456789012",
                        "SBIN0001234",
                        12, 2025,
                        DeathCaseStatus.OPEN
                ),
                createDemoDeathCase(
                        "प्रीति वर्मा",
                        "EMP002",
                        "Health Department",
                        "Indore",
                        "राज वर्मा",
                        "234567890123",
                        "HDFC0002345",
                        1, 2026,
                        DeathCaseStatus.OPEN
                ),
                createDemoDeathCase(
                        "अनिल कुमार यादव",
                        "EMP003",
                        "Agriculture Department",
                        "Jabalpur",
                        "मीरा यादव",
                        "345678901234",
                        "ICIC0003456",
                        1, 2026,
                        DeathCaseStatus.CLOSED
                ),
                createDemoDeathCase(
                        "सुनीता देवी",
                        "EMP004",
                        "Police Department",
                        "Gwalior",
                        "रामेश्वर सिंह",
                        "456789012345",
                        "AXIS0004567",
                        1, 2026,
                        DeathCaseStatus.OPEN
                ),
                createDemoDeathCase(
                        "विकास शुक्ला",
                        "EMP005",
                        "Revenue Department",
                        "Bhopal",
                        "प्रिया शुक्ला",
                        "567890123456",
                        "PUNB0005678",
                        2, 2026,
                        DeathCaseStatus.OPEN
                )
        );

        // Save to database
        List<DeathCase> savedCases = deathCaseRepository.saveAll(demoDeathCases);

        // Print results
        long newTotalCount = deathCaseRepository.count();
        System.out.println("✅ Successfully saved " + savedCases.size() + " demo death cases to database!");
        System.out.println("📊 Database now contains " + newTotalCount + " total death cases");
        System.out.println("➕ Added " + (newTotalCount - existingCount) + " new records");
        System.out.println("═══════════════════════════════════════════════════════════════");

        savedCases.forEach(deathCase -> {
            System.out.println("📋 ID: " + deathCase.getId());
            System.out.println("   👤 Deceased: " + deathCase.getDeceasedName());
            System.out.println("   🏢 Department: " + deathCase.getDepartment());
            System.out.println("   🌍 District: " + deathCase.getDistrict());
            System.out.println("   💰 Nominee: " + deathCase.getNomineeName());
            System.out.println("   🏦 Account: " + deathCase.getNomineeAccountNumber());
            System.out.println("   📅 Case: " + deathCase.getCaseMonth() + "/" + deathCase.getCaseYear());
            System.out.println("   " + (deathCase.getStatus() == DeathCaseStatus.OPEN ? "🟢" : "🔴") +
                             " Status: " + deathCase.getStatus());
            System.out.println("   ─────────────────────────────────────────");
        });

        // Print statistics
        long openCases = savedCases.stream().filter(dc -> dc.getStatus() == DeathCaseStatus.OPEN).count();
        long closedCases = savedCases.stream().filter(dc -> dc.getStatus() == DeathCaseStatus.CLOSED).count();

        System.out.println("📊 STATISTICS");
        System.out.println("═══════════════");
        System.out.println("🟢 Open Cases: " + openCases);
        System.out.println("🔴 Closed Cases: " + closedCases);
        System.out.println("📈 Total Cases: " + savedCases.size());
        System.out.println("═══════════════════════════════════════════════════════════════");
        System.out.println("🎉 Demo data has been permanently saved to database!");
        System.out.println("💿 Data will persist across application restarts");
        System.out.println("🔍 You can now test receipt uploads and other features");

        // Test repository method
        long jan2026Count = deathCaseRepository.countByCaseMonthAndCaseYear(1, 2026);
        System.out.println("🔍 January 2026 cases count: " + jan2026Count);
    }

    /**
     * Helper method to create a demo death case with all required fields
     */
    private DeathCase createDemoDeathCase(String deceasedName,
                                        String employeeCode,
                                        String department,
                                        String district,
                                        String nomineeName,
                                        String accountNumber,
                                        String ifsc,
                                        int month,
                                        int year,
                                        DeathCaseStatus status) {
        return DeathCase.builder()
                .deceasedName(deceasedName)
                .employeeCode(employeeCode)
                .department(department)
                .district(district)
                .nomineeName(nomineeName)
                .nomineeAccountNumber(accountNumber)
                .nomineeIfsc(ifsc)
                .caseMonth(month)
                .caseYear(year)
                .status(status)
                .createdBy("demo_creator")
                .updatedBy("demo_creator")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}
