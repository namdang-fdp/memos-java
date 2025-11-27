'use client';

import { LoadingSpinner } from '@/components/loading-spinner';
import { Button } from '@/components/ui/button';
import {
    Form,
    FormControl,
    FormField,
    FormItem,
    FormMessage,
} from '@/components/ui/form';
import {
    isSecondFactorFlow,
    LoginFormValues,
    useFacebookLogin,
    useGithubLogin,
    useGoogleLogin,
    useLogin,
    useOryLoginFlow,
    useSecondFactorRedirect,
} from '@/lib/service/auth';
import Image from 'next/image';
import React, { useState } from 'react';
import { socialIcons } from '../../../../constant/constant-data';
import { AppInput } from '@/components/app-input';
import { Eye, EyeOff } from 'lucide-react';
import { useRouter } from 'next/navigation';

export default function LoginPage() {
    const router = useRouter();
    const [mousePosition, setMousePosition] = useState<{
        x: number;
        y: number;
    }>({ x: 0, y: 0 });
    const [isHovering, setIsHovering] = useState(false);

    const [showPassword, setShowPassword] = useState(false);

    const { form: loginForm, mutation: loginMutation } = useLogin();

    const { flow, isLoading: isFlowLoading } = useOryLoginFlow('login');

    const { canFacebookLogin, loginWithFacebook } = useFacebookLogin(flow);
    const { canGithubLogin, loginWithGithub } = useGithubLogin(flow);
    const { canGoogleLogin, loginWithGoogle } = useGoogleLogin(flow);

    const handleSocialLogin = (name: string) => {
        switch (name) {
            case 'Facebook':
                return loginWithFacebook();
            case 'GitHub':
                return loginWithGithub();
            case 'Google':
                return loginWithGoogle();
            default:
                break;
        }
    };

    const isSocialDisable = (provider: string) => {
        if (provider === 'Facebook') {
            return isFlowLoading || !canFacebookLogin;
        }
        if (provider === 'GitHub') {
            return isFlowLoading || !canGithubLogin;
        }
        if (provider === 'Google') {
            return isFlowLoading || !canGoogleLogin;
        }
        return true;
    };

    useSecondFactorRedirect(flow);

    if (isFlowLoading || !flow) {
        return (
            <div className="flex min-h-screen items-center justify-center">
                <LoadingSpinner size="lg" showText={false} />
            </div>
        );
    }

    if (isSecondFactorFlow(flow)) {
        return (
            <div className="flex min-h-screen items-center justify-center">
                <LoadingSpinner size="lg" showText={false} />
            </div>
        );
    }

    const handleMouseMove = (e: React.MouseEvent<HTMLDivElement>) => {
        const rect = e.currentTarget.getBoundingClientRect();
        setMousePosition({
            x: e.clientX - rect.left,
            y: e.clientY - rect.top,
        });
    };

    const onSubmit = (values: LoginFormValues) => {
        loginMutation.mutate(values);
    };

    const isSubmitting = loginMutation.isPending;

    return (
        <div className="flex h-screen w-full items-center justify-center bg-(--color-bg) p-4">
            <div className="card flex h-[600px] w-[80%] justify-between gap-0 md:w-[70%] lg:w-[65%]">
                <div
                    className="relative flex h-full w-full flex-col justify-center px-6 py-8 lg:w-1/2 lg:px-12 lg:py-10"
                    onMouseMove={handleMouseMove}
                    onMouseEnter={() => setIsHovering(true)}
                    onMouseLeave={() => setIsHovering(false)}
                >
                    <div
                        className={`pointer-events-none absolute h-[500px] w-[500px] rounded-full bg-linear-to-r from-purple-300/25 via-blue-300/25 to-pink-300/25 blur-3xl transition-opacity duration-200 ${
                            isHovering ? 'opacity-100' : 'opacity-0'
                        }`}
                        style={{
                            transform: `translate(${mousePosition.x - 250}px, ${mousePosition.y - 250}px)`,
                            transition: 'transform 0.1s ease-out',
                        }}
                    />

                    <div className="relative z-10 flex flex-col gap-6">
                        <div className="space-y-2 text-left">
                            <p className="text-muted-foreground text-xs font-semibold tracking-[0.2em] uppercase">
                                Welcome back
                            </p>
                            <h1 className="text-3xl font-extrabold md:text-4xl">
                                Sign in
                            </h1>
                            <p className="text-muted-foreground text-sm">
                                Enter your credentials to access your account.
                            </p>
                        </div>

                        <div className="space-y-3">
                            <div className="flex items-center justify-start">
                                <ul className="flex gap-3 md:gap-4">
                                    {socialIcons.map((social) => (
                                        <li key={social.name}>
                                            <button
                                                type="button"
                                                className="group border-border relative flex h-10 w-10 items-center justify-center rounded-full border bg-(--color-bg-2) shadow-sm transition-all duration-200 hover:-translate-y-px hover:shadow-[0_0_25px_rgba(255,255,255,0.25)] md:h-11 md:w-11"
                                                onClick={() =>
                                                    handleSocialLogin(
                                                        social.name,
                                                    )
                                                }
                                                disabled={isSocialDisable(
                                                    social.name,
                                                )}
                                            >
                                                <span className="absolute inset-0 rounded-full bg-linear-to-tr from-white/10 via-transparent to-white/5 opacity-0 blur-sm transition-opacity duration-200 group-hover:opacity-100" />
                                                <span className="relative z-1 text-(--color-text-primary)">
                                                    {social.icon}
                                                </span>
                                            </button>
                                        </li>
                                    ))}
                                </ul>
                            </div>
                            <span className="text-muted-foreground text-sm">
                                or use your email and password
                            </span>
                        </div>

                        <Form {...loginForm}>
                            <form
                                className="flex flex-col gap-5"
                                onSubmit={loginForm.handleSubmit(onSubmit)}
                            >
                                <div className="flex flex-col gap-4">
                                    <FormField
                                        control={loginForm.control}
                                        name="email"
                                        render={({ field }) => (
                                            <FormItem>
                                                <FormControl>
                                                    <AppInput
                                                        placeholder="you@example.com"
                                                        type="email"
                                                        autoComplete="email"
                                                        {...field}
                                                    />
                                                </FormControl>
                                                <FormMessage />
                                            </FormItem>
                                        )}
                                    />
                                    <FormField
                                        control={loginForm.control}
                                        name="password"
                                        render={({ field }) => (
                                            <FormItem>
                                                <FormControl>
                                                    <AppInput
                                                        placeholder="Your password"
                                                        type={
                                                            showPassword
                                                                ? 'text'
                                                                : 'password'
                                                        }
                                                        autoComplete="current-password"
                                                        icon={
                                                            <button
                                                                type="button"
                                                                tabIndex={-1}
                                                                onClick={() =>
                                                                    setShowPassword(
                                                                        (
                                                                            prev,
                                                                        ) =>
                                                                            !prev,
                                                                    )
                                                                }
                                                                className="text-muted-foreground hover:text-foreground"
                                                            >
                                                                {showPassword ? (
                                                                    <EyeOff className="h-4 w-4" />
                                                                ) : (
                                                                    <Eye className="h-4 w-4" />
                                                                )}
                                                            </button>
                                                        }
                                                        {...field}
                                                    />
                                                </FormControl>
                                                <FormMessage />
                                            </FormItem>
                                        )}
                                    />
                                </div>

                                {loginMutation.isError && (
                                    <p className="text-sm text-red-500">
                                        {(loginMutation.error as Error)
                                            .message || 'Đăng nhập thất bại'}
                                    </p>
                                )}

                                <div className="flex flex-col gap-3 pt-1">
                                    <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                                        <Button
                                            type="submit"
                                            disabled={isSubmitting}
                                            className="inline-flex items-center justify-center rounded-md bg-white px-6 py-2 text-sm font-semibold text-black shadow-sm transition-colors hover:bg-neutral-200 disabled:cursor-not-allowed disabled:bg-neutral-400 disabled:text-neutral-900"
                                        >
                                            {isSubmitting
                                                ? 'Signing in...'
                                                : 'Sign In'}
                                        </Button>

                                        <button
                                            type="button"
                                            className="text-muted-foreground hover:text-foreground text-sm transition-colors"
                                        >
                                            Forgot your password?
                                        </button>
                                    </div>

                                    <div className="text-muted-foreground text-sm">
                                        Don&apos;t have an account?{' '}
                                        <button
                                            type="button"
                                            className="text-primary font-medium underline-offset-4 hover:underline"
                                            onClick={() => {
                                                router.push('/auth/register');
                                            }}
                                        >
                                            Sign up
                                        </button>
                                    </div>
                                </div>
                            </form>
                        </Form>
                    </div>
                </div>

                <div className="right hidden h-full w-1/2 overflow-hidden lg:block">
                    <Image
                        src="/auth-banner.jpeg"
                        loader={({ src }) => src}
                        width={1000}
                        height={1000}
                        priority
                        alt="Login Banner"
                        className="h-full w-full object-cover opacity-30 transition-transform duration-300"
                    />
                </div>
            </div>
        </div>
    );
}
