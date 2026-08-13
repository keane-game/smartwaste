# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

Monorepo for **"Gestion automatisée des ordures ménagères"** — a waste-collection supervision system for Pikine, Senegal (backend branded **SONAGED**). Goal: supervise collection points, detect fill level, trigger alerts, optimize routes, visualize on a map. ~~Note: the fill-level/IoT → automatic-alert core is not yet implemented; `Alert` is currently only a CRUD resource.~~ — **corrected 2026-08-06**: the `iot` context (`Sensor`, `Measurement`, `VehicleTracker`, device provisioning) plus `waste.FillLevelProjector`/`ThresholdResolver` implement the full sensor → measurement → threshold → automatic `Alert` chain, verified end-to-end against PostgreSQL (see `docs/IMPLEMENTATION_LOG.md`, entries 2026-08-04/05). `Alert` is no longer a plain CRUD resource. `FillLevelProjector` evaluates fill-level, temperature **and** humidity independently, each against its own configurable `AlertThreshold` (per `TypeDepotoir` or a global default) — the ADR-0004 "fill-level only" gap noted in older docs is closed. Still genuinely missing: route optimization by real fill level (tours are still priority-sorted, not re-sequenced by live data).

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
| `backend-api/` | Java 21, Spring Boot 3.5.3, Maven | REST API — the single backend (renamed from `ucgBackend/` 2026-08-06 via `git mv`, commits `5e9fad2`/`54607ad` — **corrected 2026-08-08**, this doc previously said the rename was uncommitted). Single DDD root `sn.smartwaste.collect` — the ADR-0013 migration finished 2026-07-28; `sonaged.collecte.master` no longer exists (verified: zero files) |
| `frontend/` | Angular 17.3 | **The** web frontend — sole survivor of the 2026-08-05 consolidation (199 `.ts`, ~17.4k lines as of the rename; grown substantially since, see IMPLEMENTATION_LOG for the design-system/role-navigation work). Renamed from `sonaged_web/` 2026-08-06 (commit `54607ad`, same pass as the `backend-api/` rename). Mostly NgModules (44) with a standalone bootstrap; only 16 files are `standalone: true` as of the rename. `angular/` and `ucgFrontend/` were deleted — see ADR-0006 (decision **inverted**, `sonaged_web/`/`frontend/` kept) and `docs/FRONTEND_AUDIT.md` |
| `mobile/` | Flutter 3 (Dart >=3.1.5) | Mobile app. Renamed from `mobileFlutter/` 2026-08-06 (commit `54607ad`) |
| `datas/` | GeoJSON | **13 files** of real Pikine geo data (quartiers, communes, circuits, dépotoirs, bacs de rue, points propres, caisses polybennes, pré-collecte). Importable via `POST /v1/admin/import/geojson` |

Naming is inconsistent: repo/dirs say `ucg`, code/brand says `sonaged`.

## Commands

### Backend (`backend-api/`)
```bash
./mvnw spring-boot:run                      # run API on :8089
./mvnw clean package                        # build jar
./mvnw test                                 # all tests
./mvnw test -Dtest=AuthorityServiceImplTest # single test class
./mvnw test -Dtest=AuthorityServiceImplTest#methodName
```
Requires PostgreSQL at `localhost:5433`, db `sonaged` (see `src/main/resources/application.yml` — `application.properties` no longer exists). Dev services: `docker compose -f src/main/resources/docker-compose.yml up` (smtp4dev **and MinIO**). Swagger UI at `/swagger-ui`, OpenAPI JSON at `/sonaged-docs`.

### Web (`frontend/`)
```bash
npm install
npm start          # ng serve on :4200
npm run build      # ng build
npm test           # karma/jasmine (single test: use fdescribe/fit in the spec)
npm run coverage   # ng test --no-watch --code-coverage
npm run lint
npm run serve      # serve prod build via server.js (expects dist/angular-starter/browser)
```

