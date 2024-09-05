import vue from '@vitejs/plugin-vue';
import { resolve } from 'path';
import { defineConfig, loadEnv } from 'vite';
import { viteStaticCopy } from 'vite-plugin-static-copy';
import AutoImport from 'unplugin-auto-import/vite';
import Components from 'unplugin-vue-components/vite';
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers';

import postcss from './postcss.config';

import path from 'path'
const frontendDir = resolve(__dirname, 'src');
const outDir = resolve(__dirname, '../spring-boot-starter-manager-server/src/main/resources/META-INF/server-ui/');

export default defineConfig(({ mode }) => {
  process.env = { ...process.env, ...loadEnv(mode, process.cwd()) };

  return {
    base: './',
    css: {
      postcss,
    },
    plugins: [
      vue(),
      viteStaticCopy({
        targets: [
          {
            src: ['settings.js', 'assets/'],
            dest: outDir
          }
        ]
      }),
      AutoImport({
        resolvers: [
          // 自动导入 Element Plus 相关函数，如：ElMessage, ElMessageBox... (带样式)
          ElementPlusResolver(),
        ],
        dts: path.resolve(frontendDir, 'types', 'auto-imports.d.ts')
      }),
      Components({
        resolvers: [ElementPlusResolver()],
        dts: path.resolve(frontendDir, 'types', 'components.d.ts')
      }),
    ],
    root: frontendDir,
    build: {
      target: 'es2020',
      outDir,
      rollupOptions: {
        input: {
          sm: resolve(frontendDir, './index.html'),
          login: resolve(frontendDir, './login.html')
        },
        external: ['settings.js']
      }
    },
    resolve: {
      alias: {
        '@': frontendDir
      },
      extensions: ['.vue', '.js', '.json', '.ts']
    },
    server: {
      proxy: {
        '^/api/(login|logout)': {
          target: 'http://localhost:8080/admin',
          changeOrigin: true
        },
        '^/api/(applications|instances/)': {
          target: 'http://localhost:8080/admin',
          changeOrigin: true,
          bypass: (req) => {
            const isEventStream = req.headers.accept === 'text/event-stream';
            const isAjaxCall =
              req.headers['x-requested-with'] === 'XMLHttpRequest';
            const isFile = req.url.indexOf('.js') !== -1;
            const redirectToIndex = !(isAjaxCall || isEventStream) && !isFile;
            if (redirectToIndex) {
              return '/index.html';
            }
          }
        }
      }
    }
  };
});
