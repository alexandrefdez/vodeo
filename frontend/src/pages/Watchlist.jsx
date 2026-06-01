import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { catalogApi, authApi } from '../api/axiosConfig';
import toast from 'react-hot-toast';

const Watchlist = () => {
  const navigate = useNavigate();
  const [movies, setMovies] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  useEffect(() => {
    fetchWatchlist();
  }, [currentPage]);

  const fetchWatchlist = async () => {
    try {
      setLoading(true);
      
      // Pedir IDs paginados
      const idsResponse = await authApi.get(`/watchlist?page=${currentPage}&size=12`);
      const movieIds = idsResponse.data.content; 
      
      setTotalPages(idsResponse.data.totalPages);
      setTotalElements(idsResponse.data.totalElements);

      if (!movieIds || movieIds.length === 0) {
        if (currentPage > 0) setCurrentPage(prev => prev - 1);
        else setMovies([]);
        setLoading(false);
        return;
      }

      // Pedir peliculas por GET
      const moviesResponse = await catalogApi.get(`/movies/batch?ids=${movieIds.join(',')}`);
      
      // Restaurar orden original de guardado
      const sortedMovies = moviesResponse.data.sort((a, b) => 
        movieIds.indexOf(a.id) - movieIds.indexOf(b.id)
      );
      
      setMovies(sortedMovies);
      setError(null);
    } catch (err) {
      console.error(err);
      setError("No se pudo cargar tu lista de favoritos.");
    } finally {
      setLoading(false);
    }
  };

  const removeFromWatchlist = async (e, movieId) => {
    e.stopPropagation();
    try {
      await authApi.post(`/watchlist/${movieId}`);
      toast.success("Eliminada de Mi Lista", { icon: '💔' });
      fetchWatchlist(); 
    } catch (err) {
      toast.error("Error al quitar de Mi Lista");
    }
  };

  return (
    <div className="min-h-screen bg-slate-900 text-white p-10">
      <div className="flex justify-between items-center mb-10 border-b border-slate-800 pb-4">
        <div className="flex items-center gap-6">
          <h1 className="text-3xl font-bold text-pink-500 tracking-widest">MI LISTA</h1>
          <span className="bg-pink-500/20 text-pink-400 px-3 py-1 rounded-full text-sm font-bold border border-pink-500/50">
            {totalElements} guardadas
          </span>
        </div>
        <button 
          onClick={() => navigate('/catalog')}
          className="text-slate-400 hover:text-white transition-colors border border-slate-700 px-4 py-2 rounded cursor-pointer font-bold"
        >
          &larr; Volver al Catálogo
        </button>
      </div>

      {loading && movies.length === 0 && (
        <div className="text-center text-slate-400 mt-20">
          <p className="text-xl animate-pulse">Cargando tus favoritos...</p>
        </div>
      )}

      {error && (
        <div className="bg-red-500/20 border border-red-500 text-red-200 p-4 rounded text-center">
          {error}
        </div>
      )}

      {!loading && !error && movies.length === 0 && (
        <div className="text-center text-slate-400 mt-32 bg-slate-800/30 p-16 rounded-lg border border-slate-800 max-w-2xl mx-auto">
          <p className="text-3xl font-bold mb-4 text-slate-300">Aún no tienes favoritos</p>
          <p className="text-slate-500 text-lg mb-8">Explora el catálogo y añade las películas que quieras ver más tarde.</p>
          <button 
            onClick={() => navigate('/catalog')}
            className="bg-pink-500 hover:bg-pink-600 text-white font-bold px-8 py-3 rounded-full transition-colors text-lg cursor-pointer shadow-lg shadow-pink-500/20"
          >
            Explorar películas
          </button>
        </div>
      )}

      {!error && movies.length > 0 && (
        <>
          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 lg:gap-8 gap-6">
            {movies.map((movie) => (
              <div 
                key={movie.id}
                onClick={() => navigate(`/player/${movie.id}`)}
                className="bg-slate-800 rounded-lg overflow-hidden shadow-lg hover:shadow-pink-500/20 hover:scale-105 transition-all duration-300 cursor-pointer border border-slate-700 flex flex-col relative group"
              >
                <button
                  onClick={(e) => removeFromWatchlist(e, movie.id)}
                  className="absolute top-3 right-3 z-10 p-2 rounded-full bg-slate-900/80 hover:bg-red-500/20 backdrop-blur-sm transition-all border border-slate-700 hover:border-red-500"
                  title="Quitar de Mi Lista"
                >
                  <span className="text-pink-500 group-hover:text-red-500 text-xl">✖</span>
                </button>

                <div className="h-64 bg-slate-700 flex items-center justify-center overflow-hidden shrink-0">
                  {movie.posterUrl ? (
                    <img src={movie.posterUrl} alt={movie.title} className="w-full h-full object-cover" />
                  ) : (
                    <span className="text-slate-500 font-bold">Sin Imagen</span>
                  )}
                </div>
                
                <div className="p-4 flex flex-col flex-grow">
                  <h2 className="text-lg font-bold text-pink-400 mb-1 truncate" title={movie.title}>{movie.title}</h2>
                  <div className="flex justify-between text-xs text-slate-400 mb-3">
                    <span>{movie.year}</span>
                    <span>{movie.duration} min</span>
                  </div>
                </div>
              </div>
            ))}
          </div>

          {totalPages > 1 && (
            <div className="flex justify-center items-center gap-6 mt-12">
              <button 
                onClick={() => setCurrentPage(prev => prev - 1)} 
                disabled={currentPage === 0}
                className="px-6 py-2 bg-slate-800 text-white font-bold rounded disabled:opacity-50 disabled:cursor-not-allowed hover:bg-sky-600 transition-colors border border-slate-700 cursor-pointer"
              >
                &larr; Anterior
              </button>
              <span className="text-slate-400 font-semibold">
                Página {currentPage + 1} de {totalPages}
              </span>
              <button 
                onClick={() => setCurrentPage(prev => prev + 1)} 
                disabled={currentPage >= totalPages - 1}
                className="px-6 py-2 bg-slate-800 text-white font-bold rounded disabled:opacity-50 disabled:cursor-not-allowed hover:bg-sky-600 transition-colors border border-slate-700 cursor-pointer"
              >
                Siguiente &rarr;
              </button>
            </div>
          )}
        </>
      )}
    </div>
  );
};

export default Watchlist;