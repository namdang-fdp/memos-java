export const priority = ['low', 'medium', 'high', 'urgent'];

export type CardPriority = (typeof priority)[number];

export const kanbanCircleColor = [
    'primary',
    'gray',
    'red',
    'yellow',
    'green',
    'cyan',
    'blue',
    'indigo',
    'violet',
    'purple',
    'pink',
];
export type KanbanBoardCircleColor = (typeof kanbanCircleColor)[number];

export const kanbanDropDirection = ['none', 'top', 'bottom'];

export type KanbanBoardDropDirection = (typeof kanbanDropDirection)[number];

export type CardComment = {
    id: string;
    author: string;
    avatar?: string;
    content: string;
    createdAt: Date;
};

export type Card = {
    id: string;
    title: string;
    description?: string;
    snippet?: string;
    comments?: CardComment[];
    completed?: boolean;
    deadline?: Date;
    assignedTo?: string[];
    priority?: CardPriority;
    tags?: string[];
    createdAt?: Date;
    updatedAt?: Date;
};

export type Column = {
    id: string;
    title: string;
    color: KanbanBoardCircleColor;
    items: Card[];
};
