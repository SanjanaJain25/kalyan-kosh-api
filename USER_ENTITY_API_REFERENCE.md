# Updated User Entity - API Quick Reference
Date: 2026-01-02

## User Object Structure

```json
{
  "id": "PMUMS202458108",
  "name": "Aman",
  "surname": "Soni",
  "fatherName": "Father Name",
  "email": "user@example.com",
  "phoneNumber": "6232983739",
  "mobileNumber": "6232983739",
  "countryCode": "+91",
  "gender": "MALE",
  "maritalStatus": "MARRIED",
  "homeAddress": "Address here",
  "dateOfBirth": "1990-01-01",
  "joiningDate": "2015-06-15",
  "retirementDate": "2050-06-30",
  "schoolOfficeName": "पदस्थ स्कूल/कार्यालय का नाम",
  "sankulName": "संकुल का नाम",
  "department": "Education",
  "departmentUniqueId": "EMP12345",
  "departmentState": "Madhya Pradesh",
  "departmentSambhag": "Chambal",
  "departmentDistrict": "Bhind",
  "departmentBlock": "Gohad",
  "nominee1Name": "Nominee 1",
  "nominee1Relation": "पुत्र",
  "nominee2Name": "Nominee 2",
  "nominee2Relation": "पत्नी",
  "acceptedTerms": true,
  "role": "ROLE_USER",
  "createdAt": "2025-12-27T10:30:00Z"
}
```

## Field Descriptions

| Field | Type | Required | Description | Hindi Label |
|-------|------|----------|-------------|-------------|
| `id` | String | Auto-generated | User ID (PMUMS format) | पंजीकरण संख्या |
| `name` | String | Yes | First name | नाम |
| `surname` | String | Yes | Last name | उपनाम |
| `fatherName` | String | No | Father's name | पिता का नाम |
| `email` | String | Yes | Email (used for login) | ईमेल |
| `phoneNumber` | String | Yes | Phone number | फोन नंबर |
| `mobileNumber` | String | Yes | Mobile number | मोबाइल नंबर |
| `countryCode` | String | No | Country code | देश कोड |
| `gender` | String | Yes | MALE/FEMALE | लिंग |
| `maritalStatus` | String | Yes | MARRIED/SINGLE | वैवाहिक स्थिति |
| `homeAddress` | String | No | Home address | घर का पता |
| `dateOfBirth` | Date | No | Date of birth | जन्म तिथि |
| `joiningDate` | Date | No | Service joining date | सेवा में प्रवेश तिथि |
| `retirementDate` | Date | No | Retirement date | सेवानिवृत्ति तिथि |
| `schoolOfficeName` | String | No | Posted school/office name | पदस्थ स्कूल/कार्यालय का नाम |
| `sankulName` | String | No | Cluster name | संकुल का नाम |
| `department` | String | No | Department | विभाग |
| `departmentUniqueId` | String | Yes | Unique employee ID | कर्मचारी विशिष्ट आईडी |
| `departmentState` | String | No | State name | राज्य |
| `departmentSambhag` | String | No | Sambhag name | संभाग |
| `departmentDistrict` | String | No | District name | जिला |
| `departmentBlock` | String | No | Block name | ब्लॉक |
| `nominee1Name` | String | No | First nominee name | नामांकित 1 का नाम |
| `nominee1Relation` | String | No | First nominee relation | नामांकित 1 का संबंध |
| `nominee2Name` | String | No | Second nominee name | नामांकित 2 का नाम |
| `nominee2Relation` | String | No | Second nominee relation | नामांकित 2 का संबंध |
| `acceptedTerms` | Boolean | Yes | Terms acceptance | शर्तें स्वीकार की |
| `role` | String | Auto-set | ROLE_USER/ROLE_ADMIN | भूमिका |
| `createdAt` | Timestamp | Auto-generated | Registration date | पंजीकरण तिथि |

## Important Changes

### ❌ REMOVED Fields:
- `username` - No longer exists, use `email` for authentication

