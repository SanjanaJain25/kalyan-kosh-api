package com.example.kalyan_kosh_api.service;

import com.example.kalyan_kosh_api.entity.DeathCase;
import com.example.kalyan_kosh_api.entity.DeathCaseStatus;
import com.example.kalyan_kosh_api.repository.DeathCaseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("Demo Death Case Integration Tests")
class DeathCaseDemoIntegrationTest {

    @Autowired
    private DeathCaseRepository deathCaseRepository;

    @BeforeEach
    void setUp() {
        // Prepare demo data for database persistence (no deletion)
        System.out.println("🏗️ Preparing demo death cases for database storage...");
    }

    @Test
    @DisplayName("Should create complete set of demo death cases for testing")
    void createCompleteDemoDeathCasesSet() {
        // Given
        List<DeathCase> demoDeathCases = createCompleteDemoSet();

        // When
        List<DeathCase> savedCases = deathCaseRepository.saveAll(demoDeathCases);

        // Then
        assertThat(savedCases).hasSize(10);

        long totalCount = deathCaseRepository.count();
        assertThat(totalCount).isGreaterThanOrEqualTo(10); // May have existing data

        // Verify different departments are represented
        List<String> departments = savedCases.stream()
                .map(DeathCase::getDepartment)
                .distinct()
                .toList();
        assertThat(departments).contains(
                "Education Department",
                "Health Department",
                "Agriculture Department",
                "Police Department",
                "Revenue Department"
        );

        // Verify different districts are represented
        List<String> districts = savedCases.stream()
                .map(DeathCase::getDistrict)
                .distinct()
                .toList();
        assertThat(districts).contains("Bhopal", "Indore", "Jabalpur", "Gwalior");

        // Print summary
        System.out.println("🎯 COMPLETE DEMO DEATH CASES SET SAVED TO DATABASE SUCCESSFULLY");
        System.out.println("═══════════════════════════════════════════════════════════════");
        System.out.println("💾 Total Demo Cases Added: " + savedCases.size());
        System.out.println("📊 Total Cases in Database: " + totalCount);
        System.out.println("🏢 Departments: " + departments.size());
        System.out.println("🌍 Districts: " + districts.size());
        System.out.println("📅 Time Range: December 2025 - February 2026");
        System.out.println("💿 Status: PERSISTENT DATA - SAVED TO DATABASE");
        System.out.println("═══════════════════════════════════════════════════════════════");

        // Print detailed information
        savedCases.forEach(this::printDeathCaseInfo);
    }

    @Test
    @DisplayName("Should demonstrate monthly statistics with demo data")
    void testMonthlyStatisticsWithDemoData() {
        // Given
        List<DeathCase> demoDeathCases = createCompleteDemoSet();
        deathCaseRepository.saveAll(demoDeathCases);

        // When & Then
        long dec2025Count = deathCaseRepository.countByCaseMonthAndCaseYear(12, 2025);
        long jan2026Count = deathCaseRepository.countByCaseMonthAndCaseYear(1, 2026);
        long feb2026Count = deathCaseRepository.countByCaseMonthAndCaseYear(2, 2026);

        // Verify counts
        assertThat(dec2025Count).isEqualTo(2);
        assertThat(jan2026Count).isEqualTo(4);
        assertThat(feb2026Count).isEqualTo(4);

        System.out.println("📈 MONTHLY STATISTICS DEMO");
        System.out.println("═══════════════════════════");
        System.out.println("📅 December 2025: " + dec2025Count + " cases");
        System.out.println("📅 January 2026:  " + jan2026Count + " cases");
        System.out.println("📅 February 2026: " + feb2026Count + " cases");
        System.out.println("📊 Total Cases:   " + (dec2025Count + jan2026Count + feb2026Count));
    }

