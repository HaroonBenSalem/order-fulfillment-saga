# Order Fulfillment Saga — Résumé complet pour reprise de conversation

## Contexte général

Je construis un projet portfolio de microservices Java/Spring Boot appelé **Order Fulfillment Saga**, dans le but de renforcer mon CV pour des stages/PFE en Allemagne (cible : janvier 2027).

Je ne veux **pas** juste de la génération de code. Je veux comprendre chaque décision d'architecture comme le ferait un ingénieur senior.

## Style de mentoring attendu (IMPORTANT — à respecter absolument)

- Expliquer le **POURQUOI** avant le COMMENT, à chaque décision.
- Avancer **étape par étape**, jamais plusieurs étapes à la fois.
- **S'arrêter après chaque étape importante** et attendre ma confirmation avant de continuer.
- Mentionner les alternatives, les trade-offs, et la pertinence en interview pour chaque décision.
- Ne pas générer tout le projet d'un coup — petites étapes, une à la fois.
- Je communique en mélange français / anglais / arabe tunisien (Derja) — merci de t'adapter naturellement à ce registre.

## Objectif du projet

Système distribué de microservices implémentant :
- Saga Pattern (Orchestration, pas Choreography)
- Event-Driven Architecture (Kafka)
- Transactional Outbox Pattern
- PostgreSQL (database-per-service)
- Flyway (migration-first, schéma versionné)
- Docker / Docker Compose
- Testcontainers (à venir)
- Observability (à venir)
- CI/CD (à venir)

Objectif : un projet qui ressemble à un vrai backend de production, pas un CRUD basique.

---

## Roadmap globale (12 semaines)

| Phase | Contenu | Statut |
|---|---|---|
| 0 | Infra + contracts | ✅ Fait |
| 1 | order-service + Outbox Pattern | 🚧 En cours |
| 2 | inventory-service + saga-orchestrator-service (happy path) | À venir |
| 3 | payment-service + compensation | À venir |
| 4 | notification-service | À venir |
| 5 | Idempotence, dead-letter, retry, timeout | À venir |
| 6 | Observability (Prometheus/Grafana/Loki) | À venir |
| 7 | Docker images + CI/CD GitHub Actions | À venir |
| 8 | Polish portfolio (README, ADR, diagramme) | À venir |

---

## Phase 0 — TERMINÉ (pushé sur GitHub)

Repo : `github.com/HarounBenSalem/order-fulfillment-saga`

### Décisions d'architecture prises et justifiées

- **Monorepo** (pas multi-repo).
- **Saga Orchestration** (pas Choreography) : un `saga-orchestrator-service` séparé possède l'état (`saga_instance` table), décide la prochaine étape. Choisi parce que plus testable et plus facile à défendre en interview que la choreography (où chaque service réagit aux events des autres sans contrôle central).
- **Database-per-Service** : 1 seul container Postgres en local, mais **4 databases logiques séparées** : `order_db`, `inventory_db`, `payment_db`, `orchestrator_db`. Jamais de JOIN cross-database. Toute communication entre services passe exclusivement par Kafka. Raison : loose coupling, déploiements indépendants, meilleure scalabilité.
- **Kafka en KRaft mode** (pas de ZooKeeper) — plus simple, plus moderne.
- **Pas de librairie Java partagée pour les events** — contracts stockés comme **JSON Schema versionnés** dans `/contracts` (`*.v1.json`). Raison : évite le coupling fort entre services qu'une lib Java partagée créerait.
- **Transactional Outbox Pattern** pour order-service : dans **une seule transaction DB**, on sauvegarde l'Order ET un OutboxEvent. Un scheduler séparé (OutboxPublisher) lit les events non publiés et les pousse vers Kafka, puis les marque comme publiés. Raison : évite un état incohérent si Kafka est indisponible au moment du commit DB.

### Concepts déjà expliqués en détail

- **Database per Service** : isolation complète, communication uniquement par events.
- **Pourquoi Kafka plutôt que REST synchrone** : résilience, découplage, historique des events, retries plus faciles, scalabilité.
- **Saga Pattern** : remplace le ROLLBACK SQL impossible à travers plusieurs microservices par de la **compensation métier** (ex : Create Order → Reserve Stock → Payment Failed → Release Stock → Cancel Order).
- **Saga Orchestration vs Choreography** : un orchestrateur central décide explicitement de la prochaine étape, de la compensation, et de la complétion — au lieu que chaque service décide individuellement.
- **Transactional Outbox** : détaillé ci-dessus.

### Livrables Phase 0

- `docker-compose.yml` : Kafka (KRaft) + Kafka UI (port 8090) + Postgres (port 5432, user `saga_admin` / password `saga_pass`)
- `init-db/01-init-databases.sql` : crée les 4 databases (`order_db`, `inventory_db`, `payment_db`, `orchestrator_db`)
- `contracts/` : 9 events en JSON Schema (OrderCreated, StockReserved, StockRejected, ChargePaymentCommand, PaymentProcessed, PaymentFailed, ReleaseStockCommand, OrderConfirmed, OrderCancelled) + README expliquant le mapping topic/producer/consumer
- 5 dossiers placeholder par service avec README expliquant quelle phase les construit
- Repo Git initialisé via GitHub Desktop, pushé sur GitHub
- Docker Desktop installé et fonctionnel sur machine Windows

