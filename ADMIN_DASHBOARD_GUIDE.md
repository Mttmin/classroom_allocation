# Admin Dashboard Setup Guide

This guide explains how to run the new admin dashboard for the Classroom Allocation System.

## What's Been Added

### Backend Components
- **Admin Statistics Service** - Calculates system-wide statistics
- **Admin Controller** - REST API endpoints for statistics and preferences
- **Algorithm Controller** - Runs allocation algorithm with configurable parameters
- **Data Loaders** - Load professors and courses from JSON files
- **API Server** - Lightweight HTTP server for all endpoints

### Frontend Components
- **Admin Page** - Comprehensive dashboard with:
  - System statistics (professors, courses, rooms)
  - Preference completion tracking
  - Algorithm control panel with all strategies
  - Live log output
  - Results visualization

### API Endpoints

#### Admin Statistics
- `GET /api/admin/statistics` - System-wide statistics
- `GET /api/admin/preferences/status` - Preference completion details
- `GET /api/admin/professors/incomplete` - Professors with incomplete preferences

#### Algorithm Control
- `POST /api/admin/algorithm/run` - Run allocation algorithm
- `GET /api/admin/algorithm/status` - Get algorithm status and results

#### Room Types
- `GET /api/rooms/types` - All room types
- `GET /api/rooms/type/{roomType}` - Specific room type info

## How to Run

### Option 1: Using the Provided Script (Easiest)

#### 1. Start the Backend API Server

```bash
# Run this from the project root
.\start-server.bat
```

This will:
- Compile the backend Java code
- Start the API server on port 8080
- Display the available endpoints

#### 2. Start the Frontend (In a separate terminal)

```bash
cd frontend
npm install    # Only needed first time
npm run dev
```

The frontend will start on `http://localhost:5173` (or another port if 5173 is busy)

#### 3. Access the Admin Dashboard

Open your browser to: **http://localhost:5173/admin**

### Option 2: Manual Setup

#### 1. Compile Backend

```bash
mvn clean compile -DskipTests
```

#### 2. Start API Server

```bash
java -cp target/classes com.roomallocation.server.ApiServer 8080
```

#### 3. Start Frontend

```bash
cd frontend
npm install
npm run dev
```

#### 4. Open Admin Dashboard

Navigate to: **http://localhost:5173/admin**

## Using the Admin Dashboard

### 1. View Statistics
- **Top Cards**: Quick overview of professors, courses, and rooms
- **Preference Status**: See how many professors have completed preferences
- **Course Statistics**: View cohort sizes and assignment status

### 2. Run Allocation Algorithm

**Strategy Selection:**
- **SmartRandom** - Randomly selects from suitable room types (Recommended)
- **Satisfaction** - Uses satisfaction survey data
- **SizeBased** - Matches based on cohort size
- **Random** - Completely random selection
- **Fixed** - Uses predefined preference order

**Parameters:**
- **Number of Preferences**: How many room types to include (recommended: 10)
- **Use Existing Courses**: Use courses from database vs. generate new ones
- **Auto-complete Preferences**: Fill in missing professor preferences automatically

**Simulation Parameters** (when generating new courses):
- **Num Courses**: Number of courses to generate (default: 70)
- **Min Size**: Minimum cohort size (default: 10)
- **Max Size**: Maximum cohort size (default: 200)
- **Change Size**: Split point between small/large classes (default: 35)

### 3. Monitor Execution
- Watch the **Log Output** section at the bottom for real-time progress
- Logs show algorithm start, configuration, and completion

### 4. View Results
After the algorithm completes, the **Last Allocation Results** panel shows:
- **Allocation Rate**: % of courses successfully assigned
- **Assigned Courses**: Number of courses with rooms
- **First Choice Matches**: Courses that got their 1st preference
- **Top-3 Choice Matches**: Courses that got top-3 preferences
- **Average Choice Rank**: Quality metric (lower is better)

## Toggling Between Mock and Real API

The frontend is configured to use **mock data by default** for testing.

To connect to the real backend:

1. Open `frontend/src/services/api.ts`
2. Find line 167: `private useMock = true;`
3. Change to: `private useMock = false;`
4. Save and refresh the browser

## Troubleshooting

### Backend won't compile
- Ensure Java 24 (or compatible version) is installed
- Run `mvn clean` first to clear any cached builds

### Port 8080 already in use
Start the server on a different port:
```bash
java -cp target/classes com.roomallocation.server.ApiServer 3000
```

Then update the frontend API URLs in `api.ts` to match.

### Frontend won't start
- Make sure Node.js is installed: `node --version`
- Delete `node_modules` and run `npm install` again
- Check if port 5173 is available

### CORS errors in browser
The backend API includes CORS headers to allow cross-origin requests. If you still see errors:
- Ensure the backend server is running
- Check browser console for the actual error
- Verify the frontend is making requests to the correct URL

## File Locations

### Backend Files
```
src/main/java/com/roomallocation/
├── controller/
│   ├── AdminController.java          # Admin API endpoints
│   ├── AlgorithmController.java      # Algorithm execution
│   └── RoomTypeController.java       # Room type API
├── service/
│   ├── AdminService.java             # Statistics calculations
│   └── RoomTypeService.java          # Room type logic
├── util/
│   ├── ProfessorDataLoader.java      # Load professors from JSON
│   └── CourseDataLoader.java         # Load courses from JSON
└── server/
    └── ApiServer.java                # HTTP server

src/main/resources/
├── professors.json                   # Professor data
├── courses.json                      # Course data (optional)
└── rooms.csv                         # Room data
```

### Frontend Files
```
frontend/src/
├── components/
│   ├── AdminPage.tsx                 # Main admin dashboard
│   └── Navigation.tsx                # Updated with admin link
├── services/
│   └── api.ts                        # API service with admin methods
└── App.tsx                           # Updated with /admin route
```

## Next Steps

### Connecting Real Data
1. Ensure `professors.json` has all your professors
2. Add professor courses to `courses.json` (optional)
3. Professors can enter preferences via the main UI
4. Run allocation from admin dashboard

### Production Deployment
For production:
1. Uncomment the frontend-maven-plugin in `pom.xml`
2. Fix the Node.js installation path issue
3. Run `mvn clean package` to build a single JAR
4. Deploy the JAR with embedded frontend

### Adding Features
The admin dashboard is extensible. You can add:
- Real-time WebSocket updates for algorithm progress
- Export results to CSV/Excel
- Email notifications when allocation completes
- Professor reminder emails for missing preferences
- Historical allocation comparison

## Support

If you encounter issues:
1. Check that both backend and frontend are running
2. Verify the backend logs for any errors
3. Check browser console for frontend errors
4. Ensure all required data files exist in `src/main/resources/`

---

**Built with:**
- Backend: Java 24, Maven, Jackson, com.sun.httpserver
- Frontend: React 19, TypeScript, Tailwind CSS, Vite
