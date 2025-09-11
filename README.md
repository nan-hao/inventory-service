# Inventory Service

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
- No authentication/authorization is enforced by default. Do not expose this service publicly without upstream protection. A minimal API key filter exists but is disabled unless `api.security.api-key` is configured; proper auth (e.g., JWT via Spring Security or gateway enforcement) will be added in a future iteration.
