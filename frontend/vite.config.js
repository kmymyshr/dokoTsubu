import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import { resolve } from "node:path";

export default defineConfig({
  plugins: [react()],
  publicDir: false,
  build: {
    outDir: resolve(import.meta.dirname, "../target/frontend-dist"),
    emptyOutDir: true,
    rollupOptions: {
      input: resolve(import.meta.dirname, "src/main.jsx"),
      output: {
        entryFileNames: "assets/main.js",
        chunkFileNames: "assets/[name].js",
        assetFileNames: assetInfo =>
          assetInfo.names?.some(name => name.endsWith(".css"))
            ? "assets/main.css"
            : "assets/[name][extname]"
      }
    }
  }
});
