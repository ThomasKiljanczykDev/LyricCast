import tseslint from 'typescript-eslint';

import {tsEslintConfig} from '@lyriccast-receiver/eslint-config';

export default tseslint.config(...tsEslintConfig, {
    ignores: [
        '**/.yarn/',
        '**/dist*/',
        '**/build/',
        '**/.vscode/',
        '**/.cache/',
        '**/cdk.out/',
        '**/prettier.config.js',
        '**/eslint.config.js'
    ]
});
