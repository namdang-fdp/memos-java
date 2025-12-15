-- V4__init_kanban.sql
-- Kanban core schema (matches JPA entities exactly):
-- board, feature, task, task_snippet, task_comment, label, task_label, task_assignee, task_activity

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- =========================================================
-- BOARD (com.namdang.memos.entity.Board)
-- =========================================================
CREATE TABLE IF NOT EXISTS board (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at  TIMESTAMP NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP NULL,
    is_deleted  BOOLEAN NOT NULL DEFAULT false,

    project_id  UUID NOT NULL,
    name        VARCHAR(255) NOT NULL,

    board_type  VARCHAR(20) NOT NULL DEFAULT 'KANBAN',
    is_archived BOOLEAN NOT NULL DEFAULT false,

    CONSTRAINT fk_board_project
        FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE
);

-- indexes in entity
CREATE INDEX IF NOT EXISTS idx_board_project_id
ON board (project_id);

CREATE INDEX IF NOT EXISTS idx_board_project_archived
ON board (project_id, is_archived);


-- =========================================================
-- FEATURE (com.namdang.memos.entity.Feature)
-- =========================================================
CREATE TABLE IF NOT EXISTS feature (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at  TIMESTAMP NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP NULL,
    is_deleted  BOOLEAN NOT NULL DEFAULT false,

    board_id    UUID NOT NULL,

    name        VARCHAR(255) NOT NULL,
    description TEXT NULL,

    position    NUMERIC(20, 10) NOT NULL,
    wip_limit   INT NULL,

    is_archived BOOLEAN NOT NULL DEFAULT false,

    CONSTRAINT fk_feature_board
        FOREIGN KEY (board_id) REFERENCES board(id) ON DELETE CASCADE
);

-- indexes in entity
CREATE INDEX IF NOT EXISTS idx_feature_board_id
ON feature (board_id);

CREATE INDEX IF NOT EXISTS idx_feature_board_position
ON feature (board_id, position);

CREATE INDEX IF NOT EXISTS idx_feature_board_archived
ON feature (board_id, is_archived);


-- =========================================================
-- TASK (com.namdang.memos.entity.Task)
-- =========================================================
CREATE TABLE IF NOT EXISTS task (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at   TIMESTAMP NOT NULL DEFAULT now(),
    updated_at   TIMESTAMP NULL,
    is_deleted   BOOLEAN NOT NULL DEFAULT false,

    project_id   UUID NOT NULL,
    feature_id   UUID NOT NULL,
    created_by   UUID NOT NULL,

    title        VARCHAR(500) NOT NULL,
    description  TEXT NULL,

    priority     VARCHAR(20) NULL,
    due_at       TIMESTAMP NULL,
    completed_at TIMESTAMP NULL,

    position     NUMERIC(20, 10) NOT NULL,

    is_archived  BOOLEAN NOT NULL DEFAULT false,

    CONSTRAINT fk_task_project
        FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE,

    CONSTRAINT fk_task_feature
        FOREIGN KEY (feature_id) REFERENCES feature(id) ON DELETE CASCADE,

    CONSTRAINT fk_task_created_by
        FOREIGN KEY (created_by) REFERENCES account(id)
);

-- indexes in entity
CREATE INDEX IF NOT EXISTS idx_task_project_id
ON task (project_id);

CREATE INDEX IF NOT EXISTS idx_task_feature_id
ON task (feature_id);

CREATE INDEX IF NOT EXISTS idx_task_feature_position
ON task (feature_id, position);

CREATE INDEX IF NOT EXISTS idx_task_project_completed_at
ON task (project_id, completed_at);

CREATE INDEX IF NOT EXISTS idx_task_due_at
ON task (due_at);


-- =========================================================
-- TASK_SNIPPET (com.namdang.memos.entity.TaskSnippet)
-- =========================================================
CREATE TABLE IF NOT EXISTS task_snippet (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at  TIMESTAMP NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP NULL,
    is_deleted  BOOLEAN NOT NULL DEFAULT false,

    task_id     UUID NOT NULL,
    created_by  UUID NOT NULL,

    language    VARCHAR(50) NULL,
    title       VARCHAR(120) NULL,
    context     TEXT NULL,
    content     TEXT NOT NULL,

    position    NUMERIC(20, 10) NOT NULL,

    CONSTRAINT fk_task_snippet_task
        FOREIGN KEY (task_id) REFERENCES task(id) ON DELETE CASCADE,

    CONSTRAINT fk_task_snippet_created_by
        FOREIGN KEY (created_by) REFERENCES account(id)
);

-- indexes in entity
CREATE INDEX IF NOT EXISTS idx_task_snippet_task_id
ON task_snippet (task_id);

CREATE INDEX IF NOT EXISTS idx_task_snippet_task_position
ON task_snippet (task_id, position);

CREATE INDEX IF NOT EXISTS idx_task_snippet_language
ON task_snippet (language);


-- =========================================================
-- TASK_COMMENT (com.namdang.memos.entity.TaskComment)
-- =========================================================
CREATE TABLE IF NOT EXISTS task_comment (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at  TIMESTAMP NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP NULL,
    is_deleted  BOOLEAN NOT NULL DEFAULT false,

    task_id     UUID NOT NULL,
    author_id   UUID NOT NULL,

    content     TEXT NOT NULL,
    is_edited   BOOLEAN NOT NULL DEFAULT false,

    CONSTRAINT fk_task_comment_task
        FOREIGN KEY (task_id) REFERENCES task(id) ON DELETE CASCADE,

    CONSTRAINT fk_task_comment_author
        FOREIGN KEY (author_id) REFERENCES account(id)
);