### Mobile (`mobile/`)
```bash
flutter pub get
dart run build_runner build --delete-conflicting-outputs   # regen *.freezed.dart / *.g.dart after model changes
flutter run -t lib/core/main_dev.dart                      # dev flavor (also main_staging.dart)
flutter test
```

## Architecture

### Backend — DDD (ADR-0013, migration complete since 2026-07-28)
Single root `sn.smartwaste.collect`, **10 modules** verified by Spring Modulith
(`SmartWasteModularityTests`): 7 bounded contexts (`identity`, `tenant`, `territory`, `waste`,
`iot`, `platform`, `analytics`) + 3 non-contexts (`shared`, `config`, `administration`). `iot` is
no longer reserved — it holds `Sensor`, `Measurement`, `VehicleTracker` and device provisioning
(see the correction above). Each context is `domain / application / infrastructure /
presentation`, dependencies pointing inward.

**Crossing a context boundary**: only via a published `@NamedInterface` port
(`waste.application.api.WasteReadModel`, `identity.application.api.CurrentUserProvider`,
`territory.application.api.TerritoryReadModel`, `platform.application.api.AlertStreamMetrics`,
`tenant.application.api.CurrentTenantProvider`) or a domain event in `shared.domain.event`.
Never another context's repository or entity — `modules.verify()` fails on it.

Cross-context references are **by identifier** (`UUID`), never JPA associations (ADR-0012).
Identifiers are **UUID v7** (`shared.infrastructure.persistence.UuidV7Generator`).

~~A legacy remnant survives in `sonaged.collecte.master`~~ — **corrected 2026-08-08**: that package
no longer exists (verified directly, zero files). Bootstrap, config, aspects, exception handlers
and GeoJSON import all live under the single `sn.smartwaste.collect` root now (`config`/
`administration` non-contexts).

Mappers use a **static-instance pattern**: `XxxMapper.UMP.asModel(dto)` — reuse it, don't autowire.
Circular references are **forbidden** (`spring.main.allow-circular-references=false` since 2026-07-30, ADR-0009 §4 closed) — `ApplicationContextLoadsTest` starts the real context, so a reintroduced cycle fails the build.

**Auth/security flow** (`identity/infrastructure/security`): `AuthController` → `AuthServiceImpl` (register → email activation code via `NotificationServiceImpl`/`ValidationService`; login via `AuthenticationManager`) → `JwtService` mints an HS256 token whose **subject is the user email**. `JwtFilter` (a `OncePerRequestFilter`) validates the `Bearer` token on every request; `SecurityConfiguration` is stateless, CORS-restricted to `localhost:4200`, and permits `/auth/**`, `/swagger-ui/**`, `/sonaged-docs/**` and **`GET` on `/data/**` only** (writes there were reachable unauthenticated — fixed). Users authenticate by email; `UserService.loadUserByUsername` looks up by email.

**Domain**: ~22 entities spread across the contexts — `territory` (Region → Department → Commune → Quartier, Geometry, Coordinate), `waste` (Depotoir, TypeDepotoir, MoblierUrbain, Circuit ×3, Alert, Image, History), `identity` (User, Authority, Validation, UserSession), `tenant` (Organization, OrganizationMembership), `platform` (Avis). `MapsController` now lives in `analytics.presentation` and assembles the two published map read-models. `DashboardController` serves aggregate stats. `UploadFileServiceImpl` handles image/file upload.

**API conventions**: base `/v1/**`, auth `/auth/**`, maps `/v1/maps/**`. Note `AlertController` uses quirky path suffixes (`@GetMapping("s")`, `@PostMapping("s")` under `/v1/alerts`). Schema belongs **exclusively to Liquibase** (`config/liquibase/master.xml`); Hibernate is `ddl-auto=validate`. Never switch back to `update`/`create`. Every entity change needs a changeset.

