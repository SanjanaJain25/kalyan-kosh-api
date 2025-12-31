# ✅ Multipart File + Request Body - FIXED!

## The Problem

You asked if you can take a multipart file as well as part of the request body. The issue in your original code was trying to mix `@RequestBody` with `@RequestPart` which doesn't work for multipart/form-data requests.

## ❌ What Was Wrong

```java
@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public ResponseEntity<ReceiptResponse> upload(
        @RequestBody UploadReceiptRequest data,  // ❌ This won't work with multipart
        @RequestPart("file") MultipartFile file,
        Authentication authentication
)
```

**Problem**: `@RequestBody` expects JSON in the request body, but multipart/form-data sends data as separate parts.

## ✅ Solution Applied

```java
@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public ResponseEntity<ReceiptResponse> upload(
        @RequestPart("data") String data,        // ✅ Accept data as string part
        @RequestPart("file") MultipartFile file, // ✅ File as separate part
        Authentication authentication
) throws Exception {

    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    // ✅ Parse JSON string to object
    UploadReceiptRequest req = mapper.readValue(data, UploadReceiptRequest.class);

    return ResponseEntity.ok(
            service.upload(req, file, authentication.getName())
    );
}
```

## How It Works

### 1. **Two Parts in Request**
- `data` part: JSON string containing receipt details
- `file` part: The multipart file (image/PDF)

### 2. **JSON String Parsing**
- Accept `data` as `String` using `@RequestPart("data")`
- Use `ObjectMapper` to convert JSON string to `UploadReceiptRequest` object
- Handle `LocalDate` fields properly with `JavaTimeModule`

### 3. **File Handling**
- Accept file as `MultipartFile` using `@RequestPart("file")`
- Pass both parsed object and file to service

## Request Format

### Postman/Frontend
```
Content-Type: multipart/form-data

Parts:
- data (text): {"deathCaseId": 1, "amount": 5000.00, "paymentDate": "2026-01-01", "comment": "Payment"}
- file (file): [receipt.jpg]
```

### JavaScript Example
```javascript
const formData = new FormData();

// Add data as JSON string
formData.append('data', JSON.stringify({
    deathCaseId: 1,
    amount: 5000.00,
    paymentDate: '2026-01-01',
    comment: 'Monthly payment'
}));

// Add file
formData.append('file', fileObject);

// Send request
fetch('/api/receipts', {
    method: 'POST',
    headers: {
        'Authorization': `Bearer ${token}`
        // DON'T set Content-Type - browser sets it with boundary
    },
    body: formData
});
```

### cURL Example
```bash
curl -X POST http://localhost:8080/api/receipts \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F 'data={"deathCaseId":1,"amount":5000.00,"paymentDate":"2026-01-01","comment":"Payment"}' \
  -F 'file=@receipt.jpg'
```

## Why This Approach?

### ✅ Advantages
1. **Works with multipart/form-data** - Proper for file uploads
2. **Supports complex objects** - JSON data can have nested objects, arrays
3. **Type safety** - Converts to strongly-typed DTO
4. **Validation** - Can apply validation on the parsed object
5. **Standard approach** - Commonly used pattern in Spring Boot

### ✅ Alternatives Considered

#### Option 1: Individual Form Fields
```java
@PostMapping
public ResponseEntity<ReceiptResponse> upload(
        @RequestParam Long deathCaseId,
        @RequestParam Double amount,
        @RequestParam String paymentDate,
        @RequestParam(required = false) String comment,
        @RequestPart MultipartFile file
) {
    // Manual object creation
}
```
**Pros**: Simple
**Cons**: Many parameters, no validation, hard to maintain

#### Option 2: JSON in Request Param
```java
@PostMapping
public ResponseEntity<ReceiptResponse> upload(
        @RequestParam String data,  // JSON as query param
        @RequestPart MultipartFile file
) {
    // Parse JSON from query param
}
```
**Pros**: Works
**Cons**: URL length limits, not RESTful

#### Option 3: Our Solution (Best)
```java
@PostMapping
public ResponseEntity<ReceiptResponse> upload(
        @RequestPart("data") String data,  // JSON as form part
        @RequestPart("file") MultipartFile file
) {
    // Parse JSON from form part
}
```
**Pros**: Clean, scalable, type-safe, follows REST principles

## Testing

### ✅ Postman Test
1. **Method**: POST
2. **URL**: `http://localhost:8080/api/receipts`
3. **Headers**: `Authorization: Bearer <token>`
4. **Body**: form-data
   - Key: `data`, Type: Text, Value: `{"deathCaseId": 1, "amount": 5000.00, "paymentDate": "2026-01-01", "comment": "Test payment"}`
   - Key: `file`, Type: File, Value: [Select receipt image]

### ✅ Expected Response
```json
{
  "id": 1,
  "amount": 5000.00,
  "paymentDate": "2026-01-01",
  "comment": "Test payment",
  "fileName": "receipt.jpg",
  "fileType": "image/jpeg",
  "fileSize": 2097152,
  "status": "UPLOADED",
  "uploadedAt": "2026-01-01T10:30:00Z"
}
```

## Summary

✅ **Problem**: Mixing `@RequestBody` with `@RequestPart` for multipart requests
✅ **Solution**: Use `@RequestPart` for both data (as JSON string) and file
✅ **Result**: Can accept both complex request data AND multipart file
✅ **Status**: Ready to test!

**Your controller now properly handles both multipart files and complex request data!** 🎉
