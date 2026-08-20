# Event Booking

Plateforme de réservation d’événements (concerts, ateliers, conférences).

>🚧 Application en cours de développement

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

## Détail des services

### Event Service
* Réaliser la gestion du catalogue d'événements avec **PostreSQL** + **Spring Data JPA**.
* Redis en cache pour les consultations, vu que les pages d’événements populaires sont consultées en masse, cependant elles sont peu modifiées.

### Booking Service
* Création de réservations, décrémentation des places disponibles, en utilisant le verrouillage optimiste pour gérer la concurrence sur les réservations.
* **PostreSQL** + **Kafka**.

### Notification Service
* Consomme une file **RabbitMQ** alimentée par *Booking Service* à chaque réservation.
* Envoie un email de confirmation, avec gestion des échecs.

### Search Service
* Consomme les événements **Kafka** pour maintenir une vue dénormalisée dans **MongoDB**.

### API Gateway
* Spring Cloud Gateway, routage vers les 3 services métier.
* Spring Security avec JWT pour authentifier les utilisateurs (réservation nécessite d’être connecté).

### AI Assistant
* Un service dédié, qui appelle l’API Claude pour répondre aux questions utilisateur comme “quels événements y a-t-il ce week-end ?” en s’appuyant sur les données du catalogue.

