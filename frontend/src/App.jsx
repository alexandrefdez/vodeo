import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import Login from './pages/Login';
import Register from './pages/Register';
import Catalog from './pages/Catalog';
import Player from './pages/Player';
import Admin from './pages/Admin';
import PrivateRoute from './components/PrivateRoute';
import AdminRoute from './components/AdminRoute';

function App() {
  return (
    <AuthProvider>
      <Router>
        <Routes>
          {/* Rutas Públicas */}
          <Route path="/" element={<Navigate to="/login" replace />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          
          {/* Rutas Privadas Comunes */}
          <Route 
            path="/catalog" 
            element={
              <PrivateRoute>
                <Catalog />
              </PrivateRoute>
            } 
          />
          <Route 
            path="/player/:id" 
            element={
              <PrivateRoute>
                <Player />
              </PrivateRoute>
            } 
          />

          {/* Rutas de Administración */}
          <Route 
            path="/admin" 
            element={
              <AdminRoute>
                <Admin />
              </AdminRoute>
            } 
          />
        </Routes>
      </Router>
    </AuthProvider>
  );
}

export default App;