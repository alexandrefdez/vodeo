import { useState, useEffect, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import { authApi } from '../api/axiosConfig';
import { AuthContext } from '../context/AuthContext';
import toast from 'react-hot-toast';

const Profile = () => {
  const navigate = useNavigate();
  const { logout, user } = useContext(AuthContext);

  const [loading, setLoading] = useState(true);
  const [initialData, setInitialData] = useState({ username: '' });
  const [formData, setFormData] = useState({
    username: '',
    email: '', // El email solo se muestra, no se edita
    currentPassword: '',
    newPassword: ''
  });

  // Cargar datos actuales del usuario
  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const response = await authApi.get('/profile');
        setFormData(prev => ({
          ...prev,
          username: response.data.username,
          email: response.data.email
        }));
        setInitialData({ username: response.data.username });
      } catch (err) {
        toast.error("Error al cargar los datos del perfil");
      } finally {
        setLoading(false);
      }
    };
    fetchProfile();
  }, []);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    // Validacion Frontend
    if (formData.newPassword && !formData.currentPassword) {
      toast.error("Debes introducir tu contraseña actual para cambiarla.");
      return;
    }

    const updateToast = toast.loading("Actualizando perfil...");

    try {
      await authApi.put('/profile', {
        username: formData.username,
        currentPassword: formData.currentPassword,
        newPassword: formData.newPassword
      });

      toast.dismiss(updateToast);
      toast.success("¡Perfil actualizado con éxito!");
      
      setInitialData({ username: formData.username });
      
      // Limpiamos los campos de contraseña por seguridad
      setFormData(prev => ({
        ...prev,
        currentPassword: '',
        newPassword: ''
      }));

    } catch (err) {
      toast.dismiss(updateToast);
      toast.error("Error al actualizar. Comprueba tu contraseña actual.");
    }
  };

  const isUsernameChanged = formData.username.trim() !== initialData.username;
  const isPasswordReady = formData.currentPassword.length > 0 && formData.newPassword.length >= 3;
  const canSubmit = isUsernameChanged || isPasswordReady;

  if (loading) {
    return <div className="min-h-screen bg-slate-900 flex items-center justify-center text-sky-500 text-xl animate-pulse">Cargando perfil...</div>;
  }

  return (
    <div className="min-h-screen bg-slate-900 text-white p-10">
      
      {/* HEADER */}
      <div className="flex justify-between items-center mb-10 border-b border-slate-800 pb-4 max-w-3xl mx-auto">
        <div className="flex items-center gap-4">
          <h1 className="text-3xl font-bold text-sky-500 tracking-widest">MI PERFIL</h1>
          <span className="bg-sky-500/20 text-sky-400 px-3 py-1 rounded-full text-xs font-bold border border-sky-500/50 uppercase">
            {user?.role}
          </span>
        </div>
        <button 
          onClick={() => navigate('/catalog')}
          className="text-slate-400 hover:text-white transition-colors border border-slate-700 px-4 py-2 rounded cursor-pointer font-bold"
        >
          &larr; Volver
        </button>
      </div>

      <div className="max-w-3xl mx-auto bg-slate-800 border border-slate-700 p-8 rounded-lg shadow-xl">
        <form onSubmit={handleSubmit} className="flex flex-col gap-6">
          
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label className="block text-sm font-bold text-slate-400 mb-2">Correo Electrónico (No editable)</label>
              <input 
                type="text" 
                value={formData.email} 
                disabled 
                className="w-full p-3 bg-slate-900/50 border border-slate-700 rounded text-slate-500 cursor-not-allowed"
              />
            </div>
            
            <div>
              <label className="block text-sm font-bold text-slate-400 mb-2">Nombre de Usuario</label>
              <input 
                type="text" 
                name="username"
                value={formData.username} 
                onChange={handleInputChange}
                className="w-full p-3 bg-slate-900 border border-slate-700 rounded focus:outline-none focus:border-sky-500 text-white"
                required
              />
            </div>
          </div>

          <hr className="border-slate-700 my-4" />
          
          <h2 className="text-xl font-bold text-slate-300">Cambiar Contraseña</h2>
          <p className="text-sm text-slate-500 -mt-4">Rellena estos campos solo si deseas cambiar tu contraseña actual.</p>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label className="block text-sm font-bold text-slate-400 mb-2">Contraseña Actual</label>
              <input 
                type="password" 
                name="currentPassword"
                placeholder="••••••••"
                value={formData.currentPassword} 
                onChange={handleInputChange}
                className="w-full p-3 bg-slate-900 border border-slate-700 rounded focus:outline-none focus:border-sky-500 text-white"
              />
            </div>
            
            <div>
              <label className="block text-sm font-bold text-slate-400 mb-2">Nueva Contraseña</label>
              <input 
                type="password" 
                name="newPassword"
                placeholder="Nueva contraseña"
                value={formData.newPassword} 
                onChange={handleInputChange}
                className="w-full p-3 bg-slate-900 border border-slate-700 rounded focus:outline-none focus:border-sky-500 text-white"
                minLength="3"
              />
            </div>
          </div>

          <div className="flex justify-between items-center mt-8">
            <button 
              type="button" 
              onClick={logout}
              className="text-red-500 hover:text-white bg-red-500/10 hover:bg-red-500 border border-red-500/50 px-6 py-3 rounded font-bold transition-colors cursor-pointer"
            >
              Cerrar Sesión
            </button>

            <button 
              type="submit" 
              disabled={!canSubmit}
              className={`px-8 py-3 rounded shadow-lg font-bold transition-colors cursor-pointer ${
                canSubmit 
                  ? 'bg-sky-500 hover:bg-sky-600 text-white' 
                  : 'bg-slate-700 text-slate-500 cursor-not-allowed opacity-50'
              }`}
            >
              Guardar Cambios
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default Profile;