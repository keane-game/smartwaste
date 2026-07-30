# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

Monorepo for **"Gestion automatisée des ordures ménagères"** — a waste-collection supervision system for Pikine, Senegal (backend branded **SONAGED**). Goal: supervise collection points, detect fill level, trigger alerts, optimize routes, visualize on a map. Note: the fill-level/IoT → automatic-alert core is **not yet implemented**; `Alert` is currently only a CRUD resource.

**Which document to trust** (they disagree — see `docs/KNOWLEDGE_MAP.md` §8):
1. `docs/IMPLEMENTATION_LOG.md` — **authoritative** on what exists and why. Read this before planning.
2. `docs/KNOWLEDGE_MAP.md` — full documentary map, inconsistencies, gaps between spec and backlog.
3. `ROADMAP.md` — the plan. Its P0/P1 numbering is the reference one.
4. `README.md` — accurate entry point (no longer empty).
5. ⚠️ `PROJECT_STATUS.md` and `PROJECT_ANALYSIS.md` are **archives frozen at 2026-07-11**; most of what they list as broken is fixed. Do not plan from them.

Functional specification lives in the French `.txt`/`.docx`/`.pdf` at the root (the master's thesis) — it is wider than the backlog.

## Sub-projects

| Path | Stack | Role |
|---|---|---|
| `ucgBackend/` | Java 21, Spring Boot 3.5.3, Maven | REST API — the single backend. **Two roots during the ADR-0013 migration**: `sn.smartwaste.collect` (target, DDD) and `sonaged.collecte.master` (legacy remnant) |
| `angular/` | Angular 17 | **Primary** web frontend (164 `.ts`, ~9.9k lines) |
| `sonaged_web/` | Angular 17.3 | Second web frontend — **larger and more modern** than `angular/` (196 `.ts`, ~16.9k lines, standalone components). ADR-0006 assumes the opposite; do not act on it before re-checking |
| `ucgFrontend/` | Angular 16 | **Dead** legacy scaffold — do not build on it |
| `mobileFlutter/` | Flutter 3 (Dart >=3.1.5) | Mobile app |
| `datas/` | GeoJSON | **13 files** of real Pikine geo data (quartiers, communes, circuits, dépotoirs, bacs de rue, points propres, caisses polybennes, pré-collecte). Importable via `POST /v1/admin/import/geojson` |

Naming is inconsistent: repo/dirs say `ucg`, code/brand says `sonaged`.

## Commands

### Backend (`ucgBackend/`)
```bash
./mvnw spring-boot:run                      # run API on :8089
./mvnw clean package                        # build jar
./mvnw test                                 # all tests
./mvnw test -Dtest=AuthorityServiceImplTest # single test class
./mvnw test -Dtest=AuthorityServiceImplTest#methodName
```
Requires PostgreSQL at `localhost:5433`, db `sonaged` (see `src/main/resources/application.yml` — `application.properties` no longer exists). Dev services: `docker compose -f src/main/resources/docker-compose.yml up` (smtp4dev **and MinIO**). Swagger UI at `/swagger-ui`, OpenAPI JSON at `/sonaged-docs`.

### Web (`angular/`, same scripts in `sonaged_web/`)
```bash
npm install
npm start          # ng serve on :4200
npm run build      # ng build
npm test           # karma/jasmine (single test: use fdescribe/fit in the spec)
npm run coverage   # ng test --no-watch --code-coverage
npm run lint
npm run serve      # serve prod build via server.js (expects dist/angular-starter/browser)
```

### Mobile (`mobileFlutter/`)
```bash
flutter pub get
dart run build_runner build --delete-conflicting-outputs   # regen *.freezed.dart / *.g.dart after model changes
flutter run -t lib/core/main_dev.dart                      # dev flavor (also main_staging.dart)
flutter test
```

## Architecture

### Backend — DDD, migration in progress (ADR-0013)
Target root `sn.smartwaste.collect`, **10 modules** verified by Spring Modulith
(`SmartWasteModularityTests`): 7 bounded contexts (`identity`, `tenant`, `territory`, `waste`,
`iot` reserved, `platform`, `analytics`) + 3 non-contexts (`shared`, `config`, `administration`).
Each context is `domain / application / infrastructure / presentation`, dependencies pointing inward.

**Crossing a context boundary**: only via a published `@NamedInterface` port
(`waste.application.api.WasteReadModel`, `identity.application.api.CurrentUserProvider`,
`territory.application.api.TerritoryReadModel`, `platform.application.api.AlertStreamMetrics`,
`tenant.application.api.CurrentTenantProvider`) or a domain event in `shared.domain.event`.
Never another context's repository or entity — `modules.verify()` fails on it.

Cross-context references are **by identifier** (`UUID`), never JPA associations (ADR-0012).
Identifiers are **UUID v7** (`shared.infrastructure.persistence.UuidV7Generator`).

A legacy remnant survives in `sonaged.collecte.master` (bootstrap, config, aspects, exception
handlers, GeoJSON import). Legacy may depend on the new modules; the reverse must not happen.

Mappers use a **static-instance pattern**: `XxxMapper.UMP.asModel(dto)` — reuse it, don't autowire.
Circular references are **forbidden** (`spring.main.allow-circular-references=false` since 2026-07-30, ADR-0009 §4 closed) — `ApplicationContextLoadsTest` starts the real context, so a reintroduced cycle fails the build.

**Auth/security flow** (`identity/infrastructure/security`): `AuthController` → `AuthServiceImpl` (register → email activation code via `NotificationServiceImpl`/`ValidationService`; login via `AuthenticationManager`) → `JwtService` mints an HS256 token whose **subject is the user email**. `JwtFilter` (a `OncePerRequestFilter`) validates the `Bearer` token on every request; `SecurityConfiguration` is stateless, CORS-restricted to `localhost:4200`, and permits `/auth/**`, `/swagger-ui/**`, `/sonaged-docs/**` and **`GET` on `/data/**` only** (writes there were reachable unauthenticated — fixed). Users authenticate by email; `UserService.loadUserByUsername` looks up by email.

**Domain**: ~22 entities spread across the contexts — `territory` (Region → Department → Commune → Quartier, Geometry, Coordinate), `waste` (Depotoir, TypeDepotoir, MoblierUrbain, Circuit ×3, Alert, Image, History), `identity` (User, Authority, Validation, UserSession), `tenant` (Organization, OrganizationMembership), `platform` (Avis). `MapsController` now lives in `analytics.presentation` and assembles the two published map read-models. `DashboardController` serves aggregate stats. `UploadFileServiceImpl` handles image/file upload.

**API conventions**: base `/v1/**`, auth `/auth/**`, maps `/v1/maps/**`. Note `AlertController` uses quirky path suffixes (`@GetMapping("s")`, `@PostMapping("s")` under `/v1/alerts`). Schema belongs **exclusively to Liquibase** (`config/liquibase/master.xml`); Hibernate is `ddl-auto=validate`. Never switch back to `update`/`create`. Every entity change needs a changeset.

⚠️ **Note (corrected 2026-07-30)**: the "full list" endpoints are declared `@GetMapping("s")` under e.g. `@RequestMapping("/v1/users")`, which reads like `/v1/userss` — **it is not**. Spring's `PathPattern.combine` inserts a separator, so the real path is **`/v1/users/s`** (verified: `GET /v1/users/s` returns 200, `GET /v1/userss` returns "No static resource"). Only five resources have it: `alerts`, `communes`, `depotoirs`, `quartiers`, `users` — everything else lists on its plain path. `angular/` used to call `/v1/communess`, `/v1/depotoirss`… so every "list all" screen 404'd; **fixed 2026-07-30**.

### Frontend — Angular (`angular/`)
Feature areas: `core/` (login, guards, interceptors incl. `jwt.interceptor`, cross-cutting services), `entity/` (CRUD screens generated from the declarative registry `shares/crud/entity-config.ts`), `shares/` (layout/header/sidebar/footer), `pages/general/` (mostly empty), `services/` (generic `shared.service` is the real API client; `services/user.service.ts` is an empty stub). Backend URL comes from `src/environments/environment*.ts`.

### Mobile — Flutter (`mobileFlutter/lib/`)
Feature-first clean architecture: `features/<name>/{data,domain,presentation}`, shared code in `shared/`. State via **Riverpod**, immutable models via **freezed** (regen with build_runner), routing via **go_router** (`routes/app_router.dart` + generated `app_router.g.dart`), networking via **dio** (`shared/data/remote/dio_network_service.dart`). Flavors: `lib/core/main_dev.dart` / `main_staging.dart`. API base in `lib/configs/app_configs.dart`. Contains leftover tutorial-template code (spoonacular, `product_model`) — ignore it.

## Known sharp edges (full list: `docs/KNOWLEDGE_MAP.md`)
- **The app has never been started against PostgreSQL.** `ddl-auto=validate` has therefore never confronted the entities with the Liquibase schema. Neither the compiler, nor `JpaMappingBootstrapTest`, nor the H2 context test can substitute for that.
- **Secrets remain in Git history** (JWT signing secret, DB password, Google key). Configuration is externalised and a root `.gitignore` exists, but **rotation and history purge were never done** (ADR-0002 §4-5). Do not add new ones.
- ~~`UserServiceImpl.createUser` still hardcodes `Sonaged@123`~~ — fixed 2026-07-30: it now requires and hashes the submitted password, like `AuthServiceImpl.register`. The constant remains in Git history.
- **`SecurityRule` beans are inert**: `AuthorityRules`/`UserRules` declare 13 authorization rules nothing ever applies. They are now *redundant* as well as inert — real authorization lives in `SecurityConfiguration.authorizeHttpRequests` (writes under `/v1/**` and the whole administration surface require `ADMIN`/`SUPER_ADMIN`; referential reads stay open to any account) plus `@PreAuthorize` on the newer controllers. Deleting the dead rule beans still needs validation.
- **`ADMIN` can manage roles** (`MANAGE_ROLE` is seeded on it): an `ADMIN` can therefore grant itself `SUPER_ADMIN` via `/v1/authorities`. That follows the seeded intent; narrowing it to `SUPER_ADMIN` is a product decision, not a bug fix.
- **Foreign leftovers**: `DataNotifierAspect`'s pointcut targets `com.worldline.tapandgo`, and the `Permission` enum carries ~20 values from that unrelated domain. The aspect can never fire.
- `HistoryEntity` is a hollow stub (one `@Id`) dragging a DTO, mapper, repository, service and controller.
- ⚠️ `src/main/resources/schema.sql` begins with `DROP DATABASE`. It is neutralised (`spring.sql.init.mode: never`) and must never be re-enabled.
- The Liquibase changelogs `2.0.0` and `2.1.0` are **destructive** (BIGINT → uuid): they purge the referential and all accounts. Assumed — the dev database is disposable.
- Per the project's working agreement: **do not start a major refactor or deletion (e.g. removing the dead `ucgFrontend`) without explicit validation.**
