'use client';
import { useMutation } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import {
    AttrByType,
    OidcData,
    LoginFormValues,
    LoginPayload,
    LoginResponse,
    loginSchema,
    NodeType,
    RegisterResponse,
    SendCodeForm,
    sendCodeSchema,
    VerifyOtpForm,
    verifyOtpSchema,
    RegisterFormValues,
    registerSchema,
    ProfileFormValues,
    profileSchema,
} from './type';
import { zodResolver } from '@hookform/resolvers/zod';
import { useRouter, useSearchParams } from 'next/navigation';
import { LoginFlow, UiNode } from '@ory/client';
import { useEffect, useMemo, useState } from 'react';
import { oryFetcher } from '@/lib/api/ory';
import { AxiosError } from 'axios';
import {
    axiosWrapper,
    ApiResponse,
    throwIfError,
    deserialize,
} from '@/lib/api/axios-config';
import { toast } from 'sonner';
import { useAuthStore } from '@/lib/stores/authStore';

// ory flow hook logic
// first user access to /auth/login. If the params already have ?flow="" (ory unique flow id)
// --> call getLoginFlow to get flow data and OIDC
// if not yet: call --> createBrowserLoginFlow to create new flow --> router replace
export const useOryLoginFlow = (page: string) => {
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
                        router.replace(`/auth/${page}?flow=${data.id}`);
                    })
                    .catch(console.error);
            } catch (error) {
                console.error('Init login flow error', error);
                setIsLoading(false);
            }
        };
        init();
    }, [router, searchParams, page]);
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

        const emailNode = findNode(flow, 'code', 'identifier', 'input');
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

// find node with specific attribute (Facebook, github, google)
export function findOidcProviderNode(
    flow: LoginFlow,
    providerKey: string,
): AttrByType<'input'> | null {
    const nodes = flow.ui.nodes as UiNode[];

    for (const n of nodes) {
        if (n.group !== 'oidc') continue;
        if (!isInputNode(n)) continue;
        if (n.attributes.name !== 'provider') continue;

        const value = n.attributes.value;

        if (typeof value === 'string' && value.startsWith(providerKey)) {
            return n.attributes as AttrByType<'input'>;
        }
    }
    return null;
}

