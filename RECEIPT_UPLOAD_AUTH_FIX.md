# Receipt Upload API - Authentication & File Size Fix

## Problem Analysis

### Error 1: 403 Forbidden
```
o.s.s.w.a.AnonymousAuthenticationFilter : Set SecurityContextHolder to anonymous SecurityContext
o.s.s.w.a.Http403ForbiddenEntryPoint    : Pre-authenticated entry point called. Rejecting access
```

**Cause**: The request is not including the JWT authentication token.

### Error 2: File Size Exceeded
```
Maximum upload size exceeded
```

**Cause**: Default Spring Boot file upload size limit (1MB) is too small for receipt images.

---

## Solution 1: Authentication Parameter

### Q: Do we need `Authentication authentication` in the endpoint?
**A: YES, absolutely!**

### Why?

```java
@RestController
@RequestMapping("/api/receipts")
@PreAuthorize("hasRole('USER')")  // ← Requires authentication
public class ReceiptController {
    
    @PostMapping
    public ResponseEntity<ReceiptResponse> upload(
            @RequestPart("data") String data,
            @RequestPart("file") MultipartFile file,
            Authentication authentication  // ← REQUIRED to identify user
    ) {
        // Get the logged-in user's username
        String username = authentication.getName();
        
        // Save receipt for this specific user
        service.upload(req, file, username);
    }
}
```

### Reasons:
1. **User Identification**: We need to know WHO is uploading the receipt
2. **Security**: Prevents anonymous uploads
3. **Data Ownership**: Links receipts to specific users
4. **Role-Based Access**: Enforces USER role requirement

---

## Solution 2: Send JWT Token from Frontend

### ✅ Correct Implementation

```javascript
// After successful login, store the JWT token
const token = loginResponse.token;
localStorage.setItem('jwtToken', token);

// When uploading receipt
const uploadReceipt = async (receiptData, fileObject) => {
  const formData = new FormData();
  
  // Add the receipt data as JSON
  formData.append('data', JSON.stringify(receiptData));
  
  // Add the file
  formData.append('file', fileObject);
  
  // Get token from storage
  const token = localStorage.getItem('jwtToken');
  
  const response = await fetch('http://localhost:8080/api/receipts', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`  // ← REQUIRED!
    },
    body: formData
    // DON'T set Content-Type - browser will set it automatically with boundary
  });
  
  return response.json();
};
```

### ❌ Wrong Implementation (Missing Token)

```javascript
// This will cause 403 Forbidden
fetch('http://localhost:8080/api/receipts', {
  method: 'POST',
  body: formData
  // Missing: Authorization header
});
```

---

## Solution 3: Increased File Upload Size

### Backend Configuration (application.properties)

```properties
# File Upload Configuration
# Maximum file size for single file upload (10MB)
spring.servlet.multipart.max-file-size=10MB
# Maximum request size (total for all files + data in one request)
spring.servlet.multipart.max-request-size=10MB
```

### File Size Limits:
- **max-file-size**: Maximum size for a single uploaded file
- **max-request-size**: Maximum size for the entire HTTP request (includes all files + JSON data)

### Adjust as needed:
```properties
# For larger receipts (e.g., high-res scans)
spring.servlet.multipart.max-file-size=50MB
spring.servlet.multipart.max-request-size=50MB
```

---

## Complete Frontend Example (React/JavaScript)

```javascript
import React, { useState } from 'react';

const ReceiptUpload = () => {
  const [file, setFile] = useState(null);
  const [receiptData, setReceiptData] = useState({
    amount: 0,
    transactionDate: '',
    description: ''
  });

  const handleFileChange = (e) => {
    setFile(e.target.files[0]);
  };

  const handleUpload = async (e) => {
    e.preventDefault();

    // Validate file size (optional frontend check)
    if (file && file.size > 10 * 1024 * 1024) {
      alert('File too large. Maximum size is 10MB');
      return;
    }

    // Create FormData
    const formData = new FormData();
    formData.append('data', JSON.stringify(receiptData));
    formData.append('file', file);

    try {
      // Get JWT token from localStorage
      const token = localStorage.getItem('jwtToken');
      
      if (!token) {
        alert('Please login first');
        return;
      }

      const response = await fetch('http://localhost:8080/api/receipts', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`
        },
        body: formData
      });

      if (response.ok) {
        const result = await response.json();
        console.log('Upload successful:', result);
        alert('Receipt uploaded successfully!');
      } else if (response.status === 403) {
        alert('Unauthorized. Please login again.');
        // Redirect to login page
      } else if (response.status === 413) {
        alert('File too large. Maximum size is 10MB');
      } else {
        alert('Upload failed');
      }
    } catch (error) {
      console.error('Upload error:', error);
      alert('Network error. Please try again.');
    }
  };

  return (
    <form onSubmit={handleUpload}>
      <input type="file" onChange={handleFileChange} accept="image/*,application/pdf" />
      <input 
        type="number" 
        placeholder="Amount"
        value={receiptData.amount}
        onChange={(e) => setReceiptData({...receiptData, amount: e.target.value})}
      />
      <button type="submit">Upload Receipt</button>
    </form>
  );
};

export default ReceiptUpload;
```

---

## Security Configuration (Already Correct)

```java
// SecurityConfig.java
.authorizeHttpRequests(auth -> auth
    // Public endpoints - no auth needed
    .requestMatchers("/api/auth/**").permitAll()
    .requestMatchers("/api/locations/**").permitAll()
    
    // Protected endpoints - requires USER role + JWT token
    .requestMatchers("/api/receipts/**").hasRole("USER")
    
    .anyRequest().authenticated()
)
```

---

## Testing with Postman

### Step 1: Login to Get Token

**POST** `http://localhost:8080/api/auth/login`

**Body:**
```json
{
  "username": "user@example.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": { ... }
}
```

**Copy the token value**

### Step 2: Upload Receipt

**POST** `http://localhost:8080/api/receipts`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Body (form-data):**
- Key: `data` | Type: Text | Value: `{"amount": 1000, "transactionDate": "2026-01-01", "description": "Monthly contribution"}`
- Key: `file` | Type: File | Value: [Select your image file]

**Success Response (200):**
```json
{
  "id": "123",
  "amount": 1000,
  "fileName": "receipt_2026-01-01_123.jpg",
  "uploadedAt": "2026-01-01T00:00:00"
}
```

---

## Troubleshooting

### Issue: Still getting 403 Forbidden
**Solutions:**
1. ✅ Check token is being sent in Authorization header
2. ✅ Verify token format: `Bearer <token>` (note the space)
3. ✅ Confirm user has USER role in database
4. ✅ Check token hasn't expired (24 hour default)

### Issue: File too large
**Solutions:**
1. ✅ Check `application.properties` has increased limits
2. ✅ Restart Spring Boot application after changing properties
3. ✅ Compress images before upload (frontend)
4. ✅ Validate file size in frontend before uploading

### Issue: Token expired
**Solution:**
```javascript
// Handle token expiration
if (response.status === 401 || response.status === 403) {
  localStorage.removeItem('jwtToken');
  window.location.href = '/login';
}
```

---

## Summary

### ✅ Fixed Issues:
1. **Added file upload size limits** (10MB) in application.properties
2. **Explained authentication requirement** - JWT token MUST be sent
3. **Provided complete frontend examples** with proper Authorization header
4. **Confirmed `Authentication authentication` parameter is REQUIRED**

### Next Steps:
1. ✅ Restart your Spring Boot application
2. ✅ Update frontend to send JWT token in Authorization header
3. ✅ Test with Postman using the steps above
4. ✅ Verify file uploads work correctly

