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

  const videoRef = useRef(null);
  const lastSavedTime = useRef(0);
  const isSeeking = useRef(false);

  useEffect(() => {
    const fetchMediaAndProgress = async () => {
      try {
        const response = await catalogApi.get(endpoint);
        setMedia(response.data);

        const progressRes = await authApi.get(`/history/progress/${mediaType}/${id}`);
        const savedSeconds = progressRes.data;
        lastSavedTime.current = savedSeconds;

        if (videoRef.current && savedSeconds > 0) {
          videoRef.current.currentTime = savedSeconds;
          
          const mins = Math.floor(savedSeconds / 60);
          const secs = String(savedSeconds % 60).padStart(2, '0');
          toast(`Reanudando desde ${mins}:${secs}`, { icon: '🍿' });
        }
      } catch (err) {
        setError("No se pudo cargar la información del vídeo.");
      } finally {
        setLoading(false);
      }
    };
    fetchMediaAndProgress();
  }, [id, endpoint, mediaType]);

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
    <div className="min-h-screen bg-black text-white flex flex-col">
      {/* Barra de navegación superior */}
      <div className="p-6 flex items-center justify-between z-10 absolute w-full">
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
            controls 
            autoPlay 
            controlsList="nodownload" 
            className="w-full h-full object-contain bg-black outline-none" 
            src={videoSrc}
            onTimeUpdate={handleTimeUpdate}
            onPause={handlePause}
            onSeeking={handleSeeking}
            onSeeked={handleSeeked}
          >
            Tu navegador no soporta la reproducción de vídeo.
          </video>
        )}
      </div>
    </div>
  );
};

export default Player;