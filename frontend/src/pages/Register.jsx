import { useState } from 'react';
import { Link } from 'react-router-dom';

const Register = () => {
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');

  const handleSubmit = (e) => {
    e.preventDefault();
    console.log("Registro form enviado:", { username, email, password });
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-slate-900">
      <div className="bg-slate-950/80 p-10 rounded-lg shadow-2xl w-full max-w-md border border-slate-800">
        <h1 className="text-4xl font-bold text-sky-500 mb-8 text-center tracking-widest">
          VODEO
        </h1>
        
        <form onSubmit={handleSubmit} className="flex flex-col gap-6">
          <input
            type="text"
            placeholder="Nombre de usuario"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            className="w-full p-4 bg-slate-800 text-white rounded focus:outline-none focus:ring-2 focus:ring-sky-500"
            required
          />
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
            Crear cuenta
          </button>
        </form>
        
        <p className="text-slate-400 text-center mt-6">
          ¿Ya tienes cuenta? <Link to="/login" className="text-white hover:text-sky-400 hover:underline transition-colors">Inicia sesión.</Link>
        </p>
      </div>
    </div>
  );
};

export default Register;