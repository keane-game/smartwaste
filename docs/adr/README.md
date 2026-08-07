# Architecture Decision Records (ADR)

Décisions techniques structurantes de la reprise du projet **Gestion automatisée des ordures ménagères / SONAGED**.
Format : [MADR](https://adr.github.io/madr/).

Cartographie documentaire complète : `../KNOWLEDGE_MAP.md`. Plan de reprise : `../../ROADMAP.md`.
Journal de ce qui est réellement fait : `../IMPLEMENTATION_LOG.md`.
Besoins effectifs pour que l'existant fonctionne : `../PLAN_MISE_EN_SERVICE.md` (ADR-0014, ADR-0015).

> **Correction du 2026-07-28.** Ces ADR étaient tous marqués « Proposé » sous une règle affirmant
> qu'« aucune implémentation n'est lancée avant passage à Accepté ». Or la moitié était implémentée :
> l'index ne renseignait plus sur ce qui est décidé. Les statuts ci-dessous reflètent désormais
> **l'état constaté dans le code**, et non l'intention. La règle d'origine — décider avant de coder —
> reste valable ; c'est sa tenue à jour qui avait été perdue.
>
> ⚠️ `../architecture-cible.md` décrit le découpage de l'**ADR-0010, remplacé**. Ne pas s'y fier
> pour la cible : voir ADR-0013.

| ADR | Titre | Statut réel | Priorité |
|---|---|---|---|
| [0001](0001-gestion-du-schema-liquibase.md) | Liquibase source unique du schéma | ✅ **Implémenté** | P0-4 |
| [0002](0002-gestion-des-secrets.md) | Externalisation et rotation des secrets | 🟠 **Partiel** — ⚠️ rotation et purge d'historique **non faites** | P0-2 |
| [0003](0003-authentification-et-jwt.md) | Authentification, mot de passe, durcissement JWT | 🟠 **Partiel** — TTL toujours à 10 j · part. remplacé par 0011 | P0-1, P0-3 |
| [0004](0004-ingestion-niveau-remplissage.md) | Ingestion du niveau de remplissage (cœur IoT) | ✅ **Implémenté**, périmètre élargi (température/humidité) | P0-5, P0-6 |
| [0005](0005-modele-alerte-depotoir-images.md) | Relation Alerte↔Dépotoir · images hors BLOB | 🟠 **Partiel** — MinIO fait, relation non exploitée | P0-6, P1-3 |
| [0006](0006-consolidation-frontend.md) | Frontend web unique | ⚠️ **Prémisse invalidée** — ne pas appliquer en l'état | P1-4 |
| [0007](0007-notifications-temps-reel.md) | Notifications temps réel (SSE) | 🟠 **Partiel** — SSE fait, FCM non fait | P2-1 |
| [0008](0008-strategie-fetch-et-multitenant.md) | Stratégie de fetch · multi-tenant | 🟠 **Partiel** — perf faite, cloisonnement non appliqué | P1-2, P2-3 |
| [0009](0009-nettoyage-dependances.md) | Nettoyage des dépendances backend | 🟠 **Quasi complet** — reste `allow-circular-references` | P1-1 |
| [0010](0010-evolution-microservices-monolithe-modulaire.md) | Monolithe modulaire en 5 modules | ⛔ **Remplacé par 0013** | — |
| [0011](0011-keycloak-identity-provider.md) | Keycloak comme fournisseur d'identité (OIDC) | ❌ **Non implémenté** — ⚠️ en tension avec le chantier « sessions » | P0 |
| [0012](0012-decouplage-entites-references-par-id.md) | Références par identifiant entre contextes | ✅ **Implémenté** | P1 |
| [0013](0013-architecture-ddd-smartwaste.md) | Architecture DDD `sn.smartwaste.collect` (8 contextes) | 🟢 **En cours, très avancé** | structurant |
| [0014](0014-amorcage-du-systeme.md) | Amorçage : racine territoriale semée · compte d'administration par l'environnement | 🔵 **Proposé** | P0 |
| [0015](0015-referentiel-geographique-projection-et-perimetre.md) | Référentiel géographique : reprojection UTM→WGS84, dédoublonnage, périmètre | ✅ **Implémenté** — ⚠️ §4 remplacé par 0018 | P0 |
| [0016](0016-geometrie-des-entites-dechets.md) | Géométrie des entités « déchets » : posée par elles, construite par le territoire | ✅ **Implémenté** — 71/71, 52/52, 156/156 | P0 |
| [0017](0017-ordre-de-passage-geographique.md) | Ordre de passage : l'urgence découpe, la géographie ordonne | ✅ **Implémenté** | P1 |
| [0018](0018-rattachement-territorial-par-la-geometrie.md) | Commune d'un point : par la position, non par le libellé — ⛔ remplace 0015 §4 | ✅ **Implémenté** — 70/71 rattachés | P0 |
| [0019](0019-remplissage-predictif-cadrage.md) | Remplissage prédictif : cadrage (G4) | 🔵 **Proposé** — cadrage seul, gate par point non ouvert | Lot 8 / G4 |

## Décisions structurantes prises **hors ADR**

Elles ne vivent aujourd'hui que dans `../IMPLEMENTATION_LOG.md` et mériteraient chacune un ADR :

- **Identifiants UUID v7** (générateur maison, sans dépendance) — motif : préserver la localité
  d'insertion dans les index B-tree de PostgreSQL, qu'un UUID v4 détruit.
- **Module `administration`** — 3ᵉ module non-contexte (après `shared` et `config`), pour les
  opérations transverses d'exploitation dont personne ne doit dépendre.
- **Ports applicatifs plutôt que repositories exposés** — un contexte publie une interface nommée
  rendant des types autonomes, jamais ses entités ni ses repositories.
- **Rattachement utilisateur↔organisation porté par `tenant`**, et non par `identity`.
- **Soft-delete généralisé** + corbeille REST + purge planifiée à 30 jours.
