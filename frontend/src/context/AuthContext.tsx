import React, { useState, useEffect } from "react";
import { apiGet } from "../api/api";
import { logoutUser, User } from "../api/auth-api";
import { AuthContext } from "../hooks/auth-context";



export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);

  const fetchUser = async () => {
    try {
      const response = await apiGet<User>("/auth/me");
      setUser({
        ...response.data,
      });
    } catch {
      setUser(null);
    }
  };

  const logout = async () => {
      try {
        await logoutUser();
      } catch  {
        // Error
      }
      setUser(null);
  };

  useEffect(() => {
    fetchUser();
  }, []);

  const updateUser = (userData: User) => {
    setUser(userData);
  };

  return (
    <AuthContext.Provider value={{ user, setUser, logout, fetchUser, updateUser}}>
      {children}
    </AuthContext.Provider>
  );
};

