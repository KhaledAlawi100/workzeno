# Workzeno Database Migrations

Flyway is the single owner of database schema changes.

## Naming convention

Use the format:

`V<version>__<description>.sql`

Examples:

- `V1__create_users.sql`
- `V2__create_projects.sql`
- `V3__add_task_status.sql`

Use a unique, increasing version for every new migration.

## Rules

1. Write migrations as SQL scripts under `src/main/resources/db/migration/`.
2. Never edit a migration that has already been applied in a shared environment.
3. Create a new migration to change an existing table or constraint.
4. Review migrations for constraints, indexes, data impact, and rollback considerations.
5. Do not create empty migrations just to demonstrate Flyway.
6. Keep credentials out of migration files and committed configuration.
7. Let Flyway apply schema changes; use Hibernate `ddl-auto: validate` to check entity mappings.

The first domain migration will be added when the approved Workzeno user schema is ready.