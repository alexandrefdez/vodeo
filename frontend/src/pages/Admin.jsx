import { useNavigate } from 'react-router-dom';

const Admin = () => {
  const navigate = useNavigate();

  return (
    <div className="min-h-screen bg-slate-900 text-white p-10">
      <div className="flex justify-between items-center mb-10 border-b border-slate-800 pb-4 max-w-4xl mx-auto">
        <h1 className="text-3xl font-bold text-emerald-500 tracking-widest">PANEL DE CONTROL</h1>
        <button 
          onClick={() => navigate('/catalog')}
          className="text-slate-400 hover:text-white transition-colors border border-slate-700 px-4 py-2 rounded"
        >
          &larr; Volver al Catálogo
        </button>
      </div>

      <div className="bg-slate-800 border border-slate-700 p-8 rounded-lg max-w-4xl mx-auto shadow-xl">
        <h2 className="text-xl font-bold text-sky-400 mb-6">Subir Nueva Película</h2>
        
        <p className="text-slate-400">
          (Aqui va el buscador de TMDB y el formulario de subida del .mp4)
        </p>
      </div>
    </div>
  );
};

export default Admin;