# ADR-0015 — Référentiel géographique : projection, dédoublonnage et périmètre des fichiers

- Statut : **Proposé**
- Date : 2026-07-30
- Priorité : P0 — sans cette décision, le référentiel importé est inexploitable par la carte
- Dépend de [ADR-0014](0014-amorcage-du-systeme.md) (une région doit exister avant tout import)
- Complète [ADR-0012](0012-decouplage-entites-references-par-id.md) et le lot P1-5 (import GeoJSON)

## Contexte

Le dossier `datas/` contient **13 fichiers** de données réelles de Pikine, produits par un export ArcGIS. L'import (`GeoJsonImportServiceImpl`) en consomme **6** :

| Fichier | Consommé | Contenu |
|---|---|---|
| `LIMITE DELEGATION DEPARTEMENTALE DE PIKINE.json` | ✅ département | polygones |
| `commune.json`, `QUARTIERS.json` | ✅ communes, quartiers | polygones |
| `circuit_collect.json`, `circuit_balay.json` | ✅ circuits | tracés |
| `depotoir.json` | ✅ points de collecte | 20 points |
| `bac_rue.json`, `point_pp.json`, `CAISSES POLYBENNE.json`, `depotoir2.json`, `ppef_cp.json`, `pp_pnr_pp-pnr.json` | ❌ | 112 points |
| `pre_collecte.json` | ❌ | 6 points, schéma distinct |

L'examen des fichiers non consommés fait apparaître trois faits qui changent la nature du travail.

### 1. Les coordonnées ne sont pas des latitudes et des longitudes

L'en-tête de chaque fichier porte `spatialReference: {wkid: 32628}` — **UTM zone 28N**, un système projeté en mètres. Les valeurs le confirment : `x ≈ 239 504`, `y ≈ 1 631 116`. Or l'import les stocke telles quelles, et **dans cet ordre** :

```java
// UploadFileServiceImpl:194-196
} else if (geometry.has("x") && geometry.has("y")) {
    points.add(new ImportedFeature.GeoPoint(
            String.valueOf(geometry.getDouble("x")), String.valueOf(geometry.getDouble("y"))));
}

// ImportedFeature:58
public record GeoPoint(String latitude, String longitude) { }

// TerritoryImportAdapter:195-196
coordinate.setLatitude(point.latitude());
coordinate.setLongitude(point.longitude());
```

