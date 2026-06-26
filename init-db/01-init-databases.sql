-- Exécuté automatiquement par Postgres au premier démarrage du container
-- (dossier monté sur /docker-entrypoint-initdb.d).
--
-- Règle d'or du Database-per-Service: order-service ne touchera JAMAIS
-- directement inventory_db, et inversement. La seule façon pour un
-- service de connaître l'état d'un autre service est via les events
-- Kafka. Si un jour tu te retrouves à écrire une requête SQL qui fait
-- un JOIN entre deux de ces bases, c'est le signal que tu as cassé
-- l'architecture -- arrête-toi et repense le flux d'events à la place.

CREATE DATABASE order_db;
CREATE DATABASE inventory_db;
CREATE DATABASE payment_db;
CREATE DATABASE orchestrator_db;

-- notification-service n'a pas besoin de DB persistante pour l'instant
-- (il consomme juste des events et logue/envoie une notif). Si plus
-- tard tu veux historiser les notifications envoyées, on lui créera
-- notification_db en Phase 4.