⚠️ **Note (corrected 2026-07-30)**: the "full list" endpoints are declared `@GetMapping("s")` under e.g. `@RequestMapping("/v1/users")`, which reads like `/v1/userss` — **it is not**. Spring's `PathPattern.combine` inserts a separator, so the real path is **`/v1/users/s`** (verified: `GET /v1/users/s` returns 200, `GET /v1/userss` returns "No static resource"). Only five resources have it: `alerts`, `communes`, `depotoirs`, `quartiers`, `users` — everything else lists on its plain path. Note the plain path on those five requires **mandatory** `page` and `size` params (no defaults), so calling it bare returns **400**, not 404. The corrected paths are recorded in `frontend/src/app/shared/constants/api-endpoints.ts`.

### Frontend — Angular (`frontend/`)
Feature areas: `core/` (login, password, guards, interceptors, cross-cutting services), `pages/` (one folder per resource: alert, commune, depotoir, quartier, users, maps, dashboard, avis…), `shared/` (components, constants, materials), `services/` (generic `shared.service` is the real API client). Bootstrap is standalone (`app.config.ts`), routing in `app.routes.ts`, but most screens are still NgModules. Stack: Bootstrap 5 + Material 17.3 + SweetAlert, Leaflet + proj4 + esri-leaflet for GIS, ngx-translate for i18n. Backend URLs come from `src/environments/environment*.ts` — note there are **three** bases: `apiUrl` (`/v1`), `authUrl` (`/auth`), `dataUrl` (`/data`).

**Endpoint paths live in `src/app/shared/constants/api-endpoints.ts`** — use that registry rather than writing paths inline. Eleven components used to hardcode singular paths (`/commune`, `/user`…) that the backend never exposed.

⚠️ Known open defects (full list: `docs/FRONTEND_AUDIT.md` §5.2): no route is guarded (`canActivate` is commented out in `app.routes.ts`), `environment.prod.ts` is never substituted and is missing `authUrl`/`dataUrl`, ids are typed `number` while the backend uses UUID v7, and the 55 spec files are untouched CLI stubs.

### Mobile — Flutter (`mobile/lib/`)
Feature-first clean architecture: `features/<name>/{data,domain,presentation}`, shared code in `shared/`. State via **Riverpod**, immutable models via **freezed** (regen with build_runner), routing via **go_router** (`routes/app_router.dart` + generated `app_router.g.dart`), networking via **dio** (`shared/data/remote/dio_network_service.dart`). Flavors: `lib/core/main_dev.dart` / `main_staging.dart`. API base in `lib/configs/app_configs.dart`.

⚠️ **Correction (2026-08-13)**: ~~contains leftover tutorial-template code (spoonacular,
`product_model`) — ignore it~~ was an understatement — verified directly, it is not isolated dead
code. The entire routed `/dashboard` screen (`features/dashboard/`, 10 files: datasource,
repository, provider, notifier, state, screen, drawer) is still built on the spoonacular tutorial's
`Product` domain (`productList`, `DashboardConcreteState.fetchedAllProducts`, generic search UI) —
compiles, is wired into `app_router.dart`, and is reachable in the running app, but shows tutorial
data, not SONAGED waste-collection data. Deleting `product_model.dart` would break this live screen
with no replacement. Left as-is (2026-08-13 decision) — adapting or replacing it is future work,
not something to delete blind.

