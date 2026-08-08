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
- **Justification** : supprime d'un coup les 3 failles d'auth (secret versionné, mint maison, mot de passe codé en dur) et centralise l'auth pour le futur maillage (R1, R2).
- **Fichiers** : `pom.xml` (`spring-boot-starter-oauth2-resource-server`), `security/SecurityConfiguration.java`, **suppression** `security/JwtService.java` / `JwtFilter.java` / `constant/SecurityConstants.java`, converter de rôles, `docker-compose` (Keycloak), realm exporté versionné ; clients `angular/` et `mobileFlutter/` (flux PKCE).
- **Impact** : remplace l'auth maison ; migration des comptes ; adaptation des 3 clients. **Suppression de code sécurité → validation requise.**
- **Complexité** : L · **ADR-0011**.

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

### P0-4 · Trancher la gestion de schéma (Liquibase unique)
- **Objectif** : une seule source de vérité du schéma.
- **Justification** : Liquibase **et** `ddl-auto=update` actifs → risque d'écrasement/dérive (R5).
- **Fichiers** : `resources/application.properties` (`ddl-auto=validate`), `pom.xml`, nouveaux changelogs `resources/db/changelog/`.
- **Impact** : déploiements reproductibles ; migration initiale à générer depuis l'existant.
- **Complexité** : M · **ADR-0001**.

### P0-5 · Modèle d'ingestion du niveau de remplissage (cœur métier)
- **Objectif** : recevoir/stocker le niveau de remplissage d'un point de collecte.
- **Justification** : raison d'être du produit, aujourd'hui absente ; `Depotoir` n'a pas de `fillLevel`.
- **Fichiers** : nouveau `model/MeasurementEntity`, `repository/MeasurementRepository`, `controller/IngestionController` (`POST /v1/measurements`), `service/measurement/*`, ajout `Depotoir.fillLevel`/`lastMeasuredAt`.
- **Impact** : nouvelle brique centrale ; base des alertes.
- **Complexité** : L · **ADR-0004**.

