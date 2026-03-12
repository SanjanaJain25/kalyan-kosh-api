# 💾 Demo Death Case Test Cases - Database Persistence Guide

## Overview

I've created comprehensive test cases that **SAVE demo death cases to your database permanently**. These tests create realistic demo data that persists across application restarts, perfect for development and testing.

## 🚨 **IMPORTANT: DATA PERSISTENCE**

**⚠️ These tests SAVE data to your database - they do NOT delete it!**
- ✅ Data persists after test completion
- ✅ Data survives application restarts  
- ✅ Perfect for development and manual testing
- ✅ Creates realistic demo data you can use with receipt uploads

---

## 📁 Test Files Created

### 1. **DeathCaseDemoTest.java** 
**Location**: `src/test/java/com/example/kalyan_kosh_api/entity/`
**Type**: Entity Tests with Database Persistence

**Features**:
- ✅ Creates and **saves** individual death cases to database
- ✅ Creates and **saves** multiple death cases to database
- ✅ Tests repository queries with persistent data
- ✅ Tests status updates on real database records
- ✅ Uses realistic Indian names and data
- ✅ Uses `@SpringBootTest` for full database persistence
- 💾 **Data remains in database after test completion**

### 2. **DeathCaseDemoIntegrationTest.java**
**Location**: `src/test/java/com/example/kalyan_kosh_api/service/`
**Type**: Integration Tests with Database Persistence

**Features**:
- ✅ Creates and **permanently saves** complete demo dataset (10 death cases)
- ✅ Tests monthly statistics with persistent data
- ✅ Tests status transitions on real database records
- ✅ Uses `@SpringBootTest` for full database persistence
- ✅ Covers multiple departments and districts
- 💾 **All 10 demo records remain in database after test**

### 3. **DemoDeathCaseCreator.java**
**Location**: `src/test/java/com/example/kalyan_kosh_api/demo/`
**Type**: Demo Data Creator (Permanent Database Population)

**Features**:
- ✅ **Permanently saves** demo data to database
- ✅ Shows existing vs new record counts
- ✅ Detailed console output with statistics
- ✅ Perfect for development and QA testing
- 💾 **Creates permanent demo data for manual testing**

---

## 🚀 How to Run Tests

### Run Individual Test Classes

#### Option 1: Using Maven Command Line
```bash
# Run entity tests
mvn test -Dtest=DeathCaseDemoTest

# Run integration tests  
mvn test -Dtest=DeathCaseDemoIntegrationTest

# Run demo data creator
mvn test -Dtest=DemoDeathCaseCreator
```

#### Option 2: Using IDE
1. Right-click on any test class
2. Select "Run [TestClassName]"
3. View results in test console

#### Option 3: Run All Tests
```bash
mvn test
```

---

## 📊 Demo Data Details

### Sample Death Cases Created

| ID | Deceased Name | Department | District | Month/Year | Status |
|----|---------------|------------|----------|------------|--------|
| 1 | राम कुमार शर्मा | Education | Bhopal | 12/2025 | OPEN |
| 2 | प्रीति वर्मा | Health | Indore | 01/2026 | OPEN |
| 3 | अनिल कुमार यादव | Agriculture | Jabalpur | 01/2026 | CLOSED |
| 4 | सुनीता देवी | Police | Gwalior | 01/2026 | OPEN |
| 5 | विकास शुक्ला | Revenue | Bhopal | 02/2026 | OPEN |

### Database Schema Coverage

**DeathCase Entity Fields**:
- ✅ `id` - Auto-generated Long ID
- ✅ `deceasedName` - Realistic Hindi names
- ✅ `employeeCode` - EMP001, EMP002, etc.
- ✅ `department` - Education, Health, Agriculture, Police, Revenue
- ✅ `district` - Bhopal, Indore, Jabalpur, Gwalior
- ✅ `nomineeName` - Hindi names for nominees
- ✅ `nomineeAccountNumber` - 12-digit bank account numbers
- ✅ `nomineeIfsc` - Valid IFSC codes (SBIN, HDFC, ICIC, AXIS, PUNB)
- ✅ `caseMonth` - 12, 1, 2 (Dec 2025, Jan 2026, Feb 2026)
- ✅ `caseYear` - 2025, 2026
- ✅ `status` - OPEN, CLOSED
- ✅ `createdBy` / `updatedBy` - admin, demo_creator, etc.
- ✅ `createdAt` / `updatedAt` - Instant timestamps

---

## 🧪 Test Scenarios Covered

### Entity Level Tests (DeathCaseDemoTest)

1. **Single Death Case Creation**
   - Creates death case with all fields
   - Validates all field values
   - Tests auto-generated ID

2. **Multiple Death Cases**
   - Creates 3 death cases simultaneously
   - Validates count and retrieval

3. **Repository Queries**
   - Tests `countByCaseMonthAndCaseYear()` method
   - Validates query results for different months

4. **Status Updates**
   - Updates OPEN case to CLOSED
   - Validates updatedAt timestamp changes

### Integration Level Tests (DeathCaseDemoIntegrationTest)

