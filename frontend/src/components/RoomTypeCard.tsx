import React, { useState } from 'react';

export interface RoomTypeData {
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

interface RoomTypeCardProps {
  roomType: RoomTypeData;
  onSelect?: () => void;
  isSelected?: boolean;
}

export const RoomTypeCard: React.FC<RoomTypeCardProps> = ({
  roomType,
  onSelect,
}) => {
  const [currentImageIndex, setCurrentImageIndex] = useState(0);

  const nextImage = () => {
    setCurrentImageIndex((prev) => (prev + 1) % roomType.images.length);
  };

  const prevImage = () => {
    setCurrentImageIndex(
      (prev) => (prev - 1 + roomType.images.length) % roomType.images.length
    );
  };

  return (
    <div
      className="bg-white rounded-lg shadow-md overflow-hidden transition-all hover:shadow-lg border border-gray-200"
    >
      {/* Header Row - Equal widths */}
      <div className="grid grid-cols-[1fr_200px] gap-0">
        <div className="bg-gradient-to-r from-slate-700 to-slate-600 text-white p-4 flex items-center justify-center">
          <h3 className="text-xl font-bold text-center">{roomType.name}</h3>
        </div>
        <div className="bg-gradient-to-r from-slate-600 to-slate-700 text-white p-4 flex items-center justify-center border-l border-slate-500">
          <h3 className="text-xl font-bold text-center">{roomType.building}</h3>
        </div>
      </div>

      {/* Main Content Row */}
      <div className="grid grid-cols-[1fr_200px] gap-0">
        {/* Picture Slider */}
        <div className="bg-gradient-to-br from-slate-600 to-slate-700 text-white relative h-80">
          <div className="flex items-center justify-center h-full p-6">
            {roomType.images.length > 0 ? (
              <div className="relative w-full h-full">
                <img
                  src={roomType.images[currentImageIndex]}
                  alt={`${roomType.name} - Image ${currentImageIndex + 1}`}
                  className="w-full h-full object-cover rounded"
                />
                {roomType.images.length > 1 && (
                  <>
                    {/* Previous Button */}
                    <button
                      onClick={(e) => {
                        e.stopPropagation();
                        prevImage();
                      }}
                      className="absolute left-2 top-1/2 -translate-y-1/2 bg-black/50 hover:bg-black/70 text-white rounded-full p-2 transition-colors"
                      aria-label="Previous image"
                    >
                      <svg
                        className="w-6 h-6"
                        fill="none"
                        stroke="currentColor"
                        viewBox="0 0 24 24"
                      >
                        <path
                          strokeLinecap="round"
                          strokeLinejoin="round"
                          strokeWidth={2}
                          d="M15 19l-7-7 7-7"
                        />
                      </svg>
                    </button>
                    {/* Next Button */}
                    <button
                      onClick={(e) => {
                        e.stopPropagation();
                        nextImage();
                      }}
                      className="absolute right-2 top-1/2 -translate-y-1/2 bg-black/50 hover:bg-black/70 text-white rounded-full p-2 transition-colors"
                      aria-label="Next image"
                    >
                      <svg
                        className="w-6 h-6"
                        fill="none"
                        stroke="currentColor"
                        viewBox="0 0 24 24"
                      >
                        <path
                          strokeLinecap="round"
                          strokeLinejoin="round"
                          strokeWidth={2}
                          d="M9 5l7 7-7 7"
                        />
                      </svg>
                    </button>
                    {/* Image Counter */}
                    <div className="absolute bottom-2 left-1/2 -translate-x-1/2 bg-black/70 text-white px-3 py-1 rounded-full text-sm">
                      {currentImageIndex + 1} / {roomType.images.length}
                    </div>
                  </>
                )}
              </div>
            ) : (
              <div className="text-center text-white/70 text-2xl">Picture slider</div>
            )}
          </div>
        </div>

        {/* Info Panel */}
        <div className="flex flex-col bg-white">
          {/* Seat Range */}
          <div className="bg-gradient-to-br from-slate-600 to-slate-700 text-white p-3 border-l border-t border-slate-500">
            <div className="text-center">
              <div className="text-xs font-semibold mb-1">Seat range:</div>
              <div className="text-lg font-bold">
                {roomType.seatRange.min} - {roomType.seatRange.max}
              </div>
            </div>
          </div>

          {/* Number of Rooms Available */}
          <div className="bg-gradient-to-br from-slate-600 to-slate-700 text-white p-3 border-l border-t border-slate-500">
            <div className="text-center">
              <div className="text-xs font-semibold mb-1">Number of rooms</div>
              <div className="text-xs font-semibold">available</div>
              <div className="text-2xl font-bold mt-1">{roomType.roomCount}</div>
            </div>
          </div>

          {/* Amenities - Fixed height with scroll */}
          <div className="bg-gradient-to-br from-slate-700 to-slate-800 text-white p-3 border-l border-t border-slate-600 flex-grow overflow-hidden">
            <div className="text-xs font-semibold mb-2">Amenities:</div>
            <div className="overflow-y-auto max-h-32 pr-2 custom-scrollbar">
              <ul className="space-y-1">
                {roomType.amenities.map((amenity, index) => (
                  <li key={index} className="text-xs">
                    - {amenity}
                  </li>
                ))}
              </ul>
            </div>
          </div>
        </div>
      </div>

      {/* Add Button */}
      {onSelect && (
        <div className="bg-gray-50 border-t border-gray-200 p-3 flex justify-end">
          <button
            onClick={onSelect}
            className="flex items-center gap-2 px-4 py-2 bg-slate-700 hover:bg-slate-800 text-white rounded-lg transition-colors font-medium text-sm"
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
            Add to Preferences
          </button>
        </div>
      )}
    </div>
  );
};
