import React from 'react';
import { Course, PreferenceMode } from '../types';

interface CourseListProps {
  courses: Course[];
  preferenceMode: PreferenceMode;
  selectedCourse: Course | null;
  onPreferenceModeChange: (mode: PreferenceMode) => void;
  onCourseSelect: (course: Course | null) => void;
}

export const CourseList: React.FC<CourseListProps> = ({
  courses,
  preferenceMode,
  selectedCourse,
  onPreferenceModeChange,
  onCourseSelect,
}) => {
  return (
    <div className="w-full">
      <h3 className="text-lg font-semibold mb-3 text-gray-700">Your Courses</h3>

      {/* Preference Mode Toggle */}
      <div className="mb-4 p-4 bg-gray-50 rounded-lg border border-gray-200">
        <label className="block text-sm font-medium text-gray-700 mb-2">
          Room Preference Mode
        </label>
        <div className="flex gap-4 flex-wrap">
          <label className="flex items-center cursor-pointer">
            <input
              type="radio"
              name="preferenceMode"
              value="all-courses"
              checked={preferenceMode === 'all-courses'}
              onChange={() => onPreferenceModeChange('all-courses')}
              className="mr-2 w-4 h-4 text-blue-600"
            />
            <div>
              <span className="text-sm font-medium text-gray-800">
                Same for all courses
              </span>
              <p className="text-xs text-gray-500">
                Apply one room preference ranking to all your courses
              </p>
            </div>
          </label>

          <label className="flex items-center cursor-pointer">
            <input
              type="radio"
              name="preferenceMode"
              value="per-course"
              checked={preferenceMode === 'per-course'}
              onChange={() => onPreferenceModeChange('per-course')}
              className="mr-2 w-4 h-4 text-blue-600"
            />
            <div>
              <span className="text-sm font-medium text-gray-800">
                Different per course
              </span>
              <p className="text-xs text-gray-500">
                Set individual room preferences for each course
              </p>
            </div>
          </label>

          <label className="flex items-center cursor-pointer">
            <input
              type="radio"
              name="preferenceMode"
              value="smart-random"
              checked={preferenceMode === 'smart-random'}
              onChange={() => onPreferenceModeChange('smart-random')}
              className="mr-2 w-4 h-4 text-blue-600"
            />
            <div>
              <span className="text-sm font-medium text-gray-800">
                Let the program decide
              </span>
              <p className="text-xs text-gray-500">
                Smart preferences will be assigned to all your courses
              </p>
            </div>
          </label>
        </div>
      </div>

      {/* Course List */}
      {preferenceMode === 'smart-random' ? (
        <div className="text-center py-8 bg-green-50 rounded-lg border border-green-200">
          <p className="text-green-800 font-medium">Smart uniform preferences mode enabled</p>
          <p className="text-sm text-gray-600 mt-2">
            The system will automatically assign balanced preferences fitted for each of your courses.
          </p>
        </div>
      ) : courses.length === 0 ? (
        <div className="text-center py-8 bg-gray-50 rounded-lg border border-gray-200">
          <p className="text-gray-500">No courses found</p>
        </div>
      ) : (
        <div className="space-y-3">
          {preferenceMode === 'all-courses' ? (
            // Show all courses in a simple list when using global preferences
            <div className="space-y-2">
              {courses.map((course) => (
                <div
                  key={course.id}
                  className="bg-white border border-gray-200 rounded-lg p-4 shadow-sm"
                >
                  <div className="flex items-start justify-between">
                    <div className="flex-1">
                      <h4 className="font-semibold text-gray-800">{course.name}</h4>
                      <div className="mt-2 flex gap-4 text-sm text-gray-600">
                        <span>Students: {course.cohortSize}</span>
                        <span>Duration: {course.durationMinutes} min</span>
                      </div>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            // Show selectable courses when using per-course preferences
            <div className="space-y-2">
              <p className="text-sm text-gray-600 mb-2">
                Select a course to set its room preferences:
              </p>
              {courses.map((course) => (
                <button
                  key={course.id}
                  onClick={() =>
                    onCourseSelect(selectedCourse?.id === course.id ? null : course)
                  }
                  className={`
                    w-full text-left bg-white border rounded-lg p-4 shadow-sm
                    transition-all
                    ${
                      selectedCourse?.id === course.id
                        ? 'border-blue-500 ring-2 ring-blue-200 bg-blue-50'
                        : 'border-gray-200 hover:border-blue-300 hover:bg-gray-50'
                    }
                  `}
                >
                  <div className="flex items-start justify-between">
                    <div className="flex-1">
                      <h4 className="font-semibold text-gray-800">{course.name}</h4>
                      <div className="mt-2 flex gap-4 text-sm text-gray-600">
                        <span>Students: {course.cohortSize}</span>
                        <span>Duration: {course.durationMinutes} min</span>
                      </div>
                    </div>
                    {selectedCourse?.id === course.id && (
                      <div className="ml-3">
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
                            d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
                          />
                        </svg>
                      </div>
                    )}
                  </div>
                </button>
              ))}
            </div>
          )}
        </div>
      )}

      {/* Info for per-course mode */}
      {preferenceMode === 'per-course' && selectedCourse && (
        <div className="mt-4 p-3 bg-blue-50 border border-blue-200 rounded-lg">
          <p className="text-sm text-blue-800">
            Now setting room preferences for:{' '}
            <span className="font-semibold">{selectedCourse.name}</span>
          </p>
        </div>
      )}

      {preferenceMode === 'per-course' && !selectedCourse && (
        <div className="mt-4 p-3 bg-yellow-50 border border-yellow-200 rounded-lg">
          <p className="text-sm text-yellow-800">
            Please select a course above to set its room preferences
          </p>
        </div>
      )}
    </div>
  );
};
