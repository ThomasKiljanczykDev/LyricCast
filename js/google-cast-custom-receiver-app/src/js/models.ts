/*
 * Created by Thomas Kiljanczyk on 06/12/2024, 17:11
 * Copyright (c) 2024 . All rights reserved.
 * Last modified 06/12/2024, 17:11
 */
import { z } from 'zod';

export enum LyricCastAction {
    BLANK = 'BLANK',
    CONFIGURE = 'CONFIGURE'
}

export const LyricCastControlMessage = z.object({
    action: z.string().toUpperCase().pipe(z.nativeEnum(LyricCastAction)),
    value: z.any()
});

export const LyricCastContentMessage = z.object({
    text: z.string()
});

export const LyricCastConfig = z.object({
    backgroundColor: z.string(),
    fontColor: z.string(),
    maxFontSize: z.number()
});
