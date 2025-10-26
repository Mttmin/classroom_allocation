import {
  Professor,
  Course,
  ProfessorFormData,
  ApiResponse,
  DayOfWeek,
  WeeklyAvailability,
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

// Mock API service
class ApiService {
  private useMock = true; // Set to false when backend is ready

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
