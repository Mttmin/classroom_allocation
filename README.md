# Classroom Allocation System

A sophisticated full-stack application for optimizing classroom and room allocations using market design principles, deferred acceptance algorithms, and intelligent scheduling. The system combines a powerful Java backend with a modern React frontend to provide an intuitive interface for managing course scheduling and room assignments.

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Technology Stack](#technology-stack)
- [System Requirements](#system-requirements)
- [Installation](#installation)
- [Usage](#usage)
- [Project Structure](#project-structure)
- [API Documentation](#api-documentation)
- [Algorithms](#algorithms)
- [Configuration](#configuration)
- [Development](#development)
- [License](#license)

## Overview

This project implements a comprehensive room allocation and scheduling system based on market design theory, specifically utilizing type-based deferred acceptance algorithms. The system provides both algorithmic optimization and an interactive user interface for managing classroom allocations at educational institutions.

### Key Capabilities

- Generate and manage realistic course cohorts with variable sizes
- Run multiple allocation scenarios with different preference strategies
- Optimize room assignments based on capacity fit and preferences
- Handle scheduling constraints including professor availability and room conflicts
- Visualize results through interactive dashboards
- Export allocation data and statistics in JSON format
- RESTful API for integration with other systems

## Features

### Backend Features

- **Type-Based Allocation Algorithm**: Implements a deferred acceptance mechanism for stable matching between courses and rooms
- **Multiple Preference Strategies**:
  - Satisfaction-based strategy
  - Size-based preference strategy
  - Smart random preference strategy
  - Fixed preference strategy
  - Random preference strategy
- **Constraint Management**: Handles professor availability, room capacity, and correlation constraints
- **Statistical Analysis**: Comprehensive statistics collection across multiple simulation runs
- **RESTful API**: Full API for room types, courses, professors, and allocation management
- **Data Import/Export**: CSV import for rooms/courses and JSON export for results

### Frontend Features

- **Interactive Dashboard**: Modern React-based user interface
- **Drag-and-Drop Interface**: Intuitive course and room management using @dnd-kit
- **Real-time Visualization**: View allocation results and statistics
- **Admin Controls**: Manage courses, rooms, professors, and preferences
- **Algorithm Selection**: Choose and configure different allocation strategies
- **Responsive Design**: Built with TailwindCSS for mobile-friendly experience

## Technology Stack

### Backend

- **Java 24**: Core application logic
- **Maven**: Build and dependency management
- **Jackson**: JSON serialization/deserialization
- **JUnit 5**: Testing framework
- **HttpServer**: Lightweight API server

### Frontend

- **React 19**: UI framework
- **TypeScript**: Type-safe development
- **Vite**: Fast build tool and development server
- **TailwindCSS**: Utility-first CSS framework
- **React Router**: Client-side routing
- **@dnd-kit**: Drag-and-drop functionality

## System Requirements

- **Java Development Kit (JDK)**: Version 24 or higher
- **Maven**: Version 3.6 or higher
- **Node.js**: Version 18 or higher
- **npm**: Version 9 or higher
- **Python** (optional): For data visualization scripts

## Project Structure

```
classroom_allocation/
├── frontend/                           # React frontend application
│   ├── src/
│   │   ├── components/                 # React components
│   │   ├── pages/                      # Page components
│   │   ├── App.tsx                     # Main app component
│   │   └── main.tsx                    # Entry point
│   ├── public/                         # Static assets
│   ├── package.json                    # Node dependencies
│   └── vite.config.ts                  # Vite configuration
│
├── src/main/
│   ├── java/com/roomallocation/
│   │   ├── server/
│   │   │   └── ApiServer.java          # Main API server
│   │   ├── controller/
│   │   │   ├── AdminController.java    # Admin endpoints
│   │   │   ├── AlgorithmController.java # Algorithm endpoints
│   │   │   └── RoomTypeController.java # Room type endpoints
│   │   ├── service/
│   │   │   ├── AdminService.java       # Business logic
│   │   │   └── RoomTypeService.java    # Room type service
│   │   ├── model/
│   │   │   ├── Course.java             # Course entity
│   │   │   ├── Room.java               # Room entity
│   │   │   ├── Professor.java          # Professor entity
│   │   │   ├── Schedule.java           # Schedule entity
│   │   │   └── RoomType.java           # Room type enum
│   │   ├── allocation/
│   │   │   ├── TypeBasedAllocation.java # Core allocation algorithm
│   │   │   └── AllocationStep.java     # Allocation step tracking
│   │   ├── scheduler/
│   │   │   ├── optimizer/
│   │   │   │   ├── Scheduler.java      # Scheduler interface
│   │   │   │   └── NaiveScheduler.java # Basic scheduler
│   │   │   └── scoring/
│   │   │       └── Scoring.java        # Scoring logic
│   │   ├── strategy/
│   │   │   ├── PreferenceGenerationStrategy.java
│   │   │   ├── SatisfactionBasedStrategy.java
│   │   │   ├── SizedBasedPreferenceStrategy.java
│   │   │   ├── SmartRandomPreferenceStrategy.java
│   │   │   ├── RandomPreferenceStrategy.java
│   │   │   └── FixedPreference.java
│   │   ├── constraint/
│   │   │   ├── ConstraintValidator.java
│   │   │   ├── ProfessorConstraint.java
│   │   │   ├── RoomConstraint.java
│   │   │   └── CorrelationConstraint.java
│   │   ├── statistics/
│   │   │   ├── StatisticsCollector.java
│   │   │   └── AllocationStatistics.java
│   │   ├── util/
│   │   │   ├── RoomDataLoader.java
│   │   │   ├── CourseDataLoader.java
│   │   │   └── ProfessorDataLoader.java
│   │   └── Main.java                   # CLI entry point
│   └── resources/
│       ├── rooms.csv                    # Room data
│       ├── allocation_results.json      # Output results
│       └── static/                      # Frontend build output
│
├── pom.xml                              # Maven configuration
└── LICENSE                              # License file
```

## Algorithms

### Type-Based Deferred Acceptance

The core allocation algorithm implements a type-based deferred acceptance mechanism:

1. **Proposal Phase**: Courses propose to their preferred room types in order of preference
2. **Tentative Acceptance**: Rooms tentatively accept the best-fitting course based on capacity
3. **Rejection**: Rejected courses propose to their next preference
4. **Iteration**: Process continues until all courses are assigned or exhaust preferences
5. **Stability**: The final matching is stable with no blocking pairs

### Capacity Fitting

The `capaFit` method evaluates how well a course fits in a room:

- Minimizes wasted capacity
- Ensures sufficient space for all students
- Balances utilization across room types
- Considers buffer space for flexibility

### Preference Strategies

Different strategies generate course preferences:

- **Satisfaction-Based**: Prioritizes room types that maximize user satisfaction
- **Size-Based**: Matches courses to rooms based on size compatibility
- **Smart Random**: Uses randomization with intelligent constraints
- **Fixed**: Uses predetermined preferences
- **Random**: Pure random preference generation

## License

Copyright (c) 2025. All Rights Reserved.

This software is proprietary and confidential. Unauthorized copying, distribution, or use of this software is strictly prohibited. See the [LICENSE](LICENSE) file for details.

For licensing inquiries, please contact: mam2684@columbia.edu

---

Developed as part of advanced coursework in Market Design and Algorithmic Game Theory at Ecole Polytechnique.
