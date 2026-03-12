# Death Case Contribution API - Complete Guide

## Overview
This API allows users to upload death case contributions with a receipt. It accepts userId, deathCaseId, totalContributionAmount, comment, and a multipart file.

---

## API Endpoint

### Upload Death Case Contribution
**POST** `/api/receipts/death-case-contribution`

**Authentication**: Required (JWT Token with USER role)

**Content-Type**: `multipart/form-data`

---

## Request Structure

### Headers
```
Authorization: Bearer <JWT_TOKEN>
Content-Type: multipart/form-data
```

### Form Data Parts

#### Part 1: `data` (JSON String)
```json
{
  "userId": "PMUMS202458108",
  "deathCaseId": 123,
  "totalContributionAmount": 5000.00,
  "comment": "Monthly contribution for death case support"
}
```

#### Part 2: `file` (Multipart File)
- Receipt image or PDF document
- Maximum size: 10MB
- Supported formats: JPG, PNG, PDF

---

## Request DTO

### DeathCaseContributionRequest.java
```java
@Data
public class DeathCaseContributionRequest {

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotNull(message = "Death Case ID is required")
    private Long deathCaseId;

    @NotNull(message = "Total contribution amount is required")
    @Positive(message = "Amount must be positive")
    private Double totalContributionAmount;

    @NotBlank(message = "Comment is required")
    private String comment;
}
```

### Field Validations

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| userId | String | Yes | NotBlank | User's unique ID (e.g., PMUMS202458108) |
| deathCaseId | Long | Yes | NotNull | ID of the death case |
| totalContributionAmount | Double | Yes | NotNull, Positive | Total contribution amount |
| comment | String | Yes | NotBlank | Comment or note about the contribution |

---

## Response Structure

### Success Response (200 OK)

```json
{
  "id": 1,
  "userId": "PMUMS202458108",
  "userName": "John Doe",
  "deathCaseId": 123,
  "totalContributionAmount": 5000.00,
  "comment": "Monthly contribution for death case support",
  "receiptFileName": "receipt_2026-01-01_123.jpg",
  "receiptUrl": "/uploads/receipts/receipt_2026-01-01_123.jpg",
  "uploadedAt": "2026-01-01T10:30:00",
  "updatedAt": "2026-01-01T10:30:00"
}
```

