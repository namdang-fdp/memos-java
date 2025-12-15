'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { useState } from 'react';
import { Home, Plus, Bell, Settings2, SunMoon, Sun, Moon } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { cn } from '@/lib/utils';
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuLabel,
    DropdownMenuSeparator,
    DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import { useTheme } from 'next-themes';

const projects = [
    { id: '1', name: 'Project Alpha', abbr: 'PA', color: 'bg-blue-500' },
    { id: '2', name: 'Beta Team', abbr: 'BT', color: 'bg-emerald-500' },
    { id: '3', name: 'Design Hub', abbr: 'DH', color: 'bg-purple-500' },
    { id: '4', name: 'Engineering', abbr: 'EN', color: 'bg-orange-500' },
];

export function ProjectSidebar() {
    const pathname = usePathname();
    const [hoveredProject, setHoveredProject] = useState<string | null>(null);

    const isHomeActive = pathname === '/dashboard';
    const activeProjectId = pathname.startsWith('/dashboard/')
        ? pathname.split('/dashboard/')[1]?.split('/')[0]
        : null;

    return (
        <div className="bg-sidebar border-sidebar-border flex h-full w-[72px] shrink-0 flex-col items-center gap-2 border-r py-3">
            <div className="flex flex-col items-center gap-2">
                <SidebarItem
                    href="/dashboard"
                    isActive={isHomeActive}
                    onHover={setHoveredProject}
                    hoveredId={hoveredProject}
                    id="home"
                    label="Home"
                    icon={<Home className="h-5 w-5" />}
                />

                {projects.map((p) => (
                    <SidebarProjectItem
                        key={p.id}
                        project={p}
                        isActive={activeProjectId === p.id}
                        hoveredProject={hoveredProject}
                        setHoveredProject={setHoveredProject}
                    />
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

                <SettingsDropdown />
            </div>
        </div>
    );
}

function SidebarItem({
    href,
    isActive,
    id,
    label,
    icon,
    onHover,
    hoveredId,
}: {
    href: string;
    isActive: boolean;
    id: string;
    label: string;
    icon: React.ReactNode;
    onHover: (id: string | null) => void;
    hoveredId: string | null;
}) {
    return (
        <div className="relative">
            <Link href={href}>
                <Button
                    onMouseEnter={() => onHover(id)}
                    onMouseLeave={() => onHover(null)}
                    className={cn(
                        'group relative h-12 w-12 rounded-2xl p-0 transition-all duration-200',
                        isActive
                            ? 'bg-sidebar-primary text-sidebar-primary-foreground rounded-xl shadow-lg'
                            : 'bg-sidebar-accent text-sidebar-accent-foreground hover:bg-sidebar-primary/10 hover:rounded-xl',
                    )}
                >
                    {icon}
                    {isActive && (
                        <div className="bg-sidebar-primary absolute top-1/2 -left-3 h-8 w-1 -translate-y-1/2 rounded-r-full transition-all duration-200" />
                    )}
                </Button>
            </Link>

            {hoveredId === id && <TooltipBubble label={label} />}
        </div>
    );
}

function SidebarProjectItem({
    project,
    isActive,
    hoveredProject,
    setHoveredProject,
}: {
    project: { id: string; name: string; abbr: string; color: string };
    isActive: boolean;
    hoveredProject: string | null;
    setHoveredProject: (id: string | null) => void;
}) {
    return (
        <div className="relative">
            <Link href={`/dashboard/${project.id}`}>
                <Button
                    onMouseEnter={() => setHoveredProject(project.id)}
                    onMouseLeave={() => setHoveredProject(null)}
                    className={cn(
                        'group relative h-12 w-12 rounded-2xl p-0 transition-all duration-200',
                        isActive
                            ? `${project.color} rounded-xl text-white shadow-lg`
                            : 'bg-sidebar-accent text-sidebar-accent-foreground hover:bg-sidebar-primary/10 hover:rounded-xl',
                    )}
                >
                    <span className="text-sm font-bold">{project.abbr}</span>
                    {isActive && (
                        <div className="bg-sidebar-primary absolute top-1/2 -left-3 h-8 w-1 -translate-y-1/2 rounded-r-full transition-all duration-200" />
                    )}
                </Button>
            </Link>

            {hoveredProject === project.id && (
                <TooltipBubble label={project.name} />
            )}
        </div>
    );
}

function TooltipBubble({ label }: { label: string }) {
    return (
        <div className="animate-in fade-in-0 zoom-in-95 slide-in-from-left-2 pointer-events-none absolute top-1/2 left-16 z-50 -translate-y-1/2">
            <div className="bg-popover text-popover-foreground ring-border flex items-center gap-2 rounded-lg px-3 py-2 text-sm font-medium shadow-xl ring-1">
                {label}
            </div>
        </div>
    );
}

function SettingsDropdown() {
    const { theme, setTheme } = useTheme();

    return (
        <DropdownMenu>
            <DropdownMenuTrigger asChild>
                <Button className="bg-sidebar-accent text-sidebar-accent-foreground hover:bg-sidebar-primary/10 hover:text-sidebar-primary group h-12 w-12 rounded-2xl p-0 transition-all duration-200 hover:rounded-xl">
                    <Settings2 className="h-5 w-5 transition-transform duration-200 group-data-[state=open]:rotate-90" />
                </Button>
            </DropdownMenuTrigger>

            <DropdownMenuContent align="start" side="right" className="w-56">
                <DropdownMenuLabel className="flex items-center gap-2">
                    <SunMoon className="h-4 w-4" />
                    Settings
                </DropdownMenuLabel>
                <DropdownMenuSeparator />

                <DropdownMenuItem
                    onClick={() =>
                        setTheme(theme === 'dark' ? 'light' : 'dark')
                    }
                    className="flex items-center justify-between"
                >
                    <span className="flex items-center gap-2">
                        {theme === 'dark' ? (
                            <Sun className="h-4 w-4 transition-transform duration-200 group-hover:rotate-180" />
                        ) : (
                            <Moon className="h-4 w-4 transition-transform duration-200 group-hover:-rotate-12" />
                        )}
                        Toggle theme
                    </span>

                    <span className="text-muted-foreground text-xs">
                        {theme === 'dark' ? 'Dark' : 'Light'}
                    </span>
                </DropdownMenuItem>

                <DropdownMenuSeparator />

                <DropdownMenuItem onClick={() => setTheme('system')}>
                    Use system theme
                </DropdownMenuItem>
            </DropdownMenuContent>
        </DropdownMenu>
    );
}
