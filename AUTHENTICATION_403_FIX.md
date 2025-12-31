# 🔐 Authentication Error - 403 Forbidden Fix

## Problem Identified

Your receipt upload is failing with **403 Forbidden** because of authentication issues, not the multipart request format.

## Error Analysis

From your logs:
```
2026-01-01T01:56:21.117+05:30 DEBUG ... Securing POST /api/receipts
2026-01-01T01:56:21.132+05:30 DEBUG ... AnonymousAuthenticationFilter: Set SecurityContextHolder to anonymous SecurityContext
2026-01-01T01:56:21.133+05:30 DEBUG ... Http403ForbiddenEntryPoint: Pre-authenticated entry point called. Rejecting access
```

**Translation**: Spring Security can't find a valid JWT token, so it treats you as an anonymous user and denies access.

---

## ✅ Step-by-Step Fix

### Step 1: Get a Valid JWT Token

First, login to get a fresh token:

**POST** `http://localhost:8080/api/auth/login`

**Body** (raw JSON):
```json
{
  "username": "your_email@example.com",
  "password": "your_password"
}
```

**Expected Response**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": "PMUMS202458108",
    "name": "Your Name",
    ...
  }
}
```

**→ Copy the `token` value**

### Step 2: Use Token in Receipt Upload

**POST** `http://localhost:8080/api/receipts`

**Headers**:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Body** (form-data):
| Key | Type | Value |
|-----|------|-------|
| data | Text | `{"deathCaseId": 1, "amount": 5000.00, "paymentDate": "2026-01-01", "comment": "Test payment"}` |
| file | File | [Select receipt.jpg] |

---

## 🔍 Common Authentication Issues

### Issue 1: Token Not Included
❌ **Wrong**: No Authorization header
```
POST /api/receipts
Content-Type: multipart/form-data
```

✅ **Correct**: Include Bearer token
```
POST /api/receipts
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: multipart/form-data
```

### Issue 2: Token Expired
❌ **Symptom**: Same 403 error even with token
✅ **Solution**: Login again to get fresh token

### Issue 3: Wrong Token Format
❌ **Wrong**: `Authorization: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...`
✅ **Correct**: `Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...`

### Issue 4: User Not Found
❌ **Symptom**: Token valid but user query in logs fails
✅ **Solution**: Ensure user exists in database with correct username

---

## 🧪 Testing Checklist

### ✅ 1. Test Login First
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test@example.com","password":"password123"}'
```

**Expected**: Should return JWT token

### ✅ 2. Test Receipt Upload
```bash
curl -X POST http://localhost:8080/api/receipts \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -F 'data={"deathCaseId":1,"amount":5000.00,"paymentDate":"2026-01-01","comment":"Test"}' \
  -F 'file=@receipt.jpg'
```

**Expected**: Should return receipt response (not 403)

---

## 🔧 Postman Debug Steps

### Step 1: Login Request
1. **Method**: POST
2. **URL**: `http://localhost:8080/api/auth/login`
3. **Headers**: `Content-Type: application/json`
4. **Body** (raw JSON): 
   ```json
   {
     "username": "your_email@example.com",
     "password": "your_password"
   }
   ```
5. **Send** → Should get 200 OK with token

### Step 2: Copy Token
From login response, copy the `token` value (without quotes)

### Step 3: Upload Request
1. **Method**: POST
2. **URL**: `http://localhost:8080/api/receipts`
3. **Headers**: 
   - `Authorization`: `Bearer [PASTE_TOKEN_HERE]`
4. **Body** (form-data):
   - `data` (Text): `{"deathCaseId": 1, "amount": 5000.00, "paymentDate": "2026-01-01", "comment": "Test payment"}`
   - `file` (File): Select your receipt image
5. **Send** → Should get 200 OK with receipt response

---

## 🚨 If Still Getting 403

### Check 1: Token Format
Ensure Authorization header is exactly:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Check 2: User Exists
The JWT token contains a username. Ensure that user exists in your database.

### Check 3: Role Permissions
Your endpoint requires `@PreAuthorize("hasRole('USER')")`. Ensure your user has `ROLE_USER`.

### Check 4: Security Configuration
Verify that `/api/receipts` is not excluded in security configuration.

---

## ✅ Expected Success Log

When authentication works, you should see logs like:
```
DEBUG ... JwtAuthFilter: Authentication successful for user: your_email@example.com
DEBUG ... Secured POST /api/receipts
Hibernate: select ... (user query)
✅ File saved to database: receipt.jpg (2097152 bytes)
```

---

## Summary

**Problem**: 403 Forbidden due to missing/invalid JWT token
**Solution**: 
1. ✅ Login to get valid JWT token
2. ✅ Include `Authorization: Bearer <token>` header
3. ✅ Test upload with valid token

**Your multipart request format is correct** - the issue was authentication, not the controller code!

---

## Quick Test

Try this exact sequence:

1. **Login**:
   ```
   POST /api/auth/login
   Body: {"username":"your_email","password":"your_pass"}
   ```

2. **Copy token from response**

3. **Upload**:
   ```
   POST /api/receipts
   Headers: Authorization: Bearer <token>
   Body: form-data with data + file
   ```

If login works but upload still fails with 403, then we need to check user roles or security configuration.
