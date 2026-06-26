# Distributed Order Fulfillment & Inventory Saga

Système de traitement de commandes basé sur le pattern **Saga
(orchestration)**, en Java/Spring Boot + Kafka. Projet portfolio
démontrant : transactions distribuées, compensation, idempotence,
Database-per-Service, observability.

## Pourquoi orchestration et pas choreography

Une saga orchestrée a un service dédié (`saga-orchestrator-service`)
qui possède explicitement l'état de chaque transaction métier
(`saga_instance` table) et décide de la prochaine commande à émettre.
Alternative : la choreography, où chaque service réagit aux events des
autres sans coordinateur central. Choisi l'orchestration ici parce que
l'état est centralisé et testable (un seul endroit où lire "où en est
cette commande"), et parce que c'est plus facile à expliquer et
défendre en entretien technique qu'un flux choreographié implicite.

## Architecture

```
                    ┌──────────────────┐
   POST /orders ──▶ │  order-service    │──▶ order.events (OrderCreated)
                    └──────────────────┘            │
                                                     ▼
                    ┌──────────────────────────────────────┐
                    │      saga-orchestrator-service         │
                    │   (state machine, saga_instance table) │
                    └──────────────────────────────────────┘
                       │  ChargePaymentCommand     │  ReleaseStockCommand
                       │  (saga.commands)          │  (compensation)
                       ▼                           ▼
              ┌──────────────────┐      ┌──────────────────┐
              │  payment-service  │      │ inventory-service │
              └──────────────────┘      └──────────────────┘
                       │ payment.events           │ inventory.events
                       └─────────────┬─────────────┘
                                     ▼
                    saga-orchestrator-service décide :
                    OrderConfirmed ou OrderCancelled
                                     │
                                     ▼
                       ┌────────────────────────┐
                       │ notification-service     │
                       └────────────────────────┘
```

Voir `contracts/README.md` pour le détail des topics et le mapping
exact event ⟷ producer ⟷ consumer.

## Database-per-Service

4 databases logiques dans un seul container Postgres local
(`order_db`, `inventory_db`, `payment_db`, `orchestrator_db`). Aucune
requête cross-database autorisée — toute communication inter-service
passe par Kafka.

## Phase 0 — Lancer l'infrastructure

```bash
docker compose up -d
```

Vérifier que tout tourne :

```bash
docker compose ps
```

Kafka UI disponible sur http://localhost:8090 — tu devras voir le
cluster `local` connecté. Postgres écoute sur `localhost:5432`
(user: `saga_admin`, password: `saga_pass`), avec les 4 databases déjà
créées par `init-db/01-init-databases.sql`.

Rien à coder encore à ce stade — Phase 0 = infra qui démarre proprement
+ contrats d'events figés. Le premier code Java arrive en Phase 1
(`order-service`).

## Roadmap

| Phase | Contenu |
|---|---|
| 0 | Infra (ce repo) + contracts |
| 1 | order-service + Outbox Pattern |
| 2 | inventory-service + saga-orchestrator-service (happy path) |
| 3 | payment-service + compensation |
| 4 | notification-service |
| 5 | Idempotence, dead-letter, retry, timeout |
| 6 | Observability (Prometheus/Grafana/Loki) |
| 7 | Docker images + CI/CD GitHub Actions |
| 8 | Polish portfolio (README, ADR, diagramme) |
