import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { authApi, catalogApi } from '../api/axiosConfig';

const Recommendations = ({ type = 'movie' }) => {
  const [recommended, setRecommended] = useState([]);
  const [topGenre, setTopGenre] = useState("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const generateRecommendations = async () => {
      try {
        setLoading(true);

        setRecommended([]);
        setTopGenre("");
        
        // Obtener historial del usuario
        const historyRes = await authApi.get('/history?size=50');
        const historyList = historyRes.data.content;

        if (!historyList || historyList.length === 0) {
          setLoading(false);
          return; // Si no hay historial no hay datos para el algoritmo
        }

        // Extraer ids dependiendo de si estamos en la pestaña de peliculas o series
        const historyType = type === 'series' ? 'episode' : 'movie';
        const typeHistory = historyList.filter(item => item.mediaType === historyType);
        const watchedIds = typeHistory.map(item => item.mediaId);

        if (watchedIds.length === 0) {
          setLoading(false);
          return;
        }

        let genreCounts = {};
        const watchedParentIds = new Set(); // Para no recomendar cosas que ya hemos visto

        // Obtener metadatos de lo que ha visto
        if (type === 'movie') {
          const watchedRes = await catalogApi.get(`/movies/batch?ids=${watchedIds.join(',')}`);
          watchedRes.data.forEach(movie => {
            watchedParentIds.add(movie.id);
            if (movie.genres) {
              movie.genres.forEach(genre => {
                genreCounts[genre] = (genreCounts[genre] || 0) + 1;
              });
            }
          });
        } else {
          // Si es serie obtenemos los episodios y leemos la informacion
          const episodesData = await Promise.all(
            watchedIds.map(id => catalogApi.get(`/episodes/${id}`).then(res => res.data).catch(() => null))
          );
          episodesData.forEach(ep => {
            if (ep) {
              watchedParentIds.add(ep.seriesId); // Marcamos la SERIE como vista
              if (ep.seriesGenres) {
                ep.seriesGenres.forEach(genre => {
                  genreCounts[genre] = (genreCounts[genre] || 0) + 1;
                });
              }
            }
          });
        }

        // Calcular el genero favorito por frecuencia
        const sortedGenres = Object.keys(genreCounts).sort((a, b) => genreCounts[b] - genreCounts[a]);
        const favoriteGenre = sortedGenres[0];
        
        if (!favoriteGenre) {
          setLoading(false);
          return;
        }
        
        setTopGenre(favoriteGenre);

        // Descargar catalogo para buscar coincidencias
        const catalogRes = await catalogApi.get(`/${type === 'movie' ? 'movies' : 'series'}?page=0&size=100`);
        const allItems = catalogRes.data.content;

        // Filtrar que tengan el genero favorito y que NO las haya visto todavia
        const suggestions = allItems.filter(item => 
          item.genres?.includes(favoriteGenre) && !watchedParentIds.has(item.id)
        );

        // Ordenar por la mejor nota de TMDB
        suggestions.sort((a, b) => (b.tmdbRating || 0) - (a.tmdbRating || 0));

        setRecommended(suggestions.slice(0, 10)); // Guardar el Top 10

      } catch (error) {
        console.error("Error en el Motor de Recomendaciones", error);
      } finally {
        setLoading(false);
      }
    };

    generateRecommendations();
  }, [type]); // Se recalcula cuando cambiamos entre peliculas y series

  if (loading) return null;
  if (recommended.length === 0) return null; // Si no hay nada nuevo que recomendar se oculta

  return (
    <div className="mx-8 mt-12 mb-6 animate-in fade-in duration-700">
      <h2 className="text-2xl font-bold text-white mb-4 flex items-center gap-2">
        <span className="text-pink-500 text-2xl">✨</span> Porque viste <span className="text-pink-400 underline decoration-pink-500/50 underline-offset-4">"{topGenre}"</span>
      </h2>
      <div className="flex gap-4 overflow-x-auto pb-6 custom-scrollbar snap-x">
        {recommended.map((item) => (
          <Link
            key={`rec-${item.id}`}
            to={`/details/${type}/${item.id}`}
            className="min-w-[200px] w-[200px] group bg-slate-800/80 rounded-lg overflow-hidden border border-slate-700 hover:border-pink-500 transition-all hover:-translate-y-1 hover:shadow-xl hover:shadow-pink-500/20 snap-start"
          >
            <div className="h-72 bg-slate-900 relative overflow-hidden">
              {item.posterUrl ? (
                <img
                  src={item.posterUrl}
                  alt={item.title}
                  className="w-full h-full object-cover opacity-80 group-hover:opacity-100 group-hover:scale-105 transition-all duration-300"
                />
              ) : (
                <div className="w-full h-full flex items-center justify-center text-slate-500 bg-slate-800">
                  <span className="text-4xl">🎬</span>
                </div>
              )}
              <div className="absolute inset-0 bg-gradient-to-t from-slate-900 via-transparent to-transparent opacity-0 group-hover:opacity-100 transition-opacity"></div>
            </div>
            
            <div className="p-3">
              <p className="text-slate-200 font-bold truncate group-hover:text-pink-400 transition-colors">
                {item.title}
              </p>
              <div className="flex justify-between items-center mt-1">
                <span className="text-xs text-slate-400">{item.year || item.startYear}</span>
                <span className="text-xs font-bold text-yellow-400 bg-yellow-400/10 px-2 py-0.5 rounded border border-yellow-400/20">
                  ⭐ {item.tmdbRating?.toFixed(1) || 'N/A'}
                </span>
              </div>
            </div>
          </Link>
        ))}
      </div>
    </div>
  );
};

export default Recommendations;