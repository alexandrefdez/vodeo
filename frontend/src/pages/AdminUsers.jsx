import { useState, useEffect, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import { authApi } from '../api/axiosConfig';
import { AuthContext } from '../context/AuthContext';
import toast from 'react-hot-toast';

const AdminUsers = () => {
  const navigate = useNavigate();
  const { user } = useContext(AuthContext);

  const [usersList, setUsersList] = useState([]);
  const [loadingUsers, setLoadingUsers] = useState(false);

  useEffect(() => {
    if (!user || user.role !== 'ADMIN') {
      navigate('/catalog');
      return;
    }
    fetchUsers();
  }, [user, navigate]);

  const fetchUsers = async () => {
    setLoadingUsers(true);
    try {
      const res = await authApi.get('/admin/all');
      setUsersList(res.data);
    } catch (error) {
      toast.error("Error al cargar la lista de usuarios");
    } finally {
      setLoadingUsers(false);
    }
  };

  const handleDeleteUser = async (userId, username) => {
    if (!window.confirm(`¿Seguro que quieres ELIMINAR DEFINITIVAMENTE la cuenta de "${username}"? Sus comentarios seguirán existiendo como archivo en el catálogo.`)) return;
    
    const loadingToast = toast.loading("Eliminando usuario...");
    try {
      await authApi.delete(`/admin/${userId}`);
      setUsersList(usersList.filter(u => u.id !== userId));
      toast.dismiss(loadingToast);
      toast.success("Usuario eliminado correctamente");
    } catch (error) {
      toast.dismiss(loadingToast);
      toast.error(error.response?.data?.message || "Error al eliminar usuario");
    }
  };

  return (
    <div className="min-h-screen bg-slate-900 text-white p-10 font-sans">
      
      {/* HEADER NAVEGACIÓN */}
      <div className="flex justify-between items-center mb-10 border-b border-slate-800 pb-4 max-w-6xl mx-auto">
        <h1 className="text-3xl font-bold text-purple-500 tracking-widest">GESTIÓN DE USUARIOS</h1>
        
        <div className="flex gap-4 items-center">
          <button 
            onClick={() => navigate('/admin')}
            className="bg-sky-500/10 text-sky-400 border border-sky-500 hover:bg-sky-500 hover:text-white transition-colors px-4 py-2 rounded cursor-pointer font-bold flex items-center gap-2"
          >
            🎬 Subir Contenido
          </button>
          <button 
            onClick={() => navigate('/admin/dashboard')}
            className="bg-indigo-500/10 text-indigo-400 border border-indigo-500 hover:bg-indigo-500 hover:text-white transition-colors px-4 py-2 rounded cursor-pointer font-bold flex items-center gap-2"
          >
            📊 Ver Estadísticas
          </button>
          <button 
            onClick={() => navigate('/catalog')}
            className="text-slate-400 hover:text-white transition-colors border border-slate-700 px-4 py-2 rounded cursor-pointer font-bold bg-slate-800"
          >
            &larr; Volver al Catálogo
          </button>
        </div>
      </div>

      {/* TABLA DE USUARIOS */}
      <div className="max-w-6xl mx-auto">
        <div className="bg-slate-800 border border-slate-700 p-6 rounded-lg shadow-xl">
          <div className="flex justify-between items-center mb-6 border-b border-slate-700 pb-4">
            <h2 className="text-xl font-bold text-purple-400">Cuentas Registradas</h2>
            <span className="text-slate-400 text-sm">Total: {usersList.length} usuarios</span>
          </div>
          
          <div className="overflow-x-auto custom-scrollbar">
            <table className="w-full text-left border-collapse min-w-[600px]">
              <thead>
                <tr className="border-b border-slate-700 text-slate-400 text-sm uppercase tracking-wider bg-slate-900/50">
                  <th className="p-4 rounded-tl-lg">ID</th>
                  <th className="p-4">Usuario</th>
                  <th className="p-4">Email</th>
                  <th className="p-4">Rol</th>
                  <th className="p-4 text-right rounded-tr-lg">Acciones</th>
                </tr>
              </thead>
              <tbody className="text-slate-300">
                {loadingUsers ? (
                  <tr><td colSpan="5" className="p-10 text-center text-purple-400 animate-pulse font-bold">Cargando base de datos de usuarios...</td></tr>
                ) : usersList.length === 0 ? (
                  <tr><td colSpan="5" className="p-10 text-center italic text-slate-500">No hay usuarios registrados en el sistema.</td></tr>
                ) : (
                  usersList.map((u) => (
                    <tr key={u.id} className="border-b border-slate-700/50 hover:bg-slate-700/30 transition-colors">
                      <td className="p-4 font-mono text-slate-500">#{u.id}</td>
                      <td className="p-4 font-bold text-white flex items-center gap-3">
                        <div className="w-8 h-8 rounded-full bg-gradient-to-br from-purple-500 to-indigo-600 flex items-center justify-center text-xs shadow-md">
                          {u.username.charAt(0).toUpperCase()}
                        </div>
                        {u.username}
                      </td>
                      <td className="p-4 text-slate-400">{u.email}</td>
                      <td className="p-4">
                        <span className={`px-3 py-1 rounded-full text-xs font-bold ${u.role === 'ADMIN' ? 'bg-purple-500/20 text-purple-400 border border-purple-500/30' : 'bg-slate-700 text-slate-300 border border-slate-600'}`}>
                          {u.role}
                        </span>
                      </td>
                      <td className="p-4 text-right">
                        {u.role !== 'ADMIN' ? (
                          <button 
                            onClick={() => handleDeleteUser(u.id, u.username)}
                            className="text-red-400 hover:text-white bg-red-500/10 hover:bg-red-500 border border-red-500/30 px-4 py-2 rounded font-bold transition-all shadow-sm cursor-pointer"
                          >
                            Eliminar Cuenta
                          </button>
                        ) : (
                          <span className="text-slate-500 text-xs italic px-3 border border-slate-700 rounded-full py-1 bg-slate-800/50">Cuenta Protegida</span>
                        )}
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AdminUsers;