---

## Phase 1 — EN COURS : `order-service`

### Étape 1 — Génération du squelette (TERMINÉ)

Généré via start.spring.io :
- Project: Maven
- Language: Java
- Spring Boot: dernière stable 3.x (note: a fini par tourner en v4.1.0 selon les logs — Spring Boot a apparemment sorti une version 4.x)
- Java: 21
- Group: `com.haroun`
- Artifact: `order-service`
- Package: `com.haroun.orderservice` (⚠️ note : dans les logs réels, le package généré est `com.haroun.order_service` avec underscore — à vérifier/clarifier si ça pose problème plus tard)
- Dependencies: Spring Web, Spring Data JPA, PostgreSQL Driver, Spring for Apache Kafka, Flyway Migration, Validation, Lombok

### Fichiers et concepts déjà expliqués

- `pom.xml` : groupId, artifactId, version, parent, dependencies, plugins
- `src/main` vs `src/test`
- `OrderServiceApplication.java` : `@SpringBootApplication` (= `@Configuration` + `@EnableAutoConfiguration` + `@ComponentScan`), `SpringApplication.run()`
- Maven Wrapper (`mvnw` / `mvnw.cmd`) : garantit que tout le monde utilise la même version de Maven

### Décision : `application.yml` au lieu de `application.properties`

**Pourquoi YAML** :
- Structure hiérarchique (nesting) au lieu de préfixes plats répétés
- Plus facile de gérer des profils (`application-docker.yml`, `application-test.yml`)
- Convention standard dans les projets Spring Boot de production
- Trade-off honnête : YAML est sensible à l'indentation, une erreur d'espace casse le parsing silencieusement

**Action effectuée** : `application.properties` supprimé, remplacé par `application.yml` (jamais les deux en même temps — ça crée des conflits de config confus).

### Décision : `ddl-auto: validate` (PAS `update`)

Comparé : `create`, `create-drop`, `update`, `validate`.

**Conclusion (best practice pro)** : Hibernate ne doit **jamais** modifier le schéma automatiquement en production. Le schéma est géré exclusivement par **Flyway**. Hibernate se contente de **valider** que les entités correspondent au schéma existant, et échoue si ce n'est pas le cas.

### Décision : Flyway pour la gestion du schéma

Au lieu de laisser Hibernate auto-générer/modifier les tables, on écrit des fichiers de migration versionnés :
- `V1__create_orders.sql`
- `V2__create_outbox.sql`
- etc.

**Avantages** : versionné, review-able, reproductible, même schéma garanti pour chaque développeur et en production.

### `application.yml` final assemblé (bloc par bloc, avec justification de chaque ligne)

```yaml
spring:
  application:
    name: order-service

  datasource:
    url: jdbc:postgresql://localhost:5432/order_db
    username: saga_admin
    password: saga_pass
    driver-class-name: org.postgresql.Driver

  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: true
    properties:
      hibernate:
        format_sql: true

  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: false

  kafka:
    bootstrap-servers: localhost:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer

server:
  port: 8081
```

**Justifications clés données pour chaque bloc** :
- `server.port: 8081` (pas 8080) : pour éviter les conflits de port quand plusieurs microservices tournent en même temps en local (8081, 8082, 8083, 8084...).
- `datasource.url` pointe précisément sur `order_db` (pas une autre base) → respecte le principe database-per-service posé en Phase 0.
- `ddl-auto: validate` : justifié ci-dessus.
- `show-sql: true` / `format_sql: true` : utile en dev pour voir le SQL généré, **à désactiver en prod** (verbeux, perf).
- `flyway.baseline-on-migrate: false` : on part d'une base vide, donc pas besoin de baseline. Aurait été `true` si on avait un schéma déjà existant créé par un autre outil.
- `kafka` : config minimale pour l'instant — `StringSerializer` parce qu'on publie les events en JSON sérialisé en String (cohérent avec le choix Phase 0 de JSON Schema versionné, pas de lib Java partagée). Pas de config consumer encore, car `order-service` ne consomme rien pour l'instant (seulement publication de `OrderCreated` via l'Outbox). Le consumer config (pour écouter `OrderConfirmed`/`OrderCancelled`) viendra en Phase 2.

### Discussion sur la sécurité des credentials dans `docker-compose.yml`

Question posée : est-ce acceptable de pusher `docker-compose.yml` (avec `saga_admin`/`saga_pass`) sur GitHub ?

**Réponse donnée** : Oui, c'est même recommandé pour la reproductibilité du projet (n'importe qui peut cloner et lancer `docker compose up`). C'est acceptable car ce sont des credentials locaux de dev, pas de vrais secrets de prod. La règle stricte est de ne **jamais** commiter de vrais mots de passe de production, clés API, tokens, ou certificats privés. Bonne pratique à introduire plus tard : pattern `.env` (non commité, `.gitignore`) avec `${VARIABLE_NAME}` dans `docker-compose.yml` — pas encore implémenté, flaggé pour plus tard.

