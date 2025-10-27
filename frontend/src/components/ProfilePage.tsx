import React from 'react';
import { CourseList } from './CourseList';
import { AvailabilityCalendar } from './AvailabilityCalendar';
import { Course, PreferenceMode, TimeBlocker, Professor } from '../types';

interface ProfilePageProps {
  professor: Professor | null;
  courses: Course[];
  preferenceMode: PreferenceMode;
  selectedCourse: Course | null;
  timeBlockers: TimeBlocker[];
  onPreferenceModeChange: (mode: PreferenceMode) => void;
  onCourseSelect: (course: Course | null) => void;
  onBlockersChange: (blockers: TimeBlocker[]) => void;
}

export const ProfilePage: React.FC<ProfilePageProps> = ({
  professor,
  courses,
  preferenceMode,
  selectedCourse,
  timeBlockers,
  onPreferenceModeChange,
  onCourseSelect,
  onBlockersChange,
}) => {
  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white shadow-sm border-b border-gray-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
          <h1 className="text-3xl font-bold text-gray-900">Professor Profile</h1>
          {professor && (
            <p className="text-lg text-gray-600 mt-2">{professor.name}</p>
          )}
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="space-y-8">
          {/* Section 1: Course List */}
          <section className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
            <CourseList
              courses={courses}
              preferenceMode={preferenceMode}
              selectedCourse={selectedCourse}
              onPreferenceModeChange={onPreferenceModeChange}
              onCourseSelect={onCourseSelect}
            />
          </section>

          {/* Section 2: Availability Calendar */}
          <section className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
            <AvailabilityCalendar
              blockers={timeBlockers}
              onBlockersChange={onBlockersChange}
            />
          </section>
        </div>
      </main>
    </div>
  );
};
