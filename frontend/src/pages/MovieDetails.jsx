import { useState, useEffect, useContext } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { catalogApi, authApi } from '../api/axiosConfig';
import { AuthContext } from '../context/AuthContext';
import ReviewSection from '../components/ReviewSection';
import toast from 'react-hot-toast';

const MovieDetails = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useContext(AuthContext);

  const [movie, setMovie] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [isFavorite, setIsFavorite] = useState(false);

  useEffect(() => {
    const fetchDetails = async () => {
      try {
        setLoading(true);
        const movieRes = await catalogApi.get(`/movies/${id}`);
        setMovie(movieRes.data);

        const watchlistRes = await authApi.get(`/watchlist?page=0&size=1000`);
        const savedKeys = watchlistRes.data.content.map(w => `${w.mediaType}-${w.mediaId}`);
        setIsFavorite(savedKeys.includes(`movie-${id}`));

      } catch (err) {
        console.error(err);
        setError("No se pudo cargar la información de la película.");
      } finally {
        setLoading(false);
      }
    };
    fetchDetails();
  }, [id]);

  const handleToggleWatchlist = async () => {
    const previousState = isFavorite;
    setIsFavorite(!isFavorite); 
    
    if (previousState) {
      toast.success("Eliminada de Mi Lista", { icon: '💔' });
    } else {
      toast.success("Añadida a Mi Lista", { icon: '❤️' });
    }

    try {
      await authApi.post(`/watchlist/movie/${id}`);
    } catch (err) {
      setIsFavorite(previousState); 
      toast.error("Error al sincronizar con el servidor");
    }
  };

  if (loading) {
    return <div className="min-h-screen bg-slate-900 flex items-center justify-center text-sky-500 text-xl animate-pulse">Cargando detalles...</div>;
  }

  if (error || !movie) {
    return (
      <div className="min-h-screen bg-slate-900 flex flex-col items-center justify-center text-white">
        <h1 className="text-3xl text-red-500 mb-4">Error</h1>
        <p className="text-slate-400 mb-6">{error}</p>
        <button onClick={() => navigate('/catalog')} className="px-6 py-2 bg-sky-500 rounded hover:bg-sky-600 font-bold">Volver al Catálogo</button>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-slate-900 text-white relative">
      
      <div className="absolute top-6 left-6 z-50">
        <button 
          onClick={() => navigate('/catalog')}
          className="flex items-center gap-2 bg-black/40 hover:bg-black/80 backdrop-blur-md text-white px-5 py-2 rounded-full font-bold border border-white/10 transition-all cursor-pointer"
        >
          &larr; Volver
        </button>
      </div>

      <div 
        className="w-full h-[60vh] lg:h-[75vh] bg-cover bg-center bg-no-repeat relative"
        style={{ 
          backgroundImage: `url(${movie.backdropUrl || movie.posterUrl})`,
        }}
      >
        <div className="absolute inset-0 bg-gradient-to-t from-slate-900 via-slate-900/60 to-transparent"></div>
      </div>

      <div className="max-w-6xl mx-auto px-8 relative -mt-32 lg:-mt-64 z-10 pb-20">
        <div className="flex flex-col md:flex-row gap-10">
          
          <div className="hidden md:block shrink-0">
            <img 
              src={movie.posterUrl} 
              alt={movie.title} 
              className="w-72 rounded-xl shadow-2xl shadow-black/80 border border-slate-700"
            />
          </div>

          <div className="flex flex-col justify-end pt-4">
            
            <h1 className="text-5xl lg:text-7xl font-bold text-white drop-shadow-lg mb-4">
              {movie.title}
            </h1>

            <div className="flex flex-wrap items-center gap-4 text-sm font-semibold text-slate-300 mb-8">
              <span className="text-emerald-400 font-bold">{movie.tmdbRating ? `⭐ ${movie.tmdbRating.toFixed(1)}` : '⭐ N/A'}</span>
              <span>{movie.year}</span>
              <span>{movie.duration} min</span>
              {movie.contentRating && (
                <span className="border border-slate-500 px-2 py-0.5 rounded text-slate-400">
                  {movie.contentRating}
                </span>
              )}
            </div>

            <p className="text-lg text-slate-300 leading-relaxed max-w-3xl mb-8 drop-shadow">
              {movie.description}
            </p>

            <div className="flex flex-wrap gap-2 mb-8">
              {movie.genres && movie.genres.map(genre => (
                <span key={genre} className="bg-slate-800/80 border border-slate-600 px-4 py-1 rounded-full text-sm text-sky-300">
                  {genre}
                </span>
              ))}
            </div>

            <div className="flex items-center gap-4 mb-10">
              <button 
                onClick={() => navigate(`/player/${movie.id}`)}
                className="bg-white hover:bg-slate-200 text-black px-8 py-3 rounded-md font-bold text-lg flex items-center gap-3 transition-colors shadow-lg cursor-pointer"
              >
                <span className="text-2xl">▶</span> Reproducir
              </button>
              
              <button 
                onClick={handleToggleWatchlist}
                className="bg-slate-800/80 hover:bg-slate-700 backdrop-blur-sm border border-slate-600 text-white p-3 rounded-md transition-colors shadow-lg cursor-pointer"
                title="Añadir a Mi Lista"
              >
                <span className="text-2xl">{isFavorite ? '❤️' : '🤍'}</span>
              </button>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-6 text-sm border-t border-slate-700/50 pt-6">
              <div>
                <span className="text-slate-500 block mb-1">Director</span>
                <span className="text-white font-medium">{movie.director || 'Desconocido'}</span>
              </div>
              <div>
                <span className="text-slate-500 block mb-1">Reparto Principal</span>
                <span className="text-white font-medium">{movie.castCrew || 'Desconocido'}</span>
              </div>
            </div>

          </div>
        </div>

        <ReviewSection mediaId={movie.id} mediaType="movie" />
        
      </div>

    </div>
  );
};

export default MovieDetails;