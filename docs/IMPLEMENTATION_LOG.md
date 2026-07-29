# Journal d'implémentation (boucle P0→P2)

> Suivi de la boucle `/loop` (toutes les 10 min) qui implémente les tâches de `ROADMAP.md`.
> Ordre : P0 → P1 → P2, une tâche par exécution. **IoT (P0-5/P0-6) ignoré** (module réservé, décision utilisateur).
> ✅ **2026-07-26 — JDK installé, build vérifié.** L'avertissement « environnement sans JDK » qui
> figurait ici n'est plus d'actualité : Temurin 21 est installé dans `~/.local/jdk/jdk-21.0.12+8`.
> `./mvnw clean package` **passe**, tests compris. Les mentions « non compilé » des lignes
> ci-dessous sont donc **caduques** — tout le code listé compile désormais (cf. entrée
> « Montée de version + premier build vert »). Pour rejouer le build :
>
> ```bash
> export JAVA_HOME=~/.local/jdk/jdk-21.0.12+8
> export PATH=$JAVA_HOME/bin:$PATH
> ./mvnw clean package
> ```

| Date | Tâche | Fichiers | Statut | À vérifier manuellement |
|---|---|---|---|---|
| 2026-07-11 | **P0-1** Hacher le mot de passe fourni à l'inscription | `ucgBackend/.../service/impl/AuthServiceImpl.java` | ✅ implémenté (non compilé) | Compilation ; l'inscription hache bien le mot de passe saisi ; réinitialiser les comptes créés avec l'ancien `Sonaged@123` |
| 2026-07-11 | **P0-2** Externaliser les secrets (props env) + `.gitignore` racine | `application.properties`, `.gitignore` (racine), `ucgBackend/.env.example` | ✅ partiel (non compilé) | Définir les variables d'env (`DB_PASSWORD`…) avant démarrage ; **retirer du suivi Git + roter** `google-services.json` et les secrets déjà dans l'historique ; externalisation du secret JWT reportée à P0-A (Keycloak) |
| 2026-07-11 | **P0-3** Durcir le pipeline JWT | `security/JwtFilter.java`, `security/JwtService.java` | ✅ implémenté (non compilé) | Compilation ; token expiré/invalide → 401 (pas 500) ; plus de token dans les logs ; login OK même sans rôle |
| 2026-07-11 | **P0-A** Keycloak / Resource Server — **scaffolding uniquement** | `pom.xml`, `application-keycloak.properties`, `docs/keycloak-migration.md` | 🟡 scaffolding (non compilé, non activé) | Nécessite infra Keycloak ; suivre `docs/keycloak-migration.md` (config Java, retrait JWT maison, clients PKCE, migration comptes) — **non réalisé ici** |
| 2026-07-11 | **P0-4** Liquibase source unique (`ddl-auto=validate`) + fuite `.yml` (P0-2) | `application.properties`, `application.yml` | ✅ implémenté (non compilé) | `validate` exige que les changelogs (`1.0.0`/`1.1.0`) correspondent aux entités → risque d'échec au démarrage si drift ; désactiver `schema.sql` (`initialization-mode`) pour que Liquibase soit vraiment seul ; **2 fichiers de config en double** (properties+yml) à consolider |
| 2026-07-25 | **P1-1** Nettoyer les dépendances backend (springfox + 2e lib JWT) | `ucgBackend/pom.xml` | ✅ implémenté (non compilé) | `./mvnw dependency:tree` pour confirmer l'absence de springfox / com.auth0 dans le classpath ; démarrage app + Swagger UI (`/swagger-ui`) toujours servi par springdoc ; login JWT toujours OK (jjwt conservé) |
| 2026-07-25 | **P1-2** Fetch LAZY par défaut + cascades sûres | `model/DepotoirEntity`, `CommuneEntity`, `DepartmentEntity`, `CircuitCollectEntity`, `CircuitBalayageEntity` | ✅ implémenté (non compilé) | Vérifier absence de `LazyInitializationException` sur les chemins qui lisent commune/department/region/typeDepotoir **hors** transaction/après fermeture de session (carte `MapsController`, `DashboardController`, mappers MapStruct) → ajouter fetch-joins/projections si besoin ; confirmer que `getTypeDepotoir().getTypeDepotoirId()` (DepotoirServiceImpl) et `getCommune().getCommuneId()` (QuartierServiceImpl) restent OK (lecture d'ID sur proxy = pas de hit DB) ; vérifier qu'aucun flux ne s'appuyait sur `CascadeType.ALL` Depotoir→TypeDepotoir pour créer/supprimer un type |
| 2026-07-26 | **P1-6** Réactiver le lien Depotoir ↔ Quartier (par `quartierId`) | `model/DepotoirEntity`, `dto/Depotoir`, `service/impl/DepotoirServiceImpl` | ✅ implémenté (non compilé) | **Aucune migration Liquibase** : la colonne `quartierid` (+ FK) existe déjà dans le baseline 1.0.0 → la réactivation aligne l'entité sur le schéma (réduit la dérive `validate`) ; lien par **identifiant** (ADR-0012), pas d'association objet → MapStruct mappe `quartierId` dto↔entity directement ; **FK physique cross-contexte laissée en place** (retrait systématique = P1-7) ; pas d'import GeoJSON pour ce champ (le fichier `depotoir.json` ne porte pas de quartier) ; vérifier CRUD Depotoir (POST/GET expose `quartierId`) ; `DepotoirMaps` n'expose pas encore le quartier (carto par quartier = amélioration ultérieure) ; `quartierRepository`/`typeDepotoirRepository` injectés mais inutilisés dans `DepotoirServiceImpl` (mort pré-existant, laissés) |
| 2026-07-25 | **P1-5** Import automatisé des GeoJSON (`datas/*.json` → base) | `service/geojson/GeoJsonImportService(+Impl)`, `service/geojson/InMemoryMultipartFile`, `service/geojson/GeoJsonImportRunner`, `controller/GeoJsonImportController`, `application.properties` | ✅ implémenté (non compilé) | Réutilise `UploadFileService` (aucune logique de parsing dupliquée) ; **lancer le backend depuis `ucgBackend/`** pour que `file:../datas` résolve la racine (sinon régler `GEOJSON_DIR`) ; idempotence par `count()` d'entité (pas d'upsert → `force=true` duplique) ; déclencher via `POST /v1/admin/import/geojson` (authentifié) ou `sonaged.import.geojson.on-startup=true` ; dépend de `region.sql` (region id 1) et de l'ordre département→commune→quartier→circuits→dépotoir ; **bloqué tant que le schéma `validate` ne démarre pas** (drift baseline, cf. P0-4) ; vérifier que les clés d'attributs des fichiers correspondent aux `parseXxx` (déjà OK en upload manuel) |
| 2026-07-25 | **P1-3** Sortir les images du BLOB (→ **MinIO**) | `model/AlertEntity`, `model/ImageEntity`, `dto/Image`, `service/ImageService`, `service/impl/ImageServiceImpl` (MinIO), `service/impl/AlertServiceImpl`, `config/MinioConfig`, `pom.xml` (`io.minio:minio`), `application.yml` (`sonaged.storage.minio.*`), `docker-compose.yml` (service minio), `.env.example`, `config/liquibase/changelog/1.2.0_image_out_of_blob.xml` + `master.xml` | ✅ implémenté (non compilé) | Démarrer MinIO (`docker compose -f src/main/resources/docker-compose.yml up`) ; bucket créé au 1er upload ; `image.url` = `{endpoint}/{bucket}/{clé}` → **rendre le bucket lisible** (policy) ou passer par des URLs présignées/proxy (`MINIO_PUBLIC_URL`) ; tester upload alerte → objet présent dans le bucket + URL accessible ; **Naming strategy ambiguë** : changelog cible `displaypicture`/`imageid` en minuscules (convention baseline) — vérifier le nom physique avant `dropColumn` (préconditions `MARK_RAN` = tolérant) ; `ddl-auto=validate` en dérive baseline (cf. P0-4) → l'app peut ne pas démarrer ; **migrer les BLOB existants** (ADR-0005 §3) vers MinIO (job non fourni) ; front/mobile : consommer `image.url` (plus de base64) |

## Détail

