# ROADMAP.md

> Plan de reprise priorisé. Rôle : architecte. Complète `PROJECT_ANALYSIS.md`.
> Complexité estimée : **S** (≤0,5 j), **M** (0,5–2 j), **L** (2–5 j), **XL** (>5 j / itératif).
> Aucune tâche « refonte » ou « suppression » ne démarre sans validation explicite (règle projet).

---

## Orientation d'architecture (décidée)

Cible : **évolutif vers microservices** via un **monolithe modulaire** (ADR-0010), **Keycloak** comme fournisseur d'identité OIDC (ADR-0011), et **découplage des entités par identifiant** entre contextes (ADR-0012). Ces décisions modifient les tâches d'authentification ci-dessous (Keycloak remplace le JWT maison) et ajoutent le lot « frontières de contexte » (P1-7).

## P0 — Critique (bloquant : sécurité, intégrité, cœur métier)

### P0-A · Mettre en place Keycloak + backend en OAuth2 Resource Server
- **Objectif** : externaliser l'identité (OIDC) ; le backend valide les tokens Keycloak au lieu d'en fabriquer.
- **Justification** : supprime d'un coup les 3 failles d'auth (secret versionné, mint maison, mot de passe codé en dur) et centralise l'auth pour le futur maillage (R1, R2). **Reprise décidée le 2026-08-09** (ADR-0011, addendum) suite à l'audit `docs/API_AUDIT_AUTH_USERS_TENANT_RBAC` : l'auth maison n'atteint pas seule la complétude attendue par un frontend (pas de reset/changement de mot de passe).
- **Préalable non vérifié** : disponibilité réelle d'un démon Docker dans l'environnement cible — `docs/IMPLEMENTATION_LOG.md` (2026-08-06) ne le confirme toujours pas. À vérifier **avant** toute bascule de code, pas supposé.
- **Fichiers** : `pom.xml` (`spring-boot-starter-oauth2-resource-server`), `security/SecurityConfiguration.java`, **suppression** `security/JwtService.java` / `JwtFilter.java` / `constant/SecurityConstants.java`, converter de rôles, `docker-compose` (Keycloak), realm exporté versionné ; clients `sonaged_web/` et `mobileFlutter/` (flux PKCE).
- **Impact** : remplace l'auth maison ; migration des comptes ; adaptation des 3 clients. **Suppression de code sécurité → validation requise.**
- **Complexité** : L · **ADR-0011** · plan détaillé : `docs/PLAN_IDENTITE_TENANT_RBAC.md`, blueprint : `docs/keycloak-migration.md`.

### P0-B · Pont Identité & Accès en attendant Keycloak — ✅ **fait le 2026-08-09**
- **Objectif** : combler les manques et bugs relevés par l'audit sans attendre le chantier L ci-dessus.
- **Justification** : désactivation de compte inopérante (bug), `revokeAllForUser` jamais appelé, modification des permissions d'un rôle sans effet, aucun reset/changement de mot de passe.
- **Fichiers** : `UserController`/`UserServiceImpl` (activation/désactivation), `AuthorityServiceImpl` (permissions), `AuthController` (`change-password`, `password-reset/*`), `PasswordResetToken` (+ changeset `2.26.0`), `PermissionController`.
- **Impact** : vague 1 (bugs) et vague 2 (nouveaux endpoints) livrées le même jour. Suite complète (295 tests) verte contre H2 et contre la base réelle.
- **Complexité** : S (vague 1) / M (vague 2) · **ADR-0021**.

### P0-1 · (Stopgap) Hacher le mot de passe fourni à l'inscription
- **Objectif** : correctif immédiat **si** Keycloak (P0-A) n'est pas déployable tout de suite.
- **Justification** : `register()` fait `encode("Sonaged@123")` → **tous les comptes partagent le même mot de passe** (R1). À ne faire que comme mesure transitoire.
- **Fichiers** : `service/impl/AuthServiceImpl.java`, `dto/User.java`.
- **Impact** : réduit le risque en attendant Keycloak ; **rendu caduc par P0-A**.
- **Complexité** : S · **ADR-0003** (remplacé par ADR-0011).

### P0-2 · Externaliser et roter les secrets
- **Objectif** : plus aucun secret dans le code/versionné.
- **Justification** : secret JWT (`SecurityConstants`), mot de passe BD (`application.properties`), clé Google (`google-services.json`) exposés ; pas de `.gitignore` racine (R2).
- **Fichiers** : `constant/SecurityConstants.java`, `security/JwtService.java`, `resources/application.properties`, nouveau `.gitignore` racine, `resources/application.yml`.
- **Impact** : toute la config de déploiement ; rotation obligatoire des clés compromises.
- **Complexité** : M · **ADR-0002**.

