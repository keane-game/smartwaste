# ADR-0016 — La géométrie des entités « déchets » : posée par elles, construite par le territoire

- Statut : **Accepté — implémenté** (2026-07-30)
- Date : 2026-07-30
- Priorité : P0 — débloque simultanément la carte, la traçabilité des circuits et l'ordonnancement géographique des tournées
- Complète [ADR-0015](0015-referentiel-geographique-projection-et-perimetre.md) (reprojection) et s'inscrit dans [ADR-0013](0013-architecture-ddd-smartwaste.md) §3

## Contexte

Le premier import réel du référentiel (2026-07-30, cf. `../PLAN_MISE_EN_SERVICE.md`) a chargé les données de Pikine. La vérification en base a donné ceci :

| Table | Entités avec géométrie |
|---|---|
| `commune` | **12 / 12** |
| `quartier` | **357 / 357** |
| `depotoir` | **0 / 71** |
| `circuitcollect` | **0 / 52** |
| `circuitbalayage` | **0 / 156** |

Le référentiel territorial a ses contours ; **tout le contexte « déchets » n'en a aucun**. Ce n'était pas un oubli mais une intention, écrite dans la javadoc de `WasteImportAdapter` :

> *La géométrie des circuits et des points de collecte n'est **pas** reconstruite ici : elle appartient au référentiel territorial, et l'import ne la posait déjà pas sur ces entités.*

L'intention est défendable — `GeometryEntity` et `CoordinateEntity` appartiennent bien à `territory` — mais sa conséquence ne s'était jamais manifestée, l'import n'ayant jamais tourné. Elle est triple :

1. **`GET /v1/maps/depotoirs` renvoie `[]`** alors que 71 points existent. La carte, objet central du produit, n'a rien à afficher.
2. **Aucun circuit n'est traçable** : 208 circuits réduits à un nom et une commune.
3. **Aucune tournée ne peut être ordonnée géographiquement.** `CollectionRouteServiceImpl:64` trie sur `priority().ordinal()` puis l'ancienneté de la mesure — il n'a aucune position à sa disposition. Ce n'est pas un circuit, c'est une liste de priorités.

À quoi s'ajoute une incohérence que le schéma tolérait en silence : `DepotoirEntity.geometry` est annoté `@JoinColumn(name = "geometryId", nullable = false)`, et 71 lignes portaient pourtant `NULL`. `nullable=false` n'est qu'une indication de génération de DDL ; sous `ddl-auto: validate`, rien ne l'applique. Le mapping affirmait une garantie que personne ne tenait.

## Décision

**Les entités du contexte « déchets » portent leur géométrie ; le référentiel territorial la construit.**

Concrètement, `TerritoryImportPort` — l'interface déjà publiée par `territory` pour les écritures d'import — expose une fabrique :

```java
GeometryEntity newGeometry(ImportedFeature feature);   // null si la source n'a pas de contour
```

`WasteImportAdapter` l'appelle pour ses trois entités (`importDepotoir`, `importCircuitCollect`, `importCircuitBalayage`) et attache le résultat. La persistance suit par la cascade déjà déclarée sur ces associations.

**Pourquoi la fabrique et non une reconstruction locale.** Écrire la construction une seconde fois dans `waste` ferait exister deux définitions de la même chose — l'horodatage des coordonnées, l'ordre des points, le traitement d'un contour absent — qui divergeraient au premier changement. Le contexte propriétaire du modèle reste seul à savoir comment on fabrique une géométrie ; l'autre se contente de dire qu'il en veut une.

**Pourquoi ce n'est pas une nouvelle transgression de frontière.** `waste` dépend déjà de `territory.domain.model` (`DepotoirEntity` déclare un champ `GeometryEntity`, `CircuitCollectEntity` de même) et de ses repositories, via la concession `@NamedInterface("repositories")` documentée dans `territory.application.api`. Le lien existe et `modules.verify()` l'accepte ; cet ADR ne l'élargit pas, il l'utilise. La conversion de cette concession en ports applicatifs reste une dette ouverte, distincte.

**Une entité sans contour reste importable.** `newGeometry` rend `null` quand la source n'en porte pas. Le référentiel source n'est pas parfait ; refuser l'entité entière parce que son contour manque perdrait aussi son adresse, son type et sa commune.

## Conséquences

- **+** La carte affiche enfin les 71 points de collecte et les 208 circuits.
- **+** L'ordonnancement géographique des tournées devient possible — condition de la seule fonctionnalité réellement neuve qui reste ([ADR-0017] à venir).
- **+** Une seule définition de la construction de géométrie, chez son propriétaire.
- **−** `TerritoryImportPort` rend une **entité** et non un DTO : le port publie un type persistant. C'est cohérent avec la concession existante, mais c'est aussi ce qui la prolonge d'un cran. À revoir lorsque la concession sera convertie.
- **−** Les entités déjà importées sans géométrie doivent être réimportées. Sans conséquence ici (la base de développement est jetable et les trois tables ont été vidées puis rechargées), mais à traiter si un environnement peuplé existait.
- **−** `nullable = false` sur `Depotoir.geometry` reste une affirmation non tenue par le schéma. Elle devient vraie en pratique pour les entités importées ; la rendre vraie *par contrainte* demanderait un changeset et une décision sur les dépôts saisis à la main.

## Alternatives considérées

- **Reconstruire la géométrie dans `waste`** : rejeté — duplique la construction et fait diverger deux définitions du même objet.
- **Déplacer `GeometryEntity` dans `shared`** : rejeté — la géométrie est le modèle du référentiel territorial, pas un utilitaire technique ; `shared` deviendrait un dépotoir de modèle métier.
- **Laisser les entités sans géométrie et porter la position sur le read-model de carte** : rejeté — le read-model devrait alors reconstituer une position qu'aucune table ne détient. Le problème est bien l'absence de la donnée, pas sa présentation.
- **Référencer la géométrie par identifiant (ADR-0012)** : rejeté ici — un contour n'est pas une entité d'un autre agrégat consultée de loin, c'est une **composition** : il naît, vit et meurt avec le point de collecte. ADR-0012 autorise explicitement les relations JPA à l'intérieur d'un agrégat, et cite d'ailleurs `Depotoir → Geometry` comme exemple.
