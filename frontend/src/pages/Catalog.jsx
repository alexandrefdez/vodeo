import { useContext } from 'react';
import { AuthContext } from '../context/AuthContext';

const Catalog = () => {
  const { logout } = useContext(AuthContext);

  return (
    <div className="min-h-screen bg-slate-900 text-white p-10">
      <div className="flex justify-between items-center mb-10 border-b border-slate-800 pb-4">
        <h1 className="text-3xl font-bold text-sky-500 tracking-widest">VODEO</h1>
        <button 
          onClick={logout}
          className="bg-red-500/10 text-red-500 border border-red-500 hover:bg-red-500 hover:text-white transition-colors px-4 py-2 rounded cursor-pointer font-bold"
        >
          Cerrar Sesión
        </button>
      </div>
      
      <div className="text-center text-slate-400 mt-20">
        <p className="text-xl animate-pulse">Conectando con el catálogo de películas...</p>
      </div>
    </div>
  );
};

export default Catalog;