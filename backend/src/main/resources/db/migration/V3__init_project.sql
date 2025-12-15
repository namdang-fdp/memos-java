CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- PROJECT
CREATE TABLE IF NOT EXISTS project (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at      TIMESTAMP NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP NULL,
    is_deleted      BOOLEAN NOT NULL DEFAULT false,

    name            VARCHAR(255) NOT NULL,
    project_key     VARCHAR(50) NOT NULL UNIQUE,
    image_url       TEXT NULL,
    description     TEXT NULL,

    created_by      UUID NOT NULL,
    is_archived     BOOLEAN NOT NULL DEFAULT false,

    CONSTRAINT fk_project_created_by
        FOREIGN KEY (created_by) REFERENCES account(id)
);

-- Indexes for listing/filtering
CREATE INDEX IF NOT EXISTS idx_project_created_by ON project (created_by);
CREATE INDEX IF NOT EXISTS idx_project_created_at ON project (created_at);
CREATE INDEX IF NOT EXISTS idx_project_is_deleted ON project (is_deleted);
CREATE INDEX IF NOT EXISTS idx_project_is_archived ON project (is_archived);

-- Useful partial index: active projects only
CREATE INDEX IF NOT EXISTS idx_project_active
ON project (created_at DESC)
WHERE is_deleted = false AND is_archived = false;


-- PROJECT MEMBER
CREATE TABLE IF NOT EXISTS project_member (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at          TIMESTAMP NOT NULL DEFAULT now(),
    updated_at          TIMESTAMP NULL,
    is_deleted          BOOLEAN NOT NULL DEFAULT false,

    project_id          UUID NOT NULL,
    account_id          UUID NULL,

    joined_at           TIMESTAMP NULL,

    role                VARCHAR(20) NOT NULL DEFAULT 'MEMBER',

    invited_email       TEXT NULL,
    invited_status      VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    invite_token        VARCHAR(200) UNIQUE,
    invite_expired_at   TIMESTAMP NULL,

    CONSTRAINT fk_project_member_project
        FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE,
    CONSTRAINT fk_project_member_account
        FOREIGN KEY (account_id) REFERENCES account(id),

    CONSTRAINT uq_project_member UNIQUE (project_id, account_id)
);

CREATE INDEX IF NOT EXISTS idx_project_member_project_id ON project_member (project_id);
CREATE INDEX IF NOT EXISTS idx_project_member_account_id ON project_member (account_id);
CREATE INDEX IF NOT EXISTS idx_project_member_invite_token ON project_member (invite_token);

CREATE INDEX IF NOT EXISTS idx_project_member_pending_invites
ON project_member (project_id, invite_expired_at)
WHERE invited_status = 'PENDING' AND is_deleted = false;
