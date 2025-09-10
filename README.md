# Inventory Service

Inventory Service exposing minimal APIs to support order creation. 
Provides reservation and confirmation of Items with idempotency, backed by PG transactions. Designed for high concurrency via virtual threads.

---

## 🛠️ Tech Stack
- Java 24
- Spring Boot 3.5.x 
- Springdoc OpenAPI (Swagger UI)
- Testcontainers (Postgres for integration tests)
- Docker

---

## 🚀 Getting Started

### Prerequisites
- Java 24  
- Maven 3.9+  
- Docker & Docker Compose  

### Run Locally
```bash
mvn spring-boot:run
