import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { Professor } from '../types';
import { useUser } from '../contexts/UserContext';

interface NavigationProps {
  professor: Professor | null;
  onSave: () => void;
  saving: boolean;
  disabled?: boolean;
}

export const Navigation: React.FC<NavigationProps> = ({
  professor,
  onSave,
  saving,
  disabled = false,
}) => {
  const location = useLocation();
  const { logout } = useUser();

  const isActive = (path: string) => {
    return location.pathname === path;
  };

  return (
    <header className="bg-white shadow-sm border-b border-gray-200">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-8">
            <div>
              <h1 className="text-2xl font-bold text-gray-900">
                C.A.S.T.L.E - Professor Portal
              </h1>
              {professor && (
                <p className="text-sm text-gray-600 mt-1">
                  Welcome, {professor.name}
                </p>
              )}
            </div>
            <nav className="flex space-x-4">
              <Link
                to="/"
                className={`
                  px-3 py-2 rounded-md text-sm font-medium transition-colors
                  ${
                    isActive('/')
                      ? 'bg-blue-100 text-blue-700'
                      : 'text-gray-600 hover:bg-gray-100 hover:text-gray-900'
                  }
                `}
              >
                Room Preferences
              </Link>
              <Link
                to="/profile"
                className={`
                  px-3 py-2 rounded-md text-sm font-medium transition-colors
                  ${
                    isActive('/profile')
                      ? 'bg-blue-100 text-blue-700'
                      : 'text-gray-600 hover:bg-gray-100 hover:text-gray-900'
                  }
                `}
              >
                Profile
              </Link>
              <Link
                to="/allocation"
                className={`
                  px-3 py-2 rounded-md text-sm font-medium transition-colors
                  ${
                    isActive('/allocation')
                      ? 'bg-blue-100 text-blue-700'
                      : 'text-gray-600 hover:bg-gray-100 hover:text-gray-900'
                  }
                `}
              >
                Allocation Results
              </Link>
            </nav>
          </div>
          <div className="flex items-center space-x-3">
            <a
              href="/admin.html"
              target="_blank"
              rel="noopener noreferrer"
              className="px-4 py-2 rounded-lg font-medium text-purple-700 hover:bg-purple-50 border border-purple-300 transition-colors flex items-center space-x-1"
            >
              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" />
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
              </svg>
              <span>Admin</span>
            </a>
            <button
              onClick={onSave}
              disabled={saving || disabled}
              className={`
                px-6 py-2 rounded-lg font-medium transition-colors
                ${
                  saving || disabled
                    ? 'bg-gray-400 cursor-not-allowed text-gray-200'
                    : 'bg-blue-600 hover:bg-blue-700 text-white'
                }
              `}
            >
              {saving ? 'Saving...' : 'Save Preferences'}
            </button>
            <button
              onClick={logout}
              className="px-4 py-2 rounded-lg font-medium text-gray-700 hover:bg-gray-100 border border-gray-300 transition-colors"
            >
              Logout
            </button>
          </div>
        </div>
      </div>
    </header>
  );
};
