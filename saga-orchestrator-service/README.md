# saga-orchestrator-service
LE coeur du projet -- construit progressivement en **Phases 2 à 3**.
Table saga_instance dans orchestrator_db (saga_id, order_id, status,
current_step). State machine qui décide quelle commande envoyer
ensuite et qui déclenche la compensation en cas d'échec.
