# SONAGED — Mobile App

Flutter mobile client for **"Gestion automatisée des ordures ménagères"**, a waste-collection
supervision system for Pikine, Senegal. Talks to the same Spring Boot API as the web frontend
(`../backend-api`).

## Stack

* Flutter 3, Dart `>=3.1.5 <4.0.0`
* State management: **Riverpod** (`flutter_riverpod`, `riverpod_annotation`)
* Immutable models: **freezed** (regenerate after any model change, see below)
* Routing: **go_router** (`routes/app_router.dart` + generated `app_router.g.dart`)
* Networking: **dio**
* Local storage: `shared_preferences`
* Maps: `google_maps_flutter`

## Project layout (`lib/`)

* `core/` — app bootstrap (`app.dart`), environment flavors (`app_env.dart`,
  `main_dev.dart`/`main_staging.dart`), observers
* `configs/` — `app_configs.dart` (API base URL), constants, globals
* `features/` — feature-first clean architecture, one folder per feature
  (`auth`, `account`, `dashboard`, `googleMapScreen`, `welcome`), each split into
  `data`/`domain`/`presentation`
* `routes/` — go_router configuration
* `services/`, `shared/` — cross-cutting code shared across features

## Requirements

* Flutter 3 SDK
* Backend API running (see `../backend-api` or the repo root `CLAUDE.md`) — by default on
  `:8089`; point `lib/configs/app_configs.dart` at the right host if testing on a device/emulator
  (not `localhost`)

## Setup

```bash
flutter pub get
```

## Codegen

Regenerate `*.freezed.dart` / `*.g.dart` after touching any model:

```bash
dart run build_runner build --delete-conflicting-outputs
```

## Run

```bash
flutter run -t lib/core/main_dev.dart        # dev flavor
flutter run -t lib/core/main_staging.dart    # staging flavor
```

## Tests

```bash
flutter test
```

## Known state

This app is under active development alongside the web frontend and backend. There is leftover
tutorial-template code (Spoonacular sample references, a generic `shared/domain/models/product/`
model) inherited from the original project scaffold — ignore it, it is not part of the real
feature set.
