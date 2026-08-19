import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    proxy: {
      // Debug tool only: forwards to the Java HttpServer from `mvn exec:java`.
      '/api': 'http://localhost:8080',
    },
  },
})
