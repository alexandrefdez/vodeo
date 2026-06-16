import { useState, useEffect, useContext } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { catalogApi, streamingApi, authApi } from '../api/axiosConfig';
import { AuthContext } from '../context/AuthContext';
import ReviewSection from '../components/ReviewSection';
import toast from 'react-hot-toast';

const SeriesDetails = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useContext(AuthContext);

  const [series, setSeries] = useState(null);
  const [seasons, setSeasons] = useState([]);
  const [activeSeasonId, setActiveSeasonId] = useState(null);
  const [isFavorite, setIsFavorite] = useState(false);
  
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [refreshKey, setRefreshKey] = useState(0);

  const [showSeasonForm, setShowSeasonForm] = useState(false);
  const [newSeasonNumber, setNewSeasonNumber] = useState('');
  
  const [showEpisodeForm, setShowEpisodeForm] = useState(false);
  const [episodeData, setEpisodeData] = useState({ number: '', title: '', description: '', duration: '', file: null });
  const [isUploading, setIsUploading] = useState(false);
  const [isFetchingMetadata, setIsFetchingMetadata] = useState(false);

  const [activeSubtitleEpisodeId, setActiveSubtitleEpisodeId] = useState(null);
  const [subtitleData, setSubtitleData] = useState({ language: 'Español', languageCode: 'es', file: null });
  const [isUploadingSub, setIsUploadingSub] = useState(false);

  const [episodesProgress, setEpisodesProgress] = useState({});
  const [isSeriesCompleted, setIsSeriesCompleted] = useState(false);

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        const [seriesRes, seasonsRes, watchlistRes] = await Promise.all([
          catalogApi.get(`/series/${id}`),
          catalogApi.get(`/series/${id}/seasons`),
          authApi.get(`/watchlist?page=0&size=1000`)
        ]);

        setSeries(seriesRes.data);
        setSeasons(seasonsRes.data);
        if (seasonsRes.data.length > 0 && !activeSeasonId) {
          setActiveSeasonId(seasonsRes.data[0].id);
        }

        const savedKeys = watchlistRes.data.content.map(w => `${w.mediaType}-${w.mediaId}`);
        setIsFavorite(savedKeys.includes(`series-${id}`));
      } catch (err) {
        setError("No se pudo cargar la información de la serie.");
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, [id, refreshKey]);

  // 🛠️ FIX: Descargamos el objeto DTO entero para cada episodio
  useEffect(() => {
    const fetchSeasonProgress = async () => {
      if (!activeSeasonId || seasons.length === 0) return;
      const activeSeason = seasons.find(s => s.id === activeSeasonId);
      if (!activeSeason || !activeSeason.episodes) return;

      const newProgress = {};
      await Promise.all(activeSeason.episodes.map(async (ep) => {
        try {
          const res = await authApi.get(`/history/progress/episode/${ep.id}`);
          newProgress[ep.id] = res.data || { currentTime: 0, duration: 0 };
        } catch(e) {
          newProgress[ep.id] = { currentTime: 0, duration: 0 };
        }
      }));
      setEpisodesProgress(newProgress);
    };
    fetchSeasonProgress();
  }, [activeSeasonId, seasons]);

  // 🛠️ FIX: Cálculo "Vía Netflix" usando la duración real del DTO
  useEffect(() => {
    const checkSeriesCompletion = async () => {

      if (!seasons || seasons.length === 0) return;
      
      const lastSeason = seasons[seasons.length - 1];
      if (!lastSeason.episodes || lastSeason.episodes.length === 0) return;
      
      const lastEp = lastSeason.episodes[lastSeason.episodes.length - 1];

      try {
        const res = await authApi.get(`/history/progress/episode/${lastEp.id}`);
        const historyData = res.data || { currentTime: 0, duration: 0 };
        
        if (historyData.duration > 0 && (historyData.currentTime / historyData.duration) > 0.95) {
          setIsSeriesCompleted(true);
        } else {
          setIsSeriesCompleted(false);
        }
      } catch(e) {
        setIsSeriesCompleted(false);
      }
    };
    
    checkSeriesCompletion();
  }, [seasons]);

  const handleToggleWatchlist = async () => {
    const previousState = isFavorite;
    setIsFavorite(!isFavorite);
    previousState ? toast.success("Eliminada de Mi Lista", { icon: '💔' }) : toast.success("Añadida a Mi Lista", { icon: '❤️' });
    try {
      await authApi.post(`/watchlist/series/${id}`);
    } catch (err) {
      setIsFavorite(previousState);
      toast.error("Error al sincronizar con el servidor");
    }
  };

  const handleCreateSeason = async (e) => {
    e.preventDefault();
    try {
      const res = await catalogApi.post(`/series/${id}/seasons`, { seasonNumber: parseInt(newSeasonNumber), overview: "Temporada " + newSeasonNumber });
      setRefreshKey(old => old + 1);
      setActiveSeasonId(res.data.id);
      setShowSeasonForm(false);
      setNewSeasonNumber('');
      toast.success("Temporada creada");
    } catch (err) { toast.error("Error al crear temporada"); }
  };

  const handleAutoFetchEpisode = async () => {
    if (!episodeData.number) return;
    
    setIsFetchingMetadata(true);
    const loadingToast = toast.loading("Buscando metadatos en TMDB...");
    
    try {
      const activeSeason = seasons.find(s => s.id === activeSeasonId);
      const response = await catalogApi.get(
        `/series/fetch-episode?seriesTitle=${series.title}&seasonNumber=${activeSeason.seasonNumber}&episodeNumber=${episodeData.number}`
      );
      
      setEpisodeData(prev => ({
        ...prev,
        title: response.data.title || '',
        description: response.data.description || '',
        duration: response.data.duration || ''
      }));
      
      toast.dismiss(loadingToast);
      toast.success("¡Datos completados!");
    } catch (err) {
      toast.dismiss(loadingToast);
      toast.error("No se encontró ese episodio en TMDB");
    } finally {
      setIsFetchingMetadata(false);
    }
  };

  const handleCreateEpisode = async (e) => {
    e.preventDefault();
    if (!episodeData.file) return toast.error("Selecciona un archivo de vídeo");
    
    setIsUploading(true);
    const toastId = toast.loading("Subiendo vídeo al servidor...");

    try {
      const videoFormData = new FormData();
      videoFormData.append('file', episodeData.file);
      const uploadRes = await streamingApi.post('/upload', videoFormData, { headers: { 'Content-Type': 'multipart/form-data' }});

      await catalogApi.post(`/seasons/${activeSeasonId}/episodes`, {
        episodeNumber: parseInt(episodeData.number),
        title: episodeData.title,
        description: episodeData.description,
        duration: parseInt(episodeData.duration),
        videoPath: uploadRes.data.videoPath
      });

      toast.dismiss(toastId);
      toast.success("Episodio publicado");
      setShowEpisodeForm(false);
      setEpisodeData({ number: '', title: '', description: '', duration: '', file: null });
      setRefreshKey(old => old + 1);
    } catch (err) {
      toast.dismiss(toastId);
      toast.error("Error al publicar episodio");
    } finally { setIsUploading(false); }
  };

  const handleDeleteEpisode = async (episodeId) => {
    if(!window.confirm("¿Seguro que quieres borrar este episodio de forma definitiva?")) return;
    try {
      await catalogApi.delete(`/episodes/${episodeId}`);
      toast.success("Episodio borrado");
      setRefreshKey(old => old + 1);
    } catch (err) { toast.error("Error al borrar"); }
  };

  // Función Admin: Subir Subtítulos
  const handleUploadSubtitle = async (e, episodeId) => {
    e.preventDefault();
    if (!subtitleData.file) return toast.error("Selecciona un archivo .vtt");
    
    setIsUploadingSub(true);
    const toastId = toast.loading("Subiendo subtítulo...");

    try {
      const formData = new FormData();
      formData.append('file', subtitleData.file);
      const uploadRes = await streamingApi.post('/upload', formData, { headers: { 'Content-Type': 'multipart/form-data' }});

      const res = await catalogApi.post(`/episodes/${episodeId}/subtitles`, {
        language: subtitleData.language,
        languageCode: subtitleData.languageCode,
        subtitlePath: uploadRes.data.videoPath
      });

      toast.dismiss(toastId);
      toast.success("Subtítulo añadido");
      setActiveSubtitleEpisodeId(null);
      setSubtitleData({ language: 'Español', languageCode: 'es', file: null });

      // Actualización
      setSeasons(prevSeasons => prevSeasons.map(season => {
        if (season.id !== activeSeasonId) return season;
        return {
          ...season,
          episodes: season.episodes.map(ep => {
            if (ep.id !== episodeId) return ep;
            return {
              ...ep,
              subtitles: [...(ep.subtitles || []), res.data]
            };
          })
        };
      }));

    } catch (err) {
      toast.dismiss(toastId);
      toast.error("Error al subir subtítulo");
    } finally { 
      setIsUploadingSub(false); 
    }
  };

  // Función Admin: Borrar Subtítulos
  const handleDeleteSubtitle = async (subId) => {
    if(!window.confirm("¿Seguro que quieres borrar este subtítulo?")) return;
    try {
      await catalogApi.delete(`/subtitles/${subId}`);
      toast.success("Subtítulo borrado");
      
      // Actualización
      setSeasons(prevSeasons => prevSeasons.map(season => ({
        ...season,
        episodes: season.episodes.map(ep => ({
          ...ep,
          subtitles: ep.subtitles ? ep.subtitles.filter(s => s.id !== subId) : []
        }))
      })));

    } catch (err) { toast.error("Error al borrar subtítulo"); }
  };

  if (loading) return <div className="min-h-screen bg-slate-900 flex items-center justify-center text-emerald-500 text-xl animate-pulse">Cargando detalles...</div>;
  if (error || !series) return <div className="min-h-screen bg-slate-900 flex flex-col items-center justify-center text-white"><h1 className="text-3xl text-red-500 mb-4">Error</h1><p className="text-slate-400 mb-6">{error}</p><button onClick={() => navigate('/catalog')} className="px-6 py-2 bg-emerald-500 rounded hover:bg-emerald-600 font-bold transition-colors cursor-pointer">Volver al Catálogo</button></div>;

  const activeSeason = seasons.find(s => s.id === activeSeasonId);

  return (
    <div className="min-h-screen bg-slate-900 text-white relative">
      <div className="absolute top-6 left-6 z-50">
        <button onClick={() => navigate('/catalog')} className="flex items-center gap-2 bg-black/40 hover:bg-black/80 backdrop-blur-md text-white px-5 py-2 rounded-full font-bold border border-white/10 transition-all cursor-pointer">
          &larr; Volver
        </button>
      </div>

      <div className="w-full h-[60vh] lg:h-[75vh] bg-cover bg-center bg-no-repeat relative" style={{ backgroundImage: `url(${series.backdropUrl || series.posterUrl})` }}>
        <div className="absolute inset-0 bg-gradient-to-t from-slate-900 via-slate-900/60 to-transparent"></div>
      </div>

      <div className="max-w-6xl mx-auto px-8 relative -mt-32 lg:-mt-64 z-10 pb-20">
        <div className="flex flex-col md:flex-row gap-10">
          <div className="hidden md:block shrink-0">
            <img src={series.posterUrl} alt={series.title} className="w-72 rounded-xl shadow-2xl shadow-emerald-500/10 border border-slate-700" />
          </div>

          <div className="flex flex-col justify-end pt-4">
            <h1 className="text-5xl lg:text-7xl font-bold text-white drop-shadow-lg mb-4">{series.title}</h1>
            <div className="flex flex-wrap items-center gap-4 text-sm font-semibold text-slate-300 mb-6">
              <span className="text-emerald-400 font-bold">{series.tmdbRating ? `⭐ ${series.tmdbRating.toFixed(1)}` : '⭐ N/A'}</span>
              <span>{series.startYear}</span>
              {series.contentRating && <span className="border border-slate-500 px-2 py-0.5 rounded text-slate-400">{series.contentRating}</span>}
              
              {isSeriesCompleted && (
                <span className="bg-emerald-500/20 text-emerald-400 border border-emerald-500/50 px-2 py-0.5 rounded flex items-center gap-1 text-xs font-bold shadow-sm">
                  ✓ VISTA
                </span>
              )}
            </div>
            <p className="text-lg text-slate-300 leading-relaxed max-w-3xl mb-6 drop-shadow">{series.description}</p>
            <div className="flex flex-wrap gap-2 mb-8">
              {series.genres && series.genres.map(genre => <span key={genre} className="bg-slate-800/80 border border-slate-600 px-4 py-1 rounded-full text-sm text-emerald-300">{genre}</span>)}
            </div>

            <div className="flex items-center gap-4 mb-10">
              <button onClick={handleToggleWatchlist} className="bg-slate-800/80 hover:bg-slate-700 backdrop-blur-sm border border-slate-600 text-white p-3 rounded-md transition-colors shadow-lg cursor-pointer" title="Añadir a Mi Lista">
                <span className="text-2xl">{isFavorite ? '❤️' : '🤍'}</span>
              </button>
            </div>

            {/* SECCIÓN RECUPERADA: DATOS DEL REPARTO */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-6 text-sm border-t border-slate-700/50 pt-6">
              <div>
                <span className="text-slate-500 block mb-1">Creado por</span>
                <span className="text-white font-medium">{series.director || 'Desconocido'}</span>
              </div>
              <div>
                <span className="text-slate-500 block mb-1">Reparto Principal</span>
                <span className="text-white font-medium">{series.castCrew || 'Desconocido'}</span>
              </div>
            </div>

          </div>
        </div>

        {/* GESTOR DE TEMPORADAS Y EPISODIOS */}
        <div className="mt-16 border-t border-slate-800 pt-10">
          <div className="flex justify-between items-end mb-6">
            <h2 className="text-3xl font-bold text-emerald-400">Episodios</h2>
            {user?.role === 'ADMIN' && (
              <button onClick={() => setShowSeasonForm(!showSeasonForm)} className="bg-slate-800 hover:bg-slate-700 border border-slate-600 px-4 py-2 rounded text-sm font-bold transition-colors cursor-pointer">
                + Nueva Temporada
              </button>
            )}
          </div>

          {/* Formulario Admin: Crear Temporada */}
          {showSeasonForm && (
            <form onSubmit={handleCreateSeason} className="bg-slate-800 p-4 rounded mb-6 flex gap-4 items-center border border-slate-700">
              <input type="number" placeholder="Número de temporada" value={newSeasonNumber} onChange={e => setNewSeasonNumber(e.target.value)} className="p-2 bg-slate-900 border border-slate-700 rounded text-white focus:outline-none focus:border-emerald-500" required />
              <button type="submit" className="bg-emerald-500 hover:bg-emerald-600 text-white px-4 py-2 rounded font-bold cursor-pointer transition-colors">Crear</button>
            </form>
          )}

          {seasons.length > 0 ? (
            <div>
              {/* Pestañas de Temporadas */}
              <div className="flex gap-2 overflow-x-auto custom-scrollbar mb-6 pb-2">
                {seasons.map(s => (
                  <button key={s.id} onClick={() => setActiveSeasonId(s.id)} className={`px-6 py-2 rounded-full font-bold whitespace-nowrap cursor-pointer transition-all ${activeSeasonId === s.id ? 'bg-emerald-500 text-white shadow-lg' : 'bg-slate-800 text-slate-400 hover:text-white'}`}>
                    Temporada {s.seasonNumber}
                  </button>
                ))}
              </div>

              {/* Lista de Episodios de la Temporada Activa */}
              {activeSeason && (
                <div className="space-y-4">
                  {user?.role === 'ADMIN' && (
                    <button onClick={() => setShowEpisodeForm(!showEpisodeForm)} className="w-full bg-slate-800/50 hover:bg-slate-800 border border-dashed border-slate-600 p-4 rounded text-slate-400 hover:text-emerald-400 font-bold transition-colors cursor-pointer">
                      + Subir Nuevo Episodio a la Temporada {activeSeason.seasonNumber}
                    </button>
                  )}

                  {/* Formulario Admin: Subir Episodio */}
                  {showEpisodeForm && (
                    <form onSubmit={handleCreateEpisode} className="bg-slate-800 p-6 rounded-lg border border-emerald-500/30 flex flex-col gap-4">
                      <div className="flex gap-4 items-end">
                        <div className="flex flex-col w-32 shrink-0">
                          <label className="text-xs text-slate-400 mb-1">Nº Ep.</label>
                          <div className="flex gap-2">
                            <input type="number" placeholder="Ej: 1" value={episodeData.number} onChange={e => setEpisodeData({...episodeData, number: e.target.value})} className="w-full p-2 bg-slate-900 border border-slate-700 rounded text-white focus:outline-none focus:border-emerald-500" required />
                            <button type="button" onClick={handleAutoFetchEpisode} disabled={isFetchingMetadata || !episodeData.number} className="bg-emerald-500/20 hover:bg-emerald-500 text-emerald-400 hover:text-white border border-emerald-500/50 px-3 py-2 rounded font-bold transition-colors cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed" title="Autocompletar datos desde TMDB">
                              {isFetchingMetadata ? '⏳' : '✨'}
                            </button>
                          </div>
                        </div>
                        <div className="flex flex-col flex-grow">
                          <label className="text-xs text-slate-400 mb-1">Título del Episodio</label>
                          <input type="text" placeholder="Ej: Piloto" value={episodeData.title} onChange={e => setEpisodeData({...episodeData, title: e.target.value})} className="w-full p-2 bg-slate-900 border border-slate-700 rounded text-white focus:outline-none focus:border-emerald-500" required />
                        </div>
                        <div className="flex flex-col w-24 shrink-0">
                          <label className="text-xs text-slate-400 mb-1">Minutos</label>
                          <input type="number" placeholder="Ej: 45" value={episodeData.duration} onChange={e => setEpisodeData({...episodeData, duration: e.target.value})} className="w-full p-2 bg-slate-900 border border-slate-700 rounded text-white focus:outline-none focus:border-emerald-500" required />
                        </div>
                      </div>
                      <div className="flex flex-col">
                        <label className="text-xs text-slate-400 mb-1">Sinopsis</label>
                        <textarea placeholder="Sinopsis..." value={episodeData.description} onChange={e => setEpisodeData({...episodeData, description: e.target.value})} className="w-full p-2 bg-slate-900 border border-slate-700 rounded text-white focus:outline-none focus:border-emerald-500" rows="2" required />
                      </div>
                      
                      {/* BOTÓN DE SELECCIÓN DE VÍDEO ARREGLADO */}
                      <input 
                        type="file" 
                        accept="video/mp4" 
                        onChange={e => setEpisodeData({...episodeData, file: e.target.files[0]})} 
                        className="w-full p-2 mt-2 bg-slate-900 border border-slate-700 rounded text-slate-300 file:mr-4 file:py-2 file:px-4 file:rounded file:border-0 file:text-sm file:font-bold file:bg-emerald-500 file:text-white hover:file:bg-emerald-600 cursor-pointer" 
                        required 
                      />

                      <button type="submit" disabled={isUploading} className="bg-emerald-500 hover:bg-emerald-600 disabled:bg-slate-700 mt-2 py-3 rounded font-bold transition-colors cursor-pointer text-lg shadow-lg">
                        {isUploading ? 'Procesando y Subiendo vídeo...' : 'Publicar Episodio'}
                      </button>
                    </form>
                  )}

                  {/* Listado Visual de Episodios */}
                  {activeSeason.episodes.length === 0 ? (
                    <p className="text-slate-500 italic py-4">No hay episodios subidos en esta temporada.</p>
                  ) : (
                    activeSeason.episodes.map(ep => {
                      
                      const epHistory = episodesProgress[ep.id] || { currentTime: 0, duration: 0 };
                      const isWatched = epHistory.duration > 0 && (epHistory.currentTime / epHistory.duration) > 0.95;

                      return (
                      <div key={ep.id} className="flex flex-col bg-slate-800/80 hover:bg-slate-800 border border-slate-700 p-4 rounded-lg group transition-colors">
                        <div className="flex items-center justify-between">
                          <div className="flex items-center gap-6">
                            <span className="text-2xl font-bold text-slate-500 w-8">{ep.episodeNumber}</span>
                            <div>
                              <div className="flex items-center gap-3">
                                <h3 className="font-bold text-white text-lg">{ep.title}</h3>
                                {isWatched && (
                                  <span className="bg-emerald-500/20 text-emerald-400 border border-emerald-500/50 px-2 py-0.5 rounded flex items-center gap-1 text-[10px] font-bold shadow-sm uppercase tracking-wider">
                                    ✓ Visto
                                  </span>
                                )}
                              </div>
                              <p className="text-slate-400 text-sm line-clamp-1 max-w-xl">{ep.description}</p>
                            </div>
                          </div>
                          <div className="flex items-center gap-4">
                            <span className="text-slate-500 text-sm">{ep.duration}m</span>
                            <button onClick={() => navigate(`/player/episode/${ep.id}`)} className="bg-white hover:bg-emerald-400 hover:text-white text-black p-3 rounded-full cursor-pointer transition-colors shadow-lg">
                              ▶
                            </button>
                            {/* BOTONES ADMIN: GESTIONAR SUBTÍTULOS Y BORRAR */}
                            {user?.role === 'ADMIN' && (
                              <div className="flex gap-1">
                                <button onClick={() => setActiveSubtitleEpisodeId(activeSubtitleEpisodeId === ep.id ? null : ep.id)} className="text-slate-500 hover:text-emerald-400 p-2 cursor-pointer transition-colors" title="Gestionar Subtítulos">
                                  💬
                                </button>
                                <button onClick={() => handleDeleteEpisode(ep.id)} className="text-slate-500 hover:text-red-500 p-2 cursor-pointer transition-colors" title="Borrar Episodio">
                                  🗑️
                                </button>
                              </div>
                            )}
                          </div>
                        </div>

                        {/* PANEL DE SUBTÍTULOS DEL EPISODIO */}
                        {user?.role === 'ADMIN' && (
                          <div className="mt-3 ml-14 border-t border-slate-700/50 pt-3">
                            <div className="flex flex-wrap gap-2 mb-2">
                              {ep.subtitles && ep.subtitles.length > 0 ? (
                                ep.subtitles.map(sub => (
                                  <span key={sub.id} className="bg-slate-900 border border-slate-600 px-2 py-1 rounded text-xs flex items-center gap-2 text-slate-300">
                                    {sub.language} ({sub.languageCode})
                                    <button onClick={() => handleDeleteSubtitle(sub.id)} className="text-slate-500 hover:text-red-500 font-bold ml-1 cursor-pointer">✕</button>
                                  </span>
                                ))
                              ) : (
                                <span className="text-xs text-slate-500 italic">No hay subtítulos subidos.</span>
                              )}
                            </div>

                            {activeSubtitleEpisodeId === ep.id && (
                              <form onSubmit={(e) => handleUploadSubtitle(e, ep.id)} className="bg-slate-900/50 p-3 rounded border border-slate-700 mt-2 flex flex-col gap-3">
                                <div className="flex gap-3">
                                  <input type="text" placeholder="Idioma (Ej: Español)" value={subtitleData.language} onChange={e => setSubtitleData({...subtitleData, language: e.target.value})} className="w-1/3 p-2 bg-slate-900 border border-slate-700 rounded text-white text-xs focus:outline-none focus:border-emerald-500" required />
                                  <input type="text" placeholder="ISO (Ej: es)" value={subtitleData.languageCode} onChange={e => setSubtitleData({...subtitleData, languageCode: e.target.value})} className="w-1/4 p-2 bg-slate-900 border border-slate-700 rounded text-white text-xs focus:outline-none focus:border-emerald-500" required />
                                  <input type="file" accept=".vtt,.srt" onChange={e => setSubtitleData({...subtitleData, file: e.target.files[0]})} className="flex-grow p-1 bg-slate-900 border border-slate-700 rounded text-slate-300 file:mr-2 file:py-1 file:px-2 file:rounded file:border-0 file:text-xs file:font-bold file:bg-emerald-500 file:text-white cursor-pointer text-xs" required />
                                </div>
                                <button type="submit" disabled={isUploadingSub} className="bg-emerald-500 hover:bg-emerald-600 disabled:bg-slate-700 py-1.5 rounded font-bold transition-colors cursor-pointer text-xs">
                                  {isUploadingSub ? 'Subiendo...' : 'Guardar Subtítulo'}
                                </button>
                              </form>
                            )}
                          </div>
                        )}
                      </div>
                    );
                  })
                  )}
                </div>
              )}
            </div>
          ) : (
            <div className="text-center bg-slate-800/50 border border-slate-800 rounded-lg p-10">
              <p className="text-slate-400 text-lg">Esta serie aún no tiene temporadas subidas.</p>
            </div>
          )}
        </div>

        <ReviewSection mediaId={series.id} mediaType="series" />
        
      </div>
    </div>
  );
};

export default SeriesDetails;