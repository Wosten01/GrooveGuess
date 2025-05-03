import { createContext, useContext } from "react";
import { User } from "../api/auth-api";


export interface AuthContextType {
  user: User | null;
  setUser: (user: User | null) => void;
  logout: () => void;
  fetchUser: () => Promise<void>;
  updateUser: (userData: User) => void; 
}

export const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
  return ctx;
}