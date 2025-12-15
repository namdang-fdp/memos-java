'use client';

import { useState, useRef, type FormEvent, type KeyboardEvent } from 'react';
import { flushSync } from 'react-dom';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Badge } from '@/components/ui/badge';
import { Avatar, AvatarFallback } from '@/components/ui/avatar';
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuLabel,
    DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
} from '@/components/ui/dialog';
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from '@/components/ui/select';
import {
    Tooltip,
    TooltipContent,
    TooltipProvider,
    TooltipTrigger,
} from '@/components/ui/tooltip';
import { Skeleton } from '@/components/ui/skeleton';
import { cn } from '@/lib/utils';
import { format } from 'date-fns';
import {
    AlertCircle,
    Calendar,
    CheckCircle2,
    Circle,
    MessageSquare,
    MoreHorizontalIcon,
    PenIcon,
    PlusIcon,
    Trash2Icon,
} from 'lucide-react';
import { KanbanBoardProvider } from '@/components/kanban/kanban-provider';
import { CardDetailDialog } from '@/components/kanban/card-detail-dialog';
import { useDndEvents } from '@/components/kanban/kanban-provider';
import { useJsLoaded } from '@/hooks/use-js-loaded';
import { createId } from '@paralleldrive/cuid2';
import type {
    Column,
    Card,
    KanbanBoardDropDirection,
    CardPriority,
} from '@/lib/service/feature';

const INITIAL_COLUMNS: Column[] = [
    {
        id: 'eowdjiak9f9jr27po347jr47',
        title: 'Backlog',
        color: 'gray',
        items: [
            {
                id: '1',
                title: 'Add animation to drag and drop',
                description:
                    'Implement smooth animations when dragging cards between columns',
                completed: false,
                priority: 'high',
                deadline: new Date('2024-02-15'),
                assignedTo: ['John'],
                tags: ['Feature', 'UI'],
            },
            {
                id: '2',
                title: 'Add card details view',
                description:
                    'Create a modal to view full card details including description, comments, and code snippets',
                completed: false,
                priority: 'high',
                deadline: new Date('2024-02-20'),
                assignedTo: ['Sarah'],
            },
            {
                id: '3',
                title: 'Implement task completion',
                description: 'Add checkbox to mark tasks as done',
                snippet: `const handleComplete = (id: string) => {\n  setTasks(tasks.map(t => \n    t.id === id ? {...t, completed: !t.completed} : t\n  ));\n};`,
                completed: false,
                priority: 'medium',
                assignedTo: ['Mike'],
                comments: [
                    {
                        id: 'c1',
                        author: 'Alice',
                        content: 'Should we add a strikethrough effect?',
                        createdAt: new Date('2024-01-15'),
                    },
                ],
            },
        ],
    },
    {
        id: 'ad1wx5djclsilpu8sjmp9g70',
        title: 'To Do',
        color: 'blue',
        items: [
            {
                id: '4',
                title: 'Enhance UI/UX design',
                description:
                    'Improve the overall design to match Trello, Linear, and Jira standards',
                completed: false,
                priority: 'high',
                deadline: new Date('2024-02-25'),
                assignedTo: ['Emma', 'John'],
                tags: ['Design'],
            },
        ],
    },
    {
        id: 'zm3vyxyo0x47tl60340w8jrl',
        title: 'In Progress',
        color: 'yellow',
        items: [
            {
                id: '5',
                title: 'Display task metadata',
                description:
                    'Show deadline, assignee, priority, and other metadata on cards',
                completed: false,
                priority: 'urgent',
                deadline: new Date('2024-02-10'),
                assignedTo: ['Sarah', 'Mike'],
                comments: [
                    {
                        id: 'c2',
                        author: 'John',
                        content: 'We should use badges for priority levels',
                        createdAt: new Date('2024-01-20'),
                    },
                    {
                        id: 'c3',
                        author: 'Emma',
                        content: 'Agreed! Also add avatar chips for assignees',
                        createdAt: new Date('2024-01-21'),
                    },
                ],
                tags: ['Feature'],
            },
        ],
    },
    {
        id: 'rzaksqoyfvgjbw466puqu9uk',
        title: 'Review',
        color: 'purple',
        items: [],
    },
    {
        id: 'w27comaw16gy2jxphpmt9xxv',
        title: 'Done',
        color: 'green',
        items: [
            {
                id: '6',
                title: 'Setup project structure',
                description:
                    'Initialize the Kanban board with basic components',
                completed: true,
                priority: 'high',
                assignedTo: ['John'],
            },
        ],
    },
];

