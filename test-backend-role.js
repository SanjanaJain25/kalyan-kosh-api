/**
 * Test Script for Backend Role Verification
 * Copy and paste this script in your browser console after logging in
 */

// Test 1: Check Login Response for Role
async function testLoginRole() {
    console.log("=".repeat(70));
    console.log("🧪 TEST 1: Login API - Role in Response");
    console.log("=".repeat(70));

    try {
        const response = await fetch('http://localhost:8080/api/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                username: 'ssaman7566@gmail.com',  // Change to your test user
                password: 'Shub@123'                // Change to your test password
            })
        });

        const data = await response.json();

        console.log("📊 Login Response:");
        console.log(JSON.stringify(data, null, 2));

        if (data.user && data.user.role) {
            console.log("✅ PASS: Role found in response");
            console.log(`   Role: ${data.user.role}`);
        } else {
            console.log("❌ FAIL: Role NOT found in response");
        }

        console.log("\n" + "=".repeat(70));
        return data;
    } catch (error) {
        console.error("❌ Error during login test:", error);
    }
}

// Test 2: Check Get User API for Role
async function testGetUserRole(token) {
    console.log("=".repeat(70));
    console.log("🧪 TEST 2: Get User API - Role in Response");
    console.log("=".repeat(70));

    try {
        const response = await fetch('http://localhost:8080/api/users', {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            }
        });

        const users = await response.json();

        console.log("📊 Get Users Response (First User):");
        if (users.length > 0) {
            console.log(JSON.stringify(users[0], null, 2));

            if (users[0].role) {
                console.log("✅ PASS: Role found in user response");
                console.log(`   Role: ${users[0].role}`);
            } else {
                console.log("❌ FAIL: Role NOT found in user response");
            }
        } else {
            console.log("⚠️  No users found");
        }

        console.log("\n" + "=".repeat(70));
        return users;
    } catch (error) {
        console.error("❌ Error during get user test:", error);
    }
}

// Test 3: Decode JWT Token to Check Roles Claim
function testJWTRoles(token) {
    console.log("=".repeat(70));
    console.log("🧪 TEST 3: JWT Token - Roles Claim");
    console.log("=".repeat(70));

    try {
        // Decode JWT (only works if token is not encrypted)
        const base64Url = token.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
            return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
        }).join(''));

        const payload = JSON.parse(jsonPayload);

        console.log("📊 JWT Payload:");
        console.log(JSON.stringify(payload, null, 2));

        if (payload.roles) {
            console.log("✅ PASS: Roles claim found in JWT");
            console.log(`   Roles: ${JSON.stringify(payload.roles)}`);
        } else {
            console.log("❌ FAIL: Roles claim NOT found in JWT");
        }

        console.log("\n" + "=".repeat(70));
        return payload;
    } catch (error) {
        console.error("❌ Error decoding JWT:", error);
    }
}

// Run All Tests
async function runAllTests() {
    console.log("\n\n" + "=".repeat(70));
    console.log("🚀 BACKEND ROLE VERIFICATION TEST SUITE");
    console.log("=".repeat(70) + "\n");

    // Test 1: Login and get token
    const loginData = await testLoginRole();

    if (loginData && loginData.token) {
        // Test 2: Get user with token
        await testGetUserRole(loginData.token);

        // Test 3: Check JWT token
        testJWTRoles(loginData.token);
    } else {
        console.log("⚠️  Skipping tests 2 & 3 - Login failed or no token received");
    }

    console.log("\n" + "=".repeat(70));
    console.log("✅ TEST SUITE COMPLETE");
    console.log("=".repeat(70) + "\n");
}

// Auto-run on load
console.log("\n📝 Backend Role Test Script Loaded!");
console.log("Run: runAllTests() to start testing");
console.log("Or run individual tests:");
console.log("  - testLoginRole()");
console.log("  - testGetUserRole(token)");
console.log("  - testJWTRoles(token)\n");

// Uncomment to auto-run:
// runAllTests();

