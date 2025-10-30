import { useEffect, useState } from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { CourseList } from './components/CourseList';
import { RoomTypeSelector } from './components/RoomTypeSelector';
import { Navigation } from './components/Navigation';
import { ProfilePage } from './components/ProfilePage';
import { AllocationResults } from './components/AllocationResults';
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
  const [isPreferencesValid, setIsPreferencesValid] = useState(false);

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

    // Clear preferences when switching modes
    if (mode === 'all-courses') {
      setCourseRoomPreferences(new Map());
    } else if (mode === 'per-course') {
      setGlobalRoomPreferences([]);
    } else if (mode === 'smart-random') {
      // Clear both when switching to smart-random
      setGlobalRoomPreferences([]);
      setCourseRoomPreferences(new Map());
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

  // Check if preferences are incomplete (less than recommended)
  const hasIncompletePreferences = (): boolean => {
    if (preferenceMode === 'all-courses') {
      return globalRoomPreferences.length > 0 && globalRoomPreferences.length < 10;
    } else if (preferenceMode === 'per-course') {
      for (const course of courses) {
        const prefs = courseRoomPreferences.get(course.id || '');
        if (prefs && prefs.length > 0 && prefs.length < 10) {
          return true;
        }
      }
    }
    return false;
  };

  // Validate form (no longer enforces minimum preferences)
  const validateForm = (): string | null => {
    // No validation errors - backend will handle incomplete preferences
    return null;
  };

  // Check if form is valid for enabling/disabling save button
  const isFormValid = (): boolean => {
    if (preferenceMode === 'all-courses') {
      // Valid as long as user is in all-courses mode (can have 0+ preferences)
      return true;
    } else if (preferenceMode === 'per-course') {
      // Valid as long as user is in per-course mode (can have 0+ preferences per course)
      return true;
    } else if (preferenceMode === 'smart-random') {
      // Always valid for smart-random mode
      return true;
    }
    return false;
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
        // For smart-random mode, both globalRoomPreferences and courseRoomPreferences are undefined
        // The backend will handle generating random preferences
      };

      const response = await apiService.submitProfessorPreferences(formData);

      if (response.success) {
        // Show different success messages based on preference completeness
        if (hasIncompletePreferences()) {
          setSuccessMessage(
            'Preferences saved successfully! The optimization system will complete your preferences with smart suggestions.'
          );
        } else {
          setSuccessMessage('Preferences saved successfully!');
        }
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
    <Router>
      <div className="min-h-screen bg-gray-50">
        {/* Navigation */}
        <Navigation 
          professor={professor} 
          onSave={handleSubmit} 
          saving={saving}
          disabled={!isFormValid()}
        />

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

          <Routes>
            {/* Main Room Preferences Page */}
            <Route
              path="/"
              element={
                <div className="space-y-8">
                  {/* Show preference mode selector */}
                  <section className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
                    <CourseList
                      courses={courses}
                      preferenceMode={preferenceMode}
                      selectedCourse={selectedCourse}
                      onPreferenceModeChange={handlePreferenceModeChange}
                      onCourseSelect={handleCourseSelect}
                    />
                  </section>

                  {/* Room Type Preferences - Hide for smart-random mode */}
                  {preferenceMode !== 'smart-random' && (
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
                          onValidationChange={setIsPreferencesValid}
                          minSelection={5}
                          recommendedSelection={10}
                        />
                      )}
                    </section>
                  )}

                  {/* Bottom Save Button */}
                  <div className="flex justify-end">
                    <button
                      onClick={handleSubmit}
                      disabled={saving || !isFormValid()}
                      className={`
                        px-8 py-3 rounded-lg font-medium text-lg transition-colors
                        ${
                          saving || !isFormValid()
                            ? 'bg-gray-400 cursor-not-allowed text-gray-200'
                            : 'bg-blue-600 hover:bg-blue-700 text-white shadow-md hover:shadow-lg'
                        }
                      `}
                    >
                      {saving ? 'Saving...' : 'Save All Preferences'}
                    </button>
                  </div>
                </div>
              }
            />

            {/* Profile Page */}
            <Route
              path="/profile"
              element={
                <ProfilePage
                  professor={professor}
                  courses={courses}
                  preferenceMode={preferenceMode}
                  selectedCourse={selectedCourse}
                  timeBlockers={timeBlockers}
                  onPreferenceModeChange={handlePreferenceModeChange}
                  onCourseSelect={handleCourseSelect}
                  onBlockersChange={setTimeBlockers}
                />
              }
            />

            {/* Allocation Results Page */}
            <Route
              path="/allocation"
              element={
                <AllocationResults
                  professorId={professor?.id || 'PROF001'}
                />
              }
            />
          </Routes>
        </main>
      </div>
    </Router>
  );
}

export default App;
