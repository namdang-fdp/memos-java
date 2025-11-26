'use client';

import type React from 'react';
import { useState, useEffect } from 'react';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import {
    Calendar,
    MessageSquare,
    Paperclip,
    Link2,
    Plus,
    Filter,
    ArrowUpDown,
    Sparkles,
    Github,
    Sun,
    Moon,
    Share2,
    MoreHorizontal,
    Circle,
    CheckCircle2,
    AlertCircle,
} from 'lucide-react';
import { cn } from '@/lib/utils';

type Task = {
    id: string;
    title: string;
    description: string;
    tags: { label: string; color: string }[];
    date: string;
    comments: number;
    attachments: number;
    links: number;
    progress?: string;
    priority?: 'high' | 'medium' | 'low';
    avatars: string[];
};

type Column = {
    id: string;
    title: string;
    icon: React.ReactNode;
    color: string;
    tasks: Task[];
};

const columns: Column[] = [
    {
        id: 'backlog',
        title: 'Backlog',
        icon: <Circle className="h-4 w-4" />,
        color: 'text-muted-foreground',
        tasks: [
            {
                id: '1',
                title: 'Mobile app redesign',
                description:
                    'Complete redesign of mobile application for better UX',
                tags: [
                    {
                        label: 'Design',
                        color: 'bg-blue-500/10 text-blue-600 dark:text-blue-400',
                    },
                ],
                date: 'Feb 10',
                comments: 2,
                attachments: 5,
                links: 3,
                avatars: ['JD'],
            },
            {
                id: '2',
                title: 'API documentation update',
                description:
                    'Update API docs with latest endpoints and examples',
                tags: [
                    {
                        label: 'Product',
                        color: 'bg-pink-500/10 text-pink-600 dark:text-pink-400',
                    },
                ],
                date: 'Feb 15',
                comments: 8,
                attachments: 0,
                links: 0,
                avatars: ['SC', 'MR'],
            },
            {
                id: '3',
                title: 'Accessibility improvements',
                description:
                    'Enhance accessibility for screen readers and keyboard navigation',
                tags: [
                    {
                        label: 'Design',
                        color: 'bg-blue-500/10 text-blue-600 dark:text-blue-400',
                    },
                    {
                        label: 'New releases',
                        color: 'bg-orange-500/10 text-orange-600 dark:text-orange-400',
                    },
                ],
                date: 'Feb 20',
                comments: 1,
                attachments: 2,
                links: 5,
                avatars: ['EW'],
            },
        ],
    },
    {
        id: 'todo',
        title: 'Todo',
        icon: <Circle className="h-4 w-4" />,
        color: 'text-muted-foreground',
        tasks: [
            {
                id: '4',
                title: 'Design system update',
                description:
                    'Enhance design system for consistency and usability',
                tags: [
                    {
                        label: 'Design',
                        color: 'bg-blue-500/10 text-blue-600 dark:text-blue-400',
                    },
                    {
                        label: 'New releases',
                        color: 'bg-orange-500/10 text-orange-600 dark:text-orange-400',
                    },
                ],
                date: 'Jan 25',
                comments: 4,
                attachments: 0,
                links: 0,
                progress: '1/4',
                priority: 'high',
                avatars: ['JD', 'SC'],
            },
            {
                id: '5',
                title: 'Retention rate by 23%',
                description:
                    'Improve retention through campaigns and feature updates',
                tags: [
                    {
                        label: 'Marketing',
                        color: 'bg-green-500/10 text-green-600 dark:text-green-400',
                    },
                    {
                        label: 'Product',
                        color: 'bg-pink-500/10 text-pink-600 dark:text-pink-400',
                    },
                ],
                date: 'Jan 25',
                comments: 4,
                attachments: 0,
                links: 12,
                progress: '3/4',
                avatars: ['MR', 'EW'],
            },
            {
                id: '6',
                title: 'Icon system',
                description:
                    'Develop scalable icons for cohesive platform visuals',
                tags: [
                    {
                        label: 'Design',
                        color: 'bg-blue-500/10 text-blue-600 dark:text-blue-400',
                    },
                ],
                date: 'Jan 25',
                comments: 4,
                attachments: 0,
                links: 0,
                progress: '1/4',
                priority: 'high',
                avatars: ['JD', 'EW'],
            },
            {
                id: '7',
                title: 'Task automation',
                description:
                    'Automate repetitive tasks to improve productivity',
                tags: [
                    {
                        label: 'Product',
                        color: 'bg-pink-500/10 text-pink-600 dark:text-pink-400',
                    },
                ],
                date: 'Jan 25',
                comments: 2,
                attachments: 0,
                links: 3,
                avatars: ['SC'],
            },
        ],
    },
    {
        id: 'progress',
        title: 'In Progress',
        icon: <Circle className="h-4 w-4 fill-yellow-500 text-yellow-500" />,
        color: 'text-yellow-600 dark:text-yellow-400',
        tasks: [
            {
                id: '8',
                title: 'Search features',
                description: 'Upgrade search for faster, accurate user results',
                tags: [
                    {
                        label: 'Product',
                        color: 'bg-pink-500/10 text-pink-600 dark:text-pink-400',
                    },
                ],
                date: 'Jan 25',
                comments: 0,
                attachments: 0,
                links: 12,
                priority: 'medium',
                avatars: ['MR'],
            },
            {
                id: '9',
                title: 'Checkout flow design',
                description:
                    'Optimize checkout process to improve conversion rates',
                tags: [
                    {
                        label: 'Design',
                        color: 'bg-blue-500/10 text-blue-600 dark:text-blue-400',
                    },
                ],
                date: 'Jan 25',
                comments: 0,
                attachments: 0,
                links: 12,
                progress: '2/4',
                avatars: ['JD'],
            },
        ],
    },
    {
        id: 'review',
        title: 'Technical Review',
        icon: <CheckCircle2 className="h-4 w-4 text-green-500" />,
        color: 'text-green-600 dark:text-green-400',
        tasks: [
            {
                id: '10',
                title: 'Payment gateway integration',
                description:
                    'Integrate Stripe payment system for subscriptions',
                tags: [
                    {
                        label: 'Product',
                        color: 'bg-pink-500/10 text-pink-600 dark:text-pink-400',
                    },
                ],
                date: 'Jan 20',
                comments: 8,
                attachments: 0,
                links: 5,
                progress: '3/4',
                priority: 'high',
                avatars: ['JD', 'MR'],
            },
            {
                id: '11',
                title: 'Security audit fixes',
                description:
                    'Implement fixes from recent security audit report',
                tags: [
                    {
                        label: 'Product',
                        color: 'bg-pink-500/10 text-pink-600 dark:text-pink-400',
                    },
                ],
                date: 'Jan 22',
                comments: 3,
                attachments: 7,
                links: 2,
                progress: '2/3',
                priority: 'high',
                avatars: ['EW'],
            },
            {
                id: '12',
                title: 'Code review optimizations',
                description:
                    'Review and optimize codebase for better performance',
                tags: [
                    {
                        label: 'Product',
                        color: 'bg-pink-500/10 text-pink-600 dark:text-pink-400',
                    },
                    {
                        label: 'New releases',
                        color: 'bg-orange-500/10 text-orange-600 dark:text-orange-400',
                    },
                ],
                date: 'Jan 21',
                comments: 10,
                attachments: 0,
                links: 7,
                progress: '1/2',
                priority: 'high',
                avatars: ['SC', 'JD'],
            },
        ],
    },
];