## Known sharp edges (full list: `docs/KNOWLEDGE_MAP.md`)
- ~~The app has never been started against PostgreSQL~~ — done manually 2026-08-06: `./mvnw spring-boot:run` against a real local PostgreSQL 17 (pre-existing `smartwaste` database, already migrated, 38 tables) came up clean — `ddl-auto=validate` confronted the entities with the Liquibase schema and passed, `/actuator/health` returned `UP`, Swagger served. Only generic Spring Boot startup warnings (deprecated `hibernate.dialect` property, `open-in-view` default, springdoc defaults exposed), nothing DB-related. This was a one-off manual run, not wired into CI — `SonagedApplicationTests` is still `@Disabled` (no PostgreSQL/Docker in the environment that runs it; see `docs/IMPLEMENTATION_LOG.md`).
- **Secrets remain in Git history — and the JWT secret is worse than that.** `SecurityConstants.SECRET` (`identity/infrastructure/security/SecurityConstants.java`) is still hardcoded in the *current* codebase, not externalised despite what P0-2/ADR-0002 implies — it signs every JWT issued today. DB password and the Google key are historical-only (DB password is now externalised via `${DB_PASSWORD}`, though its `application.yml` fallback default `keqne` contradicts the file's own comment that says startup should fail loudly without it — also unfixed). Configuration is externalised elsewhere and a root `.gitignore` exists, but **rotation and history purge were never done** (ADR-0002 §4-5, plan proposed 2026-08-08, not executed — needs explicit validation, invalidates all active sessions). Do not add new ones.
- ~~`UserServiceImpl.createUser` still hardcodes `Sonaged@123`~~ — fixed 2026-07-30: it now requires and hashes the submitted password, like `AuthServiceImpl.register`. The constant remains in Git history.
- ~~`SecurityRule` beans are inert~~ — removed 2026-08-06: `AuthorityRules`/`UserRules` declared 13 authorization rules nothing ever applied. Real authorization lives in `SecurityConfiguration.authorizeHttpRequests` (writes under `/v1/**` and the whole administration surface require `ADMIN`/`SUPER_ADMIN`; referential reads stay open to any account) plus `@PreAuthorize` on the newer controllers. `Permission.ACCESS_ADMIN`/`USER_VIEW` were left in place — **correction 2026-08-08**: this doc previously said they had "no readers left", which was wrong. Both (plus `MANAGE_ROLE`, `CREATE_USER`, `ACCESS_MY_ACTIVITIES`) are seeded into `authorityPermission` by the baseline changelog and become real `SimpleGrantedAuthority` values at every login via `UserEntity.getAuthorities()` — removing them would break authentication for every seeded account. Verified by extracting every permission literal ever inserted across all Liquibase changelogs, not just by grepping Java call sites.
- **`ADMIN` can manage roles** (`MANAGE_ROLE` is seeded on it): an `ADMIN` can therefore grant itself `SUPER_ADMIN` via `/v1/authorities`. That follows the seeded intent; narrowing it to `SUPER_ADMIN` is a product decision, not a bug fix.
- ~~**Foreign leftovers**: `DataNotifierAspect`'s pointcut targets `com.worldline.tapandgo`, and the `Permission` enum carries ~20 values from that unrelated domain. The aspect can never fire.~~ — **removed 2026-08-08** (explicit validation obtained): `DataNotifierAspect`, the `Notifiable` annotation it targeted, and the unrelated-but-similarly-inert `SleuthTraceJmsListener` are gone. `Permission` still carries 5 `worldline/tapandgo`-named values (`USER_VIEW`, `ACCESS_ADMIN`, `MANAGE_ROLE`, `CREATE_USER`, `ACCESS_MY_ACTIVITIES`) — kept deliberately, they are real seeded authorities (see the `SecurityRule` bullet above), not dead code. The other ~30 were removed.
- `HistoryEntity` is a hollow stub (one `@Id`) dragging a DTO, mapper, repository, service and controller.
- ⚠️ `src/main/resources/schema.sql` begins with `DROP DATABASE`. It is neutralised (`spring.sql.init.mode: never`) and must never be re-enabled.
- The Liquibase changelogs `2.0.0` and `2.1.0` are **destructive** (BIGINT → uuid): they purge the referential and all accounts. Assumed — the dev database is disposable.
- Per the project's working agreement: **do not start a major refactor or deletion without explicit validation.** (The frontend consolidation of 2026-08-05 was validated explicitly and is done.)
