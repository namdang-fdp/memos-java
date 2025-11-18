import { Configuration, FrontendApi } from '@ory/client';

export const oryFetcher = new FrontendApi(
    new Configuration({
        basePath: process.env.NEXT_PUBLIC_ORY_SDK_URL,
        baseOptions: {
            withCredentials: true,
        },
    }),
);
