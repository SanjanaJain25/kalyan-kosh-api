# File Upload Storage - Quick Answer

## ❓ Where are multipart files uploaded?

### ✅ **LOCAL FILE SYSTEM** (Not Database)

---

## Storage Breakdown

```
┌──────────────────────────────────────────┐
│  MULTIPART FILE UPLOAD                   │
│  (receipt.jpg)                           │
└─────────────────┬────────────────────────┘
                  │
         ┌────────┴────────┐
         │                 │
         ▼                 ▼
┌─────────────────┐  ┌─────────────────┐
│ LOCAL DISK      │  │ DATABASE        │
│ uploads/        │  │ (MySQL)         │
│ receipts/       │  │                 │
│                 │  │                 │
│ ✅ FILE BINARY  │  │ ✅ FILE PATH    │
│ (Actual file)   │  │ (Reference)     │
│                 │  │                 │
│ 1735689600000_  │  │ file_path:      │
│ receipt.jpg     │  │ "uploads/..."   │
│                 │  │                 │
│ 2MB binary data │  │ + metadata      │
└─────────────────┘  └─────────────────┘
```

---

## What's Stored Where?

### 📁 Local File System
**Location**: `uploads/receipts/`

**Contents**: 
- Actual file binary (JPG, PNG, PDF)
- Physical file on disk

**Example**:
```
uploads/receipts/1735689600000_receipt.jpg  ← Real file here
```

### 🗄️ Database (MySQL)
**Table**: `receipts`

**Contents**:
- File path reference: `"uploads/receipts/1735689600000_receipt.jpg"`
- Metadata: amount, date, userId, deathCaseId
- Upload timestamp: `2026-01-01T10:30:00Z`

**Example**:
```json
{
  "id": 1,
  "filePath": "uploads/receipts/1735689600000_receipt.jpg",  // ← Path only
  "amount": 5000.00,
  "uploadedAt": "2026-01-01T10:30:00Z"
}
```

---

## Why Local Storage?

✅ **Fast**: Database queries are quick (no large BLOBs)
✅ **Scalable**: Easy to migrate to S3/Azure later
✅ **Cost-effective**: Disk storage is cheap
✅ **Standard**: Industry best practice
✅ **Flexible**: Can serve via CDN later

---

## Code Implementation

### Directory Creation (Constructor)
```java
private static final String UPLOAD_DIR = "uploads/receipts/";

public ReceiptService(...) {
    // Create directory if it doesn't exist
    Path uploadPath = Paths.get(UPLOAD_DIR);
    if (!Files.exists(uploadPath)) {
        Files.createDirectories(uploadPath);
    }
}
```

### File Upload (Save to Disk)
```java
// Generate unique filename
String filename = System.currentTimeMillis() + "_" + originalFilename;
String filePath = UPLOAD_DIR + filename;

// ✅ Save file to LOCAL DISK
Path targetPath = Paths.get(filePath);
Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

// ✅ Save metadata to DATABASE
Receipt receipt = Receipt.builder()
    .filePath(filePath)  // ← Path reference only
    .amount(req.getAmount())
    .uploadedAt(Instant.now())  // ← Current timestamp
    .build();

receiptRepo.save(receipt);
```

---

## File Locations

### Physical File
```
C:\Users\shub\Downloads\kalyan-kosh-api\
└── uploads\
    └── receipts\
        ├── 1735689600000_receipt.jpg     ← File 1
        ├── 1735689700000_invoice.pdf     ← File 2
        └── 1735689800000_payment.png     ← File 3
```

### Database Record
```sql
SELECT * FROM receipts;

id | file_path                                  | amount  | uploaded_at
---+--------------------------------------------+---------+--------------------
1  | uploads/receipts/1735689600000_receipt.jpg | 5000.00 | 2026-01-01 10:30:00
```

---

## Quick Summary

| What | Stored Where | Why |
|------|-------------|-----|
| **File Binary** | Local Disk | Actual file data |
| **File Path** | Database | Reference to file |
| **Metadata** | Database | Receipt details |
| **Timestamp** | Database | Upload date/time |

---

**Answer**: Multipart files are saved to **LOCAL FILE SYSTEM** (`uploads/receipts/`), and only the **file path** is stored in the **database**.

**Full Details**: See `FILE_STORAGE_EXPLAINED.md`

