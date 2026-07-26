# ADR-0005 — Relation Alerte↔Dépotoir et stockage des images

- Statut : Proposé
- Date : 2026-07-11
- Priorité : P0-6, P1-3

## Contexte

`AlertEntity` porte un `object`, `message`, `address`, un `AlertCode`, une `Coordinate` (OneToOne) et **deux** représentations d'image :
- `@Lob byte[] displayPicture` (`length = 1_000_000`) stocké **dans la table `ALERT`** ;
- une `ImageEntity` liée en OneToOne.

Mais **aucune relation `Alert → Depotoir`** : impossible de savoir quel point de collecte une alerte concerne (l'adresse est un simple texte). Or le cœur métier (ADR-0004) doit créer des alertes rattachées à un dépotoir.

Le stockage d'images en BLOB dans la table des alertes gonfle la table, alourdit les back-ups et la mémoire, et dégrade les perfs à l'échelle.

## Décision

1. **Relier** l'alerte au point de collecte : ajouter `@ManyToOne(fetch = LAZY) DepotoirEntity depotoir` à `AlertEntity` (nullable pour les alertes manuelles hors dépotoir, non-null pour les alertes automatiques de seuil).
2. **Sortir les images du BLOB** : stocker les fichiers sur un **stockage objet MinIO** (compatible S3) et ne conserver en base qu'une **référence** (clé d'objet dans `path`, URL publique dans `url`, taille, type MIME, nom) via `ImageEntity`. Déprécier puis retirer `displayPicture byte[]`. *(Implémentation P1-3 : `MinioConfig` + `ImageServiceImpl` via `io.minio:minio` ; config `sonaged.storage.minio.*` dans `application.yml` ; service MinIO dans `docker-compose.yml`.)*
3. Migrer les images BLOB existantes vers le nouveau stockage via un changeset/job de migration.

## Conséquences

- **+** Traçabilité métier : chaque alerte pointe son dépotoir → carte, tournées, historique cohérents.
- **+** Table `ALERT` légère ; back-ups et requêtes plus rapides ; diffusion d'images via URL/CDN.
- **−** Introduit une dépendance à un stockage fichier/objet (à provisionner) et une étape de migration des BLOB.
- **−** `nullable` sur `depotoir` impose de gérer les deux cas (alerte manuelle vs automatique) côté service.

## Alternatives considérées

- **Conserver le BLOB** : rejeté (ne passe pas à l'échelle « milliers de points » + photos).
- **Rendre `depotoir` obligatoire** : rejeté — casserait les alertes manuelles/citoyennes sans point de collecte identifié.