1. **Complete Demo Dataset**
   - Creates 10 diverse death cases
   - Validates department and district coverage
   - Tests full Spring Boot context

2. **Monthly Statistics**
   - Creates cases across multiple months
   - Tests statistical queries
   - Validates monthly counts

3. **Status Transitions**
   - Demonstrates OPEN → CLOSED transitions
   - Tests bulk status updates
   - Validates status distribution

### Demo Data Creation (DemoDeathCaseCreator)

1. **Database Population**
   - Creates realistic demo data
   - Populates database for manual testing
   - Provides detailed console output

---

## 📈 Expected Test Results

### Console Output Example

When running `DemoDeathCaseCreator`:

```
🚀 Saving demo death cases to database...
═══════════════════════════════════════════
📊 Existing death cases in database: 0
───────────────────────────────────────────
✅ Successfully saved 5 demo death cases to database!
📊 Database now contains 5 total death cases
➕ Added 5 new records
═══════════════════════════════════════════════════════════════
📋 ID: 1
   👤 Deceased: राम कुमार शर्मा
   🏢 Department: Education Department
   🌍 District: Bhopal
   💰 Nominee: सुनीता शर्मा
   🏦 Account: 123456789012
   📅 Case: 12/2025
   🟢 Status: OPEN
   ─────────────────────────────────────────
📊 STATISTICS
═══════════════
🟢 Open Cases: 4
🔴 Closed Cases: 1
📈 Total Cases: 5
🔍 January 2026 cases count: 3
═══════════════════════════════════════════════════════════════
🎉 Demo data has been permanently saved to database!
💿 Data will persist across application restarts
🔍 You can now test receipt uploads and other features
```

### Database Validation

After running tests, check database:

```sql
-- Check created death cases
SELECT id, deceased_name, department, district, case_month, case_year, status 
FROM death_case 
ORDER BY id;

-- Check monthly counts
SELECT case_month, case_year, COUNT(*) as case_count
FROM death_case 
GROUP BY case_month, case_year 
ORDER BY case_year, case_month;

-- Check status distribution  
SELECT status, COUNT(*) as count
FROM death_case
GROUP BY status;
```

---

## 🎯 Use Cases

### For Developers
- **Unit Testing**: Use `DeathCaseDemoTest` for entity validation
- **Integration Testing**: Use `DeathCaseDemoIntegrationTest` for service layer testing
- **Manual Testing**: Use `DemoDeathCaseCreator` to populate database

### For QA Teams  
- Run `DemoDeathCaseCreator` to create test data
- Use created data for manual testing of receipts, reports, etc.
- Validate death case CRUD operations

### For Business Users
- Demonstrate system capabilities with realistic data
- Show different departments and districts
- Validate monthly reporting features

---

## 🔧 Customization

### Adding More Demo Cases

Extend the demo dataset in any test file:

```java
private DeathCase createCustomDeathCase() {
    return DeathCase.builder()
            .deceasedName("Your Name")
            .employeeCode("EMP999")
            .department("Your Department")
            .district("Your District")
            .nomineeName("Nominee Name")
            .nomineeAccountNumber("999888777666")
            .nomineeIfsc("BANK0009999")
            .caseMonth(3)
            .caseYear(2026)
            .status(DeathCaseStatus.OPEN)
            .createdBy("custom_admin")
            .updatedBy("custom_admin")
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
}
```

### Adding New Test Scenarios

Add new test methods to existing classes:

```java
@Test
@DisplayName("Your custom test scenario")
void testCustomScenario() {
    // Given
    DeathCase customCase = createCustomDeathCase();
    
    // When  
    DeathCase saved = deathCaseRepository.save(customCase);
    
    // Then
    assertThat(saved.getId()).isNotNull();
    // Add your assertions
}
```

---

## 🗃️ Database Schema

The tests work with this DeathCase table structure:

```sql
CREATE TABLE death_case (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    deceased_name VARCHAR(255),
    employee_code VARCHAR(255), 
    department VARCHAR(255),
    district VARCHAR(255),
    nominee_name VARCHAR(255),
    nominee_account_number VARCHAR(255),
    nominee_ifsc VARCHAR(255),
    case_month INT,
    case_year INT,
    status VARCHAR(20),
    created_by VARCHAR(255),
    updated_by VARCHAR(255), 
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

---

## ✅ Verification Checklist

After running tests, verify:

- [ ] All test methods pass without errors
- [ ] Demo data is created in database 
- [ ] Console output shows detailed information
- [ ] Repository queries work correctly
- [ ] Status updates function properly
- [ ] Monthly statistics are accurate
- [ ] Hindi names display correctly
- [ ] Bank account details are valid format

---

## 🎉 Summary

The demo death case test suite provides:

✅ **Comprehensive Coverage** - Entity, integration, and demo data creation
✅ **Realistic Data** - Hindi names, valid bank details, multiple departments
✅ **Multiple Test Types** - Unit tests, integration tests, demo creators  
✅ **Easy Execution** - Run via Maven or IDE
✅ **Detailed Output** - Console logs with statistics and validation
✅ **Customizable** - Easy to extend and modify

**Ready to use for development, testing, and demonstration purposes!** 🚀
