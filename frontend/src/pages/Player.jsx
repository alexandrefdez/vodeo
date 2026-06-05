import { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate, useLocation } from 'react-router-dom';
import { catalogApi, authApi } from '../api/axiosConfig';
import toast from 'react-hot-toast';

const Player = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const location = useLocation();
  
  const isEpisode = location.pathname.includes('/episode/');
  const mediaType = isEpisode ? 'episode' : 'movie';
  const endpoint = isEpisode ? `/episodes/${id}` : `/movies/${id}`;

  const [media, setMedia] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Estados para el Autoplay
  const [nextEpisode, setNextEpisode] = useState(null);
  const [showAutoplay, setShowAutoplay] = useState(false);
  const [countdown, setCountdown] = useState(10);

  const videoRef = useRef(null);
  const lastSavedTime = useRef(0);
  const isSeeking = useRef(false);
  const progressToResume = useRef(0); // Ref para guardar el progreso inicial

  useEffect(() => {
    // Resetear estados al cambiar de episodio
    setShowAutoplay(false);
    setCountdown(10);
    setNextEpisode(null);
    progressToResume.current = 0;

    const fetchMediaAndProgress = async () => {
      try {
        const response = await catalogApi.get(endpoint);
        setMedia(response.data);

        if (isEpisode) {
          try {
            const nextRes = await catalogApi.get(`/episodes/${id}/next`);
            if (nextRes.status === 200 && nextRes.data) {
              setNextEpisode(nextRes.data);
            }
          } catch (e) {
            console.log("No hay siguiente episodio o es el final de la serie");
          }
        }

        const progressRes = await authApi.get(`/history/progress/${mediaType}/${id}`);
        const savedSeconds = progressRes.data;
        
        lastSavedTime.current = savedSeconds;
        progressToResume.current = savedSeconds; // Guardamos el progreso para inyectarlo al cargar el vídeo

      } catch (err) {
        setError("No se pudo cargar la información del vídeo.");
      } finally {
        setLoading(false);
      }
    };
    fetchMediaAndProgress();
  }, [id, endpoint, mediaType, isEpisode]);

  // Lógica de la Cuenta Atrás
  useEffect(() => {
    let timer;
    if (showAutoplay && countdown > 0) {
      timer = setTimeout(() => setCountdown(c => c - 1), 1000);
    } else if (showAutoplay && countdown === 0) {
      handlePlayNext(); // Cuando llega a 0, saltar!
    }
    return () => clearTimeout(timer);
  }, [showAutoplay, countdown]);

  const saveProgressToBackend = async (currentTime, duration) => {
    if (!currentTime || !duration || isNaN(currentTime) || isNaN(duration)) return;
    
    // Ignorar reproducciones menores a 30 segundos para evitar clics accidentales en el historial
    if (currentTime < 30) return;
    
    try {
      await authApi.post('/history', {
        mediaId: parseInt(id),
        mediaType: mediaType,
        currentTime: Math.floor(currentTime),
        duration: Math.floor(duration)
      });
      lastSavedTime.current = currentTime;
    } catch (error) {
      console.error("Error guardando progreso", error);
    }
  };

  const handleTimeUpdate = () => {
    if (!videoRef.current || isSeeking.current) return;
    
    const currentTime = videoRef.current.currentTime;
    const duration = videoRef.current.duration;
    
    if (Math.abs(currentTime - lastSavedTime.current) >= 10) {
      saveProgressToBackend(currentTime, duration);
    }
  };

  const handlePause = () => {
    if (videoRef.current) {
      saveProgressToBackend(videoRef.current.currentTime, videoRef.current.duration);
    }
  };

  const handleSeeking = () => { 
    isSeeking.current = true; 
  };
  
  const handleSeeked = () => { 
    isSeeking.current = false; 
  };

  // Evento que se dispara cuando el vídeo carga sus datos
  const handleLoadedMetadata = () => {
    if (!videoRef.current) return;
    
    const duration = videoRef.current.duration;
    const savedSeconds = progressToResume.current;

    if (savedSeconds > 0) {
      // PREVENCION de bucle por si ya se vio casi entero mas de 95% entonces reiniciar desde 0
      if (savedSeconds / duration > 0.95) {
        videoRef.current.currentTime = 0;
        lastSavedTime.current = 0;
      } else {
        videoRef.current.currentTime = savedSeconds;
        const mins = Math.floor(savedSeconds / 60);
        const secs = String(Math.floor(savedSeconds) % 60).padStart(2, '0');
        toast(`Reanudando desde ${mins}:${secs}`, { icon: '🍿' });
      }
    }
  };

  // Finalización del vídeo y Autoplay
  const handleVideoEnded = () => {
    if (nextEpisode) {
      if (document.fullscreenElement) {
        document.exitFullscreen(); // Salir de pantalla completa
      }
      setShowAutoplay(true);
      setCountdown(10);
    } else {
      saveProgressToBackend(videoRef.current.duration, videoRef.current.duration);
    }
  };

  const handlePlayNext = () => {
    setShowAutoplay(false);
    // { replace: true } sobrescribe el historial para que el botón Volver no regrese al episodio anterior
    navigate(`/player/episode/${nextEpisode.id}`, { replace: true });
  };

  const handleCancelAutoplay = () => {
    setShowAutoplay(false);
  };

  useEffect(() => {
    return () => {
      if (videoRef.current && videoRef.current.currentTime > 0) {
        saveProgressToBackend(videoRef.current.currentTime, videoRef.current.duration);
      }
    };
  }, []);

  // Recuperamos el token para inyectarlo en la petición del vídeo nativo
  const token = localStorage.getItem('token');
  const videoSrc = media?.videoPath 
    ? `${import.meta.env.VITE_STREAMING_API_URL}/play/${media.videoPath}?token=${token}` 
    : null;

  return (
    <div className="min-h-screen bg-black text-white flex flex-col relative overflow-hidden">
      
      {/* OVERLAY DE AUTOPLAY (Siguiente Episodio) */}
      {showAutoplay && nextEpisode && (
        <div className="absolute inset-0 z-50 bg-black/85 flex flex-col items-center justify-center text-white backdrop-blur-md animate-in fade-in duration-500">
          
          <div className="relative mb-8">
            <svg className="w-32 h-32 transform -rotate-90">
              <circle cx="64" cy="64" r="60" stroke="currentColor" strokeWidth="8" fill="transparent" className="text-slate-800" />
              <circle cx="64" cy="64" r="60" stroke="currentColor" strokeWidth="8" fill="transparent" 
                strokeDasharray={377} 
                strokeDashoffset={377 - (377 * countdown) / 10} 
                className="text-sky-500 transition-all duration-1000 ease-linear" 
              />
            </svg>
            <div className="absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 text-4xl font-black">
              {countdown}
            </div>
          </div>

          <h2 className="text-2xl text-slate-400 mb-2 font-semibold">Siguiente episodio</h2>
          <h3 className="text-4xl font-bold mb-12 text-white drop-shadow-lg text-center max-w-2xl">
            {nextEpisode.seriesTitle} <br/> <span className="text-sky-400">Episodio {nextEpisode.episodeNumber}: {nextEpisode.title}</span>
          </h3>
          
          <div className="flex gap-6">
            <button 
              onClick={handlePlayNext} 
              className="bg-white hover:bg-slate-200 text-black px-10 py-4 rounded-full font-bold text-xl transition-transform hover:scale-105 shadow-xl shadow-white/10 flex items-center gap-3 cursor-pointer"
            >
              <span>▶</span> Reproducir Ahora
            </button>
            <button 
              onClick={handleCancelAutoplay} 
              className="bg-slate-800 hover:bg-slate-700 border border-slate-600 px-10 py-4 rounded-full font-bold text-xl transition-colors cursor-pointer"
            >
              Cancelar
            </button>
          </div>
        </div>
      )}

      {/* Barra de navegación superior */}
      <div className={`p-6 flex items-center justify-between z-10 absolute w-full transition-opacity duration-300 ${showAutoplay ? 'opacity-0 pointer-events-none' : 'opacity-100'}`}>
        <button 
          onClick={() => navigate(-1)} 
          className="text-slate-300 hover:text-white flex items-center gap-2 transition-colors bg-black/50 px-4 py-2 rounded-full backdrop-blur-sm cursor-pointer"
        >
          <span className="text-xl">&larr;</span> Volver
        </button>
        {media && (
          <h1 className="text-xl font-bold text-sky-500 drop-shadow-md">
            {isEpisode ? `Ep. ${media.episodeNumber}: ${media.title}` : media.title}
          </h1>
        )}
      </div>
      
      {/* Zona central del reproductor */}
      <div className="flex-grow flex items-center justify-center relative w-full h-screen">
        {loading && <p className="text-sky-500 text-xl animate-pulse">Cargando reproductor...</p>}
        {error && <div className="bg-red-500/20 text-red-200 border border-red-500 p-6 rounded"><p className="text-xl">{error}</p></div>}

        {!loading && !error && !media?.videoPath && (
          <div className="text-center bg-slate-900 border border-slate-700 p-10 rounded-lg max-w-lg">
            <p className="text-2xl font-bold text-slate-300 mb-2">Contenido no disponible</p>
            <p className="text-slate-500">El archivo de vídeo aún no ha sido subido al servidor.</p>
          </div>
        )}

        {!loading && !error && media?.videoPath && (
          <video 
            ref={videoRef}
            controls={!showAutoplay} 
            autoPlay 
            controlsList="nodownload" 
            className="w-full h-full object-contain bg-black outline-none" 
            src={videoSrc}
            onLoadedMetadata={handleLoadedMetadata}
            onTimeUpdate={handleTimeUpdate}
            onPause={handlePause}
            onSeeking={handleSeeking}
            onSeeked={handleSeeked}
            onEnded={handleVideoEnded}
          >
            Tu navegador no soporta la reproducción de vídeo.
          </video>
        )}
      </div>
    </div>
  );
};

export default Player;