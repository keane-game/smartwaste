# Audit frontend — Smart Collect

> Livrable du sprint 1. État arrêté au 2026-08-05, après consolidation sur un front unique.
> Ce document dit **ce qui est**, pas ce qui devrait être. La feuille de route est en §8.

---

## 1. Périmètre et méthode

Trois fronts Angular coexistaient. L'audit a porté sur les trois, puis la consolidation a été
exécutée. Constats établis par lecture du code et confrontation systématique au backend
(`backend-api/src/main/java`), source de vérité pour tout ce qui concerne l'API.

**Limite assumée de cet audit :** aucun SDK Node n'était disponible sur la machine d'analyse
(`npm` résout vers `/mnt/c/nvm4w/nodejs/npm`, qui ne trouve pas `node`). **Ni `ng build`, ni
`ng lint`, ni `ng test` n'ont été exécutés.** Tout constat de compilation ou de test est déduit
de la lecture, jamais observé. C'est la première chose à lever au sprint 1.

---

## 2. Décision : un seul front

| Dépôt | Verdict | Motif |
|---|---|---|
| `sonaged_web/` | **Retenu** | Base technique de la cible : Leaflet + proj4 + esri-leaflet, i18n, papaparse, dépendances Angular 17.3 cohérentes entre elles |
| `angular/` | Supprimé (1 054 fichiers) | Pile plus pauvre (Material 16 sur Angular 17), mais **était le seul à jour de l'API** — récupéré avant suppression, cf. §6 |
| `ucgFrontend/` | Supprimé | Mort depuis 2024-02-04 |

**Ceci invalide ADR-0006**, qui retenait `angular/` comme canonique. L'ADR raisonnait sur la
complétude fonctionnelle ; l'arbitrage réel s'est joué sur ce qui est coûteux à refaire. La pile
SIG et l'i18n de `sonaged_web` sont du travail ; la fraîcheur de l'API d'`angular/` était de la
connaissance, transférable en un fichier (§6).

**Correction à porter au dossier :** `sonaged_web` est souvent décrit comme « à composants
standalone ». C'est faux : **16 fichiers sur 199** portent `standalone: true`, contre
**44 NgModules**. L'application a un *bootstrap* standalone (`app.config.ts`), pas une
architecture standalone. La migration reste entièrement à faire.

État actuel : 199 fichiers `.ts`, ~17 400 lignes, 55 fichiers de test.

---

## 3. La surface réelle du backend

**37 contrôleurs REST, 147 endpoints.** Le front n'en exploitait qu'une vingtaine. C'est le
chiffre central de cet audit : l'essentiel du travail n'est pas de réparer l'existant mais
d'exposer ce qui n'a jamais eu d'interface.

### 3.1 Bases d'URL — trois préfixes, pas un

| Base | Contenu | Variable |
|---|---|---|
| `/v1/**` | Tout le métier | `environment.apiUrl` |
| `/auth/**` | `authenticate`, `register`, `activation`, `refresh`, `logout` | `environment.authUrl` |
| `/data/**` | Import de fichiers, `departmentState` | `environment.dataUrl` |
| `/avis` | Signalement citoyen — **à la racine**, hors `/v1` | dérivée |

### 3.2 Le piège des sous-chemins `/s`

Cinq ressources — `communes`, `quartiers`, `depotoirs`, `users`, `alerts` — réservent le chemin
simple à une réponse `Page`, avec `page` et `size` **obligatoires et sans valeur par défaut**.
Les appeler sans paramètres retourne **400**, pas 404. La liste complète est sur un sous-chemin
`/s` : `/v1/communes/s`.

Le backend déclare `@GetMapping("s")` sous `@RequestMapping("/v1/communes")`, ce qui se lit comme
une concaténation sans en être une : `PathPattern.combine` insère un séparateur. `/v1/communess`
n'a jamais existé et ne peut pas exister.

Ces chemins sont désormais centralisés dans
`sonaged_web/src/app/shared/constants/api-endpoints.ts`.

### 3.3 Modèle d'autorisation

**Rôles réellement existants : `SUPER_ADMIN`, `ADMIN`, `AGENT`, `USER`.** `USER` est le rôle
attribué à l'inscription (`AuthServiceImpl.DEFAULT_REGISTRATION_ROLE`).

