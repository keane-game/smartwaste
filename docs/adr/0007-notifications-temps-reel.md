# ADR-0007 — Notifications temps réel (SSE)

- Statut : Proposé
- Date : 2026-07-11
- Priorité : P2-1

## Contexte

La vision produit impose des **alertes en temps réel** vers les superviseurs. Aujourd'hui, la seule sortie « notification » est l'envoi d'e-mails de codes d'activation (`NotificationServiceImpl`). Aucun canal de poussée vers l'interface de supervision : celle-ci devrait faire du polling, coûteux et peu réactif.

Les alertes sont **unidirectionnelles** (serveur → superviseur), à faible fréquence relative (déclenchées sur seuil), et destinées à un back-office web (et mobile).

## Décision

Adopter **Server-Sent Events (SSE)** pour la poussée d'alertes back-office :
- endpoint `GET /v1/alerts/stream` (`text/event-stream`) exposant un flux par utilisateur authentifié ;
- le `ThresholdEvaluator` (ADR-0004) publie l'alerte → un `AlertBroadcaster` la pousse aux abonnés concernés ;
- côté mobile, conserver **push natif (FCM)** — la clé Firebase est déjà présente — comme canal complémentaire.

SSE est retenu car le besoin est **serveur → client** : plus simple que WebSocket (HTTP standard, reconnexion automatique, compatible proxies/JWT), suffisant pour ce flux.

## Conséquences

- **+** Réactivité immédiate en supervision sans polling ; implémentation légère.
- **+** Compatible avec l'architecture événementielle d'ADR-0004.
- **−** SSE = flux sortant seul ; toute interaction montante passe par les endpoints REST classiques.
- **−** À l'échelle multi-instances, nécessitera un bus partagé (Redis pub/sub) pour diffuser à tous les abonnés — à prévoir avec le multi-tenant (ADR-0008).

## Alternatives considérées

- **WebSocket/STOMP** : bidirectionnel, mais plus lourd que nécessaire pour un flux descendant.
- **Polling périodique** : rejeté (latence, charge inutile à l'échelle).
- **Uniquement e-mail/push** : insuffisant pour une supervision live sur écran.