const priorityConfig: Record<
    CardPriority,
    { color: string; icon: typeof AlertCircle }
> = {
    urgent: {
        color: 'bg-red-500/10 text-red-600 dark:bg-red-500/20 dark:text-red-400 border-red-500/30',
        icon: AlertCircle,
    },
    high: {
        color: 'bg-orange-500/10 text-orange-600 dark:bg-orange-500/20 dark:text-orange-400 border-orange-500/30',
        icon: AlertCircle,
    },
    medium: {
        color: 'bg-amber-500/10 text-amber-600 dark:bg-amber-500/20 dark:text-amber-400 border-amber-500/30',
        icon: AlertCircle,
    },
    low: {
        color: 'bg-blue-500/10 text-blue-600 dark:bg-blue-500/20 dark:text-blue-400 border-blue-500/30',
        icon: AlertCircle,
    },
};

export default function KanbanBoardPage() {
    return (
        <div className="bg-background flex h-screen overflow-hidden">
            <div className="flex flex-1 flex-col overflow-hidden">
                <main className="custom-scrollbar bg-muted/30 flex-1 overflow-auto p-6">
                    <TooltipProvider>
                        <KanbanBoardProvider>
                            <MyKanbanBoard />
                        </KanbanBoardProvider>
                    </TooltipProvider>
                </main>
            </div>
        </div>
    );
}

