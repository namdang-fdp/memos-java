'use client';

import { useState } from 'react';
import {
    Hash,
    Volume2,
    ChevronDown,
    Plus,
    Settings,
    Users,
    Search,
    Mic,
    Headphones,
    Cog,
} from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { cn } from '@/lib/utils';
import { Badge } from '@/components/ui/badge';

const channels = [
    { id: '1', name: 'general', type: 'text', badge: null },
    { id: '2', name: 'announcements', type: 'text', badge: 'NEW' },
    { id: '3', name: 'development', type: 'text', badge: null },
    { id: '4', name: 'design', type: 'text', badge: null },
];

const voiceChannels = [
    { id: 'v1', name: 'Team Standup', type: 'voice', users: 3 },
    { id: 'v2', name: 'Casual Chat', type: 'voice', users: 0 },
];

const directMessages = [
    {
        id: 'dm1',
        name: 'Sarah Chen',
        status: 'online',
        avatar: 'SC',
        unread: 2,
    },
    {
        id: 'dm2',
        name: 'Marcus Rodriguez',
        status: 'idle',
        avatar: 'MR',
        unread: 0,
    },
    { id: 'dm3', name: 'Emily Watson', status: 'dnd', avatar: 'EW', unread: 0 },
];

export function ChannelSidebar() {
    const [activeChannel, setActiveChannel] = useState('1');
    const [searchQuery, setSearchQuery] = useState('');

    return (
        <div className="bg-sidebar flex h-full w-60 flex-col">
            {/* Server Header */}
            <div className="border-sidebar-border flex h-12 items-center justify-between border-b px-4">
                <button className="group smooth-transition hover:text-sidebar-foreground flex flex-1 items-center gap-2">
                    <span className="text-sidebar-foreground text-base font-semibold">
                        Project Alpha
                    </span>
                    <ChevronDown className="text-muted-foreground smooth-transition group-hover:text-sidebar-foreground h-4 w-4" />
                </button>
            </div>

            {/* Search Bar */}
            <div className="p-2">
                <div className="relative">
                    <Search className="text-muted-foreground absolute top-1/2 left-3 h-4 w-4 -translate-y-1/2" />
                    <Input
                        placeholder="Search channels..."
                        value={searchQuery}
                        onChange={(e) => setSearchQuery(e.target.value)}
                        className="bg-secondary text-foreground placeholder:text-muted-foreground focus-visible:ring-ring h-8 border-none pl-9 text-sm focus-visible:ring-1"
                    />
                </div>
            </div>

            {/* Channel List */}
            <div className="flex-1 space-y-1 overflow-y-auto px-2">
                {/* Text Channels */}
                <div>
                    <button className="group text-muted-foreground smooth-transition hover:text-foreground flex w-full items-center gap-1 px-2 py-1.5 text-xs font-semibold tracking-wide uppercase">
                        <ChevronDown className="h-3 w-3" />
                        Text Channels
                    </button>
                    <div className="space-y-0.5">
                        {channels.map((channel) => (
                            <button
                                key={channel.id}
                                onClick={() => setActiveChannel(channel.id)}
                                className={cn(
                                    'group smooth-transition flex w-full items-center gap-2 rounded-md px-2 py-1.5 text-sm',
                                    activeChannel === channel.id
                                        ? 'bg-sidebar-accent text-sidebar-accent-foreground'
                                        : 'text-muted-foreground hover:bg-sidebar-accent hover:text-sidebar-accent-foreground',
                                )}
                            >
                                <Hash className="h-4 w-4 shrink-0" />
                                <span className="flex-1 truncate text-left">
                                    {channel.name}
                                </span>
                                {channel.badge && (
                                    <Badge className="bg-destructive text-destructive-foreground h-4 px-1.5 text-[10px] font-semibold">
                                        {channel.badge}
                                    </Badge>
                                )}
                                <Settings className="smooth-transition h-3.5 w-3.5 shrink-0 opacity-0 group-hover:opacity-100" />
                            </button>
                        ))}
                    </div>

                    <button className="group text-muted-foreground smooth-transition hover:bg-sidebar-accent hover:text-sidebar-accent-foreground flex w-full items-center gap-2 rounded-md px-2 py-1.5 text-sm">
                        <Plus className="h-4 w-4 shrink-0" />
                        <span>Add Channel</span>
                    </button>
                </div>

                {/* Voice Channels */}
                <div className="pt-2">
                    <button className="group text-muted-foreground smooth-transition hover:text-foreground flex w-full items-center gap-1 px-2 py-1.5 text-xs font-semibold tracking-wide uppercase">
                        <ChevronDown className="h-3 w-3" />
                        Voice Channels
                    </button>
                    <div className="space-y-0.5">
                        {voiceChannels.map((channel) => (
                            <button
                                key={channel.id}
                                className="group text-muted-foreground smooth-transition hover:bg-sidebar-accent hover:text-sidebar-accent-foreground flex w-full items-center gap-2 rounded-md px-2 py-1.5 text-sm"
                            >
                                <Volume2 className="h-4 w-4 shrink-0" />
                                <span className="flex-1 truncate text-left">
                                    {channel.name}
                                </span>
                                {channel.users > 0 && (
                                    <div className="flex items-center gap-1 text-xs">
                                        <Users className="h-3 w-3" />
                                        <span>{channel.users}</span>
                                    </div>
                                )}
                            </button>
                        ))}
                    </div>
                </div>

                {/* Direct Messages */}
                <div className="pt-2">
                    <button className="group text-muted-foreground smooth-transition hover:text-foreground flex w-full items-center gap-1 px-2 py-1.5 text-xs font-semibold tracking-wide uppercase">
                        <ChevronDown className="h-3 w-3" />
                        Direct Messages
                    </button>
                    <div className="space-y-0.5">
                        {directMessages.map((dm) => (
                            <button
                                key={dm.id}
                                className="group text-muted-foreground smooth-transition hover:bg-sidebar-accent hover:text-sidebar-accent-foreground flex w-full items-center gap-2 rounded-md px-2 py-1.5 text-sm"
                            >
                                <div className="relative">
                                    <div className="bg-primary text-primary-foreground flex h-8 w-8 items-center justify-center rounded-full text-xs font-semibold">
                                        {dm.avatar}
                                    </div>
                                    <div
                                        className={cn(
                                            'border-sidebar absolute right-0 bottom-0 h-2.5 w-2.5 rounded-full border-2',
                                            dm.status === 'online' &&
                                                'bg-green-500',
                                            dm.status === 'idle' &&
                                                'bg-yellow-500',
                                            dm.status === 'dnd' && 'bg-red-500',
                                        )}
                                    />
                                </div>
                                <span className="flex-1 truncate text-left">
                                    {dm.name}
                                </span>
                                {dm.unread > 0 && (
                                    <div className="bg-destructive text-destructive-foreground flex h-5 w-5 items-center justify-center rounded-full text-[11px] font-bold">
                                        {dm.unread}
                                    </div>
                                )}
                            </button>
                        ))}
                    </div>
                </div>
            </div>

            {/* User Panel */}
            <div className="border-sidebar-border bg-secondary flex items-center gap-2 border-t p-2">
                <div className="relative">
                    <div className="bg-primary text-primary-foreground flex h-8 w-8 items-center justify-center rounded-full text-xs font-semibold">
                        JD
                    </div>
                    <div className="border-secondary absolute right-0 bottom-0 h-3 w-3 rounded-full border-2 bg-green-500" />
                </div>
                <div className="flex-1 overflow-hidden">
                    <div className="text-sidebar-foreground truncate text-sm font-semibold">
                        John Doe
                    </div>
                    <div className="text-muted-foreground truncate text-xs">
                        Online
                    </div>
                </div>
                <div className="flex items-center gap-1">
                    <Button
                        size="icon"
                        className="text-muted-foreground hover:bg-sidebar-accent hover:text-sidebar-accent-foreground h-7 w-7 rounded-md bg-transparent"
                    >
                        <Mic className="h-4 w-4" />
                    </Button>
                    <Button
                        size="icon"
                        className="text-muted-foreground hover:bg-sidebar-accent hover:text-sidebar-accent-foreground h-7 w-7 rounded-md bg-transparent"
                    >
                        <Headphones className="h-4 w-4" />
                    </Button>
                    <Button
                        size="icon"
                        className="text-muted-foreground hover:bg-sidebar-accent hover:text-sidebar-accent-foreground h-7 w-7 rounded-md bg-transparent"
                    >
                        <Cog className="h-4 w-4" />
                    </Button>
                </div>
            </div>
        </div>
    );
}