// what will happen here. After redirect to facebook and user accept to loginWithFacebook
// ory see that the user need the MFA. they will continue create one more login flow
// that flow have the state = 'request-aal' and redirect to there own send MFA UI
export const useFacebookLogin = (flow: LoginFlow | null) => {
    // get the facebook oidc node, action, method
    const facebookData = useMemo<OidcData | null>(() => {
        if (!flow || !flow.ui?.action) return null;

        const providerAttr = findOidcProviderNode(flow, 'facebook');
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

// flow is the same with facebook login
// the different is that we must find the oidc node with contain value 'github'
export const useGithubLogin = (flow: LoginFlow | null) => {
    const githubData = useMemo<OidcData | null>(() => {
        if (!flow || !flow.ui?.action) return null;

        const providerAttr = findOidcProviderNode(flow, 'github');
        const csrfAttr = findNode(flow, 'default', 'csrf_token', 'input');

        if (
            !providerAttr ||
            !csrfAttr ||
            typeof providerAttr.value !== 'string' ||
            typeof csrfAttr.value !== 'string'
        ) {
            console.warn(
                'Missing GitHub OIDC provider or CSRF token node',
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

    const loginWithGithub = () => {
        if (!githubData) {
            console.warn('Github data not already yet');
            return;
        }
        const { action, method, providerValue, csrfToken } = githubData;
        const form = document.createElement('form');
        form.method = method;
        form.action = action;

        const crsfInput = document.createElement('input');
        crsfInput.type = 'hidden';
        crsfInput.name = 'csrf_token';
        crsfInput.value = csrfToken;
        form.appendChild(crsfInput);

        const providerInput = document.createElement('input');
        providerInput.type = 'hidden';
        providerInput.name = 'provider';
        providerInput.value = providerValue;
        form.appendChild(providerInput);

        document.body.appendChild(form);
        form.submit();
    };
    return {
        canGithubLogin: !!githubData,
        loginWithGithub,
    };
};

export const useGoogleLogin = (flow: LoginFlow | null) => {
    const googleData = useMemo<OidcData | null>(() => {
        if (!flow || !flow.ui?.action) return null;

        const providerAttr = findOidcProviderNode(flow, 'google');
        const csrfAttr = findNode(flow, 'default', 'csrf_token', 'input');

        if (
            !providerAttr ||
            !csrfAttr ||
            typeof providerAttr.value !== 'string' ||
            typeof csrfAttr.value !== 'string'
        ) {
            console.warn(
                'Missing Google OIDC provider or CSRF token node',
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

    const loginWithGoogle = () => {
        if (!googleData) {
            console.warn('Google login data not ready yet');
            return;
        }

        const { action, method, providerValue, csrfToken } = googleData;

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
        canGoogleLogin: !!googleData,
        loginWithGoogle,
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

// hook for send otp code through ory
// ory need something to send code:
// 1. csrf token
// 2. address
// 3. method
// all of them is exist in LoginFlow (isSecondFactorFlow) if successfully fetch that flow
export const useSendOtpCode = (flow: LoginFlow, email?: string) => {
    const mutation = useMutation({
        mutationFn: async () => {
            if (!flow) {
                console.log('Missing login flow for OTP');
            }

            // find csrf
            const csrfAttr = findNode(flow, 'default', 'csrf_token', 'input');
            if (!csrfAttr || typeof csrfAttr.value !== 'string') {
                console.log('Missing csrf_token');
            }

            // find address
            const addressAttr = findNode(flow, 'code', 'address', 'input');
            const addressValue =
                email ??
                (addressAttr && typeof addressAttr.value === 'string'
                    ? addressAttr.value
                    : undefined);

            if (!addressValue) {
                throw new Error('Missing address (email) for OTP');
            }

            // find method
            const methodAttr = findNode(flow, 'code', 'method', 'input');
            const methodValue =
                (methodAttr &&
                    typeof methodAttr.value === 'string' &&
                    methodAttr.value) ||
                'code';
            // some special logic here, ory still send code to gmail
            // but the form state or http code is 400 (bad request)
            // because in this case we just send otp not input otp
            // so that, we must catch the axios to be success if state is sent_email for next step
            try {
                const { data } = await oryFetcher.updateLoginFlow({
                    flow: flow.id,
                    updateLoginFlowBody: {
                        method: methodValue as 'code',
                        csrf_token: csrfAttr?.value,
                        address: addressValue,
                    },
                });
                return data;
            } catch (err) {
                const axiosErr = err as AxiosError<LoginFlow>;
                const status = axiosErr.response?.status;
                const flowData = axiosErr.response?.data;

                if (status === 400 && flowData?.state === 'sent_email') {
                    return flowData;
                }
                throw err;
            }
        },
    });

    const form = useForm<SendCodeForm>({
        resolver: zodResolver(sendCodeSchema),
        defaultValues: {
            email: email ?? '',
        },
    });

    useEffect(() => {
        if (email) {
            form.setValue('email', email, { shouldValidate: false });
        }
    }, [email, form]);

    return {
        form,
        sendCode: mutation.mutateAsync,
        isLoading: mutation.isPending,
    };
};

export const useVerifyOtpCode = (flow: LoginFlow) => {
    const mutation = useMutation({
        mutationFn: async (code: string) => {
            if (!flow) {
                console.log('Missing flow in verify OTP code');
            }
            // csrf_token
            const csrfAttr = findNode(flow, 'default', 'csrf_token', 'input');
            if (!csrfAttr || typeof csrfAttr.value !== 'string') {
                throw new Error('Missing csrf_token');
            }

            // method: code
            const methodAttr = findNode(flow, 'code', 'method', 'input');
            const methodValue =
                (methodAttr &&
                    typeof methodAttr.value === 'string' &&
                    methodAttr.value) ||
                'code';

            const { data } = await oryFetcher.updateLoginFlow({
                flow: flow.id,
                updateLoginFlowBody: {
                    method: methodValue as 'code',
                    csrf_token: csrfAttr.value,
                    code,
                },
            });

            return data;
        },
    });
    const form = useForm<VerifyOtpForm>({
        resolver: zodResolver(verifyOtpSchema),
        mode: 'onChange',
        defaultValues: {
            code: '',
        },
    });

    return {
        form,
        verifyCode: mutation.mutateAsync,
        isVerifying: mutation.isPending,
    };
};

// login with username and password
export const useLogin = () => {
    const router = useRouter();
    const setToken = useAuthStore((s) => s.setToken);
    const mutation = useMutation({
        mutationFn: async (values: LoginPayload) => {
            const response = await axiosWrapper.post<
                ApiResponse<LoginResponse>
            >('/auth/login', values);

            throwIfError(response.data, response.status);

            return {
                message: response.data.message,
                result: deserialize<LoginResponse>(response.data),
            };
        },
        onSuccess: ({ result }) => {
            setToken(result.token);
            toast.success('Login Successfully');
            router.push('/');
        },
        onError: (err) => {
            toast.error(err.message);
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

export const useOidcRegister = () => {
    const setToken = useAuthStore((s) => s.setToken);
    const mutation = useMutation({
        mutationFn: async () => {
            const response =
                await axiosWrapper.post<ApiResponse<RegisterResponse>>(
                    '/auth/oidc/ory',
                );

            throwIfError(response.data, response.status);

            return {
                message: response.data.message,
                result: deserialize<RegisterResponse>(response.data),
            };
        },
        onSuccess: ({ result }) => {
            setToken(result.accessToken);
            toast.success('Login Successfully');
        },
        onError: (err) => {
            toast.error(err.message);
        },
    });
    return mutation;
};

// refresh to get new access token
export const useRefreshToken = () => {
    const setToken = useAuthStore((s) => s.setToken);
    return useMutation({
        mutationFn: async () => {
            const response =
                await axiosWrapper.post<ApiResponse<LoginResponse>>(
                    '/auth/refresh',
                );
            throwIfError(response.data, response.status);
            return {
                message: response.data.message,
                result: deserialize<LoginResponse>(response.data),
            };
        },
        onSuccess: ({ result }) => {
            setToken(result.token);
        },
        onError: (err) => {
            toast.error(err.message);
        },
    });
};

// auto refresh within 10 mins
export const useAutoRefresh = () => {
    const { accessToken, expiresAt } = useAuthStore();
    const refresh = useRefreshToken();

    useEffect(() => {
        if (!accessToken || !expiresAt) return;

        const now = Date.now();
        const timeLeft = expiresAt - now;

        const refreshBefore = 10 * 60 * 1000;

        const triggerIn = Math.max(timeLeft - refreshBefore, 5000);

        const timer = setTimeout(() => {
            refresh.mutate();
        }, triggerIn);

        return () => clearTimeout(timer);
    }, [accessToken, expiresAt, refresh]);
};

// logout
export const useLogout = () => {
    const router = useRouter();
    const clear = useAuthStore((s) => s.clear);

    return useMutation({
        mutationFn: async () => {
            const response =
                await axiosWrapper.post<ApiResponse<void>>('/auth/logout');
            throwIfError(response.data, response.status);
            return {
                message: response.data.message,
            };
        },
        onSuccess: () => {
            clear();
            router.push('/login');
            toast.success('Logout Successfully');
        },
        onError: (err) => {
            toast.error(err.message);
        },
    });
};

export const useRegister = () => {
    const router = useRouter();
    const setToken = useAuthStore((s) => s.setToken);
    const mutation = useMutation({
        mutationFn: async (values: RegisterFormValues) => {
            const response = await axiosWrapper.post<
                ApiResponse<RegisterResponse>
            >('/auth/register', values);

            throwIfError(response.data, response.status);

            return {
                message: response.data.message,
                result: deserialize<RegisterResponse>(response.data),
            };
        },
        onSuccess: ({ result }) => {
            setToken(result.accessToken);
            toast.success('Register Successfully');
            router.push('/auth/profile/setup');
        },
        onError: (err) => {
            toast.error(err.message);
        },
    });

    const form = useForm<RegisterFormValues>({
        resolver: zodResolver(registerSchema),
        mode: 'onChange',
        defaultValues: {
            email: '',
            password: '',
            confirmPassword: '',
        },
    });

    return {
        form,
        mutation,
    };
};

export const useSetupProfile = () => {
    const router = useRouter();
    const mutation = useMutation({
        mutationFn: async (values: ProfileFormValues) => {
            const response = await axiosWrapper.post<ApiResponse>(
                '/auth/profile/setup',
                {
                    name: values.name,
                },
            );

            throwIfError(response.data, response.status);

            return response.data;
        },
        onSuccess: () => {
            toast.success('Profile updated successfully');
            router.push('/');
        },
        onError: (err) => {
            toast.error(err.message);
        },
    });
    const form = useForm<ProfileFormValues>({
        resolver: zodResolver(profileSchema),
        defaultValues: {
            name: '',
        },
    });
    return { form, mutation };
};
