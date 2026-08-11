# FRONTEND_UI_AUDIT.md — Audit avant refonte complète

> Phase 0 de la refonte Smart Collect. Rédigé le 2026-08-09, avant toute ligne de code de la
> nouvelle interface. Complète `docs/FRONTEND_AUDIT.md` (audit antérieur, Sprint 1 hardening,
> 2026-08-06) sans le remplacer — celui-ci se concentre sur ce qui doit changer pour la refonte
> Angular 20/Tailwind, pas sur les bugs déjà corrigés dans la base actuelle.

## 1. État actuel

**Stack** : Angular **17.3.0**, TypeScript 5.4, `@angular/material` 17.3.7, **Bootstrap 5.3.2**,
Leaflet 1.9.4 + proj4/esri-leaflet, Chart.js 4.4.3, SweetAlert2, ngx-translate. Bootstrap
standalone (`app.config.ts`, `app.routes.ts`) mais **44 modules NgModule**, seulement **16
composants `standalone: true`** — la grande majorité de l'app est encore en NgModule +
`loadChildren`.

**Architecture de dossiers** (`src/app/`) :
- `core/` — login, password, guards (`AuthGuard` : vérifie seulement `isLoggedIn()`, **aucun
  contrôle de rôle**), helpers (intercepteurs), services (`auth.service.ts`, `role.service.ts`),
  validators.
- `pages/` — 20 dossiers, un par ressource : `alert`, `avis`, `circuit-balayage`,
  `circuit-collect`, `collection-route`, `commune`, `dashboard`, `department`, `depotoir`,
  `device`, `general` (home/signup/contact/about/404), `login` (doublon mort, non routé),
  `maps`, `moblier-urbain`, `notification`, `profil`, `quartier`, `region`, `reports`, `users`,
  `vehicle`.
- `shared/` — composants transverses (layout, sidebar, header), constantes
  (`api-endpoints.ts` — registre des chemins backend, à jour au 2026-08-07), Material wrappers.
- `services/` (racine `app/`) — `alert-stream.service.ts` (SSE), `alert.service.ts`,
  `maps.service.ts`, `role.service.ts` (appelle `/roles`, **endpoint inexistant** côté backend —
  mort), `shared.service.ts` (client HTTP générique réutilisé par tout le CRUD).
- `models/` — 6 fichiers seulement : `geometry.model.ts`, `menu-item.model.ts`,
  `notification.model.ts`, `page.model.ts`, `route-state.model.ts`, `user.model.ts`. Aucun
  modèle pour `Depotoir`, `Alert`, `Circuit*`, `Sensor`, `Vehicle`, `CollectionRoute`, etc. — le
  CRUD générique (`shared.service.ts`) type presque tout en `any`.

**Navigation actuelle** (`sidebar.component.html`) : **un seul menu plat**, aucune distinction de
rôle. Dashboard, Département Pikine, un sous-menu « Gestion des ressources » (Utilisateurs,
Communes, Quartiers, Dépotoirs, Circuits balayage/collecte, Mobiliers urbains, Rapports, Flotte,
Capteurs), Carte, Tournée agent, Notifications, Alertes, Profil. Un agent ou un citoyen voit
exactement le même menu qu'un administrateur — les items renvoient un 403 backend s'ils n'ont pas
le rôle, mais rien ne les cache côté UI.

**Authentification** : JWT dans `localStorage` (`currentUser.bearer`/`.refresh`), refresh token
tourné à chaque usage (backend), pas de logique de décodage de rôle centralisée — `layout.component.ts`
décode le JWT localement (`@auth0/angular-jwt`) uniquement pour décider d'ouvrir ou non le flux SSE
d'alertes, un cas d'usage isolé, pas un service de rôle réutilisable.

**Design actuel** : tokens CSS (`assets/scss/variables.scss`) déjà retravaillés lors du sprint
2026-08-06/07 — palette verte (« Agriculture/Farm Tech », sans bleu, décision utilisateur explicite
et validée), Lexend + Source Sans 3, échelle d'espacement et typographique définies. Reste sur
Bootstrap : composants (`.btn`, `.card`, `.table`, `.form-control`) stylés par-dessus les classes
Bootstrap plutôt que remplacés.

## 2. Problèmes

### UX
- Aucune distinction de parcours ADMIN / AGENT / CITOYEN : un seul layout, un seul menu, pour
  trois publics aux besoins radicalement différents (section 11/18/22 du cahier des charges).
