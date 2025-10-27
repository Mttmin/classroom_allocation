import React, { useState, useEffect } from 'react';
import {
  DndContext,
  DragEndEvent,
  DragOverlay,
  DragStartEvent,
  closestCenter,
  PointerSensor,
  useSensor,
  useSensors,
} from '@dnd-kit/core';
import {
  SortableContext,
  arrayMove,
  useSortable,
  verticalListSortingStrategy,
} from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import { RoomType, formatRoomType, RoomTypeInfo } from '../types';
import { RoomTypeCard } from './RoomTypeCard';
import { roomService } from '../services/roomService';

interface RoomTypeSelectorProps {
  selectedTypes: RoomType[];
  onSelectedTypesChange: (types: RoomType[]) => void;
  minSelection?: number;
  recommendedSelection?: number;
  onValidationChange?: (isValid: boolean) => void;
}

interface SortableItemProps {
  roomType: RoomType;
  roomTypeName: string;
  rank: number;
  onRemove: () => void;
}

const SortableItem: React.FC<SortableItemProps> = ({ roomType, roomTypeName, rank, onRemove }) => {
  const { attributes, listeners, setNodeRef, transform, transition, isDragging } =
    useSortable({ id: roomType });

  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
    opacity: isDragging ? 0.5 : 1,
  };

  return (
    <div
      ref={setNodeRef}
      style={style}
      className="flex items-center justify-between bg-white border border-gray-300 rounded-lg p-3 mb-2 shadow-sm hover:shadow-md transition-shadow"
    >
      <div className="flex items-center gap-3 flex-1">
        <div
          {...listeners}
          {...attributes}
          className="cursor-grab active:cursor-grabbing p-1"
        >
          <svg
            className="w-5 h-5 text-gray-400"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M4 8h16M4 16h16"
            />
          </svg>
        </div>
        <div className="flex items-center gap-2 flex-1">
          <span className="font-semibold text-slate-700 text-sm">#{rank}</span>
          <span className="text-gray-800">{roomTypeName}</span>
        </div>
      </div>
      <button
        onClick={onRemove}
        className="text-red-500 hover:text-red-700 p-1"
        aria-label="Remove"
      >
        <svg
          className="w-5 h-5"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M6 18L18 6M6 6l12 12"
          />
        </svg>
      </button>
    </div>
  );
};

