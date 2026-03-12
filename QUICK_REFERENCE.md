# 🚀 Quick Reference Guide - PMUMS Kalyan Kosh API

## Critical Changes (Must Read!)

### ⚠️ BREAKING CHANGE: Authentication Now Uses Email
```diff
- Login with username
+ Login with email
```

**Before:**
```json
{ "username": "user123", "password": "pass" }
```

**Now:**
```json
{ "email": "user@example.com", "password": "pass" }
```

---

## Quick Start

### 1. Compile & Run
```bash
$env:JAVA_HOME = "C:\Users\shub\.jdks\corretto-17.0.9"
cd C:\Users\shub\Downloads\kalyan-kosh-api
./mvnw clean compile
./mvnw spring-boot:run
```

### 2. Test API
```bash
.\test-api.ps1
```

---

## New User Fields

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `fatherName` | String | ✅ Yes | Father's name |
| `joiningDate` | Date | ❌ No | Date of joining (yyyy-MM-dd) |
| `retirementDate` | Date | ❌ No | Retirement date (yyyy-MM-dd) |
| `sankulName` | String | ❌ No | संकुल का नाम |

---

## Quick API Test

### Test Login (Should fail without user):
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"Test@123"}'
```

### Test Location API (Public):
```bash
curl http://localhost:8080/api/locations/states
```

### Register User:
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test",
    "surname": "User",
    "fatherName": "Test Father",
    "email": "newuser@example.com",
    "password": "Test@123",
    "mobileNumber": "9876543210",
    "phoneNumber": "9876543210",
    "countryCode": "+91",
    "gender": "MALE",
    "maritalStatus": "SINGLE",
    "homeAddress": "Test Address",
    "schoolOfficeName": "Test School",
    "sankulName": "Test Sankul",
    "department": "Education",
    "departmentUniqueId": "DEPT001",
    "acceptedTerms": true
  }'
```

---

## Database Migration

**Run this BEFORE starting the application:**
```sql
-- In MySQL
source migration-user-entity-updates.sql
```

**Or manually:**
```sql
ALTER TABLE users ADD COLUMN father_name VARCHAR(255);
ALTER TABLE users ADD COLUMN joining_date DATE;
ALTER TABLE users ADD COLUMN retirement_date DATE;
ALTER TABLE users ADD COLUMN sankul_name VARCHAR(255);
ALTER TABLE users MODIFY COLUMN email VARCHAR(255) NOT NULL;
ALTER TABLE users ADD CONSTRAINT uk_users_email UNIQUE (email);
```

---

## User ID Format

- **Format**: `PMUMS2024XXXXX`
- **Starting Number**: 58108
- **Auto-incremented**: Never resets
- **Example**: PMUMS202458108, PMUMS202458109, etc.

---

## Important Files

| File | Purpose |
|------|---------|
| `COMPLETION_REPORT.md` | Full completion report |
| `API_DOCUMENTATION_UPDATED.md` | Complete API docs |
| `USER_ENTITY_CHANGES_SUMMARY.md` | Entity changes summary |
| `migration-user-entity-updates.sql` | Database migration |
| `test-api.ps1` | API test script |

---

## Common Issues

### 1. "Port 8080 was already in use"
**Solution**: Use the stop script first, then start
```bash
.\stop-app.ps1
.\start-app.ps1
```

### 2. "Could not find or load main class"
**Solution**: Compile first with `./mvnw clean compile`

### 3. "No compiler is provided"
**Solution**: Set JAVA_HOME to JDK (not JRE)
```bash
$env:JAVA_HOME = "C:\Users\shub\.jdks\corretto-17.0.9"
```

### 4. Login fails with 403
**Cause**: Either user doesn't exist or wrong password
**Solution**: Register a new user first

### 5. Email not sending
**Cause**: SMTP not configured
**Solution**: Configure `spring.mail.*` in `application.properties`

---

## Frontend Changes Required

### 1. Login Form
```javascript
// Change this field name
- <input name="username" />
+ <input name="email" type="email" />
```

### 2. Registration Form
Add these new fields:
- Father Name (required)
- Joining Date (optional)
- Retirement Date (optional)
- Sankul Name (optional)

### 3. User Display
Remove references to `username`, use `email` or `id` instead.

---

## Public Endpoints (No Auth)

- `/api/auth/login`
- `/api/auth/register`
- `/api/auth/otp/**`
- `/api/auth/email-otp/**`
- `/api/locations/**`

## Protected Endpoints (Auth Required)

- `/api/users/**`
- `/api/receipts/**`
- `/api/admin/**`

---

## Quick Postman Collection

### 1. Login
```
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "Password@123"
}
```

### 2. Get Locations (Copy token from login)
```
GET http://localhost:8080/api/locations/hierarchy
```

### 3. Get User Profile (Protected)
```
GET http://localhost:8080/api/users/PMUMS202458108
Authorization: Bearer YOUR_JWT_TOKEN_HERE
```

---

## Status

✅ **ALL CHANGES COMPLETE**
✅ **COMPILATION SUCCESSFUL**
✅ **READY TO TEST & DEPLOY**

---

**Last Updated**: January 2, 2026

