'use client';

import { Button } from '@/components/ui/button';
import {
    Form,
    FormControl,
    FormField,
    FormItem,
    FormMessage,
} from '@/components/ui/form';
import {
    LoginFormValues,
    useFacebookLogin,
    useLogin,
    useOryLoginFlow,
    useSecondFactorRedirect,
} from '@/lib/service/auth';
import Image from 'next/image';
import React, { forwardRef, InputHTMLAttributes, useState } from 'react';

const socialIcons = [
    {
        name: 'Google',
        href: '#',
        icon: (
            <svg
                xmlns="http://www.w3.org/2000/svg"
                viewBox="0 0 48 48"
                className="h-5 w-5"
            >
                <path
                    fill="#fbc02d"
                    d="M43.6 20.5H42V20H24v8h11.3A11.9 11.9 0 0 1 12 24 12 12 0 0 1 24 12c3.1 0 5.9 1.2 7.9 3.1l5.7-5.7A19.9 19.9 0 0 0 24 4 20 20 0 1 0 44 24c0-1.2-.1-2.3-.4-3.5"
                />
                <path
                    fill="#e53935"
                    d="M6.3 14.7 12.9 19A11.9 11.9 0 0 1 24 12c3.1 0 5.9 1.2 7.9 3.1l5.7-5.7A19.9 19.9 0 0 0 24 4C16.9 4 10.6 8 7.3 14.1"
                />
                <path
                    fill="#4caf50"
                    d="M24 44c5.2 0 9.9-2 13.4-5.2l-6.2-5.1A11.9 11.9 0 0 1 12 24H4a20 20 0 0 0 20 20"
                />
                <path
                    fill="#1565c0"
                    d="M43.6 20.5H42V20H24v8h11.3A12 12 0 0 1 24 36c-3 0-5.8-1.1-7.9-2.9l-6.3 5.3A19.9 19.9 0 0 0 24 44a20 20 0 0 0 19.9-20c0-1.2-.1-2.3-.3-3.5"
                />
            </svg>
        ),
    },
    {
        name: 'Facebook',
        href: '#',
        icon: (
            <svg
                xmlns="http://www.w3.org/2000/svg"
                viewBox="0 0 24 24"
                className="h-5 w-5"
            >
                <path
                    fill="currentColor"
                    d="M13 21v-7h2.5l.5-3H13V9.5C13 8.57 13.57 8 14.5 8H16V5h-1.5A4.5 4.5 0 0 0 10 9.5V11H8v3h2v7z"
                />
            </svg>
        ),
    },
    {
        name: 'GitHub',
        href: '#',
        icon: (
            <svg className="h-5 w-5" viewBox="0 0 24 24" aria-hidden="true">
                <path
                    fill="currentColor"
                    d="M12 .5A12 12 0 0 0 0 12.7a12.2 12.2 0 0 0 8.2 11.6c.6.1.8-.3.8-.6v-2.1C6 22 5.3 20 5.3 20a3.2 3.2 0 0 0-1.3-1.7c-1-.7.1-.7.1-.7 1.2.1 1.8 1.3 1.8 1.3a2.1 2.1 0 0 0 3 1c.1-.6.4-1 .7-1.3-2.7-.3-5.6-1.4-5.6-6a4.7 4.7 0 0 1 1.2-3.2 4.4 4.4 0 0 1 .1-3.1s1-.3 3.3 1.2a11.2 11.2 0 0 1 6 0C17 5.8 18 6.1 18 6.1a4.4 4.4 0 0 1 .1 3.1 4.7 4.7 0 0 1 1.2 3.2c0 4.6-2.9 5.6-5.6 5.9a2.4 2.4 0 0 1 .7 1.9v2.8c0 .3.2.7.8.6A12.2 12.2 0 0 0 24 12.7 12 12 0 0 0 12 .5"
                />
            </svg>
        ),
    },
];

interface AppInputProps extends InputHTMLAttributes<HTMLInputElement> {
    label?: string;
    icon?: React.ReactNode;
}