### P0-6 · Moteur de seuils + déclenchement automatique d'alerte
- **Objectif** : seuil dépassé → création d'`Alert` reliée au dépotoir + notification.
- **Justification** : boucle métier « détecter → alerter » ; `Alert` n'est pas relié à `Depotoir`.
- **Fichiers** : `model/AlertEntity` (ajout `@ManyToOne Depotoir`), `service/AlertService`, nouveau `service/alerting/ThresholdEvaluator`, `service/impl/NotificationServiceImpl` (méthode d'alerte).
- **Impact** : cœur fonctionnel opérationnel.
- **Complexité** : L · **ADR-0004**, **ADR-0005**.

---

## P1 — Important (fiabilité, échelle, fonctionnalités)

### P1-1 · Nettoyer les dépendances backend
- **Objectif** : build stable et sans doublon.
- **Justification** : springfox 3.0.0 (abandonné, incompatible Boot 3) coexiste avec springdoc ; 2 libs JWT (R6).
- **Fichiers** : `pom.xml`.
- **Impact** : réduction du risque de conflit classpath ; démarrage plus sûr.
- **Complexité** : S · **ADR-0009**.

### P1-2 · Corriger le fetch/cascade des entités
- **Objectif** : `LAZY` par défaut, retirer les `CascadeType.ALL` fautifs.
- **Justification** : EAGER en chaîne (Depotoir→Commune→Department→Region, Geometry) → N+1 ; `CascadeType.ALL` vers `TypeDepotoir` (référentiel partagé) peut supprimer des données de référence (R3, R4).
- **Fichiers** : `model/DepotoirEntity`, `CommuneEntity`, `DepartmentEntity`, `Circuit*Entity`.
- **Impact** : perfs + intégrité ; adapter les requêtes qui dépendaient de l'EAGER (fetch-joins/projections).
- **Complexité** : M · **ADR-0008**.

### P1-3 · Sortir les images du BLOB
- **Objectif** : ne plus stocker `displayPicture byte[]` dans la table `ALERT`.
- **Justification** : BLOB en table = tables lourdes, back-ups coûteux, mémoire (R3).
- **Fichiers** : `model/AlertEntity`, `service/impl/UploadFileServiceImpl`, `service/ImageService`.
- **Impact** : stockage fichier/objet + URL référencée.
- **Complexité** : M · **ADR-0005**.
- **minio**: utlise minio pour le stock de images et fichier et garder le filename en db

### P1-4 · Aligner et consolider les frontends — ✅ **FAIT le 2026-08-05**
- **Résultat** : `sonaged_web/` est le front unique. `angular/` et `ucgFrontend/` supprimés
  (1 054 fichiers suivis). La décision d'ADR-0006 a été **inversée** : l'arbitrage s'est fait sur
  ce qui est coûteux à refaire (pile SIG Leaflet/proj4 + i18n) plutôt que sur la complétude
  fonctionnelle, la fraîcheur de l'API d'`angular/` ayant été récupérée avant suppression.
- **Reste à faire** : les défauts ouverts de `sonaged_web` sont listés dans
  `docs/FRONTEND_AUDIT.md` §5.2 — gardes de routes, chemins d'API au singulier, environnements
  de production, identifiants typés `number`.
- **ADR-0006** (statut : exécuté, décision inversée) · **`docs/FRONTEND_AUDIT.md`**.

### P1-5 · Import automatisé des GeoJSON
- **Objectif** : charger `datas/*.json` (quartiers, circuits, dépotoirs) en base.
- **Justification** : données de référence indispensables à la carte/alertes.
- **Fichiers** : nouveau seeder/`db/changelog` ou `service/import/GeoJsonImportService`.
- **Impact** : jeu de données réel exploitable.
- **Complexité** : M.

### P1-6 · Réactiver le lien Depotoir ↔ Quartier
- **Objectif** : rétablir la relation `Depotoir *─1 Quartier` (actuellement commentée).
- **Justification** : granularité de supervision (quartier) attendue métier.
- **Fichiers** : `model/DepotoirEntity`, changelog Liquibase.
- **Impact** : requêtes/carto par quartier. **NB** : entre contextes distincts, la relier par `quartierId` et non par association objet (ADR-0012).
- **Complexité** : S.

### P1-7 · Frontières de contexte (Spring Modulith) + découplage des entités
- **Objectif** : matérialiser les bounded contexts et remplacer les associations JPA **cross-contexte** par des références par identifiant.
- **Justification** : condition de faisabilité de l'évolution microservices ; réduit le couplage fort et les N+1 (R3, R7).
- **Fichiers** : ~~réorganisation en 5 modules — cf. `docs/architecture-cible.md`~~ **caduc** : cette tâche P1-7 décrit le découpage ADR-0010 (5 modules), remplacé par l'ADR-0013 (7 contextes bornés + 3 non-contextes sous `sn.smartwaste.collect`), **déjà réalisé** (migration terminée le 2026-07-28, `docs/IMPLEMENTATION_LOG.md`). `docs/architecture-cible.md` porte son propre bandeau obsolète — ne plus y renvoyer comme référence de cible. `pom.xml` (Spring Modulith) et le découplage par identifiant (FK objet → `…Id`) sont faits ; changelogs Liquibase associés déjà joués.
- **Impact** : structurant ; refactoring progressif contexte par contexte, testé par Modulith. **Pas de suppression massive sans validation.**
- **Complexité** : L · **ADR-0010**, **ADR-0012**.

---

## P2 — Amélioration (qualité, confort, évolutivité)

### P2-1 · Notifications temps réel
- **Objectif** : pousser les alertes vers les superviseurs sans polling.
- **Justification** : vision produit « temps réel ».
- **Fichiers** : nouveau `controller/NotificationSseController` (SSE) ou WebSocket ; front abonnement.
- **Impact** : UX supervision.
- **Complexité** : L · **ADR-0007**.

### P2-2 · Tests automatisés + CI/CD
- **Objectif** : couvrir services critiques (auth, seuils, alertes) + pipeline.
- **Justification** : ~2 tests aujourd'hui ; aucune CI.
- **Fichiers** : `backend-api/src/test/**`, `.github/workflows/*`.
- **Impact** : non-régression.
- **Complexité** : L.

### P2-3 · Multi-tenant (plusieurs collectivités)
- **Objectif** : isoler les données par collectivité.
- **Justification** : évolution cible « plusieurs collectivités ».
- **Fichiers** : entités (colonne `tenantId`/`organizationId`), filtres Hibernate, sécurité.
- **Impact** : structurant — à cadrer tôt même si implémenté plus tard.
- **Complexité** : XL · **ADR-0008**.

### P2-4 · Nettoyage & documentation
- **Objectif** : README racine réel, `endpoint.md` correct, retrait des restes de template Flutter, homogénéiser UCG/SONAGED.
- **Fichiers** : docs, `mobileFlutter/lib/core/app_env.dart`, `shared/domain/models/product/*`.
- **Impact** : onboarding.
- **Complexité** : S–M.

### P2-5 · Dashboards avancés
- **Objectif** : indicateurs (taux de remplissage, alertes/jour, tournées).
- **Fichiers** : `service/impl/DashboardServiceImpl`, front dashboard.
- **Complexité** : M.

---

## Séquencement recommandé
`P0-1 → P0-2 → P0-3` (sécurité, faible risque) → `P0-4` (schéma) → `P0-5 → P0-6` (cœur métier) → `P1-1..P1-4` (fiabilité/échelle/fronts) → `P1-5/P1-6` → `P2`.
