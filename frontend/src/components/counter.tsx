'use client';

import { useCounterStore } from '@/lib/stores/counterStore';

export function Counter() {
    const count = useCounterStore((state) => state.count);
    const increase = useCounterStore((state) => state.increase);
    const decrease = useCounterStore((state) => state.decrease);
    const reset = useCounterStore((state) => state.reset);

    return (
        <div className="flex flex-col items-center gap-2">
            <p className="text-xl">
                Count: <span className="font-mono">{count}</span>
            </p>

            <div className="flex gap-2">
                <button onClick={decrease} className="rounded border px-3 py-1">
                    -1
                </button>
                <button onClick={increase} className="rounded border px-3 py-1">
                    +1
                </button>
                <button onClick={reset} className="rounded border px-3 py-1">
                    Reset
                </button>
            </div>
        </div>
    );
}
