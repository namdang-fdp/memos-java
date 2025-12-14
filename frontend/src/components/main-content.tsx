'use client';

import type React from 'react';

import {
    Hash,
    Pin,
    Users,
    Search,
    Inbox,
    HelpCircle,
    Sparkles,
    Send,
} from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';

export function MainContent() {
    return (
        <div className="bg-sidebar-tertiary flex flex-1 flex-col">
            {/* Top Bar */}
            <div className="border-border-subtle flex h-12 items-center justify-between border-b px-4">
                <div className="flex items-center gap-2">
                    <Hash className="text-text-muted h-5 w-5" />
                    <h2 className="text-text-primary text-base font-semibold">
                        general
                    </h2>
                    <div className="bg-border-subtle h-4 w-px" />
                    <p className="text-text-muted text-sm">
                        Team collaboration and updates
                    </p>
                </div>

                <div className="flex items-center gap-2">
                    <Button
                        size="icon"
                        className="text-text-muted hover:bg-hover-overlay hover:text-text-primary h-8 w-8 rounded-md bg-transparent"
                    >
                        <Pin className="h-4 w-4" />
                    </Button>
                    <Button
                        size="icon"
                        className="text-text-muted hover:bg-hover-overlay hover:text-text-primary h-8 w-8 rounded-md bg-transparent"
                    >
                        <Users className="h-4 w-4" />
                    </Button>
                    <div className="bg-sidebar-primary ml-2 flex items-center gap-2 rounded-md px-3 py-1.5">
                        <Search className="text-text-muted h-4 w-4" />
                        <span className="text-text-muted text-sm">Search</span>
                    </div>
                    <Button
                        size="icon"
                        className="text-text-muted hover:bg-hover-overlay hover:text-text-primary h-8 w-8 rounded-md bg-transparent"
                    >
                        <Inbox className="h-4 w-4" />
                    </Button>
                    <Button
                        size="icon"
                        className="text-text-muted hover:bg-hover-overlay hover:text-text-primary h-8 w-8 rounded-md bg-transparent"
                    >
                        <HelpCircle className="h-4 w-4" />
                    </Button>
                </div>
            </div>

            {/* Messages Area */}
            <div className="flex-1 overflow-y-auto p-4">
                <div className="flex flex-col items-center justify-center py-16">
                    <div className="bg-sidebar-secondary mb-6 flex h-16 w-16 items-center justify-center rounded-full">
                        <Hash className="text-text-muted h-8 w-8" />
                    </div>
                    <h3 className="text-text-primary mb-2 text-2xl font-bold">
                        Welcome to #general
                    </h3>
                    <p className="text-text-secondary max-w-md text-center leading-relaxed">
                        This is the beginning of the #general channel. Share
                        updates, collaborate with your team, and stay connected!
                    </p>

                    {/* Sample Welcome Card with Glass Effect */}
                    <div className="glass-effect mt-8 max-w-2xl space-y-4 rounded-2xl p-6">
                        <div className="flex items-start gap-3">
                            <div className="from-accent-blue to-accent-hover flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-linear-to-br text-sm font-semibold text-white shadow-lg">
                                AI
                            </div>
                            <div className="flex-1">
                                <div className="mb-1 flex items-center gap-2">
                                    <span className="text-text-primary font-semibold">
                                        AI Assistant
                                    </span>
                                    <Badge className="border-accent-blue/20 bg-accent-blue/10 text-accent-blue h-4 border px-1.5 text-[10px] font-semibold">
                                        BOT
                                    </Badge>
                                    <span className="text-text-muted text-xs">
                                        just now
                                    </span>
                                </div>
                                <p className="text-text-secondary text-sm leading-relaxed">
                                    <Sparkles className="text-accent-blue mr-1 inline h-4 w-4" />
                                    Welcome to your new workspace! I am here to
                                    help you get started. Feel free to explore
                                    channels, invite team members, and customize
                                    your experience.
                                </p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            {/* Message Input */}
            <div className="border-border-subtle border-t p-4">
                <div className="bg-sidebar-primary relative rounded-lg">
                    <Input
                        placeholder="Message #general"
                        className="text-text-primary placeholder:text-text-muted h-11 border-none bg-transparent pr-12 focus-visible:ring-0"
                    />
                    <Button
                        size="icon"
                        className="bg-accent-blue hover:bg-accent-hover absolute top-1/2 right-2 h-7 w-7 -translate-y-1/2 rounded-md text-white"
                    >
                        <Send className="h-4 w-4" />
                    </Button>
                </div>
            </div>
        </div>
    );
}

function Badge({
    children,
    className,
}: {
    children: React.ReactNode;
    className?: string;
}) {
    return <span className={className}>{children}</span>;
}
