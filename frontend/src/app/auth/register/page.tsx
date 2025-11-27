'use client';

import { Button } from '@/components/ui/button';
import {
    Form,
    FormControl,
    FormField,
    FormItem,
    FormMessage,
} from '@/components/ui/form';
import Image from 'next/image';
import React, { useState } from 'react';
import { AppInput } from '@/components/app-input';
import { socialIcons } from '../../../../constant/constant-data';
import { useRouter } from 'next/navigation';
import { RegisterFormValues, useRegister } from '@/lib/service/auth';

export default function RegisterPage() {
    const router = useRouter();
    const [mousePosition, setMousePosition] = useState<{
        x: number;
        y: number;
    }>({ x: 0, y: 0 });
    const [isHovering, setIsHovering] = useState(false);
    const { form: registerForm, mutation: registerMutation } = useRegister();

    const { isSubmitting, isValid } = registerForm.formState;

    const handleMouseMove = (e: React.MouseEvent<HTMLDivElement>) => {
        const rect = e.currentTarget.getBoundingClientRect();
        setMousePosition({
            x: e.clientX - rect.left,
            y: e.clientY - rect.top,
        });
    };

    const onSubmit = (values: RegisterFormValues) => {
        registerMutation.mutate(values);
    };

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
                                Create account
                            </p>
                            <h1 className="text-3xl font-extrabold md:text-4xl">
                                Sign up
                            </h1>
                            <p className="text-muted-foreground text-sm">
                                Enter your details to create a new account.
                            </p>
                        </div>

                        <div className="space-y-2">
                            <div className="flex items-center justify-start">
                                <ul className="flex gap-3 md:gap-4">
                                    {socialIcons.map((social) => (
                                        <li key={social.name}>
                                            <button
                                                type="button"
                                                className="group border-border relative flex h-10 w-10 items-center justify-center rounded-full border bg-(--color-bg-2) shadow-sm transition-all duration-200 hover:-translate-y-px hover:shadow-[0_0_25px_rgba(255,255,255,0.25)] md:h-11 md:w-11"
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
                                or sign up with your email
                            </span>
                        </div>

                        <Form {...registerForm}>
                            <form
                                className="flex flex-col gap-5"
                                onSubmit={registerForm.handleSubmit(onSubmit)}
                            >
                                <div className="flex flex-col gap-4">
                                    <FormField
                                        control={registerForm.control}
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
                                        control={registerForm.control}
                                        name="password"
                                        render={({ field }) => (
                                            <FormItem>
                                                <FormControl>
                                                    <AppInput
                                                        placeholder="Your password"
                                                        type="password"
                                                        autoComplete="new-password"
                                                        {...field}
                                                    />
                                                </FormControl>
                                                <FormMessage />
                                            </FormItem>
                                        )}
                                    />

                                    <FormField
                                        control={registerForm.control}
                                        name="confirmPassword"
                                        render={({ field }) => (
                                            <FormItem>
                                                <FormControl>
                                                    <AppInput
                                                        placeholder="Confirm your password"
                                                        type="password"
                                                        autoComplete="new-password"
                                                        {...field}
                                                    />
                                                </FormControl>
                                                <FormMessage />
                                            </FormItem>
                                        )}
                                    />
                                </div>

                                <div className="flex flex-col gap-3 pt-1">
                                    <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                                        <Button
                                            type="submit"
                                            disabled={isSubmitting || !isValid}
                                            className="inline-flex items-center justify-center rounded-md bg-white px-6 py-2 text-sm font-semibold text-black shadow-sm transition-colors hover:bg-neutral-200 disabled:cursor-not-allowed disabled:bg-neutral-400 disabled:text-neutral-900"
                                        >
                                            {isSubmitting
                                                ? 'Signing up...'
                                                : 'Sign Up'}
                                        </Button>

                                        <button
                                            type="button"
                                            className="text-muted-foreground hover:text-foreground text-sm transition-colors"
                                            onClick={() =>
                                                router.push('/auth/login')
                                            }
                                        >
                                            Already have an account?
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
                        alt="Register Banner"
                        className="h-full w-full object-cover opacity-30 transition-transform duration-300"
                    />
                </div>
            </div>
        </div>
    );
}
