# ADR-0012 — Découplage des entités : références par identifiant entre contextes

- Statut : **Accepté — implémenté** (P1-7a puis ADR-0013). Toutes les références cross-contexte passent par identifiant ; les FK traversantes ont été retirées (changelog 1.5.0).
- Date : 2026-07-11
- Priorité : P1 — condition de faisabilité d'ADR-0010 ; complète [ADR-0008](0008-strategie-fetch-et-multitenant.md)

## Contexte

Demande explicite : **éviter une dépendance forte entre entités**. Le modèle JPA actuel forme un graphe **fortement couplé** dont les associations `@ManyToOne`/`@OneToOne` (souvent EAGER) **traversent les futurs bounded contexts** : `Depotoir → Commune → Department → Region`, `CircuitBalayage → Commune`, `Alert → Coordinate`, etc. Ce couplage :
- empêche l'extraction en microservices (on ne peut pas séparer les bases quand les FK se croisent) ;
- provoque N+1 et payloads massifs (cf. ADR-0008) ;
- lie le cycle de vie d'entités appartenant à des domaines distincts.

## Décision

Distinguer deux niveaux de relation :

1. **À l'intérieur d'un même agrégat / contexte** : relations JPA classiques **autorisées** quand une entité **compose** réellement l'autre et partage son cycle de vie (ex. `Depotoir → Geometry`, `Alert → Image`).
2. **Entre contextes différents** : **interdiction** des associations objet ; on référence par **identifiant** :
   ```java
   // AVANT (couplage fort, cross-contexte)
   @ManyToOne CommuneEntity commune;
   // APRÈS (couplage faible)
   Long communeId;   // ou UUID
   ```
   - **pas de FK physique** cross-contexte, **pas de cascade** cross-contexte ;
   - la résolution se fait via l'**API/service du contexte propriétaire**, ou par **données répliquées** alimentées par des **événements de domaine** (cohérence éventuelle) ;
   - on expose des **DTO**, jamais les entités ; l'intégrité cross-contexte est assurée par **validation applicative**, pas par contrainte SQL.

Règle transversale : **aucune transaction ne traverse deux contextes** — coordination par événements.

## Application au modèle existant

- `DepotoirEntity` : `communeId: Long` (au lieu de `CommuneEntity`), idem `typeDepotoirId` si `TypeDepotoir` est un référentiel d'un autre contexte ; conserver `Geometry` en relation (même contexte « point de collecte »).
- `CircuitCollect`/`CircuitBalayage` : `communeId: Long`.
- `Commune → Department → Region` : la hiérarchie territoriale reste un **seul** contexte (Référentiel territorial) → relations JPA internes conservées.
- `AlertEntity` : `depotoirId: Long` (contexte Alertes ≠ contexte Points de collecte).

## Conséquences

- **+** Extraction en microservices possible sans réécrire le modèle (chaque base est autonome).
- **+** Couplage faible, agrégats petits, meilleures perfs (moins de graphes chargés).
- **−** Perte de l'intégrité référentielle **SQL** cross-contexte → à compenser par validation applicative + événements (risque d'incohérence transitoire à assumer/documenter).
- **−** Les jointures cross-contexte disparaissent : la composition se fait côté service/API (plusieurs requêtes/projections) au lieu d'un `JOIN`.
- **−** Refactoring des mappers/services qui s'appuyaient sur la navigation d'objets.

## Alternatives considérées

- **Conserver les FK cross-contexte** : rejeté — bloque ADR-0010, maintient le couplage fort.
- **Base de données partagée entre services** (shared DB) : rejeté — anti-pattern microservices (couplage par le schéma).
- **Tout en un seul contexte** : rejeté — ne répond pas à l'objectif d'évolutivité.
