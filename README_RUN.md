# Running the Classroom Allocation System

## Quick Start

### Option 1: Using the Run Script (Recommended)

Simply run:

```bash
./run.sh
```

This script will:
1. Compile the backend with Maven
2. Build the frontend
3. Start the backend API server on port 8080
4. Start the frontend preview server on port 4173
5. Display status and URLs

Press `Ctrl+C` to stop both servers.

### Option 2: Manual Start

#### Backend Only

```bash
# Compile the code
mvn clean compile

# Run the API server
mvn exec:java -Dexec.mainClass="com.roomallocation.server.ApiServer" -Dexec.args="8080"
```

The backend will be available at http://localhost:8080

#### Frontend Only

```bash
# Navigate to frontend directory
cd frontend

# Build the frontend
npm run build

# Run the preview server
npm run preview
```

The frontend will be available at http://localhost:4173

## Access Points

- **Frontend Application**: http://localhost:4173
- **Admin Dashboard**: http://localhost:4173/admin
- **Backend API**: http://localhost:8080
- **API Statistics**: http://localhost:8080/api/admin/statistics

## Prerequisites

- Java 24+ (or Java 25)
- Maven 3.8+
- Node.js 18+ and npm

## Troubleshooting

If the servers don't start:

1. Check if ports 8080 and 4173 are available:
   ```bash
   lsof -i :8080
   lsof -i :4173
   ```

2. Check the log files:
   - `backend.log` - Backend server logs
   - `frontend.log` - Frontend server logs

3. Kill any existing processes on those ports:
   ```bash
   kill $(lsof -ti:8080)
   kill $(lsof -ti:4173)
   ```

## API Endpoints

### Admin Endpoints
- `GET /api/admin/statistics` - System statistics
- `GET /api/admin/preferences/status` - Preference completion status
- `POST /api/admin/algorithm/run` - Run allocation algorithm
- `GET /api/admin/algorithm/status` - Algorithm status

### Professor Endpoints
- `GET /api/professors/{id}` - Professor details
- `GET /api/professors/{id}/courses` - Professor's courses
- `GET /api/professors/{id}/allocation` - Allocation results
- `POST /api/professors/preferences` - Submit preferences
- `PUT /api/professors/{id}/availability` - Update availability

### Room Endpoints
- `GET /api/rooms/types` - All room types
- `GET /api/rooms/type/{type}` - Specific room type info
