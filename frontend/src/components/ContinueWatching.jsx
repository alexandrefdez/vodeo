import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { authApi, catalogApi } from '../api/axiosConfig';

const ContinueWatching = ({ type = 'movie' }) => {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchHistoryAndEnrich = async () => {
      try {
        const historyRes = await authApi.get('/history?size=10');
        const historyList = historyRes.data.content;

        if (!historyList || historyList.length === 0) {
          setLoading(false);
          return;
        }

        const enrichedItems = await Promise.all(historyList.map(async (item) => {
          try {
            // Si ya lo ha visto entero >95%, no se muestra en seguir viendo
            if (item.duration > 0 && (item.currentTime / item.duration) > 0.95) {
              return null;
            }

            const endpoint = item.mediaType === 'episode' 
              ? `/episodes/${item.mediaId}` 
              : `/movies/${item.mediaId}`;
            
            const mediaRes = await catalogApi.get(endpoint);
            
            return {
              ...item,
              mediaDetails: mediaRes.data
            };
          } catch (error) {
            return null;
          }
        }));

        const validItems = enrichedItems.filter(item => item !== null);

        // Deduplicar episodios de la misma serie
        const seenSeries = new Set();
        const deduplicatedItems = [];

        for (const item of validItems) {
          if (item.mediaType === 'episode') {
            const seriesName = item.mediaDetails.seriesTitle;
            // Si ya tenemos un episodio más reciente de esta serie en la lista se salta
            if (seenSeries.has(seriesName)) {
              continue; 
            }
            seenSeries.add(seriesName);
          }
          deduplicatedItems.push(item);
        }

        setItems(deduplicatedItems);
      } catch (error) {
        console.error("Error al cargar historial", error);
      } finally {
        setLoading(false);
      }
    };

    fetchHistoryAndEnrich();
  }, []);

  if (loading) return (
    <div className="mx-8 mt-10 mb-6 animate-pulse">
      <div className="h-8 bg-slate-800 rounded w-48 mb-4"></div>
      <div className="flex gap-4">
         <div className="h-40 w-[280px] bg-slate-800 rounded-xl"></div>
         <div className="h-40 w-[280px] bg-slate-800/50 rounded-xl"></div>
      </div>
    </div>
  );

  if (items.length === 0) return null;

  return (
    <div className="mx-8 mt-10 mb-6">
      <h2 className="text-2xl font-bold text-white mb-4 flex items-center gap-2">
        <span className="text-red-600 text-xl">▶</span> Seguir Viendo
      </h2>
      <div className={`flex gap-4 overflow-x-auto pb-6 snap-x ${type === 'movie' ? 'custom-scrollbar' : 'custom-scrollbar-emerald'}`}>
        {items.map((item) => {
          const { mediaDetails, currentTime, duration, mediaType, mediaId } = item;
          const progressPercentage = duration > 0 ? Math.min((currentTime / duration) * 100, 100) : 0;
          
          const title = mediaType === 'episode' 
            ? `${mediaDetails.seriesTitle || 'Serie'} - Ep. ${mediaDetails.episodeNumber}` 
            : mediaDetails.title;

          // Asegurar que ponga al menos 1 minuto
          const remainingMinutes = Math.max(1, Math.floor((duration - currentTime) / 60));

          return (
            <Link 
              key={`${mediaType}-${mediaId}`} 
              to={`/player/${mediaType === 'episode' ? 'episode' : 'movie'}/${mediaId}`} 
              className="min-w-[280px] w-[280px] group bg-slate-800/80 rounded-xl overflow-hidden border border-slate-700 hover:border-slate-500 transition-all hover:-translate-y-1 hover:shadow-xl hover:shadow-black/50 snap-start relative"
            >
              <div className="h-36 bg-slate-900 relative overflow-hidden">
                {mediaDetails.posterUrl ? (
                  <img 
                    src={mediaDetails.posterUrl} 
                    alt={title}
                    className="w-full h-full object-cover object-top opacity-60 group-hover:opacity-100 group-hover:scale-105 transition-all duration-300"
                  />
                ) : (
                  <div className="w-full h-full flex items-center justify-center text-slate-500 bg-slate-800">
                    <span className="text-4xl">🎬</span>
                  </div>
                )}
                
                <div className="absolute inset-0 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity bg-black/40">
                  <div className="w-12 h-12 bg-white/10 backdrop-blur-sm border border-white/30 rounded-full flex items-center justify-center shadow-lg">
                    <span className="text-white text-xl pl-1">▶</span>
                  </div>
                </div>
              </div>

              <div className="h-1.5 bg-slate-700 w-full relative">
                <div 
                  className="h-full bg-red-600 shadow-[0_0_8px_rgba(220,38,38,0.8)]" 
                  style={{ width: `${progressPercentage}%` }}
                />
              </div>

              <div className="p-4">
                <p className="text-slate-200 font-semibold truncate group-hover:text-white transition-colors" title={title}>
                  {title}
                </p>
                <p className="text-xs text-slate-400 mt-1">
                  Restan {remainingMinutes} min
                </p>
              </div>
            </Link>
          );
        })}
      </div>
    </div>
  );
};

export default ContinueWatching;