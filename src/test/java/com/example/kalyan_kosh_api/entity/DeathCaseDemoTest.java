//package com.example.kalyan_kosh_api.entity;
//
//import com.example.kalyan_kosh_api.repository.DeathCaseRepository;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import java.time.Instant;
//import java.util.List;
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@SpringBootTest
//@DisplayName("DeathCase Demo Test Cases")
//class DeathCaseDemoTest {
//
//    @Autowired
//    private DeathCaseRepository deathCaseRepository;
//
//    private DeathCase demoDeathCase1;
//    private DeathCase demoDeathCase2;
//    private DeathCase demoDeathCase3;
//
//    @BeforeEach
//    void setUp() {
//        // Create demo death cases with realistic data (no deletion)
//        // These will be saved to database for testing purposes
//        demoDeathCase1 = createDemoDeathCase1();
//        demoDeathCase2 = createDemoDeathCase2();
//        demoDeathCase3 = createDemoDeathCase3();
//
//        System.out.println("📋 Demo death cases prepared for database persistence...");
//    }
//
//    @Test
//    @DisplayName("Should create and save demo death case 1 successfully")
//    void testCreateDemoDeathCase1() {
//        // Given - demo death case 1 is already created in setUp()
//
//        // When
//        DeathCase savedDeathCase = deathCaseRepository.save(demoDeathCase1);
//
//        // Then
//        assertThat(savedDeathCase).isNotNull();
//        assertThat(savedDeathCase.getId()).isNotNull();
//        assertThat(savedDeathCase.getDeceasedName()).isEqualTo("राम कुमार शर्मा");
//        assertThat(savedDeathCase.getEmployeeCode()).isEqualTo("EMP001");
//        assertThat(savedDeathCase.getDepartment()).isEqualTo("Education Department");
//        assertThat(savedDeathCase.getDistrict()).isEqualTo("Bhopal");
//        assertThat(savedDeathCase.getNomineeName()).isEqualTo("सुनीता शर्मा");
//        assertThat(savedDeathCase.getNomineeAccountNumber()).isEqualTo("123456789012");
//        assertThat(savedDeathCase.getNomineeIfsc()).isEqualTo("SBIN0001234");
//        assertThat(savedDeathCase.getCaseMonth()).isEqualTo(12);
//        assertThat(savedDeathCase.getCaseYear()).isEqualTo(2025);
//        assertThat(savedDeathCase.getStatus()).isEqualTo(DeathCaseStatus.OPEN);
//        assertThat(savedDeathCase.getCreatedBy()).isEqualTo("admin");
//        assertThat(savedDeathCase.getCreatedAt()).isNotNull();
//
//        System.out.println("✅ Demo Death Case 1 saved to database successfully:");
//        printDeathCaseDetails(savedDeathCase);
//    }
//
//    @Test
//    @DisplayName("Should create and save demo death case 2 successfully")
//    void testCreateDemoDeathCase2() {
//        // Given - demo death case 2 is already created in setUp()
//
//        // When
//        DeathCase savedDeathCase = deathCaseRepository.save(demoDeathCase2);
//
//        // Then
//        assertThat(savedDeathCase).isNotNull();
//        assertThat(savedDeathCase.getId()).isNotNull();
//        assertThat(savedDeathCase.getDeceasedName()).isEqualTo("प्रीति पटेल");
//        assertThat(savedDeathCase.getEmployeeCode()).isEqualTo("EMP002");
//        assertThat(savedDeathCase.getDepartment()).isEqualTo("Health Department");
//        assertThat(savedDeathCase.getDistrict()).isEqualTo("Indore");
//        assertThat(savedDeathCase.getNomineeName()).isEqualTo("अरुण पटेल");
//        assertThat(savedDeathCase.getCaseMonth()).isEqualTo(1);
//        assertThat(savedDeathCase.getCaseYear()).isEqualTo(2026);
//        assertThat(savedDeathCase.getStatus()).isEqualTo(DeathCaseStatus.OPEN);
//
//        System.out.println("✅ Demo Death Case 2 saved to database successfully:");
//        printDeathCaseDetails(savedDeathCase);
//    }
//
//    @Test
//    @DisplayName("Should create and save demo death case 3 successfully")
//    void testCreateDemoDeathCase3() {
//        // Given - demo death case 3 is already created in setUp()
//
//        // When
//        DeathCase savedDeathCase = deathCaseRepository.save(demoDeathCase3);
//
//        // Then
//        assertThat(savedDeathCase).isNotNull();
//        assertThat(savedDeathCase.getId()).isNotNull();
//        assertThat(savedDeathCase.getDeceasedName()).isEqualTo("अनिल कुमार यादव");
//        assertThat(savedDeathCase.getStatus()).isEqualTo(DeathCaseStatus.CLOSED);
//
//        System.out.println("✅ Demo Death Case 3 saved to database successfully:");
//        printDeathCaseDetails(savedDeathCase);
//    }
//
//    @Test
//    @DisplayName("Should save multiple demo death cases and retrieve them")
//    void testCreateMultipleDemoDeathCases() {
//        // When
//        List<DeathCase> savedDeathCases = deathCaseRepository.saveAll(List.of(
//                demoDeathCase1, demoDeathCase2, demoDeathCase3
//        ));
//
//        // Then
//        assertThat(savedDeathCases).hasSize(3);
//
//        List<DeathCase> allDeathCases = deathCaseRepository.findAll();
//        assertThat(allDeathCases.size()).isGreaterThanOrEqualTo(3); // May have existing data
//
//        System.out.println("✅ Multiple Demo Death Cases saved to database successfully:");
//        savedDeathCases.forEach(this::printDeathCaseDetails);
//        System.out.println("📊 Total death cases in database: " + allDeathCases.size());
//    }
//
//    @Test
//    @DisplayName("Should find death cases by case month and year")
//    void testFindDeathCasesByMonthAndYear() {
//        // Given
//        deathCaseRepository.saveAll(List.of(demoDeathCase1, demoDeathCase2, demoDeathCase3));
//
//        // When & Then
//        long countFor2025Dec = deathCaseRepository.countByCaseMonthAndCaseYear(12, 2025);
//        assertThat(countFor2025Dec).isGreaterThanOrEqualTo(1); // At least our demo case
//
//        long countFor2026Jan = deathCaseRepository.countByCaseMonthAndCaseYear(1, 2026);
//        assertThat(countFor2026Jan).isGreaterThanOrEqualTo(2); // At least our demo cases
//
//        System.out.println("✅ Death cases found by month/year successfully (persistent data)");
//        System.out.println("   - December 2025: " + countFor2025Dec + " cases");
//        System.out.println("   - January 2026: " + countFor2026Jan + " cases");
//    }
//
//    @Test
//    @DisplayName("Should update demo death case status")
//    void testUpdateDeathCaseStatus() {
//        // Given
//        DeathCase savedCase = deathCaseRepository.save(demoDeathCase1);
//        entityManager.flush();
//
//        // When
//        savedCase.setStatus(DeathCaseStatus.CLOSED);
//        savedCase.setUpdatedBy("manager");
//        DeathCase updatedCase = deathCaseRepository.save(savedCase);
//        entityManager.flush();
//
//        // Then
//        Optional<DeathCase> foundCase = deathCaseRepository.findById(updatedCase.getId());
//        assertThat(foundCase).isPresent();
//        assertThat(foundCase.get().getStatus()).isEqualTo(DeathCaseStatus.CLOSED);
//        assertThat(foundCase.get().getUpdatedBy()).isEqualTo("manager");
//        assertThat(foundCase.get().getUpdatedAt()).isAfter(foundCase.get().getCreatedAt());
//
//        System.out.println("✅ Demo Death Case status updated successfully:");
//        printDeathCaseDetails(foundCase.get());
//    }
//
//    // Helper method to create demo death case 1
//    private DeathCase createDemoDeathCase1() {
//        return DeathCase.builder()
//                .deceasedName("राम कुमार शर्मा")
//                .employeeCode("EMP001")
//                .department("Education Department")
//                .district("Bhopal")
//                .nomineeName("सुनीता शर्मा")
//                .nomineeAccountNumber("123456789012")
//                .nomineeIfsc("SBIN0001234")
//                .caseMonth(12)
//                .caseYear(2025)
//                .status(DeathCaseStatus.OPEN)
//                .createdBy("admin")
//                .updatedBy("admin")
//                .createdAt(Instant.now())
//                .updatedAt(Instant.now())
//                .build();
//    }
//
//    // Helper method to create demo death case 2
//    private DeathCase createDemoDeathCase2() {
//        return DeathCase.builder()
//                .deceasedName("प्रीति पटेल")
//                .employeeCode("EMP002")
//                .department("Health Department")
//                .district("Indore")
//                .nomineeName("अरुण पटेल")
//                .nomineeAccountNumber("987654321098")
//                .nomineeIfsc("HDFC0002345")
//                .caseMonth(1)
//                .caseYear(2026)
//                .status(DeathCaseStatus.OPEN)
//                .createdBy("admin")
//                .updatedBy("admin")
//                .createdAt(Instant.now())
//                .updatedAt(Instant.now())
//                .build();
//    }
//
//    // Helper method to create demo death case 3
//    private DeathCase createDemoDeathCase3() {
//        return DeathCase.builder()
//                .deceasedName("अनिल कुमार यादव")
//                .employeeCode("EMP003")
//                .department("Agriculture Department")
//                .district("Jabalpur")
//                .nomineeName("मीरा यादव")
//                .nomineeAccountNumber("456789012345")
//                .nomineeIfsc("ICIC0003456")
//                .caseMonth(1)
//                .caseYear(2026)
//                .status(DeathCaseStatus.CLOSED)
//                .createdBy("admin")
//                .updatedBy("manager")
//                .createdAt(Instant.now().minusSeconds(3600)) // 1 hour ago
//                .updatedAt(Instant.now())
//                .build();
//    }
//
//    // Helper method to print death case details
//    private void printDeathCaseDetails(DeathCase deathCase) {
//        System.out.println("   ID: " + deathCase.getId());
//        System.out.println("   Deceased: " + deathCase.getDeceasedName());
//        System.out.println("   Employee Code: " + deathCase.getEmployeeCode());
//        System.out.println("   Department: " + deathCase.getDepartment());
//        System.out.println("   District: " + deathCase.getDistrict());
//        System.out.println("   Nominee: " + deathCase.getNomineeName());
//        System.out.println("   Account: " + deathCase.getNomineeAccountNumber());
//        System.out.println("   IFSC: " + deathCase.getNomineeIfsc());
//        System.out.println("   Case Month/Year: " + deathCase.getCaseMonth() + "/" + deathCase.getCaseYear());
//        System.out.println("   Status: " + deathCase.getStatus());
//        System.out.println("   Created By: " + deathCase.getCreatedBy());
//        System.out.println("   Created At: " + deathCase.getCreatedAt());
//        System.out.println("   ─────────────────────────────────────────");
//    }
//}
