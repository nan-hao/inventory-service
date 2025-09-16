# Inventory Service

[![Java Build](https://github.com/nan-hao/inventory-service/actions/workflows/java-build.yml/badge.svg?branch=main)](https://github.com/nan-hao/inventory-service/actions/workflows/java-build.yml)
![Java](https://img.shields.io/badge/Java-24-007396?logo=java)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=nan-hao_inventory-service&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=nan-hao_inventory-service)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=nan-hao_inventory-service&metric=coverage)](https://sonarcloud.io/summary/new_code?id=nan-hao_inventory-service)

Inventory Service exposing minimal APIs to support order creation.
Provides reservation and confirmation of Items with idempotency, backed by PG transactions. Designed for high concurrency via virtual threads.

---

## Tech Stack
- Java 24
- Spring Boot 3.5.x
- Springdoc OpenAPI (Swagger UI)
- Testcontainers (Postgres for integration tests)
- Docker
---

## Getting Started

### Prerequisites
- Java 24
- Maven 3.9+
- Docker & Docker Compose

### Configuration
Datasource settings are environment specific (with local-friendly defaults):

- `SPRING_DATASOURCE_URL` (default `jdbc:postgresql://localhost:5433/inventory`)
- `SPRING_DATASOURCE_USERNAME` (default `inv`)
- `SPRING_DATASOURCE_PASSWORD` (default `inv`)

Server runs on port `8081` (change via `server.port`).

### Local Maven Settings (GitHub Packages)
To build and run the app locally (outside Docker), Maven must authenticate to your GitHub Packages registry to resolve the parent POM and internal starters.

Create `~/.m2/settings.xml` with the following content (do not commit this file):

```
<settings>
  <servers>
    <server>
      <!-- Must match the <repository> id in this project's pom.xml -->
      <id>github-recipeforcode-platform</id>
      <username>your-github-username</username>
      <!-- A Personal Access Token with read:packages scope -->
      <password>ghp_your_token_with_read_packages</password>
    </server>
  </servers>
</settings>
```

Notes:
- Use a classic PAT with `read:packages`. Fine-grained tokens also work if they grant access to the package repository.
- The `<id>` must be exactly `github-recipeforcode-platform` to match this repo’s `<repositories>` and `<pluginRepositories>`.
- After creating the file, verify with: `mvn -B -ntp -DskipTests dependency:resolve`

### Project-local Maven settings (committed, sanitized)
This repo commits a sanitized `.mvn/settings.xml` that references environment variables instead of hardcoding credentials:

```
<settings>
  <servers>
    <server>
      <id>github-recipeforcode-platform</id>
      <username>${env.GITHUB_PACKAGES_USER}</username>
      <password>${env.GITHUB_PACKAGES_TOKEN}</password>
    </server>
  </servers>
</settings>
```

Usage:
- Local Maven: export the variables before building, e.g.
  - `export GITHUB_PACKAGES_USER=your-github-username`
  - `export GITHUB_PACKAGES_TOKEN=ghp_your_token_with_read_packages`
  - `mvn -DskipTests verify`
- Docker Compose build: put the variables in a `.env` file in repo root (gitignored):
  - `GITHUB_PACKAGES_USER=your-github-username`
  - `GITHUB_PACKAGES_TOKEN=ghp_your_token_with_read_packages`
  - Then `docker compose up -d --build`

Notes:
- Docker builds still use BuildKit to mount `.mvn/settings.xml` as a secret; credentials are injected via build args and not written into image layers.
- CI injects `${{ github.actor }}` and `${{ secrets.GITHUB_TOKEN }}` as build args and also mounts a generated settings.xml for Maven.

### Run Locally
```bash
mvn spring-boot:run
```

### Docker
Build the image:
```bash
docker build -t inventory-service:local .
```

Run the container, pointing to your DB:
```bash
docker run --rm -p 8081:8081 \
  -e SPRING_DATASOURCE_URL="jdbc:postgresql://db-host:5432/inventory" \
  -e SPRING_DATASOURCE_USERNAME="inv" \
  -e SPRING_DATASOURCE_PASSWORD="inv" \
  --name inventory-service inventory-service:local
```

### Docker Compose (Postgres + App)
Authentication for GitHub Packages is required to resolve the parent POM during the Docker build. Compose forwards your local Maven `settings.xml` to the build using BuildKit secrets.

1) Ensure your local `~/.m2/settings.xml` is configured (see section above).

2) Build and start:

```bash
docker compose up -d --build
```
- App: http://localhost:8081
- DB: `localhost:5433` (db: `inventory`, user/pass: `inv`/`inv`)

Logs and teardown:
```bash
docker compose logs -f
docker compose down -v
```

Notes:
- The Docker build uses BuildKit secrets to mount your `~/.m2/settings.xml` at build time; credentials are not written into image layers.
- If `docker compose` can't find your settings file, ensure `${HOME}/.m2/settings.xml` exists and you are using a recent Docker/Compose version with BuildKit enabled.

Why credentials are required: the project inherits from and depends on internal artifacts published to GitHub Packages under your org. Maven must authenticate to download them during the Docker build.

- Parent POM: `com.recipeforcode:recipeforcode-parent`
- Starters: `com.recipeforcode:recipeforcode-starter-observability`, `com.recipeforcode:recipeforcode-starter-openapi`

### Quick Verify

After `docker compose up -d --build`:

- Health: `curl -s http://localhost:8081/actuator/health | jq` (expect `"status":"UP"`)
- API Docs (JSON): `curl -s http://localhost:8081/v3/api-docs | jq '.info'`
- Swagger UI: open http://localhost:8081/swagger-ui.html
- Prometheus metrics: scrape `http://localhost:8081/actuator/prometheus`.
- Reserve (idempotent):
  ```bash
  curl -sS -X POST http://localhost:8081/inventory/reservations \
    -H "Content-Type: application/json" \
    -d '{"reservationId":"RES-LOCAL-1","items":[{"productCode":"PROD-001","qty":2}],"ttlSec":300}' | jq
  ```
- Confirm:
  ```bash
  curl -sS -X POST http://localhost:8081/inventory/reservations/RES-LOCAL-1/confirm | jq
  ```

### Observability
  Example Prometheus scrape config:
  
  ```yaml
  scrape_configs:
    - job_name: 'inventory-service'
      static_configs:
        - targets: ['host.docker.internal:8081']
  ```

- JSON logging: logs are JSON-formatted and include a correlation id in MDC.
- Correlation ID: send `X-Correlation-Id` header; if missing, the service generates one and echoes it back.

### Constraints
- Hibernate `ddl-auto` is `update` for convenience. For production, use managed migrations (e.g., Liquibase/Flyway).
- No authentication/authorization is enforced by default. Do not expose this service publicly without upstream protection.
