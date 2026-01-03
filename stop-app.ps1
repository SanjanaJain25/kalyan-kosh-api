# ========================================
# PMUMS Kalyan Kosh API - Stop Script
# ========================================

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Stopping PMUMS Kalyan Kosh API" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Find and kill all Java processes
Write-Host "Looking for Java processes..." -ForegroundColor Yellow
$javaProcesses = Get-Process -Name "java" -ErrorAction SilentlyContinue

if ($javaProcesses) {
    Write-Host "Found $($javaProcesses.Count) Java process(es):" -ForegroundColor Yellow
    $javaProcesses | ForEach-Object {
        Write-Host "  - PID: $($_.Id) | CPU: $($_.CPU) | Memory: $([math]::Round($_.WorkingSet64/1MB, 2)) MB" -ForegroundColor Gray
    }

    Write-Host ""
    Write-Host "Stopping all Java processes..." -ForegroundColor Yellow

    $javaProcesses | ForEach-Object {
        try {
            Stop-Process -Id $_.Id -Force
            Write-Host "  ✓ Stopped process PID: $($_.Id)" -ForegroundColor Green
        } catch {
            Write-Host "  ✗ Failed to stop PID: $($_.Id)" -ForegroundColor Red
        }
    }

    Write-Host ""
    Write-Host "✅ All Java processes stopped!" -ForegroundColor Green
} else {
    Write-Host "✓ No Java processes found running" -ForegroundColor Green
}

Write-Host ""

# Check if port 8080 is now free
Write-Host "Checking port 8080 status..." -ForegroundColor Yellow
$portCheck = Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue

if ($portCheck) {
    Write-Host "⚠️  Port 8080 is still in use by PID: $($portCheck.OwningProcess)" -ForegroundColor Yellow
    Write-Host "   Attempting to kill this process..." -ForegroundColor Yellow
    try {
        Stop-Process -Id $portCheck.OwningProcess -Force
        Write-Host "   ✓ Process killed" -ForegroundColor Green
    } catch {
        Write-Host "   ✗ Failed to kill process" -ForegroundColor Red
    }
} else {
    Write-Host "✓ Port 8080 is now free" -ForegroundColor Green
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Done!" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