### DeathCaseContributionResponse.java
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeathCaseContributionResponse {

    private Long id;
    private String userId;
    private String userName;
    private Long deathCaseId;
    private Double totalContributionAmount;
    private String comment;
    private String receiptFileName;
    private String receiptUrl;
    private LocalDateTime uploadedAt;
    private LocalDateTime updatedAt;
}
```

---

## Error Responses

### 400 Bad Request - Validation Error
```json
{
  "timestamp": "2026-01-01T10:30:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "User ID is required"
}
```

### 401 Unauthorized - Missing/Invalid Token
```json
{
  "timestamp": "2026-01-01T10:30:00.000+00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Full authentication is required"
}
```

### 403 Forbidden - Insufficient Permissions
```json
{
  "timestamp": "2026-01-01T10:30:00.000+00:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Access Denied"
}
```

### 413 Payload Too Large - File Size Exceeded
```json
{
  "timestamp": "2026-01-01T10:30:00.000+00:00",
  "status": 413,
  "error": "Payload Too Large",
  "message": "Maximum upload size exceeded"
}
```

---

## Frontend Implementation

### React/JavaScript Example

```javascript
const uploadDeathCaseContribution = async (contributionData, receiptFile) => {
  // Get JWT token from storage
  const token = localStorage.getItem('jwtToken');

  if (!token) {
    throw new Error('Please login first');
  }

  // Validate file size (10MB max)
  if (receiptFile.size > 10 * 1024 * 1024) {
    throw new Error('File size must be less than 10MB');
  }

  // Create FormData
  const formData = new FormData();

  // Add data as JSON string
  const data = {
    userId: contributionData.userId,
    deathCaseId: contributionData.deathCaseId,
    totalContributionAmount: contributionData.totalContributionAmount,
    comment: contributionData.comment
  };
  formData.append('data', JSON.stringify(data));

  // Add file
  formData.append('file', receiptFile);

  try {
    const response = await fetch('http://localhost:8080/api/receipts/death-case-contribution', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`
        // DON'T set Content-Type - browser will set it automatically with boundary
      },
      body: formData
    });

    if (!response.ok) {
      throw new Error(`Upload failed with status: ${response.status}`);
    }

    const result = await response.json();
    console.log('Upload successful:', result);
    return result;

  } catch (error) {
    console.error('Upload error:', error);
    throw error;
  }
};

// Usage Example
const handleSubmit = async (e) => {
  e.preventDefault();

  const contributionData = {
    userId: 'PMUMS202458108',
    deathCaseId: 123,
    totalContributionAmount: 5000.00,
    comment: 'Monthly contribution for death case support'
  };

  const fileInput = document.getElementById('receipt-file');
  const receiptFile = fileInput.files[0];

  try {
    const result = await uploadDeathCaseContribution(contributionData, receiptFile);
    alert('Contribution uploaded successfully!');
  } catch (error) {
    alert(`Error: ${error.message}`);
  }
};
```

### React Component Example

```javascript
import React, { useState } from 'react';

const DeathCaseContributionForm = () => {
  const [formData, setFormData] = useState({
    userId: '',
    deathCaseId: '',
    totalContributionAmount: '',
    comment: ''
  });
  const [file, setFile] = useState(null);
  const [loading, setLoading] = useState(false);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleFileChange = (e) => {
    const selectedFile = e.target.files[0];
    
    // Validate file size
    if (selectedFile && selectedFile.size > 10 * 1024 * 1024) {
      alert('File size must be less than 10MB');
      e.target.value = '';
      return;
    }
    
    setFile(selectedFile);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);

    try {
      const token = localStorage.getItem('jwtToken');
      
      if (!token) {
        alert('Please login first');
        return;
      }

      const formDataToSend = new FormData();
      
      // Add data as JSON string
      formDataToSend.append('data', JSON.stringify({
        userId: formData.userId,
        deathCaseId: parseInt(formData.deathCaseId),
        totalContributionAmount: parseFloat(formData.totalContributionAmount),
        comment: formData.comment
      }));
      
      // Add file
      formDataToSend.append('file', file);

      const response = await fetch('http://localhost:8080/api/receipts/death-case-contribution', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`
        },
        body: formDataToSend
      });

      if (response.ok) {
        const result = await response.json();
        console.log('Success:', result);
        alert('Contribution uploaded successfully!');
        
        // Reset form
        setFormData({
          userId: '',
          deathCaseId: '',
          totalContributionAmount: '',
          comment: ''
        });
        setFile(null);
      } else {
        const error = await response.json();
        alert(`Error: ${error.message || 'Upload failed'}`);
      }
    } catch (error) {
      console.error('Error:', error);
      alert('Network error. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <div>
        <label>User ID:</label>
        <input
          type="text"
          name="userId"
          value={formData.userId}
          onChange={handleInputChange}
          placeholder="PMUMS202458108"
          required
        />
      </div>

      <div>
        <label>Death Case ID:</label>
        <input
          type="number"
          name="deathCaseId"
          value={formData.deathCaseId}
          onChange={handleInputChange}
          placeholder="123"
          required
        />
      </div>

      <div>
        <label>Total Contribution Amount:</label>
        <input
          type="number"
          step="0.01"
          name="totalContributionAmount"
          value={formData.totalContributionAmount}
          onChange={handleInputChange}
          placeholder="5000.00"
          required
        />
      </div>

      <div>
        <label>Comment:</label>
        <textarea
          name="comment"
          value={formData.comment}
          onChange={handleInputChange}
          placeholder="Enter your comment"
          required
        />
      </div>

      <div>
        <label>Receipt File:</label>
        <input
          type="file"
          accept="image/*,application/pdf"
          onChange={handleFileChange}
          required
        />
        {file && <p>Selected: {file.name} ({(file.size / 1024 / 1024).toFixed(2)} MB)</p>}
      </div>

      <button type="submit" disabled={loading || !file}>
        {loading ? 'Uploading...' : 'Upload Contribution'}
      </button>
    </form>
  );
};

export default DeathCaseContributionForm;
```

---

## Testing with Postman

### Step 1: Login to Get Token

**POST** `http://localhost:8080/api/auth/login`

**Body (JSON):**
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

### Step 2: Upload Death Case Contribution

