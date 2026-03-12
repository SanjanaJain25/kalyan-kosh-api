# Upload Receipt API - Complete Guide

## 📤 Upload Receipt Endpoint

### **Endpoint**
```
POST /api/receipts
```

### **Authentication**
```
Required: JWT Token (USER role)
```

### **Headers**
```
Authorization: Bearer <your_jwt_token>
Content-Type: multipart/form-data
```

---

## 📝 Request Body

### **Format**: `multipart/form-data`

### **Parts**:

#### **Part 1: `data` (JSON String)**
```json
{
  "deathCaseId": 1,
  "amount": 5000.00,
  "paymentDate": "2026-01-01",
  "transactionId": "TXN123456"
}
```

#### **Part 2: `file` (File)**
- Receipt image (JPG, PNG) or PDF
- Maximum size: 10MB

---

## 📋 Request Body Fields

| Field | Type | Required | Description | Example |
|-------|------|----------|-------------|---------|
| deathCaseId | Long | Yes | ID of death case | 1 |
| amount | Double | Yes | Amount paid | 5000.00 |
| paymentDate | String (Date) | Yes | Payment date (YYYY-MM-DD) | "2026-01-01" |
| transactionId | String | No | Transaction reference | "TXN123456" |
| file | File | Yes | Receipt file | receipt.jpg |

---

## 🔧 Postman Setup

### **Step 1: Login to Get Token**

**Request**:
```
POST http://localhost:8080/api/auth/login
```

**Headers**:
```
Content-Type: application/json
```

**Body (raw JSON)**:
```json
{
  "username": "user@example.com",
  "password": "password123"
}
```

**Response**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": "PMUMS202458108",
    "name": "John Doe",
    ...
  }
}
```

**→ Copy the `token` value**

---

### **Step 2: Upload Receipt**

**Request**:
```
POST http://localhost:8080/api/receipts
```

**Headers**:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Body Type**: `form-data`

**Body Fields**:

| Key | Type | Value |
|-----|------|-------|
| data | Text | `{"deathCaseId": 1, "amount": 5000.00, "paymentDate": "2026-01-01", "transactionId": "TXN123456"}` |
| file | File | [Select your receipt image/PDF] |

**Screenshot Example**:
```
┌─────────────┬──────┬───────────────────────────────────┐
│ Key         │ Type │ Value                             │
├─────────────┼──────┼───────────────────────────────────┤
│ data        │ Text │ {"deathCaseId": 1, "amount"...}   │
│ file        │ File │ [Choose File] receipt.jpg         │
└─────────────┴──────┴───────────────────────────────────┘
```

---

## ✅ Success Response

**Status**: `200 OK`

**Body**:
```json
{
  "id": 1,
  "amount": 5000.00,
  "paymentDate": "2026-01-01",
  "transactionId": "TXN123456",
  "fileName": "receipt.jpg",
  "fileType": "image/jpeg",
  "fileSize": 2097152,
  "status": "UPLOADED",
  "uploadedAt": "2026-01-01T10:30:00Z"
}
```

---

## ❌ Error Responses

### **401 Unauthorized** - Missing/Invalid Token
```json
{
  "timestamp": "2026-01-01T10:30:00.000+00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Full authentication is required"
}
```

**Solution**: Login first and include JWT token in Authorization header

---

### **400 Bad Request** - Validation Error
```json
{
  "timestamp": "2026-01-01T10:30:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Month is required"
}
```

**Solution**: Check all required fields are provided

---

### **413 Payload Too Large** - File Too Big
```json
{
  "timestamp": "2026-01-01T10:30:00.000+00:00",
  "status": 413,
  "error": "Payload Too Large",
  "message": "Maximum upload size exceeded"
}
```

**Solution**: Reduce file size to under 10MB

---

### **500 Internal Server Error** - Invalid File Type
```json
{
  "timestamp": "2026-01-01T10:30:00.000+00:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Only images and PDFs are allowed"
}
```

**Solution**: Upload only JPG, PNG, or PDF files

---

## 💻 JavaScript/React Example

### **Upload Function**
```javascript
const uploadReceipt = async (receiptData, fileObject) => {
  // Get JWT token
  const token = localStorage.getItem('jwtToken');
  
  if (!token) {
    throw new Error('Please login first');
  }

  // Create FormData
  const formData = new FormData();
  
  // Add data as JSON string
  const data = {
    deathCaseId: receiptData.deathCaseId,
    amount: receiptData.amount,
    paymentDate: receiptData.paymentDate,
    transactionId: receiptData.transactionId
  };
  formData.append('data', JSON.stringify(data));
  
  // Add file
  formData.append('file', fileObject);

  // Upload
  const response = await fetch('http://localhost:8080/api/receipts', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`
      // DON'T set Content-Type - browser sets it with boundary
    },
    body: formData
  });

  if (!response.ok) {
    throw new Error(`Upload failed: ${response.status}`);
  }

  return response.json();
};

// Usage
const handleUpload = async () => {
  const receiptData = {
    deathCaseId: 1,
    amount: 5000.00,
    paymentDate: '2026-01-01',
    transactionId: 'TXN123456'
  };

  const fileInput = document.getElementById('file-input');
  const file = fileInput.files[0];

  try {
    const result = await uploadReceipt(receiptData, file);
    console.log('Upload success:', result);
    alert('Receipt uploaded successfully!');
  } catch (error) {
    console.error('Upload error:', error);
    alert('Upload failed: ' + error.message);
  }
};
```

---

### **React Component Example**
```jsx
import React, { useState } from 'react';

const ReceiptUpload = () => {
  const [formData, setFormData] = useState({
    deathCaseId: '',
    amount: '',
    paymentDate: '',
    transactionId: ''
  });
  const [file, setFile] = useState(null);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);

    try {
      const token = localStorage.getItem('jwtToken');
      
      const formDataToSend = new FormData();
      formDataToSend.append('data', JSON.stringify({
        deathCaseId: parseInt(formData.deathCaseId),
        amount: parseFloat(formData.amount),
        paymentDate: formData.paymentDate,
        transactionId: formData.transactionId
      }));
      formDataToSend.append('file', file);

      const response = await fetch('http://localhost:8080/api/receipts', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`
        },
        body: formDataToSend
      });

      if (response.ok) {
        const result = await response.json();
        alert('Receipt uploaded successfully!');
        console.log(result);
      } else {
        const error = await response.json();
        alert('Upload failed: ' + error.message);
      }
    } catch (error) {
      alert('Network error: ' + error.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <div>
        <label>Death Case ID:</label>
        <input
          type="number"
          value={formData.deathCaseId}
          onChange={(e) => setFormData({...formData, deathCaseId: e.target.value})}
          required
        />
      </div>

      <div>
        <label>Amount:</label>
        <input
          type="number"
          step="0.01"
          value={formData.amount}
          onChange={(e) => setFormData({...formData, amount: e.target.value})}
          required
        />
      </div>

      <div>
        <label>Payment Date:</label>
        <input
          type="date"
          value={formData.paymentDate}
          onChange={(e) => setFormData({...formData, paymentDate: e.target.value})}
          required
        />
      </div>

      <div>
        <label>Transaction ID:</label>
        <input
          type="text"
          value={formData.transactionId}
          onChange={(e) => setFormData({...formData, transactionId: e.target.value})}
        />
      </div>

      <div>
        <label>Receipt File:</label>
        <input
          type="file"
          accept="image/*,application/pdf"
          onChange={(e) => setFile(e.target.files[0])}
          required
        />
      </div>

      <button type="submit" disabled={loading || !file}>
        {loading ? 'Uploading...' : 'Upload Receipt'}
      </button>
    </form>
  );
};

