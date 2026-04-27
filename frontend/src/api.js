// VITE_API_URL is injected at build time:
//   - Docker build: VITE_API_URL=/api  (nginx proxies to backend container)
//   - Render build: VITE_API_URL=https://dbms-khzg.onrender.com/api
//   - Local dev: falls back to /api  (vite.config.js proxy handles it)
const API = import.meta.env.VITE_API_URL || "/api";
export default API;