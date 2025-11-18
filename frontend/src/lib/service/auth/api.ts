'use client';
import { useMutation } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import {
    LoginFormValues,
    LoginPayload,
    LoginResponse,
    loginSchema,
} from './type';
import { zodResolver } from '@hookform/resolvers/zod';
import { useRouter, useSearchParams } from 'next/navigation';
import {
    LoginFlow,
    UiNode,
    UiNodeAttributes,
    UiNodeInputAttributes,
} from '@ory/client';
import { useEffect, useMemo, useState } from 'react';
import { oryFetcher } from '@/lib/api/ory';

// ory flow hook logic
// first user access to /auth/login. If the params already have ?flow="" (ory unique flow id)
// --> call getLoginFlow to get flow data and OIDC
// if not yet: call --> createBrowserLoginFlow to create new flow --> router replace
export const useOryLoginFlow = () => {
    const searchParams = useSearchParams();
    const router = useRouter();

    const [flow, setFlow] = useState<LoginFlow | null>(null);
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        const init = async () => {
            try {
                const flowId = searchParams.get('flow');
                if (flowId) {
                    const { data } = await oryFetcher.getLoginFlow({
                        id: flowId,
                    });
                    setFlow(data);
                    setIsLoading(false);
                    return;
                }
                oryFetcher
                    .createBrowserLoginFlow()
                    .then(({ data }) => {
                        router.replace(`/auth/login?flow=${data.id}`);
                    })
                    .catch(console.error);
            } catch (error) {
                console.error('Init login flow error', error);
                setIsLoading(false);
            }
        };
        init();
    }, [router, searchParams]);
    return { flow, isLoading };
};

// login facebook ory flow
// after successfully get the flow data, find the node facebook's OIDC node
// get the href and redirect

function isInputAttributes(
    attrs: UiNodeAttributes,
): attrs is UiNodeInputAttributes {
    return (attrs as UiNodeInputAttributes).node_type === 'input';
}

function findInputNode(
    flow: LoginFlow,
    group: string,
    name: string,
): UiNodeInputAttributes | null {
    const node = (flow.ui.nodes as UiNode[]).find(
        (n) =>
            n.group === group &&
            isInputAttributes(n.attributes) &&
            n.attributes.name === name,
    );

    return node ? (node.attributes as UiNodeInputAttributes) : null;
}
export const useFacebookLogin = (flow: LoginFlow | null) => {
    const facebookData = useMemo(() => {
        if (!flow) return null;

        // 1) node OIDC: provider
        const providerAttr = findInputNode(flow, 'oidc', 'provider');
        // 2) node default: csrf_token
        const csrfAttr = findInputNode(flow, 'default', 'csrf_token');

        if (
            !providerAttr ||
            !csrfAttr ||
            typeof providerAttr.value !== 'string' ||
            typeof csrfAttr.value !== 'string'
        ) {
            console.warn(
                'Missing OIDC provider or CSRF token node',
                flow.ui.nodes,
            );
            return null;
        }

        return {
            action: flow.ui.action, // URL submit form
            method: (flow.ui.method || 'POST').toUpperCase(),
            providerValue: providerAttr.value, // "facebook-...."
            csrfToken: csrfAttr.value,
        };
    }, [flow]);

    const loginWithFacebook = () => {
        if (!facebookData) {
            console.warn('Facebook login data not ready yet');
            return;
        }

        const { action, method, providerValue, csrfToken } = facebookData;

        // Tạo form ẩn và submit như UI Ory
        const form = document.createElement('form');
        form.method = method;
        form.action = action;

        // csrf_token
        const csrfInput = document.createElement('input');
        csrfInput.type = 'hidden';
        csrfInput.name = 'csrf_token';
        csrfInput.value = csrfToken;
        form.appendChild(csrfInput);

        // provider (facebook-...)
        const providerInput = document.createElement('input');
        providerInput.type = 'hidden';
        providerInput.name = 'provider';
        providerInput.value = providerValue;
        form.appendChild(providerInput);

        document.body.appendChild(form);
        form.submit(); // Browser POST -> Ory -> Facebook -> quay lại app
    };

    return {
        canFacebookLogin: !!facebookData,
        loginWithFacebook,
    };
};

// login with username and password
export const useLogin = () => {
    const mutation = useMutation({
        mutationFn: async (values: LoginPayload) => {
            await new Promise((r) => setTimeout(r, 1000));
            return {
                token: 'fake-token-123',
                user: {
                    id: '1',
                    email: values.email,
                    name: 'Ken Demo',
                },
            } as LoginResponse;
        },
        onSuccess: (data) => {
            console.log('Logged in: ', data);
        },
    });
    const form = useForm<LoginFormValues>({
        resolver: zodResolver(loginSchema),
        defaultValues: {
            email: '',
            password: '',
        },
    });

    return {
        form,
        mutation,
    };
};
