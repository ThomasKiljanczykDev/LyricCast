import autoprefixer from 'autoprefixer';
import path from 'node:path';
import postcssNesting from 'postcss-nesting';
import tailwindcss from 'tailwindcss';
import { defineConfig } from 'vite';
import tsConfigPaths from 'vite-tsconfig-paths';

import vue from '@vitejs/plugin-vue';

export default defineConfig({
    base: process.env.VITE_BASE_PUBLIC_PATH,
    plugins: [vue(), tsConfigPaths()],
    resolve: {
        alias: {
            '@': path.join(__dirname, 'src')
        }
    },
    css: {
        postcss: {
            plugins: [postcssNesting(), autoprefixer(), tailwindcss()]
        }
    }
});