### P0-3 · Durcir le pipeline JWT *(absorbé par P0-A)*
- **Objectif** : token invalide/expiré → **401** (pas 500) ; pas de crash au login.
- **Justification** : `JwtFilter` ne capture pas `ExpiredJwtException`/`JwtException` ; `roles.get(0)` plante sans rôle ; token loggué à chaque requête (R8, fuite).
- **Statut** : **rendu inutile par P0-A** — le resource server gère nativement 401/erreurs de token. Ne traiter que si le stopgap P0-1 est retenu (retirer alors le log du token).
- **Fichiers** : `security/JwtFilter.java`, `security/JwtService.java`.
- **Complexité** : S · **ADR-0003** (remplacé par ADR-0011).

### P0-4 · Trancher la gestion de schéma (Liquibase unique) — ✅ **fait**
- **Objectif** : une seule source de vérité du schéma.
- **Justification** : Liquibase **et** `ddl-auto=update` actifs → risque d'écrasement/dérive (R5).
- **Fichiers** : `resources/application.properties` (`ddl-auto=validate`), `pom.xml`, nouveaux changelogs `resources/db/changelog/`.
- **Impact** : déploiements reproductibles ; migration initiale à générer depuis l'existant.
- **Complexité** : M · **ADR-0001**.
- **Résultat** : `ddl-auto=validate` en vigueur, schéma exclusivement porté par Liquibase
  (`config/liquibase/master.xml`) — convention verrouillée, ne jamais repasser en `update`/`create`
  (voir `CLAUDE.md`).

### P0-5 · Modèle d'ingestion du niveau de remplissage (cœur métier) — ✅ **fait**
- **Objectif** : recevoir/stocker le niveau de remplissage d'un point de collecte.
- **Justification** : raison d'être du produit, aujourd'hui absente ; `Depotoir` n'a pas de `fillLevel`.
- **Fichiers** : nouveau `model/MeasurementEntity`, `repository/MeasurementRepository`, `controller/IngestionController` (`POST /v1/measurements`), `service/measurement/*`, ajout `Depotoir.fillLevel`/`lastMeasuredAt`.
- **Impact** : nouvelle brique centrale ; base des alertes.
- **Complexité** : L · **ADR-0004**.
- **Résultat** : livré via le contexte `iot` (`Sensor`, `Measurement`, device provisioning) +
  `waste.FillLevelProjector`, vérifié bout-en-bout contre PostgreSQL — voir la correction en tête
  de `CLAUDE.md` et `docs/IMPLEMENTATION_LOG.md`. L'ancienne note « `Alert` n'est pas relié à
  `Depotoir` » ne reflète plus l'état du code.

