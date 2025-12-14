'use client';
import { ProjectSidebar } from './project-sidebar';
import { ProjectChannelSidebar } from './project-channel-sidebar';
import { KanbanBoard } from '@/components/kanban-board';

export default function HomePage() {
    return (
        <div className="bg-background flex h-screen overflow-hidden">
            <ProjectSidebar />

            <ProjectChannelSidebar />

            <KanbanBoard />
        </div>
    );
}
