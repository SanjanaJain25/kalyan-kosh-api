# Receipt Upload Authentication Flow

## Why 403 Forbidden Error Occurs

```
┌─────────────┐
│  Frontend   │
│   (React)   │
└──────┬──────┘
       │
       │ POST /api/receipts
       │ ❌ NO Authorization Header
       │
       ▼
┌─────────────────────┐
│  Spring Security    │
│                     │
│  JwtAuthFilter      │
│  ↓                  │
│  No JWT Token Found │
│  ↓                  │
│  Set: Anonymous     │
│  ↓                  │
│  Check: hasRole()   │
│  ↓                  │
│  ❌ DENIED          │
└──────┬──────────────┘
       │
       ▼
   403 Forbidden
```

## Correct Flow with JWT Token

```
┌─────────────┐
│  Frontend   │
│   (React)   │
└──────┬──────┘
       │
       │ POST /api/receipts
       │ ✅ Authorization: Bearer eyJhbGc...
       │
       ▼
┌─────────────────────┐
│  Spring Security    │
│                     │
│  JwtAuthFilter      │
│  ↓                  │
│  Extract JWT Token  │
│  ↓                  │
│  Validate Token     │
│  ↓                  │
│  Extract Username   │
│  ↓                  │
│  Load User Details  │
│  ↓                  │
│  Set: Authenticated │
│  ↓                  │
│  Check: hasRole()   │
│  ↓                  │
│  ✅ ALLOWED         │
└──────┬──────────────┘
       │
       ▼
┌─────────────────────┐
│  ReceiptController  │
│                     │
│  upload(            │
│    data,            │
│    file,            │
│    authentication   │ ← Contains user info
│  )                  │
│  ↓                  │
│  username =         │
│    auth.getName()   │ ← "user@example.com"
│  ↓                  │
│  Save receipt       │
│    for this user    │
└─────────────────────┘
```

## Authentication Parameter Explained

```java
@PostMapping
public ResponseEntity<ReceiptResponse> upload(
    @RequestPart("data") String data,
    @RequestPart("file") MultipartFile file,
    Authentication authentication  // ← Spring automatically injects this
) {
    // Authentication object contains:
    // - getName()         → "user@example.com"
    // - getAuthorities()  → [ROLE_USER]
    // - isAuthenticated() → true
    // - getPrincipal()    → UserDetails object
    
    String username = authentication.getName();
    // Now we know WHO is uploading this receipt
    
    return ResponseEntity.ok(
        service.upload(req, file, username)
    );
}
```

## How Authentication Object Gets Populated

```
1. Client sends request with JWT token
   ↓
2. JwtAuthFilter intercepts request
   ↓
3. Filter extracts token from "Authorization" header
   ↓
4. Filter validates token signature
   ↓
5. Filter extracts username from token payload
   ↓
6. Filter loads UserDetails from database
   ↓
7. Filter creates Authentication object
   ↓
8. Filter sets Authentication in SecurityContext
   ↓
9. Spring injects Authentication into controller method
   ↓
10. Your code uses authentication.getName()
```

## File Upload Size Flow

```
┌─────────────┐
│  Frontend   │
└──────┬──────┘
       │
       │ Upload 5MB image
       │
       ▼
┌─────────────────────┐
│  Spring Boot        │
│  MultipartResolver  │
└──────┬──────────────┘
       │
       │ Check file size
       │
       ├─ > max-file-size (1MB default)
       │  ↓
       │  ❌ MaxUploadSizeExceededException
       │  ↓
       │  413 Payload Too Large
       │
       ├─ ≤ max-file-size (10MB configured)
       │  ↓
       │  ✅ Allow upload
       │  ↓
       │  Pass to controller
       │
       ▼
┌─────────────────────┐
│  ReceiptController  │
└─────────────────────┘
```

## Complete Request Example

### Frontend Code
```javascript
// 1. Get token after login
const loginResponse = await fetch('/api/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ username, password })
});

const { token } = await loginResponse.json();
localStorage.setItem('jwtToken', token);

// 2. Upload receipt with token
const formData = new FormData();
formData.append('data', JSON.stringify({
  amount: 1000,
  transactionDate: '2026-01-01',
  description: 'Monthly contribution'
}));
formData.append('file', fileObject);

const uploadResponse = await fetch('/api/receipts', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${token}`  // ← CRITICAL!
  },
  body: formData
});
```

### Backend Processing
```java
// 1. JwtAuthFilter validates token
// 2. Creates Authentication object with username
// 3. Spring injects it into method parameter

@PostMapping
public ResponseEntity<ReceiptResponse> upload(
    @RequestPart("data") String data,      // ← Receives JSON string
    @RequestPart("file") MultipartFile file, // ← Receives file
    Authentication authentication           // ← Receives user info
) {
    // Parse JSON
    ObjectMapper mapper = new ObjectMapper();
    UploadReceiptRequest req = mapper.readValue(data, UploadReceiptRequest.class);
    
    // Get authenticated username
    String username = authentication.getName();
    // username = "user@example.com" (from JWT token)
    
    // Save receipt linked to this user
    ReceiptResponse response = service.upload(req, file, username);
    
    return ResponseEntity.ok(response);
}
```

## Security Configuration

```java
@Configuration
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        return http
            // Add JWT filter before authentication
            .addFilterBefore(jwtAuthFilter(), 
                           UsernamePasswordAuthenticationFilter.class)
            
            .authorizeHttpRequests(auth -> auth
                // Public - no token needed
                .requestMatchers("/api/auth/**").permitAll()
                
                // Protected - requires USER role + JWT token
                .requestMatchers("/api/receipts/**").hasRole("USER")
                
                .anyRequest().authenticated()
            )
            .build();
    }
}
```

## Common Mistakes

### ❌ Mistake 1: Not sending token
```javascript
fetch('/api/receipts', {
  method: 'POST',
  body: formData
  // Missing: Authorization header
});
```
**Result**: 403 Forbidden

### ❌ Mistake 2: Wrong token format
```javascript
headers: {
  'Authorization': token  // Wrong! Missing "Bearer " prefix
}
```
**Result**: 401 Unauthorized or 403 Forbidden

### ❌ Mistake 3: Token expired
```javascript
// Token issued 25 hours ago (default expiry: 24h)
headers: {
  'Authorization': `Bearer ${oldToken}`
}
```
**Result**: 401 Unauthorized

### ✅ Correct
```javascript
const token = localStorage.getItem('jwtToken');
fetch('/api/receipts', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${token}`  // ✅ Correct format
  },
  body: formData
});
```

## Summary

| Component | Purpose | Required? |
|-----------|---------|-----------|
| JWT Token | Proves user identity | ✅ Yes |
| Authorization Header | Carries JWT token | ✅ Yes |
| `Authentication` parameter | Gets user info in controller | ✅ Yes |
| File size config | Allows larger uploads | ✅ Yes |

**All 4 components must be present for receipt upload to work!**

