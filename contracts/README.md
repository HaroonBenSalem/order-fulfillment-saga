# Event Contracts

Ce dossier est la "source de vérité" des événements échangés entre les
services. Aucun service n'importe les classes Java d'un autre service —
chaque service lit ce dossier et génère/valide ses propres DTOs.

## Règle de versioning

- Un contrat ne change JAMAIS rétroactivement. Si tu dois ajouter un champ
  obligatoire ou changer un type → tu crées `EventName.v2.json` et les deux
  versions coexistent jusqu'à ce que tous les consumers migrent.
- Ajouter un champ optionnel à un contrat existant est OK (non-breaking).
- `eventId` est obligatoire sur TOUS les events : c'est la clé d'idempotence
  que chaque consumer utilisera pour ne pas traiter le même event deux fois
  (table `processed_events`, Phase 5 du roadmap).

## Topics Kafka et mapping des events

| Topic                  | Events publiés                                    | Producer                  | Consumers                              |
|-------------------------|---------------------------------------------------|----------------------------|------------------------------------------|
| `order.events`          | OrderCreated                                       | order-service              | saga-orchestrator-service                |
| `inventory.events`      | StockReserved, StockRejected                       | inventory-service          | saga-orchestrator-service                |
| `payment.events`        | PaymentProcessed, PaymentFailed                    | payment-service            | saga-orchestrator-service                |
| `saga.commands`         | ChargePaymentCommand, ReleaseStockCommand          | saga-orchestrator-service  | payment-service, inventory-service       |
| `notification.events`   | OrderConfirmed, OrderCancelled                      | saga-orchestrator-service  | notification-service                     |

Note: l'orchestrateur ne publie JAMAIS directement sur `order.events` —
il consomme les events "de fait" (ce qui s'est passé) et publie des
"commandes" (ce qu'il veut qu'il se passe). C'est la distinction
event/command qui rend l'architecture lisible : en regardant le nom
d'un topic tu sais déjà si c'est un fait passé ou un ordre à exécuter.
