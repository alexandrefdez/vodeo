import { useState, useEffect, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import { catalogApi, authApi } from '../api/axiosConfig';
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

  const [activeGenres, setActiveGenres] = useState([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedGenre, setSelectedGenre] = useState('');
  
  const [watchlistIds, setWatchlistIds] = useState([]);

  useEffect(() => {
    const fetchGenres = async () => {
      try {
        const response = await catalogApi.get('/genres');
        setActiveGenres(response.data);
      } catch (err) {
        console.error(err);
      }
    };
    fetchGenres();
  }, []);

  useEffect(() => {
    const fetchWatchlistIds = async () => {
      try {
        // Obtenemos todos los IDs
        const response = await authApi.get('/watchlist?page=0&size=10000');
        
        if (response.data && response.data.content) {
          setWatchlistIds(response.data.content);
        } else {
          setWatchlistIds([]);
        }
      } catch (err) {
        console.error("Error al cargar favoritos", err);
        setWatchlistIds([]);
      }
    };
    if (user) fetchWatchlistIds();
  }, [user]);

  useEffect(() => {
    const fetchMovies = async () => {
      try {
        setLoading(true);
        let url = `/movies?page=${currentPage}&size=12`;

        if (searchQuery) {
          url = `/movies/search?title=${searchQuery}&page=${currentPage}&size=12`;
        } else if (selectedGenre) {
          url = `/movies/genre?genre=${selectedGenre}&page=${currentPage}&size=12`;
        }

        const response = await catalogApi.get(url);
        
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
  }, [currentPage, refreshKey, searchQuery, selectedGenre]);

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    setSelectedGenre('');
    setCurrentPage(0);
  };

  const handleGenreClick = (genre) => {
    setSearchQuery('');
    setSelectedGenre(genre);
    setCurrentPage(0);
  };

  const handleClearFilters = () => {
    setSearchQuery('');
    setSelectedGenre('');
    setCurrentPage(0);
  };

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

  const handleToggleWatchlist = async (e, movieId) => {
    e.stopPropagation();
    
    // El ? asegura que si watchlistIds es undefined, no explote el programa
    const isCurrentlySaved = watchlistIds?.includes(movieId);
    const previousIds = [...(watchlistIds || [])];
    
    if (isCurrentlySaved) {
      setWatchlistIds(watchlistIds.filter(id => id !== movieId));
      toast.success("Eliminada de Mi Lista", { icon: '💔' });
    } else {
      setWatchlistIds([...previousIds, movieId]);
      toast.success("Añadida a Mi Lista", { icon: '❤️' });
    }

    try {
      await authApi.post(`/watchlist/${movieId}`);
    } catch (err) {
      setWatchlistIds(previousIds);
      toast.error("Error de sincronización con el servidor");
    }
  };

  return (
    <div className="min-h-screen bg-slate-900 text-white p-10">
      
      {/* HEADER */}
      <div className="flex justify-between items-center mb-8 border-b border-slate-800 pb-4">
        <h1 className="text-3xl font-bold text-sky-500 tracking-widest">VODEO</h1>
        
        <div className="flex items-center gap-4">
          {user?.role === 'ADMIN' && (
            <button 
              onClick={() => navigate('/admin')}
              className="text-emerald-500 hover:text-emerald-400 transition-colors font-bold mr-2 cursor-pointer"
            >
              Panel Admin
            </button>
          )}
          
          <button 
            onClick={() => navigate('/watchlist')}
            className="flex items-center gap-2 bg-pink-500/10 text-pink-400 border border-pink-500 hover:bg-pink-500 hover:text-white transition-colors px-4 py-2 rounded cursor-pointer font-bold"
          >
            <span>❤️</span> Mi Lista
          </button>
          
          <button 
            onClick={logout}
            className="bg-red-500/10 text-red-500 border border-red-500 hover:bg-red-500 hover:text-white transition-colors px-4 py-2 rounded cursor-pointer font-bold"
          >
            Cerrar Sesión
          </button>
        </div>
      </div>

      {/* FILTROS Y BÚSQUEDA */}
      <div className="flex flex-col md:flex-row justify-between items-center gap-6 mb-10 bg-slate-800/50 p-6 rounded-lg border border-slate-800">
        
        <form onSubmit={handleSearchSubmit} className="w-full md:w-1/3 flex">
          <input 
            type="text" 
            placeholder="Buscar película por título..." 
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full p-3 bg-slate-900 border border-slate-700 rounded-l focus:outline-none focus:border-sky-500"
          />
          <button 
            type="submit"
            className="bg-sky-500 hover:bg-sky-600 text-white font-bold px-6 rounded-r transition-colors cursor-pointer"
          >
            Buscar
          </button>
        </form>

        <div className="w-full md:w-2/3 overflow-x-auto whitespace-nowrap pb-4 custom-scrollbar">
          <div className="flex gap-3 items-center">
            <span className="text-slate-400 font-semibold mr-2">Categorías:</span>
            <button
              onClick={handleClearFilters}
              className={`px-4 py-2 rounded-full font-semibold transition-colors border cursor-pointer ${!selectedGenre && !searchQuery ? 'bg-sky-500 text-white border-sky-500' : 'bg-slate-900 text-slate-300 border-slate-700 hover:border-sky-500/50'}`}
            >
              Todos
            </button>
            {activeGenres.map(genre => (
              <button
                key={genre}
                onClick={() => handleGenreClick(genre)}
                className={`px-4 py-2 rounded-full font-semibold transition-colors border cursor-pointer ${selectedGenre === genre ? 'bg-sky-500 text-white border-sky-500' : 'bg-slate-900 text-slate-300 border-slate-700 hover:border-sky-500/50'}`}
              >
                {genre}
              </button>
            ))}
          </div>
        </div>
      </div>
      
      {/* ESTADOS DE CARGA Y ERROR */}
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

      {/* ESTADO VACÍO (NO HAY RESULTADOS) */}
      {!loading && !error && movies.length === 0 && (
        <div className="text-center text-slate-400 mt-20 bg-slate-800/30 p-10 rounded-lg border border-slate-800">
          <p className="text-2xl font-bold mb-2">No hay resultados</p>
          <p className="text-slate-500">Prueba a buscar otro título o cambiar de categoría.</p>
          <button 
            onClick={handleClearFilters}
            className="mt-6 bg-sky-500 hover:bg-sky-600 text-white font-bold px-6 py-2 rounded transition-colors cursor-pointer"
          >
            Ver todo el catálogo
          </button>
        </div>
      )}

      {/* GRID DE PELÍCULAS */}
      {!error && movies.length > 0 && (
        <>
          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 lg:gap-8 gap-6">
            {movies.map((movie) => {
              const isFavorite = watchlistIds?.includes(movie.id);
              return (
                <div 
                  key={movie.id} 
                  onClick={() => navigate(`/player/${movie.id}`)} 
                  className="bg-slate-800 rounded-lg overflow-hidden shadow-lg hover:shadow-sky-500/20 hover:scale-105 transition-all duration-300 cursor-pointer border border-slate-700 flex flex-col relative group"
                >
                  
                  {/* BOTÓN CORAZÓN (OPTIMISTIC UI) */}
                  <button
                    onClick={(e) => handleToggleWatchlist(e, movie.id)}
                    className="absolute top-3 right-3 z-10 p-2 rounded-full bg-slate-900/80 backdrop-blur-sm transition-all border border-slate-700 hover:scale-110 cursor-pointer"
                    title={isFavorite ? "Quitar de Mi Lista" : "Añadir a Mi Lista"}
                  >
                    {isFavorite ? (
                      <span className="text-pink-500 text-xl">❤️</span>
                    ) : (
                      <span className="text-slate-400 hover:text-pink-400 text-xl">🤍</span>
                    )}
                  </button>

                  {/* IMAGEN DE LA PELÍCULA */}
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
                  
                  {/* INFORMACIÓN DE LA PELÍCULA */}
                  <div className="p-4 flex flex-col flex-grow">
                    <h2 className="text-lg font-bold text-sky-400 mb-1 truncate" title={movie.title}>
                      {movie.title}
                    </h2>
                    
                    <div className="flex justify-between text-xs text-slate-400 mb-3">
                      <span>{movie.year}</span>
                      <span>{movie.duration} min</span>
                    </div>
                    
                    <div className="mt-auto flex flex-col gap-3">
                      
                      {/* ETIQUETAS DE GÉNEROS */}
                      <div className="flex flex-wrap gap-2">
                        {movie.genres && movie.genres.map((genre, index) => (
                          <span 
                            key={index} 
                            className="bg-slate-700 text-sky-300 text-xs px-2 py-1 rounded-full"
                          >
                            {genre}
                          </span>
                        ))}
                      </div>

                      {/* BOTÓN DE BORRAR (SOLO ADMIN) */}
                      {user?.role === 'ADMIN' && (
                        <button 
                          onClick={async (e) => {
                            e.stopPropagation();
                            if (window.confirm(`¿Estás seguro de que quieres borrar "${movie.title}" de forma permanente?`)) {
                              const loadingToast = toast.loading("Eliminando película...");
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
                          className="w-full bg-red-500/10 hover:bg-red-500 text-red-500 hover:text-white border border-red-500/50 py-2 rounded font-bold transition-colors text-sm cursor-pointer"
                        >
                          Eliminar Película
                        </button>
                      )}
                    </div>
                  </div>
                </div>
              );
            })}
          </div>

          {/* CONTROLES DE PAGINACIÓN */}
          {totalPages > 1 && (
            <div className="flex justify-center items-center gap-6 mt-12">
              <button 
                onClick={handlePrevPage} 
                disabled={currentPage === 0}
                className="px-6 py-2 bg-slate-800 text-white font-bold rounded disabled:opacity-50 disabled:cursor-not-allowed hover:bg-sky-600 transition-colors border border-slate-700 cursor-pointer"
              >
                &larr; Anterior
              </button>
              
              <span className="text-slate-400 font-semibold">
                Página {currentPage + 1} de {totalPages}
              </span>
              
              <button 
                onClick={handleNextPage} 
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

export default Catalog;