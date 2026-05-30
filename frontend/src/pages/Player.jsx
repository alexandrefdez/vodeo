import { useParams, useNavigate } from 'react-router-dom';

const Player = () => {
  const { id } = useParams();
  const navigate = useNavigate();

  return (
    <div className="min-h-screen bg-black text-white flex flex-col items-center justify-center">
      <button 
        onClick={() => navigate('/catalog')}
        className="absolute top-6 left-6 text-slate-400 hover:text-white transition-colors"
      >
        &larr; Volver al catálogo
      </button>
      
      <h1 className="text-2xl text-slate-500">
        Aquí va el video de la pelicula con ID: <span className="text-sky-500 font-bold">{id}</span>
      </h1>
    </div>
  );
};

export default Player;