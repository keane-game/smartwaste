# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

Monorepo for **"Gestion automatisée des ordures ménagères"** — a waste-collection supervision system for Pikine, Senegal (backend branded **SONAGED**). Goal: supervise collection points, detect fill level, trigger alerts, optimize routes, visualize on a map. See `PROJECT_STATUS.md` for the authoritative, up-to-date breakdown of what is built vs. missing — read it before planning work. Note: the fill-level/IoT → automatic-alert core is **not yet implemented**; `Alert` is currently only a CRUD resource.

The root `README.md` is empty; project knowledge lives in the French `.txt`/`.docx`/`.pdf` at the root and in `PROJECT_STATUS.md`.

## Sub-projects

| Path | Stack | Role |
|---|---|---|
| `ucgBackend/` | Java 17, Spring Boot 3.2.4, Maven | REST API (package `sonaged.collecte.master`) — the single backend |
| `angular/` | Angular 16 | **Primary** web frontend |
| `sonaged_web/` | Angular | Near-duplicate second web frontend (canonical choice undecided) |
| `ucgFrontend/` | Angular 16 | **Dead** legacy scaffold — do not build on it |
| `mobileFlutter/` | Flutter 3 (Dart >=3.1.5) | Mobile app |
| `datas/` | GeoJSON | Pikine geo data (quartiers, circuits, dépotoirs, bacs) — not yet imported to DB |

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
Requires PostgreSQL at `localhost:5433`, db `sonaged` (see `src/main/resources/application.properties`). Dev SMTP: `docker compose -f src/main/resources/docker-compose.yml up` (smtp4dev). Swagger UI at `/swagger-ui`, OpenAPI JSON at `/sonaged-docs`.

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

### Backend — layered
`controller → service (interface) → service.impl → repository (JPA) → model (*Entity)`, with `dto` + MapStruct `mapper`. Mappers use a **static-instance pattern**: `XxxMapper.UMP.asModel(dto)` / `.modelToDto(entity)` — reuse it, don't autowire mappers. AOP lives in `aspects/` driven by `@Notifiable`/`@SonagedApi` annotations. Circular references are force-enabled (`spring.main.allow-circular-references=true`).

**Auth/security flow** (`security/` package): `AuthController` → `AuthServiceImpl` (register → email activation code via `NotificationServiceImpl`/`ValidationService`; login via `AuthenticationManager`) → `JwtService` mints an HS256 token whose **subject is the user email**. `JwtFilter` (a `OncePerRequestFilter`) validates the `Bearer` token on every request; `SecurityConfiguration` is stateless, CORS-restricted to `localhost:4200`, and permits `/auth/**`, `/swagger-ui/**`, `/sonaged-docs/**`, `/data/**`. Users authenticate by email; `UserService.loadUserByUsername` looks up by email.

**Domain**: ~20 entities forming the geo/collection hierarchy (Region → Department → Commune → Quartier; Circuit / CircuitCollect / CircuitBalayage; Depotoir, MoblierUrbain, Geometry, Coordinate). `MapsController` (`/v1/maps/**`) serves GeoJSON-shaped DTOs (`dto/maps/`) for the supervision map. `DashboardController` serves aggregate stats. `UploadFileServiceImpl` handles image/file upload.

**API conventions**: base `/v1/**`, auth `/auth/**`, maps `/v1/maps/**`. Note `AlertController` uses quirky path suffixes (`@GetMapping("s")`, `@PostMapping("s")` under `/v1/alerts`). Schema is managed by **both** Liquibase (declared) and Hibernate `ddl-auto=update` (active) — they conflict; confirm intent before schema changes.

### Frontend — Angular (`angular/`)
Feature areas: `core/` (login, guards, interceptors incl. `jwt.interceptor`, cross-cutting services), `entity/` (CRUD screens; **users** is complete, **depotoir** is stubbed), `shares/` (layout/header/sidebar/footer), `pages/general/` (mostly empty), `services/` (generic `shared.service` is the real API client; `services/user.service.ts` is an empty stub). Backend URL comes from `src/environments/environment*.ts`.

### Mobile — Flutter (`mobileFlutter/lib/`)
Feature-first clean architecture: `features/<name>/{data,domain,presentation}`, shared code in `shared/`. State via **Riverpod**, immutable models via **freezed** (regen with build_runner), routing via **go_router** (`routes/app_router.dart` + generated `app_router.g.dart`), networking via **dio** (`shared/data/remote/dio_network_service.dart`). Flavors: `lib/core/main_dev.dart` / `main_staging.dart`. API base in `lib/configs/app_configs.dart`. Contains leftover tutorial-template code (spoonacular, `product_model`) — ignore it.

## Known sharp edges (see PROJECT_STATUS.md §6 for full list)
- Secrets are committed (JWT secret in `SecurityConstants`, DB password, Google key) and there is no root `.gitignore` — do not add more, and flag before touching.
- `AuthServiceImpl.register` hardcodes every user's password to `Sonaged@123` (ignores submitted password) — a known P0 bug, not intended behavior.
- Per the project's working agreement: **do not start a major refactor or deletion (e.g. removing the dead `ucgFrontend`) without explicit validation.**
