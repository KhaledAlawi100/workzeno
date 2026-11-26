
# ============================================================
# Stage 1: Build the Spring Boot application
# ============================================================
FROM eclipse-temurin:21-jdk-jammy AS build

WORKDIR /app

# Copy Maven wrapper and project configuration first
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

RUN chmod +x ./mvnw

# Copy application source
COPY src/ src/

# Build executable Spring Boot JAR
# Tests are executed separately by the CI pipeline.
RUN ./mvnw -B -DskipTests package


# ============================================================
# Stage 2: Run the application
# ============================================================
FROM eclipse-temurin:21-jre-jammy AS runtime

WORKDIR /app

# Install curl for the container health check
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

# Create a dedicated non-root user
RUN groupadd --system --gid 10001 workzeno \
    && useradd --system \
        --uid 10001 \
        --gid workzeno \
        --home-dir /app \
        --shell /usr/sbin/nologin \
        workzeno

# Copy the executable JAR from the build stage
COPY --from=build --chown=workzeno:workzeno \
    /app/target/workzeno-0.0.1-SNAPSHOT.jar \
    /app/app.jar

# Use the production profile by default
ENV SPRING_PROFILES_ACTIVE=prod

# JVM container memory configuration
ENV JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=25.0 -XX:+ExitOnOutOfMemoryError"

# Default application port; Render supplies PORT at runtime
EXPOSE 8080

# Run as non-root
USER workzeno

# Check the Spring Boot readiness endpoint
# Check the Spring Boot readiness endpoint
# Check the Spring Boot readiness endpoint
HEALTHCHECK --interval=30s \
    --timeout=5s \
    --start-period=90s \
    --retries=3 \
    CMD ["sh", "-c", "curl --fail --silent http://127.0.0.1:${PORT:-8080}/actuator/health/readiness || exit 1"]

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