### P0-6 · Moteur de seuils + déclenchement automatique d'alerte — ✅ **fait**
- **Objectif** : seuil dépassé → création d'`Alert` reliée au dépotoir + notification.
- **Justification** : boucle métier « détecter → alerter » ; `Alert` n'est pas relié à `Depotoir`.
- **Fichiers** : `model/AlertEntity` (ajout `@ManyToOne Depotoir`), `service/AlertService`, nouveau `service/alerting/ThresholdEvaluator`, `service/impl/NotificationServiceImpl` (méthode d'alerte).
- **Impact** : cœur fonctionnel opérationnel.
- **Complexité** : L · **ADR-0004**, **ADR-0005**.
- **Résultat** : `waste.FillLevelProjector`/`ThresholdResolver` évaluent indépendamment
  remplissage, température et humidité contre `AlertThreshold` (par `TypeDepotoir` ou seuil global)
  et déclenchent l'`Alert` automatiquement — la chaîne complète capteur → mesure → seuil → alerte
  est verifiée, pas seulement scaffoldée.

---

## P1 — Important (fiabilité, échelle, fonctionnalités)

### P1-1 · Nettoyer les dépendances backend — ✅ **fait le 2026-07-25**
- **Objectif** : build stable et sans doublon.
- **Justification** : springfox 3.0.0 (abandonné, incompatible Boot 3) coexiste avec springdoc ; 2 libs JWT (R6).
- **Fichiers** : `pom.xml`.
- **Impact** : réduction du risque de conflit classpath ; démarrage plus sûr.
- **Complexité** : S · **ADR-0009**.

### P1-2 · Corriger le fetch/cascade des entités — ✅ **fait le 2026-08-11**
- **Objectif** : `LAZY` par défaut, retirer les `CascadeType.ALL` fautifs.
- **Justification** : EAGER en chaîne (Depotoir→Commune→Department→Region, Geometry) → N+1 ; `CascadeType.ALL` vers `TypeDepotoir` (référentiel partagé) peut supprimer des données de référence (R3, R4).
- **Fichiers** : `model/DepotoirEntity`, `CommuneEntity`, `DepartmentEntity`, `Circuit*Entity`.
- **Impact** : perfs + intégrité ; adapter les requêtes qui dépendaient de l'EAGER (fetch-joins/projections).
- **Complexité** : M · **ADR-0008**.
- **Résultat** : traité en deux passes. La chaîne `Depotoir/CircuitCollect/CircuitBalayage →
  Commune → Department → Region` et le cascade `Depotoir.typeDepotoir` sont passés en LAZY/cascade
  sûr le 2026-07-25. Cette itération (2026-08-11) ferme les angles morts restants : les deux
  dernières associations encore EAGER (`CircuitEntity.geometry`, `QuartierEntity.commune`), le
  cascade `ALL` inverse encore fautif (`TypeDepotoirEntity.depotoirs`), et surtout les DTO
  `Region`/`Department`/`Quartier` qui exposaient encore des **entités JPA brutes** — remplacées
  par des identifiants (même patron que `Commune.departmentId`), ce qui a aussi mis au jour et
  corrigé deux `NullPointerException` (département/quartier créés sans parent). `@Transactional`
  ajouté à 7 services du référentiel qui dépendaient implicitement d'`open-in-view`. Détail complet :
  `docs/IMPLEMENTATION_LOG.md`, entrée 2026-08-11.

### P1-3 · Sortir les images du BLOB — ✅ **fait le 2026-07-25**
- **Objectif** : ne plus stocker `displayPicture byte[]` dans la table `ALERT`.
- **Justification** : BLOB en table = tables lourdes, back-ups coûteux, mémoire (R3).
- **Fichiers** : `model/AlertEntity`, `service/impl/UploadFileServiceImpl`, `service/ImageService`.
- **Impact** : stockage fichier/objet + URL référencée.
- **Complexité** : M · **ADR-0005**.
- **minio**: utlise minio pour le stock de images et fichier et garder le filename en db
- **Résultat** : livré via MinIO (`ImageServiceImpl`, `MinioConfig`, `docker-compose.yml`).
  Nécessite MinIO démarré en local pour fonctionner (`docker compose -f
  backend-api/src/main/resources/docker-compose.yml up`) ; la migration des BLOB déjà en base
  n'a pas de job dédié (ADR-0005 §3).

### P1-4 · Aligner et consolider les frontends — ✅ **FAIT le 2026-08-05**
- **Résultat** : `sonaged_web/` est le front unique. `angular/` et `ucgFrontend/` supprimés
  (1 054 fichiers suivis). La décision d'ADR-0006 a été **inversée** : l'arbitrage s'est fait sur
  ce qui est coûteux à refaire (pile SIG Leaflet/proj4 + i18n) plutôt que sur la complétude
  fonctionnelle, la fraîcheur de l'API d'`angular/` ayant été récupérée avant suppression.
- **Reste à faire** : les défauts ouverts de `sonaged_web` sont listés dans
  `docs/FRONTEND_AUDIT.md` §5.2 — gardes de routes, chemins d'API au singulier, environnements
  de production, identifiants typés `number`.
- **ADR-0006** (statut : exécuté, décision inversée) · **`docs/FRONTEND_AUDIT.md`**.

### P1-5 · Import automatisé des GeoJSON — ✅ **fait le 2026-07-25**
- **Objectif** : charger `datas/*.json` (quartiers, circuits, dépotoirs) en base.
- **Justification** : données de référence indispensables à la carte/alertes.
- **Fichiers** : nouveau seeder/`db/changelog` ou `service/import/GeoJsonImportService`.
- **Impact** : jeu de données réel exploitable.
- **Complexité** : M.
- **Résultat** : `GeoJsonImportService(+Impl)`, `GeoJsonImportController`
  (`POST /v1/admin/import/geojson`), déclenchable aussi au démarrage
  (`sonaged.import.geojson.on-startup=true`).

### P1-6 · Réactiver le lien Depotoir ↔ Quartier — ✅ **fait le 2026-07-26**
- **Objectif** : rétablir la relation `Depotoir *─1 Quartier` (~~actuellement commentée~~ — fait,
  voir Résultat).
- **Justification** : granularité de supervision (quartier) attendue métier.
- **Fichiers** : `model/DepotoirEntity`, changelog Liquibase.
- **Impact** : requêtes/carto par quartier. **NB** : entre contextes distincts, la relier par `quartierId` et non par association objet (ADR-0012).
- **Complexité** : S.
- **Résultat** : `Depotoir.quartierId` (référence par identifiant, pas association objet — ADR-0012)
  ; aucune migration Liquibase requise, la colonne existait déjà dans le baseline. `DepotoirMaps`
  n'expose pas encore le quartier (amélioration cartographique ultérieure, pas bloquant).

### P1-7 · Frontières de contexte (Spring Modulith) + découplage des entités — ✅ **fait le 2026-07-28**
- **Objectif** : matérialiser les bounded contexts et remplacer les associations JPA **cross-contexte** par des références par identifiant.
- **Justification** : condition de faisabilité de l'évolution microservices ; réduit le couplage fort et les N+1 (R3, R7).
- **Fichiers** : ~~réorganisation en 5 modules — cf. `docs/architecture-cible.md`~~ **caduc** : cette tâche P1-7 décrit le découpage ADR-0010 (5 modules), remplacé par l'ADR-0013 (7 contextes bornés + 3 non-contextes sous `sn.smartwaste.collect`), **déjà réalisé** (migration terminée le 2026-07-28, `docs/IMPLEMENTATION_LOG.md`). `docs/architecture-cible.md` porte son propre bandeau obsolète — ne plus y renvoyer comme référence de cible. `pom.xml` (Spring Modulith) et le découplage par identifiant (FK objet → `…Id`) sont faits ; changelogs Liquibase associés déjà joués.
- **Impact** : structurant ; refactoring progressif contexte par contexte, testé par Modulith. **Pas de suppression massive sans validation.**
- **Complexité** : L · **ADR-0010**, **ADR-0012**.

---

## P2 — Amélioration (qualité, confort, évolutivité)

### P2-1 · Notifications temps réel — ✅ **fait le 2026-07-26**
- **Objectif** : pousser les alertes vers les superviseurs sans polling.
- **Justification** : vision produit « temps réel ».
- **Fichiers** : nouveau `controller/NotificationSseController` (SSE) ou WebSocket ; front abonnement.
- **Impact** : UX supervision.
- **Complexité** : L · **ADR-0007**.
- **Résultat** : SSE bout-en-bout (`AlertStreamController`, `AlertBroadcaster`), plus G2 (canal hors
  application ouverte, 2026-08-05) au-dessus.

### P2-2 · Tests automatisés + CI/CD — ✅ **fait**
- **Objectif** : couvrir services critiques (auth, seuils, alertes) + pipeline.
- **Justification** : ~~~2 tests aujourd'hui ; aucune CI~~ — périmé, voir Résultat.
- **Fichiers** : `backend-api/src/test/**`, `.github/workflows/*`.
- **Impact** : non-régression.
- **Complexité** : L.
- **Résultat** : 319 tests (`./mvnw test`) ; CI GitHub Actions (`.github/workflows/backend.yml`)
  construit le jar, l'exécute contre un PostgreSQL **vierge** provisionné par le job (schéma
  appliqué depuis zéro à chaque run, jamais vérifié avant le 2026-08-05), confronte les entités au
  schéma via `ddl-auto=validate`, puis lance la suite complète. Tourne sur chaque push/PR.

