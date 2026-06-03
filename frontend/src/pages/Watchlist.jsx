import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { catalogApi, authApi } from '../api/axiosConfig';
import toast from 'react-hot-toast';

const Watchlist = () => {
  const navigate = useNavigate();
  const [items, setItems] = useState([]);
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
      
      const idsResponse = await authApi.get(`/watchlist?page=${currentPage}&size=12`);
      const watchlistItems = idsResponse.data.content; // Ahora son objetos { mediaId, mediaType }
      
      setTotalPages(idsResponse.data.totalPages);
      setTotalElements(idsResponse.data.totalElements);

      if (!watchlistItems || watchlistItems.length === 0) {
        if (currentPage > 0) setCurrentPage(prev => prev - 1);
        else setItems([]);
        setLoading(false);
        return;
      }

      // Separar IDs por tipo
      const movieIds = watchlistItems.filter(i => i.mediaType === 'movie').map(i => i.mediaId);
      const seriesIds = watchlistItems.filter(i => i.mediaType === 'series').map(i => i.mediaId);

      // Lanzar peticiones en paralelo
      const [moviesRes, seriesRes] = await Promise.all([
        movieIds.length > 0 ? catalogApi.get(`/movies/batch?ids=${movieIds.join(',')}`) : Promise.resolve({ data: [] }),
        seriesIds.length > 0 ? catalogApi.get(`/series/batch?ids=${seriesIds.join(',')}`) : Promise.resolve({ data: [] })
      ]);
      
      // Inyectar a cada respuesta su mediaType para saber pintarlo luego
      const combined = [
        ...moviesRes.data.map(m => ({ ...m, mediaType: 'movie' })),
        ...seriesRes.data.map(s => ({ ...s, mediaType: 'series' }))
      ];

      // Restaurar el orden cronológico original de guardado
      const sortedItems = combined.sort((a, b) => {
        const indexA = watchlistItems.findIndex(i => i.mediaId === a.id && i.mediaType === a.mediaType);
        const indexB = watchlistItems.findIndex(i => i.mediaId === b.id && i.mediaType === b.mediaType);
        return indexA - indexB;
      });
      
      setItems(sortedItems);
      setError(null);
    } catch (err) {
      console.error(err);
      setError("No se pudo cargar tu lista de favoritos.");
    } finally {
      setLoading(false);
    }
  };

  const removeFromWatchlist = async (e, item) => {
    e.stopPropagation();
    try {
      await authApi.post(`/watchlist/${item.mediaType}/${item.id}`);
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

      {loading && items.length === 0 && (
        <div className="text-center text-slate-400 mt-20">
          <p className="text-xl animate-pulse">Cargando tus favoritos...</p>
        </div>
      )}

      {error && (
        <div className="bg-red-500/20 border border-red-500 text-red-200 p-4 rounded text-center">
          {error}
        </div>
      )}

      {!loading && !error && items.length === 0 && (
        <div className="text-center text-slate-400 mt-32 bg-slate-800/30 p-16 rounded-lg border border-slate-800 max-w-2xl mx-auto">
          <p className="text-3xl font-bold mb-4 text-slate-300">Aún no tienes favoritos</p>
          <p className="text-slate-500 text-lg mb-8">Explora el catálogo y añade el contenido que quieras ver más tarde.</p>
          <button 
            onClick={() => navigate('/catalog')}
            className="bg-pink-500 hover:bg-pink-600 text-white font-bold px-8 py-3 rounded-full transition-colors text-lg cursor-pointer shadow-lg shadow-pink-500/20"
          >
            Explorar catálogo
          </button>
        </div>
      )}

      {!error && items.length > 0 && (
        <>
          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 lg:gap-8 gap-6">
            {items.map((item) => (
              <div 
                key={`${item.mediaType}-${item.id}`}
                onClick={() => navigate(`/details/${item.mediaType}/${item.id}`)}
                className="bg-slate-800 rounded-lg overflow-hidden shadow-lg hover:shadow-pink-500/20 hover:scale-105 transition-all duration-300 cursor-pointer border border-slate-700 flex flex-col relative group"
              >
                <button
                  onClick={(e) => removeFromWatchlist(e, item)}
                  className="absolute top-3 right-3 z-10 p-2 rounded-full bg-slate-900/80 hover:bg-red-500/20 backdrop-blur-sm transition-all border border-slate-700 hover:border-red-500 cursor-pointer"
                  title="Quitar de Mi Lista"
                >
                  <span className="text-pink-500 group-hover:text-red-500 text-xl">✖</span>
                </button>

                <div className="h-64 bg-slate-700 flex items-center justify-center overflow-hidden shrink-0 relative">
                  {/* Etiqueta para saber de un vistazo si es Serie o Peli en Favoritos */}
                  <span className="absolute top-3 left-3 bg-black/60 backdrop-blur-md px-2 py-1 rounded text-xs font-bold text-white border border-white/10 z-10">
                    {item.mediaType === 'movie' ? '🎬 PELÍCULA' : '📺 SERIE'}
                  </span>
                  
                  {item.posterUrl ? (
                    <img src={item.posterUrl} alt={item.title} className="w-full h-full object-cover" />
                  ) : (
                    <span className="text-slate-500 font-bold">Sin Imagen</span>
                  )}
                </div>
                
                <div className="p-4 flex flex-col flex-grow">
                  <h2 className="text-lg font-bold text-pink-400 mb-1 truncate" title={item.title}>{item.title}</h2>
                  <div className="flex justify-between text-xs text-slate-400 mb-3">
                    <span>{item.year || item.startYear}</span>
                    <span>{item.duration ? `${item.duration} min` : 'Varias Temp.'}</span>
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