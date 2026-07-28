# Architecture cible — découpage en modules → microservices

> ⛔ **DOCUMENT OBSOLÈTE — conservé pour l'historique.**
>
> Il concrétise l'**ADR-0010**, qui a été **remplacé par l'[ADR-0013](adr/0013-architecture-ddd-smartwaste.md)**
> le 2026-07-27 : la cible n'est plus 5 modules sous `sonaged.ucg` découpés par couches, mais
> **8 contextes bornés sous `sn.smartwaste.collect`** en Clean Architecture, avec un contexte
> `tenant` et un contexte `iot` extractibles.
>
> Ce qui reste valable et n'a pas été repris ailleurs : le **flux métier central** (capteur → mesure →
> seuil → alerte → notification), l'**ordre d'extraction** en microservices, et l'inventaire des
> entités à créer (`Capteur`, `Measurement`, `Tournee`, `Notification`).
>
> Ce qui est faux : le découpage en 5 modules, les noms de packages, et la place de
> `Geometry`/`Coordinate` — annoncés ici comme *embeddables du shared kernel*, ils sont en réalité
> des **entités du contexte `territory`** exposées par `@NamedInterface("geo")`.

> Concrétise ADR-0010 (monolithe modulaire d'abord), ADR-0011 (Keycloak), ADR-0012 (découplage par identifiant).
> **5 modules** de haut niveau (frontières vérifiées par Spring Modulith), chacun **extractible** en microservice.
> Historique : une première version proposait 9 modules ; jugée trop granulaire, consolidée ici en 5 avec des **sous-domaines cloisonnés** pour préserver les coutures d'extraction.

## Principes

- **1 module = 1 bounded context = 1 schéma.**
- **Relations JPA seulement à l'intérieur d'un sous-domaine.** Entre modules **et entre sous-domaines**, référence par **identifiant** (`Long`/`UUID`), jamais par association objet (ADR-0012).
- **Communication par événements de domaine** (`ApplicationEventPublisher` → broker plus tard). Aucune transaction ne traverse un module/sous-domaine.
- **API publique par DTO** ; les entités ne sortent jamais.

## Shared Kernel (librairie partagée — PAS un service)

`AbstractAuditingEntity`, value objects géo **`Geometry`** / **`Coordinate`** (embeddables, une table par propriétaire), **contrats d'événements**, enums transverses. Minimal, pour éviter tout couplage caché.

---

## Les 5 modules

### 1. `identite-acces` — Identité & Accès  *(support · Keycloak)*
- **Features** : authentification **Keycloak** (OIDC) ; profil utilisateur local lié au `sub` Keycloak ; mapping rôles Keycloak → autorités.
- **Entités** : `UserEntity` (profil local). *Migré vers Keycloak :* `AuthorityEntity`, `AuthorityInfo`, enum `Permission`, `Validation`.
- **Réfs externes** : aucune.
- **Événements** : publie `UserRegistered`, `UserProfileUpdated`.
- **API** : `GET /v1/me`, `/v1/users/{id}`.

### 2. `referentiel-territorial` — Référentiel Territorial  *(données de référence · amont)*
- **Features** : hiérarchie Région→Département→Commune→Quartier ; contours GeoJSON ; fonds de carte.
- **Entités** : `RegionEntity`, `DepartmentEntity`, `CommuneEntity`, `QuartierEntity` (+ `Geometry`).
- **Réfs externes** : aucune.
- **Événements** : publie `TerritoryUpdated`.
- **API** : `/v1/regions`, `/v1/departments`, `/v1/communes`, `/v1/quartiers`, `/v1/maps/departments`.

### 3. `collecte` — Gestion des Collectes  *(cœur opérationnel)*
Un seul module, **4 sous-domaines cloisonnés** (packages + tables séparés, communication par id/événements) :

- **`points-collecte`** — dépotoirs, types, mobilier urbain, registre des **capteurs**.
  - Entités : `DepotoirEntity` (+ `Geometry`, `fillLevel`, `lastMeasuredAt`), `TypeDepotoirEntity`, `MoblierUrbainEntity`, `Capteur` *(nouveau : device + clé)*.
  - Réfs : `communeId`, `quartierId` (→ territorial). Consomme `MeasurementRecorded` (maj `fillLevel`).
- **`ingestion-iot`** ⚠️ *couture d'extraction prioritaire (fort volume)* — réception et historique des mesures.
  - Entités : `MeasurementEntity` *(nouveau : `depotoirId`, `capteurId`, `fillLevel`, `measuredAt`, `source`)*.
  - Réfs : `depotoirId`, `capteurId`. Publie `MeasurementRecorded`, `FillThresholdExceeded`.
  - API : `POST /v1/measurements`.
- **`alertes`** — alerte auto sur seuil + alertes manuelles, photos, statuts.
  - Entités : `AlertEntity` (`AlertCode`), `ImageEntity`.
  - Réfs : `depotoirId`, `userId`. Consomme `FillThresholdExceeded` ; publie `AlertRaised`, `AlertResolved`.
- **`circuits-tournees`** — circuits collecte/balayage, shifts (`CircuitShift`), tournées, optimisation (futur).
  - Entités : `CircuitEntity` (+ `Geometry`), `CircuitCollectEntity`, `CircuitBalayageEntity`, `Tournee` *(futur)*.
  - Réfs : `communeId`. Consomme `AlertRaised` (repriorisation, futur).

- **API du module** : `/v1/depotoirs`, `/v1/type-depotoirs`, `/v1/mobilier-urbain`, `/v1/capteurs`, `/v1/measurements`, `/v1/alerts`, `/v1/circuits`, `/v1/circuit-collects`, `/v1/circuit-balayages`.

> **Pourquoi 1 module et pas 4** : rythme d'évolution proche et forte cohésion métier « opérations de collecte ». **Mais** `ingestion-iot` garde des tables et une frontière propres → promcouvable en microservice dédié le jour où le débit capteurs l'impose, sans toucher au reste.

### 4. `communication` — Communication  *(support · aval)*
- **Features** : signalements citoyens (Avis) ; notifications multi-canal **e-mail / SSE temps réel / push FCM**.
- **Entités** : `Avis`, `Notification` *(nouveau : journal d'envoi)*.
- **Réfs externes** : `userId`, `depotoirId?`.
- **Événements** : consomme `AlertRaised`, `UserRegistered` ; publie `AvisSubmitted`, `NotificationSent`.
- **API** : `/v1/avis`, `GET /v1/alerts/stream` (SSE).

### 5. `supervision` — Supervision  *(read-side / BFF · aval)*
- **Features** : dashboard, statistiques, **historique/audit**, rapports. Modèles de lecture construits par abonnement aux événements — n'écrit dans aucun domaine.
- **Entités** : `HistoryEntity` (audit) + tables de projection.
- **Réfs externes** : lecture agrégée via événements/API.
- **Événements** : consomme *tous* les événements de domaine.
- **API** : `/v1/dashboard`, `/v1/history`, `/v1/reports`.

---

## Vue d'ensemble

| Module | Type | Entités clés | Réfs externes (id) |
|---|---|---|---|
| identite-acces | support | UserEntity (+ Keycloak) | — |
| referentiel-territorial | référence/amont | Region, Department, Commune, Quartier | — |
| collecte | cœur | Depotoir, TypeDepotoir, MobilierUrbain, Capteur, Measurement, Alert, Image, Circuit* | communeId, quartierId, userId |
| communication | support/aval | Avis, Notification | userId, depotoirId? |
| supervision | read/BFF/aval | History + projections | — |

## Flux central (cœur métier)

```
[Capteur] ─POST /v1/measurements─► collecte:ingestion-iot
     │ applique la mesure                 │ MeasurementRecorded ─► collecte:points-collecte (maj fillLevel)
     │ seuil dépassé                      │ FillThresholdExceeded ─► collecte:alertes (crée Alert)
                                                                        │ AlertRaised
                                                                        ├─► communication (e-mail/SSE/push)
                                                                        ├─► collecte:circuits-tournees (repriorisation, futur)
                                                                        └─► supervision (KPIs, historique)
```

## Sens des dépendances (aucun cycle)

`identite-acces` et `referentiel-territorial` en **amont** (référencés, ne référencent rien) → `collecte` (cœur) les référence **par id** → `communication` et `supervision` en **aval** (consomment des événements).

## Ordre d'extraction recommandé (monolithe → services)

1. **`identite-acces`** — via Keycloak (fondation d'auth du maillage).
2. **`collecte:ingestion-iot`** — extraite du module `collecte` en premier (débit capteurs, scaling indépendant).
3. **`communication`** — naturellement isolable (consomme des événements).
4. **`collecte:alertes`**, puis le reste de `collecte` selon les besoins.
5. **`referentiel-territorial`** — stable/lecture-intensif : reste dans le monolithe le plus longtemps ou devient service de référence en cache.
6. **`supervision`** — read-model, extractible en BFF quand les dashboards se densifient.

> Chaque extraction = remplacer l'appel in-process par REST/gRPC et l'événement interne par un message de broker ; le schéma du sous-domaine étant déjà isolé, la base suit sans refonte.
