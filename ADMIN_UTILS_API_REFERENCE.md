╔══════════════════════════════════════════════════════════════════╗
║                                                                  ║
║          🔧 ADMIN UTILS API REFERENCE (FOR FRONTEND)            ║
║                                                                  ║
╚══════════════════════════════════════════════════════════════════╝

## 📡 BASE URL:
```
http://localhost:8080
```

## 🔓 AUTHENTICATION:
```
✅ These endpoints are PUBLIC (no auth required)
```

---

## 1️⃣ FIX NULL LOCATIONS

**Fix all users who have NULL location data**

### Endpoint:
```
POST /api/admin/utils/fix-null-locations
```

### Request:
```javascript
// No request body needed
fetch('http://localhost:8080/api/admin/utils/fix-null-locations', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  }
})
```

### Response (Success):
```json
{
  "success": true,
  "message": "Successfully fixed NULL locations",
  "fixedCount": 3,
  "errorCount": 0,
  "defaultLocation": {
    "state": "Madhya Pradesh",
    "sambhag": "इंदौर संभाग",
    "district": "Indore",
    "block": "Indore"
  }
}
```

### Response (Error):
```json
{
  "success": false,
  "message": "Error: No state found in database!",
  "fixedCount": 0,
  "errorCount": 0
}
```

---

## 2️⃣ CHECK NULL LOCATIONS

**Check how many users have NULL location data**

### Endpoint:
```
GET /api/admin/utils/check-null-locations
```

### Request:
```javascript
fetch('http://localhost:8080/api/admin/utils/check-null-locations')
  .then(res => res.json())
  .then(data => console.log(data))
```

### Response:
```json
{
  "totalUsers": 3,
  "usersWithNullLocations": 0,
  "nullStateCount": 0,
  "nullSambhagCount": 0,
  "nullDistrictCount": 0,
  "nullBlockCount": 0,
  "message": "All users have complete location data! ✅"
}
```

---

## 3️⃣ GET ALL USERS (With Locations)

**Get all users with complete location details**

### Endpoint:
```
GET /api/users
```

### Request:
```javascript
fetch('http://localhost:8080/api/users')
  .then(res => res.json())
  .then(users => console.log(users))
```

### Response:
```json
[
  {
    "id": "PMUMS202458108",
    "name": "Aman",
    "surname": "Soni",
    "email": "ssaman7566@gmail.com",
    "mobileNumber": "6232983739",
    "departmentState": "Madhya Pradesh",
    "departmentSambhag": "इंदौर संभाग",
    "departmentDistrict": "Indore",
    "departmentBlock": "Indore",
    "role": "USER",
    "createdAt": "2026-01-02T12:00:00"
  }
]
```

---

## 🔗 CORS CONFIGURATION:

**Allowed Origins:**
```javascript
// All localhost ports allowed
http://localhost:3000   ✅
http://localhost:3001   ✅
http://localhost:*      ✅

// Production
https://pmums.com       ✅
```

---

## 📝 FRONTEND INTEGRATION EXAMPLES:

### React/Next.js Example:

```javascript
// Fix NULL locations button
const fixNullLocations = async () => {
  try {
    const response = await fetch('http://localhost:8080/api/admin/utils/fix-null-locations', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' }
    });
    
    const data = await response.json();
    
    if (data.success) {
      alert(`✅ Fixed ${data.fixedCount} users!`);
    } else {
      alert(`❌ Error: ${data.message}`);
    }
  } catch (error) {
    alert(`❌ Failed: ${error.message}`);
  }
};

// Check NULL locations
const checkNullLocations = async () => {
  try {
    const response = await fetch('http://localhost:8080/api/admin/utils/check-null-locations');
    const data = await response.json();
    
    console.log(`Total Users: ${data.totalUsers}`);
    console.log(`Users with NULL: ${data.usersWithNullLocations}`);
    console.log(`Message: ${data.message}`);
  } catch (error) {
    console.error('Error:', error);
  }
};

// Get all users
const getAllUsers = async () => {
  try {
    const response = await fetch('http://localhost:8080/api/users');
    const users = await response.json();
    
    users.forEach(user => {
      console.log(`${user.name}: ${user.departmentState} / ${user.departmentDistrict}`);
    });
  } catch (error) {
    console.error('Error:', error);
  }
};
```

### Axios Example:

```javascript
import axios from 'axios';

const API_BASE = 'http://localhost:8080';

// Fix NULL locations
export const fixNullLocations = async () => {
  const { data } = await axios.post(`${API_BASE}/api/admin/utils/fix-null-locations`);
  return data;
};

// Check NULL locations
export const checkNullLocations = async () => {
  const { data } = await axios.get(`${API_BASE}/api/admin/utils/check-null-locations`);
  return data;
};

// Get all users
export const getAllUsers = async () => {
  const { data } = await axios.get(`${API_BASE}/api/users`);
  return data;
};
```

