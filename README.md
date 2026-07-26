# Gestion automatisée des ordures ménagères — SONAGED / UCG

Système de supervision de la collecte des déchets pour **Pikine (Sénégal)**.
Objectif : superviser les points de collecte, détecter leur niveau de remplissage,
déclencher des alertes, optimiser les tournées et visualiser le tout sur une carte.

> ⚠️ **État réel du produit** : le cœur « niveau de remplissage → alerte automatique »
> (chaîne d'ingestion IoT, ADR-0004) **n'est pas implémenté** — c'est un module réservé.
> `Alert` est aujourd'hui une ressource CRUD, alimentée manuellement, désormais diffusée
> en temps réel (SSE). Voir [`PROJECT_STATUS.md`](PROJECT_STATUS.md) et [`ROADMAP.md`](ROADMAP.md).

## Sous-projets

| Dossier | Stack | Rôle |
|---|---|---|
| `ucgBackend/` | Java 21, Spring Boot 3.5.3, Maven | API REST (`sonaged.collecte.master`) — **le** backend |
| `angular/` | Angular 17 | Frontend web **principal** |
| `sonaged_web/` | Angular | Second frontend, quasi-doublon (choix canonique non tranché) |
| `ucgFrontend/` | Angular 16 | Scaffold **mort** — ne rien y construire |
| `mobileFlutter/` | Flutter 3 | Application mobile |
| `datas/` | GeoJSON | Données géo de Pikine (quartiers, circuits, dépotoirs, bacs) |

> Le dépôt et les dossiers disent `ucg`, le code et la marque disent `sonaged` : incohérence assumée.

## Démarrage rapide

### Prérequis
- **JDK 21**, **Node 18+**, **PostgreSQL** sur `localhost:5433`, base `sonaged`
- Optionnel : **Docker** pour MinIO (images) et smtp4dev (courrier)

### Backend
```bash
cd ucgBackend
cp .env.example .env          # renseigner DB_PASSWORD, MAIL_*, MINIO_*
./mvnw spring-boot:run        # API sur http://localhost:8089
```
- Swagger UI : <http://localhost:8089/swagger-ui> — OpenAPI : `/sonaged-docs`
- Liste des endpoints : [`ucgBackend/endpoint.md`](ucgBackend/endpoint.md)
- Services annexes : `docker compose -f ucgBackend/src/main/resources/docker-compose.yml up`
  (MinIO sur 9000/9001, smtp4dev)

```bash
./mvnw clean package   # build + tests
./mvnw test            # tests seuls
```

### Frontend web
```bash
cd angular
npm install
npm start              # http://localhost:4200
npm run build && npm run lint
```

### Mobile
```bash
cd mobileFlutter
flutter pub get
dart run build_runner build --delete-conflicting-outputs   # après modification des modèles
flutter run -t lib/core/main_dev.dart
```

## Architecture

**Backend en couches** : `controller → service → service.impl → repository → model`,
avec `dto` + mappers MapStruct (motif d'instance statique : `XxxMapper.UMP.asModel(dto)` —
ne pas les injecter). Sécurité : JWT maison (`security/`), sujet = e-mail de l'utilisateur.

**Frontend** : les écrans CRUD des ressources de référence sont générés à partir d'un registre
déclaratif (`angular/src/app/shares/crud/entity-config.ts`) rendu par des composants génériques
`EntityListComponent` / `EntityFormComponent`. Ajouter une ressource = ajouter une entrée au
registre plus une route.

**Décisions d'architecture** : [`docs/adr/`](docs/adr/) (12 ADR) — cible **monolithe modulaire
orienté microservices** (ADR-0010), **Keycloak** comme fournisseur d'identité (ADR-0011),
**découplage des entités par identifiant entre contextes** (ADR-0012, appliqué en P1-7a).

**Journal d'implémentation** : [`docs/IMPLEMENTATION_LOG.md`](docs/IMPLEMENTATION_LOG.md) —
ce qui a été fait, pourquoi, et ce qui reste à vérifier.

### Schéma de base
Le schéma appartient **exclusivement à Liquibase**
(`ucgBackend/src/main/resources/config/liquibase/master.xml`) ; Hibernate est en
`ddl-auto=validate`. **Ne pas** repasser en `update`/`create`.

> ⚠️ `ucgBackend/src/main/resources/schema.sql` commence par `DROP DATABASE`. Il est
> neutralisé (`spring.sql.init.mode: never`) et ne doit **jamais** être réactivé.

## Points sensibles connus

- **Secrets historiquement versionnés** (secret JWT, mot de passe BD, clé Google). La
  configuration est passée aux variables d'environnement, mais **les secrets présents dans
  l'historique Git doivent être tournés** — ne pas en ajouter de nouveaux.
- **Couverture de tests quasi nulle** sur la logique métier (soft-delete, MinIO, import
  GeoJSON, alertes) : le compilateur est aujourd'hui le principal filet de sécurité.
- **Règle de travail du projet** : aucun refactoring majeur ni aucune suppression
  (par ex. retirer `ucgFrontend`) **sans validation explicite**.

## Licence
Projet académique (mémoire de master). Aucune licence publique définie.
