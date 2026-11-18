# Payment Fee Service

A mock payment & fee processing backend, built to mirror the kind of service a
fintech backend team maintains: transactions come in, fees are calculated
using tiered business rules, and invoices are generated from the result.

Built with **Kotlin + Spring Boot**, exposing both **REST** and **GraphQL**
APIs, containerized with **Docker**, with its **AWS** infrastructure
(S3, ECR) defined in **Terraform**.

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

| Amount               | Rate  |
|----------------------|-------|
| below €100           | 2.0%  |
| €100 to €999.99      | 1.5%  |
| €1000 and above      | 1.0%  |

...plus a flat **€0.30** fee per transaction. The total fee is rounded to
2 decimal places (half-up), and the net amount is gross minus fee. This is
the most heavily tested part of the codebase — see `FeeCalculatorServiceTest`.

The tier rate applies to the **whole** amount, not just the part above each
threshold (unlike income-tax brackets). That keeps the rule simple for
merchants to read, but it creates a step at each boundary: a €999.99
transaction pays €15.30 in fees, while a €1000.00 one pays €10.30.

The minimum transaction amount is **€0.50**, so the flat fee can never exceed
the payment itself. Amounts may have at most 2 decimal places, and currencies
must be 3-letter uppercase ISO codes (e.g. `EUR`).

## Business rules

Transactions follow a simple state machine:

```
PENDING ──→ COMPLETED
   └──────→ FAILED   (status defined; no API endpoint sets it yet)
```

- Only a `PENDING` transaction can be completed.
- Only a `COMPLETED` transaction can be invoiced, and only once. Duplicate
  invoices are blocked both in the service layer and by a unique database
  constraint (which also covers concurrent requests).

Errors use a consistent JSON shape: `400` for invalid input, `404` for unknown
ids, `409` for state conflicts (e.g. invoicing twice).

## Running locally

Requires Java 17+. The Gradle wrapper is included, so no Gradle install is needed.

### Option 1 — plain Gradle (uses in-memory H2, zero setup)

```bash
./gradlew bootRun
```

App runs on `http://localhost:8080`. H2 console available at `/h2-console`.

### Option 2 — Docker Compose (app + PostgreSQL)

```bash
docker compose up --build
```

The app waits for PostgreSQL's health check before starting. Database
credentials default to local dev values and can be overridden with the
`POSTGRES_USER` / `POSTGRES_PASSWORD` environment variables. The H2 console
and GraphiQL are disabled in this profile.

## Example requests

**Create a transaction**
```bash
curl -X POST http://localhost:8080/transactions \
  -H "Content-Type: application/json" \
  -d '{"merchantId": "merchant-1", "amount": 500.00, "currency": "EUR"}'
```

**Mark it as completed** (only completed transactions can be invoiced)
```bash
curl -X POST http://localhost:8080/transactions/{id}/complete
```

**Generate an invoice for it**
```bash
curl -X POST http://localhost:8080/transactions/{id}/invoice
```

**GraphQL query** (via `/graphiql` or `POST /graphql`; returns `null` for unknown ids)
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
- `FeeCalculatorService` — all tiers, the minimum amount boundary, invalid input (parameterized tests)
- `InvoiceService` — fee/net calculation, duplicate-invoice and status rules, using MockK
- `TransactionService` — allowed and rejected status transitions
- `GraphQLController` — results and `null` for unknown ids
- `CreateTransactionRequest` — validation of amount and currency

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

## Known limitations

- Fee rules are defined in EUR, but any 3-letter currency code is accepted,
  and invoices don't store a currency yet.
- `FAILED` exists as a status, but there is no endpoint to fail a transaction.
- Status changes have no optimistic locking (`@Version`), so two concurrent
  requests on the same transaction could both read `PENDING`.
- The schema is created by Hibernate (`ddl-auto: update`) rather than
  versioned migrations.

## Possible next steps

- Restrict currencies to `EUR` (or add per-currency fee rules) and store the
  currency on each invoice
- `POST /transactions/{id}/fail` endpoint, with optimistic locking on status changes
- Versioned schema migrations with Flyway
- Idempotency keys on transaction creation
- Webhook notifications on invoice generation
- CI pipeline (GitHub Actions) running tests + Docker build on push
