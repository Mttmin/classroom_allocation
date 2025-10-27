import React, { useEffect, useState } from 'react';
import { AllocationResult, AllocatedClass, DayOfWeek, AllocationStatus } from '../types';
import { apiService } from '../services/api';

interface AllocationResultsProps {
  professorId: string;
}

export const AllocationResults: React.FC<AllocationResultsProps> = ({
  professorId,
}) => {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [allocationData, setAllocationData] = useState<AllocationResult | null>(
    null
  );

  useEffect(() => {
    loadAllocationResults();
  }, [professorId]);

  const loadAllocationResults = async () => {
    setLoading(true);
    setError(null);

    try {
      const response = await apiService.getAllocationResults(professorId);

      if (response.success && response.data) {
        setAllocationData(response.data);
      } else {
        setError(response.error || 'Failed to load allocation results');
      }
    } catch (err) {
      setError('An unexpected error occurred');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const formatDay = (day: DayOfWeek): string => {
    return day.charAt(0) + day.slice(1).toLowerCase();
  };

  const formatTime = (time: string): string => {
    return time;
  };

  const formatAllocationDate = (dateString: string): string => {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  // Group classes by day for better visualization
  const groupClassesByDay = (
    classes: AllocatedClass[]
  ): Map<DayOfWeek, AllocatedClass[]> => {
    const grouped = new Map<DayOfWeek, AllocatedClass[]>();

    classes.forEach((cls) => {
      const day = cls.timeSlot.day;
      if (!grouped.has(day)) {
        grouped.set(day, []);
      }
      grouped.get(day)!.push(cls);
    });

    // Sort classes within each day by start time
    grouped.forEach((classes) => {
      classes.sort((a, b) =>
        a.timeSlot.startTime.localeCompare(b.timeSlot.startTime)
      );
    });

    return grouped;
  };

  const weekDays = [
    DayOfWeek.MONDAY,
    DayOfWeek.TUESDAY,
    DayOfWeek.WEDNESDAY,
    DayOfWeek.THURSDAY,
    DayOfWeek.FRIDAY,
    DayOfWeek.SATURDAY,
    DayOfWeek.SUNDAY,
  ];

  if (loading) {
    return (
      <div className="flex items-center justify-center py-12">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
          <p className="mt-4 text-gray-600">Loading allocation results...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-red-50 border border-red-200 rounded-lg p-6">
        <div className="flex items-start">
          <svg
            className="w-6 h-6 text-red-600 mt-0.5 mr-3"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
            />
          </svg>
          <div>
            <h3 className="text-lg font-medium text-red-900">Error</h3>
            <p className="text-sm text-red-800 mt-1">{error}</p>
          </div>
        </div>
      </div>
    );
  }

  if (!allocationData) {
    return (
      <div className="bg-gray-50 border border-gray-200 rounded-lg p-6 text-center">
        <p className="text-gray-600">No allocation data available</p>
      </div>
    );
  }

  // Show pending state if algorithm hasn't been run yet
  if (allocationData.status === AllocationStatus.PENDING) {
    return (
      <div className="space-y-6">
        {/* Header */}
        <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
          <h1 className="text-2xl font-bold text-gray-900">
            Classroom Allocation Results
          </h1>
          <p className="text-gray-600 mt-2">
            Professor: {allocationData.professorName}
          </p>
        </div>

        {/* Pending State Message */}
        <div className="bg-blue-50 border-2 border-blue-300 rounded-lg p-8">
          <div className="flex flex-col items-center text-center">
            {/* Icon */}
            <div className="mb-4">
              <svg
                className="w-20 h-20 text-blue-500"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={1.5}
                  d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"
                />
              </svg>
            </div>

            {/* Message */}
            <h2 className="text-2xl font-bold text-gray-900 mb-3">
              Allocation Pending
            </h2>
            <p className="text-lg text-gray-700 max-w-2xl mb-6">
              The classroom allocation algorithm has not been run yet. Results will be
              published once all professors have submitted their preferences.
            </p>

            {/* Estimated Date */}
            {allocationData.estimatedPublishDate && (
              <div className="bg-white border border-blue-200 rounded-lg p-6 max-w-md">
                <div className="flex items-center justify-center space-x-3">
                  <svg
                    className="w-6 h-6 text-blue-600"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"
                    />
                  </svg>
                  <div className="text-left">
                    <p className="text-sm font-medium text-gray-600">
                      Estimated Publication Date
                    </p>
                    <p className="text-lg font-bold text-gray-900">
                      {formatAllocationDate(allocationData.estimatedPublishDate)}
                    </p>
                  </div>
                </div>
              </div>
            )}

            {/* Instructions */}
            <div className="mt-8 bg-white border border-blue-200 rounded-lg p-6 max-w-2xl">
              <h3 className="text-lg font-semibold text-gray-900 mb-3">
                What to do now?
              </h3>
              <ul className="text-left space-y-2 text-gray-700">
                <li className="flex items-start">
                  <svg
                    className="w-5 h-5 text-blue-500 mr-2 mt-0.5 flex-shrink-0"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
                    />
                  </svg>
                  <span>
                    Make sure you have submitted all your room preferences and
                    availability
                  </span>
                </li>
                <li className="flex items-start">
                  <svg
                    className="w-5 h-5 text-blue-500 mr-2 mt-0.5 flex-shrink-0"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"
                    />
                  </svg>
                  <span>
                    You will be notified by email when the allocation results are
                    available
                  </span>
                </li>
                <li className="flex items-start">
                  <svg
                    className="w-5 h-5 text-blue-500 mr-2 mt-0.5 flex-shrink-0"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"
                    />
                  </svg>
                  <span>Check back on this page after the estimated publication date</span>
                </li>
              </ul>
            </div>
          </div>
        </div>
      </div>
    );
  }

  // Show in-progress state if algorithm is running
  if (allocationData.status === AllocationStatus.IN_PROGRESS) {
    return (
      <div className="space-y-6">
        {/* Header */}
        <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
          <h1 className="text-2xl font-bold text-gray-900">
            Classroom Allocation Results
          </h1>
          <p className="text-gray-600 mt-2">
            Professor: {allocationData.professorName}
          </p>
        </div>

        {/* In Progress Message */}
        <div className="bg-yellow-50 border-2 border-yellow-300 rounded-lg p-8">
          <div className="flex flex-col items-center text-center">
            <div className="mb-4">
              <div className="animate-spin rounded-full h-20 w-20 border-b-4 border-yellow-600"></div>
            </div>
            <h2 className="text-2xl font-bold text-gray-900 mb-3">
              Allocation in Progress
            </h2>
            <p className="text-lg text-gray-700 max-w-2xl">
              The classroom allocation algorithm is currently running. This process may
              take several minutes. Please check back shortly.
            </p>
          </div>
        </div>
      </div>
    );
  }

  const groupedClasses = groupClassesByDay(allocationData.allocatedClasses);

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
        <div className="flex items-start justify-between">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">
              Classroom Allocation Results
            </h1>
            <p className="text-gray-600 mt-2">
              Professor: {allocationData.professorName}
            </p>
            {allocationData.allocationDate && (
              <p className="text-sm text-gray-500 mt-1">
                Last updated: {formatAllocationDate(allocationData.allocationDate)}
              </p>
            )}
          </div>
          <span className="inline-flex items-center px-4 py-2 rounded-full text-sm font-medium bg-green-100 text-green-800">
            <svg
              className="w-4 h-4 mr-2"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
              />
            </svg>
            Completed
          </span>
        </div>
      </div>

      {/* Summary Statistics */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-4">
          <div className="flex items-center">
            <div className="flex-shrink-0">
              <svg
                className="h-8 w-8 text-green-600"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
                />
              </svg>
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">
                Allocated Classes
              </p>
              <p className="text-2xl font-bold text-gray-900">
                {allocationData.allocatedClasses.length}
              </p>
            </div>
          </div>
        </div>

        <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-4">
          <div className="flex items-center">
            <div className="flex-shrink-0">
              <svg
                className="h-8 w-8 text-yellow-600"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"
                />
              </svg>
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">
                Unallocated Courses
              </p>
              <p className="text-2xl font-bold text-gray-900">
                {allocationData.unallocatedCourses.length}
              </p>
            </div>
          </div>
        </div>

        <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-4">
          <div className="flex items-center">
            <div className="flex-shrink-0">
              <svg
                className="h-8 w-8 text-blue-600"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"
                />
              </svg>
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">Total Courses</p>
              <p className="text-2xl font-bold text-gray-900">
                {allocationData.allocatedClasses.length +
                  allocationData.unallocatedCourses.length}
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* Weekly Schedule View */}
      <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
        <h2 className="text-xl font-semibold text-gray-900 mb-4">
          Weekly Schedule
        </h2>

        <div className="space-y-4">
          {weekDays.map((day) => {
            const dayClasses = groupedClasses.get(day) || [];

            return (
              <div key={day} className="border-b border-gray-200 pb-4 last:border-b-0">
                <h3 className="text-lg font-medium text-gray-800 mb-3">
                  {formatDay(day)}
                </h3>

                {dayClasses.length === 0 ? (
                  <p className="text-sm text-gray-500 ml-4">No classes scheduled</p>
                ) : (
                  <div className="space-y-3">
                    {dayClasses.map((cls, idx) => (
                      <div
                        key={idx}
                        className="ml-4 bg-blue-50 border-l-4 border-blue-500 rounded-r-lg p-4"
                      >
                        <div className="flex items-start justify-between">
                          <div className="flex-1">
                            <h4 className="font-semibold text-gray-900">
                              {cls.courseName}
                            </h4>
                            <div className="mt-2 grid grid-cols-1 md:grid-cols-2 gap-2 text-sm text-gray-700">
                              <div className="flex items-center">
                                <svg
                                  className="w-4 h-4 mr-2 text-gray-500"
                                  fill="none"
                                  stroke="currentColor"
                                  viewBox="0 0 24 24"
                                >
                                  <path
                                    strokeLinecap="round"
                                    strokeLinejoin="round"
                                    strokeWidth={2}
                                    d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"
                                  />
                                </svg>
                                <span>
                                  {formatTime(cls.timeSlot.startTime)} -{' '}
                                  {formatTime(cls.timeSlot.endTime)}
                                </span>
                              </div>
                              <div className="flex items-center">
                                <svg
                                  className="w-4 h-4 mr-2 text-gray-500"
                                  fill="none"
                                  stroke="currentColor"
                                  viewBox="0 0 24 24"
                                >
                                  <path
                                    strokeLinecap="round"
                                    strokeLinejoin="round"
                                    strokeWidth={2}
                                    d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"
                                  />
                                </svg>
                                <span className="font-medium">{cls.room.name}</span>
                              </div>
                              <div className="flex items-center">
                                <svg
                                  className="w-4 h-4 mr-2 text-gray-500"
                                  fill="none"
                                  stroke="currentColor"
                                  viewBox="0 0 24 24"
                                >
                                  <path
                                    strokeLinecap="round"
                                    strokeLinejoin="round"
                                    strokeWidth={2}
                                    d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"
                                  />
                                </svg>
                                <span>
                                  {cls.cohortSize} students (Capacity: {cls.room.capacity})
                                </span>
                              </div>
                              <div className="flex items-center">
                                <svg
                                  className="w-4 h-4 mr-2 text-gray-500"
                                  fill="none"
                                  stroke="currentColor"
                                  viewBox="0 0 24 24"
                                >
                                  <path
                                    strokeLinecap="round"
                                    strokeLinejoin="round"
                                    strokeWidth={2}
                                    d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"
                                  />
                                </svg>
                                <span>{cls.room.building}</span>
                              </div>
                            </div>
                          </div>
                          <div className="ml-4 flex-shrink-0">
                            <span className="inline-flex items-center px-3 py-1 rounded-full text-xs font-medium bg-green-100 text-green-800">
                              {cls.durationMinutes} min
                            </span>
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            );
          })}
        </div>
      </div>

      {/* Unallocated Courses */}
      {allocationData.unallocatedCourses.length > 0 && (
        <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
          <h2 className="text-xl font-semibold text-gray-900 mb-4">
            Unallocated Courses
          </h2>
          <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-4">
            <div className="flex items-start">
              <svg
                className="w-6 h-6 text-yellow-600 mt-0.5 mr-3"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
                />
              </svg>
              <div className="flex-1">
                <h3 className="text-sm font-medium text-yellow-900">
                  The following courses could not be allocated:
                </h3>
                <ul className="mt-2 space-y-1">
                  {allocationData.unallocatedCourses.map((course) => (
                    <li key={course.id} className="text-sm text-yellow-800">
                      {course.name} - {course.cohortSize} students,{' '}
                      {course.durationMinutes} minutes
                    </li>
                  ))}
                </ul>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
