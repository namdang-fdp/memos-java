'use client';
import { ServerSidebar } from '@/components/server-sidebar';
import { ChannelSidebar } from '@/components/channel-sidebar';
import { KanbanBoard } from '@/components/kanban-board';

export default function HomePage() {
    return (
        <div className="bg-background flex h-screen overflow-hidden">
            {/* Server/Project Sidebar */}
            <ServerSidebar />

            {/* Channel Sidebar */}
            <ChannelSidebar />

            <KanbanBoard />
        </div>
    );
}
