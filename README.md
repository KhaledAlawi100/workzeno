# Workzeno

Workzeno is a full-stack application built with React, TypeScript, Spring Boot, and PostgreSQL. It is designed to provide a maintainable foundation for project and task management.

## Technology Stack

- **Frontend:** React, TypeScript, Vite, Material UI
- **Backend:** Java 21, Spring Boot, Maven Wrapper
- **Database:** PostgreSQL
- **Schema migrations:** Flyway
- **Frontend testing:** Vitest and React Testing Library
- **Backend testing:** JUnit and Spring Boot Test
- **CI/CD:** GitHub Actions
- **Local infrastructure:** Docker Compose

## Prerequisites

Install the following tools before starting:

- Git
- Node.js LTS and npm
- Java 21 JDK
- Docker Desktop with Docker Compose
- IntelliJ IDEA or Visual Studio Code

The repository includes the Maven Wrapper, so a global Maven installation is not required.

Verify your installations:

```powershell
git --version
node --version
npm --version
java -version
docker --version
docker compose version
```

## Repository Structure

```text
Workzeno/
├── .github/
│   └── workflows/
│       └── ci.yml
├── backend/
│   └── workzeno/
│       ├── mvnw
│       ├── mvnw.cmd
│       ├── pom.xml
│       └── src/
├── frontend/
│   ├── .env.example
│   ├── package.json
│   └── src/
├── docs/
├── infra/
├── .env.example
├── .gitignore
├── docker-compose.yml
└── README.md
```

The structure above illustrates the expected project layout. Additional files and directories may exist inside `backend/workzeno/`, `frontend/`, and `infra/`.

## 1. Clone and Configure the Repository

Clone the repository and navigate to its root directory:

```powershell
git clone https://github.com/KhaledAlawi100/workzeno.git
cd workzeno
Copy-Item .env.example .env
```



## 2. Start PostgreSQL

Run these commands from the repository root:

```powershell
docker compose -f docker-compose.yml config
docker compose -f docker-compose.yml up -d db
docker compose -f docker-compose.yml ps
```

Wait until the database reports a healthy state.

The database is exposed on the host port configured by `POSTGRES_PORT`, normally `5432`.

View database logs:

```powershell
docker compose -f docker-compose.yml logs db
```

Stop PostgreSQL:

```powershell
docker compose -f docker-compose.yml stop db
```

Start it again:

```powershell
docker compose -f docker-compose.yml start db
```

The named Docker volume preserves database data across normal container stops and restarts.

**Warning:** Do not use `docker compose -f docker-compose.yml down -v` unless you intentionally want to remove the associated Docker volumes and their local database data.

## 3. Configure and Run the Backend

The Spring Boot project is located inside `backend/workzeno/`.

From the repository root, navigate to the project directory:

```powershell
cd backend/workzeno
```

### Load the root environment variables

The root `.env` file is located two directory levels above the Spring Boot project directory.

Load its simple `KEY=value` entries into the current PowerShell process:

```powershell
Get-Content ..\..\.env | ForEach-Object {
    if ($_ -match '^\s*([^#=\s]+)\s*=(.*)$') {
        $key = $matches[1].Trim()
        $value = $matches[2].Trim()
        [Environment]::SetEnvironmentVariable(
            $key,
            $value,
            "Process"
        )
    }
}
```

This loader is intended for simple environment entries without quoted values or complex shell syntax.

Activate the local Spring profile:

```powershell
$env:SPRING_PROFILES_ACTIVE = "local"
```

Start the backend:

```powershell
.\mvnw.cmd spring-boot:run
```

The backend normally listens on port `8080`.

### Database schema management

The application uses Flyway for database schema migrations and Hibernate schema validation.

All database schema changes must be represented by versioned Flyway migrations. Do not use Hibernate schema auto-update as a replacement for migrations.

## 4. Configure and Run the Frontend

Open a separate PowerShell terminal and navigate to the frontend:

```powershell
cd frontend
Copy-Item .env.example .env.local
```

The frontend environment configuration should contain:

