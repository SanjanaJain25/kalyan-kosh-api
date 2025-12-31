# Death Case Contribution API - Quick Reference

## ✅ What Was Created

### 1. Request DTO
**File**: `DeathCaseContributionRequest.java`
```java
{
  userId: String (required)
  deathCaseId: Long (required)
  totalContributionAmount: Double (required, positive)
  comment: String (required)
}
```

### 2. Response DTO
**File**: `DeathCaseContributionResponse.java`
```java
{
  id: Long
  userId: String
  userName: String
  deathCaseId: Long
  totalContributionAmount: Double
  comment: String
  receiptFileName: String
  receiptUrl: String
  uploadedAt: LocalDateTime
  updatedAt: LocalDateTime
}
```

### 3. Controller Endpoint
**File**: `ReceiptController.java`
```java
POST /api/receipts/death-case-contribution
- Requires: JWT Token (USER role)
- Accepts: multipart/form-data
- Parts: data (JSON), file (MultipartFile)
```

---

## 📝 Request Example (Postman/Frontend)

### Form Data
```javascript
{
  "data": {
    "userId": "PMUMS202458108",
    "deathCaseId": 123,
    "totalContributionAmount": 5000.00,
    "comment": "Monthly contribution"
  },
  "file": [receipt.jpg/pdf]
}
```

### Headers
```
Authorization: Bearer <JWT_TOKEN>
Content-Type: multipart/form-data
```

---

## 🚀 Frontend Code (Copy-Paste Ready)

```javascript
const uploadContribution = async (userId, deathCaseId, amount, comment, file) => {
  const token = localStorage.getItem('jwtToken');
  
  const formData = new FormData();
  formData.append('data', JSON.stringify({
    userId,
    deathCaseId: parseInt(deathCaseId),
    totalContributionAmount: parseFloat(amount),
    comment
  }));
  formData.append('file', file);

  const response = await fetch('http://localhost:8080/api/receipts/death-case-contribution', {
    method: 'POST',
    headers: { 'Authorization': `Bearer ${token}` },
    body: formData
  });

  return response.json();
};
```

---

## ⚙️ Validation Rules

| Field | Required | Type | Validation |
|-------|----------|------|------------|
| userId | ✅ Yes | String | NotBlank |
| deathCaseId | ✅ Yes | Long | NotNull |
| totalContributionAmount | ✅ Yes | Double | Positive (> 0) |
| comment | ✅ Yes | String | NotBlank |
| file | ✅ Yes | File | Max 10MB |

---

## 🎯 Status: Ready to Use

✅ DTOs created
✅ Validation configured
✅ Controller endpoint added
✅ Authentication required
✅ File upload enabled (10MB max)
✅ Documentation complete

**Note**: Service layer implementation can be added later if needed.

---

**Full Documentation**: See `DEATH_CASE_CONTRIBUTION_API.md`

