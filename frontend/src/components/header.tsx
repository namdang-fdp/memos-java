'use client';

import { useCounterStore } from '@/lib/stores/counterStore';

export function Header() {
    const count = useCounterStore((state) => state.count);
    return (
        <header className="flex w-full justify-between border-b p-4">
            <span>My App</span>
            <span>Global count: {count}</span>
        </header>
    );
}