export const RoomTypeSelector: React.FC<RoomTypeSelectorProps> = ({
  selectedTypes,
  onSelectedTypesChange,
  minSelection = 5,
  recommendedSelection = 10,
  onValidationChange,
}) => {
  const [roomTypeData, setRoomTypeData] = useState<RoomTypeInfo[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [activeId, setActiveId] = useState<RoomType | null>(null);

  const sensors = useSensors(
    useSensor(PointerSensor, {
      activationConstraint: {
        distance: 8,
      },
    })
  );

  // Load room type data on mount
  useEffect(() => {
    const loadRoomTypes = async () => {
      try {
        const data = await roomService.getAllRoomTypes();
        setRoomTypeData(data);
      } catch (error) {
        console.error('Failed to load room types:', error);
      } finally {
        setLoading(false);
      }
    };

    loadRoomTypes();
  }, []);

  // Notify parent about validation status whenever selection changes
  useEffect(() => {
    if (onValidationChange) {
      const isValid = selectedTypes.length >= minSelection;
      onValidationChange(isValid);
    }
  }, [selectedTypes.length, minSelection, onValidationChange]);

  // Available room types that haven't been selected yet
  const availableRoomTypes = roomTypeData.filter(
    (roomType) => !selectedTypes.includes(roomType.type)
  );

  // Filter available types by search term
  const filteredRoomTypes = availableRoomTypes.filter((roomType) =>
    roomType.name.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const handleSelectRoomType = (type: RoomType) => {
    if (!selectedTypes.includes(type)) {
      onSelectedTypesChange([...selectedTypes, type]);
    }
  };

  const handleRemoveType = (type: RoomType) => {
    onSelectedTypesChange(selectedTypes.filter((t) => t !== type));
  };

  const handleDragStart = (event: DragStartEvent) => {
    setActiveId(event.active.id as RoomType);
  };

  const handleDragEnd = (event: DragEndEvent) => {
    const { active, over } = event;

    if (over && active.id !== over.id) {
      const oldIndex = selectedTypes.indexOf(active.id as RoomType);
      const newIndex = selectedTypes.indexOf(over.id as RoomType);

      onSelectedTypesChange(arrayMove(selectedTypes, oldIndex, newIndex));
    }

    setActiveId(null);
  };

  // Determine color status based on selection count
  const getSelectionStatus = () => {
    const count = selectedTypes.length;
    if (count < minSelection) {
      return { color: 'red' };
    } else if (count < recommendedSelection) {
      return { color: 'yellow' };
    } else {
      return { color: 'green' };
    }
  };

  const selectionStatus = getSelectionStatus();

  if (loading) {
    return (
      <div className="w-full flex items-center justify-center py-12">
        <div className="text-gray-500">Loading room types...</div>
      </div>
    );
  }

  return (
    <div className="w-full">
      <div className="flex gap-6">
        {/* Available Room Types (Left) */}
        <div className="flex-1">
          <h3 className="text-lg font-semibold mb-3 text-gray-800">
            Available Classroom Types
          </h3>

          {/* Search Bar */}
          <div className="mb-4">
            <input
              type="text"
              placeholder="Search classroom types..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-slate-500"
            />
          </div>

          {/* Room Type Cards */}
          <div className="space-y-4 max-h-[calc(100vh-250px)] overflow-y-auto pr-2">
            {filteredRoomTypes.length > 0 ? (
              filteredRoomTypes.map((roomType) => (
                <RoomTypeCard
                  key={roomType.type}
                  roomType={{
                    name: roomType.name,
                    building: roomType.building,
                    seatRange: roomType.seatRange,
                    roomCount: roomType.roomCount,
                    amenities: roomType.amenities,
                    images: roomType.images,
                  }}
                  onSelect={() => handleSelectRoomType(roomType.type)}
                  isSelected={false}
                />
              ))
            ) : (
              <p className="text-gray-500 text-sm text-center py-8">
                {searchTerm
                  ? 'No classroom types match your search'
                  : 'All classroom types have been selected'}
              </p>
            )}
          </div>
        </div>

        {/* Selected Preferences (Right) */}
        <div className="w-96">
          <div className="sticky top-0">
            <div className="mb-3">
              <div className="flex items-center gap-2 mb-2">
                <h3 className="text-lg font-semibold text-gray-800">
                  Your Preferences
                </h3>
                <div
                  className={`w-4 h-4 rounded-full ${
                    selectionStatus.color === 'red'
                      ? 'bg-red-500'
                      : selectionStatus.color === 'yellow'
                        ? 'bg-yellow-500'
                        : 'bg-green-500'
                  }`}
                />
              </div>
              <div className="mt-2">
                {selectionStatus.color === 'red' && (
                  <p className="text-sm text-red-600 font-medium">
                    Please select at least {minSelection} classroom types (minimum required)
                  </p>
                )}
                {selectionStatus.color === 'yellow' && (
                  <p className="text-sm text-yellow-600 font-medium">
                    Consider adding {recommendedSelection - selectedTypes.length} more for better allocation ({recommendedSelection} recommended)
                  </p>
                )}
                {selectionStatus.color === 'green' && (
                  <p className="text-sm text-green-600 font-medium">
                    Great! You've provided {selectedTypes.length} preferences
                  </p>
                )}
              </div>
            </div>

            {selectedTypes.length === 0 ? (
              <div className="bg-gray-50 border-2 border-dashed border-gray-300 rounded-lg p-6 text-center">
                <p className="text-gray-500 text-sm">
                  Add classroom types to rank your preferences
                </p>
                <p className="text-gray-400 text-xs mt-2">
                  Drag to reorder your preferences
                </p>
              </div>
            ) : (
              <DndContext
                sensors={sensors}
                collisionDetection={closestCenter}
                onDragStart={handleDragStart}
                onDragEnd={handleDragEnd}
              >
                <SortableContext
                  items={selectedTypes}
                  strategy={verticalListSortingStrategy}
                >
                  <div className="space-y-2">
                    {selectedTypes.map((type, index) => {
                      const roomTypeInfo = roomTypeData.find((rt) => rt.type === type);
                      return (
                        <SortableItem
                          key={type}
                          roomType={type}
                          roomTypeName={roomTypeInfo?.name || formatRoomType(type)}
                          rank={index + 1}
                          onRemove={() => handleRemoveType(type)}
                        />
                      );
                    })}
                  </div>
                </SortableContext>

                <DragOverlay>
                  {activeId ? (
                    <div className="bg-white border border-gray-300 rounded-lg p-3 shadow-lg opacity-90">
                      <span className="text-gray-800">
                        {roomTypeData.find((rt) => rt.type === activeId)?.name ||
                          formatRoomType(activeId)}
                      </span>
                    </div>
                  ) : null}
                </DragOverlay>
              </DndContext>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};
