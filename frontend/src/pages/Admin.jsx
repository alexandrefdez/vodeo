import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { catalogApi, streamingApi } from '../api/axiosConfig';

const Admin = () => {
  const navigate = useNavigate();

  const [searchQuery, setSearchQuery] = useState('');
  const [isSearching, setIsSearching] = useState(false);
  const [searchMessage, setSearchMessage] = useState(null);
  const [isSaving, setIsSaving] = useState(false);

  const [formData, setFormData] = useState({
    title: '',
    year: '',
    description: '',
    duration: '',
    posterUrl: '',
    videoFile: null
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

  const handleSaveMovie = async (e) => {
    e.preventDefault();
    
    if (!formData.videoFile) {
      alert("Por favor, selecciona un archivo de vídeo .mp4 primero.");
      return;
    }

    setIsSaving(true);

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
        genreIds: [] 
      });

      alert("¡Película guardada y subida con éxito al servidor!");
      navigate('/catalog');

    } catch (error) {
      console.error("Error al guardar la película:", error);
      alert("Ha ocurrido un error al guardar la película. Revisa la consola.");
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

              <div className="grid grid-cols-2 gap-4">
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
                className="mt-4 bg-emerald-500 hover:bg-emerald-600 disabled:bg-slate-700 text-white font-bold py-4 rounded transition-colors text-lg cursor-pointer"
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