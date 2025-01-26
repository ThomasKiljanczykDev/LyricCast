import 'dotenv/config';
import { z } from 'zod';

import { createEnv } from '@t3-oss/env-core';

export enum DeploymentEnvironment {
    PRODUCTION = 'production',
    DEVELOPMENT = 'development'
}

export const env = createEnv({
    /*
     * Serverside Environment variables, not available on the client.
     * Will throw if you access these variables on the client.
     */
    server: {
        AWS_REGION: z.string().min(1),
        AWS_ACCOUNT_ID: z.string().min(1),
        DEPLOYMENT_ENVIRONMENT: z.nativeEnum(DeploymentEnvironment),
        LYRICCAST_PRIVACY_POLICY_CERTIFICATE_ARN: z.string().optional()
    },

    runtimeEnv: {
        AWS_REGION: process.env.AWS_REGION,
        AWS_ACCOUNT_ID: process.env.AWS_ACCOUNT_ID,
        DEPLOYMENT_ENVIRONMENT: process.env.DEPLOYMENT_ENVIRONMENT,
        LYRICCAST_PRIVACY_POLICY_CERTIFICATE_ARN:
            process.env.LYRICCAST_PRIVACY_POLICY_CERTIFICATE_ARN
    }
});
