import axios, {
    AxiosError,
    AxiosInstance,
    AxiosRequestConfig,
    AxiosResponse,
} from 'axios';

import { getApiUrl } from './api-url';

const apiUrl = getApiUrl();

export interface Pagination<T> {
    totalElements: number;
    content: T[];
}

export class ApiError extends Error {
    status?: number;
    details: Record<string, string>;

    constructor(
        message: string,
        options?: { status?: number; details?: Record<string, string> },
    ) {
        super(message);
        this.name = 'ApiError';
        this.status = options?.status;
        this.details = options?.details ?? {};
    }
}

export const api: AxiosInstance = axios.create({
    baseURL: apiUrl,
    withCredentials: true,
});
