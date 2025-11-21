export interface User {
    id: string;
    email: string;
    name: string;
    provider: string;
    createAt: string;
    role: string;
    permissions: string[];
    active: boolean;
}
