'use client';

import { useState } from 'react';
import { Home, Plus, Bell, Settings } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { cn } from '@/lib/utils';
import { useGetProjects } from '@/lib/service/project/api';

const servers = [
    { id: 'home', icon: Home, name: 'Home' },
    { id: '1', name: 'Project Alpha', abbr: 'PA' },
    { id: '2', name: 'Beta Team', abbr: 'BT' },
    { id: '3', name: 'Design Hub', abbr: 'DH' },
    { id: '4', name: 'Engineering', abbr: 'EN' },
];

export function ProjectSidebar() {
    const [activeServer, setActiveServer] = useState('home');
    const [hoveredServer, setHoveredServer] = useState<string | null>(null);
    const { data: projects, isLoading, isError } = useGetProjects();
    if (isLoading) {
        return <div>Loading</div>;
    }
    if (isError) {
        return <div>Error</div>;
    }
    return (
        <div className="bg-sidebar flex h-full w-[72px] flex-col items-center gap-2 py-3">
            <div className="flex flex-col items-center gap-2">
                {servers.map((server) => (
                    <div key={server.id} className="relative">
                        <Button
                            onClick={() => setActiveServer(server.id)}
                            onMouseEnter={() => setHoveredServer(server.id)}
                            onMouseLeave={() => setHoveredServer(null)}
                            className={cn(
                                'group smooth-transition relative h-12 w-12 rounded-3xl p-0',
                                activeServer === server.id
                                    ? 'bg-primary text-primary-foreground rounded-2xl'
                                    : 'bg-secondary text-secondary-foreground hover:bg-accent hover:text-accent-foreground hover:rounded-2xl',
                            )}
                        >
                            {server.icon ? (
                                <server.icon className="h-5 w-5" />
                            ) : (
                                <span className="text-sm font-semibold">
                                    {server.abbr}
                                </span>
                            )}
                        </Button>

                        {activeServer === server.id && (
                            <div className="bg-foreground smooth-transition absolute top-1/2 left-0 h-10 w-1 -translate-x-3 -translate-y-1/2 rounded-r-full" />
                        )}

                        {hoveredServer === server.id && (
                            <div className="animate-in fade-in-0 zoom-in-95 slide-in-from-left-2 pointer-events-none absolute top-0 left-[60px] z-50">
                                <div className="bg-popover text-popover-foreground rounded-lg px-3 py-2 text-sm font-medium shadow-xl">
                                    {server.name}
                                </div>
                            </div>
                        )}
                    </div>
                ))}

                <Button className="group border-border text-muted-foreground smooth-transition hover:border-primary hover:bg-primary/10 hover:text-primary h-12 w-12 rounded-3xl border-2 border-dashed bg-transparent p-0 hover:rounded-2xl">
                    <Plus className="h-5 w-5" />
                </Button>
            </div>

            <div className="mt-auto flex flex-col items-center gap-2">
                <div className="bg-border h-px w-8" />

                <Button className="bg-secondary text-secondary-foreground smooth-transition hover:bg-accent hover:text-accent-foreground h-12 w-12 rounded-3xl p-0 hover:rounded-2xl">
                    <Bell className="h-5 w-5" />
                </Button>

                <Button className="bg-secondary text-secondary-foreground smooth-transition hover:bg-accent hover:text-accent-foreground h-12 w-12 rounded-3xl p-0 hover:rounded-2xl">
                    <Settings className="h-5 w-5" />
                </Button>
            </div>
        </div>
    );
}
