// @ts-check
import eslintConfigPrettier from 'eslint-config-prettier';
import tailwind from 'eslint-plugin-tailwindcss';
import pluginVue from 'eslint-plugin-vue';
import tseslint from 'typescript-eslint';

import eslint from '@eslint/js';

import errors from './src/errors.js';
import style from './src/style.js';
import typescript from './src/typescript.js';





const baseEslintConfig = tseslint.config(...errors, ...style);

const tsEslintConfig = tseslint.config(
    eslint.configs.recommended,
    ...typescript,
    ...baseEslintConfig,
    ...tailwind.configs['flat/recommended'],
    {
        files: ['**/*.vue'],
        extends: [...pluginVue.configs['flat/recommended']]
    },
    eslintConfigPrettier,
    {
        rules: {
            'vue/multi-word-component-names': 'off'
        }
    }
);

export { tsEslintConfig };
