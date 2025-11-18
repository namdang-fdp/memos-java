'use client';

import { useState } from 'react';
import { Button } from '@/components/ui/button';
import { Card } from '@/components/ui/card';
import { Loader2, Mail, Shield, ArrowRight } from 'lucide-react';
import { Form, FormControl, FormField, FormItem } from '@/components/ui/form';
import { useSendOtpCode } from '@/lib/service/auth';
import { LoginFlow } from '@ory/client';

interface SendCodeProps {
    email?: string;
    flow: LoginFlow;
    onNext?: () => void;
}

export function SendCode({ email, flow, onNext }: SendCodeProps) {
    const [isLoading, setIsLoading] = useState(false);
    const [isSent, setIsSent] = useState(false);

    const { form: sendCodeForm } = useSendOtpCode();
    const handleSubmit = () => {
        // setIsLoading(true);
        // try {
        //     await onSendCode(data.email);
        //     // Transition to verify step
        //     if (onNext) {
        //         onNext();
        //     }
        // } finally {
        //     setIsLoading(false);
        // }
    };

    return (
        <Card className="mx-auto w-full max-w-5xl overflow-hidden border-0 shadow-2xl">
            <div className="grid min-h-[700px] grid-cols-1 lg:grid-cols-2">
                <div className="from-primary/10 via-primary/5 to-secondary/10 hidden items-center justify-center bg-gradient-to-br p-12 lg:flex">
                    <div className="space-y-8 text-center">
                        <div className="space-y-6">
                            <div className="from-primary/30 to-primary/10 inline-flex items-center justify-center rounded-3xl bg-gradient-to-br p-6 backdrop-blur">
                                <Shield className="text-primary h-20 w-20" />
                            </div>
                            <div className="space-y-3">
                                <h2 className="text-3xl font-bold tracking-tight">
                                    Security First
                                </h2>
                                <p className="text-muted-foreground max-w-xs text-base leading-relaxed">
                                    We protect your account with
                                    enterprise-grade security
                                </p>
                            </div>
                        </div>
                        <div className="space-y-4 pt-6">
                            <div className="text-muted-foreground flex items-center gap-4 text-base">
                                <div className="bg-primary/20 text-primary flex h-10 w-10 flex-shrink-0 items-center justify-center rounded-full text-sm font-semibold">
                                    1
                                </div>
                                <span>Send code to your email</span>
                            </div>
                            <div className="text-muted-foreground flex items-center gap-4 text-base">
                                <div className="bg-primary/20 text-primary flex h-10 w-10 flex-shrink-0 items-center justify-center rounded-full text-sm font-semibold">
                                    2
                                </div>
                                <span>Enter the code here</span>
                            </div>
                            <div className="text-muted-foreground flex items-center gap-4 text-base">
                                <div className="bg-primary/20 text-primary flex h-10 w-10 flex-shrink-0 items-center justify-center rounded-full text-sm font-semibold">
                                    3
                                </div>
                                <span>Verify your identity</span>
                            </div>
                        </div>
                    </div>
                </div>

                <div className="flex flex-col items-center justify-center p-8 lg:p-16">
                    <div className="w-full space-y-10">
                        <div className="space-y-4 text-center lg:text-left">
                            <div className="mb-6 flex justify-center lg:hidden">
                                <div className="from-primary/20 to-primary/5 rounded-3xl bg-gradient-to-br p-4">
                                    <Shield className="text-primary h-10 w-10" />
                                </div>
                            </div>
                            <h1 className="text-4xl font-bold tracking-tight lg:text-5xl">
                                Second Factor
                            </h1>
                            <p className="text-muted-foreground max-w-lg text-base leading-relaxed">
                                {isSent
                                    ? 'Check your email for the verification code'
                                    : 'Complete the second authentication challenge to verify your identity'}
                            </p>
                        </div>

                        {isSent && (
                            <div className="bg-primary/5 border-primary/20 animate-in fade-in slide-in-from-bottom-2 flex items-center gap-4 rounded-xl border p-5">
                                <Mail className="text-primary h-6 w-6 flex-shrink-0" />
                                <div className="text-foreground text-base font-medium">
                                    Code sent to
                                    <br />
                                    <span className="text-primary font-semibold">
                                        {email}
                                    </span>
                                </div>
                            </div>
                        )}

                        <Form {...sendCodeForm}>
                            <form
                                onSubmit={sendCodeForm.handleSubmit(
                                    handleSubmit,
                                )}
                                className="space-y-6"
                            >
                                <FormField
                                    control={sendCodeForm.control}
                                    name="email"
                                    render={({ field }) => (
                                        <FormItem>
                                            <FormControl>
                                                <div className="relative">
                                                    <input
                                                        {...field}
                                                        placeholder={email}
                                                        disabled
                                                        className="bg-secondary/50 text-foreground/80 border-border/50 w-full rounded-lg border px-4 py-3 text-sm font-medium disabled:opacity-60"
                                                    />
                                                </div>
                                            </FormControl>
                                        </FormItem>
                                    )}
                                />

                                <Button
                                    type="submit"
                                    disabled={isLoading}
                                    className="group from-primary to-primary/90 hover:from-primary/90 hover:to-primary/80 text-primary-foreground relative flex h-16 w-full items-center justify-center overflow-hidden rounded-lg bg-gradient-to-r text-base font-semibold transition-all duration-300 disabled:cursor-not-allowed disabled:opacity-60"
                                >
                                    <div className="absolute inset-0 -skew-x-12 transform bg-gradient-to-r from-transparent via-white/10 to-transparent transition-transform duration-700 group-hover:translate-x-full" />

                                    <div className="relative flex w-full items-center justify-between gap-4 px-6">
                                        {isLoading ? (
                                            <div className="flex w-full items-center justify-center gap-3">
                                                <Loader2 className="h-5 w-5 animate-spin" />
                                                <span>Sending code...</span>
                                            </div>
                                        ) : (
                                            <>
                                                <div className="flex flex-1 flex-col items-start gap-1">
                                                    <span className="text-xs font-medium opacity-90">
                                                        Send code to
                                                    </span>
                                                    <span className="max-w-xs text-base leading-tight font-bold">
                                                        {email}
                                                    </span>
                                                </div>
                                                <ArrowRight className="h-5 w-5 flex-shrink-0" />
                                            </>
                                        )}
                                    </div>
                                </Button>
                            </form>
                        </Form>

                        <div className="border-border/30 border-t pt-6 text-center">
                            <p className="text-muted-foreground text-base">
                                Something is not working?{' '}
                                <button className="text-primary hover:text-primary/80 font-semibold transition-colors">
                                    Logout
                                </button>
                            </p>
                        </div>

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
