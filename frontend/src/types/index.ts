// Room types matching the Java backend RoomType enum
export enum RoomType {
  COULOIR_VANNEAU = 'COULOIR_VANNEAU',
  COULOIR_SCOLARITE = 'COULOIR_SCOLARITE',
  COULOIR_LABOS = 'COULOIR_LABOS',
  SALLES_100 = 'SALLES_100',
  AMPHI_COULOIR_BINETS = 'AMPHI_COULOIR_BINETS',
  SALLES_INFO = 'SALLES_INFO',
  SALLES_LANGUES = 'SALLES_LANGUES',
  NOUVEAUX_AMPHIS = 'NOUVEAUX_AMPHIS',
  GRANDS_AMPHIS = 'GRANDS_AMPHIS',
  AMPHIS_80_100 = 'AMPHIS_80_100',
}

// Helper function to format room type names for display
export const formatRoomType = (roomType: RoomType): string => {
  const formatted = roomType
    .replace(/_/g, ' ')
    .toLowerCase()
    .split(' ')
    .map(word => word.charAt(0).toUpperCase() + word.slice(1))
    .join(' ');
  return formatted;
};

// Days of the week
export enum DayOfWeek {
  MONDAY = 'MONDAY',
  TUESDAY = 'TUESDAY',
  WEDNESDAY = 'WEDNESDAY',
  THURSDAY = 'THURSDAY',
  FRIDAY = 'FRIDAY',
  SATURDAY = 'SATURDAY',
  SUNDAY = 'SUNDAY',
}

// Time period for availability
export interface AvailabilityPeriod {
  startTime: string; // HH:mm format
  endTime: string; // HH:mm format
}

// Weekly availability map
export type WeeklyAvailability = {
  [key in DayOfWeek]: AvailabilityPeriod[];
};

// Time slot blocker (for unavailable times)
export interface TimeBlocker {
  day: DayOfWeek;
  startTime: string; // HH:mm format
  endTime: string; // HH:mm format
}

// Course interface matching Java Course class
export interface Course {
  id?: string;
  name: string;
  cohortSize: number;
  typePreferences: RoomType[];
  assignedRoom?: string;
  durationMinutes: number; // 60, 90, 120, 180, or 200
  professorId: string;
}

// Professor interface matching Java Professor class
export interface Professor {
  id: string;
  name: string;
  availability: WeeklyAvailability;
  courses?: Course[];
}

// Preference ranking mode
export type PreferenceMode = 'per-course' | 'all-courses';

// Professor input form data
export interface ProfessorFormData {
  professorId: string;
  preferenceMode: PreferenceMode;
  // For 'all-courses' mode: single ranked list of room types
  globalRoomPreferences?: RoomType[];
  // For 'per-course' mode: map of course ID to ranked room types
  courseRoomPreferences?: Map<string, RoomType[]>;
  // Time blockers (unavailable times)
  unavailableSlots: TimeBlocker[];
}

// Room type detailed information
export interface RoomTypeInfo {
  type: RoomType;
  name: string;
  building: string;
  seatRange: {
    min: number;
    max: number;
  };
  roomCount: number;
  amenities: string[];
  images: string[];
}

// Individual room information
export interface Room {
  name: string;
  capacity: number;
  type: RoomType;
  building: string;
}

// API response types
export interface ApiResponse<T> {
  success: boolean;
  data?: T;
  error?: string;
}
