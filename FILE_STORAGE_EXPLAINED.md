# File Upload Storage - Complete Explanation

## ✅ ANSWER: Where are files uploaded?

### **Files are uploaded to LOCAL FILE SYSTEM** (Not Database)

---

## Storage Architecture

### What Gets Stored Where?

```
┌─────────────────────────────────────────┐
│  MULTIPART FILE UPLOAD                  │
│  (receipt.jpg - 2MB)                    │
└─────────────────┬───────────────────────┘
                  │
                  ▼
    ┌─────────────────────────────┐
    │  ReceiptService.upload()    │
    └─────────────┬───────────────┘
                  │
         ┌────────┴────────┐
         │                 │
         ▼                 ▼
┌────────────────┐  ┌──────────────────┐
│ LOCAL STORAGE  │  │    DATABASE      │
│ (File System)  │  │    (MySQL)       │
└────────────────┘  └──────────────────┘
│                   │
│ ACTUAL FILE       │ FILE METADATA
│ receipt.jpg       │ - filePath
│ (Binary data)     │ - amount
│                   │ - uploadedAt
│                   │ - userId
│                   │ - deathCaseId
└───────────────────┴──────────────────┘
```

---

## Detailed Breakdown

### 1. File Storage (Local File System)

**Location**: `uploads/receipts/`

**What's Stored**:
- The actual binary file (JPG, PNG, PDF)
- Physical file on disk

**File Naming**:
```
{timestamp}_{original_filename}
Example: 1735689600000_receipt.jpg
```

**Storage Code**:
```java
// Upload directory
private static final String UPLOAD_DIR = "uploads/receipts/";

// Generate unique filename
String filename = System.currentTimeMillis() + "_" + originalFilename;
String filePath = UPLOAD_DIR + filename;

// Save file to local storage
Path targetPath = Paths.get(filePath);
Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
```

**Physical Location**:
```
Your Project Root/
├── uploads/
│   └── receipts/
│       ├── 1735689600000_receipt.jpg
│       ├── 1735689700000_invoice.pdf
│       └── 1735689800000_payment.png
```

---

### 2. Database Storage (MySQL)

**Table**: `receipts`

**What's Stored**:
- File path (reference to the file)
- Receipt metadata (amount, date, etc.)

**Columns Stored**:
```sql
CREATE TABLE receipts (
    id BIGINT PRIMARY KEY,
    user_id VARCHAR(255),
    death_case_id BIGINT,
    file_path VARCHAR(500),        -- ✅ Path to file
    amount DECIMAL(10, 2),
    payment_date DATE,
    month INTEGER,
    year INTEGER,
    transaction_id VARCHAR(100),
    status VARCHAR(50),
    uploaded_at TIMESTAMP,         -- ✅ Current date/time
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

**Database Record Example**:
```json
{
  "id": 1,
  "userId": "PMUMS202458108",
  "deathCaseId": 123,
  "filePath": "uploads/receipts/1735689600000_receipt.jpg",  // ← Path reference
  "amount": 5000.00,
  "paymentDate": "2026-01-01",
  "month": 1,
  "year": 2026,
  "transactionId": "TXN123456",
  "status": "UPLOADED",
  "uploadedAt": "2026-01-01T10:30:00Z"
}
```

---

## Why This Approach?

### ✅ Advantages of Local File Storage

1. **Performance**: 
   - Database queries remain fast
   - No large binary data in database

2. **Scalability**:
   - Files can be moved to cloud storage (S3, Azure Blob) later
   - Database size stays manageable

3. **Cost-Effective**:
   - Cheaper to store files on disk than in database
   - Database backup/restore is faster

4. **Flexibility**:
   - Easy to serve files via web server
   - Can implement CDN later

5. **Standard Practice**:
   - Industry standard approach
   - Most frameworks do it this way

### ❌ Why NOT Store in Database?

1. **Size Limits**: Database gets bloated with binary data
2. **Performance**: Slower queries when fetching large BLOBs
3. **Backup**: Database backups become huge
4. **Memory**: Loading files into memory for every query
5. **Cost**: Database storage is more expensive

---

## File Upload Flow

```
Step 1: Client uploads file
   ↓
Step 2: MultipartFile received by controller
   ↓
Step 3: ReceiptService.upload() called
   ↓
Step 4a: Save file to local disk
   - Generate unique filename
   - Copy file bytes to uploads/receipts/
   - File physically stored on disk ✅
   ↓