    @Test
    @DisplayName("Should demonstrate status transitions with demo data")
    void testStatusTransitionsWithDemoData() {
        // Given
        List<DeathCase> demoDeathCases = createCompleteDemoSet();
        List<DeathCase> savedCases = deathCaseRepository.saveAll(demoDeathCases);

        // When - Update some cases to CLOSED status
        savedCases.stream()
                .limit(3)
                .forEach(deathCase -> {
                    deathCase.setStatus(DeathCaseStatus.CLOSED);
                    deathCase.setUpdatedBy("system_admin");
                });

        List<DeathCase> updatedCases = deathCaseRepository.saveAll(savedCases);

        // Then
        long openCases = updatedCases.stream()
                .filter(dc -> dc.getStatus() == DeathCaseStatus.OPEN)
                .count();
        long closedCases = updatedCases.stream()
                .filter(dc -> dc.getStatus() == DeathCaseStatus.CLOSED)
                .count();

        assertThat(openCases).isEqualTo(7);
        assertThat(closedCases).isEqualTo(3);

        System.out.println("🔄 STATUS TRANSITIONS DEMO");
        System.out.println("═══════════════════════════");
        System.out.println("🟢 Open Cases:   " + openCases);
        System.out.println("🔴 Closed Cases: " + closedCases);
        System.out.println("📊 Total Cases:  " + updatedCases.size());
    }

    // Helper method to create a complete demo dataset
    private List<DeathCase> createCompleteDemoSet() {
        return Arrays.asList(
                // December 2025 cases
                createDeathCase("राम कुमार शर्मा", "EMP001", "Education Department", "Bhopal",
                        "सुनीता शर्मा", "123456789012", "SBIN0001234", 12, 2025, DeathCaseStatus.OPEN),

                createDeathCase("प्रीति वर्मा", "EMP002", "Health Department", "Indore",
                        "राज वर्मा", "234567890123", "HDFC0002345", 12, 2025, DeathCaseStatus.OPEN),

                // January 2026 cases
                createDeathCase("अनिल कुमार यादव", "EMP003", "Agriculture Department", "Jabalpur",
                        "मीरा यादव", "345678901234", "ICIC0003456", 1, 2026, DeathCaseStatus.OPEN),

                createDeathCase("सुनीता देवी", "EMP004", "Police Department", "Gwalior",
                        "रामेश्वर सिंह", "456789012345", "AXIS0004567", 1, 2026, DeathCaseStatus.CLOSED),

                createDeathCase("विकास शुक्ला", "EMP005", "Revenue Department", "Bhopal",
                        "प्रिया शुक्ला", "567890123456", "PUNB0005678", 1, 2026, DeathCaseStatus.OPEN),

                createDeathCase("कमला बाई", "EMP006", "Education Department", "Indore",
                        "मोहन लाल", "678901234567", "SBIN0006789", 1, 2026, DeathCaseStatus.OPEN),

                // February 2026 cases
                createDeathCase("राजेश कुमार तिवारी", "EMP007", "Health Department", "Jabalpur",
                        "अनिता तिवारी", "789012345678", "HDFC0007890", 2, 2026, DeathCaseStatus.OPEN),

                createDeathCase("संगीता पटेल", "EMP008", "Agriculture Department", "Gwalior",
                        "अशोक पटेल", "890123456789", "ICIC0008901", 2, 2026, DeathCaseStatus.OPEN),

                createDeathCase("मुकेश यादव", "EMP009", "Police Department", "Bhopal",
                        "सुधा यादव", "901234567890", "AXIS0009012", 2, 2026, DeathCaseStatus.CLOSED),

                createDeathCase("रेखा सिंह", "EMP010", "Revenue Department", "Indore",
                        "दिनेश सिंह", "012345678901", "PUNB0001023", 2, 2026, DeathCaseStatus.OPEN)
        );
    }

    // Helper method to create individual death case
    private DeathCase createDeathCase(String deceasedName, String employeeCode, String department,
                                    String district, String nomineeName, String accountNumber,
                                    String ifsc, int month, int year, DeathCaseStatus status) {
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
                .createdBy("demo_admin")
                .updatedBy("demo_admin")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    // Helper method to print death case information
    private void printDeathCaseInfo(DeathCase deathCase) {
        System.out.println("📋 ID: " + deathCase.getId() +
                          " | 👤 " + deathCase.getDeceasedName() +
                          " | 🏢 " + deathCase.getDepartment() +
                          " | 🌍 " + deathCase.getDistrict() +
                          " | 📅 " + deathCase.getCaseMonth() + "/" + deathCase.getCaseYear() +
                          " | " + (deathCase.getStatus() == DeathCaseStatus.OPEN ? "🟢" : "🔴") + " " + deathCase.getStatus());
    }
}
