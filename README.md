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

### Run Locally
```bash
mvn spring-boot:run

### Swagger UI
http://localhost:8080/swagger-ui.html

### Example API Calls
1. Reserve Items
```bash
curl -X POST http://localhost:8081/inventory/reservations \
  -H "Content-Type: application/json" \
  -d '{
        "reservationId": "RES-12345",
        "items": [
          { "productCode": "PROD-001", "qty": 1 }
        ],
        "ttlSec": 600
      }'
2. Confirm Items
```bash
curl -X POST http://localhost:8081/inventory/reservations/RES-12345/confirm

### Notes
In production, schema management would be handled by Liquibase. For demo purposes, Hibernate auto-ddl is used.
