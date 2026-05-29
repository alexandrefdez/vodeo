import { useState, useContext } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { authApi } from '../api/axiosConfig';
import { AuthContext } from '../context/AuthContext';

const Login = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState(null);

  const { login } = useContext(AuthContext); 
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null); 

    try {
      const response = await authApi.post('/login', {
        email: email,
        password: password
      });

      login(response.data.token);
      navigate('/catalog');
      
    } catch (err) {
      console.error("Error en el login:", err);
      setError("Credenciales incorrectas o servidor no disponible.");
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-slate-900">
      <div className="bg-slate-950/80 p-10 rounded-lg shadow-2xl w-full max-w-md border border-slate-800">
        <h1 className="text-4xl font-bold text-sky-500 mb-8 text-center tracking-widest">
          VODEO
        </h1>
        
        {error && (
          <div className="bg-red-500/20 border border-red-500 text-red-200 p-3 rounded mb-6 text-center text-sm">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="flex flex-col gap-6">
          <input
            type="email"
            placeholder="Correo electrónico"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            className="w-full p-4 bg-slate-800 text-white rounded focus:outline-none focus:ring-2 focus:ring-sky-500"
            required
          />
          <input
            type="password"
            placeholder="Contraseña"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            className="w-full p-4 bg-slate-800 text-white rounded focus:outline-none focus:ring-2 focus:ring-sky-500"
            required
          />
          
          <button 
            type="submit" 
            className="w-full py-4 mt-2 bg-sky-500 hover:bg-sky-600 text-white font-bold rounded transition-colors"
          >
            Iniciar Sesión
          </button>
        </form>
        
        <p className="text-slate-400 text-center mt-6">
          ¿Primera vez en Vodeo? <Link to="/register" className="text-white hover:text-sky-400 hover:underline transition-colors">Suscríbete ahora.</Link>
        </p>
      </div>
    </div>
  );
};

export default Login;