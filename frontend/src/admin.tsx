import React from 'react';
import ReactDOM from 'react-dom/client';
import AdminPage from './components/AdminPage';
import './index.css';

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <div className="min-h-screen bg-gray-50">
      <header className="bg-white shadow-sm border-b border-gray-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
          <div className="flex items-center justify-between">
            <h1 className="text-2xl font-bold text-gray-900">
              C.A.S.T.L.E - Admin Dashboard
            </h1>
            <a
              href="/"
              className="px-4 py-2 rounded-lg font-medium text-gray-700 hover:bg-gray-100 border border-gray-300 transition-colors"
            >
              Back to Professor Portal
            </a>
          </div>
        </div>
      </header>
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <AdminPage />
      </main>
    </div>
  </React.StrictMode>
);
