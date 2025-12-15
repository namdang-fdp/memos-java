import { forwardRef, InputHTMLAttributes, useState } from 'react';

interface AppInputProps extends InputHTMLAttributes<HTMLInputElement> {
    label?: string;
    icon?: React.ReactNode;
}

export const AppInput = forwardRef<HTMLInputElement, AppInputProps>(
    (props, ref) => {
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
                        className="peer border-input bg-card text-foreground placeholder:text-muted-foreground focus:bg-background relative z-10 h-13 w-full rounded-md border px-4 text-sm shadow-sm transition-colors duration-200 ease-in-out outline-none"
                        placeholder={placeholder}
                        onMouseMove={handleMouseMove}
                        onMouseEnter={() => setIsHovering(true)}
                        onMouseLeave={() => setIsHovering(false)}
                        {...rest}
                    />

                    {isHovering && (
                        <>
                            <div
                                className="pointer-events-none absolute inset-x-0 top-0 z-20 h-0.5 overflow-hidden rounded-t-md"
                                style={{
                                    background: `radial-gradient(30px circle at ${mousePosition.x}px 0px, var(--color-text-primary) 0%, transparent 70%)`,
                                }}
                            />
                            <div
                                className="pointer-events-none absolute inset-x-0 bottom-0 z-20 h-0.5 overflow-hidden rounded-b-md"
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
    },
);

AppInput.displayName = 'AppInput';
