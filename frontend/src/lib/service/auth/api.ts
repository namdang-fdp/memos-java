'use client';
import { useMutation } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import {
    AttrByType,
    FacebookData,
    LoginFormValues,
    LoginPayload,
    LoginResponse,
    loginSchema,
    NodeType,
    SendCodeForm,
    sendCodeSchema,
} from './type';
import { zodResolver } from '@hookform/resolvers/zod';
import { useRouter, useSearchParams } from 'next/navigation';
import { LoginFlow, UiNode } from '@ory/client';
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

// hook to get the second flow (MFA) and find the email node, send to page
// for the send otp to email step
export const useOrySecondFactorFlow = () => {
    const searchParams = useSearchParams();
    const flowId = searchParams.get('flow');

    const [flow, setFlow] = useState<LoginFlow | null>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        if (!flowId) return;
        oryFetcher
            .getLoginFlow({ id: flowId })
            .then(({ data }) => setFlow(data))
            .finally(() => setLoading(false));
    }, [flowId, setFlow, setLoading]);

    const email = useMemo(() => {
        if (!flow) return '';

        const emailNode = findNode(flow, 'code', 'address', 'input');
        if (emailNode && typeof emailNode.value === 'string') {
            return emailNode.value;
        }

        return '';
    }, [flow]);

    return { flow, email, loading };
};
// after successfully get a flow
// a flow contain it attributes and the field "ui" that is the form that we need to submit
// in the ui field, there are an array of nodes those are the fields of the form
// as the facebook login, we need to find the "input" node of 'oidc' group

// this is the function is checked that whether the node is input
// if the node is input, it will have the field name (provider facebook) for the next step
function isInputNode(
    node: UiNode,
): node is UiNode & { attributes: AttrByType<'input'> } {
    return node.attributes.node_type === 'input';
}

// function to detech there are any aal2 (otp step)
export function isSecondFactorFlow(flow: LoginFlow): boolean {
    if (flow.requested_aal === 'aal2') return true;
    const nodes = flow.ui.nodes as UiNode[];
    return nodes.some((n) => n.group === 'code');
}

// find the node in Flow with group,name,type
export function findNode<TType extends NodeType>(
    flow: LoginFlow,
    group: string,
    name: string,
    type: TType,
): AttrByType<TType> | null {
    const nodes = flow.ui.nodes as UiNode[];
    for (const n of nodes) {
        if (n.group !== group) continue;
        if (n.attributes.node_type !== type) continue;

        if (type === 'input' && isInputNode(n)) {
            if (n.attributes.name === name) {
                return n.attributes as AttrByType<TType>;
            }
        }
        if (type !== 'input') {
            return n.attributes as AttrByType<TType>;
        }
    }
    return null;
}

// what will happen here. After redirect to facebook and user accept to loginWithFacebook
// ory see that the user need the MFA. they will continue create one more login flow
// that flow have the state = 'request-aal' and redirect to there own send MFA UI
export const useFacebookLogin = (flow: LoginFlow | null) => {
    // get the facebook oidc node, action, method
    const facebookData = useMemo<FacebookData | null>(() => {
        if (!flow || !flow.ui?.action) return null;

        const providerAttr = findNode(flow, 'oidc', 'provider', 'input');
        const csrfAttr = findNode(flow, 'default', 'csrf_token', 'input');

        if (
            !providerAttr ||
            !csrfAttr ||
            typeof providerAttr.value !== 'string' ||
            typeof csrfAttr.value !== 'string'
        ) {
            console.warn(
                'Missing OIDC provider or CSRF token node',
                flow.ui?.nodes,
            );
            return null;
        }

        return {
            action: flow.ui.action,
            method: (flow.ui.method || 'POST').toUpperCase(),
            providerValue: providerAttr.value,
            csrfToken: csrfAttr.value,
        };
    }, [flow]);
    // create the hidden form that submit follow the json that Ory given back
    const loginWithFacebook = () => {
        if (!facebookData) {
            console.warn('Facebook login data not ready yet');
            return;
        }

        const { action, method, providerValue, csrfToken } = facebookData;

        const form = document.createElement('form');
        form.method = method;
        form.action = action;

        const csrfInput = document.createElement('input');
        csrfInput.type = 'hidden';
        csrfInput.name = 'csrf_token';
        csrfInput.value = csrfToken;
        form.appendChild(csrfInput);

        const providerInput = document.createElement('input');
        providerInput.type = 'hidden';
        providerInput.name = 'provider';
        providerInput.value = providerValue;
        form.appendChild(providerInput);

        document.body.appendChild(form);
        form.submit();
    };

    return {
        canFacebookLogin: !!facebookData,
        loginWithFacebook,
    };
};

// after login with facebook successfully, one more time get the login flow data
// that data will contain the step aal2 (send otp)
// catch that state and redirect to page /auth/otp
export const useSecondFactorRedirect = (flow: LoginFlow | null) => {
    const router = useRouter();
    useEffect(() => {
        if (!flow) return;
        if (!isSecondFactorFlow(flow)) return;
        const flowId = new URLSearchParams({ flow: flow.id }).toString();
        router.replace(`/auth/otp?${flowId}`);
    }, [flow, router]);
};

export const useSendOtpCode = () => {
    const form = useForm<SendCodeForm>({
        resolver: zodResolver(sendCodeSchema),
    });

    return { form };
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
