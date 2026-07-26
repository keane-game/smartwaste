# PROJECT_STATUS.md

> Projet : **Gestion automatisée des ordures ménagères** (marque backend : **SONAGED**)
> Document généré par analyse du dépôt — dernière mise à jour : 2026-07-11
> Aucune modification de code n'a été effectuée. Ce document est une **photographie de l'état actuel**.

---

## 0. Résumé exécutif

Le dépôt `master-ucg` est un **monorepo à 5 sous-projets** issu d'un mémoire de master (UNCHK, Sénégal) pour la gestion des points de collecte de déchets à Pikine. Le projet a fortement grossi (~2 100 fichiers versionnés).

**Ce qui fonctionne** : un backend Spring Boot structuré (auth JWT, ~20 entités avec CRUD, cartographie GeoJSON, dashboard, upload d'images) et un début réel de frontend Angular (CRUD utilisateurs, login) + une app Flutter (écrans auth, dashboard, carte).

**Le point critique** : **la promesse métier centrale n'est pas implémentée**. Il n'existe aucune captation de niveau de remplissage, aucun capteur/IoT, aucun seuil, aucun déclenchement automatique d'alerte. La « collecte » d'alertes est un simple CRUD, et le service de notification n'envoie que des **codes d'activation de compte** — pas des alertes de collecte.

**Sécurité** : plusieurs failles graves (mot de passe d'inscription codé en dur identique pour tous, secret JWT versionné, secrets dans Git). À traiter avant toute mise en service.

---

## 1. Analyse documentaire

### Documents lus
| Fichier | Contenu | Utilité |
|---|---|---|
| `README.md` (racine) | 13 octets (`# master-ucg`) | ❌ vide |
| `ucgBackend/endpoint.md` | Liste d'endpoints | ⚠️ copier-coller erroné (toutes les ressources listent les endpoints `/v1/users`) |
| `angular/README.md`, `mobileFlutter/README.md`, `sonaged_web/README.md`, `ucgFrontend/README.md` | Boilerplate framework | ⚠️ non spécifiques |
| `sonaged_web/cmd.md` | Notes perso (`ng g`, `ufw`, `asadmin`) | ⚠️ brouillon versionné |
| `GESTION DE DECHET IoT.txt` | Notes métier : modèle IoT Kenya, 66 circuits, balayage matin/soir/nuit, superviseurs (25 agents), points propres/bacs de rue | ✅ contexte métier |
| `Gestion automatisée … UCG.txt/.docx/.pdf` | Mémoire complet | ✅ source de vérité fonctionnelle |
| `Formulaire d'enquête … .csv` | Enquête citoyenne (demande d'un système d'alerte) | ✅ besoin utilisateur |
| `datas/*.json` | GeoJSON réels de Pikine (quartiers, circuits, dépotoirs, bacs de rue…) | ✅ données géographiques |

### Objectif du projet (d'après la doc)
Superviser les points de collecte, **détecter le niveau de remplissage**, **envoyer des alertes automatiques**, optimiser les tournées et visualiser le tout sur une interface de supervision.

### Décisions techniques existantes
- Backend Java/Spring Boot + PostgreSQL, auth JWT stateless.
- Cartographie basée sur des GeoJSON servis par l'API.
- Multi-clients : web (Angular) + mobile (Flutter).

---

## 2. Analyse technique

### 2.1 Backend — `ucgBackend/`
- **Langage / framework** : Java 17, Spring Boot **3.2.4** (package `sonaged.collecte.master`).
- **Architecture** : en couches classiques — `controller → service (interface) → service.impl → repository (JPA) → model (entity)`, avec `dto` + `mapper` (MapStruct). AOP présent (`aspects/DataNotifierAspect`, annotations `@Notifiable`/`@SonagedApi`).
- **API** : REST sous `/v1/**`, auth sous `/auth/**`, cartes sous `/v1/maps/**`, docs OpenAPI/Swagger sous `/sonaged-docs`. Port **8089**.
- **Sécurité** : `SecurityConfiguration` (filter chain JWT, CORS `localhost:4200`, stateless), `JwtFilter`, `JwtService` (jjwt), BCrypt, activation par code e-mail.
- **Services** : 21 implémentations. Les plus étoffées : `UploadFileServiceImpl` (618 l.), `AlertServiceImpl` / `UserServiceImpl` (151 l.), `DepartmentServiceImpl`, `QuartierServiceImpl`. Stub : `ImageServiceImpl` (2 l.).
- **Modèles / BD** : ~20 entités (`UserEntity`, `AlertEntity`, `CircuitEntity`, `CircuitCollectEntity`, `CircuitBalayageEntity`, `RegionEntity`, `DepartmentEntity`, `CommuneEntity`, `QuartierEntity`, `DepotoirEntity`, `MoblierUrbainEntity`, `GeometryEntity`, `CoordinateEntity`, `TypeDepotoirEntity`, `AuthorityEntity`, `HistoryEntity`, `ImageEntity`, `Avis`, `Validation`…).

### 2.2 Frontend web — `angular/` (candidat principal)
- Angular 16, Bootstrap + vendors (apexcharts, echarts, tinymce, quill…).
- Implémenté : **CRUD utilisateurs** (`entity/users/create|list|update`, 157–205 l.), login (`core/login`, 75 l.), layout/sidebar/header, guards & intercepteurs (`core/helpers/jwt.interceptor`, `error.inerceptor`), services (`shared.service`, `auth.service`, `session`, `token-storage`…).
- Stubs : **dépotoirs** (`entity/depotoir/*` ≈ 10 l.), pages `general/*` (about/contact/home) largement vides.
- ⚠️ `services/user.service.ts` est un squelette vide ; le CRUD passe par `shared.service`.

### 2.3 Autres frontends
- **`sonaged_web/`** : **deuxième** frontend Angular (rôle/canonicité non clarifiés).
- **`ucgFrontend/`** : ancien scaffold Angular **mort** (vestige du 1er état du projet).

### 2.4 Mobile — `mobileFlutter/`
- Flutter, architecture propre (Riverpod, freezed, dio, go_router, Google Maps).
- Écrans implémentés : splash, welcome, login, signup, choose-user, dashboard, accueil, **google-map + live tracking** (`liveTrackigUtils/`).
- ⚠️ Restes de template de tutoriel : `app_env.dart` (« Q Flutter TDD », `api.spoonacular.com`), `product_model` (API recettes).
- `baseUrl` = `http://10.0.2.2:8089/v1` (émulateur Android).

### 2.5 Infrastructure
- **Docker** : `docker-compose.yml` = uniquement `smtp4dev` (mail de dev). Pas de conteneur applicatif ni BD.
- **CI/CD** : ❌ aucun.
- **Migrations** : Liquibase déclaré dans le `pom.xml`, mais `spring.jpa.hibernate.ddl-auto=update` est actif → **conflit** (deux mécanismes de schéma).
- **Variables d'env** : ❌ tout est en dur dans `application.properties` / le code.

### 2.6 Base de données
- PostgreSQL, `jdbc:postgresql://localhost:5433/sonaged`.
- Relations entre entités : à cartographier finement (audit séparé recommandé) ; historiquement les entités étaient plates (tout en `String`).
- Données : GeoJSON dans `datas/` non encore chargés en base de façon automatisée.

---

## 3. Fonctionnalités terminées

| Fonctionnalité | Emplacement | État |
|---|---|---|
| Authentification JWT (login) | `security/*`, `controller/AuthController`, `service/impl/AuthServiceImpl` | ✅ fonctionnel ⚠️ (voir bug P0-1) |
| Inscription + activation par code e-mail | `AuthServiceImpl`, `ValidationServiceImpl`, `NotificationServiceImpl` | ⚠️ fonctionne mais mot de passe codé en dur (P0-1) |
| CRUD Utilisateurs (API) | `controller/UserController`, `service/impl/UserServiceImpl` | ✅ |
| CRUD Alertes (API, + upload image) | `controller/AlertController` (164 l.), `AlertServiceImpl` | ✅ CRUD seul (pas d'automatisme) |
| CRUD entités géo/métier (Region, Department, Commune, Quartier, Circuit, CircuitCollect, CircuitBalayage, Depotoir, MobilierUrbain, TypeDepotoir…) | `controller/*`, `service/impl/*` | ✅ majoritairement |
| Cartographie (départements, dépotoirs) | `controller/MapsController`, `dto/maps/*` | ✅ lecture GeoJSON |
| Dashboard (statistiques) | `controller/DashboardController`, `DashboardServiceImpl` | ✅ base |
| Upload de fichiers/images | `service/impl/UploadFileServiceImpl` (618 l.) | ✅ |
| Front web — CRUD Utilisateurs + login | `angular/src/app/entity/users/*`, `core/login` | ✅ |
| Mobile — écrans auth / dashboard / carte + live tracking | `mobileFlutter/lib/features/*` | ✅ écrans (backend à brancher) |

## 4. Fonctionnalités en cours

| Objectif | Fichiers concernés | Blocages |
|---|---|---|
| Front web dépotoirs (CRUD) | `angular/src/app/entity/depotoir/*` | composants à l'état de stub (~10 l.) |
| Pages générales (about/contact/home) | `angular/src/app/pages/general/*` | contenu vide |
| Consolidation clients web | `angular/` vs `sonaged_web/` vs `ucgFrontend/` | 3 fronts, canonique non décidé |
| Mobile ↔ API | `mobileFlutter/lib/features/*/data/*` | endpoints réels à câbler, restes de template à retirer |
| Chargement des GeoJSON en base | `datas/*.json` | pas de pipeline d'import |

## 5. Fonctionnalités restantes (priorisées)

### P0 — indispensable
- **P0-1 — Corriger l'inscription** : hacher le mot de passe *soumis* au lieu de `"Sonaged@123"` codé en dur (`AuthServiceImpl.register`).
- **P0-2 — Externaliser et roter les secrets** : secret JWT (`SecurityConstants.SECRET`), mot de passe BD, clé Google (`google-services.json`) ; ajouter un `.gitignore` racine ; purger l'historique.
- **P0-3 — Cœur métier : captation du niveau de remplissage** — modèle de mesure (capteur/point de collecte), endpoint d'ingestion, logique de **seuil**, **déclenchement automatique d'alerte**. **Actuellement inexistant.**
- **P0-4 — Notification d'alerte de collecte** : `NotificationService` n'envoie que des codes d'activation ; ajouter l'envoi d'alertes aux gestionnaires.
- **P0-5 — Robustesse JWT** : capter `ExpiredJwtException`/`JwtException` (→ 401, pas 500) ; sécuriser `roles.get(0)` (crash si aucun rôle) ; supprimer le log du token dans `JwtFilter`.

### P1 — important
- **P1-1** — Nettoyer le `pom.xml` : retirer springfox (abandonné, incompatible Boot 3) et l'un des deux libs JWT ; choisir **Liquibase OU** `ddl-auto`, pas les deux.
- **P1-2** — Aligner l'URL API Angular (`:8089/api` → `/v1` réel) ; vraie config `environment` prod.
- **P1-3** — Terminer le CRUD dépotoirs (web) et implémenter la carte de supervision (points + statut de remplissage).
- **P1-4** — Optimisation des tournées de collecte (algorithme sur circuits).
- **P1-5** — Décider du front canonique et supprimer les fronts morts (après validation).
- **P1-6** — Pipeline d'import des GeoJSON `datas/` en base.

### P2 — amélioration
- **P2-1** — Tests (actuellement quasi nuls) : unitaires services + e2e.
- **P2-2** — CI/CD + Dockerfile applicatif + conteneur PostgreSQL.
- **P2-3** — Documentation réelle : README racine, `endpoint.md` corrigé, schéma d'architecture.
- **P2-4** — Nettoyage des restes de template Flutter (spoonacular / product_model).
- **P2-5** — Homogénéiser le nommage (UCG vs SONAGED).

---

## 6. Bugs / problèmes techniques identifiés

| # | Sévérité | Problème | Emplacement |
|---|---|---|---|
| 1 | 🔴 Critique | Mot de passe d'inscription codé en dur (`Sonaged@123`) pour **tous** les comptes | `AuthServiceImpl.register` |
| 2 | 🔴 Critique | Secret JWT versionné dans le code | `constant/SecurityConstants` |
| 3 | 🔴 Critique | Secrets dans Git (mdp BD, clé Google), pas de `.gitignore` racine | `application.properties`, `google-services.json` |
| 4 | 🔴 Critique | Token bearer loggué à chaque requête (fuite) | `JwtFilter` |
| 5 | 🟠 Élevé | Token expiré/invalide → 500 au lieu de 401 (pas de try/catch) | `JwtFilter` / `JwtService` |
| 6 | 🟠 Élevé | `roles.get(0)` → crash si utilisateur sans rôle | `JwtService.generateJwt` |
| 7 | 🟠 Élevé | springfox + springdoc et 2 libs JWT en conflit | `pom.xml` |
| 8 | 🟠 Élevé | Liquibase **et** `ddl-auto=update` actifs simultanément | `application.properties` + `pom.xml` |
| 9 | 🟡 Moyen | `allow-circular-references=true` masque une dépendance circulaire | `application.properties` |
| 10 | 🟡 Moyen | URL API Angular incohérente (`/api` vs `/v1`) | `angular/src/environments/*` |
| 11 | 🟡 Moyen | Restes de template tutoriel dans Flutter | `mobileFlutter/lib/core/app_env.dart`, `shared/domain/models/product/*` |
| 12 | 🟢 Faible | `endpoint.md` erroné, README vides, notes perso versionnées | docs |

---

## 7. Architecture actuelle (schéma)

```
                         ┌──────────────────────────┐
   Web (Angular)  ─────► │                          │
   :4200                 │   ucgBackend (SONAGED)    │
                         │   Spring Boot 3.2.4       │ ──► PostgreSQL
   Mobile (Flutter)────► │   :8089  /v1  /auth  /maps│     :5433 sonaged
   10.0.2.2:8089         │   JWT · MapStruct · AOP   │
                         │   OpenAPI /sonaged-docs   │ ──► SMTP (smtp4dev / Gmail)
                         └──────────────────────────┘
   Données géo : datas/*.json (GeoJSON Pikine) — non encore importées en base

   Modules web additionnels : sonaged_web/ (2e front) · ucgFrontend/ (mort)
```

**Manquant dans l'architecture cible** : la brique **capteurs / niveau de remplissage → moteur de seuils → alertes automatiques**, qui est la raison d'être du projet.

---

## 8. Plan de reprise proposé (à valider)

1. **Sprint 0 — Sécurité (P0-1, P0-2, P0-4, P0-5)** : corriger l'inscription, sortir les secrets, blinder le JWT. *Aucune suppression, uniquement des corrections ciblées.*
2. **Sprint 1 — Cœur métier (P0-3)** : concevoir le modèle « point de collecte + mesure de remplissage », endpoint d'ingestion, seuils, déclenchement + notification d'alerte.
3. **Sprint 2 — Assainissement (P1-1, P1-2, P1-6)** : `pom.xml`, stratégie de schéma, URL API, import GeoJSON.
4. **Sprint 3 — UI supervision (P1-3, P1-4, P1-5)** : carte des points + statut, tournées, choix du front canonique.
5. **Sprint 4 — Qualité (P2)** : tests, CI/CD, Docker, documentation.

> ⚠️ Conformément au CLAUDE.md : **aucune refonte ni suppression majeure** (fronts morts, restes de template) ne sera lancée sans validation explicite.
