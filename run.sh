#!/bin/bash

# Classroom Allocation System - Startup Script
# This script starts both the backend API server and frontend preview server

set -e  # Exit on error

echo "╔══════════════════════════════════════════════════════════════════════╗"
echo "║        Starting Classroom Allocation System                          ║"
echo "╚══════════════════════════════════════════════════════════════════════╝"
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo -e "${RED}Error: Maven is not installed. Please install Maven first.${NC}"
    exit 1
fi

# Check if npm is installed
if ! command -v npm &> /dev/null; then
    echo -e "${RED}Error: npm is not installed. Please install Node.js and npm first.${NC}"
    exit 1
fi

# Function to cleanup on exit
cleanup() {
    echo ""
    echo -e "${YELLOW}Shutting down servers...${NC}"
    if [ ! -z "$BACKEND_PID" ]; then
        kill $BACKEND_PID 2>/dev/null || true
    fi
    if [ ! -z "$FRONTEND_PID" ]; then
        kill $FRONTEND_PID 2>/dev/null || true
    fi
    echo -e "${GREEN}Servers stopped.${NC}"
    exit 0
}

# Register cleanup function
trap cleanup SIGINT SIGTERM

# Step 1: Compile backend
echo -e "${BLUE}[1/4] Compiling backend with Maven...${NC}"
mvn clean compile -q
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Backend compiled successfully${NC}"
else
    echo -e "${RED}✗ Backend compilation failed${NC}"
    exit 1
fi
echo ""

# Step 2: Build frontend
echo -e "${BLUE}[2/4] Building frontend...${NC}"
cd frontend
npm run build > /dev/null 2>&1
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Frontend built successfully${NC}"
else
    echo -e "${RED}✗ Frontend build failed${NC}"
    exit 1
fi
cd ..
echo ""

# Step 3: Start backend
echo -e "${BLUE}[3/4] Starting backend API server on port 8080...${NC}"
mvn exec:java -Dexec.mainClass="com.roomallocation.server.ApiServer" -Dexec.args="8080" > backend.log 2>&1 &
BACKEND_PID=$!

# Wait for backend to start
sleep 5

# Check if backend is running
if curl -s http://localhost:8080/api/admin/statistics > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Backend API server started (PID: $BACKEND_PID)${NC}"
else
    echo -e "${RED}✗ Backend server failed to start. Check backend.log for details.${NC}"
    kill $BACKEND_PID 2>/dev/null || true
    exit 1
fi
echo ""

# Step 4: Start frontend
echo -e "${BLUE}[4/4] Starting frontend preview server on port 4173...${NC}"
cd frontend
npm run preview > ../frontend.log 2>&1 &
FRONTEND_PID=$!

# Wait for frontend to start
sleep 3

# Check if frontend is running
if curl -s http://localhost:4173 > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Frontend preview server started (PID: $FRONTEND_PID)${NC}"
else
    echo -e "${RED}✗ Frontend server failed to start. Check frontend.log for details.${NC}"
    kill $BACKEND_PID 2>/dev/null || true
    kill $FRONTEND_PID 2>/dev/null || true
    exit 1
fi
cd ..
echo ""

# Display status
echo "╔══════════════════════════════════════════════════════════════════════╗"
echo "║                     System Started Successfully!                     ║"
echo "╚══════════════════════════════════════════════════════════════════════╝"
echo ""
echo -e "${GREEN}Backend API:${NC}         http://localhost:8080"
echo -e "${GREEN}Frontend:${NC}            http://localhost:4173"
echo -e "${GREEN}Admin Dashboard:${NC}     http://localhost:4173/admin"
echo ""
echo -e "${YELLOW}Logs:${NC}"
echo -e "  Backend:  backend.log"
echo -e "  Frontend: frontend.log"
echo ""
echo -e "${BLUE}Press Ctrl+C to stop both servers${NC}"
echo ""

# Wait for user to stop
wait
