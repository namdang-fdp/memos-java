import { axiosWrapper, deserialize } from '@/lib/api/axios-config';
import { useQuery } from '@tanstack/react-query';
import { Project } from './type';

export const useGetProjects = () => {
    return useQuery({
        queryKey: ['projects'],
        queryFn: async () => {
            const response = await axiosWrapper('/projects');
            return {
                message: response.data.message,
                result: deserialize<Project>(response.data),
            };
        },
    });
};
