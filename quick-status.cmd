@echo off
echo.
echo ========================================
echo   PMUMS Kalyan Kosh API - Quick Status
echo ========================================
echo.

powershell -Command "$port = Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue; if ($port) { Write-Host 'Status: RUNNING on port 8080' -ForegroundColor Green; Write-Host 'URL: http://localhost:8080' -ForegroundColor Cyan; Write-Host 'Process ID:' $port.OwningProcess -ForegroundColor Gray } else { Write-Host 'Status: NOT RUNNING' -ForegroundColor Red; Write-Host 'Run start-app.ps1 to start' -ForegroundColor Yellow }"

echo.
pause

