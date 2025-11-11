import {
  Professor,
  Course,
  ProfessorFormData,
  ApiResponse,
  DayOfWeek,
  WeeklyAvailability,
  AllocationResult,
  AllocationStatus,
  RoomType,
} from '../types';

// Mock data for development
const mockProfessor: Professor = {
  id: 'PROF001',
  name: 'Dr. Jean Dupont',
  availability: generateDefaultAvailability(),
  courses: [
    {
      id: 'COURSE001',
      name: 'Advanced Algorithms',
      cohortSize: 45,
      typePreferences: [],
      durationMinutes: 90,
      professorId: 'PROF001',
    },
    {
      id: 'COURSE002',
      name: 'Data Structures',
      cohortSize: 60,
      typePreferences: [],
      durationMinutes: 120,
      professorId: 'PROF001',
    },
    {
      id: 'COURSE003',
      name: 'Machine Learning Fundamentals',
      cohortSize: 30,
      typePreferences: [],
      durationMinutes: 90,
      professorId: 'PROF001',
    },
  ],
};

// Generate default availability (Mon-Fri, 8am-8pm)
function generateDefaultAvailability(): WeeklyAvailability {
  const workDays = [
    DayOfWeek.MONDAY,
    DayOfWeek.TUESDAY,
    DayOfWeek.WEDNESDAY,
    DayOfWeek.THURSDAY,
    DayOfWeek.FRIDAY,
  ];

  const availability: Partial<WeeklyAvailability> = {};

  // Set work days to 8am-8pm
  workDays.forEach((day) => {
    availability[day] = [
      {
        startTime: '08:00',
        endTime: '20:00',
      },
    ];
  });

  // Weekend unavailable
  availability[DayOfWeek.SATURDAY] = [];
  availability[DayOfWeek.SUNDAY] = [];

  return availability as WeeklyAvailability;
}

// Mock allocation results - PENDING state
const mockAllocationResultPending: AllocationResult = {
  professorId: 'PROF001',
  professorName: 'Dr. Jean Dupont',
  status: AllocationStatus.PENDING,
  allocatedClasses: [],
  unallocatedCourses: [],
  // Estimated publish date - 10 days from now
  estimatedPublishDate: new Date(Date.now() + 10 * 24 * 60 * 60 * 1000).toISOString(),
};

// Mock allocation results - COMPLETED state
const mockAllocationResultCompleted: AllocationResult = {
  professorId: 'PROF001',
  professorName: 'Dr. Jean Dupont',
  status: AllocationStatus.COMPLETED,
  allocationDate: new Date().toISOString(),
  allocatedClasses: [
    {
      courseId: 'COURSE001',
      courseName: 'Advanced Algorithms',
      cohortSize: 45,
      durationMinutes: 90,
      room: {
        name: 'Amphi Arago',
        capacity: 80,
        type: RoomType.AMPHIS_80_100,
        building: 'Building A',
      },
      timeSlot: {
        day: DayOfWeek.MONDAY,
        startTime: '10:00',
        endTime: '11:30',
      },
    },
    {
      courseId: 'COURSE002',
      courseName: 'Data Structures',
      cohortSize: 60,
      durationMinutes: 120,
      room: {
        name: 'Amphi Becquerel',
        capacity: 100,
        type: RoomType.AMPHIS_80_100,
        building: 'Building B',
      },
      timeSlot: {
        day: DayOfWeek.WEDNESDAY,
        startTime: '14:00',
        endTime: '16:00',
      },
    },
    {
      courseId: 'COURSE003',
      courseName: 'Machine Learning Fundamentals',
      cohortSize: 30,
      durationMinutes: 90,
      room: {
        name: 'Salle Info 204',
        capacity: 35,
        type: RoomType.SALLES_INFO,
        building: 'Computer Science Building',
      },
      timeSlot: {
        day: DayOfWeek.FRIDAY,
        startTime: '09:00',
        endTime: '10:30',
      },
    },
  ],
  unallocatedCourses: [],
};

/**
 * TOGGLE FOR TESTING ALLOCATION STATES
 *
 * Set USE_PENDING_STATE to control what allocation status is returned:
 * - true: Shows "Allocation Pending" message with estimated publish date
 * - false: Shows completed allocation with scheduled classes
 *
 * In production, the backend will determine the actual allocation status:
 * - PENDING: Algorithm hasn't been run yet (shows waiting message)
 * - IN_PROGRESS: Algorithm is currently running (shows spinner)
 * - COMPLETED: Allocation is done (shows schedule results)
 */
const USE_PENDING_STATE = false; // Set to false to see completed allocation
const mockAllocationResult = USE_PENDING_STATE
  ? mockAllocationResultPending
  : mockAllocationResultCompleted;

// Mock API service
class ApiService {
  private useMock = false; // Set to false when backend is ready

