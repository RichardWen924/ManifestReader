// @author Richard
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    host: '0.0.0.0',
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
