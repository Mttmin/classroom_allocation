@echo off
echo ========================================
echo Starting Classroom Allocation API Server
echo ========================================
echo.

cd /d "%~dp0"

echo Building backend with dependencies...
call mvn clean package -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Build failed
    pause
    exit /b 1
)

echo.
echo Starting API server on port 8080...
echo Press Ctrl+C to stop the server
echo.

java -jar target/room-allocation-1.0-SNAPSHOT.jar 8080
pause
