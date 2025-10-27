import { RoomType, RoomTypeInfo, Room } from '../types';

// Mock room data aggregated from the CSV
// In production, this would come from a backend API endpoint
const mockRoomData: RoomTypeInfo[] = [
  {
    type: RoomType.COULOIR_VANNEAU,
    name: 'Couloir Cour Vanneau',
    building: 'Main',
    seatRange: { min: 21, max: 28 },
    roomCount: 15,
    amenities: [
      'Standard classroom setup',
      'Whiteboard',
      'Projector',
      'Natural lighting'
    ],
    images: [
      'https://via.placeholder.com/800x600/4A90A4/FFFFFF?text=Couloir+Vanneau+Room+1',
      'https://via.placeholder.com/800x600/5A9AB4/FFFFFF?text=Couloir+Vanneau+Room+2',
      'https://via.placeholder.com/800x600/6AAAC4/FFFFFF?text=Couloir+Vanneau+Room+3'
    ]
  },
  {
    type: RoomType.COULOIR_SCOLARITE,
    name: 'Couloir de la Scolarité',
    building: 'Main',
    seatRange: { min: 24, max: 35 },
    roomCount: 9,
    amenities: [
      'Medium-sized classrooms',
      'Whiteboard',
      'Projector',
      'Air conditioning'
    ],
    images: [
      'https://via.placeholder.com/800x600/4A90A4/FFFFFF?text=Couloir+Scolarite+1',
      'https://via.placeholder.com/800x600/5A9AB4/FFFFFF?text=Couloir+Scolarite+2'
    ]
  },
  {
    type: RoomType.COULOIR_LABOS,
    name: 'Couloir des Labos',
    building: 'Main',
    seatRange: { min: 28, max: 49 },
    roomCount: 7,
    amenities: [
      'Laboratory setup',
      'Whiteboard',
      'Projector',
      'Lab equipment',
      'Safety equipment'
    ],
    images: [
      'https://via.placeholder.com/800x600/4A90A4/FFFFFF?text=Lab+Room+1',
      'https://via.placeholder.com/800x600/5A9AB4/FFFFFF?text=Lab+Room+2',
      'https://via.placeholder.com/800x600/6AAAC4/FFFFFF?text=Lab+Room+3'
    ]
  },
  {
    type: RoomType.SALLES_100,
    name: 'Salles 100',
    building: 'Main',
    seatRange: { min: 16, max: 42 },
    roomCount: 7,
    amenities: [
      'Mixed capacity rooms',
      'Whiteboard',
      'Projector',
      'Flexible seating'
    ],
    images: [
      'https://via.placeholder.com/800x600/4A90A4/FFFFFF?text=Salle+100+Room'
    ]
  },
  {
    type: RoomType.AMPHI_COULOIR_BINETS,
    name: 'Amphi Couloir Binets',
    building: 'Main',
    seatRange: { min: 40, max: 48 },
    roomCount: 4,
    amenities: [
      'Amphitheatre seating',
      'Whiteboard',
      'Projector',
      'Sound system',
      'Tiered seating'
    ],
    images: [
      'https://via.placeholder.com/800x600/4A90A4/FFFFFF?text=Amphi+Binets+1',
      'https://via.placeholder.com/800x600/5A9AB4/FFFFFF?text=Amphi+Binets+2'
    ]
  },
  {
    type: RoomType.SALLES_INFO,
    name: 'Salles Info',
    building: 'Main',
    seatRange: { min: 18, max: 24 },
    roomCount: 7,
    amenities: [
      'Computer workstations',
      'Whiteboard',
      'Projector',
      'High-speed internet',
      'Software licenses'
    ],
    images: [
      'https://via.placeholder.com/800x600/4A90A4/FFFFFF?text=Computer+Lab+1',
      'https://via.placeholder.com/800x600/5A9AB4/FFFFFF?text=Computer+Lab+2'
    ]
  },
  {
    type: RoomType.SALLES_LANGUES,
    name: 'Salles Langues',
    building: 'Main',
    seatRange: { min: 12, max: 20 },
    roomCount: 12,
    amenities: [
      'Language lab equipment',
      'Whiteboard',
      'Audio system',
      'Small group setup',
      'Recording capability'
    ],
    images: [
      'https://via.placeholder.com/800x600/4A90A4/FFFFFF?text=Language+Lab'
    ]
  },
  {
    type: RoomType.NOUVEAUX_AMPHIS,
    name: 'Nouveaux Amphis',
    building: 'Main',
    seatRange: { min: 50, max: 50 },
    roomCount: 6,
    amenities: [
      'Modern amphitheatre',
      'Whiteboard',
      'Projector',
      'Sound system',
      'Recording equipment',
      'Climate control'
    ],
    images: [
      'https://via.placeholder.com/800x600/4A90A4/FFFFFF?text=New+Amphi+1',
      'https://via.placeholder.com/800x600/5A9AB4/FFFFFF?text=New+Amphi+2',
      'https://via.placeholder.com/800x600/6AAAC4/FFFFFF?text=New+Amphi+3'
    ]
  },
  {
    type: RoomType.GRANDS_AMPHIS,
    name: 'Grands Amphis',
    building: 'Main',
    seatRange: { min: 112, max: 780 },
    roomCount: 5,
    amenities: [
      'Large amphitheatre',
      'Multiple whiteboards',
      'Projector',
      'Advanced sound system',
      'Recording equipment',
      'Stage lighting',
      'Wheelchair accessible'
    ],
    images: [
      'https://via.placeholder.com/800x600/4A90A4/FFFFFF?text=Grand+Amphi+1',
      'https://via.placeholder.com/800x600/5A9AB4/FFFFFF?text=Grand+Amphi+2'
    ]
  },
  {
    type: RoomType.AMPHIS_80_100,
    name: 'Amphis 80-100 Places',
    building: 'Main',
    seatRange: { min: 81, max: 100 },
    roomCount: 4,
    amenities: [
      'Mid-size amphitheatre',
      'Whiteboard',
      'Projector',
      'Sound system',
      'Tiered seating'
    ],
    images: [
      'https://via.placeholder.com/800x600/4A90A4/FFFFFF?text=Amphi+80-100+1',
      'https://via.placeholder.com/800x600/5A9AB4/FFFFFF?text=Amphi+80-100+2'
    ]
  }
];

