# File Upload: Database vs Local Storage

## ✅ Yes, You CAN Upload to MySQL Database!

You have **two options** for storing uploaded files:

---

## Option 1: Local File System (Current Implementation)

### How It Works
- File binary saved to disk (`uploads/receipts/`)
- File path stored in database

### Pros
✅ **Fast database queries** - No large data in DB
✅ **Better performance** - Files loaded on demand
✅ **Easier backup** - Separate file and DB backups
✅ **Scalable** - Easy to migrate to cloud (S3, Azure)
✅ **Cost-effective** - Disk storage is cheap
✅ **Industry standard** - Most apps do this

### Cons
❌ **File management** - Need to manage disk space
❌ **Deployment complexity** - Files separate from DB
❌ **Sync issues** - DB and files can get out of sync

---

## Option 2: MySQL Database (Direct Storage)

### How It Works
- File binary stored in database as BLOB
- Everything in one place

### Pros
✅ **Single source of truth** - Everything in database
✅ **ACID guarantees** - Transactional integrity
✅ **Easy backup** - One database backup includes files
✅ **No file management** - No disk space concerns
✅ **Portable** - Database contains everything

### Cons
❌ **Slow queries** - Large BLOBs slow down database
❌ **Database bloat** - DB size grows rapidly
❌ **Memory issues** - Loading files into memory
❌ **Expensive** - Database storage costs more
❌ **Backup size** - Backups become huge
❌ **Performance hit** - Every query loads large data

---

## Implementation: Store Files in MySQL Database

### Step 1: Update Entity (Add BLOB Column)

```java
@Entity
@Table(name = "receipts")
public class Receipt {
    
    // ...existing fields...
    
    @Column(name = "file_path")
    private String filePath;  // Keep for backward compatibility
    
    // NEW: Store file binary in database
    @Lob
    @Column(name = "file_data", columnDefinition = "LONGBLOB")
    private byte[] fileData;
    
    @Column(name = "file_name")
    private String fileName;
    
    @Column(name = "file_type")
    private String fileType;  // e.g., "image/jpeg", "application/pdf"
    
    @Column(name = "file_size")
    private Long fileSize;
    
    // ...rest of fields...
}
```

### Step 2: Update Service (Save to Database)

```java
@Service
public class ReceiptService {

    // Remove UPLOAD_DIR - no longer needed!
    
    public ReceiptResponse upload(
            UploadReceiptRequest req,
            MultipartFile file,
            String username
    ) {
        // Validate user and death case
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        DeathCase deathCase = deathCaseRepo.findById(req.getDeathCaseId())
                .orElseThrow(() -> new IllegalStateException("Death case not found"));

        // Validate file size (optional)
        if (file.getSize() > 10 * 1024 * 1024) {  // 10MB limit
            throw new IllegalArgumentException("File size exceeds 10MB limit");
        }

        // Save current date and time
        Instant currentDateTime = Instant.now();

        try {
            // ✅ Convert file to byte array for database storage
            byte[] fileData = file.getBytes();
            
            Receipt receipt = Receipt.builder()
                    .user(user)
                    .deathCase(deathCase)
                    .month(req.getMonth())
                    .year(req.getYear())
                    .amount(req.getAmount())
                    .paymentDate(req.getPaymentDate())
                    .transactionId(req.getTransactionId())
                    
                    // ✅ Store file data in database
                    .fileData(fileData)
                    .fileName(file.getOriginalFilename())
                    .fileType(file.getContentType())
                    .fileSize(file.getSize())
                    
                    .status(ReceiptStatus.UPLOADED)
                    .uploadedAt(currentDateTime)
                    .build();

            Receipt saved = receiptRepo.save(receipt);

            return mapper.map(saved, ReceiptResponse.class);
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file data: " + e.getMessage(), e);
        }
    }
}
```

### Step 3: Add Controller to Serve Files from Database

```java
@RestController
@RequestMapping("/api/receipts")
public class ReceiptController {

    @GetMapping("/{id}/file")
    public ResponseEntity<byte[]> downloadReceipt(@PathVariable Long id) {
        
        Receipt receipt = receiptRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Receipt not found"));

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(receipt.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"" + receipt.getFileName() + "\"")
                .body(receipt.getFileData());
    }
    
    // Or serve inline (display in browser)
    @GetMapping("/{id}/view")
    public ResponseEntity<byte[]> viewReceipt(@PathVariable Long id) {
        
        Receipt receipt = receiptRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Receipt not found"));

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(receipt.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .body(receipt.getFileData());
    }
}
```

### Step 4: Update Database Schema

```sql
ALTER TABLE receipts 
ADD COLUMN file_data LONGBLOB,
ADD COLUMN file_name VARCHAR(255),
ADD COLUMN file_type VARCHAR(100),
ADD COLUMN file_size BIGINT;
```

---

## Storage Comparison

### Local Storage (Current)
```
Database: 1 KB (just metadata)
Disk: 2 MB (actual file)
Total: 2 MB

Query speed: ⚡ Fast
Backup size: Small (DB only)
```

