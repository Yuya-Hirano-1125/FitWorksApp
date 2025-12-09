import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: {
    // デフォルトのポートは5173ですが、必要なら3000に変更も可能
    port: 3000, 
    proxy: {
      '/api': {
        target: 'http://localhost:8085', // Spring Bootのポート
        changeOrigin: true,
        // Spring側が /api プレフィックスを持たない場合、書き換えが必要なことがあります
        // rewrite: (path) => path.replace(/^\/api/, ''), 
      },
      // もしAPIパスが /api 始まりでない場合は、必要に応じて設定を追加してください
      // 例: '/users' など
    },
  },
});