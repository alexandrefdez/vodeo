import axios from 'axios';

export const authApi = axios.create({
  baseURL: import.meta.env.VITE_USERS_API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

export const catalogApi = axios.create({
  baseURL: import.meta.env.VITE_CATALOG_API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

export const streamingApi = axios.create({
  baseURL: import.meta.env.VITE_STREAMING_API_URL,
});

catalogApi.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) config.headers.Authorization = `Bearer ${token}`;
    return config;
  },
  (error) => Promise.reject(error)
);

streamingApi.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) config.headers.Authorization = `Bearer ${token}`;
    return config;
  },
  (error) => Promise.reject(error)
);

authApi.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) config.headers.Authorization = `Bearer ${token}`;
    return config;
  },
  (error) => Promise.reject(error)
);