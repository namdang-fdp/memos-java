'use client';

import { useState } from 'react';
import { SendCode } from './send-code';
import { VerifyOtp } from './otp-input';

type Step = 'send' | 'verify';

export default function Home() {
    const [step, setStep] = useState<Step>('send');
    const [email] = useState('temnguyvanamdang@gmail.com');

    const handleSendCode = async (sendEmail: string) => {
        // Simulate API call
        await new Promise((resolve) => setTimeout(resolve, 1500));
    };

    const handleVerifyOtp = async (code: string) => {
        // Simulate API call
        await new Promise((resolve) => setTimeout(resolve, 1500));
    };

    const handleResendCode = async () => {
        // Simulate API call
        await new Promise((resolve) => setTimeout(resolve, 1000));
    };

    const handleBack = () => {
        setStep('send');
    };

    return (
        <main className="from-background via-secondary/20 to-background flex min-h-screen items-center justify-center bg-gradient-to-br p-4">
            <div className="w-full">
                {step === 'send' && (
                    <SendCode
                        email={email}
                        onSendCode={handleSendCode}
                        onNext={() => setStep('verify')}
                    />
                )}

                {step === 'verify' && (
                    <VerifyOtp
                        email={email}
                        onVerify={handleVerifyOtp}
                        onResend={handleResendCode}
                        onBack={handleBack}
                    />
                )}
            </div>
        </main>
    );
}
