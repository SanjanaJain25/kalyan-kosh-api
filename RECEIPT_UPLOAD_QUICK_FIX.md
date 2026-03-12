# Receipt Upload API - Quick Fix Summary

## Problem
```
403 Forbidden + Maximum upload size exceeded
```

## Root Causes

### 1. Missing JWT Token
- Client not sending `Authorization: Bearer <token>` header
- Endpoint requires USER role authentication

### 2. File Size Limit
- Default Spring Boot limit is 1MB
- Receipt images often exceed this

## Fixes Applied

### ✅ Fix 1: File Upload Size (Backend)
**File**: `application.properties`

```properties
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```

### ✅ Fix 2: Frontend Must Send Token

```javascript
const token = localStorage.getItem('jwtToken');

fetch('http://localhost:8080/api/receipts', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${token}`  // ← REQUIRED!
  },
  body: formData
});
```

## Q&A

### Q: Do we need `Authentication authentication` parameter?
**A: YES!** 

- It identifies WHO is uploading the receipt
- Prevents anonymous uploads
- Links receipts to specific users
- Required by `@PreAuthorize("hasRole('USER')")`

```java
@PostMapping
public ResponseEntity<ReceiptResponse> upload(
    @RequestPart("data") String data,
    @RequestPart("file") MultipartFile file,
    Authentication authentication  // ← KEEP THIS!
) {
    String username = authentication.getName(); // Get logged-in user
    return ResponseEntity.ok(service.upload(req, file, username));
}
```

## Testing Steps

1. **Login** to get JWT token:
   ```
   POST /api/auth/login
   ```

2. **Upload receipt** with token:
   ```
   POST /api/receipts
   Headers: Authorization: Bearer <token>
   Body: form-data with 'data' and 'file'
   ```

3. **Restart** Spring Boot app to apply new file size limits

## Next Steps
1. ✅ Backend changes applied
2. ⏳ Update frontend to send JWT token
3. ⏳ Restart Spring Boot application
4. ⏳ Test receipt upload

---

**For detailed explanation, see**: `RECEIPT_UPLOAD_AUTH_FIX.md`