export default ReceiptUpload;
```

---

## 🧪 cURL Example

```bash
curl -X POST http://localhost:8080/api/receipts \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -F 'data={"deathCaseId":1,"amount":5000.00,"paymentDate":"2026-01-01","transactionId":"TXN123456"}' \
  -F 'file=@/path/to/receipt.jpg'
```

---

## 📊 Complete Flow

```
1. User Login
   ↓
2. Get JWT Token
   ↓
3. Prepare Receipt Data (JSON)
   ↓
4. Select Receipt File
   ↓
5. Create FormData (data + file)
   ↓
6. POST to /api/receipts with JWT token
   ↓
7. Backend validates data
   ↓
8. Backend converts file to byte[]
   ↓
9. Backend saves to database
   ↓
10. Return success response
```

---

## 🔍 Validation Rules

### **Required Fields**
- ✅ deathCaseId
- ✅ amount (positive number)
- ✅ paymentDate (YYYY-MM-DD format)
- ✅ file (image or PDF)

### **Optional Fields**
- ⚪ transactionId

### **File Rules**
- Maximum size: 10MB
- Allowed types: image/jpeg, image/png, application/pdf
- Must be valid file (not corrupted)

---

## 📦 Summary

**Endpoint**: `POST /api/receipts`

**Request**:
```
Headers: Authorization: Bearer <token>
Body: multipart/form-data
  - data: JSON string with receipt details
  - file: Receipt image/PDF
```

**Response**:
```json
{
  "id": 1,
  "fileName": "receipt.jpg",
  "fileSize": 2097152,
  "status": "UPLOADED",
  ...
}
```

**Storage**: File saved as BLOB in MySQL database

---

## 🎯 Quick Copy-Paste

### **Postman Body (form-data)**

**Key: data | Type: Text | Value:**
```json
{"deathCaseId": 1, "amount": 5000.00, "paymentDate": "2026-01-01", "transactionId": "TXN123456"}
```

**Key: file | Type: File | Value:**
```
[Select your receipt.jpg or receipt.pdf]
```

---

Need help testing? Let me know! 🚀