Step 4b: Save metadata to database
   - Store file path reference
   - Store receipt details
   - Store upload timestamp
   - Database record created ✅
   ↓
Step 5: Return response with file path
```

---

## Code Implementation

### Constructor (Directory Creation)
```java
public ReceiptService(...) {
    // ...
    
    // Create upload directory if it doesn't exist
    try {
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            System.out.println("✅ Created upload directory: " + uploadPath.toAbsolutePath());
        }
    } catch (IOException e) {
        throw new RuntimeException("Failed to create upload directory", e);
    }
}
```

**What it does**:
- Checks if `uploads/receipts/` directory exists
- Creates it if missing
- Runs once when service starts

### Upload Method (File Saving)
```java
// Generate unique filename
String originalFilename = file.getOriginalFilename();
String filename = System.currentTimeMillis() + "_" + originalFilename;
String filePath = UPLOAD_DIR + filename;

// Save current date and time
Instant currentDateTime = Instant.now();

try {
    // ✅ SAVE FILE TO LOCAL STORAGE
    Path targetPath = Paths.get(filePath);
    Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
    System.out.println("✅ File saved to: " + targetPath.toAbsolutePath());
    
} catch (IOException e) {
    throw new RuntimeException("Failed to save file: " + e.getMessage(), e);
}

// ✅ SAVE METADATA TO DATABASE
Receipt receipt = Receipt.builder()
        .user(user)
        .deathCase(deathCase)
        .filePath(filePath)        // ← Only path stored in DB
        .amount(req.getAmount())
        .uploadedAt(currentDateTime)  // ← Current timestamp
        .build();

Receipt saved = receiptRepo.save(receipt);
```

---

## File Retrieval Flow

### How to Serve Files to Frontend

**Option 1: Static Resource Handler** (Recommended)
```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}
```

**Access URL**:
```
http://localhost:8080/uploads/receipts/1735689600000_receipt.jpg
```

**Option 2: Controller Endpoint**
```java
@GetMapping("/receipts/file/{filename}")
public ResponseEntity<Resource> downloadFile(@PathVariable String filename) {
    Path filePath = Paths.get(UPLOAD_DIR + filename);
    Resource resource = new UrlResource(filePath.toUri());
    
    return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=\"" + filename + "\"")
            .body(resource);
}
```

---

## Security Considerations

### Current Implementation
- ✅ JWT authentication required
- ✅ User validation
- ✅ File size limit (10MB)
- ✅ Unique filename generation

### Recommended Additions
- [ ] File type validation (only images/PDFs)
- [ ] Virus scanning
- [ ] User-specific directories
- [ ] Access control (users can only access their files)

---

## Migration to Cloud Storage (Future)

When you need to scale, you can migrate to cloud storage:

### AWS S3 Example
```java
// Replace local file save with S3 upload
AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();

s3Client.putObject(
    "kalyan-kosh-receipts",  // bucket name
    filename,                 // object key
    file.getInputStream(),    // file data
    metadata                  // metadata
);

String s3Url = "https://s3.amazonaws.com/kalyan-kosh-receipts/" + filename;
// Save s3Url to database instead of local path
```

**Benefits**:
- Scalable storage
- Automatic backups
- CDN integration
- Global availability

---

## Summary

### ✅ Current Setup

| Component | What's Stored | Where |
|-----------|---------------|-------|
| **File Binary** | Actual file (JPG, PDF, PNG) | **Local Disk** (`uploads/receipts/`) |
| **File Path** | Reference to file location | **Database** (`file_path` column) |
| **Metadata** | Amount, date, user, etc. | **Database** (receipt table) |
| **Timestamp** | Upload date/time | **Database** (`uploaded_at` column) |

### File Flow

1. **Upload**: Client → Server → **Local Disk** (file saved)
2. **Database**: File path + metadata stored in MySQL
3. **Retrieval**: Read path from DB → Load file from disk → Serve to client

### Why This Works

- ✅ **Fast**: Database stays lightweight
- ✅ **Scalable**: Can migrate to S3/Azure later
- ✅ **Standard**: Industry best practice
- ✅ **Cost-effective**: Disk storage is cheap
- ✅ **Maintainable**: Easy to backup and manage

---

**Conclusion**: Files are uploaded to **LOCAL FILE SYSTEM** (`uploads/receipts/`), and only the **file path + metadata** are stored in the **DATABASE**.

