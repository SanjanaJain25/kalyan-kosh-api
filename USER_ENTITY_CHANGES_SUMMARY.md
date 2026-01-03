# User Entity Changes Summary

## Date: January 2, 2026

## Overview
The User entity has been updated with new fields and the username field has been removed. The authentication system now uses email as the primary identifier for login.

## Changes Made

### 1. User Entity (`User.java`)
#### Added Fields:
- `fatherName` (String) - Father's name
- `joiningDate` (LocalDate) - Date of joining
- `retirementDate` (LocalDate) - Retirement date
- `sankulName` (String) - संकुल का नाम (Sankul name)

#### Modified Fields:
- `schoolOfficeName` - Now represents पदस्थ स्कूल/कार्यालय का नाम (Posted school/office name)

#### Removed Fields:
- `username` - No longer used, authentication is now based on email

### 2. Authentication Changes

#### Login System:
- **Previous**: Users logged in with username
- **Current**: Users login with **email** and **password**

#### LoginRequest DTO:
```java
{
  "email": "user@example.com",    // Changed from "username"
  "password": "password123"
}
```

#### LoginResponse:
```java
{
  "token": "JWT_TOKEN_HERE",
  "user": {
    "id": "PMUMS202458108",
    "name": "John",
    "surname": "Doe",
    "fatherName": "Father Name",
    "email": "user@example.com",
    "role": "ROLE_USER",
    // ... other fields
  }
}
```

### 3. Registration Changes

#### RegisterRequest - New Fields Added:
- `fatherName` - Father's name (required)
- `joiningDate` - Joining date (optional, format: yyyy-MM-dd)
- `retirementDate` - Retirement date (optional, format: yyyy-MM-dd)
- `sankulName` - Sankul name (optional)

#### UserResponse DTO:
- Removed `username` field
- Added `fatherName`, `joiningDate`, `retirementDate`, `sankulName`

### 4. API Endpoints Updated

#### POST `/api/auth/login`
- Request body now requires `email` instead of `username`
- Returns JWT token with user details including new fields

#### POST `/api/auth/register`
- Accepts new fields in registration payload
- Returns success message with user ID (PMUMS format)

#### POST `/api/auth/otp/verify`
- Updated to use new user fields
- Returns user ID instead of username

### 5. Database Schema Changes

The following columns need to be added/updated in the `users` table:

```sql
-- Add new columns
ALTER TABLE users ADD COLUMN father_name VARCHAR(255);
ALTER TABLE users ADD COLUMN joining_date DATE;
ALTER TABLE users ADD COLUMN retirement_date DATE;
ALTER TABLE users ADD COLUMN sankul_name VARCHAR(255);

-- Remove username column (if you want to completely remove it)
-- Note: This should be done carefully as it might break existing data
-- ALTER TABLE users DROP COLUMN username;

-- Ensure email is unique and not null
ALTER TABLE users MODIFY COLUMN email VARCHAR(255) UNIQUE NOT NULL;
```

### 6. Email Sending After Registration

A confirmation email is now sent to users after successful registration with the following content:

**Subject**: PMUMS पंजीकरण सफल | आपका रजिस्ट्रेशन नंबर

**Content includes**:
- User's registration number (PMUMS ID)
- Welcome message in Hindi
- Information about 3-month membership period
- Death case contribution obligations
- Contact information

### 7. Files Modified

1. `src/main/java/com/example/kalyan_kosh_api/entity/User.java` - Entity changes
2. `src/main/java/com/example/kalyan_kosh_api/controller/AuthController.java` - Email-based login
3. `src/main/java/com/example/kalyan_kosh_api/controller/OtpAuthController.java` - Updated responses
4. `src/main/java/com/example/kalyan_kosh_api/service/AuthService.java` - Email authentication
5. `src/main/java/com/example/kalyan_kosh_api/dto/LoginRequest.java` - Email field
6. `src/main/java/com/example/kalyan_kosh_api/dto/UserResponse.java` - New fields
7. `src/main/java/com/example/kalyan_kosh_api/dto/RegisterRequest.java` - New fields

## Testing

### To test login:
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "YourPassword123"
  }'
```

### To test registration:
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test",
    "surname": "User",
    "fatherName": "Father Name",
    "email": "test@example.com",
    "password": "Password123",
    "mobileNumber": "9876543210",
    "joiningDate": "2020-01-15",
    "retirementDate": "2050-01-15",
    "sankulName": "Test Sankul",
    "acceptedTerms": true
    // ... other required fields
  }'
```

## Important Notes

1. **Backward Compatibility**: If you have existing users with username, you need to migrate them to use email for authentication.

2. **Database Migration**: Run the database migration scripts before deploying this version.

3. **JWT Token**: JWT tokens now contain user ID (PMUMS format) as the subject, not email or username.

4. **Frontend Changes Required**: Update your frontend to send `email` instead of `username` in login requests.

5. **Email Service**: Ensure email service is properly configured in `application.properties` for sending confirmation emails.

## Migration Checklist

- [x] Update User entity with new fields
- [x] Update DTOs (LoginRequest, UserResponse, RegisterRequest)
- [x] Update AuthController to use email
- [x] Update AuthService authentication methods
- [x] Update OtpAuthController responses
- [x] Compile and test the changes
- [ ] Run database migration scripts
- [ ] Update frontend to use email instead of username
- [ ] Test all authentication flows
- [ ] Deploy to production

## Contact

For issues or questions about these changes, please contact the development team.

