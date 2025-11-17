import { useMutation } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { LoginFormValues, LoginPayload, LoginResponse, loginSchema } from './type';
import { zodResolver } from '@hookform/resolvers/zod';

export const useLogin = () => {
    const mutation = useMutation({
        mutationFn: async (values: LoginPayload) => {
            await new Promise((r) => setTimeout(r, 1000));
            return {
                token: "fake-token-123",
                user: {
                    id: "1",
                    email: values.email,
                    name: "Ken Demo",
                },
            } as LoginResponse;
        },
        onSuccess: (data) => {
            console.log("Logged in: ", data)
        }
    })
    const form = useForm<LoginFormValues>({
        resolver: zodResolver(loginSchema),
        defaultValues: {
            email: "",
            password: "",
        },
    });

    return {
        form, mutation
    }
}


