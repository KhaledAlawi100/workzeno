Workzeno
Workzeno is a project and task management application built with React, TypeScript, Spring Boot, and PostgreSQL.
Technology Stack
Frontend
•	React
•	TypeScript
•	Vite
•	Material UI
•	TanStack Query
•	React Hook Form
•	Zod
Backend
•	Java 21
•	Spring Boot
•	Maven
•	Spring Data JPA
•	Flyway
•	PostgreSQL
Repository Structure
•	frontend/ — React application
•	backend/ — Spring Boot application
•	docs/ — Project blueprint and architecture documentation
•	docker-compose.yml — Local infrastructure configuration
Prerequisites
•	Git
•	Node.js LTS compatible with the selected Vite release
•	Java 21
•	Docker Desktop or another compatible Docker Engine
Frontend Setup
cd frontend
npm ci
npm run dev
Open the local URL printed by Vite.
Backend Setup
Start PostgreSQL and configure the required database environment variables before launching the backend.
On Windows PowerShell:
cd backend
.\mvnw.cmd spring-boot:run
On macOS or Linux:
cd backend
./mvnw spring-boot:run
Build and Test
Frontend:
npm run build
npm run lint
Run these commands from the frontend/ directory.
Backend, from backend/:
./mvnw clean verify
On Windows, use .\mvnw.cmd clean verify.
Configuration and Security
Use .env.example as a reference for the required local configuration. Create local environment configuration as documented for the development environment.
Never commit real passwords, signing keys, or production credentials.
One small correction you should make as you progress: the README's frontend and backend commands assume you've already completed the dependency and database setup. We'll update the instructions as Docker Compose and the configuration are finalized.
