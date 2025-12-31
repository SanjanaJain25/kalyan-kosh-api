
# 🚀 MIGRATION EXECUTION STEPS

## Your Error Fixed! ✅

The error occurred because Hibernate tried to change `users.id` while the foreign key constraint was still active.

---

## 📋 Follow These Steps (5 Minutes)

### **Step 1: Stop Your Application** ⚠️
```bash
# Press Ctrl+C in terminal where app is running
# Or stop it from IDE
```

### **Step 2: Backup Database** 💾
```bash
# Windows PowerShell (in any directory):
mysqldump -u root -p kalyankosh_db > C:\Users\shub\Downloads\backup_kalyankosh_$(Get-Date -Format "yyyyMMdd_HHmmss").sql

# Enter password: root
```

### **Step 3: Run Migration Script** 🔧
```bash
# Option A: From command line
mysql -u root -p kalyankosh_db < C:\Users\shub\Downloads\kalyan-kosh-api\migration-mysql-ready.sql

# Option B: Using MySQL Workbench
# 1. Open MySQL Workbench
# 2. Connect to localhost
# 3. File > Open SQL Script
# 4. Select: migration-mysql-ready.sql
# 5. Execute (Lightning bolt icon or Ctrl+Shift+Enter)
```

### **Step 4: Verify Migration** ✅
Open MySQL and run:
```sql
USE kalyankosh_db;

-- Should show id as VARCHAR(20)
DESCRIBE users;

-- Should show user_id as VARCHAR(20)
DESCRIBE receipt;

-- Should show current_value
SELECT * FROM id_sequence;
```

### **Step 5: Start Application** 🚀
```bash
cd C:\Users\shub\Downloads\kalyan-kosh-api
.\mvnw.cmd spring-boot:run
```

### **Step 6: Test User Registration** 🧪
```bash
# Register a new user
curl -X POST http://localhost:8080/api/auth/register ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"testuser123\",\"password\":\"password123\",\"name\":\"Test\",\"surname\":\"User\",\"email\":\"test@test.com\",\"phoneNumber\":\"1234567890\"}"
```

**Expected Response:**
```json
{
  "id": "PMUMS202458108",
  "username": "testuser123",
  "name": "Test",
  ...
}
```

---

## ✅ After Successful Migration

Update `application.properties`:
```properties
# Change from:
spring.jpa.hibernate.ddl-auto=none

# To:
spring.jpa.hibernate.ddl-auto=validate
```

Restart application.

---

## 🚨 If Migration Fails

**Rollback:**
```sql
USE kalyankosh_db;

DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS receipt;
DROP TABLE IF EXISTS id_sequence;

RENAME TABLE users_backup TO users;
RENAME TABLE receipt_backup TO receipt;
```

Then restore from backup:
```bash
mysql -u root -p kalyankosh_db < C:\Users\shub\Downloads\backup_kalyankosh_YYYYMMDD_HHMMSS.sql
```

---

## 📞 Quick Troubleshooting

### Issue: "Table users_backup already exists"
**Solution:**
```sql
DROP TABLE IF EXISTS users_backup;
DROP TABLE IF EXISTS receipt_backup;
-- Then re-run migration script
```

### Issue: "Unknown column 'new_id'"
**Solution:** The script probably ran partially. Rollback and re-run.

### Issue: "Cannot drop primary key"
**Solution:** Foreign keys might still exist. Find and drop them:
```sql
SHOW CREATE TABLE receipt;
-- Look for CONSTRAINT name, then:
ALTER TABLE receipt DROP FOREIGN KEY <constraint_name>;
```

---

## 🎉 Success Indicators

✅ Application starts without errors  
✅ No "incompatible foreign key" errors  
✅ New user registration returns ID like `PMUMS202458108`  
✅ Can query users: `GET http://localhost:8080/api/users/`  
✅ Can login and get JWT token  

---

## 📊 Current Status

- ✅ Code refactored (all files updated)
- ✅ `application.properties` updated (ddl-auto=none)
- ✅ Migration script ready
- ⏳ **NEXT:** Run migration script
- ⏳ **THEN:** Test application

---

Generated: December 29, 2025  
Status: 🚀 READY TO MIGRATE

