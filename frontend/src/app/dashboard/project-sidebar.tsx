'use client';

import { useState } from 'react';
import { Home, Plus, Bell, Settings } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { cn } from '@/lib/utils';

const servers = [
    { id: 'home', icon: Home, name: 'Home' },
    { id: '1', name: 'Project Alpha', abbr: 'PA', color: 'bg-blue-500' },
    { id: '2', name: 'Beta Team', abbr: 'BT', color: 'bg-emerald-500' },
    { id: '3', name: 'Design Hub', abbr: 'DH', color: 'bg-purple-500' },
    { id: '4', name: 'Engineering', abbr: 'EN', color: 'bg-orange-500' },
];

export function ProjectSidebar() {
    const [activeServer, setActiveServer] = useState('home');
    const [hoveredServer, setHoveredServer] = useState<string | null>(null);

    return (
        /* Enhanced sidebar with better visual hierarchy and hover states */
        <div className="bg-sidebar border-sidebar-border flex h-full w-[72px] shrink-0 flex-col items-center gap-2 border-r py-3">
            <div className="flex flex-col items-center gap-2">
                {servers.map((server) => (
                    <div key={server.id} className="relative">
                        <Button
                            onClick={() => setActiveServer(server.id)}
                            onMouseEnter={() => setHoveredServer(server.id)}
                            onMouseLeave={() => setHoveredServer(null)}
                            className={cn(
                                'group relative h-12 w-12 rounded-2xl p-0 transition-all duration-200',
                                activeServer === server.id
                                    ? server.icon
                                        ? 'bg-sidebar-primary text-sidebar-primary-foreground rounded-xl shadow-lg'
                                        : `${server.color} rounded-xl text-white shadow-lg`
                                    : 'bg-sidebar-accent text-sidebar-accent-foreground hover:bg-sidebar-primary/10 hover:rounded-xl',
                            )}
                        >
                            {server.icon ? (
                                <server.icon className="h-5 w-5" />
                            ) : (
                                <span className="text-sm font-bold">
                                    {server.abbr}
                                </span>
                            )}
                            {activeServer === server.id && (
                                <div className="bg-sidebar-primary absolute top-1/2 -left-3 h-8 w-1 -translate-y-1/2 rounded-r-full transition-all duration-200" />
                            )}
                        </Button>

                        {hoveredServer === server.id && (
                            <div className="animate-in fade-in-0 zoom-in-95 slide-in-from-left-2 pointer-events-none absolute top-1/2 left-16 z-50 -translate-y-1/2">
                                <div className="bg-popover text-popover-foreground ring-border flex items-center gap-2 rounded-lg px-3 py-2 text-sm font-medium shadow-xl ring-1">
                                    {server.name}
                                </div>
                            </div>
                        )}
                    </div>
                ))}

                <Button className="border-sidebar-border text-muted-foreground hover:border-sidebar-primary hover:bg-sidebar-primary/10 hover:text-sidebar-primary group h-12 w-12 rounded-2xl border-2 border-dashed bg-transparent p-0 transition-all duration-200 hover:rounded-xl">
                    <Plus className="h-5 w-5 transition-transform duration-200 group-hover:rotate-90" />
                </Button>
            </div>

            <div className="mt-auto flex flex-col items-center gap-2">
                <div className="bg-sidebar-border h-px w-8" />

                <Button className="bg-sidebar-accent text-sidebar-accent-foreground hover:bg-sidebar-primary/10 hover:text-sidebar-primary h-12 w-12 rounded-2xl p-0 transition-all duration-200 hover:rounded-xl">
                    <Bell className="h-5 w-5" />
                </Button>

                <Button className="bg-sidebar-accent text-sidebar-accent-foreground hover:bg-sidebar-primary/10 hover:text-sidebar-primary h-12 w-12 rounded-2xl p-0 transition-all duration-200 hover:rounded-xl">
                    <Settings className="h-5 w-5" />
                </Button>
            </div>
        </div>
    );
}
