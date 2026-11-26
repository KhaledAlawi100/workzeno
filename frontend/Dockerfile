
# ============================================================
# Stage 1: Build the React application
# ============================================================
FROM node:22-alpine AS build

WORKDIR /app

# Install dependencies using the committed lock file
COPY package.json package-lock.json ./
RUN npm ci

# Copy frontend source
COPY . .

# Vite embeds this URL into the generated JavaScript.
# It must be supplied when building the image.
ARG VITE_API_BASE_URL
ENV VITE_API_BASE_URL=${VITE_API_BASE_URL}

RUN test -n "$VITE_API_BASE_URL" \
    && npm run build


# ============================================================
# Stage 2: Serve the application with unprivileged Nginx
# ============================================================
FROM nginxinc/nginx-unprivileged:stable-alpine AS runtime

# Replace the default Nginx server configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy the compiled React application
COPY --from=build --chown=101:101 /app/dist /usr/share/nginx/html

EXPOSE 8080

# Verify that Nginx is serving the application
HEALTHCHECK --interval=30s \
    --timeout=5s \
    --start-period=10s \
    --retries=3 \
    CMD ["wget", "--quiet", "--tries=1", "--spider", "http://127.0.0.1:8080/healthz"]