function MyKanbanBoard() {
    const [columns, setColumns] = useState<Column[]>(INITIAL_COLUMNS);
    const [selectedCard, setSelectedCard] = useState<Card | null>(null);
    const [detailDialogOpen, setDetailDialogOpen] = useState(false);
    const scrollContainerRef = useRef<HTMLDivElement>(null);

    const [draggingCardId, setDraggingCardId] = useState<string | null>(null);

    const handleAddColumn = (title?: string) => {
        if (title) {
            flushSync(() => {
                setColumns((prev) => [
                    ...prev,
                    {
                        id: createId(),
                        title,
                        color: 'primary',
                        items: [],
                    },
                ]);
            });
        }

        scrollContainerRef.current?.scrollTo({
            left: scrollContainerRef.current.scrollWidth,
            behavior: 'smooth',
        });
    };

    const handleDeleteColumn = (columnId: string) => {
        setColumns((prev) => prev.filter((col) => col.id !== columnId));
    };

    const handleUpdateColumnTitle = (columnId: string, title: string) => {
        setColumns((prev) =>
            prev.map((col) => (col.id === columnId ? { ...col, title } : col)),
        );
    };

    const handleAddCard = (columnId: string, cardContent: string) => {
        setColumns((prev) =>
            prev.map((col) =>
                col.id === columnId
                    ? {
                          ...col,
                          items: [
                              ...col.items,
                              {
                                  id: createId(),
                                  title: cardContent,
                                  completed: false,
                              },
                          ],
                      }
                    : col,
            ),
        );
    };

    const handleDeleteCard = (cardId: string) => {
        setColumns((prev) =>
            prev.map((col) => ({
                ...col,
                items: col.items.filter((card) => card.id !== cardId),
            })),
        );
    };

    const handleMoveCardToColumn = (
        columnId: string,
        index: number,
        card: Card,
    ) => {
        setColumns((prev) =>
            prev.map((col) => {
                if (col.id === columnId) {
                    const updatedItems = col.items.filter(
                        (c) => c.id !== card.id,
                    );
                    return {
                        ...col,
                        items: [
                            ...updatedItems.slice(0, index),
                            card,
                            ...updatedItems.slice(index),
                        ],
                    };
                } else {
                    return {
                        ...col,
                        items: col.items.filter((c) => c.id !== card.id),
                    };
                }
            }),
        );
    };

    const handleUpdateCardTitle = (cardId: string, cardTitle: string) => {
        setColumns((prev) =>
            prev.map((col) => ({
                ...col,
                items: col.items.map((card) =>
                    card.id === cardId ? { ...card, title: cardTitle } : card,
                ),
            })),
        );
    };

    const handleUpdateCard = (cardId: string, updates: Partial<Card>) => {
        setColumns((prev) =>
            prev.map((col) => ({
                ...col,
                items: col.items.map((card) =>
                    card.id === cardId ? { ...card, ...updates } : card,
                ),
            })),
        );

        if (selectedCard?.id === cardId) {
            setSelectedCard((prev) => (prev ? { ...prev, ...updates } : null));
        }
    };

    const handleCardClick = (card: Card) => {
        setSelectedCard(card);
        setDetailDialogOpen(true);
    };

    const handleToggleComplete = (cardId: string) => {
        setColumns((prev) =>
            prev.map((col) => ({
                ...col,
                items: col.items.map((card) =>
                    card.id === cardId
                        ? { ...card, completed: !card.completed }
                        : card,
                ),
            })),
        );
    };

    const [activeCardId, setActiveCardId] = useState<string>('');
    const originalCardPositionRef = useRef<{
        columnId: string;
        cardIndex: number;
    } | null>(null);
    const { onDragStart, onDragEnd, onDragCancel, onDragOver } = useDndEvents();

    function getOverId(column: Column, cardIndex: number): string {
        if (cardIndex < column.items.length - 1) {
            return column.items[cardIndex + 1].id;
        }
        return column.id;
    }

    function findCardPosition(cardId: string): {
        columnIndex: number;
        cardIndex: number;
    } {
        for (const [columnIndex, column] of columns.entries()) {
            const cardIndex = column.items.findIndex((c) => c.id === cardId);
            if (cardIndex !== -1) {
                return { columnIndex, cardIndex };
            }
        }
        return { columnIndex: -1, cardIndex: -1 };
    }

    function moveActiveCard(
        cardId: string,
        direction: 'ArrowLeft' | 'ArrowRight' | 'ArrowUp' | 'ArrowDown',
    ) {
        const { columnIndex, cardIndex } = findCardPosition(cardId);
        if (columnIndex === -1 || cardIndex === -1) return;

        const card = columns[columnIndex].items[cardIndex];
        let newColumnIndex = columnIndex;
        let newCardIndex = cardIndex;

        switch (direction) {
            case 'ArrowUp':
                newCardIndex = Math.max(cardIndex - 1, 0);
                break;
            case 'ArrowDown':
                newCardIndex = Math.min(
                    cardIndex + 1,
                    columns[columnIndex].items.length - 1,
                );
                break;
            case 'ArrowLeft':
                newColumnIndex = Math.max(columnIndex - 1, 0);
                newCardIndex = Math.min(
                    newCardIndex,
                    columns[newColumnIndex].items.length,
                );
                break;
            case 'ArrowRight':
                newColumnIndex = Math.min(columnIndex + 1, columns.length - 1);
                newCardIndex = Math.min(
                    newCardIndex,
                    columns[newColumnIndex].items.length,
                );
                break;
        }

        flushSync(() => {
            handleMoveCardToColumn(
                columns[newColumnIndex].id,
                newCardIndex,
                card,
            );
        });

        const { columnIndex: updatedColumnIndex, cardIndex: updatedCardIndex } =
            findCardPosition(cardId);
        const overId = getOverId(columns[updatedColumnIndex], updatedCardIndex);
        onDragOver(cardId, overId);
    }

    function handleCardKeyDown(
        event: KeyboardEvent<HTMLDivElement>,
        cardId: string,
    ) {
        const { key } = event;

        if (activeCardId === '' && key === ' ') {
            event.preventDefault();
            setActiveCardId(cardId);
            setDraggingCardId(cardId);
            onDragStart(cardId);

            const { columnIndex, cardIndex } = findCardPosition(cardId);
            originalCardPositionRef.current =
                columnIndex !== -1 && cardIndex !== -1
                    ? { columnId: columns[columnIndex].id, cardIndex }
                    : null;
        } else if (activeCardId === cardId) {
            if (key === ' ' || key === 'Enter') {
                event.preventDefault();
                flushSync(() => {
                    setActiveCardId('');
                    setDraggingCardId(null);
                });

                const { columnIndex, cardIndex } = findCardPosition(cardId);
                if (columnIndex !== -1 && cardIndex !== -1) {
                    const overId = getOverId(columns[columnIndex], cardIndex);
                    onDragEnd(cardId, overId);
                } else {
                    onDragEnd(cardId);
                }
                originalCardPositionRef.current = null;
            } else if (key === 'Escape') {
                event.preventDefault();
                if (originalCardPositionRef.current) {
                    const { columnId, cardIndex } =
                        originalCardPositionRef.current;
                    const {
                        columnIndex: currentColumnIndex,
                        cardIndex: currentCardIndex,
                    } = findCardPosition(cardId);

                    if (
                        currentColumnIndex !== -1 &&
                        (columnId !== columns[currentColumnIndex].id ||
                            cardIndex !== currentCardIndex)
                    ) {
                        const card =
                            columns[currentColumnIndex].items[currentCardIndex];
                        flushSync(() => {
                            handleMoveCardToColumn(columnId, cardIndex, card);
                        });
                    }
                }

                onDragCancel(cardId);
                originalCardPositionRef.current = null;
                setActiveCardId('');
                setDraggingCardId(null);
            } else if (
                key === 'ArrowLeft' ||
                key === 'ArrowRight' ||
                key === 'ArrowUp' ||
                key === 'ArrowDown'
            ) {
                event.preventDefault();
                moveActiveCard(cardId, key);
            }
        }
    }

    function handleCardBlur() {
        setActiveCardId('');
        setDraggingCardId(null);
    }

    const jsLoaded = useJsLoaded();

    return (
        <>
            <div
                ref={scrollContainerRef}
                className="flex h-full gap-4 overflow-x-auto pb-4"
            >
                {columns.map((column) =>
                    jsLoaded ? (
                        <MyKanbanColumn
                            key={column.id}
                            column={column}
                            activeCardId={activeCardId}
                            draggingCardId={draggingCardId}
                            onAddCard={handleAddCard}
                            onDeleteCard={handleDeleteCard}
                            onDeleteColumn={handleDeleteColumn}
                            onUpdateColumnTitle={handleUpdateColumnTitle}
                            onMoveCardToColumn={handleMoveCardToColumn}
                            onUpdateCardTitle={handleUpdateCardTitle}
                            onCardKeyDown={handleCardKeyDown}
                            onCardBlur={handleCardBlur}
                            onCardClick={handleCardClick}
                            onToggleComplete={handleToggleComplete}
                        />
                    ) : (
                        <Skeleton
                            key={column.id}
                            className="h-full w-80 shrink-0"
                        />
                    ),
                )}

                {jsLoaded && <AddColumnButton onAddColumn={handleAddColumn} />}
            </div>

            <CardDetailDialog
                card={selectedCard}
                open={detailDialogOpen}
                onOpenChange={setDetailDialogOpen}
                onUpdateCard={handleUpdateCard}
            />
        </>
    );
}

