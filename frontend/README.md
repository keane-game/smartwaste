# SONAGED — Web Frontend

Angular web frontend for **"Gestion automatisée des ordures ménagères"**, a waste-collection
supervision system for Pikine, Senegal (backend-branded **SONAGED**): supervising collection
points, tracking fill level, triggering alerts, and visualizing collection circuits on a map.

This app talks to the Spring Boot API in `../backend-api`.

## Stack

* Angular 17.3 (standalone bootstrap in `app.config.ts`, routing in `app.routes.ts`; most
  screens are still NgModules)
* Bootstrap 5 + Angular Material 17.3, SweetAlert2 for dialogs
* Leaflet + proj4 + esri-leaflet for GIS/map rendering
* ngx-translate for i18n
* Karma/Jasmine for unit tests

## Project layout (`src/app`)

* `core/` — login, password, guards, interceptors, cross-cutting services
* `pages/` — one folder per resource (alert, commune, depotoir, quartier, users, maps,
  dashboard, avis…)
* `shared/` — shared components, constants (incl. `constants/api-endpoints.ts`, the canonical
  registry of backend paths — use it instead of hardcoding paths), Material wrappers
* `services/` — `shared.service` is the generic API client
* `models/`, `directives/` — shared types and directives

Backend URLs come from `src/environments/environment*.ts`. There are **three** API bases:
`apiUrl` (`/v1`), `authUrl` (`/auth`), `dataUrl` (`/data`).

## Requirements

* Node.js + npm
* Backend API running (see `../backend-api/README.md` or `CLAUDE.md` at the repo root) — by
  default on `:8089`

## Installation

```bash
npm install       # install dependencies
npm outdated      # check dependency freshness
```

## Development

```bash
npm start
```

Then open [http://localhost:4200](http://localhost:4200). The dev server proxies API calls
according to `src/environments/environment.ts`.

## Linting

```bash
npm run lint
```

## Tests

```bash
npm test           # karma/jasmine, watch mode
npm run coverage    # ng test --no-watch --code-coverage
```

To run a single spec, mark it with `fdescribe`/`fit` in the spec file.

## Build

```bash
npm run build       # production build (no SSR), output to dist/sonaged
```

## Serve a production build

```bash
npm run serve
```

Then open [http://localhost:4000](http://localhost:4000). `npm run serve` runs `server.js`,
which serves static files via Express.

> ⚠️ `server.js` currently serves from `dist/angular-starter/browser`, but `ng build` outputs
> to `dist/sonaged` (see `angular.json`). Until this is reconciled, `npm run build` followed by
> `npm run serve` will not serve the freshly built app — check `server.js` and `angular.json`
> before relying on this path.

## Known open issues

See `docs/FRONTEND_AUDIT.md` at the repo root for the full list. Notably:

* `Avis.id` is still `int` on the backend while every other migrated resource is UUID v7
  (`circuit-balayages` and the rest of `waste` finished the migration; `Avis` did not).
* `pages/maps/esri/esri.component.ts` (routed at `/map`, outside the auth guard) is an
  orphaned prototype with hardcoded coordinates — unreachable from app navigation.
* `pages/circuit-collect/` is a copy-paste of the user list screen, not a real feature yet.
* The 55 CLI-scaffolded spec files are still unwritten stubs.
* Refresh tokens are stored in `localStorage`, not an httpOnly cookie (needs backend support
  to fix properly).

## Author

* Keaner
