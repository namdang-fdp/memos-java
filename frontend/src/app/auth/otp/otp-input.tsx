'use client';

import { useState, useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { Button } from '@/components/ui/button';
import { Card } from '@/components/ui/card';
import { Loader2, Shield, Check, RotateCcw, ArrowRight } from 'lucide-react';
import {
    Form,
    FormControl,
    FormField,
    FormItem,
    FormMessage,
} from '@/components/ui/form';

const verifyOtpSchema = z.object({
    code: z
        .string()
        .min(6, 'Code must be 6 digits')
        .max(6, 'Code must be 6 digits')
        .regex(/^\d+$/, 'Code must contain only numbers'),
});

type VerifyOtpForm = z.infer<typeof verifyOtpSchema>;

interface VerifyOtpProps {
    email?: string;
    onVerify: (code: string) => Promise<void>;
    onResend?: () => Promise<void>;
    onBack?: () => void;
}

export function VerifyOtp({
    email = 'user@example.com',
    onVerify,
    onResend,
    onBack,
}: VerifyOtpProps) {
    const [isLoading, setIsLoading] = useState(false);
    const [isResending, setIsResending] = useState(false);
    const [isVerified, setIsVerified] = useState(false);
    const [resendCooldown, setResendCooldown] = useState(0);

    const form = useForm<VerifyOtpForm>({
        resolver: zodResolver(verifyOtpSchema),
        mode: 'onChange',
        defaultValues: {
            code: '',
        },
    });

    const handleSubmit = async (data: VerifyOtpForm) => {
        setIsLoading(true);
        try {
            await onVerify(data.code);
            setIsVerified(true);
        } finally {
            setIsLoading(false);
        }
    };

    const handleResend = async () => {
        if (isResending || resendCooldown > 0) return;

        setIsResending(true);
        setResendCooldown(60);

        try {
            await onResend?.();
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
        <Card className="mx-auto w-full max-w-5xl overflow-hidden border-0 shadow-2xl">
            <div className="grid min-h-[700px] grid-cols-1 lg:grid-cols-2">
                {/* Left sidebar - enhanced spacing */}
                <div className="from-primary/10 via-primary/5 to-secondary/10 hidden items-center justify-center bg-gradient-to-br p-12 lg:flex">
                    <div className="space-y-8 text-center">
                        <div className="space-y-6">
                            <div
                                className={`inline-flex items-center justify-center rounded-3xl p-6 transition-all duration-500 ${
                                    isVerified
                                        ? 'scale-110 bg-gradient-to-br from-green-500/30 to-green-500/10'
                                        : 'from-primary/30 to-primary/10 bg-gradient-to-br'
                                }`}
                            >
                                {isVerified ? (
                                    <Check className="h-20 w-20 animate-pulse text-green-500" />
                                ) : (
                                    <Shield className="text-primary h-20 w-20" />
                                )}
                            </div>
                            <div className="space-y-3">
                                <h2 className="text-3xl font-bold tracking-tight">
                                    {isVerified ? 'Verified!' : 'Verify Code'}
                                </h2>
                                <p className="text-muted-foreground max-w-xs text-base leading-relaxed">
                                    {isVerified
                                        ? 'Your identity has been successfully verified'
                                        : 'Enter the 6-digit code sent to your email'}
                                </p>
                            </div>
                        </div>
                    </div>
                </div>

                {/* Right content area - enhanced spacing */}
                <div className="flex flex-col items-center justify-center p-8 lg:p-16">
                    <div className="w-full space-y-10">
                        {/* Header - increased sizing */}
                        <div className="space-y-4 text-center lg:text-left">
                            <div className="mb-6 flex justify-center lg:hidden">
                                <div
                                    className={`rounded-3xl p-4 transition-all duration-300 ${
                                        isVerified
                                            ? 'bg-gradient-to-br from-green-500/20 to-green-500/5'
                                            : 'from-primary/20 to-primary/5 bg-gradient-to-br'
                                    }`}
                                >
                                    {isVerified ? (
                                        <Check className="h-10 w-10 text-green-500" />
                                    ) : (
                                        <Shield className="text-primary h-10 w-10" />
                                    )}
                                </div>
                            </div>
                            <h1 className="text-4xl font-bold tracking-tight lg:text-5xl">
                                {isVerified ? 'Success!' : 'Enter Code'}
                            </h1>
                            <p className="text-muted-foreground max-w-lg text-base leading-relaxed">
                                {isVerified
                                    ? 'Your account is now fully secured with two-factor authentication'
                                    : 'Check your email and enter the 6-digit verification code below'}
                            </p>
                        </div>

                        {/* Form - increased spacing */}
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
                                            field: {
                                                value,
                                                onChange,
                                                ...field
                                            },
                                        }) => (
                                            <FormItem>
                                                <label className="text-foreground mb-6 block text-base font-semibold">
                                                    Verification Code{' '}
                                                    <span className="text-destructive">
                                                        *
                                                    </span>
                                                </label>
                                                <FormControl>
                                                    <div className="space-y-4">
                                                        <div className="flex justify-center gap-3 lg:justify-start">
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
                                                                            value[
                                                                                i
                                                                            ] ||
                                                                            ''
                                                                        }
                                                                        onChange={(
                                                                            e,
                                                                        ) => {
                                                                            const newCode =
                                                                                value.split(
                                                                                    '',
                                                                                );
                                                                            newCode[
                                                                                i
                                                                            ] =
                                                                                e.target.value.replace(
                                                                                    /[^0-9]/g,
                                                                                    '',
                                                                                );
                                                                            onChange(
                                                                                newCode.join(
                                                                                    '',
                                                                                ),
                                                                            );

                                                                            // Auto-focus next input
                                                                            if (
                                                                                e
                                                                                    .target
                                                                                    .value &&
                                                                                i <
                                                                                    5
                                                                            ) {
                                                                                const nextInput =
                                                                                    document.querySelector(
                                                                                        `input[data-otp-index="${i + 1}"]`,
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
                                                                                !value[
                                                                                    i
                                                                                ] &&
                                                                                i >
                                                                                    0
                                                                            ) {
                                                                                const prevInput =
                                                                                    document.querySelector(
                                                                                        `input[data-otp-index="${i - 1}"]`,
                                                                                    ) as HTMLInputElement;
                                                                                prevInput?.focus();
                                                                            }
                                                                        }}
                                                                        data-otp-index={
                                                                            i
                                                                        }
                                                                        className="bg-secondary/50 border-border/50 text-foreground placeholder-muted-foreground/30 focus:border-primary hover:border-border h-14 w-14 rounded-lg border-2 text-center text-2xl font-bold transition-all duration-200 focus:outline-none lg:h-16 lg:w-16"
                                                                    />
                                                                ),
                                                            )}
                                                        </div>
                                                        {form.formState.errors
                                                            .code && (
                                                            <p className="text-destructive flex items-center gap-2 text-sm font-medium">
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
                                        )}
                                    />

                                    {/* Buttons - increased spacing and sizing */}
                                    <div className="space-y-4 pt-4">
                                        <Button
                                            type="submit"
                                            disabled={
                                                isLoading ||
                                                !form.formState.isValid
                                            }
                                            className="from-primary to-primary/90 hover:from-primary/90 hover:to-primary/80 text-primary-foreground group relative h-16 w-full overflow-hidden rounded-lg bg-gradient-to-r text-base font-semibold transition-all duration-300 disabled:cursor-not-allowed disabled:opacity-50"
                                        >
                                            <div className="absolute inset-0 -skew-x-12 transform bg-gradient-to-r from-transparent via-white/10 to-transparent transition-transform duration-700 group-hover:translate-x-full" />
                                            <div className="relative flex items-center justify-center gap-3">
                                                {isLoading ? (
                                                    <>
                                                        <Loader2 className="h-5 w-5 animate-spin" />
                                                        <span>
                                                            Verifying...
                                                        </span>
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
                                                isResending ||
                                                resendCooldown > 0
                                            }
                                            className="border-border hover:bg-secondary/50 h-14 w-full rounded-lg border-2 text-base font-semibold transition-all duration-300"
                                        >
                                            {isResending ? (
                                                <>
                                                    <Loader2 className="mr-2 h-5 w-5 animate-spin" />
                                                    Sending...
                                                </>
                                            ) : resendCooldown > 0 ? (
                                                <>
                                                    <RotateCcw className="mr-2 h-5 w-5 opacity-60" />
                                                    <span>
                                                        Resend in{' '}
                                                        {resendCooldown}s
                                                    </span>
                                                </>
                                            ) : (
                                                <>
                                                    <RotateCcw className="mr-2 h-5 w-5" />
                                                    Resend code
                                                </>
                                            )}
                                        </Button>
                                    </div>
                                </form>
                            </Form>
                        )}

                        {/* Success state - added after verification */}
                        {isVerified && (
                            <div className="space-y-8 text-center lg:text-left">
                                <div className="rounded-xl border border-green-500/30 bg-green-500/10 p-8">
                                    <div className="mb-4 flex items-center justify-center gap-4 lg:justify-start">
                                        <div className="flex h-12 w-12 items-center justify-center rounded-full bg-green-500/20">
                                            <Check className="h-6 w-6 text-green-500" />
                                        </div>
                                        <div>
                                            <p className="text-muted-foreground text-sm">
                                                Your code
                                            </p>
                                            <p className="text-foreground font-mono text-xl font-bold">
                                                {form.getValues('code')}
                                            </p>
                                        </div>
                                    </div>
                                    <p className="text-sm font-medium text-green-500/90">
                                        Successfully verified!
                                    </p>
                                </div>

                                <Button
                                    onClick={onBack}
                                    className="h-14 w-full rounded-lg bg-green-500 text-base font-semibold text-white transition-all hover:bg-green-600"
                                >
                                    <Check className="mr-2 h-5 w-5" />
                                    Complete
                                </Button>
                            </div>
                        )}

                        {/* Email display - increased sizing */}
                        <div className="bg-secondary/30 border-border/50 rounded-xl border p-6">
                            <p className="text-muted-foreground mb-2 text-sm font-medium">
                                Verification email:
                            </p>
                            <p className="text-foreground truncate text-lg font-semibold">
                                {email}
                            </p>
                        </div>

                        {/* Footer - increased spacing */}
                        <div className="border-border/30 border-t pt-6 text-center">
                            <p className="text-muted-foreground text-base">
                                Something is not working?{' '}
                                <button
                                    onClick={onBack}
                                    className="text-primary hover:text-primary/80 font-semibold transition-colors"
                                >
                                    {isVerified ? 'Close' : 'Back'}
                                </button>
                            </p>
                        </div>

                        {/* Security badge */}
                        <div className="text-muted-foreground flex items-center justify-center gap-2 text-sm">
                            <Shield className="h-4 w-4" />
                            <span>Protected by ORY</span>
                        </div>
                    </div>
                </div>
            </div>
        </Card>
    );
}