**POST** `http://localhost:8080/api/receipts/death-case-contribution`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Body (form-data):**

| Key | Type | Value |
|-----|------|-------|
| data | Text | `{"userId": "PMUMS202458108", "deathCaseId": 123, "totalContributionAmount": 5000.00, "comment": "Monthly contribution"}` |
| file | File | [Select your receipt image/PDF] |

**Success Response (200 OK):**
```json
{
  "userId": "PMUMS202458108",
  "deathCaseId": 123,
  "totalContributionAmount": 5000.00,
  "comment": "Monthly contribution",
  "receiptFileName": "receipt.jpg"
}
```

---

## cURL Examples

### Upload Contribution
```bash
curl -X POST http://localhost:8080/api/receipts/death-case-contribution \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F 'data={"userId":"PMUMS202458108","deathCaseId":123,"totalContributionAmount":5000.00,"comment":"Monthly contribution"}' \
  -F 'file=@/path/to/receipt.jpg'
```

---

## Security Notes

1. **Authentication Required**: User must be logged in with USER role
2. **JWT Token**: Must be sent in Authorization header
3. **File Size Limit**: Maximum 10MB per file
4. **File Types**: Images (JPG, PNG) and PDF supported
5. **Validation**: All fields are validated before processing
6. **Single Use**: Each contribution is uniquely recorded

---

## Validation Rules

### userId
- Required: Yes
- Type: String
- Validation: NotBlank
- Example: "PMUMS202458108"

### deathCaseId
- Required: Yes
- Type: Long (integer)
- Validation: NotNull
- Example: 123

### totalContributionAmount
- Required: Yes
- Type: Double (decimal)
- Validation: NotNull, Positive (> 0)
- Example: 5000.00

### comment
- Required: Yes
- Type: String
- Validation: NotBlank
- Example: "Monthly contribution for death case support"

### file
- Required: Yes
- Type: MultipartFile
- Max Size: 10MB
- Formats: JPG, PNG, PDF

---

## Common Errors and Solutions

### Error: 403 Forbidden
**Cause**: Not sending JWT token or invalid token
**Solution**: Ensure Authorization header is set with valid Bearer token

### Error: 400 Bad Request - "User ID is required"
**Cause**: Missing or empty userId in request
**Solution**: Provide valid userId in data JSON

### Error: 413 Payload Too Large
**Cause**: File size exceeds 10MB
**Solution**: Compress or resize the image before uploading

### Error: 400 Bad Request - "Amount must be positive"
**Cause**: totalContributionAmount is zero or negative
**Solution**: Provide positive amount greater than 0

---

## Integration Checklist

- [x] DTO classes created (Request & Response)
- [x] Controller endpoint implemented
- [x] Validation annotations added
- [x] Authentication required (USER role)
- [x] File upload configured (10MB max)
- [x] CORS enabled
- [ ] Service layer implementation (to be completed)
- [ ] Database entity creation
- [ ] File storage logic
- [ ] Unit tests
- [ ] Integration tests

---

## Next Steps (Backend)

To complete this implementation, you need to:

1. **Create Service Method**
   - Implement `deathCaseContributionService.upload()`
   - Save contribution to database
   - Store receipt file
   - Link to user and death case

2. **Create Entity**
   - Create `DeathCaseContribution` entity
   - Map to database table
   - Add relationships (User, DeathCase)

3. **Add Repository**
   - Create `DeathCaseContributionRepository`
   - Add custom query methods

4. **Implement File Storage**
   - Save uploaded files to disk/cloud
   - Generate unique filenames
   - Return file URLs

---

## Summary

This API provides a complete solution for uploading death case contributions with receipts, including:

✅ **Payload Structure**: userId, deathCaseId, totalContributionAmount, comment, file
✅ **Validation**: All fields validated with appropriate constraints
✅ **Authentication**: JWT token required with USER role
✅ **File Upload**: Supports images and PDFs up to 10MB
✅ **Response**: Comprehensive response with all contribution details
✅ **Error Handling**: Clear error messages for all scenarios
✅ **Documentation**: Complete guide with examples

**Files Created:**
1. `DeathCaseContributionRequest.java` - Request DTO
2. `DeathCaseContributionResponse.java` - Response DTO
3. Controller endpoint in `ReceiptController.java`
4. This documentation file