- Dashboard existant : KPI + 2 graphiques Chart.js + une carte (`dashboard-map.component.ts`,
  ajoutée 2026-08-06) + fil d'activité — une bonne base, mais pas la « vraie page de pilotage »
  demandée (pas de section alertes dédiée avec filtre critique/récent/non-traité/résolu, pas de
  flux d'événements temps réel distinct des toasts).
- Pas de centre d'alertes dédié : `pages/alert/` est un CRUD (liste/créer/éditer/supprimer), pas
  un centre de traitement avec filtres par état/gravité et vue détail.
- Le temps réel (`AlertStreamService`, SSE) se limite à des toasts SweetAlert2 — ne met à jour ni
  la carte, ni les compteurs, ni aucune liste affichée (section 17 du cahier des charges).
- `pages/circuit-collect/` est un copier-coller de l'écran utilisateurs (même méthode
  `loadUsers()`, mêmes colonnes) — pas une vraie fonctionnalité, connu depuis l'audit précédent,
  jamais traité.

### UI / Cohérence
- Composants Bootstrap stock (`.btn`, `.table`, `.form-control`) restylés par-dessus plutôt que
  remplacés par un vrai design system de composants — pas de variantes documentées
  (primary/secondary/ghost/danger, états focus/error/disabled cohérents partout).
- Pas de composant Card/Table/Modal réutilisable versionné — chaque écran CRUD réimplémente sa
  propre mise en page de tableau.
- Aucune gestion de skeleton loading — les écrans listant des données (`pages/*`) n'affichent
  rien ou un spinner générique pendant le chargement.

### Architecture
- `SharedService` générique (`services/shared.service.ts`) : un seul service HTTP dont l'URL de
  base est **mutable** (`.url = '/quelque-chose'`) et réutilisée par tous les CRUD — c'est la
  source directe du bug historique où 6 dialogues de suppression sur 7 tapaient `/quartiers`
  quelle que soit la ressource visée (corrigé le 2026-08-06, mais le patron lui-même — état
  mutable partagé — reste en place et reste fragile).
- Pas de couche API par domaine (`DashboardApiService`, `AlertApiService`, etc.) — les appels
  HTTP directs sont dispersés dans les composants ou passent par le service générique non typé.
- `User` (modèle) type `userId` en `number` — **faux depuis le 2026-08-07** (migration UUID v7
  du backend, ADR-0012 terminée) ; `role: any` — aucun typage réel des rôles/permissions.
- `RoleService.getRolesByPage()`/`getAllRoles()` appellent `GET /roles` — **cet endpoint n'existe
  pas** côté backend (le vrai est `/v1/authorities/s`, voir `api-endpoints.ts`) : code mort ou
  cassé, jamais détecté faute d'écran qui l'utilise réellement.

### Navigation
- Pas de garde de rôle sur les routes (`AuthGuard` ne vérifie que la connexion) : un AGENT ou un
  USER peut naviguer vers `/users`, `/devices`, etc. et voir un écran cassé (403 des appels API)
  au lieu d'un menu qui ne propose pas l'option.
- `/map` (composant `EsriComponent`) est routé **hors garde d'authentification** et orphelin —
  aucun lien n'y mène, coordonnées Londres codées en dur, jamais nettoyé (connu depuis l'audit
  précédent).
- Pas de breadcrumb, pas de recherche globale, pas de sélecteur de territoire persistant.

### Responsive
- Conçu desktop-first ; aucun écran n'est pensé mobile-first, y compris `collection-route`
  (tournée agent) qui est justement l'écran le plus susceptible d'être utilisé sur téléphone.

### Accessibilité
- Non auditée formellement. Contrastes de la palette verte actuelle vérifiés WCAG lors de sa
  création (2026-08-06/07) mais pas re-testés sur les composants Bootstrap réels rendus.
- Pas de tests clavier/lecteur d'écran connus.

### Performance
- `angular.json` : budgets de bundle déjà relevés en 2026-08-06 pour accommoder la pile
  Bootstrap+Leaflet+esri-leaflet+ApexCharts+Chart.js+ECharts+Quill+TinyMCE+SweetAlert (bundle
  initial mesuré à 3,86 Mo) — plusieurs de ces libs (ApexCharts, ECharts, Quill, TinyMCE) ne
  semblent utilisées nulle part dans les 20 dossiers `pages/` actuels : à vérifier et retirer si
  confirmé mort, ça allégerait le bundle avant même la migration Tailwind.

### Dette technique connue (non re-détaillée ici, voir `docs/FRONTEND_AUDIT.md` pour le détail)
55 fichiers `*.spec.ts` sont des stubs CLI jamais écrits ; refresh token en `localStorage` (pas de
cookie httpOnly, nécessite un support backend dédié) ; dépendance `keycloak-angular` inerte,
jamais utilisée.

## 3. Fonctionnalités backend non exposées