---

## ✅ LOCATION DATA STRUCTURE:

**Database Hierarchy:**
```
State (मध्य प्रदेश / Madhya Pradesh)
  └── Sambhag (इंदौर संभाग)
      └── District (Indore / इंदौर)
          └── Block (Indore / इंदौर)
```

**User Entity Fields:**
```javascript
{
  departmentState: "Madhya Pradesh",    // State entity (linked)
  departmentSambhag: "इंदौर संभाग",     // Sambhag entity (linked)
  departmentDistrict: "Indore",         // District entity (linked)
  departmentBlock: "Indore"             // Block entity (linked)
}
```

---

## 🎯 WHEN TO USE THESE APIS:

### Use Case 1: Admin Dashboard
```javascript
// On admin dashboard load
useEffect(() => {
  checkNullLocations().then(data => {
    if (data.usersWithNullLocations > 0) {
      showFixButton(); // Show "Fix Locations" button
    }
  });
}, []);
```

### Use Case 2: User Management Page
```javascript
// Before displaying users
const loadUsers = async () => {
  const users = await getAllUsers();
  
  // Check if any user has null locations
  const needsFix = users.some(u => 
    !u.departmentState || !u.departmentDistrict
  );
  
  if (needsFix) {
    showWarning("Some users have incomplete location data");
  }
  
  setUsers(users);
};
```

### Use Case 3: One-time Migration
```javascript
// Run once after deployment
const runMigration = async () => {
  console.log("🔄 Checking for NULL locations...");
  
  const check = await checkNullLocations();
  
  if (check.usersWithNullLocations > 0) {
    console.log(`⚠️ Found ${check.usersWithNullLocations} users with NULL locations`);
    console.log("🔧 Fixing...");
    
    const result = await fixNullLocations();
    
    if (result.success) {
      console.log(`✅ Fixed ${result.fixedCount} users!`);
    }
  } else {
    console.log("✅ All users have complete location data!");
  }
};
```

---

## 🚀 TESTING ENDPOINTS:

### Using cURL:
```bash
# Fix NULL locations
curl -X POST http://localhost:8080/api/admin/utils/fix-null-locations

# Check NULL locations
curl http://localhost:8080/api/admin/utils/check-null-locations

# Get all users
curl http://localhost:8080/api/users
```

### Using Postman:
```
1. Create new collection: "Admin Utils"

2. Add request: "Fix NULL Locations"
   Method: POST
   URL: http://localhost:8080/api/admin/utils/fix-null-locations
   
3. Add request: "Check NULL Locations"
   Method: GET
   URL: http://localhost:8080/api/admin/utils/check-null-locations
   
4. Add request: "Get All Users"
   Method: GET
   URL: http://localhost:8080/api/users
```

---

## ⚠️ IMPORTANT NOTES:

### 1. Automatic Location Assignment
```
When fix-null-locations is called:
- Picks first State from database
- Picks first Sambhag under that State
- Picks first District under that Sambhag
- Picks first Block under that District
- Assigns to ALL users with NULL locations
```

### 2. Idempotent Operation
```
✅ Safe to call multiple times
✅ Only fixes users with NULL locations
✅ Does not overwrite existing valid data
```

### 3. Response Status Codes
```
200 OK    - Success
500 Error - Database error or no locations found
```

---

## 📊 EXPECTED BEHAVIOR:

### Before Fix:
```json
{
  "id": "PMUMS202458108",
  "name": "Aman",
  "departmentState": null,      ❌
  "departmentSambhag": null,    ❌
  "departmentDistrict": null,   ❌
  "departmentBlock": null       ❌
}
```

### After Fix:
```json
{
  "id": "PMUMS202458108",
  "name": "Aman",
  "departmentState": "Madhya Pradesh",  ✅
  "departmentSambhag": "इंदौर संभाग",   ✅
  "departmentDistrict": "Indore",       ✅
  "departmentBlock": "Indore"           ✅
}
```

---

## 🎉 SUMMARY:

**Available Endpoints:**
1. ✅ `POST /api/admin/utils/fix-null-locations` - Fix users
2. ✅ `GET /api/admin/utils/check-null-locations` - Check status
3. ✅ `GET /api/users` - Get all users with locations

**All endpoints are:**
- ✅ Public (no auth required)
- ✅ CORS enabled
- ✅ Returning proper JSON
- ✅ Handling errors gracefully

**Frontend can call these directly!** 🚀

