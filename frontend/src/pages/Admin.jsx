import { useState, useEffect, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import { catalogApi, streamingApi } from '../api/axiosConfig';
import { AuthContext } from '../context/AuthContext';
import toast from 'react-hot-toast';

const Admin = () => {
  const navigate = useNavigate();
  const { user } = useContext(AuthContext);

  const [contentType, setContentType] = useState('movie'); // 'movie' o 'series'

  const [searchQuery, setSearchQuery] = useState('');
  const [isSearching, setIsSearching] = useState(false);
  const [searchMessage, setSearchMessage] = useState(null);
  const [searchResults, setSearchResults] = useState([]);
  
  const [isSaving, setIsSaving] = useState(false);
  const [genreInput, setGenreInput] = useState('');

  const [formData, setFormData] = useState({
    title: '', year: '', description: '', duration: '',
    posterUrl: '', backdropUrl: '', tmdbRating: '', contentRating: '',
    director: '', castCrew: '', videoFile: null, genres: [] 
  });

  useEffect(() => {
    if (!user || user.role !== 'ADMIN') navigate('/catalog');
  }, [user, navigate]);

  // Limpiar formulario al cambiar entre Películas y Series
  useEffect(() => {
    setSearchQuery('');
    setSearchResults([]);
    setSearchMessage(null);
    setFormData({
      title: '', year: '', description: '', duration: '',
      posterUrl: '', backdropUrl: '', tmdbRating: '', contentRating: '',
      director: '', castCrew: '', videoFile: null, genres: []
    });
  }, [contentType]);

  const handleSearchTMDB = async (e) => {
    e.preventDefault();
    if (!searchQuery.trim()) return;

    setIsSearching(true);
    setSearchMessage(null);
    setSearchResults([]);

    try {
      const endpoint = contentType === 'movie' ? '/movies/fetch' : '/series/fetch';
      const response = await catalogApi.get(`${endpoint}?title=${searchQuery}`);
      setSearchResults(response.data);
      setSearchMessage({ type: 'success', text: `Se encontraron ${response.data.length} coincidencias. Elige una.` });
    } catch (error) {
      console.error(error);
      setSearchMessage({ type: 'error', text: `No se encontró ninguna ${contentType === 'movie' ? 'película' : 'serie'} con ese título.` });
    } finally {
      setIsSearching(false);
    }
  };

  const handleSelectMovie = (data) => {
    setFormData({
      ...formData,
      title: data.title || '',
      year: data.year || data.startYear || '', 
      description: data.description || '',
      posterUrl: data.posterUrl || '',
      backdropUrl: data.backdropUrl || '',
      tmdbRating: data.tmdbRating || '',
      contentRating: data.contentRating || '',
      director: data.director || '',
      castCrew: data.castCrew || '',
      duration: data.duration || '',
      genres: data.genres || [], 
    });
    setSearchResults([]);
    toast.success("Datos precargados en el formulario");
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
  };

  const handleFileChange = (e) => {
    setFormData({ ...formData, videoFile: e.target.files[0] });
  };

  const handleAddGenre = () => {
    const trimmed = genreInput.trim();
    if (trimmed && !formData.genres.includes(trimmed)) {
      setFormData({ ...formData, genres: [...formData.genres, trimmed] });
    }
    setGenreInput('');
  };

  const handleRemoveGenre = (genreToRemove) => {
    setFormData({ ...formData, genres: formData.genres.filter(g => g !== genreToRemove) });
  };

  const handleSaveMovie = async (e) => {
    e.preventDefault();
    
    if (contentType === 'movie' && !formData.videoFile) {
      toast.error("Por favor, selecciona un archivo de vídeo .mp4 primero.");
      return;
    }

    setIsSaving(true);
    const loadingToast = toast.loading(`Guardando ${contentType === 'movie' ? 'película' : 'serie'}...`);

    try {
      let uploadedVideoPath = null;

      if (contentType === 'movie') {
        toast.loading("Subiendo vídeo al servidor...", { id: loadingToast });
        const videoData = new FormData();
        videoData.append('file', formData.videoFile);

        const uploadResponse = await streamingApi.post('/upload', videoData, {
          headers: { 'Content-Type': 'multipart/form-data' }
        });
        uploadedVideoPath = uploadResponse.data.videoPath; 
      }

      const payload = {
        title: formData.title,
        description: formData.description,
        posterUrl: formData.posterUrl,
        backdropUrl: formData.backdropUrl,
        tmdbRating: parseFloat(formData.tmdbRating) || 0,
        contentRating: formData.contentRating,
        director: formData.director,
        castCrew: formData.castCrew,
        genres: formData.genres 
      };

      if (contentType === 'movie') {
        payload.year = parseInt(formData.year);
        payload.duration = parseInt(formData.duration);
        payload.videoPath = uploadedVideoPath;
        await catalogApi.post('/movies', payload);
      } else {
        payload.startYear = parseInt(formData.year);
        await catalogApi.post('/series', payload);
      }

      toast.dismiss(loadingToast);
      toast.success(`¡${contentType === 'movie' ? 'Película' : 'Serie'} guardada con éxito!`);
      navigate('/catalog');
    } catch (error) {
      console.error("Error al guardar:", error);
      toast.dismiss(loadingToast);
      toast.error("Error al conectar con el servidor.");
    } finally {
      setIsSaving(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-900 text-white p-10 font-sans">
      
      {/* HEADER PRINCIPAL */}
      <div className="flex justify-between items-center mb-8 border-b border-slate-800 pb-4 max-w-6xl mx-auto">
        <h1 className="text-3xl font-bold text-sky-500 tracking-widest">PANEL DE CONTROL</h1>
        
        <div className="flex gap-4 items-center">
          
          {/* BOTÓN NUEVO PARA IR A LA SUB-PANTALLA DE DASHBOARD */}
          <button 
            onClick={() => navigate('/admin/dashboard')}
            className="bg-indigo-500/10 text-indigo-400 border border-indigo-500 hover:bg-indigo-500 hover:text-white transition-colors px-4 py-2 rounded cursor-pointer font-bold flex items-center gap-2"
          >
            📊 Ver Estadísticas
          </button>

          <button 
            onClick={() => navigate('/catalog')}
            className="text-slate-400 hover:text-white transition-colors border border-slate-700 px-4 py-2 rounded cursor-pointer font-bold bg-slate-800"
          >
            &larr; Volver al Catálogo
          </button>
        </div>
      </div>

      {/* SELECTOR DE TIPO DE CONTENIDO */}
      <div className="max-w-6xl mx-auto flex justify-center mb-8">
        <div className="bg-slate-800 p-1 rounded-lg border border-slate-700 flex shadow-lg">
          <button
            onClick={() => setContentType('movie')}
            className={`px-8 py-2 rounded-md font-bold transition-all cursor-pointer ${contentType === 'movie' ? 'bg-sky-500 text-white shadow-md' : 'text-slate-400 hover:text-white'}`}
          >
            🎬 Películas
          </button>
          <button
            onClick={() => setContentType('series')}
            className={`px-8 py-2 rounded-md font-bold transition-all cursor-pointer ${contentType === 'series' ? 'bg-emerald-500 text-white shadow-md' : 'text-slate-400 hover:text-white'}`}
          >
            📺 Series de TV
          </button>
        </div>
      </div>

      <div className="max-w-6xl mx-auto grid grid-cols-1 lg:grid-cols-3 gap-8">
        
        {/* COLUMNA IZQUIERDA: BUSCADOR Y PÓSTER */}
        <div className="lg:col-span-1 space-y-6">
          <div className="bg-slate-800 border border-slate-700 p-6 rounded-lg shadow-xl">
            <h2 className={`text-lg font-bold mb-4 ${contentType === 'movie' ? 'text-sky-400' : 'text-emerald-400'}`}>
              1. Importar Metadatos
            </h2>
            <form onSubmit={handleSearchTMDB} className="flex flex-col gap-3">
              <input
                type="text" placeholder={`Ej: ${contentType === 'movie' ? 'Matrix' : 'Breaking Bad'}`} value={searchQuery} onChange={(e) => setSearchQuery(e.target.value)}
                className={`w-full p-3 bg-slate-900 border border-slate-700 rounded focus:outline-none ${contentType === 'movie' ? 'focus:border-sky-500' : 'focus:border-emerald-500'}`}
              />
              <button 
                type="submit" disabled={isSearching}
                className={`text-white font-bold py-2 px-4 rounded transition-colors cursor-pointer disabled:bg-slate-700 ${contentType === 'movie' ? 'bg-sky-500 hover:bg-sky-600' : 'bg-emerald-500 hover:bg-emerald-600'}`}
              >
                {isSearching ? 'Buscando...' : 'Buscar'}
              </button>
            </form>
            {searchMessage && (
              <div className={`mt-4 p-3 rounded text-sm ${searchMessage.type === 'success' ? 'bg-emerald-500/20 text-emerald-300' : 'bg-red-500/20 text-red-300'}`}>
                {searchMessage.text}
              </div>
            )}
          </div>

          {searchResults.length > 0 && (
            <div className="bg-slate-800 border border-slate-700 p-4 rounded-lg shadow-xl max-h-96 overflow-y-auto custom-scrollbar">
              <h3 className="text-sm font-bold text-slate-400 mb-3">Elige la correcta:</h3>
              <div className="flex flex-col gap-3">
                {searchResults.map((result, index) => (
                  <div 
                    key={index} onClick={() => handleSelectMovie(result)}
                    className={`flex items-center gap-3 bg-slate-900 p-2 rounded border border-slate-700 hover:bg-slate-800 cursor-pointer transition-colors ${contentType === 'movie' ? 'hover:border-sky-500' : 'hover:border-emerald-500'}`}
                  >
                    <img src={result.posterUrl || 'https://via.placeholder.com/50x75?text=NA'} alt="poster" className="w-12 h-16 object-cover rounded shrink-0" />
                    <div className="flex flex-col overflow-hidden w-full">
                      <span className={`font-bold text-sm truncate ${contentType === 'movie' ? 'text-sky-300' : 'text-emerald-300'}`}>{result.title}</span>
                      <span className="text-xs text-slate-400 mt-1">{result.year || result.startYear} • {result.director || 'Sin creador'}</span>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {formData.posterUrl && searchResults.length === 0 && (
            <div className="bg-slate-800 border border-slate-700 p-4 rounded-lg shadow-xl flex flex-col items-center">
              <span className="text-sm text-slate-400 mb-2 font-bold uppercase">Carátula Seleccionada</span>
              <img src={formData.posterUrl} alt="Poster" className="w-full max-w-[200px] rounded shadow-lg border border-slate-700" />
            </div>
          )}
        </div>

        {/* COLUMNA DERECHA: FORMULARIO */}
        <div className="lg:col-span-2">
          <div className="bg-slate-800 border border-slate-700 p-6 rounded-lg shadow-xl">
            <h2 className={`text-lg font-bold mb-6 ${contentType === 'movie' ? 'text-sky-400' : 'text-emerald-400'}`}>
              2. Revisar y Subir
            </h2>
            <form onSubmit={handleSaveMovie} className="flex flex-col gap-5">
              
              <div className="grid grid-cols-4 gap-4">
                <div className="col-span-3">
                  <label className="block text-sm text-slate-400 mb-1 font-bold">Título</label>
                  <input type="text" name="title" value={formData.title} onChange={handleInputChange} className={`w-full p-3 bg-slate-900 border border-slate-700 rounded focus:outline-none ${contentType === 'movie' ? 'focus:border-sky-500' : 'focus:border-emerald-500'}`} required />
                </div>
                <div className="col-span-1">
                  <label className="block text-sm text-slate-400 mb-1 font-bold">{contentType === 'movie' ? 'Año' : 'Año Inicio'}</label>
                  <input type="number" name="year" value={formData.year} onChange={handleInputChange} className={`w-full p-3 bg-slate-900 border border-slate-700 rounded focus:outline-none ${contentType === 'movie' ? 'focus:border-sky-500' : 'focus:border-emerald-500'}`} required />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm text-slate-400 mb-1 font-bold">{contentType === 'movie' ? 'Director' : 'Creador'}</label>
                  <input type="text" name="director" value={formData.director} onChange={handleInputChange} className={`w-full p-3 bg-slate-900 border border-slate-700 rounded focus:outline-none ${contentType === 'movie' ? 'focus:border-sky-500' : 'focus:border-emerald-500'}`} />
                </div>
                <div>
                  <label className="block text-sm text-slate-400 mb-1 font-bold">Reparto principal</label>
                  <input type="text" name="castCrew" value={formData.castCrew} onChange={handleInputChange} className={`w-full p-3 bg-slate-900 border border-slate-700 rounded focus:outline-none ${contentType === 'movie' ? 'focus:border-sky-500' : 'focus:border-emerald-500'}`} />
                </div>
              </div>

              <div>
                <label className="block text-sm text-slate-400 mb-1 font-bold">Sinopsis</label>
                <textarea name="description" value={formData.description} onChange={handleInputChange} rows="3" className={`w-full p-3 bg-slate-900 border border-slate-700 rounded focus:outline-none ${contentType === 'movie' ? 'focus:border-sky-500' : 'focus:border-emerald-500'} resize-none`} required />
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4 p-4 bg-slate-900/50 rounded border border-slate-700/50">
                <div className="flex flex-col">
                  <label className="text-xs text-slate-400 mb-1 font-bold uppercase">URL del Póster (Vertical)</label>
                  <input type="url" name="posterUrl" value={formData.posterUrl} onChange={handleInputChange} placeholder="https://ejemplo.com/poster.jpg" className={`p-2 bg-slate-900 border border-slate-700 rounded text-slate-300 text-sm focus:outline-none ${contentType === 'movie' ? 'focus:border-sky-500' : 'focus:border-emerald-500'}`} />
                </div>
                <div className="flex flex-col">
                  <label className="text-xs text-slate-400 mb-1 font-bold uppercase">URL del Fondo (Horizontal)</label>
                  <input type="url" name="backdropUrl" value={formData.backdropUrl} onChange={handleInputChange} placeholder="https://ejemplo.com/fondo.jpg" className={`p-2 bg-slate-900 border border-slate-700 rounded text-slate-300 text-sm focus:outline-none ${contentType === 'movie' ? 'focus:border-sky-500' : 'focus:border-emerald-500'}`} />
                </div>
              </div>

              <div>
                <label className="block text-sm text-slate-400 mb-2 font-bold">Géneros / Etiquetas</label>
                <div className="flex gap-2 mb-3">
                  <input 
                    type="text" placeholder="Ej: Thriller" value={genreInput} onChange={(e) => setGenreInput(e.target.value)}
                    onKeyDown={(e) => { if (e.key === 'Enter') { e.preventDefault(); handleAddGenre(); } }}
                    className={`flex-grow p-3 bg-slate-900 border border-slate-700 rounded focus:outline-none ${contentType === 'movie' ? 'focus:border-sky-500' : 'focus:border-emerald-500'}`}
                  />
                  <button type="button" onClick={handleAddGenre} className="bg-slate-700 hover:bg-slate-600 text-white font-bold py-2 px-6 rounded transition-colors cursor-pointer border border-slate-600">
                    Añadir
                  </button>
                </div>
                
                <div className="flex flex-wrap gap-2">
                  {formData.genres.map((genre, index) => (
                    <span key={index} className={`flex items-center gap-2 bg-slate-800 border px-3 py-1 rounded-full text-sm font-semibold group ${contentType === 'movie' ? 'text-sky-300 border-sky-500/30' : 'text-emerald-300 border-emerald-500/30'}`}>
                      {genre}
                      <button 
                        type="button" 
                        onClick={() => handleRemoveGenre(genre)}
                        className={`group-hover:text-red-400 hover:text-red-500 focus:outline-none transition-colors cursor-pointer ${contentType === 'movie' ? 'text-sky-300/50' : 'text-emerald-300/50'}`}
                        title="Eliminar género"
                      >
                        ✕
                      </button>
                    </span>
                  ))}
                  {formData.genres.length === 0 && <span className="text-slate-500 text-sm italic">Ningún género asignado...</span>}
                </div>
              </div>

              <div className="grid grid-cols-4 gap-4 mt-2 border-t border-slate-700 pt-4">
                <div className="col-span-1">
                  <label className="block text-xs text-slate-500 mb-1 font-bold uppercase">Nota API</label>
                  <input type="text" value={formData.tmdbRating ? parseFloat(formData.tmdbRating).toFixed(1) : ''} readOnly className="w-full p-2 bg-slate-900/50 border border-slate-800 rounded text-slate-400 text-sm cursor-not-allowed" />
                </div>
                <div className="col-span-1">
                  <label className="block text-xs text-slate-500 mb-1 font-bold uppercase">Edad</label>
                  <input type="text" value={formData.contentRating} readOnly className="w-full p-2 bg-slate-900/50 border border-slate-800 rounded text-slate-400 text-sm cursor-not-allowed" />
                </div>
                
                {contentType === 'movie' && (
                  <div className="col-span-1">
                    <label className="block text-xs text-slate-400 mb-1 font-bold uppercase">Dur. (Min)</label>
                    <input type="number" name="duration" value={formData.duration} onChange={handleInputChange} className="w-full p-2 bg-slate-900 border border-slate-700 rounded focus:outline-none focus:border-sky-500" required />
                  </div>
                )}
              </div>

              {contentType === 'movie' && (
                <div>
                  <label className="block text-sm font-bold text-sky-400 mt-2 mb-2 uppercase tracking-wider">3. Archivo de vídeo (.mp4)</label>
                  <input type="file" accept="video/mp4" onChange={handleFileChange} className="w-full p-2 bg-slate-900 border border-sky-500/50 rounded text-slate-300 file:mr-4 file:py-2 file:px-4 file:rounded file:border-0 file:text-sm file:font-bold file:bg-sky-500 file:text-white hover:file:bg-sky-600 cursor-pointer" required />
                </div>
              )}
              
              {contentType === 'series' && (
                <div className="bg-emerald-500/10 border border-emerald-500/50 p-4 rounded text-sm text-emerald-200 mt-2">
                  <p><strong>Nota:</strong> Los archivos de vídeo de las series se suben episodio a episodio desde la pantalla de detalles de la serie una vez publicada.</p>
                </div>
              )}

              <button 
                type="submit" disabled={isSaving}
                className={`mt-6 text-white font-bold py-4 rounded shadow-lg text-lg cursor-pointer transition-all disabled:opacity-50 disabled:cursor-not-allowed ${contentType === 'movie' ? 'bg-sky-500 hover:bg-sky-600 shadow-sky-500/20' : 'bg-emerald-500 hover:bg-emerald-600 shadow-emerald-500/20'}`}
              >
                {isSaving ? 'Procesando...' : `Publicar ${contentType === 'movie' ? 'Película' : 'Serie'}`}
              </button>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Admin;