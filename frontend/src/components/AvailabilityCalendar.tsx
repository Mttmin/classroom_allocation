import React, { useState } from 'react';
import { DayOfWeek, TimeBlocker } from '../types';

interface AvailabilityCalendarProps {
  blockers: TimeBlocker[];
  onBlockersChange: (blockers: TimeBlocker[]) => void;
}

// Time slots from 8am to 8pm in 30-minute increments
const TIME_SLOTS = Array.from({ length: 24 }, (_, i) => {
  const hour = Math.floor(i / 2) + 8;
  const minute = i % 2 === 0 ? '00' : '30';
  return `${hour.toString().padStart(2, '0')}:${minute}`;
});

const DAYS = [
  DayOfWeek.MONDAY,
  DayOfWeek.TUESDAY,
  DayOfWeek.WEDNESDAY,
  DayOfWeek.THURSDAY,
  DayOfWeek.FRIDAY,
];

const formatDay = (day: DayOfWeek): string => {
  return day.charAt(0) + day.slice(1).toLowerCase();
};

interface TimeSlotCellProps {
  day: DayOfWeek;
  timeSlot: string;
  isBlocked: boolean;
  onMouseDown: () => void;
  onMouseEnter: () => void;
  onMouseUp: () => void;
}

const TimeSlotCell: React.FC<TimeSlotCellProps> = ({
  day,
  timeSlot,
  isBlocked,
  onMouseDown,
  onMouseEnter,
  onMouseUp,
}) => {
  return (
    <button
      onMouseDown={onMouseDown}
      onMouseEnter={onMouseEnter}
      onMouseUp={onMouseUp}
      className={`
        w-full h-5 border border-gray-200 transition-colors select-none
        ${
          isBlocked
            ? 'bg-red-500 hover:bg-red-600'
            : 'bg-green-100 hover:bg-green-200'
        }
      `}
      title={`${formatDay(day)} ${timeSlot} - ${isBlocked ? 'Unavailable' : 'Available'}`}
    />
  );
};