### Database Storage (New)
```
Database: 2 MB (metadata + file)
Disk: 0 MB
Total: 2 MB

Query speed: 🐌 Slower
Backup size: Large (includes files)
```

---

## Real-World Example

### Scenario: 10,000 Receipts @ 2MB each

#### Local Storage
- Database: 10,000 rows × 1 KB = 10 MB
- Files on Disk: 10,000 × 2 MB = 20 GB
- **Total**: 20 GB (10 MB DB + 20 GB files)
- **Query**: SELECT * FROM receipts → 0.1 seconds
- **Backup**: DB backup = 10 MB (fast!)

#### Database Storage
- Database: 10,000 rows × 2 MB = 20 GB
- Files on Disk: 0 MB
- **Total**: 20 GB (all in DB)
- **Query**: SELECT * FROM receipts → 30 seconds (loading 20 GB!)
- **Backup**: DB backup = 20 GB (slow!)

---

## Recommendation

### Use **Local Storage** (Current) if:
✅ You have many files
✅ Files are large (> 1 MB)
✅ You need fast queries
✅ You might migrate to cloud later
✅ Performance is critical

### Use **Database Storage** if:
✅ You have few files (< 1000)
✅ Files are small (< 100 KB)
✅ You want simplicity
✅ Transactional integrity is critical
✅ You need easy backups

---

## Hybrid Approach (Best of Both Worlds)

Store small files in database, large files on disk:

```java
private static final long DB_STORAGE_THRESHOLD = 100 * 1024; // 100 KB

public ReceiptResponse upload(...) {
    
    if (file.getSize() <= DB_STORAGE_THRESHOLD) {
        // ✅ Small file: Store in database
        receipt.setFileData(file.getBytes());
        receipt.setFilePath(null);
    } else {
        // ✅ Large file: Store on disk
        String filePath = saveToLocalDisk(file);
        receipt.setFilePath(filePath);
        receipt.setFileData(null);
    }
    
    receiptRepo.save(receipt);
}
```

---

## Migration Guide: Switch to Database Storage

### Step 1: Add Columns to Entity
```java
@Lob
@Column(name = "file_data", columnDefinition = "LONGBLOB")
private byte[] fileData;
```

### Step 2: Run Database Migration
```sql
ALTER TABLE receipts ADD COLUMN file_data LONGBLOB;
```

### Step 3: Update Service
```java
receipt.setFileData(file.getBytes());
```

### Step 4: Test Upload
Upload a file and verify it's stored in database:
```sql
SELECT id, file_name, LENGTH(file_data) as size_bytes 
FROM receipts 
WHERE id = 1;
```

### Step 5: Add Download Endpoint
```java
@GetMapping("/{id}/file")
public ResponseEntity<byte[]> download(@PathVariable Long id) {
    Receipt receipt = receiptRepo.findById(id).orElseThrow();
    return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(receipt.getFileType()))
            .body(receipt.getFileData());
}
```

---

## Performance Considerations

### Database Storage Performance Tips

1. **Lazy Loading**
```java
@Lob
@Basic(fetch = FetchType.LAZY)  // Don't load file data by default
@Column(name = "file_data")
private byte[] fileData;
```

2. **Separate Table for Files**
```java
@Entity
class Receipt {
    @OneToOne(mappedBy = "receipt", fetch = FetchType.LAZY)
    private ReceiptFile file;
}

@Entity
class ReceiptFile {
    @Lob
    private byte[] data;
    
    @OneToOne
    private Receipt receipt;
}
```

3. **Projections (Don't Load File Data)**
```java
@Query("SELECT r.id, r.amount, r.fileName FROM Receipt r")
List<ReceiptSummary> findAllWithoutFileData();
```

---

## Summary Table

| Feature | Local Storage | Database Storage |
|---------|---------------|------------------|
| **Query Speed** | ⚡⚡⚡ Fast | 🐌 Slow |
| **Setup Complexity** | Medium | Easy |
| **Backup Size** | Small | Large |
| **Scalability** | Excellent | Poor |
| **Cost** | Low | High |
| **File Management** | Manual | Automatic |
| **Cloud Migration** | Easy | Hard |
| **Transaction Safety** | Medium | Excellent |
| **Best For** | Large files, many files | Small files, few files |

---

## Final Recommendation

**Stick with Local Storage** (your current approach) because:

1. ✅ **Better performance** for typical file sizes (receipts are usually 1-5 MB)
2. ✅ **Industry standard** - Most production apps use this
3. ✅ **Future-proof** - Easy to migrate to S3/Azure later
4. ✅ **Cost-effective** - Cheaper storage and faster queries
5. ✅ **Scalable** - Can handle millions of files

**Only use Database Storage if**:
- You have very few files (< 100)
- Files are very small (< 50 KB)
- You absolutely need ACID guarantees for files
- Simplicity is more important than performance

---

**Would you like me to implement database storage for you, or stick with the current local storage approach?**

