import { createContext, useState } from 'react';

// Creamos la caja fuerte vacía
export const AuthContext = createContext();

// Creamos el proveedor
export const AuthProvider = ({ children }) => {
  
  const [token, setToken] = useState(localStorage.getItem('token') || null);

  const login = (newToken) => {
    setToken(newToken);
    localStorage.setItem('token', newToken);
  };

  const logout = () => {
    setToken(null);
    localStorage.removeItem('token'); // Lo borra del navegador
  };

  return (
    <AuthContext.Provider value={{ token, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};