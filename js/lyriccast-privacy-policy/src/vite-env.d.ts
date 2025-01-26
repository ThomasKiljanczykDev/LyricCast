/*
 * Created by Thomas Kiljanczyk on 15/01/2025, 17:30
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 15/01/2025, 17:30
 */

/* eslint-disable spaced-comment */

/// <reference types="vite/client" />

interface ImportMetaEnv {
    VITE_BASE_PUBLIC_PATH?: string;
}

interface ImportMeta {
    readonly env: ImportMetaEnv;
}