```dotenv
VITE_API_BASE_URL=http://localhost:8080/api/v1
```

Ensure the variable name and URL match the actual frontend configuration and backend API routes.

Install the locked dependencies:

```powershell
npm ci
```

Start the development server:

```powershell
npm run dev
```

Open the local URL printed by Vite, normally:

`http://localhost:5173`

**Security:** Variables prefixed with `VITE_` are exposed to the browser. Never store passwords, private keys, or other secrets in frontend environment variables.

## 5. Verify Application Health

With PostgreSQL and Spring Boot running, execute the following commands from a PowerShell terminal:

```powershell
Invoke-RestMethod http://localhost:8080/api/v1/health
```

Check Spring Boot Actuator health:

```powershell
Invoke-RestMethod http://localhost:8080/actuator/health
```

Check readiness:

```powershell
Invoke-RestMethod http://localhost:8080/actuator/health/readiness
```

These endpoints must be implemented and exposed by the backend for the commands to succeed.

The API health endpoint verifies that the application responds. The readiness endpoint can verify whether the application is ready to serve requests, including database connectivity when the appropriate health indicators are configured.

Open the frontend and use its backend connectivity check, if available, to verify browser-to-API communication.

## 6. Run Verification Locally

### Backend

From the repository root:

```powershell
cd backend/workzeno
.\mvnw.cmd clean verify
```

Backend tests must have the database configuration and test infrastructure expected by the test setup.

### Frontend

From the repository root:

```powershell
cd frontend
npm ci
npm run lint
npm test
npm run build
```

Run the verification commands before pushing changes. All commands should pass before a change is considered ready for review.

## 7. Continuous Integration

The GitHub Actions workflow is located at:

```text
.github/workflows/ci.yml
```

The workflow is expected to run backend verification and frontend linting, tests, and production build checks on configured pushes and pull requests.

Inspect the **Actions** tab on GitHub to review job logs and results.

If merges must be blocked when checks fail, configure the appropriate required status checks in the repository's branch protection rules or ruleset.

CI should use temporary PostgreSQL infrastructure and dedicated test credentials. It must not depend on production secrets.

## 8. Troubleshooting

### PostgreSQL connection refused

- Confirm Docker Desktop is running.
- Run `docker compose -f docker-compose.yml ps`.
- Inspect database logs.
- Confirm that the configured host port and database URL match.

### Port 5432 is already in use

Change `POSTGRES_PORT` in the root `.env` file to an available host port.

Update `DB_URL` to use the same host port, then restart the affected services if necessary.

### Spring Boot reports a missing database password

Load the root `.env` into the same PowerShell process used to launch Spring Boot.

Docker Compose reading `.env` does not automatically export those variables into your PowerShell session.

### Flyway refuses to start

- Read the migration error carefully.
- Inspect the migration history and current database schema.
- Check for conflicting or previously applied migrations.
- Do not disable Flyway or delete database data without understanding the cause.

### Frontend reports a network or CORS error

- Confirm Spring Boot is running.
- Confirm `VITE_API_BASE_URL` points to the correct backend URL.
- Confirm `CORS_ALLOWED_ORIGIN` matches the actual Vite origin.
- Restart the development server after changing environment variables.

### Frontend dependency installation fails

Ensure `package.json` and `package-lock.json` are committed and synchronized. Check that the installed Node.js version satisfies the project's requirements.

### Maven Wrapper command fails

Ensure you are running the command from `backend/workzeno/` and that `mvnw.cmd` and `pom.xml` exist in that directory.

## Security Notes

- Local development credentials are placeholders, not production secrets.
- Never commit `.env`, production credentials, access tokens, or private keys.
- Keep frontend environment variables free of secrets.
- Use a suitable secret-management mechanism for production deployments.
- Use separate configuration for local development, testing, and production.

## Current Milestone

**Phase 1 — Project Foundation**

Phase 1 establishes a reproducible development environment, database configuration, application health checks, and automated verification.

Authentication, registration, projects, and tasks are planned for subsequent milestones.
