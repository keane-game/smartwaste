# API SONAGED — Endpoints

> Généré depuis les contrôleurs le 2026-07-26.
> Documentation interactive : **Swagger UI** `/swagger-ui` — OpenAPI JSON `/sonaged-docs`.

⚠️ **Périmé (2026-08-13)** : ce fichier date d'avant une longue série de correctifs API
(endpoints morts retirés, chemins irréguliers corrigés, pagination étendue à 9 ressources
supplémentaires, DTO `Authority`...) — voir `docs/IMPLEMENTATION_LOG.md` et
`docs/FRONTEND_API_MAPPING.md` pour l'état réel. Ne pas s'y fier ; ci-dessous seulement pour les
grandes lignes encore vraies. **Swagger UI (`/swagger-ui`) est la référence à jour**, générée à
chaque démarrage depuis le code — ce fichier statique, lui, ne l'est pas.

## Conventions

| Préfixe | Authentification | Remarque |
|---|---|---|
| `/auth/**` | publique | inscription, activation, connexion |
| `/v1/**` | **JWT requis** (`Authorization: Bearer …`) | ressources métier |
| `/data/**` | ~~publique (dette : devrait être protégé)~~ **corrigé** : `GET` seul reste public, les écritures exigent désormais une authentification |
| `/swagger-ui`, `/sonaged-docs` | publique | documentation |

⚠️ **Correction** : la ligne ci-dessous décrivait mal le chemin réel — un double « s » collé
(`/v1/communess`) **n'existe pas**. `PathPattern.combine` insère un séparateur : le vrai chemin de
liste complète est **`/v1/communes/s`** (idem `/v1/quartiers/s`, `/v1/depotoirs/s`, `/v1/users/s`,
`/v1/alerts/s`) — vérifié, voir `CLAUDE.md`. ~~Certaines ressources exposent la liste complète sur
un chemin à double « s » (`/v1/communess`, `/v1/quartierss`, `/v1/depotoirss`, `/v1/userss`,
`/v1/alertss`), tandis que le chemin simple renvoie une réponse paginée (`?page=&size=`).~~ Le
chemin simple (`GET /v1/communes` etc.) renvoie bien une réponse paginée, avec `page`/`size`
**obligatoires** (400 sinon) pour les cinq ressources concernées.

---


## Alert  
`/v1/alerts`

- `GET   ` `/v1/alerts`
- `GET   ` `/v1/alerts/{alertId}`
- `GET   ` `/v1/alertss`
- `POST  ` `/v1/alerts`
- `POST  ` `/v1/alerts/test`
- `POST  ` `/v1/alerts/test1`
- `POST  ` `/v1/alerts/test2`
- `POST  ` `/v1/alertss`
- `PUT   ` `/v1/alerts/{alertId}`
- `DELETE` `/v1/alerts/{alertId}`


## AlertStream  
`/v1/alerts`

- `GET   ` `/v1/alerts/stream`
- `GET   ` `/v1/alertss`


## Auth  
`/auth/`

- `POST  ` `/auth/activation`
- `POST  ` `/auth/authenticate`
- `POST  ` `/auth/register`


## Authority  
`/v1/authorities`

- `GET   ` `/v1/authorities`
- `GET   ` `/v1/authorities/{authorityId}`
- `POST  ` `/v1/authorities`
- `PUT   ` `/v1/authorities/{authorityId}`
- `DELETE` `/v1/authorities/{authorityId}`


## AvisControleur  
`/avis`

- `POST  ` `/avis`


## CircuitBalayage  
`/v1/circuit-balayages`

- `GET   ` `/v1/circuit-balayages`
- `GET   ` `/v1/circuit-balayages/circuit-balayage/{circuitBalayageId}`
- `POST  ` `/v1/circuit-balayages`
- `PUT   ` `/v1/circuit-balayages/{circuitBalayageId}`
- `DELETE` `/v1/circuit-balayages/{circuitBalayageId}`


## CircuitCollect  
`/v1/circuit-collects`

- `GET   ` `/v1/circuit-collects`
- `GET   ` `/v1/circuit-collects/{circuitCollectId}`
- `POST  ` `/v1/circuit-collects`
- `PUT   ` `/v1/circuit-collects/{circuitCollectId}`
- `DELETE` `/v1/circuit-collects/{circuitCollectId}`


## Circuit  
`/v1/circuits`

- `GET   ` `/v1/circuits`
- `GET   ` `/v1/circuits/{circuitId}`
- `POST  ` `/v1/circuits`
- `PUT   ` `/v1/circuits/{circuitId}`
- `DELETE` `/v1/circuits/{circuitId}`


