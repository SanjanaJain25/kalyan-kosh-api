@echo off
echo ===============================================
echo FIXING NULL LOCATIONS FOR EXISTING USERS
echo ===============================================
echo.
echo Calling: POST http://localhost:8080/api/admin/utils/fix-null-locations
echo.

curl -X POST http://localhost:8080/api/admin/utils/fix-null-locations -H "Content-Type: application/json" -v

echo.
echo ===============================================
echo Check console logs above for results!
echo ===============================================
pause

