# Switch to Database Storage - Step-by-Step Guide

## 🎯 Goal
Change from **Local File Storage** → **MySQL Database Storage**

---

## ⚠️ Important Decision

**Before you proceed, read this:**

### Current: Local Storage
- ✅ Fast queries
- ✅ Scalable
- ✅ Industry standard
- ✅ Can migrate to cloud later

### New: Database Storage
- ✅ Simpler setup
- ✅ Single backup
- ❌ Slower queries
- ❌ Database bloat
- ❌ Hard to scale

**Recommendation**: **Keep local storage** unless you have < 1000 files or files < 100KB

---

## If You Still Want Database Storage...

### Step 1: Update Receipt Entity

**File**: `Receipt.java`

Add these fields after existing fields:

```java
@Entity
@Table(name = "receipts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Receipt {

    // ...existing fields (id, user, deathCase, etc.)...

    @Column(name = "file_path")
    private String filePath;  // Keep for backward compatibility
    
    // ✅ NEW FIELDS FOR DATABASE STORAGE
    
    @Lob
    @Basic(fetch = FetchType.LAZY)  // Important: Don't load by default!
    @Column(name = "file_data", columnDefinition = "LONGBLOB")
    private byte[] fileData;
    
    @Column(name = "file_name", length = 500)
    private String fileName;
    
    @Column(name = "file_type", length = 100)
    private String fileType;  // MIME type: "image/jpeg", "application/pdf"
    
    @Column(name = "file_size")
    private Long fileSize;  // Size in bytes

    // ...rest of existing fields...
}
```

### Step 2: Run Database Migration

**Option A: MySQL Workbench**
```sql
-- Add columns for file storage
ALTER TABLE receipts 
ADD COLUMN file_data LONGBLOB,
ADD COLUMN file_name VARCHAR(500),
ADD COLUMN file_type VARCHAR(100),
ADD COLUMN file_size BIGINT;

-- Verify columns were added
DESCRIBE receipts;
```

**Option B: Spring Boot (automatic)**
- Set `spring.jpa.hibernate.ddl-auto=update` in `application.properties`
- Restart application
- Hibernate will add columns automatically

### Step 3: Update ReceiptService

**Replace current implementation:**

```java
@Service
public class ReceiptService {

    // Remove: private static final String UPLOAD_DIR = "uploads/receipts/";
    // Remove: Directory creation logic from constructor
    
    private final ReceiptRepository receiptRepo;
    private final UserRepository userRepo;
    private final DeathCaseRepository deathCaseRepo;
    private final MonthlySahyogRepository sahyogRepo;
    private final ModelMapper mapper;

    public ReceiptService(
            ReceiptRepository receiptRepo,
            UserRepository userRepo,
            DeathCaseRepository deathCaseRepo,
            MonthlySahyogRepository sahyogRepo,
            ModelMapper mapper
    ) {
        this.receiptRepo = receiptRepo;
        this.userRepo = userRepo;
        this.deathCaseRepo = deathCaseRepo;
        this.sahyogRepo = sahyogRepo;
        this.mapper = mapper;
        // Remove directory creation code
    }

    public ReceiptResponse upload(
            UploadReceiptRequest req,
            MultipartFile file,
            String username
    ) {
        // Validate user
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        // Validate death case
        DeathCase deathCase = deathCaseRepo.findById(req.getDeathCaseId())
                .orElseThrow(() -> new IllegalStateException("Death case not found"));

        // Monthly Sahyog check
        MonthlySahyog sahyog = sahyogRepo
                .findByMonthAndYear(req.getMonth(), req.getYear())
                .orElseThrow(() ->
                        new IllegalStateException("Monthly Sahyog not generated"));

        // FREEZE GUARD
        if (sahyog.getStatus() == SahyogStatus.FROZEN) {
            throw new IllegalStateException(
                    "Month is frozen. Receipt upload not allowed.");
        }

        // Validate file size (10MB limit)
        if (file.getSize() > 10 * 1024 * 1024) {
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
                    
                    // ✅ Store file in database
                    .fileData(fileData)
                    .fileName(file.getOriginalFilename())
                    .fileType(file.getContentType())
                    .fileSize(file.getSize())
                    
                    .status(ReceiptStatus.UPLOADED)
                    .uploadedAt(currentDateTime)
                    .build();

            Receipt saved = receiptRepo.save(receipt);

            System.out.println("✅ File saved to database: " + 
                             file.getOriginalFilename() + " (" + file.getSize() + " bytes)");

            return mapper.map(saved, ReceiptResponse.class);
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file data: " + e.getMessage(), e);
        }
    }

    public List<ReceiptResponse> getMyReceipts(String username) {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        List<Receipt> receipts = receiptRepo.findByUser(user);

        return receipts.stream()
                .map(r -> mapper.map(r, ReceiptResponse.class))
                .toList();
    }
}
```

### Step 4: Add Download Endpoint

**Add to ReceiptController:**

