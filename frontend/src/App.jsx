import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import { AuthProvider } from './context/AuthContext';
import Login from './pages/Login';
import Register from './pages/Register';
import Catalog from './pages/Catalog';
import Player from './pages/Player';
import Admin from './pages/Admin';
import Watchlist from './pages/Watchlist';
import Profile from './pages/Profile';
import MovieDetails from './pages/MovieDetails';
import PrivateRoute from './components/PrivateRoute';
import AdminRoute from './components/AdminRoute';

function App() {
  return (
    <AuthProvider>
      <Toaster 
        position="bottom-right"
        toastOptions={{
          style: {
            background: '#1e293b',
            color: '#fff',
            border: '1px solid #334155',
          },
          success: {
            iconTheme: { primary: '#10b981', secondary: '#fff' },
          },
          error: {
            iconTheme: { primary: '#ef4444', secondary: '#fff' },
          },
        }}
      />
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
          <Route 
            path="/details/movie/:id" 
            element={
              <PrivateRoute>
                <MovieDetails />
              </PrivateRoute>
            } 
          />
          <Route 
            path="/watchlist"  
            element=
              {<PrivateRoute>
                <Watchlist />
              </PrivateRoute>
            } 
            />
            <Route 
            path="/profile"  
            element={
              <PrivateRoute>
                <Profile />
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