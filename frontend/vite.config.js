/**
 * =====================================================================
 * Vite 构建 / 开发服务器配置
 * =====================================================================
 */
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

// https://vitejs.dev/config/
export default defineConfig({
  // 让 Vite 能编译 .vue 单文件组件
  plugins: [vue()],
  resolve: {
    alias: {
      // 路径别名：'@' 指向 src 目录，代码里可写 @/api、@/views/Home.vue
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    port: 5173,
    // 后端接口统一走 /api 前缀，开发时代理到 Spring Boot（默认 8080）
    // 这样前端不需要处理跨域，生产环境则由 Nginx 等反向代理承担同样职责
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
        // 若后端地址/端口不同，改这里的 target
      }
    }
  }
})