import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
  server: {
    /*https: {
      key: './finances-privateKey.key',
      cert: './finances.crt'
    }*/
  },
  plugins: [vue()],
})