export function KanbanBoard() {
    const [selectedDate, setSelectedDate] = useState('Sep 7, 2024');
    const [theme, setTheme] = useState<'light' | 'dark'>('dark');

    useEffect(() => {
        const root = document.documentElement;
        const savedTheme = localStorage.getItem('theme') as
            | 'light'
            | 'dark'
            | null;

        if (savedTheme) {
            setTheme(savedTheme);
            if (savedTheme === 'light') {
                root.classList.add('light');
            } else {
                root.classList.remove('light');
            }
        }
    }, []);

    const toggleTheme = () => {
        const root = document.documentElement;
        const newTheme = theme === 'dark' ? 'light' : 'dark';

        setTheme(newTheme);
        localStorage.setItem('theme', newTheme);

        if (newTheme === 'light') {
            root.classList.add('light');
        } else {
            root.classList.remove('light');
        }
    };

    return (
        <div className="bg-background flex h-full flex-1 flex-col overflow-hidden">
            {/* Top Bar */}
            <div className="border-border flex h-14 items-center justify-between border-b px-6">
                <div className="flex items-center gap-4">
                    <h1 className="text-foreground text-lg font-semibold">
                        Task
                    </h1>
                </div>

                <div className="flex items-center gap-3">
                    <Button
                        variant="ghost"
                        size="sm"
                        className="text-muted-foreground hover:text-foreground h-8 gap-2 text-sm font-medium"
                    >
                        <Github className="h-4 w-4" />
                        GitHub
                    </Button>

                    <Button
                        variant="ghost"
                        size="icon"
                        className="text-muted-foreground hover:text-foreground h-8 w-8"
                        onClick={toggleTheme}
                    >
                        {theme === 'dark' ? (
                            <Sun className="h-4 w-4" />
                        ) : (
                            <Moon className="h-4 w-4" />
                        )}
                    </Button>

                    <div className="text-muted-foreground text-sm">
                        Last update 3 days ago
                    </div>

                    <div className="flex -space-x-2">
                        <div className="ring-background h-6 w-6 rounded-full bg-green-500 ring-2" />
                        <div className="ring-background h-6 w-6 rounded-full bg-red-500 ring-2" />
                    </div>

                    <Button size="sm" className="h-8 gap-2 text-sm font-medium">
                        <Share2 className="h-3.5 w-3.5" />
                        Share
                    </Button>
                </div>
            </div>

            {/* Toolbar */}
            <div className="border-border flex h-14 items-center justify-between border-b px-6">
                <div className="flex items-center gap-2">
                    <Button
                        variant="outline"
                        size="sm"
                        className="h-8 gap-2 bg-transparent text-sm"
                    >
                        <Filter className="h-3.5 w-3.5" />
                        Filter
                    </Button>

                    <Button
                        variant="outline"
                        size="sm"
                        className="h-8 gap-2 bg-transparent text-sm"
                    >
                        <ArrowUpDown className="h-3.5 w-3.5" />
                        Sort
                    </Button>

                    <Button
                        variant="outline"
                        size="sm"
                        className="h-8 gap-2 bg-transparent text-sm"
                    >
                        <Sparkles className="h-3.5 w-3.5" />
                        Automate
                        <Badge className="bg-primary/10 text-primary ml-1 h-4 px-1.5 text-[10px] font-semibold">
                            Pro
                        </Badge>
                    </Button>
                </div>

                <div className="flex items-center gap-2">
                    <Button
                        variant="outline"
                        size="sm"
                        className="h-8 gap-2 bg-transparent text-sm"
                    >
                        <Calendar className="h-3.5 w-3.5" />
                        {selectedDate}
                    </Button>

                    <Button
                        variant="outline"
                        size="sm"
                        className="h-8 gap-2 bg-transparent text-sm"
                    >
                        Import / Export
                    </Button>

                    <Button size="sm" className="h-8 gap-2 text-sm">
                        <Plus className="h-3.5 w-3.5" />
                        Request task
                    </Button>
                </div>
            </div>

            {/* Kanban Columns */}
            <div className="flex flex-1 gap-4 overflow-x-auto p-6">
                {columns.map((column) => (
                    <div
                        key={column.id}
                        className="flex w-[320px] shrink-0 flex-col"
                    >
                        {/* Column Header */}
                        <div className="mb-3 flex items-center justify-between">
                            <div className="flex items-center gap-2">
                                <div
                                    className={cn(
                                        'smooth-transition',
                                        column.color,
                                    )}
                                >
                                    {column.icon}
                                </div>
                                <h2 className="text-foreground text-sm font-semibold">
                                    {column.title}
                                </h2>
                                <span className="text-muted-foreground text-xs">
                                    {column.tasks.length}
                                </span>
                            </div>
                            <div className="flex items-center gap-1">
                                <Button
                                    variant="ghost"
                                    size="icon"
                                    className="h-6 w-6"
                                >
                                    <Plus className="h-3.5 w-3.5" />
                                </Button>
                                <Button
                                    variant="ghost"
                                    size="icon"
                                    className="h-6 w-6"
                                >
                                    <MoreHorizontal className="h-3.5 w-3.5" />
                                </Button>
                            </div>
                        </div>

                        {/* Tasks */}
                        <div className="space-y-3 overflow-y-auto">
                            {column.tasks.map((task) => (
                                <div
                                    key={task.id}
                                    className="group border-border bg-card smooth-transition rounded-lg border p-4 hover:shadow-lg hover:shadow-black/5 dark:hover:shadow-black/20"
                                >
                                    {/* Task Header */}
                                    <div className="mb-2 flex items-start justify-between gap-2">
                                        <div className="flex items-start gap-2">
                                            <Circle className="text-muted-foreground mt-0.5 h-4 w-4 shrink-0" />
                                            <h3 className="text-foreground text-sm font-medium">
                                                {task.title}
                                            </h3>
                                        </div>
                                        {task.priority === 'high' && (
                                            <AlertCircle className="h-4 w-4 shrink-0 text-red-500" />
                                        )}
                                    </div>

                                    {/* Task Description */}
                                    <p className="text-muted-foreground mb-3 text-xs">
                                        {task.description}
                                    </p>

                                    {/* Tags */}
                                    <div className="mb-3 flex flex-wrap gap-1.5">
                                        {task.tags.map((tag, idx) => (
                                            <Badge
                                                key={idx}
                                                className={cn(
                                                    'h-5 px-2 text-[11px] font-medium',
                                                    tag.color,
                                                )}
                                            >
                                                {tag.label}
                                            </Badge>
                                        ))}
                                    </div>

                                    {/* Task Footer */}
                                    <div className="text-muted-foreground flex items-center justify-between text-xs">
                                        <div className="flex items-center gap-3">
                                            <div className="flex items-center gap-1">
                                                <Calendar className="h-3.5 w-3.5" />
                                                <span>{task.date}</span>
                                            </div>
                                            {task.comments > 0 && (
                                                <div className="flex items-center gap-1">
                                                    <MessageSquare className="h-3.5 w-3.5" />
                                                    <span>{task.comments}</span>
                                                </div>
                                            )}
                                            {task.attachments > 0 && (
                                                <div className="flex items-center gap-1">
                                                    <Paperclip className="h-3.5 w-3.5" />
                                                    <span>
                                                        {task.attachments}
                                                    </span>
                                                </div>
                                            )}
                                            {task.links > 0 && (
                                                <div className="flex items-center gap-1">
                                                    <Link2 className="h-3.5 w-3.5" />
                                                    <span>{task.links}</span>
                                                </div>
                                            )}
                                            {task.progress && (
                                                <div className="flex items-center gap-1">
                                                    <Circle className="h-3.5 w-3.5" />
                                                    <span>{task.progress}</span>
                                                </div>
                                            )}
                                        </div>

                                        {/* Avatars */}
                                        <div className="flex -space-x-1.5">
                                            {task.avatars.map((avatar, idx) => (
                                                <div
                                                    key={idx}
                                                    className="bg-primary text-primary-foreground ring-background flex h-6 w-6 items-center justify-center rounded-full text-[10px] font-semibold ring-2"
                                                >
                                                    {avatar}
                                                </div>
                                            ))}
                                        </div>
                                    </div>
                                </div>
                            ))}

                            {/* Add Task Button */}
                            <Button
                                variant="ghost"
                                className="text-muted-foreground hover:text-foreground w-full justify-start gap-2 text-sm"
                            >
                                <Plus className="h-4 w-4" />
                                Add task
                            </Button>
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
}
