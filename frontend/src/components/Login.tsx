import { useState } from 'react';
import { useUser } from '../contexts/UserContext';
import { apiService } from '../services/api';

export const Login = () => {
  const [professorId, setProfessorId] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const { login, setProfessor } = useUser();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!professorId.trim()) {
      setError('Please enter a professor ID');
      return;
    }

    setLoading(true);
    setError(null);

    try {
      // Fetch professor data from the backend
      const response = await apiService.getProfessorById(professorId.trim());

      if (response.success && response.data) {
        // Set the professor in context
        setProfessor(response.data);
        // Store professor ID in session storage
        login(professorId.trim());
      } else {
        setError(response.error || 'Failed to load professor data');
      }
    } catch (err) {
      setError('Failed to connect to server. Please try again.');
      console.error('Login error:', err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 to-indigo-100">
      <div className="bg-white p-8 rounded-lg shadow-xl w-full max-w-md">
        <div className="text-center mb-8">
          <h1 className="text-3xl font-bold text-gray-800 mb-2">
            Classroom Allocation System
          </h1>
          <p className="text-gray-600">Professor Login</p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-6">
          <div>
            <label
              htmlFor="professorId"
              className="block text-sm font-medium text-gray-700 mb-2"
            >
              Professor ID
            </label>
            <input
              type="text"
              id="professorId"
              value={professorId}
              onChange={(e) => {
                setProfessorId(e.target.value);
                setError(null);
              }}
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none transition"
              placeholder="Enter your professor ID (e.g., PROF001)"
              disabled={loading}
              autoFocus
            />
            <p className="mt-2 text-sm text-blue-600">
              For testing: enter <span className="font-mono font-semibold">PROF001</span>
            </p>
          </div>

          {error && (
            <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg text-sm">
              {error}
            </div>
          )}

          <button
            type="submit"
            disabled={loading || !professorId.trim()}
            className="w-full bg-blue-600 hover:bg-blue-700 disabled:bg-gray-400 disabled:cursor-not-allowed text-white font-semibold py-3 px-4 rounded-lg transition duration-200 flex items-center justify-center"
          >
            {loading ? (
              <>
                <svg className="animate-spin -ml-1 mr-3 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                  <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                  <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
                Logging in...
              </>
            ) : (
              'Login'
            )}
          </button>
        </form>

        <div className="mt-6 text-center text-sm text-gray-500">
          <p>Enter your professor ID to access your preferences and schedule</p>
        </div>
      </div>
    </div>
  );
};