-- indexes in entity
CREATE INDEX IF NOT EXISTS idx_task_comment_task_created
ON task_comment (task_id, created_at);

CREATE INDEX IF NOT EXISTS idx_task_comment_author
ON task_comment (author_id);


-- =========================================================
-- LABEL (com.namdang.memos.entity.Label)
-- =========================================================
CREATE TABLE IF NOT EXISTS label (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at  TIMESTAMP NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP NULL,
    is_deleted  BOOLEAN NOT NULL DEFAULT false,

    project_id  UUID NOT NULL,

    name        VARCHAR(80) NOT NULL,
    color       VARCHAR(30) NULL,

    CONSTRAINT fk_label_project
        FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE,

    CONSTRAINT uq_label_project_name
        UNIQUE (project_id, name)
);

-- indexes in entity
CREATE INDEX IF NOT EXISTS idx_label_project_id
ON label (project_id);


-- =========================================================
-- TASK_LABEL (com.namdang.memos.entity.TaskLabel)
-- Composite PK: (task_id, label_id)
-- =========================================================
CREATE TABLE IF NOT EXISTS task_label (
    task_id    UUID NOT NULL,
    label_id   UUID NOT NULL,

    added_at   TIMESTAMP NOT NULL DEFAULT now(),

    CONSTRAINT pk_task_label PRIMARY KEY (task_id, label_id),

    CONSTRAINT fk_task_label_task
        FOREIGN KEY (task_id) REFERENCES task(id) ON DELETE CASCADE,

    CONSTRAINT fk_task_label_label
        FOREIGN KEY (label_id) REFERENCES label(id) ON DELETE CASCADE
);

-- unique constraint name from entity (redundant with PK but keep for matching intent)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'uq_task_label_task_label'
    ) THEN
        ALTER TABLE task_label
        ADD CONSTRAINT uq_task_label_task_label UNIQUE (task_id, label_id);
    END IF;
END $$;

-- indexes in entity
CREATE INDEX IF NOT EXISTS idx_task_label_task_id
ON task_label (task_id);

CREATE INDEX IF NOT EXISTS idx_task_label_label_id
ON task_label (label_id);


-- =========================================================
-- TASK_ASSIGNEE (com.namdang.memos.entity.TaskAssignee)
-- Composite PK: (task_id, account_id)
-- =========================================================
CREATE TABLE IF NOT EXISTS task_assignee (
    task_id     UUID NOT NULL,
    account_id  UUID NOT NULL,

    assigned_at TIMESTAMP NOT NULL DEFAULT now(),
    assigned_by UUID NULL,

    CONSTRAINT pk_task_assignee PRIMARY KEY (task_id, account_id),

    CONSTRAINT fk_task_assignee_task
        FOREIGN KEY (task_id) REFERENCES task(id) ON DELETE CASCADE,

    CONSTRAINT fk_task_assignee_account
        FOREIGN KEY (account_id) REFERENCES account(id),

    CONSTRAINT fk_task_assignee_assigned_by
        FOREIGN KEY (assigned_by) REFERENCES account(id)
);

-- unique constraint name from entity (redundant with PK but keep for matching intent)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'uq_task_assignee_task_account'
    ) THEN
        ALTER TABLE task_assignee
        ADD CONSTRAINT uq_task_assignee_task_account UNIQUE (task_id, account_id);
    END IF;
END $$;

-- indexes in entity
CREATE INDEX IF NOT EXISTS idx_task_assignee_task_id
ON task_assignee (task_id);

CREATE INDEX IF NOT EXISTS idx_task_assignee_account_id
ON task_assignee (account_id);


-- =========================================================
-- TASK_ACTIVITY (com.namdang.memos.entity.TaskActivity)
-- =========================================================
CREATE TABLE IF NOT EXISTS task_activity (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at    TIMESTAMP NOT NULL DEFAULT now(),
    updated_at    TIMESTAMP NULL,
    is_deleted    BOOLEAN NOT NULL DEFAULT false,

    project_id    UUID NOT NULL,
    task_id       UUID NOT NULL,
    actor_id      UUID NOT NULL,

    activity_type VARCHAR(40) NOT NULL,

    from_feature_id UUID NULL,
    to_feature_id   UUID NULL,

    payload       JSONB NULL,

    CONSTRAINT fk_task_activity_project
        FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE,

    CONSTRAINT fk_task_activity_task
        FOREIGN KEY (task_id) REFERENCES task(id) ON DELETE CASCADE,

    CONSTRAINT fk_task_activity_actor
        FOREIGN KEY (actor_id) REFERENCES account(id),

    CONSTRAINT fk_task_activity_from_feature
        FOREIGN KEY (from_feature_id) REFERENCES feature(id),

    CONSTRAINT fk_task_activity_to_feature
        FOREIGN KEY (to_feature_id) REFERENCES feature(id)
);

-- indexes in entity
CREATE INDEX IF NOT EXISTS idx_task_activity_project_created
ON task_activity (project_id, created_at);

CREATE INDEX IF NOT EXISTS idx_task_activity_task_created
ON task_activity (task_id, created_at);

CREATE INDEX IF NOT EXISTS idx_task_activity_actor_created
ON task_activity (actor_id, created_at);

CREATE INDEX IF NOT EXISTS idx_task_activity_type
ON task_activity (activity_type);
