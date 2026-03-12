# PMUMS Kalyan Kosh API - Updated Documentation

## Overview
This is the backend API for PMUMS (प्राथमिक–माध्यमिक–उच्च–माध्यमिक शिक्षक संघ, मध्यप्रदेश) Kalyan Kosh system.

**Latest Update**: January 2, 2026
- Authentication now uses **email** instead of username
- New user fields added: fatherName, joiningDate, retirementDate, sankulName
- User ID format: PMUMS2024XXXXX (auto-generated)

---

## Base URL
```
http://localhost:8080
```

---

## Authentication Endpoints

### 1. Register New User
**Endpoint**: `POST /api/auth/register`

**Request Body**:
```json
{
  "name": "Aman",
  "surname": "Soni",
  "fatherName": "Father Name",
  "email": "user@example.com",
  "password": "Password@123",
  "mobileNumber": "9876543210",
  "phoneNumber": "9876543210",
  "countryCode": "+91",
  "dateOfBirth": "1990-01-15",
  "joiningDate": "2020-01-15",
  "retirementDate": "2050-01-15",
  "gender": "MALE",
  "maritalStatus": "MARRIED",
  "homeAddress": "123 Main St",
  "schoolOfficeName": "ABC School",
  "sankulName": "XYZ Sankul",
  "department": "Education",
  "departmentUniqueId": "DEPT001",
  "departmentDistrict": "Indore",
  "departmentBlock": "Block1",
  "nominee1Name": "Nominee 1",
  "nominee1Relation": "पत्नी",
  "nominee2Name": "Nominee 2",
  "nominee2Relation": "माता",
  "acceptedTerms": true
}
```

**Success Response** (200):
```json
{
  "message": "User registered successfully with ID: PMUMS202458108"
}
```

**Note**: A confirmation email will be sent to the registered email address.

---

### 2. Login
**Endpoint**: `POST /api/auth/login`

**Request Body**:
```json
{
  "email": "user@example.com",
  "password": "Password@123"
}
```

