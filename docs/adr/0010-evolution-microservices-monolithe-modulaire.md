# ADR-0010 — Évolution vers les microservices : monolithe modulaire d'abord

- Statut : Proposé
- Date : 2026-07-11
- Priorité : structurant (cadre P1-2, P2-3) — supporte ADR-0011, ADR-0012

## Contexte

La cible produit doit pouvoir évoluer vers une architecture **microservices** (plusieurs collectivités, milliers de points, IoT, temps réel). Or le projet est jeune, porté par une petite équipe, et le cœur métier (ingestion/alertes) n'est pas encore construit. Découper en microservices **immédiatement** introduirait, sans bénéfice à ce stade : transactions distribuées, latence réseau, cohérence éventuelle à gérer partout, et une lourde charge opérationnelle (déploiement multiple, observabilité distribuée).

## Décision

Adopter un **monolithe modulaire orienté microservices** comme étape intermédiaire assumée.

1. **5 modules** de haut niveau matérialisés dès maintenant (frontières strictes, vérifiées par **Spring Modulith**). Détail dans `../architecture-cible.md` :
   - **identite-acces** → délégué à Keycloak (ADR-0011) ;
   - **referentiel-territorial** (Region, Department, Commune, Quartier, Geometry) ;
   - **collecte** — cœur opérationnel, en **4 sous-domaines cloisonnés** (`points-collecte`, `ingestion-iot`, `alertes`, `circuits-tournees`) communiquant par id/événements, pour rester extractibles individuellement (l'ingestion IoT en priorité) ;
   - **communication** (Avis, notifications e-mail/SSE/push) ;
   - **supervision** (dashboard, statistiques, historique/audit, rapports — read-side).

   > Une première version proposait 9 modules ; consolidée en 5 (trop granulaire pour l'équipe), tout en conservant les coutures d'extraction via les sous-domaines de `collecte`.
2. **Communication inter-contexte interdite en direct JPA** : par interfaces de service exposées et par **événements de domaine** (`ApplicationEventPublisher` en interne aujourd'hui, prêt à basculer sur un broker demain). Voir ADR-0012.
3. **Chaque contexte possède ses tables** ; pas de jointure SQL ni de FK physique traversant les contextes (ADR-0012).
4. **Extraction progressive** : un contexte devient un microservice le jour où il doit scaler/évoluer indépendamment. Comme il a déjà son API et son schéma isolés, l'extraction se limite à remplacer l'appel in-process par un appel réseau (REST/gRPC) et l'événement interne par un message de broker.
5. **Cible microservices** (quand justifiée) : API Gateway en frontal, communication **asynchrone par broker** (Kafka/RabbitMQ) privilégiée pour le découplage, sync REST/gRPC pour les besoins requête/réponse, observabilité distribuée (tracing).

## Conséquences

- **+** Évolutivité réelle à coût progressif ; la valeur métier est livrée vite (monolithe simple à exploiter) tout en gardant la porte des microservices ouverte.
- **+** Les frontières forcent une conception à faible couplage utile même si l'on n'extrait jamais.
- **−** Discipline requise : les frontières doivent être respectées (Spring Modulith les teste).
- **−** Refactoring du modèle actuel, qui viole aujourd'hui ces frontières (FK EAGER cross-contexte) — objet de l'ADR-0012.

## Alternatives considérées

- **Microservices immédiats** : rejeté — prématuré, surcoût opérationnel sans bénéfice à ce stade.
- **Monolithe classique (sans modules)** : rejeté — ne prépare pas l'extraction, régénère du couplage.
- **Serverless/functions** : rejeté — inadapté au domaine transactionnel et à l'équipe actuelle.