function MyKanbanColumn({
    column,
    activeCardId,
    draggingCardId,
    onAddCard,
    onDeleteCard,
    onDeleteColumn,
    onUpdateColumnTitle,
    onMoveCardToColumn,
    onUpdateCardTitle,
    onCardKeyDown,
    onCardBlur,
    onCardClick,
    onToggleComplete,
}: {
    column: Column;
    activeCardId: string;
    draggingCardId: string | null;
    onAddCard: (columnId: string, content: string) => void;
    onDeleteCard: (cardId: string) => void;
    onDeleteColumn: (columnId: string) => void;
    onUpdateColumnTitle: (columnId: string, title: string) => void;
    onMoveCardToColumn: (columnId: string, index: number, card: Card) => void;
    onUpdateCardTitle: (cardId: string, title: string) => void;
    onCardKeyDown: (
        event: KeyboardEvent<HTMLDivElement>,
        cardId: string,
    ) => void;
    onCardBlur: () => void;
    onCardClick: (card: Card) => void;
    onToggleComplete: (cardId: string) => void;
}) {
    const [isEditingTitle, setIsEditingTitle] = useState(false);
    const [addCardDialogOpen, setAddCardDialogOpen] = useState(false);
    const [isDropTarget, setIsDropTarget] = useState(false);
    const { onDragEnd, onDragOver } = useDndEvents();

    const handleSubmitTitle = (event: FormEvent<HTMLFormElement>) => {
        event.preventDefault();
        const formData = new FormData(event.currentTarget);
        const title = formData.get('columnTitle') as string;
        onUpdateColumnTitle(column.id, title);
        setIsEditingTitle(false);
    };

    const handleDropOverColumn = (dataTransferData: string) => {
        const card = JSON.parse(dataTransferData) as Card;
        onMoveCardToColumn(column.id, 0, card);
    };

    const handleDropOverListItem = (cardId: string) => {
        return (
            dataTransferData: string,
            dropDirection: KanbanBoardDropDirection,
        ) => {
            const card = JSON.parse(dataTransferData) as Card;
            const cardIndex = column.items.findIndex((c) => c.id === cardId);
            const currentCardIndex = column.items.findIndex(
                (c) => c.id === card.id,
            );

            const baseIndex =
                dropDirection === 'top' ? cardIndex : cardIndex + 1;
            const targetIndex =
                currentCardIndex !== -1 && currentCardIndex < baseIndex
                    ? baseIndex - 1
                    : baseIndex;

            onMoveCardToColumn(column.id, targetIndex, card);
        };
    };

    return (
        <>
            <section
                className={cn(
                    'bg-muted/30 flex w-80 shrink-0 flex-col rounded-xl p-3',
                    'kanban-column-elevation',
                    'transition-all duration-300',
                    isDropTarget &&
                        'bg-primary/5 ring-primary/30 shadow-lg ring-2',
                )}
                onDragLeave={() => setIsDropTarget(false)}
                onDragOver={(event) => {
                    if (
                        event.dataTransfer.types.includes('kanban-board-card')
                    ) {
                        event.preventDefault();
                        setIsDropTarget(true);
                        onDragOver('', column.id);
                    }
                }}
                onDrop={(event) => {
                    const data =
                        event.dataTransfer.getData('kanban-board-card');
                    handleDropOverColumn(data);
                    onDragEnd(JSON.parse(data).id as string, column.id);
                    setIsDropTarget(false);
                }}
            >
                <div className="mb-3 flex items-center justify-between px-1">
                    {isEditingTitle ? (
                        <form onSubmit={handleSubmitTitle} className="flex-1">
                            <Input
                                name="columnTitle"
                                defaultValue={column.title}
                                autoFocus
                                onBlur={() => setIsEditingTitle(false)}
                                className="h-7 text-sm font-semibold"
                            />
                        </form>
                    ) : (
                        <div className="flex items-center gap-2">
                            <h2 className="text-foreground text-sm font-semibold">
                                {column.title}
                            </h2>
                            <Badge
                                variant="secondary"
                                className="h-5 rounded-full px-2 py-0 text-xs font-medium"
                            >
                                {column.items.length}
                            </Badge>
                        </div>
                    )}

                    <DropdownMenu>
                        <DropdownMenuTrigger asChild>
                            <Button
                                variant="ghost"
                                size="icon"
                                className="text-muted-foreground hover:text-foreground h-7 w-7"
                            >
                                <MoreHorizontalIcon className="h-4 w-4" />
                            </Button>
                        </DropdownMenuTrigger>
                        <DropdownMenuContent align="end">
                            <DropdownMenuLabel>
                                Column Actions
                            </DropdownMenuLabel>
                            <DropdownMenuItem
                                onClick={() => setIsEditingTitle(true)}
                            >
                                <PenIcon className="mr-2 h-4 w-4" />
                                Rename
                            </DropdownMenuItem>
                            <DropdownMenuItem
                                onClick={() => onDeleteColumn(column.id)}
                                className="text-destructive"
                            >
                                <Trash2Icon className="mr-2 h-4 w-4" />
                                Delete
                            </DropdownMenuItem>
                        </DropdownMenuContent>
                    </DropdownMenu>
                </div>

                <ul className="custom-scrollbar mb-3 flex-1 space-y-2.5 overflow-y-auto">
                    {column.items.map((card) => (
                        <KanbanCard
                            key={card.id}
                            card={card}
                            columnId={column.id}
                            isActive={activeCardId === card.id}
                            isDragging={draggingCardId === card.id}
                            onDeleteCard={onDeleteCard}
                            onUpdateCardTitle={onUpdateCardTitle}
                            onCardKeyDown={onCardKeyDown}
                            onCardBlur={onCardBlur}
                            onCardClick={onCardClick}
                            onToggleComplete={onToggleComplete}
                            onDropOverListItem={handleDropOverListItem(card.id)}
                        />
                    ))}
                </ul>

                <Button
                    variant="ghost"
                    className="text-muted-foreground hover:bg-accent hover:text-accent-foreground w-full justify-start gap-2 text-sm"
                    onClick={() => setAddCardDialogOpen(true)}
                >
                    <PlusIcon className="h-4 w-4" />
                    Add card
                </Button>
            </section>

            <AddCardDialog
                open={addCardDialogOpen}
                onOpenChange={setAddCardDialogOpen}
                onAddCard={(cardData) => {
                    const newCard = {
                        id: createId(),
                        ...cardData,
                        completed: false,
                    };
                    onMoveCardToColumn(column.id, column.items.length, newCard);
                }}
            />
        </>
    );
}

