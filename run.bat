@echo off
setlocal enabledelayedexpansion

REM Classroom Allocation System - Startup Script
REM This script starts both the backend API server and frontend preview server

echo ======================================================================
echo         Starting Classroom Allocation System
echo ======================================================================
echo.

REM Check if Maven is installed
where mvn >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] Maven is not installed. Please install Maven first.
    exit /b 1
)

REM Check if npm is installed
where npm >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] npm is not installed. Please install Node.js and npm first.
    exit /b 1
)

REM Step 1: Compile backend
echo [1/4] Compiling backend with Maven...
call mvn clean compile -q
if %errorlevel% neq 0 (
    echo [X] Backend compilation failed
    exit /b 1
)
echo [OK] Backend compiled successfully
echo.

REM Step 2: Build frontend
echo [2/4] Building frontend...
cd frontend
call npm run build >nul 2>&1
if %errorlevel% neq 0 (
    echo [X] Frontend build failed
    cd ..
    exit /b 1
)
echo [OK] Frontend built successfully
cd ..
echo.

REM Step 3: Start backend
echo [3/4] Starting backend API server on port 8080...
start /b cmd /c "mvn exec:java -Dexec.mainClass="com.roomallocation.server.ApiServer" -Dexec.args="8080" > backend.log 2>&1"

REM Wait for backend to start
timeout /t 5 /nobreak >nul

REM Check if backend is running
curl -s http://localhost:8080/api/admin/statistics >nul 2>&1
if %errorlevel% equ 0 (
    echo [OK] Backend API server started
) else (
    echo [X] Backend server failed to start. Check backend.log for details.
    taskkill /f /im java.exe >nul 2>&1
    exit /b 1
)
echo.

REM Step 4: Start frontend
echo [4/4] Starting frontend preview server on port 4173...
cd frontend
start /b cmd /c "npm run preview > ../frontend.log 2>&1"
cd ..

REM Wait for frontend to start
timeout /t 3 /nobreak >nul

REM Check if frontend is running
curl -s http://localhost:4173 >nul 2>&1
if %errorlevel% equ 0 (
    echo [OK] Frontend preview server started
) else (
    echo [X] Frontend server failed to start. Check frontend.log for details.
    taskkill /f /im java.exe >nul 2>&1
    taskkill /f /im node.exe >nul 2>&1
    exit /b 1
)
echo.

REM Display status
echo ======================================================================
echo                      System Started Successfully!
echo ======================================================================
echo.
echo Backend API:         http://localhost:8080
echo Frontend:            http://localhost:4173
echo Admin Dashboard:     http://localhost:4173/admin
echo.
echo Logs:
echo   Backend:  backend.log
echo   Frontend: frontend.log
echo.
echo Press Ctrl+C to stop both servers
echo.

REM Keep window open
pause