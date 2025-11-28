'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { LoadingSpinner } from '@/components/loading-spinner';
import { useOidcRegister } from '@/lib/service/auth';
import { useAuthStore } from '@/lib/stores/authStore';

let oidcRegisterInProgress = false;

export default function OidcCallbackPage() {
    const router = useRouter();
    const registerOidc = useOidcRegister();
    const { accessToken } = useAuthStore();

    useEffect(() => {
        if (accessToken) {
            console.log('I am here useEffect');
            router.push('/auth/profile/setup');
            return;
        }

        if (oidcRegisterInProgress) {
            return;
        }

        oidcRegisterInProgress = true;

        registerOidc.mutate(undefined, {
            onSuccess: () => {
                oidcRegisterInProgress = false;
            },
            onError: (err) => {
                console.error('OIDC register failed', err);
                oidcRegisterInProgress = false;
                router.replace('/auth/login');
            },
        });
    }, [accessToken, registerOidc, router]);

    return (
        <div className="flex min-h-screen items-center justify-center">
            <LoadingSpinner size="lg" showText={false} />
        </div>
    );
}
