import { useState, useEffect, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import { catalogApi, authApi } from '../api/axiosConfig';
import ContinueWatching from '../components/ContinueWatching';
import Recommendations from '../components/Recommendations';
import toast from 'react-hot-toast';

const Catalog = () => {
  const { logout, user } = useContext(AuthContext);
  const navigate = useNavigate();
  
  const [contentType, setContentType] = useState('movie'); // 'movie' o 'series'

  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [refreshKey, setRefreshKey] = useState(0);

  const [activeGenres, setActiveGenres] = useState([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedGenre, setSelectedGenre] = useState('');
  
  const [watchlistKeys, setWatchlistKeys] = useState([]); // Ahora guarda strings: "movie-1", "series-5"

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
    const fetchWatchlistKeys = async () => {
      try {
        const response = await authApi.get('/watchlist?page=0&size=10000');
        if (response.data && response.data.content) {
          // Mapeamos a claves compuestas para que no colisionen IDs de pelis con series
          setWatchlistKeys(response.data.content.map(w => `${w.mediaType}-${w.mediaId}`));
        } else {
          setWatchlistKeys([]);
        }
      } catch (err) {
        setWatchlistKeys([]);
      }
    };
    if (user) fetchWatchlistKeys();
  }, [user]);

  useEffect(() => {
    setCurrentPage(0);
    setSearchQuery('');
    setSelectedGenre('');
  }, [contentType]);

  useEffect(() => {
    const fetchItems = async () => {
      try {
        setLoading(true);
        const baseEndpoint = contentType === 'movie' ? '/movies' : '/series';
        let url = `${baseEndpoint}?page=${currentPage}&size=12`;

        if (searchQuery) {
          url = `${baseEndpoint}/search?title=${searchQuery}&page=${currentPage}&size=12`;
        } else if (selectedGenre) {
          url = `${baseEndpoint}/genre?genre=${selectedGenre}&page=${currentPage}&size=12`;
        }

        const response = await catalogApi.get(url);
        
        if (response.data.content.length === 0 && currentPage > 0) {
          setCurrentPage(prev => Math.max(0, prev - 1)); 
        } else {
          setItems(response.data.content);
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

    fetchItems();
  }, [currentPage, refreshKey, searchQuery, selectedGenre, contentType]);

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

  const handleToggleWatchlist = async (e, itemId) => {
    e.stopPropagation();
    const itemKey = `${contentType}-${itemId}`;
    const isCurrentlySaved = watchlistKeys?.includes(itemKey);
    const previousKeys = [...(watchlistKeys || [])];
    
    if (isCurrentlySaved) {
      setWatchlistKeys(watchlistKeys.filter(k => k !== itemKey));
      toast.success("Eliminada de Mi Lista", { icon: '💔' });
    } else {
      setWatchlistKeys([...previousKeys, itemKey]);
      toast.success("Añadida a Mi Lista", { icon: '❤️' });
    }

    try {
      await authApi.post(`/watchlist/${contentType}/${itemId}`);
    } catch (err) {
      setWatchlistKeys(previousKeys);
      toast.error("Error de sincronización con el servidor");
    }
  };

  return (
    <div className="min-h-screen bg-slate-900 text-white p-10">
      
      {/* HEADER */}
      <div className="flex justify-between items-center mb-8 border-b border-slate-800 pb-4">
        <h1 className="text-3xl font-bold text-white tracking-widest flex gap-2 items-center">
          VODEO
        </h1>
        
        <div className="flex items-center gap-4">
          {user?.role === 'ADMIN' && (
            <button 
              onClick={() => navigate('/admin')}
              className="flex items-center gap-2 bg-emerald-500/10 text-emerald-400 border border-emerald-500 hover:bg-emerald-500 hover:text-white transition-colors px-4 py-2 rounded cursor-pointer font-bold mr-2"
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
            onClick={() => navigate('/profile')}
            className="flex items-center gap-2 bg-slate-700/50 text-slate-300 border border-slate-600 hover:bg-slate-600 hover:text-white transition-colors px-4 py-2 rounded cursor-pointer font-bold"
          >
            <span>👤</span> Perfil
          </button>
          <button 
            onClick={logout}
            className="bg-red-500/10 text-red-500 border border-red-500 hover:bg-red-500 hover:text-white transition-colors px-4 py-2 rounded cursor-pointer font-bold"
          >
            Cerrar Sesión
          </button>
        </div>
      </div>

      

      {/* MENÚ DE NAVEGACIÓN PRINCIPAL */}
      <div className="flex justify-center mb-10">
        <div className="bg-slate-800 p-1 rounded-full border border-slate-700 inline-flex shadow-xl">
          <button 
            onClick={() => setContentType('movie')}
            className={`px-8 py-3 rounded-full font-bold text-lg transition-all cursor-pointer ${contentType === 'movie' ? 'bg-sky-500 text-white shadow-md' : 'text-slate-400 hover:text-white'}`}
          >
            Películas
          </button>
          <button 
            onClick={() => setContentType('series')}
            className={`px-8 py-3 rounded-full font-bold text-lg transition-all cursor-pointer ${contentType === 'series' ? 'bg-emerald-500 text-white shadow-md' : 'text-slate-400 hover:text-white'}`}
          >
            Series de TV
          </button>
        </div>
      </div>

      {/* FILTROS Y BÚSQUEDA */}
      <div className="flex flex-col md:flex-row justify-between items-center gap-6 mb-10 bg-slate-800/50 p-6 rounded-lg border border-slate-800">
        <form onSubmit={handleSearchSubmit} className="w-full md:w-1/3 flex">
          <input 
            type="text" 
            placeholder={`Buscar ${contentType === 'movie' ? 'película' : 'serie'}...`} 
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className={`w-full p-3 bg-slate-900 border border-slate-700 rounded-l focus:outline-none ${contentType === 'movie' ? 'focus:border-sky-500' : 'focus:border-emerald-500'}`}
          />
          <button 
            type="submit"
            className={`text-white font-bold px-6 rounded-r transition-colors cursor-pointer ${contentType === 'movie' ? 'bg-sky-500 hover:bg-sky-600' : 'bg-emerald-500 hover:bg-emerald-600'}`}
          >
            Buscar
          </button>
        </form>

        <div className="w-full md:w-2/3 overflow-x-auto whitespace-nowrap pb-4 custom-scrollbar">
          <div className="flex gap-3 items-center">
            <span className="text-slate-400 font-semibold mr-2">Categorías:</span>
            <button
              onClick={handleClearFilters}
              className={`px-4 py-2 rounded-full font-semibold transition-colors border cursor-pointer ${!selectedGenre && !searchQuery ? (contentType === 'movie' ? 'bg-sky-500 text-white border-sky-500' : 'bg-emerald-500 text-white border-emerald-500') : 'bg-slate-900 text-slate-300 border-slate-700'}`}
            >
              Todos
            </button>
            {activeGenres.map(genre => (
              <button
                key={genre}
                onClick={() => handleGenreClick(genre)}
                className={`px-4 py-2 rounded-full font-semibold transition-colors border cursor-pointer ${selectedGenre === genre ? (contentType === 'movie' ? 'bg-sky-500 text-white border-sky-500' : 'bg-emerald-500 text-white border-emerald-500') : 'bg-slate-900 text-slate-300 border-slate-700'}`}
              >
                {genre}
              </button>
            ))}
          </div>
        </div>
      </div>
      
      {/* ESTADOS DE CARGA Y ERROR */}
      {loading && items.length === 0 && (
        <div className="text-center text-slate-400 mt-20">
          <p className="text-xl animate-pulse">Cargando catálogo...</p>
        </div>
      )}

      {error && (
        <div className="bg-red-500/20 border border-red-500 text-red-200 p-4 rounded text-center">
          {error}
        </div>
      )}

      {/* ESTADO VACÍO (NO HAY RESULTADOS) */}
      {!loading && !error && items.length === 0 && (
        <div className="text-center text-slate-400 mt-20 bg-slate-800/30 p-10 rounded-lg border border-slate-800 max-w-2xl mx-auto">
          {searchQuery || selectedGenre ? (
            <>
              <p className="text-2xl font-bold mb-2">No hay resultados para tu búsqueda</p>
              <p className="text-slate-500">Prueba a buscar otro título o quitar los filtros de categoría.</p>
              <button 
                onClick={handleClearFilters}
                className={`mt-6 text-white font-bold px-6 py-2 rounded transition-colors cursor-pointer ${contentType === 'movie' ? 'bg-sky-500 hover:bg-sky-600' : 'bg-emerald-500 hover:bg-emerald-600'}`}
              >
                Ver todo el catálogo
              </button>
            </>
          ) : (
            <>
              <p className="text-2xl font-bold mb-2 text-white">🎬 El catálogo está vacío</p>
              <p className="text-slate-400">Aún no hay contenido disponible en esta sección. Vuelve más tarde para descubrir nuevas {contentType === 'movie' ? 'películas' : 'series'}.</p>
            </>
          )}
        </div>
      )}

      {/* GRID DE PELÍCULAS/SERIES */}
      {!error && items.length > 0 && (
        <>
          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 lg:gap-8 gap-6">
            {items.map((item) => {
              const itemKey = `${contentType}-${item.id}`;
              const isFavorite = watchlistKeys?.includes(itemKey);
              return (
                <div 
                  key={item.id} 
                  onClick={() => navigate(`/details/${contentType}/${item.id}`)}
                  className={`bg-slate-800 rounded-lg overflow-hidden shadow-lg hover:scale-105 transition-all duration-300 cursor-pointer border border-slate-700 flex flex-col relative group ${contentType === 'movie' ? 'hover:shadow-sky-500/20' : 'hover:shadow-emerald-500/20'}`}
                >
                  
                  {/* BOTÓN CORAZÓN (AHORA DISPONIBLE PARA SERIES TAMBIÉN) */}
                  <button
                    onClick={(e) => handleToggleWatchlist(e, item.id)}
                    className="absolute top-3 right-3 z-10 p-2 rounded-full bg-slate-900/80 backdrop-blur-sm transition-all border border-slate-700 hover:scale-110 cursor-pointer"
                    title={isFavorite ? "Quitar de Mi Lista" : "Añadir a Mi Lista"}
                  >
                    {isFavorite ? (
                      <span className="text-pink-500 text-xl">❤️</span>
                    ) : (
                      <span className="text-slate-400 hover:text-pink-400 text-xl">🤍</span>
                    )}
                  </button>

                  <div className="h-64 bg-slate-700 flex items-center justify-center overflow-hidden shrink-0">
                    {item.posterUrl ? (
                      <img src={item.posterUrl} alt={item.title} className="w-full h-full object-cover" />
                    ) : (
                      <span className="text-slate-500 font-bold">Sin Imagen</span>
                    )}
                  </div>
                  
                  <div className="p-4 flex flex-col flex-grow">
                    <h2 className={`text-lg font-bold mb-1 truncate ${contentType === 'movie' ? 'text-sky-400' : 'text-emerald-400'}`} title={item.title}>
                      {item.title}
                    </h2>
                    
                    <div className="flex justify-between text-xs text-slate-400 mb-3">
                      <span>{item.year || item.startYear}</span>
                      {item.duration && <span>{item.duration} min</span>}
                    </div>
                    
                    <div className="mt-auto flex flex-col gap-3">
                      <div className="flex flex-wrap gap-2">
                        {item.genres && item.genres.map((genre, index) => (
                          <span 
                            key={index} 
                            className={`bg-slate-700 text-xs px-2 py-1 rounded-full ${contentType === 'movie' ? 'text-sky-300' : 'text-emerald-300'}`}
                          >
                            {genre}
                          </span>
                        ))}
                      </div>

                      {user?.role === 'ADMIN' && (
                        <button 
                          onClick={async (e) => {
                            e.stopPropagation();
                            if (window.confirm(`¿Seguro que quieres borrar "${item.title}"?`)) {
                              const loadingToast = toast.loading(`Eliminando...`);
                              try {
                                await catalogApi.delete(`/${contentType === 'movie' ? 'movies' : 'series'}/${item.id}`);
                                setRefreshKey(old => old + 1);
                                toast.dismiss(loadingToast);
                                toast.success("Eliminada correctamente");
                              } catch (err) {
                                toast.dismiss(loadingToast);
                                toast.error("Error al eliminar del sistema");
                              }
                            }
                          }} 
                          className="w-full bg-red-500/10 hover:bg-red-500 text-red-500 hover:text-white border border-red-500/50 py-2 rounded font-bold transition-colors text-sm cursor-pointer"
                        >
                          Eliminar
                        </button>
                      )}
                    </div>
                  </div>
                </div>
              );
            })}
          </div>

           {/* CONTINUAR VIENDO Y RECOMENDACIONES */}
          {!searchQuery && !selectedGenre && currentPage === 0 && (
            <div className="mt-16 border-t border-slate-800 pt-8">
              <ContinueWatching />
              {/* Recomendaciones */}
              <Recommendations type={contentType} />
            </div>
          )}

          {totalPages > 1 && (
            <div className="flex justify-center items-center gap-6 mt-12">
              <button 
                onClick={() => setCurrentPage(prev => prev - 1)} 
                disabled={currentPage === 0}
                className="px-6 py-2 bg-slate-800 text-white font-bold rounded disabled:opacity-50 disabled:cursor-not-allowed hover:bg-slate-700 transition-colors border border-slate-700 cursor-pointer"
              >
                &larr; Anterior
              </button>
              <span className="text-slate-400 font-semibold">
                Página {currentPage + 1} de {totalPages}
              </span>
              <button 
                onClick={() => setCurrentPage(prev => prev + 1)} 
                disabled={currentPage >= totalPages - 1} 
                className="px-6 py-2 bg-slate-800 text-white font-bold rounded disabled:opacity-50 disabled:cursor-not-allowed hover:bg-slate-700 transition-colors border border-slate-700 cursor-pointer"
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