export const AvailabilityCalendar: React.FC<AvailabilityCalendarProps> = ({
  blockers,
  onBlockersChange,
}) => {
  const [isDragging, setIsDragging] = useState(false);
  const [dragMode, setDragMode] = useState<'block' | 'unblock'>('block');

  // Check if a specific time slot is blocked
  const isTimeSlotBlocked = (day: DayOfWeek, time: string): boolean => {
    const [hours, minutes] = time.split(':').map(Number);
    const timeMinutes = hours * 60 + minutes;

    return blockers.some((blocker) => {
      if (blocker.day !== day) return false;

      const [startHours, startMinutes] = blocker.startTime.split(':').map(Number);
      const [endHours, endMinutes] = blocker.endTime.split(':').map(Number);

      const startMinutes_total = startHours * 60 + startMinutes;
      const endMinutes_total = endHours * 60 + endMinutes;

      return timeMinutes >= startMinutes_total && timeMinutes < endMinutes_total;
    });
  };

  // Calculate the end time for a time slot (30 minutes later)
  const getEndTime = (startTime: string): string => {
    const [hours, minutes] = startTime.split(':').map(Number);
    const totalMinutes = hours * 60 + minutes + 30;
    const endHours = Math.floor(totalMinutes / 60);
    const endMinutes = totalMinutes % 60;
    return `${endHours.toString().padStart(2, '0')}:${endMinutes.toString().padStart(2, '0')}`;
  };

  // Toggle a time slot's blocked status
  const toggleTimeSlot = (day: DayOfWeek, time: string, forceMode?: 'block' | 'unblock') => {
    const isCurrentlyBlocked = isTimeSlotBlocked(day, time);
    const endTime = getEndTime(time);
    const shouldBlock = forceMode ? forceMode === 'block' : !isCurrentlyBlocked;

    if (!shouldBlock) {
      // Remove this blocker
      const newBlockers = blockers.filter((blocker) => {
        if (blocker.day !== day) return true;
        // Remove if this blocker covers this exact time slot
        return !(blocker.startTime === time && blocker.endTime === endTime);
      });
      onBlockersChange(newBlockers);
    } else {
      // Add a new blocker (only if not already blocked)
      if (!isCurrentlyBlocked) {
        const newBlocker: TimeBlocker = {
          day,
          startTime: time,
          endTime,
        };
        onBlockersChange([...blockers, newBlocker]);
      }
    }
  };

  // Handle mouse down on a cell
  const handleMouseDown = (day: DayOfWeek, time: string) => {
    const isCurrentlyBlocked = isTimeSlotBlocked(day, time);
    setDragMode(isCurrentlyBlocked ? 'unblock' : 'block');
    setIsDragging(true);
    toggleTimeSlot(day, time);
  };

  // Handle mouse enter on a cell while dragging
  const handleMouseEnter = (day: DayOfWeek, time: string) => {
    if (isDragging) {
      toggleTimeSlot(day, time, dragMode);
    }
  };

  // Handle mouse up
  const handleMouseUp = () => {
    setIsDragging(false);
  };

  // Add global mouse up listener
  React.useEffect(() => {
    const handleGlobalMouseUp = () => {
      setIsDragging(false);
    };
    
    document.addEventListener('mouseup', handleGlobalMouseUp);
    return () => {
      document.removeEventListener('mouseup', handleGlobalMouseUp);
    };
  }, []);

  // Clear all blockers
  const clearAllBlockers = () => {
    onBlockersChange([]);
  };

  // Block all time slots
  const blockAllSlots = () => {
    const allBlockers: TimeBlocker[] = [];
    DAYS.forEach((day) => {
      TIME_SLOTS.forEach((time) => {
        allBlockers.push({
          day,
          startTime: time,
          endTime: getEndTime(time),
        });
      });
    });
    onBlockersChange(allBlockers);
  };

  return (
    <div className="w-full">
      <div className="mb-4 flex items-center justify-between gap-4">
        <div className="flex-1 min-w-0">
          <h3 className="text-lg font-semibold text-gray-700">
            Weekly Availability
          </h3>
          <p className="text-sm text-gray-500 mt-1">
            Default: Available Mon-Fri, 8am-8pm. Click and drag to block/unblock times.
          </p>
          <p className="text-sm text-gray-500 mt-1">
            Note: The algorithm prioritizes scheduling during regular business hours and
            then tries to avoid 8:00–10:00 AM for student presence when possible. Providing
            broader availability helps it find a better overall allocation for everyone, while reducing overlap with other professors enables more students to take your class.
          </p>
        </div>
        <div className="flex gap-2 flex-shrink-0 items-center">
          <button
            onClick={clearAllBlockers}
            className="px-4 py-2 text-sm bg-green-500 text-white rounded hover:bg-green-600 transition-colors whitespace-nowrap"
          >
            Clear All Blocks
          </button>
          <button
            onClick={blockAllSlots}
            className="px-4 py-2 text-sm bg-red-500 text-white rounded hover:bg-red-600 transition-colors whitespace-nowrap"
          >
            Block All
          </button>
        </div>
      </div>

      {/* Legend */}
      <div className="mb-3 flex gap-4 text-sm">
        <div className="flex items-center gap-2">
          <div className="w-6 h-6 bg-green-100 border border-gray-200 rounded" />
          <span className="text-gray-600">Available</span>
        </div>
        <div className="flex items-center gap-2">
          <div className="w-6 h-6 bg-red-500 border border-gray-200 rounded" />
          <span className="text-gray-600">Blocked</span>
        </div>
      </div>

      {/* Calendar Grid */}
      <div className="border border-gray-300 rounded-lg overflow-hidden shadow-sm">
        <div className="overflow-x-auto">
          <table className="w-full border-collapse">
            <thead>
              <tr className="bg-gray-100">
                <th className="border border-gray-300 p-2 text-sm font-semibold text-gray-700 w-16">
                  Time
                </th>
                {DAYS.map((day) => (
                  <th
                    key={day}
                    className="border border-gray-300 p-2 text-sm font-semibold text-gray-700"
                  >
                    {formatDay(day)}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {TIME_SLOTS.map((time) => {
                const isHourMark = time.endsWith(':00');
                const showTimeLabel = isHourMark;
                
                return (
                  <tr key={time}>
                    {showTimeLabel ? (
                      <td 
                        rowSpan={2} 
                        className="border border-gray-300 text-xs text-gray-600 text-center bg-gray-50"
                      >
                        <div className="flex items-center justify-center h-full py-1">
                          {time}
                        </div>
                      </td>
                    ) : null}
                    {DAYS.map((day) => (
                      <td key={`${day}-${time}`} className="border border-gray-300 p-0">
                        <TimeSlotCell
                          day={day}
                          timeSlot={time}
                          isBlocked={isTimeSlotBlocked(day, time)}
                          onMouseDown={() => handleMouseDown(day, time)}
                          onMouseEnter={() => handleMouseEnter(day, time)}
                          onMouseUp={handleMouseUp}
                        />
                      </td>
                    ))}
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </div>

      {/* Summary */}
      <div className="mt-3 text-sm text-gray-600">
        {blockers.length === 0 ? (
          <p>No unavailable time slots. You are available during all default hours.</p>
        ) : (
          <p>
            {blockers.length} time slot{blockers.length !== 1 ? 's' : ''} blocked
          </p>
        )}
      </div>
    </div>
  );
};
