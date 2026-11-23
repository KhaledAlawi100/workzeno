-- Workzeno Phase 4: task management and task discussions.
-- This migration intentionally uses the existing V1-V8 table and constraint names.

-- Tasks belong to one project. An optional assignee must be a member of that
-- same project; the composite foreign key below enforces this in PostgreSQL.
CREATE TABLE tasks (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL,
    assignee_id UUID,
    title VARCHAR(200) NOT NULL,
    description VARCHAR(2000),
    status VARCHAR(20) NOT NULL DEFAULT 'TODO',
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    due_date DATE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_tasks_project
        FOREIGN KEY (project_id)
        REFERENCES projects (id)
        ON DELETE CASCADE,

    -- The referenced pair is already unique in V4:
    -- uq_project_memberships_project_user (project_id, user_id).
    -- A NULL assignee is allowed; a non-NULL assignee must belong to this project.
    -- NO ACTION prevents deleting a referenced membership. The application must
    -- unassign tasks before removing a member. It also permits a project deletion
    -- to cascade to both tasks and memberships within the same SQL statement.
    CONSTRAINT fk_tasks_project_assignee
        FOREIGN KEY (project_id, assignee_id)
        REFERENCES project_memberships (project_id, user_id)
        ON DELETE NO ACTION,

    CONSTRAINT chk_tasks_title
        CHECK (LENGTH(BTRIM(title)) BETWEEN 1 AND 200),

    CONSTRAINT chk_tasks_status
        CHECK (status IN ('TODO', 'IN_PROGRESS', 'IN_REVIEW', 'DONE')),

    CONSTRAINT chk_tasks_priority
        CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'URGENT'))
);

-- Supports loading a project's Kanban board and filtering tasks by status.
CREATE INDEX idx_tasks_project_status
    ON tasks (project_id, status);

-- Supports project-scoped priority filtering.
CREATE INDEX idx_tasks_project_priority
    ON tasks (project_id, priority);

-- Supports filtering a project's tasks by assignee.
CREATE INDEX idx_tasks_project_assignee
    ON tasks (project_id, assignee_id)
    WHERE assignee_id IS NOT NULL;

-- Supports a user's assigned-task views, including status filtering.
CREATE INDEX idx_tasks_assignee_status
    ON tasks (assignee_id, status)
    WHERE assignee_id IS NOT NULL;

-- Comments are retained with their task. Deleting a task explicitly (or deleting
-- its project through the existing project cascade) removes its comments.
CREATE TABLE task_comments (
    id UUID PRIMARY KEY,
    task_id UUID NOT NULL,
    author_id UUID NOT NULL,
    content VARCHAR(2000) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_task_comments_task
        FOREIGN KEY (task_id)
        REFERENCES tasks (id)
        ON DELETE CASCADE,

    -- Preserve comment authorship history; user deletion must be handled explicitly.
    CONSTRAINT fk_task_comments_author
        FOREIGN KEY (author_id)
        REFERENCES users (id)
        ON DELETE RESTRICT,

    CONSTRAINT chk_task_comments_content
        CHECK (LENGTH(BTRIM(content)) BETWEEN 1 AND 2000)
);

-- Supports retrieving a task's discussion in chronological order.
CREATE INDEX idx_task_comments_task_created
    ON task_comments (task_id, created_at, id);

-- Supports finding comments authored by a user, for account/audit workflows.
CREATE INDEX idx_task_comments_author
    ON task_comments (author_id);