### P2-3 · Multi-tenant (plusieurs collectivités) — ✅ **fait, cloisonnement vérifié effectif le 2026-08-11**
- **Objectif** : isoler les données par collectivité + exposer une API Organisation.
- **Mise à jour 2026-08-09** : l'API Organisation (`OrganizationController`, `/v1/organizations*`,
  permission `MANAGE_ORGANIZATIONS`) a été livrée en parallèle de ce cadrage — voir
  `docs/TENANT_ORGANIZATIONS_PLAN.md` et ADR-0020 §5.
- **Justification** : évolution cible « plusieurs collectivités ».
- **Fichiers** : `Commune` + `Depotoir`/`MoblierUrbain`/`Circuit*`/`Alert`/`Vehicle`/`Sensor`/
  `VehicleTracker`/`CollectionSchedule`/`AlertThreshold` (colonne `organizationId`), filtre Hibernate
  activé après authentification, nouveau `tenant/presentation/controller/OrganizationController`.
- **Impact** : discriminant `organizationId` posé sur 12 entités et API `/v1/organizations*` livrés
  le 2026-08-10 (changelog `2.27.0`). ⚠️ **Corrigé le 2026-08-11** : le filtre Hibernate
  d'isolation posé le 2026-08-10 était en réalité **inopérant** — mesuré contre PostgreSQL réel, un
  `ADMIN` d'une autre collectivité voyait les mêmes chiffres qu'un `SUPER_ADMIN` (71 dépotoirs, 52
  circuits, 12 communes). Deux causes : (1) `TenantFilterActivationFilter` s'exécutait avant que
  l'`EntityManager` d'open-in-view soit lié au thread, donc activait le filtre sur une session
  jetée aussitôt (corrigé par `PersistenceSessionBindingConfig`, ordonnancement explicite) ; (2) un
  `@Filter` Hibernate ne couvre pas `EntityManager.find()`/`findById` — étanche en liste, ouvert à
  l'unité (corrigé par `applyToLoadByKey = true` sur le `@FilterDef`, 32 sites `findById` couverts
  d'un coup). Un sélecteur `X-Organization-Id` a été ajouté pour le `SUPER_ADMIN` (observer une
  collectivité sans changer de compte, ignoré pour tout autre rôle). **Revérifié contre PostgreSQL
  réel après correctif** : `ADMIN` d'une autre collectivité → 0 partout, accès par identifiant hors
  périmètre → 404. Tout nouveau compte est rattaché automatiquement à Pikine
  (`UserAccountCreated`/`DefaultOrganizationEnrollmentListener`).
