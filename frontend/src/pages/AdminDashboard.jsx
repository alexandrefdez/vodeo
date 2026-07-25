import { useState, useEffect, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import { catalogApi, authApi } from '../api/axiosConfig';
import { AuthContext } from '../context/AuthContext';

const AdminDashboard = () => {
  const navigate = useNavigate();
  const { user } = useContext(AuthContext);

  const [stats, setStats] = useState({ users: 0, movies: 0, series: 0 });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!user || user.role !== 'ADMIN') {
      navigate('/catalog');
      return;
    }

    const fetchStats = async () => {
      try {
        const [usersRes, moviesRes, seriesRes] = await Promise.all([
          authApi.get('/count'),
          catalogApi.get('/movies?page=0&size=1'),
          catalogApi.get('/series?page=0&size=1')
        ]);
        setStats({
          users: usersRes.data,
          movies: moviesRes.data.totalElements,
          series: seriesRes.data.totalElements
        });
      } catch (err) {
        console.error("Error cargando estadísticas del dashboard", err);
      } finally {
        setLoading(false);
      }
    };
    fetchStats();
  }, [user, navigate]);

  return (
    <div className="min-h-screen bg-slate-900 text-white p-10 font-sans">
      
      {/* HEADER PRINCIPAL */}
      <div className="flex justify-between items-center mb-12 border-b border-slate-800 pb-4 max-w-6xl mx-auto">
        <div className="flex items-center gap-4">
          <h1 className="text-3xl font-bold text-indigo-500 tracking-widest">DASHBOARD</h1>
          <span className="bg-indigo-500/20 text-indigo-400 px-3 py-1 rounded-full text-xs font-bold border border-indigo-500/50 uppercase">
            Métricas
          </span>
        </div>
        
        <div className="flex gap-4">
          <button 
            onClick={() => navigate('/admin')}
            className="bg-sky-500/10 text-sky-400 border border-sky-500 hover:bg-sky-500 hover:text-white transition-colors px-4 py-2 rounded cursor-pointer font-bold flex items-center gap-2"
          >
            🎬 Subir Contenido
          </button>
          <button 
            onClick={() => navigate('/admin/users')}
            className="bg-purple-500/10 text-purple-400 border border-purple-500 hover:bg-purple-500 hover:text-white transition-colors px-4 py-2 rounded cursor-pointer font-bold flex items-center gap-2"
          >
            👥 Usuarios
          </button>
          <button 
            onClick={() => navigate('/catalog')}
            className="text-slate-400 hover:text-white transition-colors border border-slate-700 px-4 py-2 rounded cursor-pointer font-bold"
          >
            &larr; Volver al Catálogo
          </button>
        </div>
      </div>

      {loading ? (
        <div className="text-center text-indigo-500 mt-20 animate-pulse text-xl font-bold">
          Calculando métricas del servidor...
        </div>
      ) : (
        <div className="max-w-6xl mx-auto grid grid-cols-1 md:grid-cols-3 gap-8">
          
          <div className="bg-slate-800 border border-slate-700 p-10 rounded-xl shadow-2xl flex items-center justify-between relative overflow-hidden group">
            <div className="absolute top-0 right-0 w-32 h-32 bg-indigo-500/10 rounded-bl-full -mr-10 -mt-10 transition-transform group-hover:scale-110"></div>
            <div className="z-10">
              <p className="text-slate-400 text-sm font-bold uppercase tracking-widest mb-2">Total Usuarios</p>
              <p className="text-6xl font-black text-white drop-shadow-md">{stats.users}</p>
            </div>
            <div className="text-7xl opacity-20 z-10 group-hover:text-indigo-400 transition-colors">👤</div>
          </div>

          <div className="bg-slate-800 border border-slate-700 p-10 rounded-xl shadow-2xl flex items-center justify-between relative overflow-hidden group">
            <div className="absolute top-0 right-0 w-32 h-32 bg-sky-500/10 rounded-bl-full -mr-10 -mt-10 transition-transform group-hover:scale-110"></div>
            <div className="z-10">
              <p className="text-slate-400 text-sm font-bold uppercase tracking-widest mb-2">Películas</p>
              <p className="text-6xl font-black text-sky-400 drop-shadow-md">{stats.movies}</p>
            </div>
            <div className="text-7xl opacity-20 z-10 group-hover:text-sky-400 transition-colors">🎬</div>
          </div>

          <div className="bg-slate-800 border border-slate-700 p-10 rounded-xl shadow-2xl flex items-center justify-between relative overflow-hidden group">
            <div className="absolute top-0 right-0 w-32 h-32 bg-emerald-500/10 rounded-bl-full -mr-10 -mt-10 transition-transform group-hover:scale-110"></div>
            <div className="z-10">
              <p className="text-slate-400 text-sm font-bold uppercase tracking-widest mb-2">Series de TV</p>
              <p className="text-6xl font-black text-emerald-400 drop-shadow-md">{stats.series}</p>
            </div>
            <div className="text-7xl opacity-20 z-10 group-hover:text-emerald-400 transition-colors">📺</div>
          </div>
          
        </div>
      )}
    </div>
  );
};

export default AdminDashboard;