function KanbanCard({
    card,
    columnId,
    isActive,
    isDragging,
    onDeleteCard,
    onUpdateCardTitle,
    onCardKeyDown,
    onCardBlur,
    onCardClick,
    onToggleComplete,
    onDropOverListItem,
}: {
    card: Card;
    columnId: string;
    isActive: boolean;
    isDragging: boolean;
    onDeleteCard: (cardId: string) => void;
    onUpdateCardTitle: (cardId: string, title: string) => void;
    onCardKeyDown: (
        event: KeyboardEvent<HTMLDivElement>,
        cardId: string,
    ) => void;
    onCardBlur: () => void;
    onCardClick: (card: Card) => void;
    onToggleComplete: (cardId: string) => void;
    onDropOverListItem: (
        dataTransferData: string,
        dropDirection: KanbanBoardDropDirection,
    ) => void;
}) {
    const [isEditing, setIsEditing] = useState(false);
    const [dropDirection, setDropDirection] =
        useState<KanbanBoardDropDirection | null>(null);
    const { onDragStart, onDragEnd, onDragOver } = useDndEvents();

    return (
        <li
            className={cn(
                'group bg-card relative cursor-pointer rounded-lg',
                'kanban-card-elevation',
                'transition-all duration-200 ease-out',
                'hover:-translate-y-0.5',
                isActive && 'kanban-card-active',
                isDragging && 'kanban-card-dragging',
                card.completed && 'opacity-70',
                dropDirection === 'top' && 'border-t-primary border-t-2',
                dropDirection === 'bottom' && 'border-b-primary border-b-2',
            )}
            onDragLeave={() => setDropDirection(null)}
            onDragOver={(event) => {
                if (event.dataTransfer.types.includes('kanban-board-card')) {
                    event.preventDefault();
                    event.stopPropagation();
                    const rect = event.currentTarget.getBoundingClientRect();
                    const midpoint = (rect.top + rect.bottom) / 2;
                    setDropDirection(
                        event.clientY <= midpoint ? 'top' : 'bottom',
                    );
                    onDragOver('', card.id);
                }
            }}
            onDrop={(event) => {
                event.stopPropagation();
                const data = event.dataTransfer.getData('kanban-board-card');
                onDropOverListItem(data, dropDirection!);
                onDragEnd(JSON.parse(data).id as string, card.id);
                setDropDirection(null);
            }}
        >
            <div
                draggable
                onDragStart={(event) => {
                    event.dataTransfer.setData(
                        'kanban-board-card',
                        JSON.stringify(card),
                    );
                    event.dataTransfer.effectAllowed = 'move';
                    onDragStart(card.id);
                }}
                onDragEnd={() => {
                    onDragEnd(card.id);
                }}
                onKeyDown={(event) => onCardKeyDown(event, card.id)}
                onBlur={onCardBlur}
                tabIndex={0}
                onClick={() => onCardClick(card)}
                className="cursor-grab p-3 active:cursor-grabbing"
            >
                <div className="mb-2.5 flex items-start gap-2.5">
                    <button
                        onClick={(e) => {
                            e.stopPropagation();
                            onToggleComplete(card.id);
                        }}
                        className="text-muted-foreground hover:text-foreground mt-0.5 transition-all hover:scale-110"
                    >
                        {card.completed ? (
                            <CheckCircle2 className="h-4 w-4 text-green-600 dark:text-green-500" />
                        ) : (
                            <Circle className="h-4 w-4" />
                        )}
                    </button>

                    <div className="min-w-0 flex-1">
                        {isEditing ? (
                            <Input
                                defaultValue={card.title}
                                autoFocus
                                onBlur={(e) => {
                                    onUpdateCardTitle(card.id, e.target.value);
                                    setIsEditing(false);
                                }}
                                onClick={(e) => e.stopPropagation()}
                                className="-mt-1 h-7 text-sm"
                            />
                        ) : (
                            <h3
                                className={cn(
                                    'text-foreground text-sm leading-snug font-medium',
                                    card.completed &&
                                        'text-muted-foreground line-through',
                                )}
                            >
                                {card.title}
                            </h3>
                        )}
                    </div>

                    <DropdownMenu>
                        <DropdownMenuTrigger
                            asChild
                            onClick={(e) => e.stopPropagation()}
                        >
                            <Button
                                variant="ghost"
                                size="icon"
                                className="-mt-0.5 h-6 w-6 opacity-0 transition-opacity group-hover:opacity-100"
                            >
                                <MoreHorizontalIcon className="text-muted-foreground h-3.5 w-3.5" />
                            </Button>
                        </DropdownMenuTrigger>
                        <DropdownMenuContent align="end">
                            <DropdownMenuItem
                                onClick={(e) => {
                                    e.stopPropagation();
                                    setIsEditing(true);
                                }}
                            >
                                <PenIcon className="mr-2 h-4 w-4" />
                                Edit
                            </DropdownMenuItem>
                            <DropdownMenuItem
                                onClick={(e) => {
                                    e.stopPropagation();
                                    onDeleteCard(card.id);
                                }}
                                className="text-destructive"
                            >
                                <Trash2Icon className="mr-2 h-4 w-4" />
                                Delete
                            </DropdownMenuItem>
                        </DropdownMenuContent>
                    </DropdownMenu>
                </div>

                <div className="space-y-2.5 pl-6">
                    {(card.priority || card.tags) && (
                        <div className="flex flex-wrap gap-1.5">
                            {card.priority && (
                                <Badge
                                    variant="outline"
                                    className={cn(
                                        'h-5 px-2 py-0 text-[10px] font-semibold',
                                        priorityConfig[card.priority].color,
                                    )}
                                >
                                    {card.priority.toUpperCase()}
                                </Badge>
                            )}
                            {card.tags?.map((tag) => (
                                <Badge
                                    key={tag}
                                    variant="secondary"
                                    className="h-5 px-2 py-0 text-[10px] font-medium"
                                >
                                    {tag}
                                </Badge>
                            ))}
                        </div>
                    )}

                    {(card.deadline || card.comments || card.assignedTo) && (
                        <div className="border-border/40 flex items-center justify-between border-t pt-2">
                            <div className="flex items-center gap-3">
                                {card.deadline && (
                                    <Tooltip>
                                        <TooltipTrigger>
                                            <div className="text-muted-foreground hover:text-foreground flex items-center gap-1.5 text-xs transition-colors">
                                                <Calendar className="h-3.5 w-3.5" />
                                                <span className="font-medium">
                                                    {format(
                                                        card.deadline,
                                                        'MMM d',
                                                    )}
                                                </span>
                                            </div>
                                        </TooltipTrigger>
                                        <TooltipContent>
                                            <p>
                                                Due:{' '}
                                                {format(
                                                    card.deadline,
                                                    'MMMM d, yyyy',
                                                )}
                                            </p>
                                        </TooltipContent>
                                    </Tooltip>
                                )}

                                {card.comments && card.comments.length > 0 && (
                                    <div className="text-muted-foreground flex items-center gap-1.5 text-xs">
                                        <MessageSquare className="h-3.5 w-3.5" />
                                        <span className="font-medium">
                                            {card.comments.length}
                                        </span>
                                    </div>
                                )}
                            </div>

                            {card.assignedTo && card.assignedTo.length > 0 && (
                                <div className="flex -space-x-1.5">
                                    {card.assignedTo
                                        .slice(0, 3)
                                        .map((assignee, index) => (
                                            <Tooltip key={index}>
                                                <TooltipTrigger>
                                                    <Avatar className="border-card ring-border/30 h-6 w-6 border-2 ring-1 transition-transform hover:z-10 hover:scale-110">
                                                        <AvatarFallback className="bg-primary text-primary-foreground text-[10px] font-bold">
                                                            {assignee
                                                                .substring(0, 2)
                                                                .toUpperCase()}
                                                        </AvatarFallback>
                                                    </Avatar>
                                                </TooltipTrigger>
                                                <TooltipContent>
                                                    <p>{assignee}</p>
                                                </TooltipContent>
                                            </Tooltip>
                                        ))}
                                    {card.assignedTo.length > 3 && (
                                        <Avatar className="border-card ring-border/30 h-6 w-6 border-2 ring-1">
                                            <AvatarFallback className="bg-muted text-muted-foreground text-[10px] font-bold">
                                                +{card.assignedTo.length - 3}
                                            </AvatarFallback>
                                        </Avatar>
                                    )}
                                </div>
                            )}
                        </div>
                    )}
                </div>
            </div>
        </li>
    );
}

