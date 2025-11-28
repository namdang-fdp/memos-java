'use client';

import { useState } from 'react';
import { Loader2, Shield, Check, RotateCcw, ArrowRight } from 'lucide-react';

import { Button } from '@/components/ui/button';
import { Card } from '@/components/ui/card';
import { Form, FormControl, FormField, FormItem } from '@/components/ui/form';
import { useVerifyOtpCode } from '@/lib/service/auth';
import { LoginFlow } from '@ory/client';

interface VerifyOtpProps {
    email?: string;
    onVerify: (code: string) => Promise<void>;
    onResend?: () => Promise<void>;
    onBack?: () => void;
    flow: LoginFlow;
}

export function VerifyOtp({
    email,
    onVerify,
    onResend,
    onBack,
    flow,
}: VerifyOtpProps) {
    const [isLoading, setIsLoading] = useState(false);
    const [isResending, setIsResending] = useState(false);
    const [isVerified, setIsVerified] = useState(false);
    const [resendCooldown, setResendCooldown] = useState(0);

    const { form } = useVerifyOtpCode(flow);

    const handleSubmit = async (values: { code: string }) => {
        setIsLoading(true);
        try {
            await onVerify(values.code);
            setIsVerified(true);
        } finally {
            setIsLoading(false);
        }
    };

    const handleResend = async () => {
        if (!onResend) return;
        if (isResending || resendCooldown > 0) return;

        setIsResending(true);
        setResendCooldown(60);

        try {
            await onResend();
        } finally {
            setIsResending(false);

            const interval = setInterval(() => {
                setResendCooldown((prev) => {
                    if (prev <= 1) {
                        clearInterval(interval);
                        return 0;
                    }
                    return prev - 1;
                });
            }, 1000);
        }
    };

    return (
        <Card className="mx-auto w-full max-w-2xl overflow-hidden border-0 shadow-2xl">
            <div className="flex min-h-[520px] flex-col items-center justify-center p-8 lg:p-12">
                <div className="w-full space-y-10">
                    <div className="space-y-4 text-center">
                        <div className="mb-4 flex justify-center">
                            <div
                                className={`rounded-3xl p-4 transition-all duration-300 ${
                                    isVerified
                                        ? 'bg-linear-to-br from-green-500/20 to-green-500/5'
                                        : 'from-primary/20 to-primary/5 bg-linear-to-br'
                                }`}
                            >
                                {isVerified ? (
                                    <Check className="h-10 w-10 text-green-500" />
                                ) : (
                                    <Shield className="text-primary h-10 w-10" />
                                )}
                            </div>
                        </div>

                        <h1 className="text-3xl font-bold tracking-tight lg:text-4xl">
                            {isVerified
                                ? 'Success!'
                                : 'Enter Verification Code'}
                        </h1>
                        <p className="text-muted-foreground mx-auto max-w-lg text-base leading-relaxed">
                            {isVerified
                                ? 'Your account has been successfully verified.'
                                : 'Please check your email and enter the 6-digit code to proceed.'}
                        </p>
                    </div>

                    {!isVerified && (
                        <Form {...form}>
                            <form
                                onSubmit={form.handleSubmit(handleSubmit)}
                                className="space-y-8"
                            >
                                <FormField
                                    control={form.control}
                                    name="code"
                                    render={({
                                        field: { value, onChange, ...field },
                                    }) => {
                                        const safeValue = value ?? '';

                                        return (
                                            <FormItem>
                                                <label className="text-foreground mb-6 block text-base font-semibold">
                                                    Verification Code
                                                    <span className="text-destructive">
                                                        {' '}
                                                        *
                                                    </span>
                                                </label>
                                                <FormControl>
                                                    <div className="space-y-4">
                                                        <div className="flex justify-center gap-3">
                                                            {[...Array(6)].map(
                                                                (_, i) => (
                                                                    <input
                                                                        key={i}
                                                                        type="text"
                                                                        inputMode="numeric"
                                                                        maxLength={
                                                                            1
                                                                        }
                                                                        value={
                                                                            safeValue[
                                                                                i
                                                                            ] ||
                                                                            ''
                                                                        }
                                                                        onChange={(
                                                                            e,
                                                                        ) => {
                                                                            const numeric =
                                                                                e.target.value.replace(
                                                                                    /[^0-9]/g,
                                                                                    '',
                                                                                );
                                                                            const newCode =
                                                                                safeValue.split(
                                                                                    '',
                                                                                );
                                                                            newCode[
                                                                                i
                                                                            ] =
                                                                                numeric;
                                                                            onChange(
                                                                                newCode.join(
                                                                                    '',
                                                                                ),
                                                                            );

                                                                            if (
                                                                                numeric &&
                                                                                i <
                                                                                    5
                                                                            ) {
                                                                                const nextInput =
                                                                                    document.querySelector(
                                                                                        `input[data-otp-index="${
                                                                                            i +
                                                                                            1
                                                                                        }"]`,
                                                                                    ) as HTMLInputElement;
                                                                                nextInput?.focus();
                                                                            }
                                                                        }}
                                                                        onKeyDown={(
                                                                            e,
                                                                        ) => {
                                                                            // Handle backspace
                                                                            if (
                                                                                e.key ===
                                                                                    'Backspace' &&
                                                                                !safeValue[
                                                                                    i
                                                                                ] &&
                                                                                i >
                                                                                    0
                                                                            ) {
                                                                                const prevInput =
                                                                                    document.querySelector(
                                                                                        `input[data-otp-index="${
                                                                                            i -
                                                                                            1
                                                                                        }"]`,
                                                                                    ) as HTMLInputElement;
                                                                                prevInput?.focus();
                                                                            }
                                                                        }}
                                                                        data-otp-index={
                                                                            i
                                                                        }
                                                                        {...field}
                                                                        className="bg-secondary/50 border-border/50 text-foreground placeholder-muted-foreground/30 focus:border-primary hover:border-border h-14 w-14 rounded-lg border-2 text-center text-2xl font-bold transition-all duration-200 focus:outline-none lg:h-16 lg:w-16"
                                                                    />
                                                                ),
                                                            )}
                                                        </div>
                                                        {form.formState.errors
                                                            .code && (
                                                            <p className="text-destructive flex items-center justify-center gap-2 text-sm font-medium">
                                                                <span className="bg-destructive/20 flex h-4 w-4 items-center justify-center rounded-full text-xs">
                                                                    !
                                                                </span>
                                                                {
                                                                    form
                                                                        .formState
                                                                        .errors
                                                                        .code
                                                                        .message
                                                                }
                                                            </p>
                                                        )}
                                                    </div>
                                                </FormControl>
                                            </FormItem>
                                        );
                                    }}
                                />

                                <div className="space-y-4 pt-4">
                                    <Button
                                        type="submit"
                                        disabled={
                                            isLoading || !form.formState.isValid
                                        }
                                        className="from-primary to-primary/90 hover:from-primary/90 hover:to-primary/80 text-primary-foreground group relative h-12 w-full overflow-hidden rounded-lg bg-linear-to-r text-base font-semibold transition-all duration-300 disabled:cursor-not-allowed disabled:opacity-50"
                                    >
                                        <div className="absolute inset-0 -skew-x-12 transform bg-linear-to-r from-transparent via-white/10 to-transparent transition-transform duration-700 group-hover:translate-x-full" />
                                        <div className="relative flex items-center justify-center gap-3">
                                            {isLoading ? (
                                                <>
                                                    <Loader2 className="h-5 w-5 animate-spin" />
                                                    <span>Verifying...</span>
                                                </>
                                            ) : (
                                                <>
                                                    <span>
                                                        Verify & Continue
                                                    </span>
                                                    <ArrowRight className="h-5 w-5" />
                                                </>
                                            )}
                                        </div>
                                    </Button>

                                    <Button
                                        type="button"
                                        variant="outline"
                                        onClick={handleResend}
                                        disabled={
                                            isResending || resendCooldown > 0
                                        }
                                        className="border-border hover:bg-secondary/50 h-12 w-full rounded-lg border-2 text-base font-semibold transition-all duration-300"
                                    >
                                        {isResending ? (
                                            <>
                                                <Loader2 className="mr-2 h-5 w-5 animate-spin" />
                                                Resending...
                                            </>
                                        ) : resendCooldown > 0 ? (
                                            <>
                                                <RotateCcw className="mr-2 h-5 w-5 opacity-60" />
                                                <span>
                                                    Resend after{' '}
                                                    {resendCooldown}s
                                                </span>
                                            </>
                                        ) : (
                                            <>
                                                <RotateCcw className="mr-2 h-5 w-5" />
                                                Resend Code
                                            </>
                                        )}
                                    </Button>
                                </div>
                            </form>
                        </Form>
                    )}

                    {isVerified && (
                        <div className="space-y-8 text-center">
                            <div className="rounded-xl border border-green-500/30 bg-green-500/10 p-8">
                                <div className="mb-4 flex items-center justify-center gap-4">
                                    <div className="flex h-12 w-12 items-center justify-center rounded-full bg-green-500/20">
                                        <Check className="h-6 w-6 text-green-500" />
                                    </div>
                                    <div>
                                        <p className="text-muted-foreground text-sm">
                                            Your Code
                                        </p>
                                        <p className="text-foreground font-mono text-xl font-bold">
                                            {form.getValues('code')}
                                        </p>
                                    </div>
                                </div>
                                <p className="text-sm font-medium text-green-500/90">
                                    Verified successfully!
                                </p>
                            </div>

                            <Button
                                onClick={onBack}
                                className="h-12 w-full rounded-lg bg-green-500 text-base font-semibold text-white transition-all hover:bg-green-600"
                            >
                                <Check className="mr-2 h-5 w-5" />
                                Finish
                            </Button>
                        </div>
                    )}

                    <div className="bg-secondary/30 border-border/50 rounded-xl border p-4">
                        <p className="text-muted-foreground mb-1 text-xs font-medium">
                            Email for code:
                        </p>
                        <p className="text-foreground truncate text-sm font-semibold">
                            {email}
                        </p>
                    </div>

                    <div className="border-border/30 border-t pt-4 text-center">
                        <p className="text-muted-foreground text-sm">
                            Having trouble verifying?{' '}
                            <button
                                onClick={onBack}
                                className="text-primary hover:text-primary/80 font-semibold transition-colors"
                            >
                                {isVerified ? 'Close' : 'Go Back'}
                            </button>
                        </p>
                    </div>

                    <div className="text-muted-foreground flex items-center justify-center gap-2 text-xs">
                        <Shield className="h-4 w-4" />
                        <span>Protected by ORY</span>
                    </div>
                </div>
            </div>
        </Card>
    );
}
