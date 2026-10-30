
-- Projects
CREATE TABLE projects (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(2000),
    owner_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,


    -- Owner must exist. RESTRICT prevents deleting a user who still owns
    -- projects, forcing explicit reassignment/deletion first.
    CONSTRAINT fk_projects_owner
        FOREIGN KEY (owner_id)
        REFERENCES users (id)
        ON DELETE RESTRICT,

    -- Reject empty/whitespace-only names (BTRIM catches '   ').
    CONSTRAINT chk_projects_name
        CHECK (LENGTH(BTRIM(name)) BETWEEN 1 AND 120),

    -- Closed set of allowed statuses (simulated enum).
    CONSTRAINT chk_projects_status
        CHECK (status IN ('ACTIVE', 'ARCHIVED'))
);

-- "List my projects" filters by owner; avoids full table scan.
CREATE INDEX idx_projects_owner_id
    ON projects (owner_id);

-- "List ACTIVE projects" filters by status; avoids full table scan.
CREATE INDEX idx_projects_status
    ON projects (status);


-- Project memberships
CREATE TABLE project_memberships (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL,
    user_id UUID NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'MEMBER',
    joined_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Memberships are dependent data: deleting a project removes them.
    CONSTRAINT fk_project_memberships_project
        FOREIGN KEY (project_id)
        REFERENCES projects (id)
        ON DELETE CASCADE,

    -- Deleting a user cleans up their memberships everywhere.
    CONSTRAINT fk_project_memberships_user
        FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON DELETE CASCADE,

    -- A user can be a member of a project only once; also serves as the
    -- natural composite key (project_id, user_id).
    CONSTRAINT uq_project_memberships_project_user
        UNIQUE (project_id, user_id),

    -- Closed set of project roles.
    CONSTRAINT chk_project_memberships_role
        CHECK (role IN ('ADMIN', 'MEMBER'))
);

-- "List projects I'm a member of" scans by user. (UNIQUE above already
-- covers "list members of a project".)
CREATE INDEX idx_project_memberships_user_id
    ON project_memberships (user_id);


-- Project invitations
CREATE TABLE project_invitations (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL,
    invitee_email VARCHAR(254) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'MEMBER',
    token_hash VARCHAR(64) NOT NULL,
    invited_by_user_id UUID NOT NULL,
    accepted_by_user_id UUID,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    accepted_at TIMESTAMPTZ,

    -- Deleting a project removes its pending invitations.
    CONSTRAINT fk_project_invitations_project
        FOREIGN KEY (project_id)
        REFERENCES projects (id)
        ON DELETE CASCADE,

    -- RESTRICT preserves the audit trail; invitations must not silently
    -- vanish when the inviter is deleted.
    CONSTRAINT fk_project_invitations_invited_by
        FOREIGN KEY (invited_by_user_id)
        REFERENCES users (id)
        ON DELETE RESTRICT,

    -- RESTRICT preserves the audit trail; accepted invitations must not silently
    -- vanish when the accepter is deleted.
    CONSTRAINT fk_project_invitations_accepted_by
        FOREIGN KEY (accepted_by_user_id)
        REFERENCES users (id)
        ON DELETE RESTRICT,

    -- One invitation per token; enables O(1) lookup by hash and prevents
    -- a token from resolving to multiple rows.
    CONSTRAINT uq_project_invitations_token_hash
        UNIQUE (token_hash),

    -- Force normalization (lowercased + trimmed) so lookups by email are
    -- deterministic and duplicate invites differing only by case are blocked.
    CONSTRAINT chk_project_invitations_email_normalized
        CHECK (invitee_email = LOWER(BTRIM(invitee_email))),

    -- Closed set of roles, same as memberships.
    CONSTRAINT chk_project_invitations_role
        CHECK (role IN ('ADMIN', 'MEMBER')),

    -- Closed set of statuses; EXPIRED is set by the app/job, not auto.
    CONSTRAINT chk_project_invitations_status
        CHECK (status IN ('PENDING', 'ACCEPTED', 'REVOKED', 'EXPIRED')),

    -- Enforce exactly SHA-256 hex, lowercase: prevents base64/uppercase/raw
    -- token from being stored and keeps UNIQUE comparison canonical.
    CONSTRAINT chk_project_invitations_token_hash
        CHECK (token_hash ~ '^[0-9a-f]{64}$'),

    -- Expiry must be strictly after creation.
    CONSTRAINT chk_project_invitations_expiry
        CHECK (expires_at > created_at),


    -- All-or-nothing acceptance: ACCEPTED ⇒ both columns set; otherwise both
    -- NULL. Prevents half-accepted rows.
    CONSTRAINT chk_project_invitations_acceptance
        CHECK (
            (status = 'ACCEPTED'
                AND accepted_by_user_id IS NOT NULL
                AND accepted_at IS NOT NULL)
            OR
            (status <> 'ACCEPTED'
                AND accepted_by_user_id IS NULL
                AND accepted_at IS NULL)
        )
);

-- Dominant query: "pending invitations for a project".
-- Order matters: project_id (selective) first, status second.
CREATE INDEX idx_project_invitations_project_status
    ON project_invitations (project_id, status);

-- "List my pending invitations" filters by normalized email.
CREATE INDEX idx_project_invitations_email
    ON project_invitations (invitee_email);
