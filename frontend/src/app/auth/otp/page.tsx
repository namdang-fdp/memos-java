'use client';

import { useState } from 'react';
import { SendCode } from './send-code';
import { VerifyOtp } from './otp-input';
import {
    useOrySecondFactorFlow,
    useSendOtpCode,
    useVerifyOtpCode,
} from '@/lib/service/auth';
import { LoadingSpinner } from '@/components/loading-spinner';
import { useRouter } from 'next/navigation';
import { LoginFlow } from '@ory/client';

type Step = 'send' | 'verify';

export default function Home() {
    const router = useRouter();
    const { flow, email, loading } = useOrySecondFactorFlow();
    const [step, setStep] = useState<Step>('send');

    const { sendCode } = useSendOtpCode(flow as LoginFlow, email);

    const { verifyCode } = useVerifyOtpCode(flow as LoginFlow);

    if (loading || !flow) {
        return (
            <div className="flex min-h-screen items-center justify-center">
                <LoadingSpinner size="lg" showText={false} />
            </div>
        );
    }

    const handleVerifyOtp = async (code: string) => {
        await verifyCode(code);
        router.replace('/');
    };

    const handleResendCode = async () => {
        await sendCode();
    };

    const handleBack = () => {
        setStep('send');
    };

    return (
        <main className="from-background via-secondary/20 to-background flex min-h-screen items-center justify-center bg-linear-to-br p-4">
            <div className="w-full">
                {step === 'send' && (
                    <SendCode
                        email={email}
                        flow={flow}
                        onNext={() => setStep('verify')}
                    />
                )}

                {step === 'verify' && (
                    <VerifyOtp
                        email={email}
                        onVerify={handleVerifyOtp}
                        onResend={handleResendCode}
                        onBack={handleBack}
                        flow={flow}
                    />
                )}
            </div>
        </main>
    );
}
