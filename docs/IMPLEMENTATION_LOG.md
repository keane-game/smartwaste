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

## Découvertes à traiter (hors périmètre d'une itération)
- **Double config** `application.properties` + `application.yml` (valeurs qui se chevauchent) → à fusionner (le `.properties` prime, source de confusion).
- `schema.sql` + `spring.batch.initialize-schema=always` concurrencent Liquibase.

## Prochaines tâches (selon ROADMAP, hors IoT)
- **P1-4** front unique *(suppression de dépôts → validation requise avant retrait)*. **P1-7** frontières Modulith + découplage systématique des entités cross-contexte par ID (commune, circuits…) + validation applicative + retrait des FK cross-contexte (ADR-0010/0012).
- **P2** tests/CI, doc, multi-tenant.