> ⚠️ Le cahier des charges mentionne `MANAGER` et `CITIZEN` : **ni l'un ni l'autre n'existe**.
> Le citoyen, c'est `USER`. Introduire `MANAGER` demanderait une décision produit et une
> migration Liquibase, pas du code frontend.

Permissions (autorités, distinctes des rôles) : `MANAGE_ROLE`, `MANAGE_DEVICES`,
`SEND_AWARENESS`, `VIEW_COLLECTION_ROUTE`, `DECLARE_COLLECTION`, `ACCESS_MY_ACTIVITIES`.

| Surface | Exigence |
|---|---|
| `/v1/users/**`, `/v1/authorities/**`, `/v1/deletions/**`, `/v1/admin/**`, `/v1/supervision/**` | ADMIN ou SUPER_ADMIN — **la lecture aussi** |
| Toute écriture `POST/PUT/PATCH/DELETE` sous `/v1/**` | ADMIN ou SUPER_ADMIN par défaut |
| `GET /v1/alerts/stream` (SSE) | **ADMIN ou SUPER_ADMIN** |
| `POST /v1/collection-routes/stops/*/collected` et `/inaccessible` | AGENT, ADMIN, SUPER_ADMIN + `DECLARE_COLLECTION` |
| `GET /v1/collection-routes` | `VIEW_COLLECTION_ROUTE` |
| `POST`/`DELETE /v1/device-tokens` | authentifié — l'identité vient du jeton, jamais du corps |
| Lectures du référentiel sous `/v1` | tout compte authentifié |

**Conséquence de conception, à intégrer dès le sprint 1 :** le flux temps réel est réservé à
l'administration. Le module « temps réel » ne peut pas alimenter les espaces agent et citoyen
tels que décrits — il leur faudra un autre canal, ou une ouverture côté backend qui est une
décision de sécurité, pas un détail d'implémentation.

---

## 4. Écarts entre le cahier des charges et le système réel

Cinq prémisses de la commande ne correspondent pas à l'état vérifié. Elles sont listées ici
pour être tranchées avant d'écrire le code qui en dépend.

**4.1 — Le flux SSE n'est pas sur `/events`.** Il est sur **`GET /v1/alerts/stream`**.

**4.2 — Le flux ne porte qu'un seul type d'événement métier.** Types émis : `connected`
(ouverture), `alert` (charge utile), `heartbeat` (maintien). Seul `AlertRaisedEvent` est
rediffusé. `MeasurementRecorded`, `SensorSilenceDetected`, `SensorBackOnline` **existent comme
événements de domaine** (`shared/domain/event/`) mais `AlertBroadcaster` ne les écoute pas. Les
faire remonter est une modification backend d'une dizaine de lignes — à arbitrer, la consigne
étant de ne pas toucher au backend sauf nécessité.

**4.3 — Keycloak est inerte.** La dépendance `spring-boot-starter-oauth2-resource-server` est
au `pom.xml`, mais aucun `issuer-uri` n'est configuré et **aucune classe de `src/main` ne
référence Keycloak** (ADR-0011, `docs/keycloak-migration.md`). L'authentification réelle est
JWT maison, sujet `sub` = e-mail. Côté front, `keycloak-angular` est en dépendance et tous ses
usages sont commentés. À traiter comme « JWT seul » jusqu'à décision contraire.

**4.4 — La cible Angular 20+ / Tailwind / signals est une migration, pas un réglage.** L'existant
est Angular 17.3, Bootstrap 5 + SweetAlert + Material 17.3, NgModules majoritaires, aucun signal,
aucun Tailwind. Deux montées majeures (17→18→19→20), le remplacement du système de style, et la
conversion NgModule→standalone. C'est le vrai contenu du sprint 1, et cela entre en tension
directe avec « ne jamais remplacer complètement l'existant » : arbitrage nécessaire (§8).

**4.5 — « Backend stable et terminé » est vrai aujourd'hui, ne l'était pas il y a trois jours.**
Les commits `b247f4b` (G2 notifications), `ba73706` (G3 sensibilisation) et `3444ff7`
(permissions) ont atterri pendant cet audit. Les endpoints `/v1/device-tokens`, `/v1/awareness`
et le durcissement des permissions en font partie et sont pris en compte ici.

---

## 5. Dette technique de `sonaged_web`

### 5.1 Corrigé pendant la consolidation

