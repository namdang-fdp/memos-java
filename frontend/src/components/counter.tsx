"use client";

import { useCounterStore } from "@/lib/stores/counterStore";

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
        <button onClick={decrease} className="px-3 py-1 rounded border">
          -1
        </button>
        <button onClick={increase} className="px-3 py-1 rounded border">
          +1
        </button>
        <button onClick={reset} className="px-3 py-1 rounded border">
          Reset
        </button>
      </div>
    </div>
  );
}
