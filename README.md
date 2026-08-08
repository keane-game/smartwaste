# Gestion automatisée des ordures ménagères — SONAGED / UCG

Système de supervision de la collecte des déchets pour **Pikine (Sénégal)**.
Objectif : superviser les points de collecte, détecter leur niveau de remplissage,
déclencher des alertes, optimiser les tournées et visualiser le tout sur une carte.

> ✅ **État réel du produit** (corrigé le 2026-08-06) : la chaîne « capteur → mesure → seuil →
> alerte automatique » (contexte `iot` : `Sensor`, `Measurement`, `VehicleTracker`, provisioning
> des appareils, plus `waste.FillLevelProjector`/`ThresholdResolver`) **est implémentée** et
> vérifiée de bout en bout contre PostgreSQL — `Alert` n'est plus une simple ressource CRUD.
> `FillLevelProjector` évalue indépendamment niveau de remplissage, température et humidité,
> chacun contre son propre `AlertThreshold`. Ce qui manque encore réellement : l'optimisation
> des tournées par niveau de remplissage réel (les tournées restent triées par priorité, pas
> re-séquencées par la donnée live). Voir [`docs/IMPLEMENTATION_LOG.md`](docs/IMPLEMENTATION_LOG.md)
> (source de vérité) et [`ROADMAP.md`](ROADMAP.md). `PROJECT_STATUS.md`/`PROJECT_ANALYSIS.md` sont
> des **archives figées au 2026-07-11** — ne pas s'y fier.

## Sous-projets

| Dossier | Stack | Rôle |
|---|---|---|
| `backend-api/` | Java 21, Spring Boot 3.5.3, Maven | API REST — **le** backend (renommé depuis `ucgBackend/` le 2026-08-06). Deux racines pendant la migration ADR-0013 : `sn.smartwaste.collect` (cible, DDD) et `sonaged.collecte.master` (legacy) |
| `frontend/` | Angular 17.3 | **Le** frontend web (renommé depuis `sonaged_web/` le 2026-08-06 ; seul survivant de la consolidation du 2026-08-05 — `angular/` et `ucgFrontend/` supprimés, cf. ADR-0006 et `docs/FRONTEND_AUDIT.md`) |
| `mobile/` | Flutter 3 | Application mobile (renommé depuis `mobileFlutter/` le 2026-08-06) |
| `datas/` | GeoJSON | 13 fichiers de données géo réelles de Pikine (quartiers, communes, circuits, dépotoirs, bacs de rue, points propres, caisses polybennes, pré-collecte) — importables via `POST /v1/admin/import/geojson` |

> Le dépôt et les dossiers disent `ucg`, le code et la marque disent `sonaged` : incohérence assumée.
> ⚠️ Le renommage `sonaged_web/ucgBackend/mobileFlutter` → `frontend/backend-api/mobile` n'a pas
> encore été committé avec l'historique préservé (`git mv`) — les chemins ci-dessus reflètent
> l'état réel du disque, pas encore celui de `git log`.

## Démarrage rapide

### Prérequis
- **JDK 21**, **Node 18+**, **PostgreSQL** sur `localhost:5433`, base `sonaged`
- Optionnel : **Docker** pour MinIO (images) et smtp4dev (courrier)

### Backend
```bash
cd backend-api
cp .env.example .env          # renseigner DB_PASSWORD, MAIL_*, MINIO_*
./mvnw spring-boot:run        # API sur http://localhost:8089
```
- Swagger UI : <http://localhost:8089/swagger-ui> — OpenAPI : `/sonaged-docs`
- Liste des endpoints : [`backend-api/endpoint.md`](backend-api/endpoint.md)
- Services annexes : `docker compose -f backend-api/src/main/resources/docker-compose.yml up`
  (MinIO sur 9000/9001, smtp4dev)

```bash
./mvnw clean package   # build + tests
./mvnw test            # tests seuls
```

### Frontend web
```bash
cd frontend
npm install
npm start              # http://localhost:4200
npm run build && npm run lint
```
Voir [`frontend/README.md`](frontend/README.md) pour le détail (structure, tests, build de prod).

### Mobile
```bash
cd mobile
flutter pub get
dart run build_runner build --delete-conflicting-outputs   # après modification des modèles
flutter run -t lib/core/main_dev.dart
```
Voir [`mobile/README.md`](mobile/README.md) pour le détail.

## Architecture

**Backend en couches** : `controller → service → service.impl → repository → model`,
avec `dto` + mappers MapStruct (motif d'instance statique : `XxxMapper.UMP.asModel(dto)` —
ne pas les injecter). Sécurité : JWT maison (`security/`), sujet = e-mail de l'utilisateur.

**Frontend** : un écran par ressource sous `frontend/src/app/pages/`. Les chemins d'API sont
centralisés dans `src/app/shared/constants/api-endpoints.ts` — s'en servir plutôt que d'écrire
une URL en dur : onze composants avaient codé des chemins au singulier que le backend n'expose
pas. Trois bases d'URL cohabitent (`apiUrl` = `/v1`, `authUrl` = `/auth`, `dataUrl` = `/data`),
et `/avis` est monté à la racine du serveur.

**Décisions d'architecture** : [`docs/adr/`](docs/adr/) (**13 ADR**, avec leur statut réel) — cible
**architecture DDD en 8 contextes bornés** ([ADR-0013](docs/adr/0013-architecture-ddd-smartwaste.md),
qui **remplace** ADR-0010), **Keycloak** comme fournisseur d'identité (ADR-0011, non implémenté),
**découplage des entités par identifiant** (ADR-0012, appliqué).

> ⚠️ [`docs/architecture-cible.md`](docs/architecture-cible.md) décrit le découpage de l'**ADR-0010,
> remplacé**. Il est conservé pour l'historique — la cible actuelle est l'ADR-0013.

**Cartographie documentaire** : [`docs/KNOWLEDGE_MAP.md`](docs/KNOWLEDGE_MAP.md) — inventaire de
toutes les sources, incohérences entre documents, fonctionnalités spécifiées mais absentes.

**Journal d'implémentation** : [`docs/IMPLEMENTATION_LOG.md`](docs/IMPLEMENTATION_LOG.md) —
ce qui a été fait, pourquoi, et ce qui reste à vérifier.

### Schéma de base
Le schéma appartient **exclusivement à Liquibase**
(`backend-api/src/main/resources/config/liquibase/master.xml`) ; Hibernate est en
`ddl-auto=validate`. **Ne pas** repasser en `update`/`create`.

> ⚠️ `backend-api/src/main/resources/schema.sql` commence par `DROP DATABASE`. Il est
> neutralisé (`spring.sql.init.mode: never`) et ne doit **jamais** être réactivé.

## Points sensibles connus

- **Secrets historiquement versionnés** (secret JWT, mot de passe BD, clé Google). La
  configuration est passée aux variables d'environnement, mais **les secrets présents dans
  l'historique Git doivent être tournés** — ne pas en ajouter de nouveaux.
- **Couverture de tests quasi nulle** sur la logique métier (soft-delete, MinIO, import
  GeoJSON, alertes) : le compilateur est aujourd'hui le principal filet de sécurité.
- **Règle de travail du projet** : aucun refactoring majeur ni aucune suppression
  **sans validation explicite**. (La consolidation des fronts du 2026-08-05 a été validée
  explicitement ; elle est faite.)

## Licence
Projet académique (mémoire de master). Aucune licence publique définie.
