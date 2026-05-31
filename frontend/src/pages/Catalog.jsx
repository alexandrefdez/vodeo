import { useState, useEffect, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import { catalogApi } from '../api/axiosConfig';
import toast from 'react-hot-toast';

const Catalog = () => {
  const { logout, user } = useContext(AuthContext);
  const navigate = useNavigate();
  
  const [movies, setMovies] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [refreshKey, setRefreshKey] = useState(0);

  useEffect(() => {
    const fetchMovies = async () => {
      try {
        setLoading(true);
        const response = await catalogApi.get(`/movies?page=${currentPage}&size=12`);
        
        if (response.data.content.length === 0 && currentPage > 0) {
          setCurrentPage(prev => prev - 1);
        } else {
          setMovies(response.data.content);
          setTotalPages(response.data.totalPages);
        }
        setError(null);
      } catch (err) {
        console.error(err);
        setError("No se pudo cargar el catálogo. Inténtalo más tarde.");
      } finally {
        setLoading(false);
      }
    };

    fetchMovies();
  }, [currentPage, refreshKey]);

  const handleNextPage = () => {
    if (currentPage < totalPages - 1) {
      setCurrentPage(prev => prev + 1);
    }
  };

  const handlePrevPage = () => {
    if (currentPage > 0) {
      setCurrentPage(prev => prev - 1);
    }
  };

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
      
      {loading && movies.length === 0 && (
        <div className="text-center text-slate-400 mt-20">
          <p className="text-xl animate-pulse">Cargando películas...</p>
        </div>
      )}

      {error && (
        <div className="bg-red-500/20 border border-red-500 text-red-200 p-4 rounded text-center">
          {error}
        </div>
      )}

      {!loading && !error && movies.length === 0 && (
        <div className="text-center text-slate-400 mt-20">
          <p className="text-xl">No hay películas en el catálogo todavía.</p>
        </div>
      )}

      {!error && movies.length > 0 && (
        <>
          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 lg:gap-8 gap-6">
            {movies.map((movie) => (
              <div 
                key={movie.id}
                onClick={() => navigate(`/player/${movie.id}`)}
                className="bg-slate-800 rounded-lg overflow-hidden shadow-lg hover:shadow-sky-500/20 hover:scale-105 transition-all duration-300 cursor-pointer border border-slate-700 flex flex-col"
              >
                <div className="h-64 bg-slate-700 flex items-center justify-center overflow-hidden shrink-0">
                  {movie.posterUrl ? (
                    <img 
                      src={movie.posterUrl} 
                      alt={movie.title}
                      className="w-full h-full object-cover"
                    />
                  ) : (
                    <span className="text-slate-500 font-bold">Sin Imagen</span>
                  )}
                </div>
                
                <div className="p-4 flex flex-col flex-grow">
                  <h2 className="text-lg font-bold text-sky-400 mb-1 truncate" title={movie.title}>
                    {movie.title}
                  </h2>
                  <div className="flex justify-between text-xs text-slate-400 mb-3">
                    <span>{movie.year}</span>
                    <span>{movie.duration} min</span>
                  </div>
                  <p className="text-sm text-slate-300 line-clamp-3 mb-4">
                    {movie.description}
                  </p>
                  
                  <div className="mt-auto flex flex-col gap-3">
                    <div className="flex flex-wrap gap-2">
                      {movie.genres && movie.genres.map((genre, index) => (
                        <span key={index} className="bg-slate-700 text-sky-300 text-xs px-2 py-1 rounded-full">
                          {genre}
                        </span>
                      ))}
                    </div>

                    {user?.role === 'ADMIN' && (
                      <button
                        onClick={async (e) => {
                          e.stopPropagation();
                          const confirmDelete = window.confirm(`¿Estás seguro de que quieres borrar "${movie.title}" de forma permanente?`);
                          if (confirmDelete) {
                            const loadingToast = toast.loading("Eliminando película y archivos del servidor...");
                            try {
                              await catalogApi.delete(`/movies/${movie.id}`);
                              setRefreshKey(old => old + 1);
                              toast.dismiss(loadingToast);
                              toast.success("Película eliminada correctamente");
                            } catch (err) {
                              toast.dismiss(loadingToast);
                              toast.error("Error al eliminar la película del sistema");
                            }
                          }
                        }}
                        className="w-full bg-red-500/10 hover:bg-red-500 text-red-500 hover:text-white border border-red-500/50 py-2 rounded font-bold transition-colors text-sm"
                      >
                        Eliminar Película
                      </button>
                    )}
                  </div>
                </div>
              </div>
            ))}
          </div>

          {totalPages > 1 && (
            <div className="flex justify-center items-center gap-6 mt-12">
              <button 
                onClick={handlePrevPage} 
                disabled={currentPage === 0}
                className="px-6 py-2 bg-slate-800 text-white font-bold rounded disabled:opacity-50 disabled:cursor-not-allowed hover:bg-sky-600 transition-colors border border-slate-700"
              >
                &larr; Anterior
              </button>
              
              <span className="text-slate-400 font-semibold">
                Página {currentPage + 1} de {totalPages}
              </span>

              <button 
                onClick={handleNextPage} 
                disabled={currentPage >= totalPages - 1}
                className="px-6 py-2 bg-slate-800 text-white font-bold rounded disabled:opacity-50 disabled:cursor-not-allowed hover:bg-sky-600 transition-colors border border-slate-700"
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

export default Catalog;