### P0-1 — Hacher le mot de passe fourni (2026-07-11)
- **Problème** : `register()` faisait `passwordEncoder.encode("Sonaged@123")` → tous les comptes partageaient le même mot de passe.
- **Cause** : constante codée en dur au lieu du mot de passe soumis (`user.getUserPassword()`).
- **Approche** : valider la présence du mot de passe puis hacher la valeur soumise.
- **Impacts** : le champ `userPassword` du DTO `User` (write-only) est désormais utilisé ; les comptes existants créés avec `Sonaged@123` doivent être réinitialisés.
- **Statut** : implémenté, **non compilé** (pas de JDK dans l'environnement).

### P0-2 — Externaliser les secrets (2026-07-11)
- **Problème** : mot de passe BD (`keane`), identifiants SMTP en clair dans `application.properties` ; pas de `.gitignore` racine (2 100+ fichiers suivis, dont `google-services.json`).
- **Cause** : configuration sensible codée en dur et versionnée.
- **Approche** : remplacer les valeurs sensibles par des placeholders `${VAR:defaut}` ; ajouter un `.gitignore` racine ; fournir `.env.example`.
- **Impacts** : les secrets doivent désormais venir de l'environnement (`DB_PASSWORD`, `MAIL_USERNAME`, `MAIL_PASSWORD`) ; `spring.datasource.password=${DB_PASSWORD:}` (vide par défaut → à fournir). **Non traité ici** : retrait du suivi Git + rotation de `google-services.json` et purge de l'historique (opération Git sensible) ; externalisation du secret JWT `SecurityConstants.SECRET` reportée à **P0-A** (Keycloak supprime la signature JWT maison — éviter un refactor jetable).
- **Statut** : implémenté (partiel), **non compilé**.

### P0-3 — Durcir le pipeline JWT (2026-07-11)
- **Problème** : token expiré/invalide → 500 (pas de try/catch) ; token journalisé à chaque requête ; `roles.get(0)` plante si l'utilisateur n'a aucun rôle.
- **Cause** : parsing JWT non protégé + log de debug oublié + accès non gardé à la liste des rôles.
- **Approche** : (1) supprimer `logger.error(...Authorization)` ; (2) entourer `isTokenExpired`/`extractUsername` d'un `try/catch (JwtException)` → requête non authentifiée (401) ; (3) `roles.isEmpty() ? "" : roles.get(0)`.
- **Impacts** : `JwtFilter` (import `io.jsonwebtoken.JwtException`), `JwtService`. Comportement inchangé pour un token valide ; claim `role` informatif seulement (l'autz utilise les authorities en base).
- **Statut** : implémenté, **non compilé**. Reste pertinent tant que P0-A (Keycloak) n'est pas déployé ; sera supprimé avec la bascule resource server.

### P0-A — Keycloak / Resource Server (scaffolding) (2026-07-11)
- **Problème** : auth JWT maison (secret versionné, minting, mot de passe) à remplacer par Keycloak (ADR-0011) ; cible microservices → auth centralisée.
- **Cause** : sécurité maison non standard, non SSO.
- **Approche (scaffolding SÛR, pas de bascule)** : (1) ajout dép `spring-boot-starter-oauth2-resource-server` — **inerte** sans issuer-uri ; (2) profil `keycloak` désactivé par défaut (`application-keycloak.properties`) ; (3) blueprint complet `docs/keycloak-migration.md`. **Aucune classe Java de sécurité créée/supprimée** (éviter de casser un build non compilable ici).
- **Impacts** : nul tant que le profil `keycloak` n'est pas actif ; le JWT maison (durci en P0-3) reste opérationnel entre-temps.
- **Statut** : 🟡 scaffolding, **non compilé, non activé**. Bascule à faire en session dédiée avec JDK + Keycloak.

### P0-4 — Liquibase source unique du schéma (2026-07-11)
- **Problème** : `ddl-auto=update` (Hibernate mute le schéma) coexiste avec Liquibase **et** un `schema.sql` → 3 mécanismes concurrents ; de plus `application.yml` contenait encore `password: keane` (fuite P0-2 non couverte).
- **Cause** : configuration schéma non unifiée + double fichier de config (properties + yml).
- **Approche** : `ddl-auto=validate` dans **les deux** fichiers (Liquibase, déjà activé via `master.xml`, devient seul propriétaire) ; externalisation du mot de passe du `.yml` (`${DB_PASSWORD:}`, complète P0-2).
- **Impacts** : au démarrage, Hibernate ne fait plus que **valider** ; si les changelogs `1.0.0/1.1.0` ne correspondent pas exactement aux entités, l'app échoue (drift à corriger). **À faire manuellement** : désactiver l'init `schema.sql` (`spring.sql.init.mode=never`/`initialization-mode`), vérifier l'inclusion de `1.1.0_schema.xml` dans `master.xml`, régénérer le baseline si besoin (`liquibase-maven-plugin` diff), **consolider properties+yml en un seul fichier**.
- **Statut** : implémenté, **non compilé/non démarré**.

### P1-1 — Nettoyer les dépendances backend (2026-07-25)
- **Problème** : `pom.xml` embarquait springfox 3.0.0 (`springfox-data-rest`, `springfox-swagger-ui`) — abandonné et incompatible Spring Boot 3 — en doublon fonctionnel avec springdoc-openapi ; et deux bibliothèques JWT coexistaient (`io.jsonwebtoken:jjwt` **et** `com.auth0:java-jwt`).
- **Cause** : dépendances héritées d'un scaffolding antérieur, jamais retirées après l'adoption de springdoc et de jjwt.
- **Approche** : vérification préalable par `grep` qu'aucune classe `src/main/java` n'importe `springfox.*`, `com.auth0.*` ni n'utilise `JWT.create`/`Algorithm.*` (0 occurrence) ; retrait des 2 dépendances springfox, de `com.auth0:java-jwt`, et de la propriété orpheline `java-jwt.version`. springdoc (Swagger UI) et jjwt (pipeline JWT maison durci en P0-3) conservés ; `io.swagger.core.v3:swagger-annotations` conservé (utilisé par 16 fichiers).
- **Impacts** : classpath allégé, suppression du risque de conflit springfox↔Boot 3 ; aucun changement d'API ni de comportement runtime attendu (dépendances mortes uniquement).
- **Statut** : implémenté, **non compilé** (pas de JDK). À confirmer via `./mvnw dependency:tree` + démarrage.

### P1-2 — Fetch LAZY par défaut + cascades sûres (2026-07-25)
- **Problème** : (a) chaîne d'associations EAGER `Depotoir → Commune → Department → Region` (et `CircuitCollect/CircuitBalayage → Commune`) → requêtes N+1 systématiques (R3) ; (b) `Depotoir.typeDepotoir` en `@ManyToOne(cascade = CascadeType.ALL)` vers un **référentiel partagé** `TypeDepotoir` → une suppression/persistance en cascade depuis un Depotoir pouvait corrompre des données de référence (R4).
- **Cause** : `fetch = EAGER` explicite sur les `@ManyToOne` de type « commune », défaut EAGER des `@ManyToOne` `department`/`region` non surchargé, et `CascadeType.ALL` accordé à une relation vers un référentiel.
- **Approche (chirurgicale)** : passer en `fetch = LAZY` les 5 `@ManyToOne` de la chaîne (`Depotoir.commune`, `Depotoir.typeDepotoir`, `CircuitCollect.commune`, `CircuitBalayage.commune`, `Commune.department`, `Department.region`) ; ramener la cascade de `Depotoir.typeDepotoir` de `ALL` à `{ REFRESH, MERGE }` (lecture + rattachement d'un type existant, jamais suppression/persistance du référentiel). Vérifié au préalable que les seuls conscommateurs directs ne lisent que l'**ID** de l'association (proxy LAZY → pas de hit DB).
- **Impacts / non traité** : les collections `@OneToMany(cascade = ALL)` de `Commune` (quartiers/circuits/depotoirs) sont laissées telles quelles (côté agrégat propriétaire, sémantique de composition — hors périmètre d'une itération surgicale) ; `CircuitEntity.geometry` (`OneToOne` EAGER utilisé dans `equals`) laissé inchangé. Les OneToOne `geometry` étaient déjà LAZY. **Risque à valider** : chemins de sérialisation/mapping qui accédaient à `commune`/`department`/`region` **après** fermeture de session → `LazyInitializationException` possible (ajouter fetch-joins ou projections DTO).
- **Statut** : implémenté, **non compilé** (pas de JDK).

### P1-3 — Sortir les images du BLOB (2026-07-25)
- **Problème** : deux stockages d'images en BLOB en base — `AlertEntity.displayPicture byte[]` (`@Lob`, jusqu'à 1 Mo) **dans la table `ALERT`**, et `ImageEntity.data byte[]` (`@Lob`) dans la table `IMAGE`. Tables lourdes, back-ups coûteux, mémoire (R3).
- **Cause** : images sérialisées en base au lieu d'être posées sur un stockage fichier/objet référencé par URL.
- **Approche (ADR-0005 §2/§3)** :
  1. `ImageEntity` : remplacer `@Lob byte[] data` par une **référence** — `path` (nom de fichier stocké), `url` (URL publique), `size` (octets) ; conserver `name`, `type`.
  2. `dto/Image` : idem (`path`/`url`/`size` à la place de `byte[] data`) — MapStruct mappe par nom, `ImageMapper` inchangé.
  3. `AlertEntity` : **supprimer** `displayPicture byte[]` (BLOB table `ALERT`) ; l'image d'une alerte passe uniquement par l'association `image` (OneToOne).
  4. Nouveau `ImageService.store(MultipartFile)` + `ImageServiceImpl` : envoie le fichier sur **MinIO** (stockage objet compatible S3) sous une clé aléatoire (UUID + extension) dans le bucket `sonaged.storage.minio.bucket` (créé au 1er upload), et renvoie l'`Image` (référence : `path`=clé, `url`=URL publique, `size`, `name`, `type`). Remplace l'ancien `AlertServiceImpl.getImageData` (qui posait les octets) — supprimé. Client MinIO fourni par `config/MinioConfig` (`io.minio:minio` 8.5.11).
  5. `AlertServiceImpl` : les 4 chemins d'écriture d'image (`createAlert(Alert,file)`, `createAlert(String,file)`, `createAlertFile`, `updateAlert`) appellent `imageService.store(...)` ; gardes `file != null && !file.isEmpty()`.
  6. **Diffusion** : les objets sont servis par MinIO (`url` = `{endpoint}/{bucket}/{clé}`, ou `MINIO_PUBLIC_URL` si proxy/CDN). L'ancien `WebConfig` (handler fichier `/data/images/**`) est **supprimé**.
  7. Config **dans `application.yml`** (choix « yml pour la config ») : bloc `sonaged.storage.minio.*` (endpoint, access/secret-key, bucket, public-base-url) + `sonaged.import.geojson.*` (P1-5) + `spring.servlet.multipart.enabled: true` ; retirés de `application.properties`. `docker-compose.yml` : service `minio` (ports 9000/9001, volume). `.env.example` : `MINIO_*`, `GEOJSON_*`.
  8. Migration schéma `1.2.0_image_out_of_blob.xml` (inclus dans `master.xml`) : crée/aligne la table `IMAGE` en mode référence, retire `IMAGE.data` et `ALERT.displaypicture` si présents (bases créées par `ddl-auto=update`), ajoute `ALERT.imageid` + FK. Changesets idempotents (préconditions `MARK_RAN`).
- **Impacts / non traité** :
  - **Migration des BLOB existants** (ADR-0005 §3) : aucun job fourni — les images déjà en base ne sont pas ré-uploadées sur MinIO (perte de référence si `data` est droppé sans extraction préalable). À faire avant d'appliquer `1.2.0-2`/`1.2.0-4` en prod.
  - **Accès aux objets** : `image.url` en accès direct suppose un bucket lisible (policy publique) ; sinon prévoir des URLs présignées ou un proxy applicatif (`MINIO_PUBLIC_URL`).
  - **Nommage physique** : `application.yml` déclare deux stratégies contradictoires ; le changelog suit la convention observée du baseline (minuscules sans underscore). Vérifier le nom réel de `displaypicture` avant application (préconditions tolérantes sinon).
  - **Clients** : `angular/`, `sonaged_web/`, `mobileFlutter/` doivent consommer `image.url` (plus de base64 `image.data`).
- **Statut** : implémenté, **non compilé** (pas de JDK).

### P1-5 — Import automatisé des GeoJSON (2026-07-25)
- **Problème** : les GeoJSON de référence (`datas/*.json` : département, communes, quartiers, circuits collecte/balayage, dépotoirs) ne pouvaient être chargés qu'à la main, fichier par fichier, via les endpoints d'upload `POST /data/*`. Données indispensables à la carte et aux futures alertes, jamais importées automatiquement.
- **Cause** : pas d'orchestration d'import ; la logique de parsing (`UploadFileServiceImpl`) n'était accessible que par MultipartFile via HTTP.
- **Approche (réutilisation, zéro duplication)** :
  1. `InMemoryMultipartFile` : adaptateur `MultipartFile` sur un `byte[]`, pour alimenter les méthodes `uploadDataXxx(MultipartFile)` existantes sans toucher au parsing.
  2. `GeoJsonImportService.importAll(force)` + impl : résout chaque fichier via `ResourceLoader` depuis `sonaged.import.geojson.location` (défaut `file:../datas`), et appelle les imports **dans l'ordre de dépendance** (département → commune → quartier → circuit collecte → circuit balayage → dépotoir). Idempotence : chaque étape est sautée si l'entité est déjà peuplée (`repository.count() > 0`), sauf `force=true`.
  3. `GeoJsonImportRunner` (`ApplicationRunner`) : rejoue l'import au démarrage si `sonaged.import.geojson.on-startup=true` (désactivé par défaut).
  4. `GeoJsonImportController` : `POST /v1/admin/import/geojson[?force=true]` — sous `/v1/**`, donc **authentifié** (les uploads `/data/**` restent publics — dette existante non aggravée).
  5. Config : `sonaged.import.geojson.location=${GEOJSON_DIR:file:../datas}`, `...on-startup=${GEOJSON_IMPORT_ON_STARTUP:false}`.
- **Impacts / non traité** :
  - **Chemin par défaut relatif** (`file:../datas`) : valide seulement si le backend est lancé depuis `ucgBackend/`. Pour un jar déployé, régler `GEOJSON_DIR` (ou pointer un `classpath:` après avoir empaqueté les fichiers).
  - **Pas d'upsert** : `force=true` réimporte sans dédoublonner → doublons. L'idempotence par `count()` est grossière (tout-ou-rien par entité), pas par enregistrement.
  - **Dépend du schéma** : ne peut aboutir que si l'app démarre (schéma `validate`, drift baseline à régler — cf. P0-4) et si `region.sql` a bien seedé la région id 1.
  - Fichiers non importés (`bac_rue`, `point_pp`, `pp_pnr`, `caisses polybenne`, `pre_collecte`, `depotoir2`, `ppef_cp`) : pas de parseur dédié aujourd'hui → hors périmètre.
- **Statut** : implémenté, **non compilé** (pas de JDK).

### Feature — Soft-delete (toutes entités) + purge 30 j + restauration (2026-07-26)
- **Demande** : la suppression doit être **logique** (soft-delete), avec **30 j** de rétention avant purge définitive, **restauration** possible avant le délai, **liste filtrée** par statut `pending_deletion` et endpoint **« get deletion »**. **Pour toutes les entités.** Backend **et** frontend. Réutiliser les **services de suppression existants** (ne pas créer de nouveau service).
- **Backend (infra transverse, toutes entités)** :
  1. `enums/DeletionStatus` (`ACTIVE` / `PENDING_DELETION`).
  2. `model/AbstractAuditingEntity` (base héritée par toutes les entités) : champs `deletionStatus` (défaut ACTIVE) + `deletionRequestedAt` + méthodes `markForDeletion()` / `restore()` / `isPendingDeletion()`.
  3. `repository/SoftDeleteRepository` (`@NoRepositoryBean`) : requêtes `findByDeletionStatus(...)`, `countByDeletionStatus`, `findByDeletionStatusAndDeletionRequestedAtBefore`. **Les 15 repositories** des entités auditées l'étendent (au lieu de `JpaRepository`) → couverts par la purge. (User/Avis/Image : entités hors base d'audit, non concernées.)
  4. `service/SoftDeleteService` : `softDelete` / `restore` (409 si délai dépassé ou pas en attente) / `purgeExpired` / `purgeDueAt`. Rétention `sonaged.deletion.retention-days` (30).
  5. `service/impl/DeletionPurgeScheduler` : `@Scheduled` (cron `sonaged.deletion.purge-cron`, 03:00) injectant **tous** les `SoftDeleteRepository` → purge globale. `@EnableScheduling` sur `SonagedApplication`.
  6. **Services de suppression existants adaptés** (pas de nouveau service) : les 12 `deleteXxx` (Alert, Authority, Circuit, CircuitBalayage, CircuitCollect, Commune, Coordinate, Department, Geometry, MoblierUrbain, Quartier, TypeDepotoir) font désormais `markForDeletion()` + `save()` au lieu du hard-delete.
  7. **Depotoir = résource de référence entièrement câblée** : `deleteDepotoir` (soft, via `SoftDeleteService`), `restoreDepotoir`, `readPendingDeletions()` (+ `purgeDueAt`), lectures filtrées `ACTIVE` (list/page/map). Endpoints : `DELETE /v1/depotoirs/{id}` (soft), `POST /v1/depotoirs/{id}/restore`, `GET /v1/depotoirs/deletions`. DTO `Depotoir` : `deletionStatus` + `deletionRequestedAt` + `purgeDueAt`.
  8. Migration `1.4.0_soft_delete_all.xml` (dans `master.xml`) : ajoute `deletionstatus` (défaut ACTIVE, NOT NULL) + `deletionrequestedat` aux **15 tables** (changesets idempotents `MARK_RAN`).
- **Frontend (Angular, service existant réutilisé)** :
  1. Correction **`environment(.development).ts`** : `apiUrl` `/api` → **`/v1`** (bug P1-4 : le front n'atteignait pas le backend).
  2. **`services/shared.service.ts`** (service générique **existant**) étendu : `getDeletions()` (`GET {url}/deletions`), `restore(id)` (`POST {url}/{id}/restore`) ; `delete()` passé en `responseType:'text'` (les endpoints de suppression renvoient du texte). **Aucun nouveau service créé** (le `DepotService` reste un stub, restauré).
  3. `entity/depotoir/list` : liste des dépotoirs actifs + **Corbeille** (toggle) listant les `pending_deletion` avec « Supprimé le » / « Purge prévue le » et bouton **Restaurer** ; suppression = soft-delete via `SharedService`. Routing corrigé (`''` → liste, `create` → création).
- **Rollout à toutes les ressources (2026-07-26)** :
  - **Lectures filtrées** : les 13 services CRUD (Alert, Authority, Circuit, CircuitBalayage, CircuitCollect, Commune, Coordinate, Department, Geometry, MoblierUrbain, Quartier, Region, TypeDepotoir) lisent désormais `findByDeletionStatus(ACTIVE)` au lieu de `findAll()` → les soft-deletés disparaissent des listes normales partout.
  - **Endpoints transverses** : nouveau `controller/DeletionController` (`GET /v1/deletions`, `GET /v1/deletions/{resource}` = en attente + `purgeDueAt`, `POST /v1/deletions/{resource}/{id}/restore`) résolvant la ressource par nom (clé = nom d'entité, dérivé du bean repository). Réutilise `SoftDeleteService` (helpers génériques `pendingDeletion` / `restoreById`, casts bruts contenus). Depotoir garde en plus ses endpoints dédiés (rétrocompat).
  - **Frontend** : `SharedService.getPendingDeletions(resource)` / `restoreResource(resource, id)` (endpoints génériques) réutilisables par n'importe quelle ressource ; la corbeille Depotoir reste sur ses endpoints dédiés (fonctionnelle).
- **Impacts / non traité** :
  - **Endpoint générique renvoie des entités** (enveloppées `{item, deletionRequestedAt, purgeDueAt}`), pas des DTO — acceptable pour une vue corbeille/admin ; à mapper si exposition publique fine requise.
  - Seule la corbeille **Depotoir** a une UI ; les autres ressources ont le backend prêt (endpoints génériques) mais pas d'écran dédié (frontends souvent stubs).
  - **Naming physique** (cf. P1-3/P0-4) : colonnes en minuscules `deletionstatus`/`deletionrequestedat` (convention baseline). `validate` reste en dérive baseline → démarrage à valider.
  - Frontend : composant en HTML simple (pas Angular Material comme `users`) ; à harmoniser si besoin.
- **Statut** : implémenté, **non compilé** (backend : pas de JDK ; frontend : non buildé).

### P1-6 — Réactiver le lien Depotoir ↔ Quartier (2026-07-26)
- **Problème** : la relation `Depotoir *─1 Quartier` était **commentée** dans `DepotoirEntity` (ancienne association `@ManyToOne QuartierEntity quartier`, EAGER) → granularité de supervision par quartier indisponible.
- **Cause** : association objet cross-contexte désactivée, jamais rétablie.
- **Approche (ADR-0012, référence par identifiant)** :
  1. `DepotoirEntity` : remplacer le bloc commenté par `@Column(name = "quartierId") Long quartierId;` — **référence par ID**, pas d'association objet, pas de cascade (Depotoir = contexte « Point de collecte » ; Quartier = « Référentiel territorial »).
  2. `dto/Depotoir` : ajouter `Long quartierId` (exposé en création/lecture) — MapStruct mappe `quartierId` dto↔entity par nom, aucun mapper à modifier.
  3. `DepotoirServiceImpl.createDepotoir` : suppression du bloc mort commenté (qui résolvait un `QuartierEntity` via `findById` sur une variable inexistante) ; le `quartierId` transite désormais tel quel par le DTO.
- **Impacts / non traité** :
  - **Aucune migration** : `depotoir.quartierid` + FK `fkgf8eko8nxre5h8f690ohu6lnv` existent déjà dans le baseline `1.0.0` → la réactivation **réduit** la dérive entité↔schéma.
  - **FK physique cross-contexte** conservée : ADR-0012 prône « pas de FK cross-contexte », mais son retrait systématique (quartier, commune, circuits) — avec compensation par validation applicative — relève de **P1-7**. La retirer isolément ici ôterait l'intégrité sans filet.
  - **Validation d'existence** du `quartierId` (le quartier référencé existe-t-il ?) non implémentée → P1-7.
  - `depotoir.json` ne portant pas de quartier, l'import P1-5 ne peuple pas ce champ ; `DepotoirMaps` n'expose pas encore le quartier (carto par quartier = amélioration ultérieure).
- **Statut** : implémenté, **non compilé** (pas de JDK).

### Montée de version des dépendances + **premier build vert** (2026-07-26)
- **Problème** : aucun des 10 lots précédents n'avait jamais été compilé (pas de JDK) — le backend était un tas de code non vérifié. Par ailleurs `pom.xml` était figé sur Spring Boot 3.2.4.
- **Approche** :
  1. **JDK** : installation de Temurin 21 (`~/.local/jdk/jdk-21.0.12+8`, sans droits root, archive tar.gz). Débloque toute vérification.
  2. **Spring Boot 3.2.4 → 3.5.3** ; Spring Modulith 1.1.5 → 1.4.1 ; springdoc 2.2.0 → 2.8.17 ; MapStruct 1.5.5 → 1.6.3 ; Liquibase (+ ext hibernate6) 4.28.0 → 4.32.0 ; jjwt 0.12.3 → 0.12.6 ; MinIO 8.5.11 → 8.5.17 ; org.json 20230227 → 20260719.
  3. **Versions rendues au BOM** : `hibernate-core` (résout désormais 6.6.18.Final) et `postgresql` (42.7.7). ⚠️ **Correction d'analyse** : la propriété `hibernate-core.version=7.0.0.Alpha2` était **orpheline** — Boot pilote Hibernate via `hibernate.version` — elle ne s'appliquait donc jamais. C'était de la configuration morte et trompeuse, pas une alpha réellement embarquée.
  4. **Doublon de classpath supprimé** : `io.swagger.core.v3:swagger-annotations` retiré (springdoc fournit déjà `swagger-annotations-jakarta` 2.2.47, même package `io.swagger.v3.oas.annotations`).
  5. **Annotation processors** : `mapstruct-processor` déplacé de dépendance de compilation vers `annotationProcessorPaths` du maven-compiler-plugin, avec `lombok` + `lombok-mapstruct-binding` — garantit que Lombok génère getters/setters **avant** que MapStruct ne lise le modèle. 16 mappers générés correctement.
  6. **Nettoyage** : `apt-maven-plugin` (QueryDSL, abandonné, sans exécution, zéro usage) retiré ; dépôts `spring-milestones`/`spring-snapshots` retirés (plus aucune dépendance non-GA) ; propriétés orphelines (`slf4j.version`, `maven-failsafe-plugin.version`, `swagger.version`) supprimées ; `hibernate-validator` explicite remplacé par `spring-boot-starter-validation`.
  7. **Tests réactivés** : `skipTests=true` était **codé en dur** dans la configuration surefire — retiré.
- **Bugs réels révélés par la première compilation** :
  - `service/SoftDeleteService.java:93` — **erreur de compilation** (lot soft-delete). `(SoftDeleteRepository) repository` en **type brut** effaçait aussi les génériques des méthodes héritées : `Optional.orElseThrow(Supplier)` retombait sur sa signature effacée `throws Throwable`. Corrigé par un cast *paramétré* (`SoftDeleteRepository<AbstractAuditingEntity<?>, Object>`) confiné dans une méthode `erase(...)` ; les `@SuppressWarnings("rawtypes")` disparaissent.
  - `test/.../SonagedApplicationTests` — situé dans `sonaged.collecte.test`, la recherche ascendante de `@SpringBootConfiguration` ne pouvait **jamais** atteindre `SonagedApplication` (package frère `sonaged.collecte.master`). Déplacé dans le package de la classe d'application.
  - `security/SecurityConfiguration.java:82` — `new DaoAuthenticationProvider()` + `setUserDetailsService(...)` dépréciés en Spring Security 6.5 ; passés au constructeur. **Zéro avertissement de dépréciation** désormais.
- **Résultat** : `./mvnw clean package` → **BUILD SUCCESS**, jar exécutable `target/Sonaged-0.0.1-SNAPSHOT.jar` (91 Mo). **Tests : 9 exécutés, 8 réussis, 1 ignoré**, 0 échec.
- **Non couvert (décision utilisateur : « compilation + tests seulement »)** :
  - `SonagedApplicationTests` est **`@Disabled`** : le chargement du contexte exige une vraie base PostgreSQL (et `ddl-auto=validate` face aux changelogs). Ni base ni Docker ici → Testcontainers impossible. **Le démarrage réel de l'application reste donc non vérifié**, y compris la dérive baseline Liquibase signalée en P0-4/P1-3. Retirer `@Disabled` sur un poste équipé.
  - Les 8 tests qui passent couvrent `AuthorityServiceImpl` (5) et les frontières Modulith (3) — **la logique métier (soft-delete, MinIO, import GeoJSON, alertes) n'a aucun test**. Compiler n'est pas valider : cf. P2-2.

### P1-7a — Découplage des entités cross-contexte par identifiant (2026-07-26)
- **ADR-0012 appliqué** : `DepotoirEntity.commune`, `CircuitCollectEntity.commune`, `CircuitBalayageEntity.commune` (`@ManyToOne CommuneEntity`) → `Long communeId`. `AlertEntity` reçoit `depotoirId`. Les DTO `Depotoir`/`CircuitCollect`/`CircuitBalayage` n'exposent plus l'**entité JPA** `CommuneEntity` (fuite du modèle) mais l'identifiant.
- **Relations conservées** (intra-contexte, conformes à ADR-0012 §1) : hiérarchie `Quartier→Commune→Department→Region`, compositions `*→Geometry`, `Alert→Image`, `Depotoir→TypeDepotoir`.
- **Intégrité** : nouveau `service/CrossContextReferenceValidator` — l'existence de `communeId`/`quartierId` est vérifiée en applicatif, puisque les FK SQL disparaissent. Limite assumée et documentée : la vérification n'est pas liée transactionnellement à l'écriture (cohérence éventuelle).
- **Migration** `1.5.0_context_boundaries.xml` : retire les **4** FK cross-contexte (`depotoir→commune`, `depotoir→quartier`, `circuitbalayage→commune`, `circuitcollect→commune`), ajoute des index de substitution + `alert.depotoirid`. Préconditions `MARK_RAN`.
- **Statut** : ✅ compilé, build vert.

### P2-1 — Notifications temps réel SSE, bout en bout (2026-07-26)
- **Backend** (ADR-0007) : `event/AlertRaisedEvent` (événement de domaine), `service/notification/AlertBroadcaster` (registre d'`SseEmitter`, diffusion en `@TransactionalEventListener(AFTER_COMMIT)` — jamais d'alerte issue d'une transaction annulée —, heartbeat `@Scheduled` contre les coupures de proxy), `controller/AlertStreamController` → `GET /v1/alerts/stream`. `AlertServiceImpl` publie l'événement sur ses 3 chemins de création.
- **Pourquoi un événement** : le jour où le moteur de seuils IoT (P0-6, réservé) créera des alertes, il publiera le même événement — la diffusion temps réel fonctionnera sans modification.
- **Frontend** : `services/alert-stream.service.ts`. Implémenté en `fetch` + `ReadableStream` et **non** avec `EventSource` : celui-ci ne peut pas porter d'en-tête `Authorization`, et les contournements (JWT en paramètre d'URL, ou ouverture de l'endpoint) auraient été des régressions de sécurité. Reconnexion avec back-off réimplémentée ; `NgZone.run` pour rafraîchir la vue. Intégré à l'écran Alertes (bandeau + insertion en tête de liste, dédoublonnage par `alertId`).
- **Portée mono-instance** assumée (registre en mémoire) — cf. ADR-0007 (bus partagé Redis à prévoir avec ADR-0008).
- **Statut** : ✅ backend et frontend compilés, builds verts.

### Frontend — Couche CRUD générique et parité avec l'API (2026-07-26)
- **Problème** : sur 12 écrans d'entité, **seuls `users` et `depotoir` disposaient d'un formulaire**. Les dix autres étaient des listes en lecture seule : aucune création, modification, suppression, validation, recherche, tri ni pagination. `authorities`, `coordinates` et `geometries` (CRUD complet côté backend) n'avaient aucun écran.
- **Approche — configuration plutôt que duplication** : plutôt que d'écrire 12 × 3 écrans, `shares/crud/` fournit `ENTITY_CONFIGS` (registre déclaratif : chemins, identifiant, colonnes, champs, capacités) + `EntityListComponent` + `EntityFormComponent` + `EntityCrudService`, et `crudRoutes(key)` génère le triplet liste/création/édition.
- **Irrégularités du backend encodées dans le registre** (plutôt que contournées ailleurs) : chemins de liste à double « s » (`/communess`, `/quartierss`…) distincts des chemins paginés ; `Region` sans PUT/DELETE ; chemin de suppression non standard de `Coordinate` ; suppressions renvoyant du **texte** et non du JSON.
- **Bug d'intégration corrigé pendant l'implémentation** : la corbeille appelait `/v1/deletions/{clé de route}` (`communes`, `circuit-collects`) alors que le backend dérive la clé du **nom du bean repository** (`communeRepository` → `commune`). Sans le champ `deletionResource` ajouté au registre, toute la corbeille répondait 404.
- **`EntityCrudService` volontairement distinct de `SharedService`** : ce dernier porte l'URL courante dans un champ **mutable** (`sharedService.url = …` avant chaque appel), ce qui devient incorrect dès que deux requêtes concurrentes visent des ressources différentes. `SharedService` est laissé inchangé pour les écrans existants.
- **Écrans hérités conservés** : les `list-*.component` désormais non routés restent sur disque (la **suppression exige une validation explicite**, règle projet).
- **Statut** : ✅ `ng build` vert.

### Environnement — Node installé, build frontend débloqué (2026-07-26)
- **Node 24.18.0 LTS** installé dans `~/.local/node/` (aucun droit root), comme le JDK : `export PATH=~/.local/node/node-v24.18.0-linux-x64/bin:$PATH`.
- **esbuild** : `node_modules` avait été installé sous Windows (`@esbuild/win32-x64`) ; sous WSL, esbuild exige `@esbuild/linux-x64` → **le build Angular était totalement bloqué**. Les deux binaires sont désormais présents côte à côte, afin que `ng serve` continue de fonctionner **sous Windows comme sous WSL**.
- ⚠️ Un `npm install` complet relancé depuis WSL retirera à nouveau le binaire Windows (et inversement). En cas de blocage, réinstaller le binaire de la plateforme manquante plutôt que tout le `node_modules`.

### P2-5 — Indicateurs avancés de supervision (2026-07-26)
- **Backend** : `dto/SupervisionStats`, `service/SupervisionStatsService`, `controller/SupervisionStatsController` → `GET /v1/supervision/stats?windowDays=30` (**authentifié**, contrairement aux compteurs historiques servis sous `/data/**` qui est public).
- **Indicateurs** : alertes/jour (série pré-remplie à zéro pour ne pas masquer les creux dans un graphique), alertes par code, points de collecte par type, circuits par commune (regroupés sur `communeId` depuis ADR-0012), corbeille par ressource, nombre de flux SSE ouverts.
- **Taux de remplissage volontairement absent** : il suppose `Depotoir.fillLevel`, alimenté par l'ingestion IoT (P0-5/P0-6, module réservé). L'exposer aurait produit un indicateur faux.
- **Agrégation en mémoire** assumée et documentée à ce volume ; à basculer en `@Query` d'agrégation quand l'ingestion IoT arrivera.
- **Frontend** : section « Activité des alertes » du tableau de bord (KPI, histogramme CSS, répartitions, sélecteur de fenêtre 7/30/90 j), chargée indépendamment des compteurs `/data` pour qu'une indisponibilité ne masque pas le reste.
- **Statut** : ✅ builds backend et frontend verts.

### P2-4 — Consolidation de la configuration + documentation (2026-07-26)
- **Fusion `application.properties` + `application.yml` → un seul `application.yml`** ; `application.properties` supprimé. Les deux fichiers divergeaient (courrier, stratégie de nommage) et **`.properties` l'emportait** : la configuration réellement appliquée n'était pas celle qu'on lisait dans le `.yml`. Les valeurs gagnantes ont été conservées pour ne pas changer le comportement.
- 🔴 **`schema.sql` — bombe à retardement désamorcée.** Le fichier commence par `DROP DATABASE IF EXISTS sonaged;` et le `.yml` le référençait via `spring.datasource.schema` + `initialization-mode: always`. Ces deux propriétés sont du **Spring Boot 1.x**, inexistantes en Boot 3 : elles étaient **ignorées en silence**, et c'est la SEULE raison pour laquelle la base a survécu. Toute « modernisation » vers `spring.sql.init.*` aurait détruit la base. Désormais : `spring.sql.init.mode: never` explicite + bandeau d'avertissement en tête du fichier.
- 🔴 **Stratégies de nommage contradictoires résolues** — cause la plus probable de la « dérive `validate` » traînée depuis P0-4. Le `.yml` déclarait à la fois `PhysicalNamingStrategyStandardImpl` et `SpringPhysicalNamingStrategy`. Le baseline Liquibase nomme les colonnes `depotoirid`/`quartierid`/`createddate`, c'est-à-dire le `@Column(name="DepotoirId")` repris **verbatim** puis replié en minuscules par PostgreSQL — ce que produit `PhysicalNamingStrategyStandardImpl`. La stratégie « Spring » aurait exigé `depotoir_id` et fait échouer `validate` au démarrage. Une seule stratégie est désormais déclarée. **À confirmer au premier démarrage réel avec une base.**
- **Nettoyage de clés inertes** : `spring.activemq.*` et `spring.batch.initialize-schema` (ni ActiveMQ ni Spring Batch ne sont des dépendances du projet) retirés. `management.endpoints.web.exposure.include` passe de `"*"` à `health,info` (paramétrable par `ACTUATOR_EXPOSE`) — Actuator n'étant pas non plus une dépendance, la clé reste inerte aujourd'hui.
- **Documentation** : `ucgBackend/endpoint.md` **régénéré depuis les contrôleurs** (l'ancien était du copier-coller : toutes les sections listaient `/v1/users`), avec les conventions d'authentification et le piège des chemins à double « s ». **`README.md` racine** (auparavant une seule ligne) : sous-projets, démarrage des trois stacks, architecture, renvois ADR, et points sensibles.
- **Statut** : ✅ `./mvnw clean verify` vert.

### P1-7b — Monolithe modulaire : amorçage de la migration (2026-07-26) — **EN COURS**
- **Constat de départ** : `sonaged.ucg.*` n'était qu'un **échafaudage vide** (classes marqueurs + `package-info`). `UcgModularityTests` passait donc **à vide** : `modules.verify()` ne vérifiait rien, puisque aucun module ne contenait de code (« Spring beans: none » partout).
- **Pourquoi une migration incrémentale et non un déplacement global** : `ApplicationModules.of(UcgModulith.class)` n'analyse que les packages sous `sonaged.ucg`. Le code resté dans `sonaged.collecte.master` est **hors périmètre d'analyse** : un module migré peut donc encore référencer le code hérité sans violer les frontières. En revanche, déplacer les 196 fichiers d'un coup transformerait les **56 dépendances inter-modules** en violations simultanées (avec un découpage par couches, tout ce qui est sous la racine d'un module est « interne »), imposant de concevoir d'emblée les `@NamedInterface` de chaque module. ADR-0010 prescrit d'ailleurs explicitement un « refactoring progressif contexte par contexte ».
- **Prérequis indispensable — périmètre de scan** : `SonagedApplication` passe en `scanBasePackages`/`@EntityScan`/`@EnableJpaRepositories` sur **`sonaged`** (ancêtre commun des deux arborescences). Sans cela, toute classe déplacée cesserait **silencieusement** d'être un bean : l'application compilerait, puis échouerait à l'exécution. C'est le principal risque de cette tâche, et il n'est **pas couvert par les tests actuels**.
- **Cycle 1 rompu** — `dto/Commune` portait `List<DepotoirEntity> depotoirs` : (a) fuite d'entité JPA dans un DTO, (b) cycle « Référentiel territorial » → « Points de collecte ». Champ retiré (les dépotoirs d'une commune se lisent via l'API du contexte propriétaire). `DepartmentEntity department` → `departmentId` (même contexte, mais l'entité n'a pas à être exposée) ; correspondance MapStruct explicite, rattachement déplacé sur l'entité dans `CommuneServiceImpl`. **Bug corrigé au passage** : l'ancien code déréférençait `getDepartment()` sans garde → `NullPointerException` à la création d'une commune sans département.
- **1er module migré — `supervision`** (choisi parce que **rien ne dépend de lui** : migration sans effet de bord sur les autres). 7 fichiers : `DashboardController/Service/ServiceImpl`, `DepartmentState`, `SupervisionStats(+Controller/Service)`.
- **2e module migré — `communication`** : `AvisControleur`, `Avis`, `AvisRepository`, `AvisService`, `NotificationService(+Impl)`. Le lien `Avis → UserEntity` reste une association objet cross-contexte : ce n'est plus un cycle (l'événement l'a rompu), donc une simple dépendance orientée `communication → identiteacces`, légale pour Modulith. À convertir en `userId` (ADR-0012) lors de la migration d'`identiteacces`.
- **Structure interne des modules — décision utilisateur** : chaque module **reproduit l'architecture en couches du projet** (`controller/`, `service/`, `service/impl/`, `repository/`, `model/`, `dto/`) plutôt qu'un package plat par domaine. *(Rectification : une première version de ce journal annonçait l'inverse — « par domaine et non par couche ». C'est bien le découpage en couches qui est retenu, pour rester homogène avec `sonaged.collecte.master`.)*
  **Conséquence Modulith à connaître** : tout sous-package d'un module est considéré comme **interne**. Les types de `…supervision.dto` ou `…communication.service` ne sont donc pas accessibles depuis un autre module sans `@NamedInterface` explicite — encapsulation renforcée, mais chaque exposition devra être déclarée volontairement.
- **Résultat** : `UcgModularityTests` **n'est plus vide** — `supervision` et `communication` déclarent de vrais beans et `verify()` passe. Build complet vert.

#### Rupture du cycle Identité ↔ Communication (fait)
- **Cycle réel** : `ValidationServiceImpl` (identité) appelait `NotificationService` (communication), et `NotificationServiceImpl` remontait vers `Validation`/`UserEntity` (identité).
- **Solution (ADR-0010 §2)** : nouvel événement `ActivationCodeIssued(recipientEmail, recipientLastname, code)` — charge utile **autonome**, aucune entité transportée. L'identité publie, la communication écoute. Cette dernière n'importe plus aucun type d'identité.
- **Sémantique préservée** : écouteur `@EventListener` **synchrone** (et non `@TransactionalEventListener`), car `registerUserCode` n'est pas transactionnel et l'envoi était jusqu'ici immédiat, une erreur remontant à l'appelant. Le découplage est structurel, pas comportemental.
- **Corrections au passage** : `java.util.Random` → `SecureRandom` (un code d'activation est un secret ; un générateur non cryptographique est prédictible et permettrait d'activer le compte d'un tiers) ; borne `nextInt(999999)` → `nextInt(1_000_000)`, l'ancienne excluant la valeur 999999 ; le code n'est jamais journalisé.

#### ⚠️ Découverte : la suite de tests était **vide**
Les 5 tests d'`AuthorityServiceImplTest` sont des **méthodes au corps vide** — ils ne vérifient rien. Combinés aux tests Modulith qui passaient à vide, le projet affichait « Tests run: 8, Failures: 0 » sans aucune assertion réelle. **Le vert du build ne signifiait rien.**
Quatre tests réels ont été ajoutés pour le code modifié ici (`ValidationServiceImplTest`, `NotificationServiceImplTest`) et **vérifiés par mutation** : en publiant volontairement un code différent de celui persisté, le test échoue bien (`expected: "959774"`). Ils ne sont donc pas vides à leur tour. `AuthorityServiceImplTest` reste à écrire.

#### Suite du chantier (ordre recommandé, du moins couplé au plus couplé)
Nombre de dépendances **entrantes** mesurées avant migration : `supervision` 0 (fait) · `identiteacces` 3 · `communication` 4 · `collecte` 5 · `referentiel` 19.
1. ~~**Rompre le cycle `identiteacces` ↔ `communication`**~~ — **FAIT** (événement `ActivationCodeIssued`). *Rappel de l'analyse initiale :* — couplage bidirectionnel réel : `ValidationServiceImpl` (identité) appelle `NotificationService` (communication), et `NotificationServiceImpl` importe `UserEntity`/`Validation` (identité). Solution conforme à ADR-0010 §2 : l'identité **publie un événement** (`ActivationCodeIssued`) portant les données nécessaires ; la communication l'écoute et cesse d'importer les entités d'identité. ⚠️ Modifie le comportement d'envoi des e-mails — **à faire avec des tests**.
2. ~~Migrer `communication`~~ — **FAIT**. Reste `identiteacces` : à cette occasion, convertir `Avis.user` en `userId` (ADR-0012) et exposer les types nécessaires via `@NamedInterface`, les sous-packages étant internes.
3. **Reclasser `Image`** dans `collecte` (sous-domaine « alertes ») et non `communication` : ADR-0012 §1 range explicitement `Alert → Image` dans un même contexte. Cela supprime l'edge `collecte → communication`.
4. **Statuer sur `UploadFileServiceImpl` / import GeoJSON**, qui traverse tous les domaines par nature (département, commune, quartier, circuits, dépotoirs). Ce n'est pas du « shared » : le placer dans un module dont **personne ne dépend** (administration/import) préserve l'acyclicité.
5. Migrer `collecte`, puis `referentiel` (le plus dépendu, donc en dernier).
6. **Read-models de carte** (`DepotoirMaps`, `DepartmentMaps`) : produits par `collecte`/`referentiel`, consommés par `supervision` — ils traversent la frontière et demandent une décision d'API (`@NamedInterface` exposée par le contexte producteur, ou contrat publié par `supervision`). Laissés en place pour cette raison.
7. Une fois un module dépendu d'un autre **migré**, exposer son API via `@NamedInterface` et masquer ses internes.

### ADR-0013 — Bascule vers `sn.smartwaste.collect` : `analytics`, `platform`, `territory` (2026-07-27)
- **Changement de cible d'architecture.** Le cadrage produit passe à un **SaaS multi-collectivités** : l'ADR-0010 (5 modules sous `sonaged.ucg`, découpage par couches) est **remplacé par l'ADR-0013** — nouvelle racine `sn.smartwaste.collect`, **8 contextes bornés**, Clean Architecture interne (`domain` / `application` / `infrastructure` / `presentation`). Trois manques rendaient l'ADR-0010 bloquant : aucun contexte `tenant`, un module `collecte` fourre-tout, et un découpage par couches qui ne dit rien du métier.
- **Migrés** : `analytics` (ex-`supervision`), `platform` (ex-`communication`), `territory` (42 fichiers). `SonagedApplication` scanne les deux racines pendant la transition ; `sonaged.ucg` n'est plus qu'un échafaudage vide et **obsolète** (son retrait exige une validation explicite — règle projet).
- **UUID v7 (contexte territory)** — `UuidV7Generator` sans dépendance externe, branché par `@UuidGenerator`. *Pourquoi pas v4* : un identifiant entièrement aléatoire tombe n'importe où dans l'index B-tree de PostgreSQL → fragmentation et écritures amplifiées ; le v7 préfixe un horodatage et conserve la localité d'insertion d'un `BIGSERIAL`. 4 tests réels (version, variante, horodatage encodé, ordonnancement, 50 000 générations sans collision).
- **Changelog `2.0.0_territory_uuid.xml`** : PK et colonnes de référence `BIGINT → uuid` natif. **DESTRUCTEUR, assumé** (base jetable) — PostgreSQL ne convertit pas `BIGINT` en `uuid`, il faut supprimer/recréer les colonnes.
- **Deux bugs introduits puis corrigés pendant la conversion** : un remplacement global `Long → UUID` avait produit `SUM(CAST(c.total AS UUID))` dans un agrégat JPQL (compilait, aurait échoué au démarrage d'Hibernate) ; `UuidV7Generator` avait un constructeur privé alors qu'Hibernate l'instancie par réflexion. `UploadFileServiceImpl` faisait `findById(1L)` sur la région/le département semés — identifiant qui n'existe plus, remplacé par « le premier enregistré ».
- **Statut** : ✅ `mvnw clean verify` vert, 20 tests (19 passants, 1 ignoré).

### ADR-0013 — Shared kernel + contexte `identity` migrés (2026-07-27)

**1. Shared kernel** — `AbstractAuditingEntity`, `DeletionStatus`, `SoftDeleteRepository`, les trois exceptions métier et `ErrorMessage` quittent `sonaged.collecte.master` pour `sn.smartwaste.collect.shared` (`domain/model`, `domain/repository`, `domain/exception`, `presentation`). C'est le préalable au reste : ces types étaient les 6 imports hérités les plus fréquents depuis la nouvelle racine, et tant qu'ils vivaient dans le legacy, chaque contexte migré traînait une dépendance vers lui.

**2. Contexte `identity`** — 27 fichiers (entités, repositories, DTO, mapper, services, sécurité JWT, contrôleurs) réorganisés en Clean Architecture. `ActivationCodeIssued` rejoint `shared/domain/event` : l'événement est un contrat entre deux contextes, le loger chez l'émetteur obligerait le récepteur à dépendre de lui.

**3. Frontière `platform → identity` fermée par un port, pas par un repository.**
- `AvisService` lisait le `SecurityContextHolder` puis **castait le principal en `UserEntity`** : le contexte plateforme connaissait l'entité JPA de l'identité. Remplacé par `identity.application.api.CurrentUserProvider` (`@NamedInterface("api")`), qui ne renvoie qu'un `UUID`. Le cast vit désormais dans un seul endroit, `IdentityApiAdapter`, et l'externalisation vers Keycloak (ADR-0011) se fera en réimplémentant ce port.
- `Avis.user` (`@ManyToOne UserEntity`) → `Avis.userId` (`UUID`, sans FK SQL), conformément à l'ADR-0012.
- **Choix délibéré de ne pas reproduire la concession `territory`** : le référentiel territorial publie encore ses six repositories via `@NamedInterface("repositories")`. Publier de même `UserRepository` aurait exposé tout l'agrégat utilisateur — l'ADR-0013 §3 l'interdit explicitement. `DashboardServiceImpl` **injectait `UserRepository` sans jamais s'en servir** : l'injection morte a simplement été retirée, ce qui a supprimé la seule dépendance `analytics → identity`. Aucun port n'a donc eu à être créé pour elle.
- `modules.verify()` confirme la frontière : le seul type d'`identity` visible de l'extérieur est `CurrentUserProvider`.

**4. Identifiants `identity` en UUID v7** — `UserEntity.userId`, `AuthorityEntity.authorityId` (`Long`) et `Validation.id` (`int`) passent en `UUID`, propagés aux repositories, DTO, services et contrôleurs (`@PathVariable UUID`).

**5. Changelog `2.1.0_identity_uuid.xml`** — même schéma destructeur qu'en 2.0.0 (base jetable) : purge de `users` / `authority` / `authoritypermission` / `validation`, PK et colonnes de référence en `uuid`, FK **internes au contexte** rétablies (elles ne traversent aucune frontière, donc légitimes). Les rôles de référence sont **resemés** : `config/liquibase/data/authority.sql` insérait les identifiants `1, 2, 3` en dur et ne peut plus s'appliquer ; les trois UUID de remplacement sont fixes, de forme v7 valide, et volontairement reconnaissables — un rôle est une donnée de référence, son identifiant doit être stable d'un environnement à l'autre.

**6. 🔴 Défaut latent de démarrage corrigé au passage.** La table `avis` n'a **jamais eu de colonne** pour l'association `Avis.user` : le baseline ne déclare qu'`utilisateur_id`, héritée d'une table `utilisateur` abandonnée. Avec `ddl-auto: validate` (P0-4), Hibernate attendait `user_userid` et **le démarrage aurait échoué**. Invisible jusqu'ici parce que l'application n'a jamais été lancée contre une base. Le changelog crée `avis.userid uuid` (+ index, sans FK).

**7. `AuthorityServiceImplTest` : 5 méthodes vides → 7 tests réels.** La dette signalée en P1-7b est soldée pour cette classe. Les tests couvrent notamment les deux comportements contre-intuitifs du service : `readAllAuthority` filtre sur `DeletionStatus.ACTIVE` (et non `findAll`), et `deleteAuthority` est une **suppression logique** — un test vérifie explicitement que `delete()` n'est jamais appelé.

**8. Frontend** — `angular/src/app/models/user.model.ts` : `userId: number` → `string`. Aucune autre occurrence de `userId` dans l'application (l'identifiant ne sert qu'à composer les URL), donc pas d'impact fonctionnel — mais le type déclaré était devenu faux.

- **Statut** : ✅ `./mvnw clean verify` vert — **22 tests, 21 passants, 1 ignoré**. `modules.verify()` passe sur les 4 contextes peuplés. *(Le build Angular échoue sur 18 dépassements de budget SCSS **préexistants**, vérifié en rejouant le build sans la modification.)*
- **Restes du legacy** : 90 fichiers dans `sonaged.collecte.master` — contexte `waste` (dépotoirs, circuits, alertes, mobilier, historique, images) + `UploadFileServiceImpl` / import GeoJSON.

### Alerte citoyenne « sortez vos ordures » (2026-07-29)

**La demande n°1 de la seule étude utilisateur du projet**, absente du backlog jusqu'ici.
L'enquête (34 réponses) donne ~29/34 favorables à « un système d'alerte pour sortir vos ordures »,
et les deux problèmes les plus cités — « les voitures ne passent pas souvent » et « on oublie de
sortir les ordures » — se répondent par la même chose : savoir **quand** le camion passe. La chaîne
d'alerte construite jusqu'ici va du bac vers le superviseur ; celle-ci va vers l'habitant.

- **`CollectionSchedule`** (waste) : jour et heure de passage d'un circuit dans un quartier.
  `CircuitCollect` portait déjà `frequence` et `rotation`, mais **en texte libre**, donc
  inexploitable par une machine — impossible d'en déduire « le camion passe demain à 7 h ici ».
  C'était le chaînon manquant entre les données de collecte et l'habitant.
- **`CollectionSubscription`** (platform) : l'habitant s'abonne à son **quartier**, maille qu'il
  connaît, et non au circuit qui le dessert. L'abonné est toujours l'utilisateur du jeton — sinon
  n'importe qui pourrait abonner ou désabonner un tiers.
- **`CollectionReminderScheduler`** : envoie le rappel avec un délai d'avance (90 min par défaut).
  Prévenir à l'instant du passage serait inutile ; prévenir six heures avant aussi.
- **`Clock` injectable** (`ClockConfig`) : sans lui, la logique horaire ne serait testable qu'en
  attendant le bon moment de la journée.

#### Les deux gardes qui font la différence
Les répondants reprochent précisément au klaxon des camions d'être intrusif. Un rappel envoyé en
double, ou après le passage, reproduirait le défaut qu'on cherche à corriger.
- **Anti-répétition** : la tâche tourne toutes les 15 min, la fenêtre en fait 90 — sans garde, le
  même passage serait notifié six fois. Les couples (quartier, heure) déjà traités sont mémorisés,
  purgés au changement de jour.
- **Fenêtre stricte** : un passage déjà effectué ne déclenche rien. Prévenir après coup est pire
  que ne rien envoyer — l'habitant a raté le camion.

5 tests, **deux mutations vérifiées** : retirer l'anti-répétition → 1 échec ; accepter les passages
déjà effectués → 1 échec.

#### 🔴 Correctif de sécurité sur ce même chantier
La revue de sécurité automatique du commit a signalé — à raison — que `subscribe()` acceptait
**l'adresse e-mail depuis le client**. Un compte authentifié pouvait donc inscrire l'adresse de
n'importe qui à des rappels récurrents, expédiés au nom du service : un vecteur d'envoi non
sollicité, sans le moindre consentement du destinataire.

- `email` disparaît de la requête, de l'interface de service **et de l'entité**.
- L'adresse est résolue **à l'envoi** via un nouveau port `identity.application.api.UserDirectory`.
  C'est le seul contexte qui la détient **prouvée** : l'inscription y envoie un code d'activation
  et le compte ne s'ouvre qu'une fois le code saisi. Ma justification initiale — « figer l'adresse
  pour ne pas dépendre du contexte identité » — était le raisonnement qui avait créé le trou.
- Bénéfice secondaire : une adresse modifiée est désormais suivie, là où la version figée aurait
  continué d'écrire à l'ancienne.
- Un abonnement dont le titulaire a disparu est ignoré sans interrompre les autres.

4 tests supplémentaires, dont un qui vérifie par réflexion que l'entité **n'a plus aucun champ
e-mail** — c'est la forme la plus directe d'empêcher la régression.

Changelog 2.5.0 corrigé en place (aucun environnement ne l'avait appliqué).
`verify` EXIT=0, **69 tests**.

⚠️ Les horaires doivent être saisis : aucune donnée source ne les porte (les GeoJSON de circuits
n'ont qu'une `frequence` textuelle). Un écran d'administration ou un import dédié reste à faire
pour que la fonctionnalité serve en production.

### Import GeoJSON recâblé — la dette de la migration est soldée (2026-07-29)

L'import écrivait **directement dans 12 repositories** de `territory` et `waste`, ce qui avait
obligé à ouvrir trois `@NamedInterface` transitoires. **Elles sont refermées.**

- **`ImportedFeature`** (shared) : attributs bruts + contour, en types neutres (`Map`, `String`).
  Aucune dépendance JPA, JSON ou Spring ne franchit plus la frontière.
- **`TerritoryImportPort`** et **`WasteImportPort`** : chaque contexte reçoit des attributs bruts et
  décide seul de leur signification. Le savoir déplacé est celui qui n'appartenait pas à l'import —
  que `COD_DEPT` est un code de département, que `Type_de_Mo` est un type de point de collecte,
  qu'un type inconnu se crée à la volée.
- **`UploadFileServiceImpl` : 646 → 195 lignes**, et ne connaît plus aucune entité ni aucun
  repository. Il ne fait plus que ce qui est réellement son métier : lire du GeoJSON ArcGIS
  (`features` / `attributes` / `geometry`, anneaux `rings` ou tracés `paths`).
- Les six méthodes d'upload, qui dupliquaient la même boucle d'analyse, se réduisent à une
  déclaration chacune.

**Supprimés** : `waste/domain/model/package-info.java`, `waste/domain/repository/package-info.java`,
`territory/domain/model/package-info.java` — les trois expositions ouvertes la veille.

⚠️ **Il reste une exposition, antérieure** : `territory.domain.repository`
(`@NamedInterface("repositories")`), avec quatre consommateurs — `CrossContextReferenceValidator`,
`DepotoirServiceImpl`, `DashboardServiceImpl` et `GeoJsonImportServiceImpl`. Ce n'était pas l'objet
de cette tâche ; la refermer suppose de traiter la vérification d'existence cross-contexte et
l'écriture de géométries depuis `waste`, ce qui est un chantier distinct.

### ADR-0013 — 🏁 Migration terminée : plus rien dans `sonaged.collecte.master` (2026-07-28)

Dernière passe, sur décision explicite de finir malgré la réserve exprimée.

- **Import GeoJSON déplacé** vers `administration` (7 fichiers), réparti en `application/service`,
  `application/service/impl`, `infrastructure/geojson`, `presentation/controller`.
- **Les 6 `POST /data/{ressource}` quittent `DashboardController`** pour un `DataImportController`
  dans `administration`, **aux mêmes URL** (le frontend n'est pas touché). Ils héritaient du préfixe
  public du tableau de bord : c'est la cause structurelle des écritures joignables sans jeton,
  corrigée à la racine. Au passage, l'invariant « personne ne dépend d'`administration` » est
  préservé — sans ce déplacement, `analytics` en aurait dépendu.
- **`SonagedApplication` remonte à la racine** `sn.smartwaste.collect`, sa place conventionnelle.
  `scanBasePackages`, `@EntityScan` et `@EnableJpaRepositories` explicites **disparaissent** : le
  scan par défaut suffit désormais. `ApplicationContextLoadsTest` n'a plus besoin de
  `classes = ...`, ce qui referme le piège de la recherche ascendante de `@SpringBootConfiguration`.
- `sonaged.collecte.master` **n'existe plus**. Subsiste `sonaged.ucg` : l'échafaudage mort de
  l'ADR-0010 (11 classes marqueurs, aucun bean), conservé faute de validation pour le retirer.

#### ⚠️ Le prix payé, à ne pas oublier
Trois `@NamedInterface` **transitoires** ont dû être ouvertes pour que l'import reste légal :
`waste.domain.model`, `waste.domain.repository` et `territory.domain.model`. C'est contraire à
l'ADR-0013 §3 et c'est exactement ce qui avait été refusé pour `analytics`. La raison est assumée :
`UploadFileServiceImpl` écrit **directement dans 12 repositories** de deux contextes, et le recâbler
sur les services applicatifs est un refactoring de ~550 lignes, pas un déplacement de packages.

**Ces trois fichiers sont la dette de la migration.** Le jour où l'import passe par les services
applicatifs, ils disparaissent et les frontières se referment d'elles-mêmes. Chacun le dit dans son
propre javadoc pour que personne ne s'en serve par commodité.

**Bilan ADR-0013** : 10 modules, `modules.verify()` sans violation, ~197 classes déplacées,
52 tests (51 passants, 1 ignoré), build vert.

### ADR-0013 — Amorçage et technique transverse (2026-07-28)

Dernière passe. `SonagedApplication`, `MinioConfig`, `OpenApiConfig`, les annotations maison, les
deux aspects AOP et les deux `@ControllerAdvice` rejoignent `sn.smartwaste.collect.config`.

**Il ne reste que l'import GeoJSON** dans `sonaged.collecte.master` (7 fichiers), et c'est
délibéré. Le déplacer dans `administration` imposerait d'exposer les **entités et les
repositories** de `territory` **et** de `waste` — il y écrit directement, dans 12 repositories.
Ce serait exactement l'anti-pattern écarté pour `analytics`, et en écriture. Sa migration suppose
de le recâbler sur les services applicatifs de chaque contexte : un **refactoring**, pas un
déplacement de packages, et il mérite son propre commit.

- `SonagedApplicationTests` suit son sujet (toujours `@Disabled`, redondant avec
  `ApplicationContextLoadsTest` — candidat à suppression, sous réserve de validation).
- Deux vestiges documentés dans le `package-info` du module : `DataNotifierAspect`, dont le
  pointcut vise `com.worldline.tapandgo` (projet étranger — l'aspect ne peut jamais se déclencher),
  et `SleuthTraceJmsListener`, alors que Spring Cloud Sleuth n'est pas une dépendance.
- Le scan reste sur les deux racines tant que l'import n'a pas bougé ; `sonaged.ucg` ne contient
  plus que l'échafaudage mort de l'ADR-0010, sans aucun bean.

**Bilan de la migration ADR-0013** : 10 modules, `modules.verify()` sans violation, ~190 classes
déplacées, 52 tests (51 passants, 1 ignoré), build vert.

### Réconciliation documentaire + sessions révocables (2026-07-28)

#### Documentation : fermeture de la boucle de retour
La cartographie exhaustive (`docs/KNOWLEDGE_MAP.md`) a mis au jour une cause racine unique : la chaîne
descendante *spécification → analyse → plan → ADR → code* fonctionne, mais **rien ne remonte**. Ce
journal enregistrait le réel ; ni les statuts d'ADR, ni `PROJECT_STATUS.md`, ni `CLAUDE.md`, ni
`architecture-cible.md` n'étaient mis à jour en retour. Corrigé :

- **Statuts des 13 ADR** alignés sur l'état constaté dans le code. Ils étaient tous « Proposé » sous
  une règle affirmant qu'« aucune implémentation n'est lancée avant passage à Accepté » — alors que la
  moitié était implémentée. L'index ne renseignait plus sur rien.
- **`CLAUDE.md`** : c'est le seul document chargé automatiquement à chaque session, donc celui dont les
  erreurs se propagent le plus. Corrigé sur Java 21 / Boot 3.5.3 / Angular 17, l'existence du
  `.gitignore`, l'architecture (DDD 10 modules et non « layered »), les ports de frontière, le
  `Sonaged@123` résiduel dans `createUser`, et une hiérarchie explicite des documents fiables.
- **`PROJECT_STATUS.md` et `PROJECT_ANALYSIS.md`** portent un bandeau **ARCHIVE gelée au 2026-07-11**.
  Le premier était encore désigné par `CLAUDE.md` comme « autoritatif, à lire avant de planifier »,
  alors que la majorité des défauts qu'il liste sont corrigés — et que sa numérotation P0 **diffère**
  de celle de la ROADMAP (« P0-4 » ne désignait pas la même chose selon le fichier lu).
- **`architecture-cible.md`** porte un bandeau d'obsolescence : il décrit les 5 modules d'ADR-0010,
  remplacé, alors que le README le présentait comme la cible. Le bandeau distingue ce qui reste valable
  (flux métier IoT, ordre d'extraction, entités à créer) de ce qui est faux (découpage, packages,
  `Geometry`/`Coordinate` en embeddables).
- **`README.md`** : 12 → 13 ADR, cible corrigée en ADR-0013, renvoi vers la cartographie.
- **ADR-0006 marqué « prémisse invalidée »** : il retient `angular/` comme « le plus complet » et
  propose de supprimer `sonaged_web/`. Mesure : `angular/` = 164 fichiers `.ts` / ~9 900 l. (Angular
  17.0.7, NgModules) ; **`sonaged_web/` = 196 fichiers / ~16 900 l. (Angular 17.3, standalone)**.
  L'appliquer en l'état pourrait supprimer la meilleure base.
- **ADR-0004 marqué « périmètre à revoir »** : le mémoire demande des seuils de **température et
  d'humidité** (capteur DHT11) en plus du remplissage ; l'ADR ne modélise que le remplissage.
- Les **décisions prises hors ADR** (UUID v7, module `administration`, ports applicatifs, propriétaire
  du rattachement tenant, soft-delete) sont désormais listées dans `docs/adr/README.md` comme dette
  documentaire assumée.

#### Sessions d'authentification révocables (contexte identity)
- **Le défaut corrigé** : l'authentification était purement autoportante — un JWT signé valable
  **10 jours**, rien côté serveur. La déconnexion n'existait donc pas : `logout()` vidait le
  `localStorage` du navigateur et le jeton restait parfaitement valide. Un jeton copié ne pouvait pas
  être révoqué, et un changement de mot de passe ne fermait aucune session.
- **`UserSession`** + `POST /auth/refresh` + `POST /auth/logout`. Le jeton d'accès porte un claim
  `sid` ; `JwtFilter` vérifie à chaque requête que la session est ouverte — c'est ce qui rend la
  déconnexion réelle. Coût assumé : une lecture indexée par requête authentifiée, prix incontournable
  de la révocation (un JWT autoportant ne peut pas être révoqué, par construction).
- **Le jeton de rafraîchissement est opaque, pas un JWT** : un JWT se vérifie sans la base, ce qui est
  exactement ce qu'on ne veut pas pour le jeton révocable. 256 bits de `SecureRandom`, stocké
  **haché en SHA-256** — une base exfiltrée ne permet pas de rejouer les sessions. SHA-256 sans sel
  volontairement : le raisonnement bcrypt vaut pour un mot de passe à faible entropie, pas pour 256
  bits d'aléa.
- **Rotation à chaque rafraîchissement** : si un jeton est volé, la première des deux parties à s'en
  servir invalide l'autre — l'anomalie devient visible au lieu de rester silencieuse.
- **Messages d'échec indiscernables** (inconnu / révoqué / expiré → même texte) : distinguer les cas
  renseignerait un attaquant sur la validité d'un jeton en sa possession.
- **Réponse de connexion inchangée** (`bearer`), `refresh` purement additif : aucun client existant ne
  casse. **La TTL du jeton d'accès reste à 10 jours** et devient configurable
  (`sonaged.security.jwt.access-ttl-ms`) : la réduire — ce que la session rend enfin possible —
  déconnecterait les clients qui n'appellent pas encore `/auth/refresh`. C'est un changement de
  configuration à faire quand les clients savent rafraîchir, pas un effet de bord de ce chantier.
- **Second secret versionné supprimé** : `ENCRIPTION_KEY` / `getKey()` dans `JwtService`. Ce n'était
  **pas** la clé de signature (`SecurityConstants.SECRET` l'est) et son unique appelant était un bloc
  commenté. La revue de sécurité automatique l'avait signalé en croyant l'inverse. Complète l'ADR-0009 §3.
  ⚠️ Les deux secrets restent dans l'historique Git et doivent être rotés (ADR-0002 §4-5).
- **Changelog `2.3.0_user_session.xml`**, non destructeur.
- **10 tests, vérifiés par deux mutations** : supprimer la rotation → 1 échec ; accepter une session
  révoquée → 2 échecs.
- ⚠️ **Tension à arbitrer** : `docs/keycloak-migration.md` §4 prévoit de **supprimer** `JwtService`,
  `JwtFilter` et `SecurityConstants`, que ce chantier étend. Les deux directions sont défendables ;
  elles ne doivent pas être menées en parallèle.

- **Statut** : ✅ `./mvnw clean verify` vert — **52 tests, 51 passants, 1 ignoré**.

### Le contexte Spring démarre — pour la première fois vérifié (2026-07-28)

**C'est le trou le plus grave qui restait**, et l'ADR-0013 se l'attribuait lui-même : « une régression de câblage Spring ne serait visible qu'au démarrage ». Aucun test n'instanciait le contexte — le seul qui l'aurait fait, `SonagedApplicationTests`, était `@Disabled` faute de PostgreSQL. Toute la classe d'erreurs « ça compile mais ça ne démarre pas » (bean manquant, injection ambiguë, référence circulaire, classe sortie du périmètre de `scanBasePackages`) était donc invisible — pendant une migration qui déplace des centaines de classes entre deux racines de packages.

- **`ApplicationContextLoadsTest`** démarre le contexte complet sur **H2 en mode compatibilité PostgreSQL** (dépendance de test ajoutée), Liquibase désactivé, schéma dérivé des entités. Trois tests : le contexte démarre, **les deux racines sont scannées** (un bean de chaque), et **chaque port publié a exactement une implémentation** — une injection ambiguë ou manquante ne casse qu'au démarrage, jamais à la compilation.
- **Le piège rencontré est instructif** : placé dans `sn.smartwaste.collect`, le test échouait sur « Unable to find a @SpringBootConfiguration » — la recherche ascendante part du package du test et ne peut pas atteindre `SonagedApplication`, resté dans `sonaged.collecte.master`. C'est exactement ce qui rendait l'ancien test inopérant avant qu'il ne soit déplacé puis désactivé. Résolu par `@SpringBootTest(classes = SonagedApplication.class)` — et c'est un argument de plus pour faire remonter la classe d'application dans la racine cible.
- ⚠️ **Ce que ce test ne prouve pas** : que les entités correspondent au schéma **Liquibase**. Un schéma dérivé des entités leur est cohérent *par construction* — c'est précisément ce que `ddl-auto: validate` sert à contredire, au démarrage réel contre PostgreSQL, qui n'a toujours pas été exercé. Ne pas confondre les deux garanties.
- `SonagedApplicationTests` (désactivé) devient redondant ; laissé en place (suppression → validation).

### ADR-0013 §4 — Contexte `tenant` : fondations multi-tenant (2026-07-28)

Objectif de l'ADR : « l'architecture doit **porter** l'isolation avant qu'elle ne soit exploitée ».

- **`Organization`** (collectivité cliente) et **`OrganizationMembership`** (rattachement utilisateur → collectivité), en UUID v7, + repositories. Changelog **`2.2.0_tenant.xml`**, **non destructeur** cette fois : deux tables ajoutées, rien de touché.
- **Le rattachement vit côté tenant, pas sur `UserEntity`.** Poser un `organizationId` sur l'utilisateur aurait été plus court, mais aurait mis une donnée de cloisonnement dans « Identité & Accès », qui répond à une autre question : *qui es-tu*, pas *pour le compte de quelle collectivité*. Conséquence concrète : **l'identité n'a pas eu à changer**, et ouvrir au multi-collectivités ne demandera que de lever une contrainte d'unicité.
- **`CurrentTenantProvider`** (`@NamedInterface("api")`) rend un `Optional<UUID>`.

#### Pourquoi pas un filtre + ThreadLocal, la solution réflexe
Deux raisons, la première rédhibitoire :
1. **Cycle de modules.** Le filtre devrait être inséré dans la chaîne Spring Security, déclarée par `SecurityConfiguration` — qui appartient à *identity*. Ce module dépendrait donc de *tenant*, lequel dépend déjà de lui pour connaître l'utilisateur. `modules.verify()` l'aurait refusé, à raison.
2. **Fuite entre requêtes.** Un `ThreadLocal` mal nettoyé se propage d'une requête à l'autre sur un pool de threads. Fuiter un identifiant de tenant, c'est servir les données d'une collectivité à une autre — le pire défaut possible dans un SaaS cloisonné.

La résolution est donc **paresseuse** : une lecture indexée sur `userId` au moment où la question est posée. Si le volume l'exigeait, la parade serait un cache de portée requête, pas un `ThreadLocal` maison.

- **3 tests, vérifiés par mutation.** Le cas décisif est la requête **non authentifiée** : `CurrentUserProvider` lève alors `IllegalStateException`, et la laisser remonter ferait répondre 500 à tout endpoint public interrogeant le tenant. Mutation : en retirant le `try/catch`, le test échoue bien sur `IllegalStateException: Aucun utilisateur authentifié`.
- **Volontairement absent** : aucun `tenantId` sur les agrégats métier, et **aucun rattachement semé**. Le premier est P2-3 (XL) et suppose de trancher, agrégat par agrégat, ce qui est cloisonné et ce qui reste partagé — le référentiel territorial de Pikine n'a aucune raison d'être dupliqué par collectivité. Le second donnerait l'illusion d'un cloisonnement qui n'est pas encore appliqué. `currentOrganizationId()` rend donc `empty` aujourd'hui, et **`empty` n'autorise rien** : l'appelant qui cloisonnera devra refuser, jamais élargir au global.
- Pas encore de CRUD d'organisation : la gestion des collectivités est une décision produit (qui les crée ? un super-admin transverse ?), pas une conséquence de la migration.

- **Statut** : ✅ `./mvnw clean verify` vert — **42 tests, 41 passants, 1 ignoré**. 10 modules, aucune violation.

### ADR-0013 — Read-models de carte, corbeille transverse, module `administration` (2026-07-28)

#### 🔴 Import de données ouvert sans authentification
`SecurityConfiguration` déclarait `.requestMatchers("/data/**").permitAll()` — **toutes méthodes confondues**. Or ce préfixe ne sert pas qu'aux compteurs publics du tableau de bord : `DashboardController` y expose aussi **sept `POST /data/{commune|department|quartier|circuitcollect|circuitbalayage|depotoir|…}`** qui écrivent des GeoJSON en base. N'importe qui, sans jeton, pouvait donc injecter ou écraser le référentiel territorial et les points de collecte.
- Corrigé en restreignant l'ouverture à la **lecture** : `.requestMatchers(GET, "/data/**").permitAll()`. L'écriture retombe sur `anyRequest().authenticated()`.
- **Non régressif** : `JwtInterceptor` (Angular) pose le jeton sur *toutes* les requêtes, sans filtre d'URL, et l'écran d'import n'est atteignable qu'authentifié — vérifié avant de changer la règle.
- Cause structurelle : des endpoints d'**écriture d'administration** vivent dans le contrôleur du tableau de bord, donc sous son préfixe public. Ils rejoindront `administration` avec le reste de l'import.

#### Read-models de carte : produits par le propriétaire, assemblés par la supervision
- `DepotoirMaps` → `waste.application.api`, `DepartmentMaps` → nouvelle `territory.application.api` (`@NamedInterface("api")`). Ces projections sont **produites** par le contexte propriétaire, qui seul sait résoudre une géométrie et un type dans sa transaction.
- `MapsController` rejoint `analytics.presentation` : la carte est un *read-side*, au même titre que le tableau de bord. Il n'assemble plus que deux ports (`WasteReadModel.collectionPointsForMap()`, `TerritoryReadModel.firstDepartmentForMap()`) et ne dépend plus d'aucun service interne.
- `TerritoryReadModel` est le **premier pas hors de la concession** `@NamedInterface("repositories")` du référentiel : un contrat applicatif plutôt que six repositories exposés.
- **Corrigé au passage** : `DepotoirMaps.java` vivait dans `dto/maps/` en déclarant `package …dto;` (incohérence répertoire/package datant de `cae9be4`). Maven compilait, mais la navigation IDE et tout outillage supposant l'arborescence standard s'y cassaient les dents.

#### 🔴 Deux NPE qui vidaient la carte de supervision
`getDepotoirMap()` faisait `d.getGeometry().getType()` **et** `d.getTypeDepotoir().getName()` sans aucune garde ; `getFirstDepartment()` déréférençait `department.getGeometry()` de même. Les trois associations sont facultatives — un dépotoir créé depuis les écrans CRUD n'a ni contour ni type. **Un seul enregistrement de ce genre faisait répondre 500 à tout l'endpoint** : carte vide, sans message exploitable.
- Un point sans géométrie est désormais **omis** (on ne peut pas le dessiner) sans emporter les autres ; un type absent donne un `typeDepot` nul ; un département sans contour est rendu sans tracé plutôt que pas du tout.
- **`DepotoirMapReadModelTest`, 4 tests, vérifiés par mutation** : en retirant la garde, le test échoue bien sur `NullPointerException: Cannot invoke "GeometryEntity.getType()" because "geometry" is null`.

#### Nouveau module `administration` (non-contexte)
- Accueille la **corbeille** : `DeletionController` + `DeletionPurgeScheduler`. `SoftDeleteService` va en revanche dans le **shared kernel** (`shared.domain.service`) — les services métier l'utilisent, il ne pouvait donc pas rejoindre un module dont personne ne doit dépendre.
- **Justification du 3ᵉ module non-contexte.** Ces opérations ne sont le métier de personne : la corbeille agit sur *toute* entité soft-deletable, l'import écrit dans le référentiel *et* dans le cœur métier. Les ranger dans un contexte lui donnerait autorité sur les autres ; les mettre dans le shared kernel en ferait une dépendance de tout le système, contrôleurs compris. L'ADR-0013 admet déjà `shared` et `config` comme non-contextes ; celui-ci est le troisième, dans le même esprit. **Invariant à tenir : personne ne dépend d'`administration`** — c'est ce qui préserve l'acyclicité malgré sa position transverse.

#### Statuer sur l'import GeoJSON — décision prise, exécution différée
**Destination : `administration`.** Mais le déplacement ne peut pas être mécanique. `UploadFileServiceImpl` (~550 lignes) écrit **directement** dans 12 repositories des contextes `territory` et `waste`. Deux voies :
1. **Exposer les 12 repositories** en `@NamedInterface` — rejeté : c'est exactement l'anti-pattern écarté pour `analytics` (ADR-0013 §3), et en écriture, ce qui est pire.
2. **Recâbler l'import sur les services applicatifs** de chaque contexte (`RegionService`, `CommuneService`, `DepotoirService`… qui existent déjà). C'est la bonne cible, mais c'est un **refactoring** de la logique d'assemblage, pas un déplacement de packages.
La voie 2 est retenue, et traitée comme une tâche à part entière — la faire en douce au milieu d'une migration mélangerait deux natures de changement dans un même diff. En attendant, l'import reste dans `sonaged.collecte.master`, d'où il ne gêne personne : le code hérité a le droit de dépendre des nouveaux modules.

- **Statut** : ✅ `./mvnw clean verify` vert — **36 tests, 35 passants, 1 ignoré**. `modules.verify()` reconnaît les **10 modules** et ne signale aucune violation.
- **Reste dans le legacy : 16 fichiers**, et **un seul import** du legacy depuis la nouvelle racine (`UploadFileService`, depuis `DashboardController`). Le reste est de l'amorçage (`SonagedApplication`, `config/`, `annotations/`, `aspects/`, gestionnaires d'exceptions) plus l'import GeoJSON.

### ADR-0013 — Contexte `waste` migré, `analytics` sevré des repositories (2026-07-27)

**55 fichiers** déplacés vers `sn.smartwaste.collect.waste` en Clean Architecture : 9 entités + 2 enums (`AlertCode`, `CircuitShift`) en `domain/model`, 9 repositories en `domain/repository`, 9 DTO + 9 mappers + 10 services + 9 implémentations en `application`, 8 contrôleurs en `presentation`. `Image` est bien reclassée ici (et non dans Plateforme) : l'ADR-0012 §1 range `Alert → Image` dans un même contexte. `CrossContextReferenceValidator` suit, seul `waste` l'utilisant. **Il ne reste que 22 fichiers** dans `sonaged.collecte.master`.

**Diffusion SSE rangée dans Plateforme.** `AlertBroadcaster` et `AlertStreamController` vont dans `platform`, conformément au schéma de l'ADR-0013 §3 (`waste ──AlertRaised──▶ platform`) : l'écouteur d'un événement appartient au contexte qui notifie, pas à celui qui produit.

#### `analytics` ne touche plus aucun repository d'un autre contexte
`modules.verify()` a listé **28 violations** dès le déplacement : la supervision lisait quatre repositories de `waste`, naviguait dans ses entités JPA, et dépendait d'`AlertBroadcaster`. Deux ports publiés les remplacent :
- **`waste.application.api.WasteReadModel`** (`@NamedInterface("api")`) — compteurs du tableau de bord + trois projections autonomes (`ActiveAlert`, `ActiveCollectionPoint`, `ActiveCircuit`). Aucune entité ne franchit plus la frontière.
- **`platform.application.api.AlertStreamMetrics`** — nombre de flux SSE ouverts, implémenté par `AlertBroadcaster`.
- **Bénéfice non prévu** : les projections sont produites dans la transaction du contexte propriétaire. L'accès à `Depotoir.typeDepotoir`, LAZY depuis P1-2, ne dépend plus de la transaction de l'appelant — la supervision portait jusqu'ici cette contrainte sans raison.
- **Encore des injections mortes** : `DashboardServiceImpl` injectait **sept** repositories jamais utilisés (`circuitRepository`, `typeDepotoirRepository`, `geometryRepository`, `coordinateRepository`, `departmentRepository`, `regionRepository`, `quartierRepository`) — après `UserRepository` à l'itération précédente. Retirés. La moitié du couplage `analytics → tout le reste` n'était donc que du code mort.
- La corbeille (`Map<String, SoftDeleteRepository<?,?>>`) est **conservée telle quelle** : l'injection est faite par type sur une interface du shared kernel, donc sans dépendance vers les modules propriétaires — Modulith ne la signale pas, à juste titre.

#### 🔴 `AlertRaisedEvent` transportait le DTO d'un contexte, depuis le shared kernel
- Le contrat d'événement vit dans `shared`, qui est un module **ouvert** dont tout le système dépend. En portant `waste.application.dto.Alert`, il faisait du contexte « Déchets » une dépendance implicite de **tous** les autres (`Module 'shared' depends on non-exposed type … within module 'waste'`).
- La charge utile devient autonome : `RaisedAlert` + `ImageRef`, en types primitifs, définis dans `shared`. `code` devient une `String` — le noyau partagé n'a pas à connaître le vocabulaire métier de `waste`, et Jackson produisait déjà cette chaîne.
- **Format SSE préservé** pour le frontend (`alertId`, `object`, `message`, `address`, `code`, `image.url`). Deux champs disparaissent volontairement : `coordinate`, qu'aucun abonné ne lit, et **`file` — un `MultipartFile`** : un flux de requête HTTP n'avait rien à faire dans une charge utile sérialisée en SSE et n'aurait pas survécu à la fin de la requête.
- **`AlertRaisedEventPayloadTest`, 3 tests, vérifiés par mutation** : le test assemble le JSON réellement émis et l'assertionne champ par champ, parce que **aucun type ne relie le backend au gabarit Angular** — un renommage casserait le temps réel en silence. Mutation : renommer `alertId` en `id` fait bien échouer le test.

#### Géométrie : exposition nommée, dette documentée
`waste` compose `GeometryEntity`/`CoordinateEntity` (FK `geometryid`, rétablies par le changelog 2.0.0) et imbrique leurs DTO. Ces cinq types sont exposés par `@NamedInterface("geo")` **au niveau du type** — et non du package, ce qui aurait exposé toutes les entités territoriales au passage. Décision assumée et annotée dans le code : la géométrie est conceptuellement un *value object* partagé (le `package-info` du shared kernel annonce déjà des « value objects géographiques »), mais l'y déplacer emporterait repositories, DTO, mappers, services **et contrôleurs** — or un contrôleur n'a rien à faire dans un noyau partagé. La décision de modélisation mérite d'être prise pour elle-même, pas au détour d'une migration.

- **Statut** : ✅ `./mvnw clean verify` vert — **32 tests, 31 passants, 1 ignoré**. `modules.verify()` passe sur les 5 contextes peuplés, sans aucune violation.
- **Reste dans le legacy (22 fichiers)** : amorçage (`SonagedApplication`, `config/`, `annotations/`, `aspects/`, gestionnaires d'exceptions), la corbeille transverse (`SoftDeleteService`, `DeletionPurgeScheduler`, `DeletionController`), l'import (`UploadFileService(+Impl)`, `service/geojson/`) et les read-models de carte (`MapsController`, `dto/maps/`). Ce sont exactement les trois points suivants du plan.
- **Découverte à traiter** : `DataNotifierAspect` a un pointcut sur `com.worldline.tapandgo.user.annotations.Notifiable` — un vestige d'un autre projet. Il ne peut donc **jamais** intercepter le `@Notifiable` local : l'aspect est mort. `HistoryEntity` est par ailleurs une coquille vide (un `@Id` sans générateur, aucun autre champ) qui traîne DTO, mapper, repository, service et contrôleur.

### 🔴 Mapping JPA cassé : le démarrage était impossible (2026-07-27)
*Trouvé en préparant la migration du contexte `waste` : c'est la dernière dépendance `territory → waste` qui restait.*

- **Le défaut.** `CommuneEntity` portait trois `@OneToMany(mappedBy = "commune")` vers `DepotoirEntity`, `CircuitCollectEntity` et `CircuitBalayageEntity`. Or P1-7a (ADR-0012) avait converti le côté propriétaire en `UUID communeId` : **le champ `commune` n'existe plus sur aucune des trois**. Hibernate refuse de construire le métamodèle :
  `AnnotationException: Collection 'CommuneEntity.depotoirs' is 'mappedBy' a property named 'commune' which does not exist in the target entity 'DepotoirEntity'`
- **Pourquoi c'est passé inaperçu** : `mappedBy` est une **chaîne de caractères**, le compilateur ne la vérifie pas. Le pendant sur `QuartierEntity.depotoirs` avait bien été commenté lors de P1-7a ; ces trois-là ont été oubliés. Le build restait vert, et l'échec ne se serait manifesté qu'au premier démarrage réel — qui n'a jamais eu lieu.
- **Le correctif** : les trois collections sont retirées. Ce n'est pas un arbitrage de modélisation — une association inverse **ne peut pas** exister quand le côté propriétaire est une référence par identifiant. Les dépotoirs/circuits d'une commune se lisent via le contexte propriétaire (`findByCommuneId`). Aucun code ne lisait ces collections (vérifié). Au passage, leurs `CascadeType.ALL` cross-contexte auraient supprimé en cascade les dépotoirs et circuits d'une commune effacée — le risque R4 signalé en P1-2, bien réel ici.
- **Bénéfice de frontière** : `territory` n'importe plus rien du contexte « Déchets ». Il redevient ce que l'ADR-0013 prescrit — une source de vérité qui ne dépend de personne — ce qui débloque la migration de `waste`.
- **Garde-fou ajouté : `JpaMappingBootstrapTest`.** Il construit le métamodèle Hibernate de **toutes** les entités des deux racines, **sans base de données** (dialecte imposé, donc aucune connexion). Il ferme toute une classe d'erreurs que le compilateur ne voit pas : `mappedBy` orphelin, `@JoinColumn` en double, identifiant incohérent. Les entités sont découvertes par scan (le test couvrira donc les futures entités automatiquement) avec une assertion de cardinalité minimale, pour qu'il ne puisse pas passer à vide comme l'ont fait les tests Modulith et `AuthorityServiceImplTest`.
- **Vérifié par mutation** : en rétablissant une seule des trois collections, le test échoue avec exactement l'`AnnotationException` ci-dessus.
- ⚠️ Ce test ne valide **pas** que le schéma SQL correspond aux entités — seulement que le mapping objet est cohérent avec lui-même. La correspondance au schéma Liquibase reste vérifiée par `ddl-auto: validate`, au démarrage réel, qui n'a toujours pas été exercé.

#### 🔴 Élévation de privilèges à l'inscription publique — trouvée et corrigée (2026-07-27)
- **La faille** : `/auth/register` est en `permitAll`, et `register()` faisait `user.setAuthority(user.getAuthority())` — le rôle arrivait donc **du corps de la requête**. Chemin d'exploitation complet et non authentifié : s'inscrire avec `authority: {authorityId: <uuid SUPER_ADMIN>}`, recevoir le code d'activation à sa propre adresse, activer, se connecter administrateur. Aggravé par le fait que les règles `SecurityRule` sont inertes (cf. ci-dessous) : `POST /v1/users` n'exige qu'un compte authentifié, donc le compte ainsi obtenu peut en créer d'autres.
- **Le correctif** : le rôle est imposé côté serveur (`USER`, via `findByNameAndDeletionStatus`), **après** le mapping DTO → entité pour qu'aucune valeur du client ne survive. Le filtre sur `DeletionStatus` évite d'attribuer un rôle mis à la corbeille, qui disparaîtrait à la purge.
- **Effet de bord : l'inscription était de toute façon cassée.** Le formulaire Angular n'envoie aucun `authority` alors que la colonne est `nullable=false` — tout signup légitime échouait sur une violation de contrainte. Le correctif ferme la faille *et* répare le parcours.
- **6 tests ajoutés** (`AuthServiceImplTest`), **vérifiés par mutation** : en rétablissant l'ancien comportement, le test échoue bien (`expected: "USER" but was: "SUPER_ADMIN"`). Il ne s'agit donc pas d'un test qui passe à vide, contrairement à ce que fut `AuthorityServiceImplTest`.
- Trouvé via la revue de sécurité automatique déclenchée par la migration — les fichiers déplacés ont été re-scannés comme neufs.

#### Points connus, volontairement non traités ici
- **Deux secrets versionnés dans `JwtService`, dont un mort.** La revue automatique a signalé `ENCRIPTION_KEY` ; c'est bien un secret versionné, mais **ce n'est pas la clé de signature** : son unique lecteur `getKey()` n'est appelé que depuis un bloc `getAllClaims()` **commenté**. La signature et la vérification utilisent `SecurityConstants.SECRET`, que la revue n'a pas signalé. Les deux sont compromis et à roter ; seul le second influe aujourd'hui sur la validité des jetons. P0-2 / ADR-0002 — non touché ici (CLAUDE.md : signaler avant d'intervenir sur les secrets).
- **`createUser` et `register` divergent** : `UserServiceImpl.createUser` force encore `encode("Sonaged@123")` alors que `AuthServiceImpl.register` hache bien le mot de passe fourni. Le correctif relève de P0-1/P0-A (Keycloak), pas d'une migration de packages.
- **CSRF désactivé avec `setAllowCredentials(true)`** : signalé par la revue. Non exploitable en l'état — la chaîne est `STATELESS` et l'authentification passe uniquement par l'en-tête `Authorization`, donc aucune créance ambiante ne peut être rejouée. À passer à `false` pour rendre le contrat « jeton uniquement » explicite.
- **Les beans `SecurityRule` ne sont jamais appliqués** : `AuthorityRules` et `UserRules` déclarent 13 règles d'autorisation que `SecurityConfiguration` n'appelle nulle part (`configure(...)` n'est invoqué par personne). L'autorisation réelle se limite donc à `anyRequest().authenticated()` — **aucun contrôle de permission par rôle n'est actif**. Migré tel quel pour ne pas changer le comportement au milieu d'un déplacement de packages ; à trancher (câbler ou retirer) avec P0-A.
- **`AuthReponse`, `AuthRequest`, `UserResponse`, `AuthorityInfo`** ne sont référencés nulle part. Conservés (suppression → validation requise).

## Découvertes à traiter (hors périmètre d'une itération)
- **Double config** `application.properties` + `application.yml` (valeurs qui se chevauchent) → à fusionner (le `.properties` prime, source de confusion).
- `schema.sql` + `spring.batch.initialize-schema=always` concurrencent Liquibase.

## Prochaines tâches (selon ROADMAP, hors IoT)
- **Suite ADR-0013** — migrer `waste` (le gros morceau : dépotoirs, circuits, alertes, mobilier, historique, images ; y reclasser `Image`, cf. point 3 de la liste P1-7b), puis statuer sur `UploadFileServiceImpl` / import GeoJSON, puis les read-models de carte (`DepotoirMaps`, `DepartmentMaps`). Restent ensuite `tenant` et `iot`, à créer.
- **Convertir la concession `territory`** : remplacer `@NamedInterface("repositories")` par un port applicatif, sur le modèle d'`identity.application.api`.
- **P1-4** front unique *(suppression de dépôts → validation requise avant retrait)*.
- **P2** tests/CI, doc, multi-tenant.
