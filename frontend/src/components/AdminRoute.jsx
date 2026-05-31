import { useContext } from 'react';
import { Navigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';

const AdminRoute = ({ children }) => {
  const { user, loading } = useContext(AuthContext);

  if (loading) {
    return <div className="min-h-screen bg-slate-900"></div>;
  }

  if (!user || user.role !== 'ADMIN') {
    return <Navigate to="/catalog" replace />;
  }

  return children;
};

export default AdminRoute;