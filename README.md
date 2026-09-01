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

    Booking -->|Kafka | Search
    Booking -->|Kafka | Notif[Notification Service]
    Booking -->|RabbitMQ: email queue| Notif

    Search -.->|lecture| Assistant
````

## Tech Stack

- **Language**: Java 21
- **Framework**: Spring Boot 4, Spring Data JPA
- **Database**: PostgreSQL (Flyway migrations)
- **Testing**: JUnit 5, Mockito, Testcontainers
- **Containerization**: Docker, Docker Compose

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

## Roadmap

- [x] Phase 1 — Event Service (CRUD, PostgreSQL, tests, Docker)
- [ ] Phase 2 — Booking Service + Kafka
- [ ] Phase 3 — Notification Service (RabbitMQ) + Search Service (MongoDB)
- [ ] Phase 4 — Redis cache + API Gateway + Security
- [ ] Phase 5 — Load testing (Gatling)
- [ ] Phase 6 — CI/CD + AI Assistant + Cloud deployment

## Service Details

### Event Service
* Manages the event catalog with **PostgreSQL** + **Spring Data JPA**.
* Redis as a cache for read operations, since popular event pages are read heavily but rarely modified.

### Booking Service
* Handles booking creation and available seats decrement, using optimistic locking to manage concurrency on reservations.
* **PostgreSQL** + **Kafka**.

### Notification Service
* Consumes a **RabbitMQ** queue fed by the *Booking Service* on every booking.
* Sends a confirmation email, with failure handling.

### Search Service
* Consumes **Kafka** events to maintain a denormalized view in **MongoDB**.

### API Gateway
* Spring Cloud Gateway, routing to the 3 business services.
* Spring Security with JWT to authenticate users (booking requires being logged in).

### AI Assistant
* A dedicated service that calls the Claude API to answer user questions such as "what events are happening this weekend?" based on the catalog data.