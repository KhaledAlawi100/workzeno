ALTER TABLE project_invitations
    DROP CONSTRAINT chk_project_invitations_status;

ALTER TABLE project_invitations
    ADD CONSTRAINT chk_project_invitations_status
        CHECK (status IN (
            'PENDING',
            'ACCEPTED',
            'DECLINED',
            'REVOKED',
            'EXPIRED'
        ));