
-- Remove the unique constraint on invitation token hashes.
ALTER TABLE project_invitations
    DROP CONSTRAINT IF EXISTS uq_project_invitations_token_hash;

-- Remove the SHA-256 hexadecimal format validation.
ALTER TABLE project_invitations
    DROP CONSTRAINT IF EXISTS chk_project_invitations_token_hash;

-- Remove the token hash column itself.
ALTER TABLE project_invitations
    DROP COLUMN IF EXISTS token_hash;