function AddCardDialog({
    open,
    onOpenChange,
    onAddCard,
}: {
    open: boolean;
    onOpenChange: (open: boolean) => void;
    onAddCard: (card: Omit<Card, 'id' | 'completed'>) => void;
}) {
    const [title, setTitle] = useState('');
    const [description, setDescription] = useState('');
    const [priority, setPriority] = useState<CardPriority>('medium');
    const [assignee, setAssignee] = useState('');
    const [deadline, setDeadline] = useState('');
    const [tags, setTags] = useState('');

    const handleSubmit = () => {
        if (!title.trim()) return;

        onAddCard({
            title: title.trim(),
            description: description.trim() || undefined,
            priority,
            assignedTo: assignee ? [assignee.trim()] : undefined,
            deadline: deadline ? new Date(deadline) : undefined,
            tags: tags
                ? tags
                      .split(',')
                      .map((t) => t.trim())
                      .filter(Boolean)
                : undefined,
        });

        setTitle('');
        setDescription('');
        setPriority('medium');
        setAssignee('');
        setDeadline('');
        setTags('');
        onOpenChange(false);
    };

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent className="sm:max-w-[500px]">
                <DialogHeader>
                    <DialogTitle>Create New Card</DialogTitle>
                    <DialogDescription>
                        Add a new task to your board with all the details.
                    </DialogDescription>
                </DialogHeader>
                <div className="space-y-4 py-4">
                    <div className="space-y-2">
                        <Label htmlFor="title">Title *</Label>
                        <Input
                            id="title"
                            placeholder="Enter task title..."
                            value={title}
                            onChange={(e) => setTitle(e.target.value)}
                        />
                    </div>

                    <div className="space-y-2">
                        <Label htmlFor="description">Description</Label>
                        <Textarea
                            id="description"
                            placeholder="Add a description..."
                            value={description}
                            onChange={(e) => setDescription(e.target.value)}
                            rows={3}
                        />
                    </div>

                    <div className="grid grid-cols-2 gap-4">
                        <div className="space-y-2">
                            <Label htmlFor="priority">Priority</Label>
                            <Select
                                value={priority}
                                onValueChange={(value) =>
                                    setPriority(value as CardPriority)
                                }
                            >
                                <SelectTrigger id="priority">
                                    <SelectValue />
                                </SelectTrigger>
                                <SelectContent>
                                    <SelectItem value="low">Low</SelectItem>
                                    <SelectItem value="medium">
                                        Medium
                                    </SelectItem>
                                    <SelectItem value="high">High</SelectItem>
                                    <SelectItem value="urgent">
                                        Urgent
                                    </SelectItem>
                                </SelectContent>
                            </Select>
                        </div>

                        <div className="space-y-2">
                            <Label htmlFor="deadline">Deadline</Label>
                            <Input
                                id="deadline"
                                type="date"
                                value={deadline}
                                onChange={(e) => setDeadline(e.target.value)}
                            />
                        </div>
                    </div>

                    <div className="space-y-2">
                        <Label htmlFor="assignee">Assign To</Label>
                        <Input
                            id="assignee"
                            placeholder="Enter name..."
                            value={assignee}
                            onChange={(e) => setAssignee(e.target.value)}
                        />
                    </div>

                    <div className="space-y-2">
                        <Label htmlFor="tags">Tags (comma separated)</Label>
                        <Input
                            id="tags"
                            placeholder="e.g. Feature, UI, Backend"
                            value={tags}
                            onChange={(e) => setTags(e.target.value)}
                        />
                    </div>
                </div>
                <DialogFooter>
                    <Button
                        variant="outline"
                        onClick={() => onOpenChange(false)}
                    >
                        Cancel
                    </Button>
                    <Button onClick={handleSubmit} disabled={!title.trim()}>
                        Create Card
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
}

