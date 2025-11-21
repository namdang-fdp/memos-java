import z from 'zod';

import type { UiNode } from '@ory/client';
import { permission } from 'process';

export type NodeType = UiNode['attributes']['node_type'];

export const oidcProvider = [
    'LOCAL',
    'LOCAL_OIDC',
    'GOOGLE',
    'FACEBOOK',
    'GITHUB',
] as const;

export type OidcProvider = (typeof oidcProvider)[number];

export type AttrByType<TType extends NodeType> = Extract<
    UiNode['attributes'],
    { node_type: TType }
>;

export type OidcData = {
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
    authenticated: boolean;
    token: string;
    role: string;
};

export interface RegisterResponse {
    accessToken: string;
    role: string;
    permission: string[];
    provider: OidcProvider;
}

// -------------------- Schema ------------------------
export const loginSchema = z.object({
    email: z
        .string()
        .min(1, 'This field cannot be null')
        .email('Invalid email'),
    password: z.string().min(6, 'Password must be at least 6 characters'),
});

export type LoginFormValues = z.infer<typeof loginSchema>;

export const sendCodeSchema = z.object({
    email: z.string().email('Invalid email address'),
});

export type SendCodeForm = z.infer<typeof sendCodeSchema>;

export const verifyOtpSchema = z.object({
    code: z
        .string()
        .min(6, 'Code must be 6 digits')
        .max(6, 'Code must be 6 digits')
        .regex(/^\d+$/, 'Code must contain only numbers'),
});

export type VerifyOtpForm = z.infer<typeof verifyOtpSchema>;
