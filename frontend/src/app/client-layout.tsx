'use client';

import { Toaster } from 'sonner';
import { useAutoRefresh } from '@/lib/service/auth';

export default function ClientLayout({
    children,
}: {
    children: React.ReactNode;
}) {
    useAutoRefresh();
    return (
        <>
            {children}
            <Toaster richColors />
        </>
    );
}
