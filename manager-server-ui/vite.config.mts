import vue from "@vitejs/plugin-vue";
import { resolve } from "path";
import { visualizer } from "rollup-plugin-visualizer";
import { defineConfig, loadEnv } from "vite";
import { viteStaticCopy } from "vite-plugin-static-copy";

import postcss from "./postcss.config";

const frontendDir = resolve(__dirname, "src");
const outDir = resolve(__dirname, "../spring-boot-starter-manager-server/src/main/resources/META-INF/server-ui/");

export default defineConfig(({ mode }) => {
  process.env = { ...process.env, ...loadEnv(mode, process.cwd()) };

  return {
    base: "./",
    define: {
      __VUE_PROD_DEVTOOLS__: process.env.NODE_ENV === "development",
      __PROJECT_VERSION__: JSON.stringify(
        `${process.env.PROJECT_VERSION || "0.0.0"}`
      )
    },
    plugins: [
      vue(),
      visualizer(() => {
        return {
          filename: resolve(__dirname, "target/vite.bundle-size-analyzer.html")
        };
      }),
      viteStaticCopy({
        targets: [
          {
            src: ["settings.js", "assets/"],
            dest: outDir
          }
        ]
      })
    ],
    css: {
      postcss
    },
    root: frontendDir,
    build: {
      target: "es2020",
      outDir,
      rollupOptions: {
        input: {
          sm: resolve(frontendDir, "./index.html"),
          login: resolve(frontendDir, "./login.html")
        },
        external: ["settings.js"]
      }
    },
    resolve: {
      alias: {
        "@": frontendDir
      },
      extensions: [".vue", ".js", ".json", ".ts"]
    },
    server: {
      proxy: {
        "^/api/(login|logout)": {
          target: "http://localhost:8080/admin",
          changeOrigin: true
        },
        "^/api/(applications|instances/)": {
          target: "http://localhost:8080/admin",
          changeOrigin: true,
          bypass: (req) => {
            const isEventStream = req.headers.accept === "text/event-stream";
            const isAjaxCall =
              req.headers["x-requested-with"] === "XMLHttpRequest";
            const isFile = req.url.indexOf(".js") !== -1;
            const redirectToIndex = !(isAjaxCall || isEventStream) && !isFile;
            if (redirectToIndex) {
              return "/index.html";
            }
          }
        }
      }
    }
  };
});
