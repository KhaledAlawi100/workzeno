CREATE UNIQUE INDEX uq_project_invitations_pending_project_email
ON project_invitations (project_id, invitee_email)
WHERE status = 'PENDING';