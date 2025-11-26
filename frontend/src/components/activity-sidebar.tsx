'use client';

import { useState } from 'react';
import { X, Users, Clock } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { cn } from '@/lib/utils';

const activities = [
    {
        id: '1',
        user: 'Sarah Chen',
        avatar: 'SC',
        status: 'online',
        activity: 'Reviewing design mockups',
        duration: '2h',
    },
    {
        id: '2',
        user: 'Marcus Rodriguez',
        avatar: 'MR',
        status: 'idle',
        activity: 'In a meeting',
        duration: '30m',
    },
];

export function ActivitySidebar() {
    const [isOpen, setIsOpen] = useState(true);

    if (!isOpen) {
        return (
            <Button
                onClick={() => setIsOpen(true)}
                className="bg-accent-blue hover:bg-accent-hover fixed top-4 right-4 h-10 w-10 rounded-full text-white shadow-lg"
            >
                <Users className="h-5 w-5" />
            </Button>
        );
    }

    return (
        <div className="border-border-subtle bg-sidebar-secondary flex h-full w-80 flex-col border-l">
            {/* Header */}
            <div className="border-border-subtle flex h-12 items-center justify-between border-b px-4">
                <h3 className="text-text-primary text-base font-semibold">
                    Activity
                </h3>
                <Button
                    onClick={() => setIsOpen(false)}
                    size="icon"
                    className="text-text-muted hover:bg-hover-overlay hover:text-text-primary h-6 w-6 rounded-md bg-transparent"
                >
                    <X className="h-4 w-4" />
                </Button>
            </div>

            {/* Activity List */}
            <div className="flex-1 space-y-4 overflow-y-auto p-4">
                <div>
                    <h4 className="text-text-muted mb-3 text-xs font-semibold tracking-wide uppercase">
                        Currently Active — {activities.length}
                    </h4>

                    <div className="space-y-3">
                        {activities.map((activity) => (
                            <div
                                key={activity.id}
                                className="group smooth-transition hover-lift"
                            >
                                <div className="glass-effect rounded-xl p-4">
                                    <div className="flex items-start gap-3">
                                        <div className="relative shrink-0">
                                            <div className="from-accent-blue to-accent-hover flex h-12 w-12 items-center justify-center rounded-full bg-linear-to-br text-sm font-semibold text-white shadow-lg">
                                                {activity.avatar}
                                            </div>
                                            <div
                                                className={cn(
                                                    'border-sidebar-secondary absolute right-0 bottom-0 h-4 w-4 rounded-full border-2 shadow-sm',
                                                    activity.status ===
                                                        'online' &&
                                                        'bg-status-online',
                                                    activity.status ===
                                                        'idle' &&
                                                        'bg-status-idle',
                                                )}
                                            />
                                        </div>
                                        <div className="flex-1 overflow-hidden">
                                            <h5 className="text-text-primary mb-0.5 truncate font-semibold">
                                                {activity.user}
                                            </h5>
                                            <p className="text-text-secondary mb-2 text-sm">
                                                {activity.activity}
                                            </p>
                                            <div className="text-text-muted flex items-center gap-1.5 text-xs">
                                                <Clock className="h-3 w-3" />
                                                <span>{activity.duration}</span>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        ))}
                    </div>
                </div>

                {/* Empty State with Animation */}
                <div className="mt-8 text-center">
                    <div className="mb-4 flex justify-center">
                        <div className="bg-sidebar-primary animate-pulse rounded-full p-8">
                            <Users className="text-text-muted h-12 w-12" />
                        </div>
                    </div>
                    <h5 className="text-text-primary mb-2 font-semibold">
                        No Recent Activity
                    </h5>
                    <p className="text-text-muted text-sm leading-relaxed text-pretty">
                        When team members are active, you will see their status
                        and activities here.
                    </p>
                </div>
            </div>
        </div>
    );
}
