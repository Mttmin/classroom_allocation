# Classroom Allocation - Frontend

React TypeScript frontend for the Classroom Allocation system's Professor Portal.

## Features

### Professor Interface
- **Course List Display**: View all courses taught by the professor
- **Room Type Preferences**:
  - Two modes: Set preferences for all courses at once, or individually per course
  - Drag-and-drop ranking system
  - Search functionality to find room types
  - Minimum 5 room type selections required
- **Weekly Availability Calendar**:
  - Interactive calendar grid (Monday-Friday, 8am-8pm)
  - Default availability pre-filled
  - Click to block unavailable time slots
  - Visual feedback (green = available, red = blocked)

## Tech Stack

- **React 19** with TypeScript
- **Vite** - Build tool and dev server
- **Tailwind CSS v3** - Styling
- **@dnd-kit** - Drag and drop functionality

## Getting Started

### Prerequisites
- Node.js 18+ and npm

### Installation

```bash
# Install dependencies
npm install
```

### Development

```bash
# Start development server (runs on port 3000)
npm run dev
```

The app will be available at [http://localhost:3000](http://localhost:3000)

### Build

```bash
# Build for production
npm run build
```

The build output will be in the `build/` directory, which is configured to be served by the Java backend.

### Type Checking

```bash
# Run TypeScript type checking
npm run lint
```

## Project Structure

```
frontend/
├── src/
│   ├── components/           # React components
│   │   ├── CourseList.tsx           # Course display and mode toggle
│   │   ├── RoomTypeSelector.tsx     # Drag-and-drop room preference selector
│   │   └── AvailabilityCalendar.tsx # Weekly availability grid
│   ├── services/             # API services
│   │   └── api.ts                   # Mock/real API service
│   ├── types/                # TypeScript type definitions
│   │   └── index.ts                 # All type definitions
│   ├── App.tsx               # Main application component
│   ├── main.tsx              # Application entry point
│   └── index.css             # Global styles with Tailwind
├── index.html                # HTML template
├── vite.config.ts            # Vite configuration
├── tsconfig.json             # TypeScript configuration
├── tailwind.config.js        # Tailwind CSS configuration
└── postcss.config.js         # PostCSS configuration
```

## API Integration

The app currently uses a **mock API service** (`src/services/api.ts`) that returns hardcoded data. To switch to the real backend:

1. Open `src/services/api.ts`
2. Call `apiService.setUseMock(false)` or set `useMock = false` in the ApiService class

### API Endpoints (To Be Implemented in Backend)

```typescript
GET    /api/professors/:id              // Get professor by ID
GET    /api/professors/:id/courses      // Get professor's courses
POST   /api/professors/preferences      // Submit preferences
PUT    /api/professors/:id/availability // Update availability
```

## Data Models

All TypeScript types mirror the Java backend models:

- `RoomType` - Enum of 10 room types
- `Professor` - Professor with availability and courses
- `Course` - Course with cohort size, duration, preferences
- `TimeBlocker` - Unavailable time slot
- `ProfessorFormData` - Complete form submission data

See `src/types/index.ts` for full definitions.

## Backend Integration

The Maven `pom.xml` is configured to:
1. Install Node.js and npm
2. Run `npm install` in the frontend directory
3. Run `npm run build`
4. Copy the build output to `src/main/resources/static`

The Java backend can then serve the built frontend as a static SPA.

## Development Notes

- The app uses React 19 with the new JSX transform (no need to import React)
- Tailwind CSS v3 is used for styling (v4 had Windows compatibility issues)
- @dnd-kit provides accessible drag-and-drop functionality
- TypeScript strict mode is enabled for type safety

## Future Enhancements

- [ ] Authentication/login system
- [ ] Real-time validation
- [ ] Save draft preferences
- [ ] View previous submissions
- [ ] Admin interface for managing rooms
- [ ] Schedule visualization