Confirmé par recoupement avec la session backend de ce même chantier (`CLAUDE.md`,
`docs/IMPLEMENTATION_LOG.md`) et le registre `api-endpoints.ts` :

| Fonctionnalité backend | État frontend |
|---|---|
| Campagnes de sensibilisation + quiz (`/v1/awareness/campaigns`, `/v1/quizzes`, changelog `2.23.0`, 2026-08-07) | **Absent.** Seul `pages/avis/` affiche les messages de sensibilisation reçus (`GET /v1/awareness/mine`) ; aucune interface de gestion des campagnes/quiz côté admin, aucune interface de réponse au quiz côté citoyen |
| Rapports de performance + export CSV (`/v1/supervision/reports`) | Présent (`pages/reports/`) — à moderniser, pas à créer |
| Historique/journal par point de collecte (`PointJournalController`) | **Absent** — aucun écran ne consulte cet historique alors que le cahier des charges (section 21) en fait un élément de démonstration prioritaire |
| Abonnements citoyens aux rappels de collecte (`/v1/collection-subscriptions`) | Présent dans `pages/avis/` (section « mes abonnements »), fonctionnel |
| Auto-affectation agent → commune (`GET /v1/users/me/communes` équivalent) | **N'existe pas côté backend non plus** — l'agent choisit manuellement sa commune dans `collection-route`, un mauvais choix renvoie 403. Gap documenté des deux côtés |
| Rotation de clé capteur/traceur, désactivation (`/v1/devices/**`) | Présent (`pages/device/`) |
| Statistiques de supervision temps réel (`/v1/supervision/stats`) | Consommé par le dashboard actuel (KPI + graphiques), mais pas par un vrai centre d'alertes |
| `GET /v1/me` | **N'existe pas côté backend** (confirmé absent lors de l'audit backend du 2026-08-08) — aucune alternative pour que le frontend récupère proprement son propre profil/rôle sans redécoder le JWT à la main |

Cartographie API exhaustive (38 contrôleurs, ~153 endpoints) : voir `docs/FRONTEND_API_MAPPING.md`,
qui liste en plus 5 incohérences de contrat (pagination non uniforme, sémantiques de suppression
différentes par ressource, endpoints de test morts dans `AlertController`, `Region` sans
update/delete, deux chemins irréguliers) à absorber dans la couche API plutôt qu'à corriger côté
backend sans validation.

## 4. Fonctionnalités frontend inutiles / à ne pas reconduire

- `pages/login/` — doublon mort de l'écran de connexion réel, jamais routé. À supprimer (pas
  juste ignorer) lors de la migration, puisque le code est de toute façon réécrit.
- `EsriComponent` (`/map`) — prototype orphelin, coordonnées Londres codées en dur, aucune donnée
  réelle. Ne pas migrer.
- `RoleService` actuel — appelle un endpoint inexistant, à remplacer entièrement par un vrai
  service de session/permissions basé sur le JWT + les rôles réels du backend
  (`SUPER_ADMIN`/`ADMIN`/`USER`/`AGENT`/`SUPERVISEUR`/`TECHNICIEN_IOT`).
- Dépendances à auditer pour suppression pendant la migration Tailwind (probable mort code, à
  confirmer par grep avant de trancher) : ApexCharts, Quill, TinyMCE, `keycloak-angular`.

## 5. Recommandations — nouvelle structure

Voir section 28 du cahier des charges pour l'arborescence cible (`core/`, `shared/`, `layout/`,
`features/*`). Point d'attention spécifique à ce projet : **trois shells de navigation distincts**
(admin, agent, citoyen) plutôt qu'un layout unique avec du contenu conditionnel — les audiences et
les besoins (desktop dense vs. mobile-first vs. grand public) sont trop différents pour un seul
composant de layout paramétré.

## 6. Priorisation

Reprend la priorisation du cahier des charges (section 35), affinée avec les éléments trouvés
ici :

**P0** : design system, app shell + garde de rôle réelle (absente aujourd'hui), dashboard, carte,
alertes, temps réel réellement branché sur l'UI (pas que des toasts).

**P1** : IoT (aucun écran de santé capteur dédié aujourd'hui, seulement le provisioning), tournées/
espace agent (mobile-first, écran le plus utilisé sur le terrain), rapports/analytics
(modernisation, pas création), historique/journal par point (à créer, actuellement totalement
absent).

**P2** : citoyen/sensibilisation (étendre l'existant `pages/avis/` + créer la gestion de
campagnes/quiz, aujourd'hui inexistante des deux côtés admin).

**P3** : nettoyage des dépendances mortes probables (ApexCharts/Quill/TinyMCE/keycloak-angular),
suppression définitive de `pages/login/` et `EsriComponent`.
