-- ============================================================
-- V11: Create notifications
-- ============================================================

CREATE TABLE notifications (

    id UUID NOT NULL,

    recipient_id UUID NOT NULL,

    actor_id UUID,

    type VARCHAR(40) NOT NULL,

    title VARCHAR(120) NOT NULL,

    message VARCHAR(500) NOT NULL,

    target_url VARCHAR(500),

    reference_type VARCHAR(40),

    reference_id UUID,

    read_at TIMESTAMPTZ,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_notifications
        PRIMARY KEY (id),

    CONSTRAINT fk_notifications_recipient
        FOREIGN KEY (recipient_id)
        REFERENCES users (id)
        ON DELETE CASCADE,

    CONSTRAINT fk_notifications_actor
        FOREIGN KEY (actor_id)
        REFERENCES users (id)
        ON DELETE SET NULL,

    CONSTRAINT chk_notifications_reference
        CHECK (
            (reference_type IS NULL AND reference_id IS NULL)
            OR
            (reference_type IS NOT NULL AND reference_id IS NOT NULL)
        )
);


-- Retrieve a user's notifications in chronological order.
CREATE INDEX idx_notifications_recipient_created
    ON notifications (
        recipient_id,
        created_at DESC,
        id DESC
    );


-- Efficiently count and retrieve unread notifications.
CREATE INDEX idx_notifications_unread
    ON notifications (
        recipient_id,
        created_at DESC
    )
    WHERE read_at IS NULL;