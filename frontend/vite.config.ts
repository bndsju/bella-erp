import path from 'node:path'
import tailwindcss from '@tailwindcss/vite'
import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'
import { cabecalhosSeguranca } from './cabecalhos-seguranca.ts'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), tailwindcss()],
  build: {
    // Fontes sempre como arquivo: a política de segurança (font-src 'self') não aceita fonte embutida em data:.
    assetsInlineLimit: (arquivo: string) => (/\.(woff2?|ttf|otf)$/.test(arquivo) ? false : undefined),
  },
  // O servidor de desenvolvimento precisa de scripts inline (recarga automática); só o preview usa a política completa.
  // No preview a API ainda é o backend local (porta 8080), então ela entra no connect-src.
  preview: {
    headers: {
      ...cabecalhosSeguranca,
      'Content-Security-Policy': cabecalhosSeguranca['Content-Security-Policy'].replace(
        "connect-src 'self'",
        "connect-src 'self' http://localhost:8080",
      ),
    },
  },
  resolve: {
    alias: {
      '@': path.resolve(import.meta.dirname, './src'),
    },
  },
})
