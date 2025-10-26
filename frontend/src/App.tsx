import { useEffect, useState } from 'react';
import { CourseList } from './components/CourseList';
import { RoomTypeSelector } from './components/RoomTypeSelector';
import { AvailabilityCalendar } from './components/AvailabilityCalendar';
import {
  Course,
  PreferenceMode,
  RoomType,
  TimeBlocker,
  Professor,
  ProfessorFormData,
} from './types';
import { apiService } from './services/api';

function App() {
  // State
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [professor, setProfessor] = useState<Professor | null>(null);
  const [courses, setCourses] = useState<Course[]>([]);
  const [preferenceMode, setPreferenceMode] = useState<PreferenceMode>('all-courses');
  const [selectedCourse, setSelectedCourse] = useState<Course | null>(null);
  const [globalRoomPreferences, setGlobalRoomPreferences] = useState<RoomType[]>([]);
  const [courseRoomPreferences, setCourseRoomPreferences] = useState<
    Map<string, RoomType[]>
  >(new Map());
  const [timeBlockers, setTimeBlockers] = useState<TimeBlocker[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  // Load professor data on mount
  useEffect(() => {
    loadProfessorData();
  }, []);

  const loadProfessorData = async () => {
    setLoading(true);
    setError(null);

    try {
      // In production, this would come from authentication/session
      const professorId = 'PROF001';

      const response = await apiService.getProfessorById(professorId);

      if (response.success && response.data) {
        setProfessor(response.data);
        setCourses(response.data.courses || []);
      } else {
        setError(response.error || 'Failed to load professor data');
      }
    } catch (err) {
      setError('An unexpected error occurred');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  // Handle preference mode change
  const handlePreferenceModeChange = (mode: PreferenceMode) => {
    setPreferenceMode(mode);
    setSelectedCourse(null);

    // Clear the other mode's preferences
    if (mode === 'all-courses') {
      setCourseRoomPreferences(new Map());
    } else {
      setGlobalRoomPreferences([]);
    }
  };

  // Handle course selection
  const handleCourseSelect = (course: Course | null) => {
    setSelectedCourse(course);

    // Load existing preferences for this course if any
    if (course && courseRoomPreferences.has(course.id || '')) {
      // Preferences are already in state
    }
  };

  // Handle room preferences change
  const handleRoomPreferencesChange = (preferences: RoomType[]) => {
    if (preferenceMode === 'all-courses') {
      setGlobalRoomPreferences(preferences);
    } else if (selectedCourse) {
      const newPreferences = new Map(courseRoomPreferences);
      newPreferences.set(selectedCourse.id || '', preferences);
      setCourseRoomPreferences(newPreferences);
    }
  };

  // Get current room preferences based on mode
  const getCurrentRoomPreferences = (): RoomType[] => {
    if (preferenceMode === 'all-courses') {
      return globalRoomPreferences;
    } else if (selectedCourse) {
      return courseRoomPreferences.get(selectedCourse.id || '') || [];
    }
    return [];
  };

  // Validate form
  const validateForm = (): string | null => {
    if (preferenceMode === 'all-courses') {
      if (globalRoomPreferences.length < 5) {
        return 'Please select at least 5 room type preferences';
      }
    } else {
      // Check that all courses have at least 5 preferences
      for (const course of courses) {
        const prefs = courseRoomPreferences.get(course.id || '');
        if (!prefs || prefs.length < 5) {
          return `Please set at least 5 room preferences for "${course.name}"`;
        }
      }
    }
    return null;
  };

  // Handle form submission
  const handleSubmit = async () => {
    setError(null);
    setSuccessMessage(null);

    // Validate
    const validationError = validateForm();
    if (validationError) {
      setError(validationError);
      return;
    }

    setSaving(true);

    try {
      if (!professor) {
        setError('Professor data not loaded');
        return;
      }

      const formData: ProfessorFormData = {
        professorId: professor.id,
        preferenceMode,
        globalRoomPreferences:
          preferenceMode === 'all-courses' ? globalRoomPreferences : undefined,
        courseRoomPreferences:
          preferenceMode === 'per-course' ? courseRoomPreferences : undefined,
        unavailableSlots: timeBlockers,
      };

      const response = await apiService.submitProfessorPreferences(formData);

      if (response.success) {
        setSuccessMessage('Preferences saved successfully!');
        // Auto-hide success message after 5 seconds
        setTimeout(() => setSuccessMessage(null), 5000);
      } else {
        setError(response.error || 'Failed to save preferences');
      }
    } catch (err) {
      setError('An unexpected error occurred while saving');
      console.error(err);
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
          <p className="mt-4 text-gray-600">Loading...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white shadow-sm border-b border-gray-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-2xl font-bold text-gray-900">
                Professor Portal - Room Preferences
              </h1>
              {professor && (
                <p className="text-sm text-gray-600 mt-1">
                  Welcome, {professor.name}
                </p>
              )}
            </div>
            <button
              onClick={handleSubmit}
              disabled={saving}
              className={`
                px-6 py-2 rounded-lg font-medium transition-colors
                ${
                  saving
                    ? 'bg-gray-400 cursor-not-allowed'
                    : 'bg-blue-600 hover:bg-blue-700 text-white'
                }
              `}
            >
              {saving ? 'Saving...' : 'Save Preferences'}
            </button>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Error Message */}
        {error && (
          <div className="mb-6 bg-red-50 border border-red-200 rounded-lg p-4">
            <div className="flex items-start">
              <svg
                className="w-5 h-5 text-red-600 mt-0.5 mr-3"
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
              <p className="text-sm text-red-800">{error}</p>
            </div>
          </div>
        )}

        {/* Success Message */}
        {successMessage && (
          <div className="mb-6 bg-green-50 border border-green-200 rounded-lg p-4">
            <div className="flex items-start">
              <svg
                className="w-5 h-5 text-green-600 mt-0.5 mr-3"
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
              <p className="text-sm text-green-800">{successMessage}</p>
            </div>
          </div>
        )}

        <div className="space-y-8">
          {/* Section 1: Course List */}
          <section className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
            <CourseList
              courses={courses}
              preferenceMode={preferenceMode}
              selectedCourse={selectedCourse}
              onPreferenceModeChange={handlePreferenceModeChange}
              onCourseSelect={handleCourseSelect}
            />
          </section>

          {/* Section 2: Room Type Preferences */}
          <section className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
            <h2 className="text-xl font-semibold text-gray-800 mb-4">
              {preferenceMode === 'all-courses'
                ? 'Room Type Preferences (All Courses)'
                : selectedCourse
                  ? `Room Type Preferences - ${selectedCourse.name}`
                  : 'Room Type Preferences'}
            </h2>

            {preferenceMode === 'per-course' && !selectedCourse ? (
              <div className="text-center py-12 bg-gray-50 rounded-lg border-2 border-dashed border-gray-300">
                <p className="text-gray-600">
                  Please select a course from the list above to set its room
                  preferences
                </p>
              </div>
            ) : (
              <RoomTypeSelector
                selectedTypes={getCurrentRoomPreferences()}
                onSelectedTypesChange={handleRoomPreferencesChange}
                minSelection={5}
              />
            )}
          </section>

          {/* Section 3: Availability Calendar */}
          <section className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
            <AvailabilityCalendar
              blockers={timeBlockers}
              onBlockersChange={setTimeBlockers}
            />
          </section>
        </div>

        {/* Bottom Save Button */}
        <div className="mt-8 flex justify-end">
          <button
            onClick={handleSubmit}
            disabled={saving}
            className={`
              px-8 py-3 rounded-lg font-medium text-lg transition-colors
              ${
                saving
                  ? 'bg-gray-400 cursor-not-allowed'
                  : 'bg-blue-600 hover:bg-blue-700 text-white shadow-md hover:shadow-lg'
              }
            `}
          >
            {saving ? 'Saving...' : 'Save All Preferences'}
          </button>
        </div>
      </main>
    </div>
  );
}

export default App;
