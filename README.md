# Classroom Allocation System

A sophisticated Java-based application for optimizing classroom and room allocations using market design principles and deferred acceptance algorithms. This system simulates course scheduling scenarios and employs various preference generation strategies to efficiently match courses with available rooms.

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [System Requirements](#system-requirements)
- [Installation](#installation)
- [Usage](#usage)
- [Project Structure](#project-structure)
- [Configuration](#configuration)
- [Algorithms &amp; Strategies](#algorithms--strategies)
- [Visualization](#visualization)
- [License](#license)
- [Contact](#contact)

## Overview

This project implements a room allocation system based on market design theory, specifically utilizing a type-based deferred acceptance algorithm. The system simulates realistic classroom allocation scenarios where courses have preferences over room types, and rooms have varying capacities and characteristics.

The application can:

- Generate realistic course cohorts with variable sizes
- Simulate multiple allocation scenarios with different preference strategies
- Optimize room assignments based on capacity fit and preferences
- Collect and analyze allocation statistics
- Visualize results through interactive Python-generated charts and maps

## Features

- **Type-Based Allocation Algorithm**: Implements a deferred acceptance mechanism for stable matching between courses and rooms
- **Multiple Preference Strategies**:
  - Satisfaction-based strategy
  - Size-based preference strategy
  - Smart random preference strategy
  - Fixed preference strategy
  - Random preference strategy
- **Course Simulation**: Generate realistic course distributions with configurable parameters
- **Statistical Analysis**: Comprehensive statistics collection across multiple simulation runs
- **Data Visualization**: Interactive charts and campus maps showing allocation results
- **JSON Export**: Export allocation results and statistics for further analysis
- **Capacity Optimization**: Intelligent fitting algorithms to minimize wasted space

## System Requirements

- **Java**: JDK 11 or higher
- **Maven**: 3.6 or higher
- **Python**: 3.7 or higher (for visualization)
- **Python Libraries**:
  - matplotlib
  - json
  - (additional dependencies as specified in visualization scripts)

## Installation

1. **Clone or download the repository**:

   ```bash
   cd c:\Users\matth\Documents\Cours 3A\market design\classroom_allocation
   ```
2. **Navigate to the project directory**:

   ```bash
   cd room-allocation
   ```
3. **Build the project using Maven**:

   ```bash
   mvn clean install
   ```
4. **Install Python dependencies** (for visualization):

   ```bash
   pip install matplotlib numpy pandas
   ```

## Usage

### Running the Application

1. **Execute the main program**:

   ```bash
   mvn exec:java -Dexec.mainClass="com.roomallocation.Main"
   ```

   Or compile and run the JAR:

   ```bash
   mvn clean package
   java -jar target/room-allocation-1.0-SNAPSHOT.jar
   ```
2. **Configure simulation parameters** in `Main.java`:

   ```java
   int numSimulations = 100;  // Number of simulation runs
   int numCourses = 70;       // Number of courses to allocate
   int minSize = 10;          // Minimum cohort size
   int maxSize = 200;         // Maximum cohort size
   int changeSize = 35;       // Size variation parameter
   ```
3. **View results**:

   - Allocation results are saved to `src/main/resources/allocation_results.json`
   - Visualization charts are automatically generated via Python scripts

### Input Data

- **Room Data**: Define available rooms in `src/main/resources/rooms.csv`
  - Format: `Room Name, Capacity, Room Type`
  - Example: `Amphi A, 150, AMPHITHEATER`

### Output

- **JSON Results**: Detailed allocation data including:

  - Simulation parameters
  - Statistical summaries for each strategy
  - Final room assignments
  - Unallocated courses
- **Visualizations**:

  - Allocation performance charts
  - Campus maps with room assignments
  - Comparative strategy analysis

## Project Structure

```
room-allocation/
├── src/
│   └── main/
│       ├── java/com/roomallocation/
│       │   ├── Main.java                          # Application entry point
│       │   ├── allocation/
│       │   │   ├── AllocationStep.java           # Step recording for allocation process
│       │   │   └── TypeBasedAllocation.java      # Core allocation algorithm
│       │   ├── fitmethods/
│       │   │   └── capaFit.java                  # Capacity fitting methods
│       │   ├── model/
│       │   │   ├── Course.java                   # Course entity
│       │   │   ├── Room.java                     # Room entity
│       │   │   └── RoomType.java                 # Room type enumeration
│       │   ├── simulator/
│       │   │   └── CourseSimulator.java          # Course generation simulator
│       │   ├── statistics/
│       │   │   ├── AllocationStatistics.java     # Statistics data model
│       │   │   └── StatisticsCollector.java      # Statistics aggregation
│       │   ├── strategy/
│       │   │   ├── PreferenceGenerationStrategy.java  # Strategy interface
│       │   │   ├── SatisfactionBasedStrategy.java     # Satisfaction strategy
│       │   │   ├── SizedBasedPreferenceStrategy.java  # Size-based strategy
│       │   │   ├── SmartRandomPreferenceStrategy.java # Smart random strategy
│       │   │   ├── RandomPreferenceStrategy.java      # Random strategy
│       │   │   └── FixedPreference.java               # Fixed strategy
│       │   ├── util/
│       │   │   └── RoomDataLoader.java           # CSV data loader
│       │   └── visualization/
│       │       └── PythonVisualizer.java         # Python integration
│       └── resources/
│           ├── rooms.csv                          # Room data
│           ├── allocation_results.json            # Output results
│           ├── visualize.py                       # Visualization script
│           └── visualize_map.py                   # Map visualization script
├── pom.xml                                        # Maven configuration
└── target/                                        # Compiled classes and JAR
```

## Configuration

### Room Types

The system supports various room types defined in `RoomType.java`:

- `AMPHITHEATER`: Large lecture halls
- `CLASSROOM`: Standard classrooms
- `COMPUTER_LAB`: Computer labs
- `SEMINAR_ROOM`: Small seminar rooms
- (Add other types as defined in your enum)

### Preference Strategies

Different strategies can be tested by adding them to the `StatisticsCollector`:

```java
collector.addStrategy(new SatisfactionBasedStrategy(numPreferences, rooms));
collector.addStrategy(new SmartRandomPreferenceStrategy(numPreferences, rooms));
collector.addStrategy(new SizedBasedPreferenceStrategy(numPreferences, rooms));
```

## Algorithms & Strategies

### Type-Based Deferred Acceptance

The core allocation algorithm implements a type-based deferred acceptance mechanism:

1. Courses propose to their preferred room types in order of preference
2. Rooms tentatively accept the best-fitting course
3. Rejected courses propose to their next preference
4. Process continues until all courses are assigned or exhaust their preferences

### Capacity Fitting

The `capaFit` method evaluates how well a course fits in a room, optimizing for:

- Minimizing wasted capacity
- Ensuring sufficient space for all students
- Balancing utilization across room types

## Visualization

The system includes Python-based visualization tools:

- **Performance Charts**: Compare allocation success rates across strategies
- **Campus Maps**: Visual representation of room assignments
- **Statistical Plots**: Analyze allocation efficiency and satisfaction metrics

Visualizations are automatically generated after running simulations.

## License

**Copyright © 2025. All Rights Reserved.**

### Usage Restrictions

This software and associated documentation files (the "Software") are proprietary.

**IMPORTANT NOTICE:**

1. **Prior Authorization Required**: Any use, reproduction, modification, distribution, or any other exploitation of this Software requires explicit written permission from the copyright holder.
2. **Commercial Use Prohibited**: Commercial use of this Software is strictly forbidden without a separate commercial license agreement.
3. **No Warranty**: The Software is provided "AS IS", without warranty of any kind, express or implied, including but not limited to the warranties of merchantability, fitness for a particular purpose, and noninfringement.
4. **Contact Requirement**: Before using this Software in any capacity, you must contact the author for authorization.

### Contact for Licensing

For permission requests, licensing inquiries, academic use or any questions regarding the use of this Software, please contact:

**Email**: mam2684@columbia.edu
**Subject**: Classroom Allocation System - License Request

Unauthorized use of this Software may result in legal action.

---

*This project was developed as part of advanced coursework in Market Design, focusing on practical applications of matching theory and algorithmic game theory.*
