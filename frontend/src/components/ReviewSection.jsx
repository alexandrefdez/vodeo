import { useState, useEffect, useContext } from 'react';
import { catalogApi } from '../api/axiosConfig';
import { AuthContext } from '../context/AuthContext';
import toast from 'react-hot-toast';

const ReviewSection = ({ mediaId, mediaType }) => {
  const { user } = useContext(AuthContext);
  
  const [reviews, setReviews] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);

  const [rating, setRating] = useState(0);
  const [hoverRating, setHoverRating] = useState(0);
  const [comment, setComment] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const fetchReviews = async (pageNum = 0) => {
    try {
      const res = await catalogApi.get(`/reviews/${mediaType}/${mediaId}?page=${pageNum}&size=5`);
      if (pageNum === 0) {
        setReviews(res.data.content);
      } else {
        setReviews(prev => [...prev, ...res.data.content]);
      }
      setTotalPages(res.data.totalPages);
    } catch (error) {
      console.error("Error al cargar reseñas", error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (mediaId && mediaType) {
      fetchReviews(0);
    }
  }, [mediaId, mediaType]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!user) return toast.error("Debes iniciar sesión para opinar.");
    if (rating === 0) return toast.error("Por favor, selecciona una puntuación (estrellas).");
    
    setIsSubmitting(true);
    const toastId = toast.loading("Publicando valoración...");

    try {
      const payload = { mediaId, mediaType, rating, comment };
      const res = await catalogApi.post('/reviews', payload);
      
      setReviews([res.data, ...reviews]);
      setRating(0);
      setComment('');
      toast.success("¡Valoración publicada con éxito!", { id: toastId });
    } catch (error) {
      if (error.response && error.response.status === 409) {
        toast.error("Ya has valorado este título anteriormente.", { id: toastId });
      } else {
        toast.error("Error al publicar la valoración.", { id: toastId });
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  // Borrado de reseñas por el Admin
  const handleDeleteReview = async (reviewId) => {
    if (!window.confirm("¿Seguro que quieres borrar esta reseña?")) return;
    
    try {
      await catalogApi.delete(`/reviews/${reviewId}`);
      setReviews(reviews.filter(rev => rev.id !== reviewId));
      toast.success("Reseña eliminada (Moderación)");
    } catch (error) {
      toast.error("Error al eliminar la reseña");
    }
  };

  const renderStars = (currentRating) => {
    return [...Array(5)].map((_, index) => (
      <span key={index} className={`text-xl ${index < currentRating ? 'text-yellow-400' : 'text-slate-600'}`}>
        ★
      </span>
    ));
  };

  return (
    <div className="mt-16 border-t border-slate-800 pt-10">
      <h2 className="text-3xl font-bold text-white mb-8 flex items-center gap-3">
        <span>Valoraciones y Reseñas</span>
        <span className="bg-slate-800 text-slate-300 text-sm px-3 py-1 rounded-full font-normal border border-slate-700">
          {reviews.length}
        </span>
      </h2>

      {user ? (
        <div className="bg-slate-800/50 border border-slate-700 p-6 rounded-xl mb-10 shadow-lg">
          <h3 className="text-lg font-bold text-slate-200 mb-4">Deja tu valoración</h3>
          <form onSubmit={handleSubmit} className="flex flex-col gap-4">
            
            <div className="flex items-center gap-2">
              <span className="text-sm text-slate-400 mr-2">Tu nota:</span>
              <div className="flex cursor-pointer" onMouseLeave={() => setHoverRating(0)}>
                {[1, 2, 3, 4, 5].map((star) => (
                  <button
                    key={star} type="button"
                    onClick={() => setRating(star)}
                    onMouseEnter={() => setHoverRating(star)}
                    className="focus:outline-none transition-transform hover:scale-110 cursor-pointer"
                  >
                    <span className={`text-3xl transition-colors ${(hoverRating || rating) >= star ? 'text-yellow-400 drop-shadow-[0_0_8px_rgba(250,204,21,0.5)]' : 'text-slate-600'}`}>
                      ★
                    </span>
                  </button>
                ))}
              </div>
            </div>

            <textarea
              placeholder="¿Quieres añadir un comentario? (Opcional)"
              value={comment}
              onChange={(e) => setComment(e.target.value)}
              rows="3"
              className="w-full p-4 bg-slate-900 border border-slate-700 rounded-lg text-white focus:outline-none focus:border-sky-500 resize-none transition-colors"
            />
            
            <div className="flex justify-end">
              <button 
                type="submit" disabled={isSubmitting}
                className="bg-sky-500 hover:bg-sky-600 text-white font-bold py-2 px-8 rounded-lg shadow-lg shadow-sky-500/20 transition-all disabled:opacity-50 disabled:cursor-not-allowed cursor-pointer"
              >
                {isSubmitting ? 'Enviando...' : 'Publicar'}
              </button>
            </div>
          </form>
        </div>
      ) : (
        <div className="bg-slate-800/30 border border-slate-700 p-6 rounded-xl mb-10 text-center">
          <p className="text-slate-400">Debes iniciar sesión para valorar.</p>
        </div>
      )}

      {loading ? (
        <div className="text-center py-10 text-slate-500 animate-pulse font-bold">Cargando valoraciones...</div>
      ) : reviews.length === 0 ? (
        <div className="text-center py-10 bg-slate-900/50 rounded-xl border border-dashed border-slate-700">
          <p className="text-slate-500 italic">Aún no hay valoraciones. ¡Sé el primero!</p>
        </div>
      ) : (
        <div className="flex flex-col gap-4">
          {reviews.map((rev) => (
            <div key={rev.id} className="bg-slate-900/80 border border-slate-800 p-5 rounded-lg hover:border-slate-700 transition-colors relative group">
              
              {/* BOTÓN BORRAR (SOLO ADMIN)*/}
              {user?.role === 'ADMIN' && (
                <button 
                  onClick={() => handleDeleteReview(rev.id)}
                  className="absolute top-4 right-4 z-10 p-2 text-slate-500 hover:text-red-400 bg-slate-800 hover:bg-red-500/10 rounded-full border border-transparent hover:border-red-500/30 opacity-0 group-hover:opacity-100 transition-all cursor-pointer flex items-center justify-center shadow-sm"
                  title="Borrar Reseña (Moderación)"
                >
                  <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 pointer-events-none" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                  </svg>
                </button>
              )}

              <div className="flex justify-between items-start mb-3">
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 bg-gradient-to-br from-sky-500 to-indigo-600 rounded-full flex items-center justify-center text-white font-bold text-lg shadow-md">
                    {rev.username.charAt(0).toUpperCase()}
                  </div>
                  <div>
                    <p className="font-bold text-slate-200">{rev.username}</p>
                    <p className="text-xs text-slate-500">
                      {new Date(rev.createdAt).toLocaleDateString('es-ES', { year: 'numeric', month: 'long', day: 'numeric' })}
                    </p>
                  </div>
                </div>
                <div className="flex gap-1 drop-shadow-sm pr-12">
                  {renderStars(rev.rating)}
                </div>
              </div>
              
              {/* Solo pinta el comentario si el usuario escribió algo */}
              {rev.comment && rev.comment.trim() !== '' && (
                <p className="text-slate-300 text-sm leading-relaxed whitespace-pre-line pl-13">
                  {rev.comment}
                </p>
              )}
            </div>
          ))}
          
          {page < totalPages - 1 && (
            <button 
              onClick={() => { setPage(page + 1); fetchReviews(page + 1); }}
              className="mt-4 py-3 bg-slate-800 hover:bg-slate-700 text-white font-bold rounded-lg border border-slate-700 transition-colors w-full sm:w-auto sm:self-center sm:px-10 cursor-pointer"
            >
              Cargar más valoraciones
            </button>
          )}
        </div>
      )}
    </div>
  );
};

export default ReviewSection;