---

## Débogage important — chaîne de bugs d'environnement résolue (TOUT EST RÉSOLU MAINTENANT)

Cette section est purement informative — **tout fonctionne maintenant**, mais utile à savoir si jamais un problème similaire revient (ex: en relançant l'environnement après un redémarrage de machine).

### Bug 1 — Java version mismatch
- `./mvnw spring-boot:run` échouait avec `release version 21 not supported`.
- Cause : `JAVA_HOME` pointait vers Java 17 (`C:\Program Files\Java\jdk-17`), alors que le projet nécessite Java 21.
- Java 21 (Temurin) était bien installé (`C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot`), mais Maven utilise `JAVA_HOME`, pas le `java` du PATH.
- **Fix appliqué** : `$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot"` (à rendre permanent via les variables d'environnement système Windows si pas encore fait).

### Bug 2 — Syntaxe PowerShell
- `./mvnw` ne fonctionne pas sur PowerShell (syntaxe bash). Il faut utiliser `.\mvnw.cmd` (backslash + extension `.cmd`).

### Bug 3 — Volume Postgres périmé (credentials figés)
- Après le fix Java, erreur : `FATAL: authentification par mot de passe échouée pour l'utilisateur « saga_admin »`.
- Cause : `POSTGRES_USER`/`POSTGRES_PASSWORD` ne sont lus par Postgres qu'**à la toute première initialisation du volume**. Un volume Docker existant (`order-fulfillment-saga_saga_postgres_data`) contenait déjà un utilisateur créé avec d'anciens credentials, et changer `docker-compose.yml` après coup ne les met pas à jour.
- **Fix appliqué** : `docker compose down -v` (supprime aussi les volumes) puis `docker compose up -d` pour réinitialiser proprement.
- **Leçon retenue** : en dev, en cas de doute sur les credentials Postgres dans un conteneur, supprimer le volume plutôt que de chercher pendant des heures.

### Bug 4 — Conflit de port avec PostgreSQL natif Windows
- Même après le reset du volume, la même erreur d'authentification persistait.
- Test trompeur fait via `docker exec -it saga-postgres psql ...` : a connecté sans demander de mot de passe (authentification `trust` locale dans le conteneur — ce test ne valide PAS le mot de passe réseau, leçon apprise).
- Vraie cause trouvée via `netstat -ano | findstr :5432` : **deux process** écoutaient sur le port 5432 — un conteneur Docker ET un PostgreSQL natif installé comme service Windows (`postgresql-x64-18`). Spring Boot (qui tourne sur l'host, pas dans Docker) tapait sur le Postgres natif, qui n'a évidemment pas l'utilisateur `saga_admin`/`saga_pass`.
- **Fix appliqué** (PowerShell en mode Administrateur) :
  ```powershell
  Stop-Service postgresql-x64-18
  Set-Service postgresql-x64-18 -StartupType Disabled
  ```
- Vérifié après fix : `netstat -ano | findstr :5432` n'affiche plus qu'une seule ligne (celle de Docker).

### Résultat final

`order-service` démarre maintenant correctement :
- Connexion réussie à `order_db` via Docker Postgres
- Flyway valide (0 migration pour l'instant, normal — aucun fichier de migration encore créé)
- Hibernate en mode `validate` n'a rien à valider (0 entité encore créée)
- Tomcat démarre sur le port 8081

---

## État actuel exact — RIEN N'A ENCORE ÉTÉ CRÉÉ COMME CODE MÉTIER

Confirmé explicitement : pas encore d'Order Entity, pas d'Outbox Entity, pas de Controller, pas de Service, pas de Repository, pas de fichier de migration Flyway. Tout ça est intentionnel — l'objectif était de comprendre et sécuriser l'architecture/environnement d'abord.

## Prochaine étape (où on s'est arrêtés)

On allait discuter de la **conception du schéma de base de données** AVANT d'écrire le SQL de migration Flyway (`V1__...sql`). Plan pour `order-service` :

1. Concevoir le schéma : tables `orders`, `order_items`, `outbox_event` — quelles colonnes, quelles contraintes, pourquoi l'`outbox_event` est structuré d'une certaine façon (colonnes typiques attendues : id, aggregate_type, aggregate_id, event_type, payload, created_at, published_at/published boolean).
2. Une fois le schéma discuté et validé, écrire le fichier `V1__...sql` ligne par ligne avec justification.
3. Ensuite seulement : créer les entités JPA (`Order`, `OrderItem`, `OutboxEvent`) qui doivent correspondre **exactement** au schéma SQL (sinon Hibernate en mode `validate` échoue bruyamment au démarrage).

**Instruction pour la suite de la conversation** : reprendre exactement à cette étape — discuter la conception du schéma de la base de données pour `order-service` (tables `orders`, `order_items`, `outbox_event`), avec la même approche pédagogique (pourquoi avant comment, étape par étape, pause après chaque étape importante pour confirmation).
