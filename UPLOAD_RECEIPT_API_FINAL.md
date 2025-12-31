# 📤 Upload Receipt API - Quick Reference

## ✅ Fixed and Ready to Use!

All compilation errors have been resolved. The `month` and `year` fields have been removed as requested.

---

## 📍 Endpoint

```
POST http://localhost:8080/api/receipts
```

---

## 🔐 Authentication

**Required**: JWT Token (USER role)

**Header**:
```
Authorization: Bearer <your_jwt_token>
```

---

## 📝 Request Body

**Content-Type**: `multipart/form-data`

### **Part 1: data** (JSON String)

```json
{
  "deathCaseId": 1,
  "amount": 5000.00,
  "paymentDate": "2026-01-01",
  "comment": "Monthly contribution payment"
}
```

### **Part 2: file** (Multipart File)

- Receipt image (JPG, PNG) or PDF
- Maximum size: 10MB

---

## 📋 Field Descriptions

| Field | Type | Required | Description | Example |
|-------|------|----------|-------------|---------|
| deathCaseId | Long | ✅ Yes | ID of the death case | 1 |
| amount | Double | ✅ Yes | Payment amount (positive) | 5000.00 |
| paymentDate | String (Date) | ✅ Yes | Payment date (YYYY-MM-DD) | "2026-01-01" |
| transactionId | String | ⚪ No | Transaction reference/ID | "TXN123456" |
| file | File | ✅ Yes | Receipt file (image/PDF) | receipt.jpg |

---

## ✅ Success Response

**Status**: `200 OK`

```json
{
  "id": 1,
  "amount": 5000.00,
  "paymentDate": "2026-01-01",
  "comment": "Monthly contribution payment",
  "fileName": "receipt.jpg",
  "fileType": "image/jpeg",
  "fileSize": 2097152,
  "status": "UPLOADED",
  "uploadedAt": "2026-01-01T10:30:00Z"
}
```

---

## 🔧 Postman Setup

### Step 1: Login

**Request**:
```
POST http://localhost:8080/api/auth/login
```

**Body** (raw JSON):
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
  "user": {...}
}
```

→ **Copy the token**

### Step 2: Upload Receipt

**Request**:
```
POST http://localhost:8080/api/receipts
```

**Headers**:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Body** (form-data):

| Key | Type | Value |
|-----|------|-------|
| data | Text | `{"deathCaseId": 1, "amount": 5000.00, "paymentDate": "2026-01-01", "comment": "Monthly contribution"}` |
| file | File | [Select receipt.jpg] |

---

## 💻 JavaScript Example

```javascript
const uploadReceipt = async (receiptData, fileObject) => {
  const token = localStorage.getItem('jwtToken');
  
  if (!token) {
    alert('Please login first');
    return;
  }

  const formData = new FormData();
  
  // Add data as JSON string
  formData.append('data', JSON.stringify({
    deathCaseId: receiptData.deathCaseId,
    amount: receiptData.amount,
    paymentDate: receiptData.paymentDate,
    comment: receiptData.comment
  }));
  
  // Add file
  formData.append('file', fileObject);

  try {
    const response = await fetch('http://localhost:8080/api/receipts', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`
        // DON'T set Content-Type - browser sets it automatically
      },
      body: formData
    });

    if (response.ok) {
      const result = await response.json();
      console.log('Success:', result);
      return result;
    } else {
      const error = await response.json();
      throw new Error(error.message || 'Upload failed');
    }
  } catch (error) {
    console.error('Upload error:', error);
    throw error;
  }
};

// Usage
const handleUpload = async () => {
  const fileInput = document.getElementById('file-input');
  const file = fileInput.files[0];
  
  const data = {
    deathCaseId: 1,
    amount: 5000.00,
    paymentDate: '2026-01-01',
    comment: 'Monthly contribution payment'
  };
  
  try {
    const result = await uploadReceipt(data, file);
    alert('Receipt uploaded successfully!');
  } catch (error) {
    alert('Upload failed: ' + error.message);
  }
};
```

---

## ⚛️ React Example

```jsx
import React, { useState } from 'react';

const ReceiptUpload = () => {
  const [formData, setFormData] = useState({
    deathCaseId: '',
    amount: '',
    paymentDate: '',
    comment: ''
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
        comment: formData.comment
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
        alert('Receipt uploaded successfully!');
        // Reset form
        setFormData({
          deathCaseId: '',
          amount: '',
          paymentDate: '',
          comment: ''
        });
        setFile(null);
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
        <label>Comment (optional):</label>
        <input
          type="text"
          value={formData.comment}
          onChange={(e) => setFormData({...formData, comment: e.target.value})}
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
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F 'data={"deathCaseId":1,"amount":5000.00,"paymentDate":"2026-01-01","comment":"Monthly payment"}' \
  -F 'file=@/path/to/receipt.jpg'
```

---

## ❌ Error Responses

### 401 Unauthorized
```json
{
  "timestamp": "2026-01-01T10:30:00.000+00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Full authentication is required"
}
```
**Solution**: Login first and include JWT token

### 400 Bad Request
```json
{
  "timestamp": "2026-01-01T10:30:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Amount must be positive"
}
```
**Solution**: Check all required fields are valid

### 413 Payload Too Large
```json
{
  "timestamp": "2026-01-01T10:30:00.000+00:00",
  "status": 413,
  "error": "Payload Too Large",
  "message": "Maximum upload size exceeded"
}
```
**Solution**: Reduce file size to under 10MB

### 500 Internal Server Error
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

## 🔍 Validation Rules

### Required Fields
- ✅ deathCaseId (must exist in database)
- ✅ amount (must be positive number)
- ✅ paymentDate (YYYY-MM-DD format)
- ✅ file (max 10MB, images or PDF only)

### Optional Fields
- ⚪ comment

### File Rules
- **Size**: Maximum 10MB
- **Type**: image/jpeg, image/png, application/pdf
- **Storage**: Saved as BLOB in MySQL database

---

## 🎯 Quick Copy-Paste (Postman)

### Headers
```
Authorization: Bearer YOUR_TOKEN_HERE
```

### Body (form-data)

**Key: data | Type: Text**
```json
{"deathCaseId": 1, "amount": 5000.00, "paymentDate": "2026-01-01", "comment": "Monthly payment"}
```

**Key: file | Type: File**
```
[Select your receipt.jpg or receipt.pdf]
```

---

## ✅ Status

- ✅ All compilation errors fixed
- ✅ Month and year fields removed
- ✅ Database storage implemented
- ✅ File validation active (10MB, images/PDFs)
- ✅ JWT authentication required
- ✅ Ready to use!

---

## 🎉 Summary

**Endpoint**: `POST /api/receipts`

**Auth**: JWT Token required

**Request**: multipart/form-data
- data: JSON with deathCaseId, amount, paymentDate, comment
- file: Receipt image/PDF (max 10MB)

**Response**: Receipt details with uploadedAt timestamp

**Storage**: File saved as BLOB in MySQL database

---

Need help testing? Just restart your application and try uploading via Postman! 🚀

