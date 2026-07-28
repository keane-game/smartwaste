# ADR-0008 — Stratégie de fetch/performance et multi-tenant

- Statut : **Accepté — partiellement implémenté**. Performance (LAZY par défaut, cascades cross-contexte retirées, index) : faite (P1-2). Multi-tenant : **fondations posées** (contexte `tenant`, `Organization`, `OrganizationMembership`, `CurrentTenantProvider`) mais ⚠️ **le discriminant `organizationId` n'est posé sur aucun agrégat métier** — le cloisonnement n'est pas appliqué (P2-3, XL).
- Date : 2026-07-11
- Priorité : P1-2, P2-3

## Contexte

Cible : plusieurs collectivités, **milliers de points de collecte**, plusieurs utilisateurs. Le modèle JPA actuel présente des choix qui ne passeront pas à l'échelle :
- `FetchType.EAGER` en chaîne : `Depotoir → Commune → Department → Region` (tous EAGER) + `OneToOne Geometry` EAGER → charger un dépotoir tire toute la hiérarchie + la géométrie ; risque de **N+1** et de payloads énormes sur les listes.
- `CascadeType.ALL` sur des `@ManyToOne` vers des **référentiels partagés** (ex. `Depotoir → TypeDepotoir`) → supprimer/persister un dépotoir peut **propager** sur le type partagé (corruption de référentiel).
- Aucune isolation par organisation (multi-tenant) : toutes les données sont globales.

## Décision

### Performance (P1-2)
1. **`FetchType.LAZY` par défaut** sur toutes les associations ; charger explicitement via **fetch joins** ou, mieux, des **projections DTO** (MapStruct) pour les listes/carte.
2. Retirer `CascadeType.ALL` des `@ManyToOne` vers des référentiels ; ne cascader que les compositions réelles (ex. `Depotoir → Geometry`). Vers un référentiel partagé : **aucune cascade de suppression**.
3. Ajouter des **index** sur les clés étrangères et les colonnes de filtrage fréquent (`communeId`, `depotoirId`, `(depotoir, measuredAt)`).

### Multi-tenant (P2-3)
Adopter le pattern **discriminant partagé** : colonne `organizationId` sur les entités « métier », filtrée par un **filtre Hibernate** activé selon l'utilisateur, plus contrôle d'accès côté sécurité. À **cadrer maintenant** (impact schéma) même si l'implémentation vient plus tard.

## Conséquences

- **+** Requêtes maîtrisées, payloads adaptés, montée en charge possible ; intégrité des référentiels préservée.
- **+** Le schéma multi-tenant anticipé évite une migration douloureuse ultérieure.
- **−** Passer en LAZY casse le code qui s'appuyait implicitement sur l'EAGER → adapter services/mappers (fetch joins/projections) et tester `LazyInitializationException`.
- **−** Le multi-tenant ajoute une contrainte transverse (filtres, tests, sécurité).

## Alternatives considérées

- **Garder EAGER** : rejeté (N+1, mémoire, ne passe pas à l'échelle).
- **Multi-tenant par base/schéma séparés** : plus fort en isolation mais plus lourd à exploiter pour de nombreuses petites collectivités ; le discriminant partagé est le meilleur compromis initial.