/**
 * Service for managing room type data
 */
class RoomService {
  private useMock: boolean = true;

  /**
   * Toggle between mock and real API
   */
  setUseMock(useMock: boolean): void {
    this.useMock = useMock;
  }

  /**
   * Get all room type information
   */
  async getAllRoomTypes(): Promise<RoomTypeInfo[]> {
    if (this.useMock) {
      // Simulate API delay
      await new Promise(resolve => setTimeout(resolve, 300));
      return mockRoomData;
    }

    // Real API call would go here
    const response = await fetch('/api/rooms/types');
    if (!response.ok) {
      throw new Error('Failed to fetch room types');
    }
    return response.json();
  }

  /**
   * Get room type information by type
   */
  async getRoomTypeInfo(type: RoomType): Promise<RoomTypeInfo | undefined> {
    const allTypes = await this.getAllRoomTypes();
    return allTypes.find(rt => rt.type === type);
  }

  /**
   * Get all rooms of a specific type
   */
  async getRoomsByType(type: RoomType): Promise<Room[]> {
    if (this.useMock) {
      // In a real implementation, this would fetch individual rooms
      await new Promise(resolve => setTimeout(resolve, 200));
      return [];
    }

    const response = await fetch(`/api/rooms/type/${type}`);
    if (!response.ok) {
      throw new Error('Failed to fetch rooms by type');
    }
    return response.json();
  }
}

export const roomService = new RoomService();