  /**
   * Fetch professor data by ID
   */
  async getProfessorById(professorId: string): Promise<ApiResponse<Professor>> {
    if (this.useMock) {
      // Simulate network delay
      await this.delay(500);

      return {
        success: true,
        data: mockProfessor,
      };
    }

    try {
      const response = await fetch(`/api/professors/${professorId}`);

      // Check if response is JSON
      const contentType = response.headers.get('content-type');
      if (!contentType || !contentType.includes('application/json')) {
        return {
          success: false,
          error: 'Server returned non-JSON response. Is the backend running?',
        };
      }

      const data = await response.json();

      return {
        success: response.ok,
        data: response.ok ? data : undefined,
        error: response.ok ? undefined : 'Failed to fetch professor data',
      };
    } catch (error) {
      return {
        success: false,
        error: error instanceof Error ? error.message : 'Unknown error',
      };
    }
  }

  /**
   * Fetch courses taught by a professor
   */
  async getCoursesByProfessor(professorId: string): Promise<ApiResponse<Course[]>> {
    if (this.useMock) {
      await this.delay(500);

      return {
        success: true,
        data: mockProfessor.courses || [],
      };
    }

    try {
      const response = await fetch(`/api/professors/${professorId}/courses`);

      // Check if response is JSON
      const contentType = response.headers.get('content-type');
      if (!contentType || !contentType.includes('application/json')) {
        return {
          success: false,
          error: 'Server returned non-JSON response. Is the backend running?',
        };
      }

      const data = await response.json();

      return {
        success: response.ok,
        data: response.ok ? data : undefined,
        error: response.ok ? undefined : 'Failed to fetch courses',
      };
    } catch (error) {
      return {
        success: false,
        error: error instanceof Error ? error.message : 'Unknown error',
      };
    }
  }