- **Leçon** : le test qui couvrait ce composant avant le correctif utilisait une `Session` mockée —
  il ne pouvait donc pas prouver le filtrage effectif, et n'a pas détecté la régression. Symptomatique
  d'un principe déjà noté ailleurs dans ce projet : « ce qui n'a jamais été exécuté contre une vraie
  base ne fonctionne pas forcément ».
- **Complexité** : XL, réalisée et vérifiée effective · **ADR-0008**, **ADR-0020** · plan détaillé : `docs/PLAN_IDENTITE_TENANT_RBAC.md`.

### P2-4 · Nettoyage & documentation
- **Objectif** : README racine réel, `endpoint.md` correct, retrait des restes de template Flutter, homogénéiser UCG/SONAGED.
- **Fichiers** : docs, `mobile/lib/core/app_env.dart`, `shared/domain/models/product/*`.
- **Impact** : onboarding.
- **Complexité** : S–M.
- **État (2026-08-13)** : README racine réel — fait. `endpoint.md` — banni comme périmé, pointe
  vers Swagger, pas réécrit ligne à ligne (voir `docs/IMPLEMENTATION_LOG.md`). Restes de template
  Flutter — **plus gros que prévu** : `product_model.dart` n'est pas un fichier isolé à supprimer,
  c'est la dépendance d'un écran entier et routé (`features/dashboard/`, `/dashboard`, 10 fichiers)
  qui affiche encore des données tutoriel plutôt que des données SONAGED — décision explicite du
  2026-08-13 de ne rien supprimer tant qu'aucun remplacement n'existe (voir `CLAUDE.md`, section
  Mobile). Homogénéiser UCG/SONAGED — non fait, cosmétique, faible priorité.

### P2-5 · Dashboards avancés — ✅ **fait le 2026-07-26**
- **Objectif** : indicateurs (taux de remplissage, alertes/jour, tournées).
- **Fichiers** : `service/impl/DashboardServiceImpl`, front dashboard.
- **Complexité** : M.

---

## Séquencement recommandé
`P0-1 → P0-2 → P0-3` (sécurité, faible risque) → `P0-4` (schéma) → `P0-5 → P0-6` (cœur métier) → `P1-1..P1-4` (fiabilité/échelle/fronts) → `P1-5/P1-6` → `P2`.

**Ajout 2026-08-09** : `P0-B` (pont identité, vague 1 faite) → vérification infra Docker → `P0-A`
(Keycloak) en parallèle de `P2-3` (multi-tenant, ADR-0020) — les deux sont indépendants et
n'ont pas à être séquencés l'un après l'autre (ADR-0020 §7).