## Commune  
`/v1/communes`

- `GET   ` `/v1/communes`
- `GET   ` `/v1/communes/{communeId}`
- `GET   ` `/v1/communess`
- `POST  ` `/v1/communes`
- `PUT   ` `/v1/communes/{communeId}`
- `DELETE` `/v1/communes/{communeId}`


## Coordinate  
`/v1/coordinates`

- `GET   ` `/v1/coordinates`
- `GET   ` `/v1/coordinates/{coordinateId}`
- `POST  ` `/v1/coordinates`
- `PUT   ` `/v1/coordinates/{coordinateId}`
- `DELETE` `/v1/coordinates/delete/coordinate/{coordinateId}`


## Dashboard  
`/data`

- `GET   ` `/data/departmentState`
- `POST  ` `/data/circuitbalayage`
- `POST  ` `/data/circuitcollect`
- `POST  ` `/data/commune`
- `POST  ` `/data/department`
- `POST  ` `/data/depotoir`
- `POST  ` `/data/quartier`


## Deletion  
`/v1/deletions`

- `GET   ` `/v1/deletions`
- `GET   ` `/v1/deletions/{resource}`
- `POST  ` `/v1/deletions/{resource}/{id}/restore`


## Department  
`/v1/departments`

- `GET   ` `/v1/departments`
- `GET   ` `/v1/departments/{departmentId}`
- `POST  ` `/v1/departments`
- `PUT   ` `/v1/departments/{departmentId}`
- `DELETE` `/v1/departments/{departmentId}`


## Depotoir  
`/v1/depotoirs`

- `GET   ` `/v1/depotoirs`
- `GET   ` `/v1/depotoirs/deletions`
- `GET   ` `/v1/depotoirs/{depotoirId}`
- `GET   ` `/v1/depotoirss`
- `POST  ` `/v1/depotoirs`
- `POST  ` `/v1/depotoirs/{depotoirId}/restore`
- `PUT   ` `/v1/depotoirs/{depotoirId}`
- `DELETE` `/v1/depotoirs/{depotoirId}`


## GeoJsonImport  
`/v1/admin/import`

- `POST  ` `/v1/admin/import/geojson`


## Geometry  
`/v1/geometries`

- `GET   ` `/v1/geometries`
- `GET   ` `/v1/geometries/{geometryId}`
- `POST  ` `/v1/geometries`
- `PUT   ` `/v1/geometries/{geometryId}`
- `DELETE` `/v1/geometries/{geometryId}`


## Maps  
`/v1/maps`

- `GET   ` `/v1/maps/departments`
- `GET   ` `/v1/maps/depotoirs`


## MoblierUrbain  
`/v1/moblier-urbains`

- `GET   ` `/v1/moblier-urbains`
- `GET   ` `/v1/moblier-urbains/{moblierUrbainId}`
- `POST  ` `/v1/moblier-urbains`
- `PUT   ` `/v1/moblier-urbains/{moblierUrbainId}`
- `DELETE` `/v1/moblier-urbains/{moblierUrbainId}`


## Quartier  
`/v1/quartiers`

- `GET   ` `/v1/quartiers`
- `GET   ` `/v1/quartiers/{quartierId}`
- `GET   ` `/v1/quartierss`
- `POST  ` `/v1/quartiers`
- `PUT   ` `/v1/quartiers/{quartierId}`
- `DELETE` `/v1/quartiers/{quartierId}`


## Region  
`/v1/regions`

- `GET   ` `/v1/regions`
- `GET   ` `/v1/regions/{regionId}`
- `POST  ` `/v1/regions`


## SupervisionStats  
`/v1/supervision`

- `GET   ` `/v1/supervision/stats`


## TypeDepotoir  
`/v1/typedepotoirs`

- `GET   ` `/v1/typedepotoirs`
- `GET   ` `/v1/typedepotoirs/{typeDepotoirId}`
- `POST  ` `/v1/typedepotoirs`
- `PUT   ` `/v1/typedepotoirs/{typeDepotoirId}`
- `DELETE` `/v1/typedepotoirs/{typeDepotoirId}`


## User  
`/v1/users`

- `GET   ` `/v1/users`
- `GET   ` `/v1/users/{id}`
- `GET   ` `/v1/userss`
- `POST  ` `/v1/users`
- `PUT   ` `/v1/users/{id}`
- `DELETE` `/v1/users/{id}`