const AppInput = forwardRef<HTMLInputElement, AppInputProps>((props, ref) => {
    const { label, placeholder, icon, ...rest } = props;
    const [mousePosition, setMousePosition] = useState({ x: 0, y: 0 });
    const [isHovering, setIsHovering] = useState(false);

    const handleMouseMove = (e: React.MouseEvent<HTMLInputElement>) => {
        const rect = e.currentTarget.getBoundingClientRect();
        setMousePosition({
            x: e.clientX - rect.left,
            y: e.clientY - rect.top,
        });
    };

    return (
        <div className="relative w-full min-w-[200px]">
            {label && <label className="mb-2 block text-sm">{label}</label>}
            <div className="relative w-full">
                <input
                    ref={ref}
                    className="peer border-input bg-card text-foreground placeholder:text-muted-foreground focus:bg-background relative z-10 h-[3.25rem] w-full rounded-md border px-4 text-sm shadow-sm transition-colors duration-200 ease-in-out outline-none"
                    placeholder={placeholder}
                    onMouseMove={handleMouseMove}
                    onMouseEnter={() => setIsHovering(true)}
                    onMouseLeave={() => setIsHovering(false)}
                    {...rest}
                />

                {isHovering && (
                    <>
                        <div
                            className="pointer-events-none absolute inset-x-0 top-0 z-20 h-[2px] overflow-hidden rounded-t-md"
                            style={{
                                background: `radial-gradient(30px circle at ${mousePosition.x}px 0px, var(--color-text-primary) 0%, transparent 70%)`,
                            }}
                        />
                        <div
                            className="pointer-events-none absolute inset-x-0 bottom-0 z-20 h-[2px] overflow-hidden rounded-b-md"
                            style={{
                                background: `radial-gradient(30px circle at ${mousePosition.x}px 2px, var(--color-text-primary) 0%, transparent 70%)`,
                            }}
                        />
                    </>
                )}

                {icon && (
                    <div className="absolute top-1/2 right-3 z-20 -translate-y-1/2">
                        {icon}
                    </div>
                )}
            </div>
        </div>
    );
});

AppInput.displayName = 'AppInput';

export default function LoginPage() {
    const [mousePosition, setMousePosition] = useState<{
        x: number;
        y: number;
    }>({
        x: 0,
        y: 0,
    });
    const [isHovering, setIsHovering] = useState(false);

    const { form: loginForm, mutation: loginMutation } = useLogin();

    const { flow, isLoading: isFlowLoading } = useOryLoginFlow();

    const { canFacebookLogin, loginWithFacebook } = useFacebookLogin(flow);

    useSecondFactorRedirect(flow);
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
        <div className="flex h-screen w-full items-center justify-center bg-[var(--color-bg)] p-4">
            <div className="card flex h-[600px] w-[80%] justify-between gap-0 md:w-[70%] lg:w-[65%]">
                <div
                    className="relative flex h-full w-full flex-col justify-center px-6 py-8 lg:w-1/2 lg:px-12 lg:py-10"
                    onMouseMove={handleMouseMove}
                    onMouseEnter={() => setIsHovering(true)}
                    onMouseLeave={() => setIsHovering(false)}
                >
                    <div
                        className={`pointer-events-none absolute h-[500px] w-[500px] rounded-full bg-gradient-to-r from-purple-300/25 via-blue-300/25 to-pink-300/25 blur-3xl transition-opacity duration-200 ${
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
                                                className="group border-border relative flex h-10 w-10 items-center justify-center rounded-full border bg-[var(--color-bg-2)] shadow-sm transition-all duration-200 hover:-translate-y-[1px] hover:shadow-[0_0_25px_rgba(255,255,255,0.25)] md:h-11 md:w-11"
                                                onClick={() => {
                                                    if (
                                                        social.name ===
                                                        'Facebook'
                                                    ) {
                                                        loginWithFacebook();
                                                    }
                                                }}
                                                disabled={
                                                    social.name ===
                                                        'Facebook' &&
                                                    (isFlowLoading ||
                                                        !canFacebookLogin)
                                                }
                                            >
                                                <span className="absolute inset-0 rounded-full bg-gradient-to-tr from-white/10 via-transparent to-white/5 opacity-0 blur-sm transition-opacity duration-200 group-hover:opacity-100" />
                                                <span className="relative z-[1] text-[var(--color-text-primary)]">
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
                                                        type="password"
                                                        autoComplete="current-password"
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
                        src="https://images.pexels.com/photos/7102037/pexels-photo-7102037.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2"
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
