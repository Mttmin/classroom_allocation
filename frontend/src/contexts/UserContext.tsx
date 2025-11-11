import { createContext, useContext, useState, ReactNode } from 'react';
import { Professor } from '../types';

interface UserContextType {
  professor: Professor | null;
  setProfessor: (professor: Professor | null) => void;
  isAuthenticated: boolean;
  login: (professorId: string) => void;
  logout: () => void;
}

const UserContext = createContext<UserContextType | undefined>(undefined);

export const UserProvider = ({ children }: { children: ReactNode }) => {
  const [professor, setProfessor] = useState<Professor | null>(null);

  const login = (professorId: string) => {
    // Store the professor ID in session storage
    sessionStorage.setItem('professorId', professorId);
  };

  const logout = () => {
    setProfessor(null);
    sessionStorage.removeItem('professorId');
  };

  const isAuthenticated = professor !== null;

  return (
    <UserContext.Provider value={{ professor, setProfessor, isAuthenticated, login, logout }}>
      {children}
    </UserContext.Provider>
  );
};

export const useUser = () => {
  const context = useContext(UserContext);
  if (context === undefined) {
    throw new Error('useUser must be used within a UserProvider');
  }
  return context;
};
