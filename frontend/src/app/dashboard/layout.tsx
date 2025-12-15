import { ProjectSidebar } from './project-sidebar';

export default function DashboardLayout({
    children,
}: {
    children: React.ReactNode;
}) {
    return (
        <div className="bg-background flex h-screen overflow-hidden">
            <ProjectSidebar />
            <div className="flex flex-1 flex-col overflow-hidden">
                <header className="border-border bg-card/50 flex items-center justify-between border-b px-6 py-4 backdrop-blur-sm">
                    <div>
                        <h1 className="text-foreground text-xl font-semibold">
                            Dashboard
                        </h1>
                        <p className="text-muted-foreground text-sm">
                            Manage your work
                        </p>
                    </div>
                </header>

                <main className="custom-scrollbar bg-muted/30 flex-1 overflow-auto p-6">
                    {children}
                </main>
            </div>
        </div>
    );
}
