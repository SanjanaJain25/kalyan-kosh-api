@echo off

echo 🎯 DEMO DEATH CASE TEST SUITE
echo ═══════════════════════════════════════════
echo.

echo 📋 Running Entity Tests (DeathCaseDemoTest)...
echo ─────────────────────────────────────────────
call mvn test -Dtest=DeathCaseDemoTest -q

echo.
echo 🔧 Running Integration Tests (DeathCaseDemoIntegrationTest)...
echo ─────────────────────────────────────────────────────────────
call mvn test -Dtest=DeathCaseDemoIntegrationTest -q

echo.
echo 💾 Running Demo Data Creator (DemoDeathCaseCreator)...
echo ───────────────────────────────────────────────────────
call mvn test -Dtest=DemoDeathCaseCreator -q

echo.
echo ✅ All demo death case tests completed!
echo ═══════════════════════════════════════════

pause