**Success Response** (200):
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "user": {
    "id": "PMUMS202458108",
    "name": "Aman",
    "surname": "Soni",
    "fatherName": "Father Name",
    "email": "user@example.com",
    "phoneNumber": "9876543210",
    "mobileNumber": "9876543210",
    "gender": "MALE",
    "maritalStatus": "MARRIED",
    "dateOfBirth": "1990-01-15",
    "joiningDate": "2020-01-15",
    "retirementDate": "2050-01-15",
    "schoolOfficeName": "ABC School",
    "sankulName": "XYZ Sankul",
    "department": "Education",
    "departmentUniqueId": "DEPT001",
    "role": "ROLE_USER",
    "createdAt": "2026-01-02T10:30:00Z"
  }
}
```

**Error Response** (403):
```json
{
  "error": "Invalid credentials"
}
```

---

### 3. Send OTP (for registration)
**Endpoint**: `POST /api/auth/otp/send`

**Request Body**:
```json
{
  "email": "user@example.com"
}
```

**Success Response** (200):
```json
{
  "message": "OTP sent successfully"
}
```

---

### 4. Send Email OTP
**Endpoint**: `POST /api/auth/email-otp/send`

**Request Body**:
```json
{
  "email": "user@example.com"
}
```

**Success Response** (200):
```json
{
  "message": "OTP sent successfully to email"
}
```

---

### 5. Verify Email OTP
**Endpoint**: `POST /api/auth/email-otp/verify`

**Request Body**:
```json
{
  "email": "user@example.com",
  "otp": "123456"
}
```

**Success Response** (200):
```json
{
  "message": "Email verified successfully"
}
```

---

## Location Endpoints

### 1. Get Complete Location Hierarchy
**Endpoint**: `GET /api/locations/hierarchy`

**Success Response** (200):
```json
[
  {
    "id": "state-uuid",
    "name": "Madhya Pradesh",
    "code": "MP",
    "sambhags": [
      {
        "id": "sambhag-uuid",
        "name": "Indore",
        "districts": [
          {
            "id": "district-uuid",
            "name": "Indore",
            "blocks": [
              {
                "id": "block-uuid",
                "name": "Block 1"
              }
            ]
          }
        ]
      }
    ]
  }
]
```

**Description**: Returns complete State → Sambhag → District → Block hierarchy.

---

### 2. Get All States
**Endpoint**: `GET /api/locations/states`

**Success Response** (200):
```json
[
  {
    "id": "state-uuid",
    "name": "Madhya Pradesh",
    "code": "MP"
  }
]
```

---

### 3. Get Sambhags by State
**Endpoint**: `GET /api/locations/states/{stateId}/sambhags`

**Success Response** (200):
```json
[
  {
    "id": "sambhag-uuid",
    "name": "Indore",
    "stateId": "state-uuid"
  }
]
```

---

### 4. Get Districts by Sambhag
**Endpoint**: `GET /api/locations/sambhags/{sambhagId}/districts`

**Success Response** (200):
```json
[
  {
    "id": "district-uuid",
    "name": "Indore",
    "sambhagId": "sambhag-uuid"
  }
]
```

---

### 5. Get Blocks by District
**Endpoint**: `GET /api/locations/districts/{districtId}/blocks`

**Success Response** (200):
```json
[
  {
    "id": "block-uuid",
    "name": "Block 1",
    "districtId": "district-uuid"
  }
]
```

---

## Receipt Endpoints

### 1. Upload Receipt
**Endpoint**: `POST /api/receipts`

**Headers**:
```
Authorization: Bearer {JWT_TOKEN}
Content-Type: multipart/form-data
```

**Form Data**:
- `deathCaseId` (Long): Death case ID
- `amount` (Double): Contribution amount
- `paymentDate` (String): Payment date (yyyy-MM-dd)
- `comment` (String): Additional comments
- `file` (File): Receipt image/document

**Success Response** (200):
```json
{
  "message": "Receipt uploaded successfully",
  "receiptId": 123
}
```

---

### 2. Get User Receipts
**Endpoint**: `GET /api/receipts/user/{userId}`

**Headers**:
```
Authorization: Bearer {JWT_TOKEN}
```

**Success Response** (200):
```json
[
  {
    "id": 123,
    "userId": "PMUMS202458108",
    "deathCaseId": 1,
    "amount": 500.0,
    "paymentDate": "2026-01-01",
    "comment": "Contribution for death case",
    "status": "PENDING",
    "uploadedAt": "2026-01-02T10:00:00Z"
  }
]
```

---

## User Management Endpoints

### 1. Get All Users (Admin/Manager)
**Endpoint**: `GET /api/users/`

**Headers**:
```
Authorization: Bearer {JWT_TOKEN}
```

**Success Response** (200):
```json
[
  {
    "id": "PMUMS202458108",
    "name": "Aman",
    "surname": "Soni",
    "email": "user@example.com",
    "role": "ROLE_USER"
  }
]
```

---

### 2. Get User by ID
**Endpoint**: `GET /api/users/{userId}`

**Headers**:
```
Authorization: Bearer {JWT_TOKEN}
```

**Success Response** (200):
```json
{
  "id": "PMUMS202458108",
  "name": "Aman",
  "surname": "Soni",
  "fatherName": "Father Name",
  "email": "user@example.com",
  "phoneNumber": "9876543210",
  "mobileNumber": "9876543210",
  "gender": "MALE",
  "role": "ROLE_USER",
  "createdAt": "2026-01-02T10:30:00Z"
}
```

---

### 3. Update User
**Endpoint**: `PUT /api/users/{userId}`

**Headers**:
```
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json
```

**Request Body**: (Same as registration, but all fields optional)

---

## Death Case Endpoints

### 1. Create Death Case (Admin)
**Endpoint**: `POST /api/admin/death-cases`

**Headers**:
```
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json
```

**Request Body**:
```json
{
  "deceasedName": "John Doe",
  "employeeCode": "EMP001",
  "department": "Education",
  "district": "Indore",
  "nomineeName": "Jane Doe",
  "nomineeAccountNumber": "1234567890",
  "nomineeIfsc": "SBIN0001234",
  "caseMonth": 1,
  "caseYear": 2026
}
```

---

## Important Notes

### Authentication
- All endpoints except `/api/auth/**` and `/api/locations/**` require JWT token
- Token should be included in `Authorization` header as `Bearer {token}`
- Token expires after 24 hours

### User ID Format
- Auto-generated format: `PMUMS2024XXXXX`
- Starting number: 58108
- Counter never resets and persists across application restarts

### Email Confirmation
- After successful registration, a confirmation email is sent
- Email contains registration number and important information in Hindi

### CORS
- CORS is enabled for all origins (`*`)
- In production, should be restricted to specific domains

### Database
- MySQL database required
- Run migration script `migration-user-entity-updates.sql` before first use

---

## Error Responses

### 400 Bad Request
```json
{
  "error": "Validation error",
  "details": "Field 'email' is required"
}
```

### 401 Unauthorized
```json
{
  "error": "Unauthorized",
  "message": "Invalid or missing token"
}
```

### 403 Forbidden
```json
{
  "error": "Forbidden",
  "message": "Access denied"
}
```

### 404 Not Found
```json
{
  "error": "Not found",
  "message": "Resource not found"
}
```

### 500 Internal Server Error
```json
{
  "error": "Internal server error",
  "message": "An unexpected error occurred"
}
```

---

## Development Setup

### Prerequisites
- Java 17 (JDK)
- Maven
- MySQL 8.0+

### Running the Application
```bash
# Set JAVA_HOME
$env:JAVA_HOME = "C:\Users\shub\.jdks\corretto-17.0.9"

# Compile
./mvnw clean compile

# Run
./mvnw spring-boot:run
```

### Configuration
Edit `src/main/resources/application.properties`:
```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/kalyan_kosh
spring.datasource.username=your_username
spring.datasource.password=your_password

# Email (for confirmation emails)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your_email@gmail.com
spring.mail.password=your_app_password
```

---

## Contact & Support

For issues or questions, please contact the development team.

**Last Updated**: January 2, 2026

