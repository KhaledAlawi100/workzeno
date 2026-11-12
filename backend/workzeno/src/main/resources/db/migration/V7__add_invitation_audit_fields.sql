
-- Records when an invitation was declined.
ALTER TABLE project_invitations
    ADD COLUMN declined_at TIMESTAMPTZ;

-- Records when an invitation was revoked.
ALTER TABLE project_invitations
    ADD COLUMN revoked_at TIMESTAMPTZ;

-- Records the user who revoked an invitation.
ALTER TABLE project_invitations
    ADD COLUMN revoked_by_user_id UUID;

-- Preserve referential integrity between the invitation
-- and the user who performed the revocation.
ALTER TABLE project_invitations
    ADD CONSTRAINT fk_project_invitations_revoked_by
        FOREIGN KEY (revoked_by_user_id)
        REFERENCES users (id)
        ON DELETE RESTRICT;
