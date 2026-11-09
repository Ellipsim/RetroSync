import {
  createContext,
  useContext,
  useState,
  type ReactNode,
} from 'react';
import type { User } from '../types';

interface AuthContextType {
  user: User | null;
  setUser: (user: User | null) => void;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUserState] = useState<User | null>(() => {
    const storedUser = sessionStorage.getItem('retrosync_user');
    return storedUser ? JSON.parse(storedUser) as User : null;
  });

  function setUser(newUser: User | null) {
    setUserState(newUser);

    if (newUser) {
      sessionStorage.setItem('retrosync_user', JSON.stringify(newUser));
    } else {
      sessionStorage.removeItem('retrosync_user');
    }
  }

  function logout() {
    setUser(null);
  }

  return (
    <AuthContext.Provider value={{ user, setUser, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error('useAuth must be used inside AuthProvider');
  }

  return context;
}