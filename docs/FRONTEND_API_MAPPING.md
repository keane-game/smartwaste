# FRONTEND_API_MAPPING.md — Contrats backend → couche API frontend

> Cartographie exhaustive des 38 contrôleurs REST / ~153 endpoints du backend, organisée par
> service API frontend cible (section 29 du cahier des charges de refonte). Base pour construire
> `core/api/*.service.ts` — typé, un service par domaine, jamais d'appel `HttpClient` direct dans
> un composant. Généré le 2026-08-09 par lecture exhaustive du code source (pas de supposition).

## Modèle de sécurité — ce qu'un service API doit savoir

Deux mécanismes **cumulatifs** (les deux s'appliquent si les deux existent) :
- **Niveau URL** (`SecurityConfiguration.java`) — première règle qui correspond gagne :
  - Public (`permitAll`) : `/auth/**`, `GET /data/**`, `POST /v1/measurements`, `POST /v1/vehicle-positions`, `/swagger-ui/**`, `GET /actuator/health`
  - Authentifié simple : `POST /avis`, `GET /avis/mine`, `/v1/collection-subscriptions/**`, `POST/DELETE /v1/device-tokens`, `POST /v1/quizzes/*/answers`
  - `ADMIN`/`SUPER_ADMIN` : `/v1/users/**`, `/v1/authorities/**`, `/v1/deletions/**`, `/v1/admin/**`, `GET /v1/alerts/stream`, `POST /data/**`
  - `SUPERVISEUR`/`ADMIN`/`SUPER_ADMIN` : `/v1/supervision/**`
  - `AGENT`/`ADMIN`/`SUPER_ADMIN` : `POST .../collection-routes/stops/*/collected|inaccessible`
  - `TECHNICIEN_IOT`/`ADMIN`/`SUPER_ADMIN` : `POST|DELETE /v1/devices/**`
  - Écriture générique (`POST/PUT/PATCH/DELETE /v1/**` non listée ci-dessus) : `ADMIN`/`SUPER_ADMIN`
  - **Lecture générique (`GET /v1/**` non listée ci-dessus) : ouverte à tout compte authentifié** — c'est le cas de tout le référentiel territorial (communes, quartiers, régions, départements, géométries) et de la plupart des ressources `waste` (circuits, dépotoirs, mobilier, types).
- **Niveau méthode** (`@PreAuthorize`) — permissions fines par acte métier (`VIEW_COLLECTION_ROUTE`,
  `DECLARE_COLLECTION`, `MANAGE_DEVICES`, `MANAGE_ROLE`, `SEND_AWARENESS`), à vérifier
  indépendamment du rôle — un rôle qui passe le filtre URL peut encore se faire refuser par une
  permission manquante.

**Deux authentifications distinctes coexistent** : JWT `Authorization: Bearer` pour les comptes
utilisateur, en-tête `X-Device-Key` pour les capteurs/traceurs IoT (`POST /v1/measurements`,
`POST /v1/vehicle-positions`) — ces deux endpoints ne sont **jamais** appelés par le frontend web,
seulement par du matériel/simulateur, à exclure de la couche API Angular.

## Codes HTTP — désormais uniformes (2026-08-10)

30 incohérences corrigées sur 15 contrôleurs (audit exhaustif du 2026-08-10) : tout `POST`
créateur répond **201 CREATED**, tout `PUT` modificateur répond **200 OK** — plus de
`createXxx` en 200 ni d'`updateXxx` en 201 à absorber côté client. Deux cas de non-conformité HTTP
réelle ont aussi été fermés (`AuthorityController.updateAuthority` et `AlertController.updateAlert`
renvoyaient un corps avec un statut 204, qui l'interdit).

## Services API cibles

### `AuthApiService` — base `/auth` (public)
| Action | Endpoint | Notes UI |
|---|---|---|
| Inscription | `POST /auth/register` | Le rôle envoyé par le client est ignoré côté serveur (toujours `USER`) — ne pas proposer de sélecteur de rôle à l'inscription |
| Activation | `POST /auth/activation` | `{code}` |
| Connexion | `POST /auth/authenticate` | `{username, password}` → `{bearer, refresh}` |
| Rafraîchissement | `POST /auth/refresh` | `{refresh}` → nouveau couple ; **le refresh tourne à chaque appel**, toujours stocker celui qui revient |
| Déconnexion | `POST /auth/logout` | Idempotent, jamais d'erreur même sans jeton |

### `UserApiService` — base `/v1/users` (ADMIN/SUPER_ADMIN, lecture incluse)
CRUD standard + `GET /v1/users/s` (liste complète, non paginée) vs `GET /v1/users?page=&size=`
(paginée). **Attention** : `POST`/`PUT` répondent `200`/`201` malgré `@ResponseStatus` parfois
incohérent avec la doc Swagger — ne pas se fier au code HTTP seul pour distinguer create/update
côté client, se fier au verbe.

### `AuthorityApiService` — base `/v1/authorities` (permission `MANAGE_ROLE`)
CRUD des rôles/permissions. Écran sensible : un `ADMIN` peut ici s'auto-attribuer `SUPER_ADMIN`
(risque déjà documenté côté backend, produit assumé) — l'UI doit au moins le rendre visible
(confirmation renforcée avant d'assigner une permission à soi-même), pas nécessairement le bloquer.

### `TerritoryApiService` — regroupe 6 contrôleurs
`RegionController` (`/v1/regions` — CRUD complet depuis le 2026-08-10 : `updateRegion`/
`deleteRegion` existaient déjà côté service mais en stubs jamais écrits ni exposés, corrigé),
`DepartmentController` (`/v1/departments`), `CommuneController` (`/v1/communes`),
`QuartierController` (`/v1/quartiers`), `GeometryController` (`/v1/geometries`),
`CoordinateController` (`/v1/coordinates` — suppression alignée sur `DELETE /{id}` depuis le
2026-08-10, l'ancien chemin `/delete/coordinate/{id}` n'existe plus). **Pagination désormais
uniforme depuis le 2026-08-11** : les 6 contrôleurs ont tous `?page=&size=` (obligatoires) **et**
`/s` (liste complète), même patron — plus besoin de distinguer deux formes de réponse par
ressource. Department/Region **exposent encore des entités JPA brutes** (`communes`, `departments`
imbriqués) — ne pas s'appuyer sur cette profondeur, elle peut disparaître si corrigée côté backend.

### `WasteApiService` — regroupe 9 contrôleurs du contexte `waste`
`DepotoirController` (`/v1/depotoirs` — **plus le seul soft-delete du lot depuis le 2026-08-11**,
`deletions`/`{id}/restore` restent en revanche ses seuls endpoints dédiés, les autres ressources
passant par la corbeille générique `/v1/deletions`), `TypeDepotoirController`,
`MoblierUrbainController`, `CircuitController`, `CircuitCollectController`,
`CircuitBalayageController` (lecture par id alignée sur `GET /{id}` depuis le 2026-08-10, l'ancien
chemin `/circuit-balayage/{id}` n'existe plus). **Pagination ajoutée le 2026-08-11** sur `Circuit`,
`CircuitCollect`, `CircuitBalayage`, `MoblierUrbain`, `TypeDepotoir` (`?page=&size=` + `/s`, même
patron que Depotoir) — n'ont plus de liste plate comme seule option.
`AlertController` (`/v1/alerts` — **les 3 endpoints `/test`, `/test1`, `/test2` étaient morts/
expérimentaux, supprimés le 2026-08-10** ; n'utiliser que `POST /v1/alerts` ou `/v1/alerts/s`,
inchangés), `AlertThresholdController` (`/v1/alert-thresholds`, ADMIN/SUPER_ADMIN — configuration
des seuils par type de dépotoir), `VehicleController` (`/v1/vehicles`, ADMIN/SUPER_ADMIN,
désactivation pas suppression), `CollectionScheduleController` (`/v1/collection-schedules`).

### `CollectionRouteApiService` — `/v1/collection-routes` (permission `VIEW_COLLECTION_ROUTE`/`DECLARE_COLLECTION`)
Le cœur de l'espace agent (section 18) :
- `GET ?communeId=` → tournée priorisée (`RouteStop[]`, champ `priority` = `DEBORDEMENT` |
  `ETAT_INCONNU` | `A_SURVEILLER` | `RIEN_A_FAIRE`)
- `POST .../stops/{depotoirId}/collected` → déclarer un passage (204)
- `POST .../stops/{depotoirId}/inaccessible` → avec motif optionnel (204)
- `GET .../completion?communeId=` → `{stops, served, collected}` pour la barre de progression

### `IoTApiService` — 3 contrôleurs
`DeviceProvisioningController` (`/v1/devices`, permission `MANAGE_DEVICES`) — enrôlement
capteur/traceur, **la clé API n'est lisible qu'une seule fois à la réponse d'enrôlement/rotation**,
ne jamais tenter de la re-fetcher. `GET /v1/devices/sensors|vehicle-trackers` → liste sans les
clés, avec `active`/`lastSeenAt`/`silenceReportedAt`/`orphaned` — c'est la donnée pour l'écran de
santé IoT (section 15, actuellement absent du frontend). `MeasurementIngestionController` et
`VehiclePositionController` sont **hors périmètre frontend web** (auth par clé device, appelés
par le matériel/MQTT, pas par l'UI).

### `AlertStreamApiService` — `GET /v1/alerts/stream` (SSE, ADMIN/SUPER_ADMIN)
**Seul flux SSE de tout le backend.** Événements : `connected` (accusé), `alert` (payload =
`Alert` complet), `heartbeat` (keep-alive). Un `RealtimeService` unique doit gérer ce flux et
distribuer les événements aux composants intéressés (dashboard, carte, centre d'alertes, compteur
de notifications) — pas une connexion SSE par composant.

### `CitizenApiService` — 4 contrôleurs côté `platform`
`AvisControleur` (base **`avis`, sans `/v1`**) — signalement citoyen, cycle
`SIGNALE→EN_COURS→{TRAITE,REJETE}` (transition illégale = 409) ; `GET /avis` (liste par statut) et
`GET /avis/map` réservés ADMIN/SUPER_ADMIN, `POST /avis` et `GET /avis/mine` ouverts à tout
authentifié. `CollectionSubscriptionController` (`/v1/collection-subscriptions`) — abonnement aux
rappels de collecte par quartier, l'identité vient toujours du jeton. `AwarenessController`
(`/v1/awareness`) + `AwarenessCampaignController` (`/v1/awareness/campaigns`, permission
`SEND_AWARENESS`) — diffusion de messages/campagnes, **totalement absent du frontend actuel côté
admin**. `QuizController` (`/v1/quizzes`, permission `SEND_AWARENESS` pour créer) — **la bonne
réponse n'est jamais renvoyée par `GET .../questions`**, un attempt par citoyen (409 au second).
`DeviceTokenController` (`/v1/device-tokens`) — enregistrement push, idempotent.

### `AnalyticsApiService` — 4 contrôleurs
`DashboardController` (`GET /data/departmentState`, **public**) — compteurs globaux.
`MapsController` (`/v1/maps`, tout authentifié) — `departments` (contour), `depotoirs` (avec
`fillLevelPercent`/`lastMeasuredAt`), `vehicles` (positions récentes seulement). C'est la source de
la carte centrale (section 13). `SupervisionStatsController` (`/v1/supervision/stats`,
SUPERVISEUR+) — statistiques complètes pour le dashboard : alertes/jour, répartition par code/type/
remplissage/commune, **santé d'ingestion** (`ingestion.silentSensors`), **rapports citoyens**
(`citizenReports.medianResolutionMinutes`). `PerformanceReportController`
(`/v1/supervision/reports` + `.../reports/csv`) — déjà consommé par `pages/reports/`, export CSV
existant. `PointJournalController` (`/v1/supervision/points/{id}/journal`) — **timeline
chronologique mesure/alerte/collecte, absente du frontend actuel**, c'est l'endpoint pour la
section 21 du cahier des charges (journal/historique).

### `AdminApiService` — 3 contrôleurs
`DataImportController` (`POST /data/{resource}`, multipart, ADMIN/SUPER_ADMIN) — import GeoJSON
manuel par ressource. `GeoJsonImportController` (`POST /v1/admin/import/geojson?force=`) — import
global du référentiel Pikine. `DeletionController` (`/v1/deletions`) — corbeille générique
multi-ressources : `GET /v1/deletions` (clés de ressources disponibles), `GET .../{resource}`
(éléments en attente de purge), `POST .../{resource}/{id}/restore` (**seul id `Long` de toute
l'API**, tout le reste est UUID). Ne couvre que les ressources réellement soft-deletable — `Depotoir`
a en plus ses **propres** endpoints dédiés (`/v1/depotoirs/deletions`, `/{id}/restore`).

## Incohérences à absorber dans la couche API (pas à corriger côté backend sans validation)

1. **Pagination non uniforme** : `Commune`/`Quartier`/`Depotoir`/`User`/`Alert` ont `Page<T>` **et**
   `/s` (liste complète) ; `Department`/`Region`/`Circuit*`/`Coordinate`/`Geometry`/`MoblierUrbain`/
   `TypeDepotoir` n'ont qu'une liste plate. Le typage `ResourceApiService<T>` générique doit
   distinguer les deux formes plutôt que supposer `Page<T>` partout.
2. **Suppression = sémantiques différentes selon la ressource** : `Depotoir` seul est un vrai
   soft-delete restorable. Les autres `DELETE /{id}` semblent définitifs malgré la réponse
   identique (`"Successfully delete"`) — ne pas proposer de bouton « restaurer » générique en
   dehors de ce que `GET /v1/deletions` liste réellement disponible.
3. ~~`AlertController` contient des endpoints de test morts~~ — **corrigé le 2026-08-10**, retirés.
4. ~~`Region` n'a pas d'endpoint de mise à jour/suppression~~ — **corrigé le 2026-08-10** : CRUD
   complet désormais aligné sur les 4 autres niveaux géographiques.
5. ~~Chemins irréguliers isolés~~ — **corrigés le 2026-08-10** :
   `DELETE /v1/coordinates/{id}` et `GET /v1/circuit-balayages/{id}` suivent maintenant le même
   patron que le reste de l'API ; plus besoin de les coder en dur séparément.
   ⚠️ **`frontend/src/app/shared/constants/api-endpoints.ts` n'a pas été mise à jour** (décision
   explicite, 2026-08-11) : `deletePathTemplate`/`getByIdPathTemplate` pour `coordinates` et
   `circuit-balayages` y pointent encore vers les anciens chemins, désormais morts côté backend.
6. ~~Pagination non uniforme~~ — **corrigée le 2026-08-11** : `Department`, `Region`, `Circuit`,
   `CircuitCollect`, `CircuitBalayage`, `Coordinate`, `Geometry`, `MoblierUrbain`, `TypeDepotoir`
   suivent désormais le même patron que Commune/Quartier/Depotoir/User/Alert (`GET` nu paginé,
   `GET .../s` liste complète). ⚠️ **`api-endpoints.ts` n'a pas été mise à jour non plus** (même
   décision) : `listPath` y présuppose encore l'ancien comportement (liste plate sans pagination)
   pour ces neuf ressources — à corriger côté frontend avant de construire `core/api/*.service.ts`
   dessus.
7. ~~Suppression = sémantiques différentes selon la ressource~~ — **largement corrigée le
   2026-08-11** : `User` était le seul repository du projet à supprimer réellement ses lignes
   (`DELETE /v1/users/{id}` est maintenant un soft-delete comme le reste). Un bug plus large a aussi
   été fermé : `POST /v1/deletions/{resource}/{id}/restore` attendait un `id` de type `Long` alors
   que toutes les entités du projet sont en `UUID` — la restauration ne pouvait fonctionner pour
   **aucune** des ~21 ressources déjà rattachées à la corbeille générique. Reste hors périmètre,
   décision distincte : `OrganizationMembership` (relation, pas une ressource au sens de cette
   liste) et le fait qu'un compte supprimé peut encore se connecter (`UserEntity.isEnabled()` ne
   dérive que d'`activated`, jamais de l'état de suppression).

## Fonctionnalités backend prêtes mais non exposées côté frontend (confirmé, met à jour §3 de l'audit)

| Endpoint | Feature attendue | Priorité refonte |
|---|---|---|
| `GET /v1/supervision/points/{id}/journal` | Timeline détecter→alerter→planifier→collecter par point (section 21) | P1 |
| `GET/POST /v1/awareness/campaigns`, `/v1/quizzes` | Gestion de campagnes + quiz de sensibilisation (section 22) | P2 |
| `GET /v1/devices/sensors\|vehicle-trackers` (au-delà du provisioning déjà présent) | Écran de santé IoT dédié (silencieux/actifs/orphelins) — le CRUD d'enrôlement existe déjà, la supervision continue non | P1 |
| `GET /v1/supervision/stats` (`ingestion`, `citizenReports`) | KPIs avancés du dashboard (capteurs muets, délai médian de traitement citoyen) — endpoint déjà consommé partiellement, ces deux sous-objets non affichés | P0 |
| `GET /v1/maps/vehicles` | Position flotte sur la carte centrale | P0/P1 |
| `GET /v1/alert-thresholds` (CRUD) | Configuration des seuils par type de dépotoir — aucun écran actuellement | P1 |
