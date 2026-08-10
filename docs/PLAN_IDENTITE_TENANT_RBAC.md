# Plan de correction et d'implémentation — Identité, Users, Tenant, Rôles, Permissions

> Fait suite à `docs/API_AUDIT_AUTH_USERS_TENANT_RBAC` (audit factuel, 2026-08-09, aucune
> modification à l'époque). Ce document est le plan d'exécution des décisions prises dans
> [ADR-0011](adr/0011-keycloak-identity-provider.md) (reprise Keycloak),
> [ADR-0020](adr/0020-cloisonnement-multi-tenant-et-api-organisation.md) (multi-tenant) et
> [ADR-0021](adr/0021-completion-api-identite-pont-keycloak.md) (pont identité).
> Style et complexité : voir légende `ROADMAP.md` (S ≤0,5 j, M 0,5–2 j, L 2–5 j, XL >5 j/itératif).
> Aucune tâche « refonte » ou « suppression » ne démarre sans validation explicite (règle projet) —
> déjà obtenue ici pour les orientations de fond (reprise Keycloak, cloisonnement complet), pas pour
> chaque détail d'exécution qui reste à raffiner en implémentant.

## Principe de séquencement

1. **Les bugs se corrigent avant les fonctionnalités.** La désactivation de compte cassée et la
   modification de permissions sans effet sont des régressions silencieuses, pas des manques — elles
   passent avant tout le reste, quelle que soit la taille des chantiers Keycloak/multi-tenant.
2. **Le pont (ADR-0021) ne bloque pas la cible (ADR-0011).** Les deux avancent en parallèle : le pont
   comble un manque immédiat, la cible le rend caduc plus tard. Ne pas attendre l'un pour commencer
   l'autre.
3. **Keycloak (ADR-0011) et multi-tenant (ADR-0020) sont indépendants** (ADR-0020 §7) : aucun ordre
   imposé entre les deux, ils peuvent être menés par des sessions différentes sans se bloquer.
4. **Aucune étape XL ne se lance sans vérification d'infrastructure préalable** — leçon déjà tirée
   ailleurs dans ce projet (`PLAN_BACKLOG.md` §Principe 3 : « ce qui n'a jamais été exécuté ne
   fonctionne pas »). Pour Keycloak : vérifier le démon Docker avant d'écrire le moindre code
   d'intégration définitif.

---

## Lot 0 — Corrections de bugs (P0, livré le 2026-08-09)

**Pourquoi en premier.** Aucune dépendance, taille S, corrige des régressions silencieuses déjà en
production potentielle.

### 0.1 — Désactivation de compte réparée
- **Constat** : `UserServiceImpl.updateUser` ne recopie jamais `activated` depuis le DTO `User` —
  aucun moyen actuel de désactiver un compte via l'API.
- **Décision** (ADR-0021 §Vague 1) : endpoints dédiés plutôt qu'un champ de plus dans `PUT /v1/users/{id}`.
- **Fichiers** : `UserController` (`POST /v1/users/{id}/activate`, `POST /v1/users/{id}/deactivate`),
  `UserService`/`UserServiceImpl` (méthodes `activate`/`deactivate`), branchement de
  `SessionService.revokeAllForUser` à la désactivation.
- **Tests** : `UserServiceImplTest` (nouveaux cas), pas de régression sur `AdministrationAuthorizationTest`
  (garde ADMIN/SUPER_ADMIN héritée de `SecurityConfiguration`, aucune règle d'URL à ajouter).
- **Statut** : ✅ fait.

### 0.2 — Modification des permissions d'un rôle réparée
- **Constat** : `AuthorityServiceImpl.updateAuthority` n'applique que `name`, ignore `permissions`
  silencieusement.
- **Fichiers** : `AuthorityServiceImpl.updateAuthority`.
- **Tests** : `AuthorityServiceImplTest` (nouveau cas : mise à jour des permissions).
- **Statut** : ✅ fait.

---

## Lot 1 — Pont Identité & Accès, vague 2 — ✅ **livré (2026-08-09)**

**Pourquoi séparément du Lot 0.** Ces endpoints touchent l'e-mail transactionnel (comme l'activation
existante) et une nouvelle table — ils méritent leurs propres tests d'intégration (`MockMvc` +
vérification d'envoi d'événement), pas seulement des tests de service, contrairement aux corrections
ponctuelles du Lot 0.

