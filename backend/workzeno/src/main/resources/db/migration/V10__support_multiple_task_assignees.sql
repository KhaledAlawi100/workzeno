-- ============================================================
-- V10: Support multiple assignees per task
-- ============================================================

-- ------------------------------------------------------------
-- 1. Add a composite unique constraint to tasks.
-- ------------------------------------------------------------

ALTER TABLE tasks
    ADD CONSTRAINT uq_tasks_id_project
    UNIQUE (id, project_id);


-- ------------------------------------------------------------
-- 2. Create the task_assignees association table.
--
-- ------------------------------------------------------------

CREATE TABLE task_assignees (

    task_id UUID NOT NULL,

    project_id UUID NOT NULL,

    user_id UUID NOT NULL,

    assigned_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- A user can only be assigned once to the same task.
    CONSTRAINT pk_task_assignees
        PRIMARY KEY (task_id, user_id),

    -- Ensure the task belongs to the specified project.
    -- Deleting a task automatically removes its assignments.
    CONSTRAINT fk_task_assignees_task_project
        FOREIGN KEY (task_id, project_id)
        REFERENCES tasks (id, project_id)
        ON DELETE CASCADE,

    -- Ensure the assigned user is a member of that same project.
    -- Membership removal must remove assignments first.
    CONSTRAINT fk_task_assignees_project_membership
        FOREIGN KEY (project_id, user_id)
        REFERENCES project_memberships (project_id, user_id)
        ON DELETE NO ACTION
);


-- ------------------------------------------------------------
-- 3. Migrate existing assignments.
--

-- ------------------------------------------------------------

INSERT INTO task_assignees (
    task_id,
    project_id,
    user_id
)
SELECT
    id,
    project_id,
    assignee_id
FROM tasks
WHERE assignee_id IS NOT NULL;


-- ------------------------------------------------------------
-- 4. Add indexes for assignment queries.
-- ------------------------------------------------------------


-- Find all tasks assigned to a project member.
CREATE INDEX idx_task_assignees_project_user
    ON task_assignees (project_id, user_id);

-- Find tasks assigned to a user within a project,
-- ordered or filtered further by task ID.
CREATE INDEX idx_task_assignees_project_user_task
    ON task_assignees (project_id, user_id, task_id);


-- ------------------------------------------------------------
-- 5. Remove the old single-assignee structure.
--
-- ------------------------------------------------------------

DROP INDEX idx_tasks_project_assignee;

DROP INDEX idx_tasks_assignee_status;

ALTER TABLE tasks
    DROP CONSTRAINT fk_tasks_project_assignee;

ALTER TABLE tasks
    DROP COLUMN assignee_id;


