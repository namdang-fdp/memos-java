import { Counter } from "@/components/counter";
import { Header } from "@/components/header";

export default function HomePage() {
  return (
    <main className="min-h-screen flex flex-col items-center justify-center gap-4">
      <h1 className="text-2xl font-bold">Zustand Counter Demo</h1>
      <Counter />
      <Header />
    </main>
  );
}
