"use client";

import { useCounterStore } from "@/lib/stores/counterStore";

export function Header() {
  const count = useCounterStore((state) => state.count);
  return (
    <header className="w-full p-4 border-b flex justify-between">
      <span>My App</span>
      <span>Global count: {count}</span>
    </header>
  );
}
