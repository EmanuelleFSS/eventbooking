# Event Booking

An event booking platform built with a microservices architecture, showcasing asynchronous messaging (Kafka, RabbitMQ), polyglot persistence (PostgreSQL, MongoDB, Redis), and cloud-native practices.

> 🚧 **Status**: Under active development

## Architecture

````mermaid
graph TD
    Client[Client] --> Gateway[API Gateway<br/>Spring Cloud Gateway + Security]

    Gateway --> Event[Event Service<br/>PostgreSQL]
    Gateway --> Booking[Booking Service<br/>PostgreSQL]
    Gateway --> Search[Search Service<br/>MongoDB]
    Gateway --> Assistant[AI Assistant<br/>Claude API]

    Event -.->|cache| Redis[(Redis)]
    Event -->|Kafka | Search

    Booking --> Event
    Booking -->|Kafka | Search
    Booking -->|Kafka | Notif[Notification Service<br/>PostgreSQL]
    Booking -->|RabbitMQ | Notif

    Search -.->|lecture| Assistant
````

## Tech Stack

- **Language**: Java 21
- **Framework**: Spring Boot 4, Spring Data JPA, Spring Data MongoDB
- **Database**: PostgreSQL (Flyway migrations), MongoDB
- **Testing**: JUnit 5, Mockito, Testcontainers
- **Containerization**: Docker, Docker Compose
- **Messaging**: Apache Kafka, RabbitMQ

## Timezone Handling

- `eventDate` is stored as `OffsetDateTime` (`TIMESTAMPTZ` in PostgreSQL) — it preserves the original timezone offset of the event, since the local time of an event (e.g. "8 PM in Paris") is part of its meaning.
- `createdAt` is stored as `Instant` (`TIMESTAMPTZ` in PostgreSQL) — a pure UTC timestamp with no local timezone semantics, since it's a technical record-creation marker.
- Hibernate is explicitly configured to use UTC (`hibernate.jdbc.time_zone=UTC`) for all JDBC conversions, regardless of the host machine's default timezone.

## Getting Started

```bash
# Create a .env file at the project root (see .env.example)
docker compose up --build
```

The API will be available at `http://localhost:8080`.

## Event Service — API Endpoints

| Method | Endpoint | Description |
|--------|----------|--------------|
| POST | `/api/events` | Create a new event |
| GET | `/api/events/{id}` | Get an event by id |
| GET | `/api/events` | List events (paginated) |
| PUT | `/api/events/{id}` | Update an event |
| DELETE | `/api/events/{id}` | Delete an event |

## Booking Service — API Endpoints

| Method | Endpoint | Description |
|--------|----------|--------------|
| POST | `/api/bookings` | Create a booking (synchronously reserves seats on Event Service) |
| GET | `/api/bookings/{id}` | Get a booking by id |
| POST | `/api/bookings/{id}/cancel` | Cancel a booking (releases seats via compensation) |

## Roadmap

- [x] Phase 1 — Event Service (CRUD, PostgreSQL, tests, Docker)
- [x] Phase 2 — Booking Service + Kafka
- [ ] Phase 3 — Notification Service (RabbitMQ) + Search Service (MongoDB)
- [ ] Phase 4 — Redis cache + API Gateway + Security
- [ ] Phase 5 — Load testing (Gatling)
- [ ] Phase 6 — CI/CD + AI Assistant + Cloud deployment

## Service Details

### Event Service
* Manages the event catalog with **PostgreSQL** + **Spring Data JPA**.
* Exposes `reserve-seats` / `release-seats` endpoints, consumed synchronously by Booking Service to guarantee strong consistency on seat availability.
* Publishes catalog changes (`event.created` / `event.updated` / `event.deleted`) to **Kafka**, consumed by Search Service to build its read model.
* Redis as a cache for read operations, since popular event pages are read heavily but rarely modified.

### Booking Service
* Handles booking creation and available seats decrement, calling Event Service **synchronously** to guarantee strong consistency and avoid overselling.
* Publishes `booking.created` / `booking.cancelled` events to **Kafka** for asynchronous consumers, and confirmation email tasks to a **RabbitMQ** queue.
* **PostgreSQL** for its own data.

### Notification Service
* Consumes a **RabbitMQ** queue fed by *Booking Service* on every booking, sending a confirmation email.
* Also consumes **Kafka** `booking-events` to track booking status changes.
* **Idempotent** by design — a PostgreSQL ledger prevents duplicate processing if a message is redelivered (Kafka/RabbitMQ guarantee at-least-once delivery).
* Failed messages are routed to a **dead-letter queue** instead of being retried indefinitely.

### Search Service
* Consumes **Kafka** events from both Event Service (catalog) and Booking Service (bookings) to maintain a denormalized read model in **MongoDB** — a CQRS-style read model built from multiple event streams.
* Exposes `GET /api/search/events?q=...` for text-based search, with a live booking popularity count per event.

### API Gateway
* Spring Cloud Gateway, routing to the 3 business services.
* Spring Security with JWT to authenticate users (booking requires being logged in).

### AI Assistant
* A dedicated service that calls the Claude API to answer user questions such as "what events are happening this weekend?" based on the catalog data.