### 1.1 — Changement de mot de passe (self-service) — ✅ fait
- **Endpoint** : `POST /auth/change-password` (authentifié — règle explicite dans
  `SecurityConfiguration`, avant le `permitAll` général de `/auth/**`).
- **Fichiers** : `AuthController`, `AuthService`/`AuthServiceImpl.changePassword`, branchement
  `SessionService.revokeAllForUser`.
- **Vérifié** : suite complète (295 tests) contre H2 (autorisation) et contre la base réelle
  (`LiquibaseSchemaMatchesEntitiesTest`, jointure schéma/entités) — pas de test manuel bout-en-bout
  via `smtp4dev` à cette étape.

### 1.2 — Réinitialisation de mot de passe (mot de passe oublié) — ✅ fait
- **Endpoints** : `POST /auth/password-reset/request`, `POST /auth/password-reset/confirm`.
- **Fichiers** : `PasswordResetToken` (+ repository, + changeset `2.26.0`, appliqué contre
  `jdbc:postgresql://localhost:5432/smartwaste`), `AuthController`, `PasswordResetServiceImpl`,
  événement dédié `PasswordResetRequested` (table et événement séparés de l'activation — ADR-0021 §4,
  tranché en faveur de la séparation).
- **Paramètre** : `sonaged.security.password-reset.ttl-minutes` (défaut 60).
- **Reste à vérifier** : bout-en-bout contre `smtp4dev` (demander un reset, récupérer le jeton dans
  l'e-mail reçu, confirmer) — couvert par tests unitaires (`PasswordResetServiceImplTest`), pas par
  un test d'intégration MockMvc ni un envoi réel.

### 1.3 — Catalogue de permissions — ✅ fait
- **Endpoint** : `GET /v1/permissions` (ADMIN/SUPER_ADMIN + `MANAGE_ROLE`, même garde qu'`/v1/authorities`).
- **Fichiers** : `PermissionController`.

---

## Lot 2 — Bascule Keycloak (P0-A, XL, cf. ADR-0011)

**Ne pas démarrer le code avant l'étape 2.1.** C'est la leçon du blocage initial de 2026-07-29 :
écrire une configuration Keycloak sans jamais la vérifier reproduirait exactement l'erreur déjà évitée.

### 2.1 — Vérification d'infrastructure — ❌ **bloqué, vérifié le 2026-08-09**
- Vérifié dans cette session : `docker` est introuvable, à la fois côté CLI (`command not found` en
  Bash et PowerShell) et côté service Windows (`com.docker.service` absent). Ce n'est plus
  « non vérifié » comme le disait `docs/keycloak-migration.md` — c'est **confirmé absent** dans cet
  environnement.
- **Conséquence** : le Lot 2 dans son ensemble reste bloqué, ainsi que la vérification bout-en-bout
  de `docs/PLAN_IDENTITE_TENANT_RBAC.md` §1.2 (reset de mot de passe via `smtp4dev`, lui aussi un
  service `docker-compose`). Rien à faire ici tant qu'un poste ou une CI avec démon Docker n'est pas
  disponible — pas de contournement à chercher.
- **Ce qui reste possible entre-temps** : la base PostgreSQL réelle, elle, est confirmée joignable
  (`jdbc:postgresql://localhost:5432/smartwaste`, hors Docker) — le Lot 3 (multi-tenant) n'est donc
  pas affecté par ce blocage.

### 2.2 — Resource server (backend)
- Suivre `docs/keycloak-migration.md` §2-3 : activer `KeycloakResourceServerConfig` derrière le
  profil `keycloak`, compiler et vérifier réellement le code de référence (jusqu'ici non fait).
- **Ne pas retirer `JwtService`/`JwtFilter`/`SecurityConstants`** avant que ce profil soit validé de
  bout en bout (§Vérification de `docs/keycloak-migration.md`) — ce sont eux qui portent les sessions
  révocables en vigueur.

### 2.3 — Profil utilisateur local
- `docs/keycloak-migration.md` §5 : `UserEntity` référencé par `keycloakSub`, sans mot de passe.
- Coordination ADR-0020 : `OrganizationMembership.userId` continue de référencer cet identifiant
  local — aucun changement requis côté tenant.

### 2.4 — Clients
- `sonaged_web/` : `angular-oauth2-oidc`, flux PKCE.
- `mobileFlutter/` : `flutter_appauth`, flux PKCE.

### 2.5 — Migration des comptes + retrait du code maison
- Import des comptes existants, réinitialisation forcée.
- Retirer `JwtService`, `JwtFilter`, `SecurityConstants`, le minting dans `AuthServiceImpl`, les
  endpoints du Lot 1 (§Ce que la bascule Keycloak rendra caduc, ADR-0021) — **suppression de code
  sécurité, validation explicite requise avant d'exécuter cette étape**, conformément à la règle
  projet.

---

## Lot 3 — Cloisonnement multi-tenant + API Organisation (P2-3, XL, cf. ADR-0020)

### 3.1 — API `tenant` minimale — ✅ **livré (2026-08-09, hors de ce plan)**
- Constaté en cours de session : `tenant/presentation/controller/OrganizationController` existe déjà
  (`GET/POST/PUT /v1/organizations`, `suspend`/`reactivate`, `GET/POST/DELETE .../members`),
  permission dédiée `MANAGE_ORGANIZATIONS` (SUPER_ADMIN uniquement, changelog `2.25.0`) — voir
  `docs/TENANT_ORGANIZATIONS_PLAN.md` (spécification suivie) et ADR-0020 §5 (mis à jour en
  conséquence). Ce travail n'a **pas** été fait par la présente session — probablement une session
  parallèle sur le même dépôt — mais la suite de tests complète passe avec, et son contrat correspond
  à ce que ce plan visait.
- **Reste à faire** : lecture/gestion des membres par un `ADMIN` scopé à sa propre organisation
  (aujourd'hui `MANAGE_ORGANIZATIONS`/SUPER_ADMIN uniquement sur tout le controller).

### 3.2 — Discriminant `organizationId` sur les agrégats métier — ✅ **livré (2026-08-10)**
- Entités : `Commune` (ancre), `Depotoir`, `MoblierUrbain`, `Circuit*` (×3), `Alert`, `Vehicle`,
  `Sensor`, `VehicleTracker`, `CollectionSchedule`, `AlertThreshold` — 12 entités, changelog
  `2.27.0_organization_id_business_entities.xml`, appliqué contre la base réelle.
- Colonne + backfill vers Pikine + `NOT NULL` dans le même changeset (pas d'expansion en deux
  temps — cohérent avec le reste du projet, base de développement assumée jetable).
- **Tous les comptes existants rattachés à Pikine** dans le même changeset (2.27.0-13) : sans cette
  ligne, activer un jour le filtre par défaut fermé aurait rendu tout invisible à tout le monde —
  aucun rattachement n'avait jamais été semé jusqu'ici (2.2.0-3, délibéré à l'époque).
- Posé côté serveur à la création dans les ~15 chemins d'écriture concernés (CRUD, import GeoJSON,
  alertes automatiques `FillLevelProjector`/`SensorSilenceProjector` qui héritent de l'organisation
  du dépotoir concerné faute d'utilisateur authentifié) — jamais depuis le DTO entrant.
- **Piège rencontré et corrigé** : Hibernate refuse deux `@FilterDef` du même nom dans une unité de
  persistance — `@FilterDef` n'est déclaré que sur `CommuneEntity`, les onze autres n'ont que `@Filter`.
- **Complexité** : L, réalisée. 295 tests, 0 échec, vérifié contre PostgreSQL réel.

### 3.3 — Filtre Hibernate par organisation — ✅ **livré (2026-08-10)**

**Décision retenue** (validée explicitement) : rattacher automatiquement tout nouveau compte à
Pikine, plutôt que de laisser les lectures du référentiel public non cloisonnées. C'est cohérent
avec la réalité mono-collectivité actuelle — la question se reposera le jour où une deuxième
collectivité existera (voir ADR-0020, non rouvert ici).

**Réalisé** :
- `shared.domain.event.UserAccountCreated` + `DefaultOrganizationEnrollmentListener` (module
  `tenant`) : tout compte créé par `AuthServiceImpl.register` ou `UserServiceImpl.createUser`
  reçoit automatiquement un `OrganizationMembership` vers Pikine, dans la même transaction — même
  patron découplé que `ActivationCodeIssued`/`PasswordResetRequested` (évite un cycle de modules,
  `tenant` dépendant déjà de `identity`).
- `tenant.infrastructure.security.TenantFilterActivationFilter` : active `organizationFilter` sur
  la session Hibernate de chaque requête, sauf pour `SUPER_ADMIN` (non filtré, supervision
  multi-collectivités). Défaut fermé par une sentinelle (`UUID` nulle) si aucune organisation n'est
  résolue — jamais un filtre désactivé, qui montrerait tout.
  **N'est pas câblé depuis `SecurityConfiguration`** (module `identity`) : cela formerait un cycle
  `identity ↔ tenant`. Le composant est un `@Component` Hibernate-filtre ordinaire, auto-enregistré
  par Spring Boot comme filtre servlet sans `@Order` explicite — il s'exécute donc après toute la
  chaîne Spring Security (enregistrée à un ordre très prioritaire), donc après `JwtFilter`.
- Tests dédiés (`TenantFilterActivationFilterTest`) : bypass SUPER_ADMIN vérifié, sentinelle de
  fermeture vérifiée, application du paramètre d'organisation vérifiée. 300 tests, 0 échec.

**Non fait, hors périmètre de cette décision** : un test bout-en-bout multi-organisations (deux
communes de deux collectivités différentes, un compte de chaque, vérification qu'aucune ne voit
l'autre) — nécessiterait une authentification `MockMvc` portée par un vrai `UserEntity` plutôt que
`@WithMockUser`, plus de scaffolding que ce lot n'en justifiait. Le comportement du composant
lui-même est vérifié unitairement ; son effet de bout en bout sur un scénario multi-collectivités
réel reste à observer le jour où une deuxième collectivité est réellement onboardée.
- `@FilterDef`/`@Filter` sur les entités du §3.2, activé après authentification (composant au même
  point d'insertion que `JwtFilter`), paramétré par `CurrentTenantProvider.currentOrganizationId()`.
- Défaut fermé : aucune organisation courante → aucune ligne visible.
- Exception explicite `SUPER_ADMIN` (filtre non activé) — **test d'intégration dédié obligatoire**
  avant de considérer ce point fait (ADR-0020 §4).
- **Complexité** : M, mais sensible — chaque requête existante qui listait sans notion de tenant doit
  être auditée une par une (ADR-0020 §Conséquences).

---

## Vue d'ensemble

| Lot | Contenu | Priorité | Complexité | Statut |
|---|---|---|---|---|
| 0 | Bugs désactivation + permissions de rôle | P0 | S | ✅ fait (2026-08-09) |
| 1 | Reset/changement mdp + catalogue permissions | P1 | S/M | ✅ fait (2026-08-09) |
| 2.1 | Vérification infra Docker | P0-A préalable | S | À faire en premier sur ce lot |
| 2.2–2.5 | Bascule Keycloak | P0-A | L | À faire après 2.1 |
| 3.1 | API Organisation minimale | P2-3 | M | ✅ fait (2026-08-09, hors de ce plan) |
| 3.2 | Discriminant `organizationId` (12 entités) | P2-3 | L | ✅ fait (2026-08-10) |
| 3.3 | Filtre Hibernate (activation) | P2-3 | M | ✅ fait (2026-08-10) |