### ✅ NEW Fields:
- `fatherName` - Father's name
- `joiningDate` - Date of joining service
- `retirementDate` - Date of retirement
- `sankulName` - Cluster name (संकुल का नाम)

### ✅ RETAINED Fields:
- `schoolOfficeName` - Posted school/office name (पदस्थ स्कूल/कार्यालय का नाम)

## Date Format

All date fields use ISO 8601 format:
- **Input (String):** `"yyyy-MM-dd"` (e.g., `"2015-06-15"`)
- **Output (LocalDate):** `"yyyy-MM-dd"` (e.g., `"2015-06-15"`)

## Authentication

**Login Credentials:**
- **Username Field:** ❌ NO (removed)
- **Email Field:** ✅ YES (use email for login)
- **Password:** Required

**Example Login Request:**
```json
{
  "email": "user@example.com",
  "password": "Password@123"
}
```

## API Endpoints Using User Entity

### 1. Registration
- **POST** `/api/auth/register`
- Body: `OtpRegisterRequest` (contains `RegisterRequest`)

### 2. Login
- **POST** `/api/auth/login`
- Body: `LoginRequest` (email + password)
- Returns: JWT token + user details

### 3. Get User
- **GET** `/api/users/{userId}`
- Returns: Complete user object

### 4. Update User
- **PUT** `/api/users/{userId}`
- Body: `UpdateUserRequest` (partial update supported)

### 5. Get All Users
- **GET** `/api/users/`
- Returns: Array of user objects

## Validation Rules

1. **Email:** Must be valid email format, unique
2. **Mobile Number:** Required, used for OTP
3. **Password:** Minimum requirements enforced
4. **Date Format:** Must be `yyyy-MM-dd` for input
5. **Gender:** Must be MALE or FEMALE
6. **Marital Status:** Must be MARRIED or SINGLE
7. **Department Unique ID:** Must be unique across system

## Frontend Integration Notes

### Registration Form Updates Required:
1. ✅ Add "Father Name" input field
2. ✅ Add "Joining Date" date picker
3. ✅ Add "Retirement Date" date picker
4. ✅ Keep "School/Office Name" field (पदस्थ स्कूल/कार्यालय का नाम)
5. ✅ Add "Sankul Name" field (संकुल का नाम)
6. ❌ Remove "Username" field
7. ✅ Use "Email" for login

### API Client Updates:
```javascript
// OLD - Don't use this
const loginData = {
  username: "user123",  // ❌ WRONG
  password: "password"
};

// NEW - Use this
const loginData = {
  email: "user@example.com",  // ✅ CORRECT
  password: "password"
};
```

## Example Registration Payload

```json
{
  "otp": {
    "mobileNumber": "9876543210"
  },
  "user": {
    "name": "Aman",
    "surname": "Soni",
    "fatherName": "Ram Soni",
    "email": "aman@example.com",
    "mobileNumber": "9876543210",
    "phoneNumber": "9876543210",
    "countryCode": "+91",
    "dateOfBirth": "1990-05-15",
    "joiningDate": "2015-06-15",
    "retirementDate": "2050-06-30",
    "gender": "MALE",
    "maritalStatus": "MARRIED",
    "homeAddress": "123 Main Street",
    "schoolOfficeName": "प्राथमिक विद्यालय, गोहद",
    "sankulName": "संकुल केंद्र, गोहद",
    "department": "Education",
    "departmentUniqueId": "EMP12345",
    "departmentState": "Madhya Pradesh",
    "departmentSambhag": "Chambal",
    "departmentDistrict": "Bhind",
    "departmentBlock": "Gohad",
    "nominee1Name": "Suresh Soni",
    "nominee1Relation": "पुत्र",
    "nominee2Name": "Rita Soni",
    "nominee2Relation": "पत्नी",
    "password": "Secure@123",
    "acceptedTerms": true
  }
}
```

---

**Version:** 2.0 (Updated with new fields)
**Last Updated:** 2026-01-02