function AddColumnButton({
    onAddColumn,
}: {
    onAddColumn: (title?: string) => void;
}) {
    const [isAdding, setIsAdding] = useState(false);
    const [title, setTitle] = useState('');

    const handleAdd = () => {
        if (title.trim()) {
            onAddColumn(title);
            setTitle('');
            setIsAdding(false);
        }
    };

    if (isAdding) {
        return (
            <div className="border-border bg-card/50 w-80 shrink-0 space-y-2 rounded-xl border p-4">
                <Input
                    placeholder="Column title..."
                    value={title}
                    onChange={(e) => setTitle(e.target.value)}
                    autoFocus
                    onKeyDown={(e) => {
                        if (e.key === 'Enter') handleAdd();
                        if (e.key === 'Escape') setIsAdding(false);
                    }}
                />
                <div className="flex gap-2">
                    <Button size="sm" onClick={handleAdd}>
                        Add
                    </Button>
                    <Button
                        size="sm"
                        variant="ghost"
                        onClick={() => setIsAdding(false)}
                    >
                        Cancel
                    </Button>
                </div>
            </div>
        );
    }

    return (
        <Button
            variant="outline"
            className="bg-card/30 hover:bg-card/50 h-fit w-80 shrink-0 justify-start border-dashed"
            onClick={() => setIsAdding(true)}
        >
            <PlusIcon className="mr-2 h-4 w-4" />
            Add column
        </Button>
    );
}
