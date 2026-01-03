# ========================================
# PMUMS Kalyan Kosh API - Start Script
# ========================================

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  PMUMS Kalyan Kosh API Manager" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Step 1: Kill any existing Java processes
Write-Host "Step 1: Checking for existing Java processes..." -ForegroundColor Yellow
$javaProcesses = Get-Process -Name "java" -ErrorAction SilentlyContinue
if ($javaProcesses) {
    Write-Host "Found $($javaProcesses.Count) Java process(es). Stopping them..." -ForegroundColor Yellow
    $javaProcesses | ForEach-Object {
        Stop-Process -Id $_.Id -Force
        Write-Host "  ✓ Killed process PID: $($_.Id)" -ForegroundColor Green
    }
    Start-Sleep -Seconds 2
} else {
    Write-Host "  ✓ No existing Java processes found" -ForegroundColor Green
}

Write-Host ""

# Step 2: Check if port 8080 is in use
Write-Host "Step 2: Checking if port 8080 is available..." -ForegroundColor Yellow
$portInUse = Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue
if ($portInUse) {
    Write-Host "  ⚠️  Port 8080 is in use by PID: $($portInUse.OwningProcess)" -ForegroundColor Red
    Write-Host "  Attempting to kill the process..." -ForegroundColor Yellow
    Stop-Process -Id $portInUse.OwningProcess -Force -ErrorAction SilentlyContinue
    Start-Sleep -Seconds 2
    Write-Host "  ✓ Process killed" -ForegroundColor Green
} else {
    Write-Host "  ✓ Port 8080 is available" -ForegroundColor Green
}

Write-Host ""

# Step 3: Set JAVA_HOME
Write-Host "Step 3: Setting JAVA_HOME..." -ForegroundColor Yellow
$env:JAVA_HOME = "C:\Users\shub\.jdks\corretto-17.0.9"
Write-Host "  ✓ JAVA_HOME set to: $env:JAVA_HOME" -ForegroundColor Green

Write-Host ""

# Step 4: Navigate to project directory
Write-Host "Step 4: Navigating to project directory..." -ForegroundColor Yellow
Set-Location "C:\Users\shub\Downloads\kalyan-kosh-api"
Write-Host "  ✓ Current directory: $(Get-Location)" -ForegroundColor Green

Write-Host ""

# Step 5: Start the application
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Starting Application..." -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "🚀 Starting PMUMS Kalyan Kosh API on http://localhost:8080" -ForegroundColor Green
Write-Host ""
Write-Host "📝 Note: Application will start in a few seconds..." -ForegroundColor Yellow
Write-Host "📝 Press Ctrl+C to stop the application" -ForegroundColor Yellow
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Start the application
& ./mvnw spring-boot:run
