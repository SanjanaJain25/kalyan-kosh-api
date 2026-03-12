@echo off
echo PMUMS Backend Compilation Troubleshooting
echo ========================================

echo.
echo Checking Java version...
java -version
echo.

echo Checking JAVA_HOME...
echo JAVA_HOME: %JAVA_HOME%
echo.

echo Checking Maven version...
mvn -version
echo.

echo Current directory structure:
dir src\main\java\com\example\kalyan_kosh_api\dto\
echo.

echo Attempting to compile individual files...
echo.

cd /d "%~dp0"

echo Compiling LoginResponse.java...
javac -cp "C:\Users\%USERNAME%\.m2\repository\org\springframework\boot\spring-boot-starter\*" src\main\java\com\example\kalyan_kosh_api\dto\LoginResponse.java

echo.
echo If compilation worked, class files should be created.
echo If not, you need to install JDK (not just JRE) and set JAVA_HOME properly.

echo.
echo Next steps:
echo 1. Install JDK 17 or later
echo 2. Set JAVA_HOME to JDK path (not JRE)
echo 3. Restart your IDE/terminal
echo 4. Run: mvn clean compile

pause
