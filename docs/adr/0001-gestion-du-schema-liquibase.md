# ADR-0001 — Gestion du schéma de base de données : Liquibase comme source unique

- Statut : **Accepté — implémenté** (2026-07-11, P0-4). Liquibase est source unique, `ddl-auto=validate`.
- Date : 2026-07-11
- Priorité : P0-4

## Contexte

Le backend active **simultanément** deux mécanismes de gestion du schéma :
- Hibernate `spring.jpa.hibernate.ddl-auto=update` (dans `application.properties`) ;
- Liquibase (dépendances `liquibase-core`, `liquibase-hibernate6`, `liquibase-maven-plugin` dans `pom.xml`).

Les deux se disputent la propriété du schéma. `ddl-auto=update` modifie la base au démarrage de façon non déterministe et ne sait ni supprimer une colonne, ni gérer des données, ni versionner les changements — inacceptable pour un déploiement reproductible et pour une cible « plusieurs collectivités / milliers de points ».

## Décision

**Liquibase devient l'unique source de vérité du schéma.**
- Passer `spring.jpa.hibernate.ddl-auto=validate` (Hibernate ne fait plus que vérifier la cohérence).
- Générer un changelog initial à partir du schéma existant (`liquibase-hibernate6` / `diffChangeLog`) sous `resources/db/changelog/`.
- Toute évolution de schéma = nouveau changeset versionné, revu en PR.

## Conséquences

- **+** Migrations reproductibles, auditables, réversibles ; déploiements fiables.
- **+** Découplage schéma / code.
- **−** Discipline : chaque changement d'entité impose un changeset.
- **−** Coût initial : produire et vérifier le baseline sur une base existante (utiliser `changelogSync` pour marquer l'existant comme appliqué).

## Alternatives considérées

- **Tout Hibernate `update`** : rejeté (non déterministe, pas de rollback, dangereux en prod).
- **Flyway** : équivalent valable, mais Liquibase est **déjà** dans le projet → moindre coût.