| Défaut | Fichier | Effet avant correction |
|---|---|---|
| Aucun renouvellement de jeton | `core/services/auth.service.ts` | Éjection au bout de quelques minutes — les jetons sont courts et tournants côté serveur |
| Intercepteur d'erreurs non enregistré | `app.config.ts` | Aucun traitement des 401 |
| Intercepteur d'erreurs cassé | `core/helpers/error.inerceptor.ts` | `err.error.message` levait sur coupure réseau ; une `string` était passée à un test `instanceof HttpErrorResponse`, rendant tout le `switch` inatteignable ; `of({})` **avalait** chaque erreur |
| `Bearer undefined` | `core/helpers/auth.interceptor.ts` | En-tête invalide sur toutes les requêtes hors session, y compris les fichiers i18n |
| `logout` purement local | `core/services/auth.service.ts` | Le jeton restait valide côté serveur |

### 5.2 Ouvert

| # | Défaut | Localisation | Gravité |
|---|---|---|---|
| 1 | **Aucune route protégée** — `canActivate` commenté, deux implémentations de garde concurrentes dont une au corps commenté | `app.routes.ts:14`, `core/gaurds/`, `core/helpers/` | haute |
| 2 | **11 chemins d'API au singulier** — création commune/quartier/dépotoir et modification utilisateur impossibles | composants `create-*`, `update-user` | haute |
| 3 | **Listes appelées sans `page`/`size`** → 400 | `services/shared.service.ts:28` | haute |
| 4 | `environment.prod.ts` jamais substitué (aucun `fileReplacements`) et **incompatible** (il manque `authUrl` et `dataUrl`) | `angular.json`, `src/environments/` | haute |
| 5 | Identifiants typés `number` alors que le backend est en UUID v7 | `shared.service.ts:66,75,81` | moyenne |
| 6 | Jeton **et** jeton de rafraîchissement en `localStorage` | `core/services/auth.service.ts` | moyenne |
| 7 | Deux écrans de connexion routés (`/login` et `/logins`) | `app.routes.ts:39,44` | basse |
| 8 | Keycloak inerte : dépendance + service, tous usages commentés | `directives/secure.directive.ts`, `shared/shared.module.ts` | basse |
| 9 | Appel tiers externe `https://api.ipify.org` depuis le navigateur | `shared.service.ts:115` | basse |
| 10 | **55 fichiers de test, tous des squelettes CLI.** Aucun ne fournit `HttpClient`. `app.component.spec.ts` attend `title === 'angular-routing'` alors qu'il vaut `'Sonaged sénégal'` — cette spec échoue | `src/**/*.spec.ts` | moyenne |

Le point 1 n'est pas une faille de données — `SecurityConfiguration` filtre côté serveur. C'est
que l'interface se rend et se remplit de 401/403 au lieu de rediriger.

---

## 6. Récupéré depuis `angular/` avant suppression

| Élément | Destination | Ce que ça apporte |
|---|---|---|
| Renouvellement de jeton + déconnexion serveur | `core/services/auth.service.ts` | `POST /auth/refresh` avec stockage du couple qui revient (le backend fait tourner le jeton) |
| Intercepteur 401 avec verrou anti-concurrence | `core/helpers/error.inerceptor.ts` | Un seul rafraîchissement en vol, rejeu unique |
| Client SSE `fetch` + `ReadableStream` | `services/alert-stream.service.ts` | `EventSource` n'accepte pas d'en-tête `Authorization` ; reconnexion en back-off |
| Registre des chemins | `shared/constants/api-endpoints.ts` | 15 ressources + ~30 chemins hors CRUD ; corrige les points 2 et 3 du §5.2 |
| Accès typés `/v1/maps/**` | `services/maps.service.ts` | Interfaces de charge utile + `toLatLng()` |
| Écran « Donner mon avis » | `pages/avis/` | Seul écran citoyen du dépôt |

**Non récupéré, délibérément :** le rendu SVG de la carte de supervision. Il avait été écrit à la
main parce qu'`angular/` n'avait aucune bibliothèque cartographique ; `sonaged_web` a Leaflet, et
le sprint 3 construit la vraie carte. Sa couche de données a été portée. Le rendu reste
consultable : `git show bce8da4:angular/src/app/supervision/supervision-map.component.ts`.

**Piège documenté au passage :** `/maps/departments` et `/maps/depotoirs` sérialisent
`latitude`/`longitude` en **chaînes**. Leaflet attend des `number` — sans `parseFloat`, les
marqueurs se posent en `NaN` et la carte reste vide **sans lever d'erreur**.
`MapsService.toLatLng()` s'en charge.

