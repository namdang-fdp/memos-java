CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ACCOUNT
CREATE TABLE IF NOT EXISTS account (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at      TIMESTAMP NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP NULL,
    is_deleted      BOOLEAN NOT NULL DEFAULT false,

    email           VARCHAR(255) NOT NULL UNIQUE,
    password        TEXT NULL,
    name            VARCHAR(150) NULL,
    ory_identity_id VARCHAR(255) UNIQUE,
    auth_provider   VARCHAR(50) NULL,
    is_active       BOOLEAN NOT NULL DEFAULT true
);

CREATE INDEX IF NOT EXISTS idx_account_created_at ON account (created_at);
CREATE INDEX IF NOT EXISTS idx_account_is_deleted ON account (is_deleted);

-- ROLE
CREATE TABLE IF NOT EXISTS role (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(32) NOT NULL UNIQUE,
    description VARCHAR(255) NULL
);

-- PERMISSION
CREATE TABLE IF NOT EXISTS permission (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(100) NOT NULL UNIQUE,
    description TEXT NULL
);

-- ACCOUNT_ROLE (many-to-many)
CREATE TABLE IF NOT EXISTS account_role (
    account_id  UUID NOT NULL,
    role_id     UUID NOT NULL,
    PRIMARY KEY (account_id, role_id),
    CONSTRAINT fk_account_role_account
        FOREIGN KEY (account_id) REFERENCES account(id) ON DELETE CASCADE,
    CONSTRAINT fk_account_role_role
        FOREIGN KEY (role_id) REFERENCES role(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_account_role_account_id ON account_role (account_id);
CREATE INDEX IF NOT EXISTS idx_account_role_role_id ON account_role (role_id);

-- ROLE_PERMISSION (many-to-many)
CREATE TABLE IF NOT EXISTS role_permission (
    role_id       UUID NOT NULL,
    permission_id UUID NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_role_permission_role
        FOREIGN KEY (role_id) REFERENCES role(id) ON DELETE CASCADE,
    CONSTRAINT fk_role_permission_permission
        FOREIGN KEY (permission_id) REFERENCES permission(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_role_permission_role_id ON role_permission (role_id);
CREATE INDEX IF NOT EXISTS idx_role_permission_permission_id ON role_permission (permission_id);

-- INVALIDATED TOKEN
CREATE TABLE IF NOT EXISTS invalidated_token (
    id          VARCHAR(255) PRIMARY KEY,
    expiry_time TIMESTAMP NULL
);
