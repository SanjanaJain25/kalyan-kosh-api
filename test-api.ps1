# API Test Script for PMUMS Kalyan Kosh
# Run this after starting the application

Write-Host "======================================" -ForegroundColor Cyan
Write-Host "PMUMS Kalyan Kosh API Test Script" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan
Write-Host ""

$baseUrl = "http://localhost:8080"

# Test 1: Check if server is running
Write-Host "Test 1: Checking if server is running..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/api/locations/hierarchy" -Method GET -TimeoutSec 5
    Write-Host "✓ Server is running!" -ForegroundColor Green
    Write-Host "  Status Code: $($response.StatusCode)" -ForegroundColor Gray
} catch {
    Write-Host "✗ Server is not running or not responding" -ForegroundColor Red
    Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

Write-Host ""

# Test 2: Test Location API (Public endpoint)
Write-Host "Test 2: Testing Location API..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/api/locations/states" -Method GET
    $states = $response.Content | ConvertFrom-Json
    Write-Host "✓ Location API working!" -ForegroundColor Green
    Write-Host "  Found $($states.Count) states" -ForegroundColor Gray
} catch {
    Write-Host "✗ Location API failed" -ForegroundColor Red
    Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# Test 3: Test Login API (should fail without valid credentials)
Write-Host "Test 3: Testing Login API..." -ForegroundColor Yellow
try {
    $loginBody = @{
        email = "test@example.com"
        password = "TestPassword123"
    } | ConvertTo-Json

    $response = Invoke-WebRequest -Uri "$baseUrl/api/auth/login" `
        -Method POST `
        -ContentType "application/json" `
        -Body $loginBody `
        -ErrorAction Stop

    Write-Host "✓ Login API responded" -ForegroundColor Green
    $loginData = $response.Content | ConvertFrom-Json
    if ($loginData.token) {
        Write-Host "  Token received!" -ForegroundColor Gray
    }
} catch {
    if ($_.Exception.Response.StatusCode -eq 403) {
        Write-Host "✓ Login API working (returned 403 for invalid credentials as expected)" -ForegroundColor Green
    } else {
        Write-Host "⚠ Login API responded with: $($_.Exception.Response.StatusCode)" -ForegroundColor Yellow
    }
}

Write-Host ""

# Test 4: Test Registration API structure
Write-Host "Test 4: Testing Registration API..." -ForegroundColor Yellow
try {
    $registrationBody = @{
        name = "Test"
        surname = "User"
        fatherName = "Test Father"
        email = "testuser$(Get-Random)@example.com"
        password = "Test@123"
        mobileNumber = "9876543210"
        phoneNumber = "9876543210"
        countryCode = "+91"
        gender = "MALE"
        maritalStatus = "SINGLE"
        homeAddress = "Test Address"
        schoolOfficeName = "Test School"
        sankulName = "Test Sankul"
        department = "Education"
        departmentUniqueId = "DEPT$(Get-Random)"
        acceptedTerms = $true
    } | ConvertTo-Json

    $response = Invoke-WebRequest -Uri "$baseUrl/api/auth/register" `
        -Method POST `
        -ContentType "application/json" `
        -Body $registrationBody `
        -ErrorAction Stop

    Write-Host "✓ Registration API working!" -ForegroundColor Green
    Write-Host "  Response: $($response.Content)" -ForegroundColor Gray
} catch {
    Write-Host "⚠ Registration test: $($_.Exception.Response.StatusCode)" -ForegroundColor Yellow
    if ($_.Exception.Response) {
        $reader = [System.IO.StreamReader]::new($_.Exception.Response.GetResponseStream())
        $errorBody = $reader.ReadToEnd()
        Write-Host "  Details: $errorBody" -ForegroundColor Gray
    }
}

Write-Host ""

# Test 5: Test Get All Users (should require authentication)
Write-Host "Test 5: Testing User API (requires auth)..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/api/users/" -Method GET -ErrorAction Stop
    Write-Host "✗ User API should require authentication!" -ForegroundColor Red
} catch {
    if ($_.Exception.Response.StatusCode -eq 403 -or $_.Exception.Response.StatusCode -eq 401) {
        Write-Host "✓ User API correctly requires authentication" -ForegroundColor Green
        Write-Host "  Status: $($_.Exception.Response.StatusCode)" -ForegroundColor Gray
    } else {
        Write-Host "⚠ Unexpected response: $($_.Exception.Response.StatusCode)" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "======================================" -ForegroundColor Cyan
Write-Host "Test Summary Complete" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Next Steps:" -ForegroundColor Yellow
Write-Host "1. Create a test user via registration" -ForegroundColor White
Write-Host "2. Login with the test user to get JWT token" -ForegroundColor White
Write-Host "3. Test protected endpoints with the token" -ForegroundColor White
Write-Host ""
Write-Host "For detailed API documentation, see:" -ForegroundColor Yellow
Write-Host "  - API_DOCUMENTATION_UPDATED.md" -ForegroundColor White
Write-Host "  - USER_ENTITY_CHANGES_SUMMARY.md" -ForegroundColor White
Write-Host ""

