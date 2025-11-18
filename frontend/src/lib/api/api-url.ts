'use server';

export const getApiUrl = () => {
    return process.env.API_URL || 'http://localhost:8080';
};