---

## 7. Capacités backend sans aucune interface

Classé par valeur de démonstration. C'est ici que se trouve l'écart entre « interface CRUD » et
« plateforme Smart City ».

| Domaine | Endpoints | État UI |
|---|---|---|
| **Supervision & rapports** | `/v1/supervision/stats`, `/v1/supervision/reports`, `/v1/supervision/reports/csv`, `/v1/supervision/points/{id}/journal` | aucune |
| **Tournée de l'agent** | `/v1/collection-routes`, `/stops/{id}/collected`, `/stops/{id}/inaccessible`, `/completion` | aucune |
| **Planification** | `/v1/collection-schedules`, `/v1/alert-thresholds` | aucune |
| **Flotte** | `/v1/vehicles`, `/v1/vehicle-positions`, `/v1/maps/vehicles` | aucune |
| **IoT** | `/v1/devices/sensors`, `/v1/devices/vehicle-trackers`, `/v1/measurements` | aucune |
| **Citoyen** | `/v1/collection-subscriptions`, `/v1/device-tokens`, `/v1/awareness`, `/avis` | `/avis` seul |
| **Temps réel** | `/v1/alerts/stream` | service porté, non branché à l'UI |
| **Corbeille 30 j** | `/v1/deletions/**` | aucune |
| **Cartographie** | `/v1/maps/departments`, `/depotoirs`, `/vehicles` | aucune |

L'export CSV des rapports (`/v1/supervision/reports/csv`) existe déjà côté serveur : le module 7
n'a pas à le réimplémenter, seulement à le déclencher et à traiter la réponse `text/csv`.

---

## 8. Recommandations et feuille de route

### 8.1 Deux arbitrages à rendre avant de coder

**A. Migration Angular 17.3 → 20 et Bootstrap → Tailwind.** C'est une refonte de la couche de
présentation. La consigne « ne jamais remplacer complètement l'existant sans raison » et la cible
technique imposée ne peuvent pas être satisfaites ensemble sans décision explicite. Trois voies :
migration progressive (Tailwind coexistant avec Bootstrap le temps de la bascule), refonte
franche de la couche présentation en conservant services et modèles, ou maintien en 17.3. La
première est la seule qui préserve les 17 400 lignes existantes tout en atteignant la cible.

**B. Événements SSE.** Faire remonter `MeasurementRecorded`, `SensorSilenceDetected` et
`SensorBackOnline` demande une modification de `AlertBroadcaster`. Sans elle, le module 4 se
limite aux alertes.

### 8.2 Ordre de travail

| Sprint | Contenu | Préalable levé par |
|---|---|---|
| **1** | Rendre l'outillage exécutable (`node`/`npm`) ; arbitrages A et B ; **gardes de routes** (§5.2-1) ; chemins d'API (§5.2-2,3) ; environnements (§5.2-4) ; layout global | §3, §5 |
| **2** | Dashboard admin — KPI depuis `/v1/supervision/stats`, graphiques | §7 |
| **3** | Carte Pikine — Leaflet + `toLatLng()`, données `/v1/maps/**` et `datas/` | §6 |
| **4** | Temps réel — brancher `AlertStreamService` (admin seulement, cf. §3.3) | §6 |
| **5** | Espace agent — `/v1/collection-routes`, mobile first | §7 |
| **6** | Rapports — `/v1/supervision/reports` + export CSV natif | §7 |
| **7** | Espace citoyen — abonnements, `/avis` (existant), sensibilisation | §6, §7 |
| **8** | UX, tests (les 55 specs sont à écrire, pas à corriger), déploiement | §5.2-10 |

### 8.3 Règle transverse

Les identifiants sont des **UUID v7**, jamais des entiers. Tout typage `id: number` est un bug en
attente. C'est le point 5 du §5.2, et il traverse toute la couche service.

---

## Annexe — documents à mettre à jour

Suppression des deux fronts non répercutée dans : `CLAUDE.md:25,109`, `README.md:19,70,104`,
`ROADMAP.md:92`, et surtout **`docs/adr/0006-consolidation-frontend.md`**, dont la décision est
inversée par le §2 de ce document. `PROJECT_STATUS.md` et `PROJECT_ANALYSIS.md` sont des archives
gelées au 2026-07-11 et n'ont pas vocation à être corrigés.
