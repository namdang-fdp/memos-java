import z from 'zod';

import type { UiNode } from '@ory/client';

export type NodeType = UiNode['attributes']['node_type'];

export type AttrByType<TType extends NodeType> = Extract<
    UiNode['attributes'],
    { node_type: TType }
>;

export type FacebookData = {
    action: string;
    method: string;
    providerValue: string;
    csrfToken: string;
};

export type LoginPayload = {
    email: string;
    password: string;
};

export type LoginResponse = {
    token: string;
    user: {
        id: string;
        email: string;
        name: string;
    };
};

// ------------- Schema ------------------------
export const loginSchema = z.object({
    email: z
        .string()
        .min(1, 'This field cannot be null')
        .email('Invalid email'),
    password: z.string().min(6, 'Password must be at least 6 characters'),
});

export type LoginFormValues = z.infer<typeof loginSchema>;
