# Simulator

### Classes

1. **ProfessorSimulator.java** - Generate professors with configurable availability

   - 9 different availability modes (full-time, part-time, morning-only, etc.)
   - Mixed availability distributions
   - Realistic French professor names
   - Time block management with gaps
2. **ClassroomSimulator.java** - Generate classrooms with various distributions

   - 4 distribution modes (uniform, realistic, small-focused, large-focused)
   - Custom room configurations
   - Unavailability simulation
   - Maintenance window support
3. **CourseSimulator.java (Extended)** - Enhanced correlation matrix generation

   - 4 correlation modes (none, fully random, subject-based, clustered)
   - Professor assignment (random/round-robin)
   - Duration assignment
   - Configurable cluster simulation
4. **SimulatorConfig.java** - Centralized configuration

   - Builder pattern for easy setup
   - 4 preset configurations (default, small, large, stress-test)
   - All settings in one place
   - toString() for debugging
5. **SimulationRunner.java** - Main orchestrator

   - Runs all simulators in correct order
   - Generates complete datasets
   - Provides summary statistics
   - Result container with all data
6. **JsonExporter.java** - Export to JSON

   - Frontend-compatible format
   - Backend-compatible format
   - Individual or bulk export
   - Proper TypeScript interface alignment
7. **SimulatorExample.java** - Usage examples

   - 9 different example scenarios
   - All modes demonstrated
   - Ready to run

## Key Features

### Professor Availability Modes

```java
FULL_TIME              // 100% available (Mon-Fri 8AM-8PM)
MOSTLY_AVAILABLE_95    // 95% available with random gaps
MOSTLY_AVAILABLE_85    // 85% available with random gaps
PART_TIME_50          // ~50% available
PART_TIME_30          // ~30% available
MORNING_ONLY          // 8AM-1PM only
AFTERNOON_ONLY        // 1PM-8PM only
THREE_DAYS_WEEK       // Mon/Wed/Fri only
TWO_DAYS_WEEK         // Tue/Thu only
```

### Correlation Matrix Modes

```java
NONE           // No correlations (all 0.0)
FULLY_RANDOM   // Random correlations with realistic distribution
SUBJECT_BASED  // Based on course prefix (original implementation)
CLUSTERED      // Simulates program clusters with high internal correlation
```

### Classroom Distribution Modes

```java
UNIFORM        // Equal distribution across all room types
REALISTIC      // Realistic university distribution
SMALL_FOCUSED  // More small classrooms, fewer amphitheaters
LARGE_FOCUSED  // More large amphitheaters
CUSTOM         // Define your own distribution
```

## Quick Start

```java
// 1. Create configuration
SimulatorConfig config = new SimulatorConfig.Builder()
    .courses(50)
    .professors(20)
    .classrooms(80)
    .mixedProfessorAvailability(0.5, 0.3, 0.2)
    .correlationMode(CorrelationMode.CLUSTERED)
    .clusterConfiguration(4, 0.3, 0.9)
    .build();

// 2. Run simulation
PreferenceGenerationStrategy strategy = new RandomPreferenceStrategy(5);
SimulationRunner runner = new SimulationRunner(config, strategy);
SimulationRunner.SimulationResult result = runner.runSimulation();

// 3. Export data
JsonExporter.exportAll(result, "./simulation_output");

// 4. Access data
List<Course> courses = result.getCourses();
List<Professor> professors = result.getProfessors();
List<Room> rooms = result.getRooms();
double[][] correlationMatrix = result.getCorrelationMatrix();
```

## Preset Configurations

Use these for common scenarios:

```java
// Simple test (10 courses, 5 professors, 15 rooms)
SimulatorConfig.createSimpleTestConfig()

// Small university (30 courses, 10 professors, 40 rooms)
SimulatorConfig.createSmallUniversityConfig()

// Large university (200 courses, 80 professors, 150 rooms)
SimulatorConfig.createLargeUniversityConfig()

// Stress test (500 courses, 50 professors, 100 rooms, high constraints)
SimulatorConfig.createStressTestConfig()
```

## JSON Output Format

### Frontend Compatible

All exported JSON files match your frontend TypeScript interfaces in `frontend/src/types/index.ts`:

- Course interface
- Professor interface with WeeklyAvailability
- Room interface
- TimeBlocker interface
- DayOfWeek and RoomType enums

### Backend Compatible

All data structures match your backend Java models:

- Course model with professorId and durationMinutes
- Professor model with AvailabilityPeriod
- Room model with unavailableSlots
- Correlation matrix as double[][]

## Example Outputs

### Example 1: Simple Test

```
Courses: 10 (sizes: 20-50)
Professors: 5 (all full-time)
Classrooms: 15 (realistic distribution)
Correlation: None
```

### Example 2: Realistic University

```
Courses: 120 (sizes: 15-300, mostly small)
Professors: 40 (60% full-time, 25% mostly available, 15% part-time)
Classrooms: 90 (realistic distribution)
Correlation: Clustered (5 programs, high internal correlation)
```

### Example 3: Stress Test

```
Courses: 500
Professors: 50 (many part-time, high course-to-professor ratio)
Classrooms: 100 (limited capacity)
Correlation: Clustered (high constraints to stress the algorithm)
```

## Running Examples

```bash
# Compile
javac src/main/java/com/roomallocation/simulator/*.java

# Run all examples
java -cp . com.roomallocation.simulator.SimulatorExample

# Or run from your IDE
# Just execute SimulatorExample.main()
```

## Configuration Options

### Course Configuration

- Number of courses
- Cohort size range (min, max, threshold)
- Size distribution (small %, medium %, large %)
- Professor assignment mode (random/round-robin)

### Professor Configuration

- Number of professors
- Single availability mode for all
- Mixed availability distribution
- Custom availability patterns

### Classroom Configuration

- Number of classrooms
- Distribution mode
- Custom room type configurations
- Unavailability rates

### Correlation Configuration

- Correlation mode
- Number of clusters (for CLUSTERED mode)
- Inter-cluster correlation
- Intra-cluster correlation

## Integration

### With Backend Allocation

```java
// Generate test data
SimulationResult result = runner.runSimulation();

// Use in allocation algorithm
TypeBasedAllocation allocator = new TypeBasedAllocation(
    result.getCourses(),
    result.getRooms()
);
Map<String, String> assignments = allocator.allocate();
```

### With Backend Scheduler

```java
// Use in scheduler
Scheduler scheduler = new MyScheduler(
    result.getCourses(),
    result.getRooms(),
    convertToMap(result.getProfessors()), // Convert List to Map
    result.getCorrelationMatrix()
);
Schedule schedule = scheduler.optimize();
```

### With Frontend

```typescript
// Import generated JSON
import courses from './simulation_output/courses.json';
import professors from './simulation_output/professors.json';
import rooms from './simulation_output/rooms.json';

// Types already match your interfaces!
const courseList: Course[] = courses;
const professorList: Professor[] = professors;
const roomList: Room[] = rooms;
```
