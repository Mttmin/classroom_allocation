import React, { useState, useEffect } from 'react';
import { apiService } from '../services/api';

interface AdminStats {
  totalProfessors: number;
  totalCourses: number;
  totalRooms: number;
  preferenceStatistics: {
    professorsWithAllPreferences: number;
    professorsWithNoPreferences: number;
    professorsWithPartialPreferences: number;
    totalCoursesWithPreferences: number;
    totalCoursesWithoutPreferences: number;
  };
  roomsByType: Record<string, number>;
  courseStatistics: {
    assignedCourses: number;
    unassignedCourses: number;
    totalStudents: number;
    averageCohortSize: number;
    minCohortSize: number;
    maxCohortSize: number;
  };
}

interface AlgorithmStatus {
  isRunning: boolean;
  lastResult?: {
    success: boolean;
    totalCourses: number;
    assignedCourses: number;
    unassignedCourses: number;
    firstChoiceCount: number;
    topThreeChoiceCount: number;
    averageChoiceRank: number;
    allocationRate: number;
    timestamp: number;
    error?: string;
  };
}

const AdminPage: React.FC = () => {
  const [stats, setStats] = useState<AdminStats | null>(null);
  const [algorithmStatus, setAlgorithmStatus] = useState<AlgorithmStatus | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Algorithm parameters
  const [strategy, setStrategy] = useState('SmartRandom');
  const [optimizer, setOptimizer] = useState('SimulatedAnnealing'); // New: optimizer selection
  const [numPreferences, setNumPreferences] = useState(10);
  // DISABLED: Always use simulated courses
  const useExistingCourses = false; // const [useExistingCourses, setUseExistingCourses] = useState(true);
  const [completePreferences, setCompletePreferences] = useState(true);
  const [numCourses, setNumCourses] = useState(70);
  const [minSize, setMinSize] = useState(10);
  const [maxSize, setMaxSize] = useState(200);
  const [changeSize, setChangeSize] = useState(35);

  const [isRunning, setIsRunning] = useState(false);
  const [logs, setLogs] = useState<string[]>([]);

  // Load statistics on mount
  useEffect(() => {
    loadStatistics();
    loadAlgorithmStatus();
  }, []);

  // Poll algorithm status while running
  useEffect(() => {
    let interval: NodeJS.Timeout | null = null;

    if (isRunning) {
      interval = setInterval(() => {
        loadAlgorithmStatus();
      }, 2000);
    }

    return () => {
      if (interval) clearInterval(interval);
    };
  }, [isRunning]);

  const loadStatistics = async () => {
    try {
      setLoading(true);
      const response = await apiService.getAdminStatistics();

      if (response.success && response.data) {
        setStats(response.data);
      } else {
        setError(response.error || 'Failed to load statistics');
      }
    } catch (err) {
      setError('Failed to load statistics');
    } finally {
      setLoading(false);
    }
  };

  const loadAlgorithmStatus = async () => {
    try {
      const response = await apiService.getAlgorithmStatus();

      if (response.success && response.data) {
        setAlgorithmStatus(response.data);

        if (response.data.isRunning !== isRunning) {
          setIsRunning(response.data.isRunning);
        }

        // If algorithm just finished, reload statistics
        if (!response.data.isRunning && isRunning) {
          loadStatistics();
          if (response.data.lastResult) {
            addLog(`Algorithm completed at ${new Date(response.data.lastResult.timestamp).toLocaleTimeString()}`);
            addLog(`Assigned ${response.data.lastResult.assignedCourses}/${response.data.lastResult.totalCourses} courses`);
          }
        }
      }
    } catch (err) {
      console.error('Failed to load algorithm status', err);
    }
  };

  const addLog = (message: string) => {
    setLogs((prev) => [...prev, `[${new Date().toLocaleTimeString()}] ${message}`]);
  };

  const runAlgorithm = async (e?: React.MouseEvent<HTMLButtonElement>) => {
    // Prevent any default behavior
    if (e) {
      e.preventDefault();
      e.stopPropagation();
    }

    try {
      setIsRunning(true);
      setLogs([]);
      addLog('Starting allocation algorithm...');
      addLog(`Optimizer: ${optimizer}`);
      addLog(`Strategy: ${strategy}`);
      addLog(`Number of preferences: ${numPreferences}`);
      addLog(`Complete preferences: ${completePreferences ? 'Yes' : 'No'}`);

      const params = {
        strategy,
        optimizer,
        numPreferences,
        useExistingCourses,
        completePreferences,
        ...(useExistingCourses ? {} : { numCourses, minSize, maxSize, changeSize }),
      };

      const response = await apiService.runAllocation(params);

      if (response.success) {
        addLog('Algorithm started successfully');
      } else {
        addLog(`Error: ${response.error}`);
        setIsRunning(false);
      }
    } catch (err) {
      addLog(`Error: ${err instanceof Error ? err.message : 'Unknown error'}`);
      setIsRunning(false);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
          <p className="text-gray-600">Loading statistics...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="bg-red-50 border border-red-200 rounded-lg p-6 max-w-md">
          <h3 className="text-red-800 font-semibold mb-2">Error</h3>
          <p className="text-red-600">{error}</p>
          <button
            onClick={loadStatistics}
            className="mt-4 px-4 py-2 bg-red-600 text-white rounded-md hover:bg-red-700"
          >
            Retry
          </button>
        </div>
      </div>
    );
  }

  const preferenceCompletionRate = stats
    ? ((stats.preferenceStatistics.professorsWithAllPreferences /
        stats.totalProfessors) *
        100).toFixed(1)
    : 0;

  return (
    <div className="min-h-screen bg-gray-50 p-6">
      <div className="max-w-7xl mx-auto">
        <h1 className="text-3xl font-bold text-gray-900 mb-6">Admin Dashboard</h1>

        {/* System Statistics */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-6">
          <StatCard
            title="Total Professors"
            value={stats?.totalProfessors || 0}
            subtitle={`${stats?.preferenceStatistics.professorsWithAllPreferences || 0} completed preferences`}
            color="blue"
          />
          <StatCard
            title="Total Courses"
            value={stats?.totalCourses || 0}
            subtitle={`${stats?.courseStatistics.assignedCourses || 0} assigned`}
            color="green"
          />
          <StatCard
            title="Total Rooms"
            value={stats?.totalRooms || 0}
            subtitle="Available for allocation"
            color="purple"
          />
        </div>

        {/* Preference Statistics */}
        <div className="bg-white rounded-lg shadow-md p-6 mb-6">
          <h2 className="text-xl font-semibold mb-4">Preference Completion Status</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
            <div className="bg-green-50 p-4 rounded-lg">
              <div className="text-2xl font-bold text-green-600">
                {stats?.preferenceStatistics.professorsWithAllPreferences || 0}
              </div>
              <div className="text-sm text-green-700">All Preferences Complete</div>
            </div>
            <div className="bg-yellow-50 p-4 rounded-lg">
              <div className="text-2xl font-bold text-yellow-600">
                {stats?.preferenceStatistics.professorsWithPartialPreferences || 0}
              </div>
              <div className="text-sm text-yellow-700">Partial Preferences</div>
            </div>
            <div className="bg-red-50 p-4 rounded-lg">
              <div className="text-2xl font-bold text-red-600">
                {stats?.preferenceStatistics.professorsWithNoPreferences || 0}
              </div>
              <div className="text-sm text-red-700">No Preferences</div>
            </div>
            <div className="bg-blue-50 p-4 rounded-lg">
              <div className="text-2xl font-bold text-blue-600">{preferenceCompletionRate}%</div>
              <div className="text-sm text-blue-700">Completion Rate</div>
            </div>
          </div>
        </div>

        {/* Course Statistics */}
        {stats && (
          <div className="bg-white rounded-lg shadow-md p-6 mb-6">
            <h2 className="text-xl font-semibold mb-4">Course Statistics</h2>
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
              <div>
                <div className="text-sm text-gray-600">Total Students</div>
                <div className="text-2xl font-bold text-gray-900">
                  {stats.courseStatistics.totalStudents}
                </div>
              </div>
              <div>
                <div className="text-sm text-gray-600">Average Cohort Size</div>
                <div className="text-2xl font-bold text-gray-900">
                  {stats.courseStatistics.averageCohortSize}
                </div>
              </div>
              <div>
                <div className="text-sm text-gray-600">Min Cohort Size</div>
                <div className="text-2xl font-bold text-gray-900">
                  {stats.courseStatistics.minCohortSize}
                </div>
              </div>
              <div>
                <div className="text-sm text-gray-600">Max Cohort Size</div>
                <div className="text-2xl font-bold text-gray-900">
                  {stats.courseStatistics.maxCohortSize}
                </div>
              </div>
            </div>
          </div>
        )}

        {/* Algorithm Control Panel */}
        <div className="bg-white rounded-lg shadow-md p-6 mb-6">
          <h2 className="text-xl font-semibold mb-4">Allocation Algorithm</h2>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-6">
            {/* Optimizer Selection */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Scheduler Optimizer
              </label>
              <select
                value={optimizer}
                onChange={(e) => setOptimizer(e.target.value)}
                disabled={isRunning}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:bg-gray-100"
              >
                <option value="OneAtATime">One At A Time (Greedy)</option>
                <option value="SimulatedAnnealing">Simulated Annealing</option>
              </select>
              <p className="mt-1 text-xs text-gray-500">
                {optimizer === 'OneAtATime' && 'Fast greedy scheduler (~5s)'}
                {optimizer === 'SimulatedAnnealing' && 'Optimized scheduler (~30-60s)'}
              </p>
            </div>

            {/* Strategy Selection */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Preference Strategy
              </label>
              <select
                value={strategy}
                onChange={(e) => setStrategy(e.target.value)}
                disabled={isRunning}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:bg-gray-100"
              >
                <option value="SmartRandom">Smart Random</option>
                <option value="Satisfaction">Satisfaction Based</option>
                <option value="SizeBased">Size Based</option>
                <option value="Random">Random</option>
                <option value="Fixed">Fixed</option>
              </select>
              <p className="mt-1 text-xs text-gray-500">
                {strategy === 'SmartRandom' && 'Randomly selects from suitable room types'}
                {strategy === 'Satisfaction' && 'Uses satisfaction survey data'}
                {strategy === 'SizeBased' && 'Matches based on cohort size'}
                {strategy === 'Random' && 'Completely random selection'}
                {strategy === 'Fixed' && 'Uses predefined preference order'}
              </p>
            </div>

            {/* Number of Preferences */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Number of Preferences
              </label>
              <input
                type="number"
                min="1"
                max="10"
                value={numPreferences}
                onChange={(e) => setNumPreferences(parseInt(e.target.value))}
                disabled={isRunning}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:bg-gray-100"
              />
              <p className="mt-1 text-xs text-gray-500">Recommended: 10 preferences</p>
            </div>
          </div>

          {/* Toggles */}
          <div className="space-y-3 mb-6">
            <label className="flex items-center">
              <input
                type="checkbox"
                checked={completePreferences}
                onChange={(e) => setCompletePreferences(e.target.checked)}
                disabled={isRunning}
                className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
              />
              <span className="ml-2 text-sm text-gray-700">
                Auto-complete missing preferences
              </span>
            </label>
          </div>

          {/* Simulation Parameters */}
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6 p-4 bg-gray-50 rounded-lg">
              <div>
                <label className="block text-xs font-medium text-gray-700 mb-1">
                  Num Courses
                </label>
                <input
                  type="number"
                  min="1"
                  value={numCourses}
                  onChange={(e) => setNumCourses(parseInt(e.target.value))}
                  disabled={isRunning}
                  className="w-full px-2 py-1 text-sm border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:bg-gray-100"
                />
              </div>
              <div>
                <label className="block text-xs font-medium text-gray-700 mb-1">
                  Min Size
                </label>
                <input
                  type="number"
                  min="1"
                  value={minSize}
                  onChange={(e) => setMinSize(parseInt(e.target.value))}
                  disabled={isRunning}
                  className="w-full px-2 py-1 text-sm border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:bg-gray-100"
                />
              </div>
              <div>
                <label className="block text-xs font-medium text-gray-700 mb-1">
                  Max Size
                </label>
                <input
                  type="number"
                  min="1"
                  value={maxSize}
                  onChange={(e) => setMaxSize(parseInt(e.target.value))}
                  disabled={isRunning}
                  className="w-full px-2 py-1 text-sm border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:bg-gray-100"
                />
              </div>
              <div>
                <label className="block text-xs font-medium text-gray-700 mb-1">
                  Change Size
                </label>
                <input
                  type="number"
                  min="1"
                  value={changeSize}
                  onChange={(e) => setChangeSize(parseInt(e.target.value))}
                  disabled={isRunning}
                  className="w-full px-2 py-1 text-sm border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:bg-gray-100"
                />
              </div>
            </div>

          {/* Run Button */}
          <button
            type="button"
            onClick={runAlgorithm}
            disabled={isRunning}
            className={`w-full px-6 py-3 rounded-md text-white font-medium ${
              isRunning
                ? 'bg-gray-400 cursor-not-allowed'
                : 'bg-blue-600 hover:bg-blue-700'
            }`}
          >
            {isRunning ? (
              <span className="flex items-center justify-center">
                <svg
                  className="animate-spin -ml-1 mr-3 h-5 w-5 text-white"
                  xmlns="http://www.w3.org/2000/svg"
                  fill="none"
                  viewBox="0 0 24 24"
                >
                  <circle
                    className="opacity-25"
                    cx="12"
                    cy="12"
                    r="10"
                    stroke="currentColor"
                    strokeWidth="4"
                  ></circle>
                  <path
                    className="opacity-75"
                    fill="currentColor"
                    d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                  ></path>
                </svg>
                Running Algorithm...
              </span>
            ) : (
              'Run Allocation Algorithm'
            )}
          </button>
        </div>

        {/* Algorithm Results */}
        {algorithmStatus?.lastResult && (
          <div className="bg-white rounded-lg shadow-md p-6 mb-6">
            <h2 className="text-xl font-semibold mb-4">Last Allocation Results</h2>

            {algorithmStatus.lastResult.success ? (
              <div className="space-y-4">
                <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                  <div className="bg-blue-50 p-4 rounded-lg">
                    <div className="text-sm text-blue-700">Allocation Rate</div>
                    <div className="text-2xl font-bold text-blue-600">
                      {(algorithmStatus.lastResult.allocationRate * 100).toFixed(1)}%
                    </div>
                  </div>
                  <div className="bg-green-50 p-4 rounded-lg">
                    <div className="text-sm text-green-700">Assigned Courses</div>
                    <div className="text-2xl font-bold text-green-600">
                      {algorithmStatus.lastResult.assignedCourses}/
                      {algorithmStatus.lastResult.totalCourses}
                    </div>
                  </div>
                  <div className="bg-purple-50 p-4 rounded-lg">
                    <div className="text-sm text-purple-700">First Choice</div>
                    <div className="text-2xl font-bold text-purple-600">
                      {algorithmStatus.lastResult.firstChoiceCount}
                    </div>
                  </div>
                  <div className="bg-indigo-50 p-4 rounded-lg">
                    <div className="text-sm text-indigo-700">Top 3 Choices</div>
                    <div className="text-2xl font-bold text-indigo-600">
                      {algorithmStatus.lastResult.topThreeChoiceCount}
                    </div>
                  </div>
                </div>
                <div className="text-sm text-gray-600">
                  Average choice rank: {algorithmStatus.lastResult.averageChoiceRank.toFixed(2)}
                </div>
                <div className="text-xs text-gray-500">
                  Completed at {new Date(algorithmStatus.lastResult.timestamp).toLocaleString()}
                </div>
              </div>
            ) : (
              <div className="bg-red-50 border border-red-200 rounded-lg p-4">
                <div className="text-red-800 font-semibold">Algorithm Failed</div>
                <div className="text-red-600 text-sm mt-1">
                  {algorithmStatus.lastResult.error || 'Unknown error'}
                </div>
              </div>
            )}
          </div>
        )}

        {/* Log Output */}
        <div className="bg-gray-900 rounded-lg shadow-md p-6">
          <h2 className="text-xl font-semibold text-white mb-4">Algorithm Log Output</h2>
          <div className="bg-black rounded-lg p-4 h-64 overflow-y-auto font-mono text-sm">
            {logs.length === 0 ? (
              <div className="text-gray-500">No logs yet. Run the algorithm to see output.</div>
            ) : (
              logs.map((log, index) => (
                <div key={index} className="text-green-400 mb-1">
                  {log}
                </div>
              ))
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

// Stat Card Component
const StatCard: React.FC<{
  title: string;
  value: number;
  subtitle: string;
  color: 'blue' | 'green' | 'purple';
}> = ({ title, value, subtitle, color }) => {
  const colorClasses = {
    blue: 'bg-blue-50 text-blue-600 border-blue-200',
    green: 'bg-green-50 text-green-600 border-green-200',
    purple: 'bg-purple-50 text-purple-600 border-purple-200',
  };

  return (
    <div className={`rounded-lg border p-6 ${colorClasses[color]}`}>
      <h3 className="text-sm font-medium opacity-80 mb-2">{title}</h3>
      <div className="text-3xl font-bold mb-1">{value}</div>
      <p className="text-sm opacity-70">{subtitle}</p>
    </div>
  );
};

export default AdminPage;