```java
@RestController
@RequestMapping("/api/receipts")
@PreAuthorize("hasRole('USER')")
public class ReceiptController {

    // ...existing code...

    /**
     * Download receipt file from database
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadReceipt(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Receipt receipt = receiptRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Receipt not found"));

        // Security: Verify user owns this receipt
        String username = authentication.getName();
        if (!receipt.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Access denied");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(receipt.getFileType()));
        headers.setContentDisposition(
            ContentDisposition.builder("attachment")
                .filename(receipt.getFileName())
                .build()
        );

        return ResponseEntity.ok()
                .headers(headers)
                .body(receipt.getFileData());
    }

    /**
     * View receipt file in browser
     */
    @GetMapping("/{id}/view")
    public ResponseEntity<byte[]> viewReceipt(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Receipt receipt = receiptRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Receipt not found"));

        // Security: Verify user owns this receipt
        String username = authentication.getName();
        if (!receipt.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Access denied");
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(receipt.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .body(receipt.getFileData());
    }
}
```

### Step 5: Update ReceiptRepository (Optional Optimization)

Add query to exclude file data when listing receipts:

```java
public interface ReceiptRepository extends JpaRepository<Receipt, Long> {

    // Existing methods...
    List<Receipt> findByUser(User user);

    // ✅ NEW: Get receipts WITHOUT loading file data (performance optimization)
    @Query("SELECT new Receipt(r.id, r.user, r.deathCase, r.month, r.year, " +
           "r.amount, r.paymentDate, r.transactionId, r.fileName, r.fileType, " +
           "r.fileSize, r.status, r.uploadedAt) FROM Receipt r WHERE r.user = :user")
    List<Receipt> findByUserWithoutFileData(@Param("user") User user);
}
```

Then add constructor to Receipt:

```java
// Constructor for query projection (without file data)
public Receipt(Long id, User user, DeathCase deathCase, Integer month, 
               Integer year, Double amount, LocalDate paymentDate, 
               String transactionId, String fileName, String fileType, 
               Long fileSize, ReceiptStatus status, Instant uploadedAt) {
    this.id = id;
    this.user = user;
    this.deathCase = deathCase;
    // ... set all fields except fileData
}
```

### Step 6: Test Upload

**Postman Test:**

1. **Login** to get JWT token
2. **Upload receipt**:
   - POST `http://localhost:8080/api/receipts`
   - Headers: `Authorization: Bearer <token>`
   - Body: form-data with `data` and `file`

3. **Verify in database**:
```sql
SELECT id, file_name, LENGTH(file_data) as file_size_bytes, file_type
FROM receipts
WHERE id = 1;
```

Expected output:
```
id | file_name    | file_size_bytes | file_type
---+--------------+-----------------+------------
1  | receipt.jpg  | 2097152         | image/jpeg
```

4. **Download file**:
   - GET `http://localhost:8080/api/receipts/1/download`
   - Headers: `Authorization: Bearer <token>`
   - File should download

### Step 7: Clean Up (Optional)

Remove local file storage directory:

```powershell
# Delete uploads folder (if you want)
Remove-Item -Recurse -Force uploads\
```

---

## Verification Checklist

- [ ] Receipt entity has new fields (fileData, fileName, etc.)
- [ ] Database migration completed (columns added)
- [ ] ReceiptService updated (file.getBytes() instead of file save)
- [ ] Download endpoint added to controller
- [ ] Test: Upload file via Postman
- [ ] Test: Verify file in database (LENGTH(file_data) > 0)
- [ ] Test: Download file via /download endpoint
- [ ] Test: View file via /view endpoint
- [ ] Test: Multiple uploads work correctly
- [ ] Performance: Queries are still fast (use LAZY loading)

---

## Performance Tips

### 1. Always Use LAZY Loading
```java
@Lob
@Basic(fetch = FetchType.LAZY)  // ← Important!
private byte[] fileData;
```

### 2. Use Projections for Listing
Don't load file data when showing receipt list:
```java
// Bad: Loads all file data
List<Receipt> receipts = receiptRepo.findByUser(user);  // ❌ Slow!

// Good: Excludes file data
List<ReceiptDTO> receipts = receiptRepo.findReceiptSummaryByUser(user);  // ✅ Fast!
```

### 3. Add Database Indexes
```sql
CREATE INDEX idx_receipt_user ON receipts(user_id);
CREATE INDEX idx_receipt_month_year ON receipts(month, year);
```

---

## Rollback Plan

If you want to revert back to local storage:

1. **Stop using new fields** in ReceiptService
2. **Switch back** to old implementation (file save to disk)
3. **Keep database columns** (for backward compatibility)
4. **Migrate existing files** from database to disk (if needed)

---

## Summary

### Files Changed
1. ✅ `Receipt.java` - Add fileData, fileName, fileType, fileSize
2. ✅ Database - Add columns via migration
3. ✅ `ReceiptService.java` - Change to file.getBytes()
4. ✅ `ReceiptController.java` - Add download/view endpoints

### Key Changes
```java
// OLD: Local storage
Files.copy(file.getInputStream(), targetPath);

// NEW: Database storage
receipt.setFileData(file.getBytes());
```

---

## ⚠️ Final Warning

**Database storage is suitable ONLY for:**
- Small number of files (< 1000)
- Small file sizes (< 500 KB)
- Simple applications

**For production apps with many/large files:**
- ✅ Use local storage (current implementation)
- ✅ Or migrate to cloud storage (S3, Azure Blob)

**Your current local storage implementation is actually better for most cases!**

---

## Need Help?

If you decide to proceed with database storage, I can:
1. Update all files for you
2. Create the migration script
3. Test the implementation
4. Add download endpoints

Let me know if you want me to implement this!

