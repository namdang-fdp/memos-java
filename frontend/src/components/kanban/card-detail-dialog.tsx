'use client';

import { useState } from 'react';
import {
    Dialog,
    DialogContent,
    DialogHeader,
    DialogTitle,
} from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Textarea } from '@/components/ui/textarea';
import { Avatar, AvatarFallback } from '@/components/ui/avatar';
import { Separator } from '@/components/ui/separator';
import {
    CheckCircle2,
    Circle,
    Calendar,
    User,
    AlertCircle,
    MessageSquare,
    Code2,
} from 'lucide-react';
import { cn } from '@/lib/utils';
import type { Card, CardComment, CardPriority } from '@/lib/service/feature';
import { format } from 'date-fns';

type CardDetailDialogProps = {
    card: Card | null;
    open: boolean;
    onOpenChange: (open: boolean) => void;
    onUpdateCard?: (cardId: string, updates: Partial<Card>) => void;
};

const priorityConfig: Record<
    CardPriority,
    { color: string; icon: typeof AlertCircle }
> = {
    urgent: {
        color: 'text-red-600 bg-red-50 dark:bg-red-950/30',
        icon: AlertCircle,
    },
    high: {
        color: 'text-orange-600 bg-orange-50 dark:bg-orange-950/30',
        icon: AlertCircle,
    },
    medium: {
        color: 'text-yellow-600 bg-yellow-50 dark:bg-yellow-950/30',
        icon: AlertCircle,
    },
    low: {
        color: 'text-blue-600 bg-blue-50 dark:bg-blue-950/30',
        icon: AlertCircle,
    },
};

export function CardDetailDialog({
    card,
    open,
    onOpenChange,
    onUpdateCard,
}: CardDetailDialogProps) {
    const [newComment, setNewComment] = useState('');

    if (!card) return null;

    const handleToggleComplete = () => {
        onUpdateCard?.(card.id, { completed: !card.completed });
    };

    const handleAddComment = () => {
        if (!newComment.trim()) return;

        const comment: CardComment = {
            id: Date.now().toString(),
            author: 'You',
            content: newComment,
            createdAt: new Date(),
        };

        onUpdateCard?.(card.id, {
            comments: [...(card.comments || []), comment],
        });
        setNewComment('');
    };

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent className="max-h-[90vh] max-w-3xl overflow-y-auto">
                <DialogHeader>
                    <div className="flex items-start gap-3">
                        <button
                            onClick={handleToggleComplete}
                            className="mt-1 transition-colors"
                        >
                            {card.completed ? (
                                <CheckCircle2 className="h-5 w-5 text-green-600" />
                            ) : (
                                <Circle className="text-muted-foreground hover:text-foreground h-5 w-5" />
                            )}
                        </button>
                        <DialogTitle
                            className={cn(
                                'flex-1 text-xl',
                                card.completed &&
                                    'text-muted-foreground line-through',
                            )}
                        >
                            {card.title}
                        </DialogTitle>
                    </div>
                </DialogHeader>

                <div className="space-y-6">
                    {/* Priority, Deadline, Assigned */}
                    <div className="flex flex-wrap gap-4">
                        {card.priority && (
                            <div className="flex items-center gap-2">
                                <AlertCircle className="text-muted-foreground h-4 w-4" />
                                <Badge
                                    className={
                                        priorityConfig[card.priority].color
                                    }
                                >
                                    {card.priority.toUpperCase()}
                                </Badge>
                            </div>
                        )}

                        {card.deadline && (
                            <div className="flex items-center gap-2">
                                <Calendar className="text-muted-foreground h-4 w-4" />
                                <span className="text-sm">
                                    {format(card.deadline, 'MMM d, yyyy')}
                                </span>
                            </div>
                        )}

                        {card.assignedTo && card.assignedTo.length > 0 && (
                            <div className="flex items-center gap-2">
                                <User className="text-muted-foreground h-4 w-4" />
                                <div className="flex -space-x-2">
                                    {card.assignedTo.map((person, idx) => (
                                        <Avatar
                                            key={idx}
                                            className="border-background h-6 w-6 border-2"
                                        >
                                            <AvatarFallback className="text-xs">
                                                {person
                                                    .slice(0, 2)
                                                    .toUpperCase()}
                                            </AvatarFallback>
                                        </Avatar>
                                    ))}
                                </div>
                            </div>
                        )}
                    </div>

                    {/* Description */}
                    {card.description && (
                        <div>
                            <h3 className="mb-2 text-sm font-semibold">
                                Description
                            </h3>
                            <p className="text-muted-foreground text-sm whitespace-pre-wrap">
                                {card.description}
                            </p>
                        </div>
                    )}

                    {/* Code Snippet */}
                    {card.snippet && (
                        <div>
                            <div className="mb-2 flex items-center gap-2">
                                <Code2 className="h-4 w-4" />
                                <h3 className="text-sm font-semibold">
                                    Code Snippet
                                </h3>
                            </div>
                            <pre className="bg-muted overflow-x-auto rounded-lg p-4 text-xs">
                                <code>{card.snippet}</code>
                            </pre>
                        </div>
                    )}

                    <Separator />

                    {/* Comments */}
                    <div>
                        <div className="mb-4 flex items-center gap-2">
                            <MessageSquare className="h-4 w-4" />
                            <h3 className="text-sm font-semibold">
                                Comments ({card.comments?.length || 0})
                            </h3>
                        </div>

                        <div className="mb-4 space-y-4">
                            {card.comments?.map((comment) => (
                                <div key={comment.id} className="flex gap-3">
                                    <Avatar className="h-8 w-8">
                                        <AvatarFallback className="text-xs">
                                            {comment.author
                                                .slice(0, 2)
                                                .toUpperCase()}
                                        </AvatarFallback>
                                    </Avatar>
                                    <div className="flex-1">
                                        <div className="mb-1 flex items-center gap-2">
                                            <span className="text-sm font-semibold">
                                                {comment.author}
                                            </span>
                                            <span className="text-muted-foreground text-xs">
                                                {format(
                                                    comment.createdAt,
                                                    'MMM d, h:mm a',
                                                )}
                                            </span>
                                        </div>
                                        <p className="text-muted-foreground text-sm">
                                            {comment.content}
                                        </p>
                                    </div>
                                </div>
                            ))}
                        </div>

                        {/* Add Comment */}
                        <div className="flex gap-2">
                            <Textarea
                                placeholder="Add a comment..."
                                value={newComment}
                                onChange={(e) => setNewComment(e.target.value)}
                                className="min-h-[80px]"
                            />
                        </div>
                        <Button
                            onClick={handleAddComment}
                            className="mt-2"
                            disabled={!newComment.trim()}
                        >
                            Add Comment
                        </Button>
                    </div>
                </div>
            </DialogContent>
        </Dialog>
    );
}
