'use client';

import { Button } from '@/components/ui/button';
import {
    Form,
    FormControl,
    FormField,
    FormItem,
    FormMessage,
} from '@/components/ui/form';
import { AppInput } from '@/components/app-input';
import { ProfileFormValues, useSetupProfile } from '@/lib/service/auth';

export default function ProfileSetupPage() {
    const { form: profileForm, mutation: setupProfileMutation } =
        useSetupProfile();

    const handleSubmit = (values: ProfileFormValues) => {
        setupProfileMutation.mutate(values);
    };

    return (
        <div className="flex h-screen w-full items-center justify-center bg-(--color-bg) p-4">
            <div className="card flex h-[500px] w-[400px] justify-center gap-0 md:w-[70%] lg:w-[65%]">
                <div className="relative flex h-full w-full flex-col justify-center px-6 py-8 lg:px-12 lg:py-10">
                    <div className="relative z-10 flex flex-col gap-6">
                        <div className="space-y-2 text-left">
                            <p className="text-muted-foreground text-xs font-semibold tracking-[0.2em] uppercase">
                                Complete your profile
                            </p>
                            <h1 className="text-3xl font-extrabold md:text-4xl">
                                Personal Information
                            </h1>
                            <p className="text-muted-foreground text-sm">
                                Please provide your name to complete your setup.
                            </p>
                        </div>

                        <Form {...profileForm}>
                            <form
                                className="flex flex-col gap-5"
                                onSubmit={profileForm.handleSubmit(
                                    handleSubmit,
                                )}
                            >
                                <div className="flex flex-col gap-4">
                                    <FormField
                                        control={profileForm.control}
                                        name="name"
                                        render={({ field }) => (
                                            <FormItem>
                                                <FormControl>
                                                    <AppInput
                                                        placeholder="Your name"
                                                        type="text"
                                                        {...field}
                                                    />
                                                </FormControl>
                                                <FormMessage />
                                            </FormItem>
                                        )}
                                    />

                                    <p className="text-muted-foreground text-sm">
                                        Your name will be visible to other
                                        teammates in your project.
                                    </p>
                                </div>

                                <div className="flex flex-col gap-3 pt-1">
                                    <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                                        <Button
                                            type="submit"
                                            // disabled={isSubmitting}
                                            className="inline-flex items-center justify-center rounded-md bg-white px-6 py-2 text-sm font-semibold text-black shadow-sm transition-colors hover:bg-neutral-200 disabled:cursor-not-allowed disabled:bg-neutral-400 disabled:text-neutral-900"
                                        >
                                            {/* {isSubmitting ? 'Saving...' : 'Save'} */}
                                            Save
                                        </Button>
                                    </div>
                                </div>
                            </form>
                        </Form>
                    </div>
                </div>
            </div>
        </div>
    );
}
