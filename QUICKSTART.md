# Quick Start - Admin Dashboard

## Run in 2 Steps:

### Step 1: Start Backend API Server
Open a terminal and run:
```bash
.\start-server.bat
```

You should see:
```
========================================
API Server started on port 8080
Base URL: http://localhost:8080
========================================
```

### Step 2: Start Frontend (in a new terminal)
```bash
cd frontend
npm run dev
```

You should see:
```
  VITE v7.x.x  ready in XXX ms

  ➜  Local:   http://localhost:5173/
```

### Step 3: Access Admin Dashboard
Open browser to: **http://localhost:5173/admin**

---

## What You'll See

### Admin Dashboard Features:
✅ **Total professors, courses, and rooms** at a glance
✅ **Preference completion tracking** - see who hasn't entered preferences
✅ **Course statistics** - cohort sizes, assignments
✅ **Algorithm control panel** with all strategies:
   - SmartRandom (recommended)
   - Satisfaction-based
   - Size-based
   - Random
   - Fixed
✅ **Live log output** as algorithm runs
✅ **Results visualization** - allocation rate, match quality

### To Run the Algorithm:
1. Select a strategy (SmartRandom is recommended)
2. Configure parameters (defaults are good)
3. Click "Run Allocation Algorithm"
4. Watch logs in real-time
5. View results when complete

---

## Connecting to Real Backend

Currently using **mock data**. To use real backend:

1. Edit `frontend/src/services/api.ts`
2. Change line 167: `private useMock = false;`
3. Save and refresh browser

---

See **ADMIN_DASHBOARD_GUIDE.md** for full documentation.
