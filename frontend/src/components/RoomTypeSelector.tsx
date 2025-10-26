import React, { useState } from 'react';
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
import { RoomType, formatRoomType } from '../types';

interface RoomTypeSelectorProps {
  selectedTypes: RoomType[];
  onSelectedTypesChange: (types: RoomType[]) => void;
  minSelection?: number;
}

// All available room types
const ALL_ROOM_TYPES = Object.values(RoomType);

interface SortableItemProps {
  roomType: RoomType;
  rank: number;
  onRemove: () => void;
}

const SortableItem: React.FC<SortableItemProps> = ({ roomType, rank, onRemove }) => {
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
          <span className="font-semibold text-blue-600 text-sm">#{rank}</span>
          <span className="text-gray-800">{formatRoomType(roomType)}</span>
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
}) => {
  const [activeId, setActiveId] = useState<RoomType | null>(null);
  const [searchTerm, setSearchTerm] = useState('');

  const sensors = useSensors(
    useSensor(PointerSensor, {
      activationConstraint: {
        distance: 8,
      },
    })
  );

  // Available room types that haven't been selected yet
  const availableTypes = ALL_ROOM_TYPES.filter(
    (type) => !selectedTypes.includes(type)
  );

  // Filter available types by search term
  const filteredAvailableTypes = availableTypes.filter((type) =>
    formatRoomType(type).toLowerCase().includes(searchTerm.toLowerCase())
  );

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

  const handleAddType = (type: RoomType) => {
    onSelectedTypesChange([...selectedTypes, type]);
  };

  const handleRemoveType = (type: RoomType) => {
    onSelectedTypesChange(selectedTypes.filter((t) => t !== type));
  };

  const isMinimumMet = selectedTypes.length >= minSelection;

  return (
    <div className="w-full">
      <div className="flex gap-6">
        {/* Available Room Types (Left/Center) */}
        <div className="flex-1">
          <h3 className="text-lg font-semibold mb-3 text-gray-700">
            Available Room Types
          </h3>

          {/* Search Bar */}
          <div className="mb-4">
            <input
              type="text"
              placeholder="Search room types..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>

          {/* Room Type Cards */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-3 max-h-96 overflow-y-auto pr-2">
            {filteredAvailableTypes.length > 0 ? (
              filteredAvailableTypes.map((type) => (
                <div
                  key={type}
                  className="flex items-center justify-between bg-gray-50 border border-gray-200 rounded-lg p-3 hover:bg-gray-100 transition-colors"
                >
                  <span className="text-gray-700 text-sm">
                    {formatRoomType(type)}
                  </span>
                  <button
                    onClick={() => handleAddType(type)}
                    className="text-green-600 hover:text-green-700 p-1"
                    aria-label="Add to preferences"
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
                        d="M12 6v6m0 0v6m0-6h6m-6 0H6"
                      />
                    </svg>
                  </button>
                </div>
              ))
            ) : (
              <p className="text-gray-500 text-sm col-span-2 text-center py-4">
                {searchTerm
                  ? 'No room types match your search'
                  : 'All room types have been selected'}
              </p>
            )}
          </div>
        </div>

        {/* Selected Preferences (Right) */}
        <div className="w-96">
          <div className="sticky top-0">
            <h3 className="text-lg font-semibold mb-3 text-gray-700">
              Your Preferences
              <span
                className={`text-sm ml-2 ${
                  isMinimumMet ? 'text-green-600' : 'text-red-600'
                }`}
              >
                ({selectedTypes.length}/{minSelection} minimum)
              </span>
            </h3>

            {selectedTypes.length === 0 ? (
              <div className="bg-gray-50 border-2 border-dashed border-gray-300 rounded-lg p-6 text-center">
                <p className="text-gray-500 text-sm">
                  Add room types from the list to rank your preferences
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
                    {selectedTypes.map((type, index) => (
                      <SortableItem
                        key={type}
                        roomType={type}
                        rank={index + 1}
                        onRemove={() => handleRemoveType(type)}
                      />
                    ))}
                  </div>
                </SortableContext>

                <DragOverlay>
                  {activeId ? (
                    <div className="bg-white border border-gray-300 rounded-lg p-3 shadow-lg opacity-90">
                      <span className="text-gray-800">
                        {formatRoomType(activeId)}
                      </span>
                    </div>
                  ) : null}
                </DragOverlay>
              </DndContext>
            )}

            {!isMinimumMet && selectedTypes.length > 0 && (
              <p className="mt-3 text-sm text-red-600">
                Please select at least {minSelection} room types
              </p>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};
