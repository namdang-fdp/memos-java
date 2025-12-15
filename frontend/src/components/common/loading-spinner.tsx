'use client';

import { cn } from '@/lib/utils';

interface LoadingSpinnerProps {
    isLoading?: boolean;
    size?: 'sm' | 'md' | 'lg';
    showText?: boolean;
}

export function LoadingSpinner({
    isLoading = true,
    size = 'md',
    showText = true,
}: LoadingSpinnerProps) {
    if (!isLoading) return null;

    const sizeClasses = {
        sm: 'w-8 h-8',
        md: 'w-12 h-12',
        lg: 'w-16 h-16',
    };

    return (
        <div className="flex flex-col items-center justify-center gap-4">
            <div className="relative flex items-center justify-center">
                <div
                    className={cn(
                        'absolute rounded-full',
                        'bg-linear-to-r from-blue-500/20 via-purple-500/20 to-blue-500/20',
                        'blur-xl',
                        sizeClasses[size],
                    )}
                    style={{
                        animation:
                            'pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite',
                    }}
                />

                <div
                    className={cn(
                        'absolute rounded-full border-2',
                        'border-transparent border-t-white border-r-white',
                        'dark:border-t-white dark:border-r-white',
                        sizeClasses[size],
                    )}
                    style={{
                        animation: 'spin 2s linear infinite',
                    }}
                />

                <div
                    className={cn(
                        'absolute rounded-full border border-dashed',
                        'border-white/40',
                        sizeClasses[size],
                    )}
                    style={{
                        animation: 'spin 3s linear reverse infinite',
                    }}
                />
            </div>

            {showText && (
                <p className="text-muted-foreground animate-pulse text-sm">
                    Loading...
                </p>
            )}

            <style>{`
        @keyframes spin {
          from {
            transform: rotate(0deg);
          }
          to {
            transform: rotate(360deg);
          }
        }

        @keyframes pulse {
          0%,
          100% {
            opacity: 1;
          }
          50% {
            opacity: 0.5;
          }
        }
      `}</style>
        </div>
    );
}
