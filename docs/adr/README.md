# Architecture Decision Records (ADR)

Décisions techniques structurantes de la reprise du projet **Gestion automatisée des ordures ménagères / SONAGED**.
Format : [MADR](https://adr.github.io/madr/). Statut ∈ {Proposé, Accepté, Rejeté, Remplacé}.

Contexte global : voir `PROJECT_ANALYSIS.md`. Plan : `ROADMAP.md`. Découpage cible en modules/microservices : `../architecture-cible.md`.

| ADR | Titre | Statut | Priorité liée |
|---|---|---|---|
| [0001](0001-gestion-du-schema-liquibase.md) | Gestion du schéma de base de données (Liquibase source unique) | Proposé | P0-4 |
| [0002](0002-gestion-des-secrets.md) | Externalisation et rotation des secrets | Proposé | P0-2 |
| [0003](0003-authentification-et-jwt.md) | Authentification, mot de passe et durcissement JWT | Proposé — partiel. remplacé par 0011 | P0-1, P0-3 |
| [0004](0004-ingestion-niveau-remplissage.md) | Architecture d'ingestion du niveau de remplissage (cœur IoT) | Proposé | P0-5, P0-6 |
| [0005](0005-modele-alerte-depotoir-images.md) | Relation Alerte↔Dépotoir et stockage des images | Proposé | P0-6, P1-3 |
| [0006](0006-consolidation-frontend.md) | Consolidation vers un frontend web unique | Proposé | P1-4 |
| [0007](0007-notifications-temps-reel.md) | Notifications temps réel (SSE) | Proposé | P2-1 |
| [0008](0008-strategie-fetch-et-multitenant.md) | Stratégie de fetch/performance et multi-tenant | Proposé | P1-2, P2-3 |
| [0009](0009-nettoyage-dependances.md) | Nettoyage des dépendances backend | Proposé | P1-1 |
| [0010](0010-evolution-microservices-monolithe-modulaire.md) | Évolution vers microservices : monolithe modulaire d'abord | ⚠️ Remplacé par 0013 | structurant |
| [0011](0011-keycloak-identity-provider.md) | Keycloak comme fournisseur d'identité (OIDC) | Proposé (remplace part. 0003) | P0 |
| [0012](0012-decouplage-entites-references-par-id.md) | Découplage des entités : références par identifiant entre contextes | Proposé | P1 |
| [0013](0013-architecture-ddd-smartwaste.md) | Architecture DDD `sn.smartwaste.collect` : 8 contextes, Clean Architecture, SaaS multi-tenant | **Accepté** (remplace 0010) | structurant |

> « Proposé » = en attente de validation. Conformément à la règle projet, aucune implémentation n'est lancée avant passage à « Accepté ».
