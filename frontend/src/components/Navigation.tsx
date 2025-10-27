import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { Professor } from '../types';

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
            </nav>
          </div>
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
        </div>
      </div>
    </header>
  );
};
