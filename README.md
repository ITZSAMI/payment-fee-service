# Payment Fee Service

A mock payment & fee processing backend, built to mirror the kind of service a
fintech backend team maintains: transactions come in, fees are calculated
using tiered business rules, and invoices are generated from the result.

Built with **Kotlin + Spring Boot**, exposing both **REST** and **GraphQL**
APIs, containerized with **Docker**, and provisioned on **AWS via Terraform**.

## Why this project

I built this to get hands-on with the stack used by fintech backend teams
(Kotlin, Spring Boot, REST/GraphQL, Docker, AWS, Terraform) — rather than just
listing these as "familiar with" on a CV.

## Architecture

```
Client
  │
  ├── REST API  ──┐
  └── GraphQL API ─┼──> Service Layer ──> JPA Repositories ──> PostgreSQL / H2
                    │        │
                    │        └── FeeCalculatorService (pure business logic)
                    │
              Controllers
```

- **`controller/`** — REST controllers + GraphQL controller (thin, delegate to services)
- **`service/`** — business logic: `TransactionService`, `InvoiceService`, `FeeCalculatorService`
- **`model/`** — JPA entities (`Transaction`, `Invoice`) and the `FeeRule` strategy hierarchy
- **`repository/`** — Spring Data JPA repositories
- **`dto/`** — request/response payloads, decoupled from entities
- **`exception/`** — custom exceptions + a `@RestControllerAdvice` handler for clean error responses

## Fee calculation logic

`FeeCalculatorService` applies a tiered percentage fee plus a flat per-transaction
fee, similar to common card-processor pricing models:

| Amount            | Rate  |
|--------------------|-------|
| < €100             | 2.0%  |
| €100 – €1000       | 1.5%  |
| > €1000            | 1.0%  |

...plus a flat **€0.30** fee per transaction. This is the most heavily tested
part of the codebase — see `FeeCalculatorServiceTest`.

## Running locally

> **First-time setup:** this repo ships `gradle/wrapper/gradle-wrapper.properties`
> but not the wrapper scripts/jar (binary files don't transfer well outside a
> real Gradle run). If you have Gradle installed locally, generate them once with:
> ```bash
> gradle wrapper --gradle-version 8.9
> ```
> After that, use `./gradlew` as shown below. Alternatively, just use a
> locally installed Gradle directly (`gradle bootRun` instead of `./gradlew bootRun`).

### Option 1 — plain Gradle (uses in-memory H2, zero setup)

```bash
./gradlew bootRun
```

App runs on `http://localhost:8080`. H2 console available at `/h2-console`.

### Option 2 — Docker Compose (app + PostgreSQL)

```bash
docker-compose up --build
```

## Example requests

**Create a transaction**
```bash
curl -X POST http://localhost:8080/transactions \
  -H "Content-Type: application/json" \
  -d '{"merchantId": "merchant-1", "amount": 500.00, "currency": "EUR"}'
```

**Generate an invoice for it**
```bash
curl -X POST http://localhost:8080/transactions/{id}/invoice
```

**GraphQL query** (via `/graphiql` or `POST /graphql`)
```graphql
query {
  invoice(id: "...") {
    grossAmount
    feeAmount
    netAmount
  }
}
```

## Testing

```bash
./gradlew test
```

Unit tests cover:
- `FeeCalculatorService` — all tiers, edge cases, invalid input (parameterized tests)
- `InvoiceService` — mocked dependencies via MockK, verifying correct fee/net calculation and persistence calls

## Infrastructure (Terraform)

The `terraform/` directory provisions the AWS resources this service would use
in a real deployment:
- An S3 bucket for storing generated invoice documents
- An ECR repository for the app's Docker image

```bash
cd terraform
terraform init
terraform plan
```

*(Not applied by default — this is included to demonstrate IaC familiarity, not as a live deployment.)*

## Tech stack

Kotlin · Spring Boot · Spring Data JPA · Spring GraphQL · PostgreSQL · H2 ·
Docker · Docker Compose · Terraform · AWS (S3, ECR) · JUnit 5 · MockK

## Possible next steps

- Idempotency keys on transaction creation
- Webhook notifications on invoice generation
- Currency conversion support
- CI pipeline (GitHub Actions) running tests + Docker build on push
