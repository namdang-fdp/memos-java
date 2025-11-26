'use client';

import { Toaster } from '@/components/ui/sonner';
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
            <Toaster />
        </>
    );
}
