# ========================================
# PMUMS Kalyan Kosh API - Status Check
# ========================================

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Application Status Check" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Check 1: Java processes
Write-Host "1. Java Processes:" -ForegroundColor Yellow
$javaProcesses = Get-Process -Name "java" -ErrorAction SilentlyContinue
if ($javaProcesses) {
    Write-Host "   ✓ Found $($javaProcesses.Count) Java process(es) running" -ForegroundColor Green
    $javaProcesses | ForEach-Object {
        Write-Host "     - PID: $($_.Id) | Memory: $([math]::Round($_.WorkingSet64/1MB, 2)) MB | CPU: $($_.CPU)s" -ForegroundColor Gray
    }
} else {
    Write-Host "   ✗ No Java processes running" -ForegroundColor Red
}

Write-Host ""

# Check 2: Port 8080
Write-Host "2. Port 8080 Status:" -ForegroundColor Yellow
$portCheck = Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue
if ($portCheck) {
    Write-Host "   ✓ Port 8080 is IN USE" -ForegroundColor Green
    Write-Host "     - Process ID: $($portCheck.OwningProcess)" -ForegroundColor Gray
    Write-Host "     - State: $($portCheck.State)" -ForegroundColor Gray
    Write-Host "     - Local Address: $($portCheck.LocalAddress):$($portCheck.LocalPort)" -ForegroundColor Gray
} else {
    Write-Host "   ✗ Port 8080 is FREE (not in use)" -ForegroundColor Red
}

Write-Host ""

# Check 3: API Response
Write-Host "3. API Endpoint Test:" -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api/locations/states" -Method GET -TimeoutSec 3 -ErrorAction Stop
    Write-Host "   ✓ API is RESPONDING!" -ForegroundColor Green
    Write-Host "     - Status Code: $($response.StatusCode)" -ForegroundColor Gray
    Write-Host "     - Content Type: $($response.Headers.'Content-Type')" -ForegroundColor Gray
    $content = $response.Content | ConvertFrom-Json
    if ($content -is [Array]) {
        Write-Host "     - Response: Array with $($content.Count) items" -ForegroundColor Gray
    }
} catch {
    Write-Host "   ✗ API is NOT responding" -ForegroundColor Red
    Write-Host "     - Error: $($_.Exception.Message)" -ForegroundColor Gray
}

Write-Host ""

# Check 4: Project Directory
Write-Host "4. Project Directory:" -ForegroundColor Yellow
$projectPath = "C:\Users\shub\Downloads\kalyan-kosh-api"
if (Test-Path $projectPath) {
    Write-Host "   ✓ Project directory exists" -ForegroundColor Green
    Write-Host "     - Path: $projectPath" -ForegroundColor Gray

    # Check for pom.xml
    if (Test-Path "$projectPath\pom.xml") {
        Write-Host "   ✓ pom.xml found" -ForegroundColor Green
    } else {
        Write-Host "   ✗ pom.xml not found" -ForegroundColor Red
    }

    # Check for target directory
    if (Test-Path "$projectPath\target") {
        Write-Host "   ✓ target directory exists (compiled)" -ForegroundColor Green
    } else {
        Write-Host "   ⚠  target directory not found (not compiled yet)" -ForegroundColor Yellow
    }
} else {
    Write-Host "   ✗ Project directory not found" -ForegroundColor Red
}

Write-Host ""

# Summary
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Summary" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$isRunning = $portCheck -and $javaProcesses

if ($isRunning) {
    Write-Host "✅ APPLICATION IS RUNNING!" -ForegroundColor Green
    Write-Host ""
    Write-Host "🌐 Access the API at:" -ForegroundColor Cyan
    Write-Host "   http://localhost:8080" -ForegroundColor White
    Write-Host ""
    Write-Host "📋 Available endpoints:" -ForegroundColor Cyan
    Write-Host "   http://localhost:8080/api/locations/states" -ForegroundColor White
    Write-Host "   http://localhost:8080/api/locations/hierarchy" -ForegroundColor White
    Write-Host "   http://localhost:8080/api/auth/login" -ForegroundColor White
    Write-Host ""
    Write-Host "To stop the application, run:" -ForegroundColor Yellow
    Write-Host "   .\stop-app.ps1" -ForegroundColor White
} else {
    Write-Host "⚠️  APPLICATION IS NOT RUNNING" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "To start the application, run:" -ForegroundColor Yellow
    Write-Host "   .\start-app.ps1" -ForegroundColor White
    Write-Host ""
    Write-Host "To test the API after starting, run:" -ForegroundColor Yellow
    Write-Host "   .\test-api.ps1" -ForegroundColor White
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

