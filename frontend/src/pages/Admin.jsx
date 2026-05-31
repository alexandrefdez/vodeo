import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { catalogApi, streamingApi } from '../api/axiosConfig';
import toast from 'react-hot-toast';

const Admin = () => {
  const navigate = useNavigate();

  const [searchQuery, setSearchQuery] = useState('');
  const [isSearching, setIsSearching] = useState(false);
  const [searchMessage, setSearchMessage] = useState(null);
  const [isSaving, setIsSaving] = useState(false);
  const [genreInput, setGenreInput] = useState('');

  const [formData, setFormData] = useState({
    title: '',
    year: '',
    description: '',
    duration: '',
    posterUrl: '',
    videoFile: null,
    genres: [] 
  });

  const handleSearchTMDB = async (e) => {
    e.preventDefault();
    if (!searchQuery.trim()) return;

    setIsSearching(true);
    setSearchMessage(null);

    try {
      const response = await catalogApi.get(`/movies/fetch?title=${searchQuery}`);
      const movieData = response.data;
      
      setFormData({
        ...formData,
        title: movieData.title || '',
        year: movieData.year || '',
        description: movieData.description || '',
        posterUrl: movieData.posterUrl || '',
        duration: movieData.duration || '',
        genres: movieData.genres || [], 
      });
      
      setSearchMessage({ type: 'success', text: '¡Metadatos importados con éxito!' });
    } catch (error) {
      console.error(error);
      setSearchMessage({ type: 'error', text: 'No se encontró la película.' });
    } finally {
      setIsSearching(false);
    }
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
    setFormData({
      ...formData,
      genres: formData.genres.filter(g => g !== genreToRemove)
    });
  };

  const handleSaveMovie = async (e) => {
    e.preventDefault();
    
    if (!formData.videoFile) {
      toast.error("Por favor, selecciona un archivo de vídeo .mp4 primero.");
      return;
    }

    setIsSaving(true);
    const loadingToast = toast.loading("Subiendo vídeo al servidor, por favor espera...");

    try {
      const videoData = new FormData();
      videoData.append('file', formData.videoFile);

      const uploadResponse = await streamingApi.post('/upload', videoData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      });

      const uploadedVideoPath = uploadResponse.data.videoPath; 

      await catalogApi.post('/movies', {
        title: formData.title,
        description: formData.description,
        year: parseInt(formData.year),
        duration: parseInt(formData.duration),
        posterUrl: formData.posterUrl,
        videoPath: uploadedVideoPath, 
        genres: formData.genres 
      });

      toast.dismiss(loadingToast);
      toast.success("¡Película guardada y subida con éxito!");
      navigate('/catalog');

    } catch (error) {
      console.error("Error al guardar la película:", error);
      toast.dismiss(loadingToast);
      toast.error("Error al conectar con el servidor. Inténtalo de nuevo.");
    } finally {
      setIsSaving(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-900 text-white p-10">
      <div className="flex justify-between items-center mb-10 border-b border-slate-800 pb-4 max-w-5xl mx-auto">
        <h1 className="text-3xl font-bold text-emerald-500 tracking-widest">PANEL DE CONTROL</h1>
        <button 
          onClick={() => navigate('/catalog')}
          className="text-slate-400 hover:text-white transition-colors border border-slate-700 px-4 py-2 rounded cursor-pointer"
        >
          &larr; Volver al Catálogo
        </button>
      </div>

      <div className="max-w-5xl mx-auto grid grid-cols-1 md:grid-cols-3 gap-8">
        <div className="md:col-span-1 space-y-6">
          <div className="bg-slate-800 border border-slate-700 p-6 rounded-lg shadow-xl">
            <h2 className="text-lg font-bold text-sky-400 mb-4">1. Importar de TMDB</h2>
            <form onSubmit={handleSearchTMDB} className="flex flex-col gap-3">
              <input
                type="text"
                placeholder="Ej: Matrix"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="w-full p-3 bg-slate-900 border border-slate-700 rounded focus:outline-none focus:border-sky-500"
              />
              <button 
                type="submit"
                disabled={isSearching}
                className="bg-sky-500 hover:bg-sky-600 disabled:bg-slate-700 text-white font-bold py-2 px-4 rounded transition-colors cursor-pointer"
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

          {formData.posterUrl && (
            <div className="bg-slate-800 border border-slate-700 p-4 rounded-lg shadow-xl flex flex-col items-center">
              <span className="text-sm text-slate-400 mb-2">Previsualización de Carátula</span>
              <img src={formData.posterUrl} alt="Poster" className="w-full max-w-[200px] rounded shadow-lg" />
            </div>
          )}
        </div>

        <div className="md:col-span-2">
          <div className="bg-slate-800 border border-slate-700 p-6 rounded-lg shadow-xl">
            <h2 className="text-lg font-bold text-sky-400 mb-6">2. Revisar y Subir</h2>
            <form onSubmit={handleSaveMovie} className="flex flex-col gap-5">
              <div className="grid grid-cols-4 gap-4">
                <div className="col-span-3">
                  <label className="block text-sm text-slate-400 mb-1">Título</label>
                  <input type="text" name="title" value={formData.title} onChange={handleInputChange} className="w-full p-3 bg-slate-900 border border-slate-700 rounded focus:outline-none focus:border-sky-500" required />
                </div>
                <div className="col-span-1">
                  <label className="block text-sm text-slate-400 mb-1">Año</label>
                  <input type="number" name="year" value={formData.year} onChange={handleInputChange} className="w-full p-3 bg-slate-900 border border-slate-700 rounded focus:outline-none focus:border-sky-500" required />
                </div>
              </div>

              <div>
                <label className="block text-sm text-slate-400 mb-1">Sinopsis</label>
                <textarea name="description" value={formData.description} onChange={handleInputChange} rows="4" className="w-full p-3 bg-slate-900 border border-slate-700 rounded focus:outline-none focus:border-sky-500" required />
              </div>

              <div>
                <label className="block text-sm text-slate-400 mb-2">Géneros / Etiquetas</label>
                <div className="flex gap-2 mb-3">
                  <input 
                    type="text" 
                    placeholder="Ej: Thriller" 
                    value={genreInput}
                    onChange={(e) => setGenreInput(e.target.value)}
                    onKeyDown={(e) => {
                      if (e.key === 'Enter') {
                        e.preventDefault(); 
                        handleAddGenre();
                      }
                    }}
                    className="flex-grow p-3 bg-slate-900 border border-slate-700 rounded focus:outline-none focus:border-sky-500"
                  />
                  <button 
                    type="button"
                    onClick={handleAddGenre}
                    className="bg-slate-700 hover:bg-slate-600 text-white font-bold py-2 px-6 rounded transition-colors cursor-pointer"
                  >
                    Añadir
                  </button>
                </div>
                
                <div className="flex flex-wrap gap-2">
                  {formData.genres.map((genre, index) => (
                    <span key={index} className="flex items-center gap-2 bg-sky-500/20 text-sky-300 border border-sky-500/30 px-3 py-1 rounded-full text-sm font-semibold group">
                      {genre}
                      <button 
                        type="button" 
                        onClick={() => handleRemoveGenre(genre)}
                        className="text-sky-300/50 group-hover:text-red-400 hover:text-red-500 focus:outline-none transition-colors"
                        title="Eliminar género"
                      >
                        ✕
                      </button>
                    </span>
                  ))}
                  {formData.genres.length === 0 && (
                    <span className="text-slate-500 text-sm italic">Ningún género asignado...</span>
                  )}
                </div>
              </div>

              <div className="grid grid-cols-2 gap-4 mt-2">
                <div>
                  <label className="block text-sm text-slate-400 mb-1">Duración (Minutos)</label>
                  <input type="number" name="duration" value={formData.duration} onChange={handleInputChange} className="w-full p-3 bg-slate-900 border border-slate-700 rounded focus:outline-none focus:border-sky-500" required />
                </div>
                <div>
                  <label className="block text-sm text-slate-400 mb-1">Archivo de Vídeo (.mp4)</label>
                  <input type="file" accept="video/mp4" onChange={handleFileChange} className="w-full p-2 bg-slate-900 border border-slate-700 rounded text-slate-300 file:mr-4 file:py-1 file:px-3 file:rounded file:border-0 file:text-sm file:font-semibold file:bg-sky-500 file:text-white hover:file:bg-sky-600" required />
                </div>
              </div>

              <button 
                type="submit" 
                disabled={isSaving}
                className="mt-6 bg-emerald-500 hover:bg-emerald-600 disabled:bg-slate-700 text-white font-bold py-4 rounded transition-colors text-lg cursor-pointer"
              >
                {isSaving ? 'Subiendo vídeo al servidor...' : 'Guardar Película'}
              </button>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Admin;