import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 8082,
    proxy: {
      '/client-api': {
        target: 'http://localhost:81',
        changeOrigin: true
      },
      '/common/upload': {
        target: 'http://localhost:81',
        changeOrigin: true
      },
      '/profile': {
        target: 'http://localhost:81',
        changeOrigin: true
      }
    }
  }
})