  /**
   * Submit professor preferences and availability
   */
  async submitProfessorPreferences(
    formData: ProfessorFormData
  ): Promise<ApiResponse<void>> {
    if (this.useMock) {
      await this.delay(1000);

      // Log the submitted data for debugging
      console.log('Mock API: Submitting professor preferences:', formData);

      return {
        success: true,
      };
    }

    try {
      const response = await fetch('/api/professors/preferences', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          ...formData,
          // Convert Map to object for JSON serialization
          courseRoomPreferences: formData.courseRoomPreferences
            ? Object.fromEntries(formData.courseRoomPreferences)
            : undefined,
        }),
      });

      return {
        success: response.ok,
        error: response.ok ? undefined : 'Failed to submit preferences',
      };
    } catch (error) {
      return {
        success: false,
        error: error instanceof Error ? error.message : 'Unknown error',
      };
    }
  }

  /**
   * Update professor availability
   */
  async updateProfessorAvailability(
    professorId: string,
    availability: WeeklyAvailability
  ): Promise<ApiResponse<void>> {
    if (this.useMock) {
      await this.delay(500);

      console.log('Mock API: Updating professor availability:', {
        professorId,
        availability,
      });

      return {
        success: true,
      };
    }

    try {
      const response = await fetch(`/api/professors/${professorId}/availability`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(availability),
      });

      return {
        success: response.ok,
        error: response.ok ? undefined : 'Failed to update availability',
      };
    } catch (error) {
      return {
        success: false,
        error: error instanceof Error ? error.message : 'Unknown error',
      };
    }
  }

  /**
   * Get allocation results for a professor
   */
  async getAllocationResults(
    professorId: string
  ): Promise<ApiResponse<AllocationResult>> {
    if (this.useMock) {
      await this.delay(500);

      return {
        success: true,
        data: mockAllocationResult,
      };
    }

    try {
      const response = await fetch(`/api/professors/${professorId}/allocation`);

      // Check if response is JSON
      const contentType = response.headers.get('content-type');
      if (!contentType || !contentType.includes('application/json')) {
        return {
          success: false,
          error: 'Server returned non-JSON response. Is the backend running?',
        };
      }

      const data = await response.json();

      return {
        success: response.ok,
        data: response.ok ? data : undefined,
        error: response.ok ? undefined : 'Failed to fetch allocation results',
      };
    } catch (error) {
      return {
        success: false,
        error: error instanceof Error ? error.message : 'Unknown error',
      };
    }
  }

  /**
   * Get admin statistics
   */
  async getAdminStatistics(): Promise<ApiResponse<any>> {
    if (this.useMock) {
      await this.delay(500);

      return {
        success: true,
        data: {
          totalProfessors: 20,
          totalCourses: 70,
          totalRooms: 150,
          preferenceStatistics: {
            professorsWithAllPreferences: 12,
            professorsWithNoPreferences: 5,
            professorsWithPartialPreferences: 3,
            professorsWithNoCourses: 0,
            totalCoursesWithPreferences: 52,
            totalCoursesWithoutPreferences: 18,
          },
          roomsByType: {
            COULOIR_VANNEAU: 15,
            COULOIR_SCOLARITE: 20,
            SALLES_100: 25,
            GRANDS_AMPHIS: 8,
            NOUVEAUX_AMPHIS: 10,
            SALLES_INFO: 12,
            SALLES_LANGUES: 8,
          },
          courseStatistics: {
            assignedCourses: 0,
            unassignedCourses: 70,
            totalStudents: 5000,
            averageCohortSize: 71,
            minCohortSize: 10,
            maxCohortSize: 200,
          },
        },
      };
    }

    try {
      const response = await fetch('/api/admin/statistics');

      // Check if response is JSON
      const contentType = response.headers.get('content-type');
      if (!contentType || !contentType.includes('application/json')) {
        return {
          success: false,
          error: 'Server returned non-JSON response. Is the backend running?',
        };
      }

      const data = await response.json();

      return {
        success: response.ok,
        data: response.ok ? data : undefined,
        error: response.ok ? undefined : 'Failed to fetch statistics',
      };
    } catch (error) {
      return {
        success: false,
        error: error instanceof Error ? error.message : 'Unknown error',
      };
    }
  }

  /**
   * Get professor preference completion status
   */
  async getPreferenceStatus(): Promise<ApiResponse<any>> {
    if (this.useMock) {
      await this.delay(500);

      return {
        success: true,
        data: {
          professorsWithAllPreferences: 12,
          professorsWithNoPreferences: 5,
          professorsWithPartialPreferences: 3,
          professorDetails: [
            {
              professorId: 'PROF001',
              professorName: 'Dr. Jean Dupont',
              totalCourses: 3,
              coursesWithPreferences: 3,
              coursesWithoutPreferences: 0,
            },
            {
              professorId: 'PROF002',
              professorName: 'Dr. Marie Curie',
              totalCourses: 2,
              coursesWithPreferences: 0,
              coursesWithoutPreferences: 2,
            },
          ],
        },
      };
    }

    try {
      const response = await fetch('/api/admin/preferences/status');
      const data = await response.json();

      return {
        success: response.ok,
        data: response.ok ? data : undefined,
        error: response.ok ? undefined : 'Failed to fetch preference status',
      };
    } catch (error) {
      return {
        success: false,
        error: error instanceof Error ? error.message : 'Unknown error',
      };
    }
  }

  /**
   * Run allocation algorithm with parameters
   */
  async runAllocation(params: {
    strategy: string;
    numPreferences: number;
    useExistingCourses: boolean;
    completePreferences: boolean;
    numCourses?: number;
    minSize?: number;
    maxSize?: number;
    changeSize?: number;
  }): Promise<ApiResponse<any>> {
    if (this.useMock) {
      await this.delay(2000);

      return {
        success: true,
        data: {
          success: true,
          message: 'Algorithm started',
        },
      };
    }

    try {
      const response = await fetch('/api/admin/algorithm/run', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(params),
      });

      // Check if response is JSON
      const contentType = response.headers.get('content-type');
      if (!contentType || !contentType.includes('application/json')) {
        return {
          success: false,
          error: 'Server returned non-JSON response. Is the backend running?',
        };
      }

      const data = await response.json();

      return {
        success: response.ok,
        data: response.ok ? data : undefined,
        error: response.ok ? undefined : 'Failed to start algorithm',
      };
    } catch (error) {
      return {
        success: false,
        error: error instanceof Error ? error.message : 'Unknown error',
      };
    }
  }

  /**
   * Get allocation algorithm status
   */
  async getAlgorithmStatus(): Promise<ApiResponse<any>> {
    if (this.useMock) {
      await this.delay(300);

      return {
        success: true,
        data: {
          isRunning: false,
          lastResult: {
            success: true,
            totalCourses: 70,
            assignedCourses: 68,
            unassignedCourses: 2,
            firstChoiceCount: 45,
            topThreeChoiceCount: 62,
            averageChoiceRank: 1.8,
            allocationRate: 0.97,
            timestamp: Date.now(),
          },
        },
      };
    }

    try {
      const response = await fetch('/api/admin/algorithm/status');

      // Check if response is JSON
      const contentType = response.headers.get('content-type');
      if (!contentType || !contentType.includes('application/json')) {
        return {
          success: false,
          error: 'Server returned non-JSON response. Is the backend running?',
        };
      }

      const data = await response.json();

      return {
        success: response.ok,
        data: response.ok ? data : undefined,
        error: response.ok ? undefined : 'Failed to fetch algorithm status',
      };
    } catch (error) {
      return {
        success: false,
        error: error instanceof Error ? error.message : 'Unknown error',
      };
    }
  }

  /**
   * Utility: simulate network delay
   */
  private delay(ms: number): Promise<void> {
    return new Promise((resolve) => setTimeout(resolve, ms));
  }

  /**
   * Toggle between mock and real API
   */
  setUseMock(useMock: boolean): void {
    this.useMock = useMock;
  }
}

export const apiService = new ApiService();
