# Testing Checklist for User Entity Updates
Date: 2026-01-02

## Prerequisites
1. ✅ Run database migration: `migration-add-user-fields.sql`
2. ✅ Restart the Spring Boot application
3. ✅ Verify JDK is properly configured (not JRE)

## Database Migration Testing

### Step 1: Apply Migration Script
```sql
-- Run the migration script
source migration-add-user-fields.sql;

-- Verify new columns exist
DESCRIBE users;

-- Expected new columns:
-- - father_name VARCHAR(255)
-- - joining_date DATE
-- - retirement_date DATE
-- - sankul_name VARCHAR(255)
-- - school_office_name VARCHAR(255) (should already exist)
```

## API Testing

### 1. Registration API Testing

**Endpoint:** `POST /api/auth/register`

**Test Case 1: Register with All New Fields**
```json
{
  "otp": {
    "mobileNumber": "9876543210"
  },
  "user": {
    "name": "Test",
    "surname": "User",
    "fatherName": "Father Name",
    "email": "test@example.com",
    "mobileNumber": "9876543210",
    "phoneNumber": "9876543210",
    "countryCode": "+91",
    "dateOfBirth": "1990-01-01",
    "joiningDate": "2015-06-15",
    "retirementDate": "2050-06-30",
    "gender": "MALE",
    "maritalStatus": "MARRIED",
    "homeAddress": "Test Address",
    "schoolOfficeName": "पदस्थ स्कूल/कार्यालय का नाम",
    "sankulName": "संकुल का नाम",
    "department": "Education",
    "departmentUniqueId": "TEST12345",
    "departmentState": "Madhya Pradesh",
    "departmentSambhag": "Chambal",
    "departmentDistrict": "Bhind",
    "departmentBlock": "Gohad",
    "nominee1Name": "Nominee 1",
    "nominee1Relation": "पुत्र",
    "nominee2Name": "Nominee 2",
    "nominee2Relation": "पत्नी",
    "password": "Test@123",
    "acceptedTerms": true
  }
}
```

**Expected Response:**
- Status: 200 OK
- Response contains: JWT token and user details
- User ID should be in PMUMS2024XXXXX format
- All new fields should be populated in the response

**Verify in Response:**
- ✅ `fatherName` is present
- ✅ `joiningDate` is present (LocalDate format)
- ✅ `retirementDate` is present (LocalDate format)
- ✅ `schoolOfficeName` is present
- ✅ `sankulName` is present
- ✅ `username` field is NOT present (removed)

### 2. Login API Testing

**Endpoint:** `POST /api/auth/login`

**Test Case 1: Login with Email**
```json
{
  "email": "test@example.com",
  "password": "Test@123"
}
```

**Expected Response:**
- Status: 200 OK
- Response contains: JWT token and user details
- All user fields including new ones should be populated

**Verify:**
- ✅ Login works with email (not username)
- ✅ Response contains all new fields
- ✅ JWT token is valid

### 3. Update User API Testing

**Endpoint:** `PUT /api/users/{userId}`

**Test Case 1: Update New Fields**
```json
{
  "fatherName": "Updated Father Name",
  "joiningDate": "2016-01-01",
  "retirementDate": "2051-01-01",
  "schoolOfficeName": "Updated School Name",
  "sankulName": "Updated Sankul Name"
}
```

**Expected Response:**
- Status: 200 OK
- All updated fields are reflected in the response

### 4. Get User API Testing

**Endpoint:** `GET /api/users/{userId}`

**Expected Response:**
- Status: 200 OK
- User details include all new fields:
  - `fatherName`
  - `joiningDate`
  - `retirementDate`
  - `schoolOfficeName`
  - `sankulName`
- No `username` field in response

### 5. Get All Users API Testing

**Endpoint:** `GET /api/users/`

**Expected Response:**
- Status: 200 OK
- Array of users, each containing new fields
- Verify pagination works correctly

## Email OTP Verification Testing

**Endpoint:** `POST /api/auth/email-otp/send`

```json
{
  "email": "test@example.com"
}
```

**Expected:** OTP sent successfully to email

**Endpoint:** `POST /api/auth/email-otp/verify`

```json
{
  "email": "test@example.com",
  "otp": "123456"
}
```

**Expected:** OTP verified successfully

## Database Verification

After registration/update, verify in database:

```sql
SELECT 
  id, 
  name, 
  surname, 
  father_name,
  email,
  joining_date,
  retirement_date,
  school_office_name,
  sankul_name
FROM users 
WHERE email = 'test@example.com';
```

**Expected:**
- All new fields should have values
- `username` column should be NULL or removed
- Dates should be in proper DATE format

## Security Testing

### Test 1: Authentication with Email
- ✅ Login works with email
- ✅ JWT token contains userId (not username)
- ✅ JWT token can be used for authenticated requests

### Test 2: Public Endpoints
- ✅ `/api/auth/register` is publicly accessible
- ✅ `/api/auth/login` is publicly accessible
- ✅ `/api/locations/**` is publicly accessible
- ✅ `/api/auth/email-otp/**` is publicly accessible

### Test 3: Protected Endpoints
- ✅ `/api/users/**` requires authentication
- ✅ `/api/receipts/**` requires authentication
- ✅ `/api/admin/**` requires ADMIN role

## Edge Cases to Test

### 1. Optional Fields
- Register without optional fields (fatherName, joiningDate, retirementDate, sankulName)
- Should succeed without errors

### 2. Date Format Validation
- Invalid date format: `"joiningDate": "invalid"`
- Should return 400 Bad Request with proper error message

### 3. Existing Users
- Verify existing users (registered before migration) still work
- They will have NULL values for new fields
- Update API should work to populate new fields

### 4. Email Uniqueness
- Try registering with duplicate email
- Should return 400/409 with appropriate error

## Frontend Integration Checklist

Update frontend forms to include:
- ✅ Father Name input field (पिता का नाम)
- ✅ Joining Date picker (सेवा में प्रवेश तिथि)
- ✅ Retirement Date picker (सेवानिवृत्ति तिथि)
- ✅ School/Office Name input (पदस्थ स्कूल/कार्यालय का नाम)
- ✅ Sankul Name input (संकुल का नाम)
- ✅ Remove username field from all forms
- ✅ Use email for login (not username)

## Confirmation Email Testing

After successful registration, verify:
- ✅ Confirmation email is sent
- ✅ Email contains PMUMS registration number
- ✅ Email contains all necessary information

## Known Issues

1. **JDK Requirement:** Maven compilation requires JDK (not JRE)
   - Solution: Ensure JAVA_HOME points to JDK installation

2. **Email Service:** If email sending fails, OTP is printed to console in DEV mode
   - Check application.properties for email configuration

## Success Criteria

✅ All API endpoints work correctly with new fields
✅ Database schema is updated
✅ No compilation errors
✅ Authentication works with email
✅ Existing functionality is not broken
✅ New fields are properly validated
✅ Frontend can integrate with updated API

---

**Testing Status:** Ready for testing after database migration
**Last Updated:** 2026-01-02