`x` devient donc `latitude` et `y` devient `longitude`. Deux erreurs superposées : l'unité (mètres projetés au lieu de degrés) **et** l'ordre des axes (`x` est l'abscisse, donc apparentée à la longitude). Tout point importé se retrouverait à « latitude 239504, longitude 1631116 » — hors de la Terre pour n'importe quel consommateur WGS84 (Leaflet, Google Maps, l'application Flutter). Aucune bibliothèque de projection n'est présente : `grep -rl 'proj4\|EPSG\|CRS'` sur `src/main` et `pom.xml` ne renvoie rien.

Ce défaut n'a jamais été visible parce que l'import n'a jamais tourné : il était bloqué par la dérive de schéma (corrigée le 2026-07-30) puis par l'absence de région (ADR-0014).

### 2. Les fichiers se recouvrent largement

Les 7 fichiers de points contiennent **132 entrées pour 71 coordonnées distinctes** ; **49 coordonnées apparaissent dans plusieurs fichiers**. Exemple : le point `(239504.80, 1631115.84)` figure dans `depotoir.json`, `point_pp.json` **et** `pp_pnr_pp-pnr.json`. Ce sont des exports successifs des mêmes couches SIG, filtrés différemment, pas des jeux disjoints.

Les importer naïvement gonflerait le référentiel de 86 % en doublons. Le mécanisme d'idempotence actuel n'y peut rien : il compare un `count()` global à zéro et saute l'étape entière (`force=true` duplique, faute d'upsert).

### 3. Les libellés de commune sont incohérents

Le rattachement d'un point à sa commune passe par `processWithCommune(file, "Sectection", …)`, c'est-à-dire par correspondance de **nom**. Or les mêmes communes s'écrivent différemment selon le fichier : `Diamagueune` / `Diamaguene` / `Diamagueune Sicap Mbao`, `Guinaw rail` / `Guinaw rails`, `Pikine EST` / `Pikine Est`. Une correspondance exacte laisserait une partie des points sans commune — donc invisibles dans toute vue filtrée par territoire.

## Décision

### 1. Reprojeter à l'import, stocker en WGS84

Les coordonnées sont converties **EPSG:32628 → EPSG:4326** au moment de l'import, et l'ordre des axes est rétabli (`x` → longitude, `y` → latitude). La base ne contient que du WGS84 ; le `wkid` d'origine reste porté par `GeometryEntity.spatialReference` à titre de traçabilité de la source.

La conversion est isolée dans un composant dédié du module `administration` (`CoordinateProjector`), avec une interface qui ne dit que cela : « prends un point dans le système déclaré par le fichier, rends-le en WGS84 ». Le projecteur est le seul endroit du code qui connaît un système de projection.

**Motif du choix « convertir à l'import » plutôt que « stocker brut et convertir à l'affichage »** : le modèle nomme déjà ses champs `latitude`/`longitude` — des noms qui *promettent* du WGS84. Stocker autre chose dedans oblige chaque consommateur (carte web, Flutter, read-models `DepotoirMaps`/`DepartmentMaps`, futurs calculs de distance pour les tournées) à connaître et appliquer la projection, et garantit qu'un seul l'oubliera. La conversion à l'écriture est faite une fois, par un composant testable sur des points de contrôle connus.

### 2. Dédoublonner par identité géographique

Les points sont dédoublonnés à l'import sur la **coordonnée arrondie** (précision métrique) combinée au type de mobilier. Le premier fichier qui fournit un point fait foi ; les occurrences suivantes sont comptabilisées et ignorées, et le résultat de l'import rapporte explicitement le nombre d'ignorés — un import silencieux qui perd la moitié de ses entrées est exactement ce qu'on cherche à ne plus avoir.

**Motif du choix de la coordonnée comme clé** plutôt que `OBJECTID` : les `OBJECTID` sont attribués par couche d'export et se répètent d'un fichier à l'autre pour des points différents ; la position, elle, identifie physiquement un point de collecte.

### 3. Périmètre : tous les points sont des dépôts, typés

Les 6 fichiers de points non consommés portent **le même schéma d'attributs** que `depotoir.json` (`FID, OBJECTID_1, OBJECTID, R_gion, Commune, Type_de_Mo, Adresse_de, X, Y`) et sont déjà discriminés par `Type_de_Mo` (`PP`, `PRN`, `Bac de rue`, `Caisse Polybenne`). Ils sont donc importés **par le chemin existant** (`importDepotoir`, qui résout ou crée le `TypeDepotoir` via `resolveOrCreateType(feature.text("Type_de_Mo"))`) : la liste des fichiers devient une liste, aucun analyseur nouveau n'est écrit.

`pre_collecte.json` (schéma `FID, Id, Nom, Commune`, sans type ni adresse) reste **hors périmètre** de cette décision : il décrit une organisation de pré-collecte, pas un point physique, et mérite son propre cadrage.

`MoblierUrbainEntity` **n'est pas retenu** comme cible. Son CRUD complet (dto, mapper, service, repository, contrôleur) reste une coquille vide : les données réelles disent « point de collecte typé », ce que `Depotoir` + `TypeDepotoir` représentent déjà. Statuer sur le retrait de `MoblierUrbain` relève d'une suppression de code — **validation explicite requise**, hors de cet ADR.

### 4. Rapprochement des communes par nom normalisé

La correspondance de commune se fait sur un nom **normalisé** (casse repliée, accents retirés, espaces réduits) plutôt que sur l'égalité stricte. Les points dont la commune reste introuvable sont importés **sans commune** et **comptés dans le rapport d'import**, jamais rejetés en silence.

## Conséquences

- **+** La carte affiche enfin des points au bon endroit — condition de toute démonstration du produit.
- **+** 71 points de collecte réels au lieu de 20, sans doublon.
- **+** Un seul composant connaît la projection ; les consommateurs (read-models de carte, Flutter, calculs de tournée) restent ignorants du sujet.
- **−** Une dépendance de projection entre dans le backend. À choisir minimale : la conversion UTM 28N → WGS84 est une formule fermée, implémentable sans bibliothèque si l'on refuse la dépendance — au prix d'un code mathématique à tester sérieusement.
- **−** Les données déjà importées dans un environnement quelconque seraient dans l'ancien repère. Sans conséquence aujourd'hui (toutes les tables géographiques sont vides), mais la reprise devra être traitée si un environnement est peuplé entre-temps.
- **−** Le dédoublonnage à la coordonnée fusionne deux points réellement distincts qui partageraient une position au mètre près. Le rapport d'import doit donc rester lisible pour que le cas soit repérable.

## Alternatives considérées

- **Stocker brut et convertir côté client** : rejeté — répartit la connaissance de la projection sur trois clients et les read-models ; le premier oubli produit une carte fausse sans erreur.
- **Renommer les champs en `x`/`y` et assumer l'UTM en base** : rejeté — casse les read-models de carte, l'API publique et les clients existants, pour éviter une conversion faite une fois à l'écriture.
- **N'importer que `depotoir.json`** (statu quo) : rejeté — laisse 51 points réels inutilisés alors que le fichier attendu est déjà lu par le même code.
- **Dédoublonner par `OBJECTID`** : rejeté — identifiant local à une couche d'export, non stable entre fichiers.
- **Importer les 6 fichiers dans `MoblierUrbain`** : rejeté — dupliquerait la notion de point de collecte entre deux entités, alors que `Type_de_Mo` est déjà un type de dépôt.
