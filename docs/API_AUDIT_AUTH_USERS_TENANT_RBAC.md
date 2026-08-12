# Audit factuel — Auth / Users / Agences-Tenants / Rôles / Permissions

**Portée** : `backend-api` uniquement (`sn.smartwaste.collect`, contextes `identity` et `tenant`).
**Méthode** : lecture directe des controllers, services, DTO, entités, `SecurityConfiguration`, tests.
**Date** : 2026-08-09. Basé sur l'état du code sur `chore/backend-finalisation`, pas sur la
documentation (qui peut diverger — voir `CLAUDE.md` §Which document to trust).

> Suite donnée à cet audit : `docs/PLAN_IDENTITE_TENANT_RBAC.md` (plan de correction/implémentation),
> [ADR-0011](adr/0011-keycloak-identity-provider.md) (reprise Keycloak),
> [ADR-0020](adr/0020-cloisonnement-multi-tenant-et-api-organisation.md) (multi-tenant),
> [ADR-0021](adr/0021-completion-api-identite-pont-keycloak.md) (pont identité — corrige les bugs
> §1.1/§4 ci-dessous).

---

## 1. AUTHENTIFICATION

| Fonctionnalité | État | Endpoint | Méthode | Controller | Service | Auth | Tests | Remarques |
|---|---|---|---|---|---|---|---|---|
| Login | OUI | `/auth/authenticate` | POST | `AuthController` | `AuthServiceImpl.authentication` | Non | `AuthServiceImplTest` | Retourne `{bearer, refresh}` |
| Refresh token | OUI | `/auth/refresh` | POST | `AuthController` | `SessionServiceImpl.refresh` | Non (public) | `SessionServiceImplTest` | Refresh token **opaque** (256 bits), stocké hashé SHA-256, **rotation** à chaque appel |
| Logout | OUI | `/auth/logout` | POST | `AuthController` | `SessionServiceImpl.revoke` | Lit le Bearer si présent | Couvert via service | Idempotent, jamais d'erreur |
| Révocation (1 session) | OUI | via logout | POST | - | `SessionService.revoke(sessionId)` | - | OUI | `UserSession.revokedAt` |
| Révocation (toutes sessions user) | **PARTIEL — code mort à l'audit** | aucun endpoint alors | - | - | `revokeAllForUser` existait, jamais appelée | - | Aucun | **Corrigé par ADR-0021 §Vague 1** : branchée sur la désactivation de compte |
| Current user / profil | **Corrigé le 2026-08-10** | `GET /auth/me` | GET | `AuthController` | Oui | - | Autorisation (`AdministrationAuthorizationTest`) | Retourne le `User` (mot de passe en écriture seule, jamais renvoyé) |
| Token validation | OUI (implicite) | - | - | `JwtFilter` | - | - | - | Vérifié à chaque requête protégée |
| Token revoke (access seul) | **NON, impossible par construction** | - | - | - | - | - | - | JWT auto-porteur ; seule la session est révocable |
| Expiration session | OUI | - | - | `UserSession.isActive()` | - | - | `SessionServiceImplTest` | TTL configurables (access 10j, refresh 30j par défaut) |
| Changement mot de passe | **NON à l'audit** | aucun endpoint | - | - | - | - | - | **Traité par ADR-0021 §Vague 2** (`POST /auth/change-password`), non livré à cette date |
| Reset mot de passe | **NON à l'audit** | aucun endpoint | - | - | - | - | - | **Traité par ADR-0021 §Vague 2** (`POST /auth/password-reset/*`), non livré à cette date |
| Activation compte | OUI | `/auth/activation` | POST | `AuthController` | `AuthServiceImpl.activation` | Non (public) | - | Code 6 chiffres, `SecureRandom`, événement `ActivationCodeIssued` |
| Désactivation compte | **NON fonctionnel à l'audit (bug)** | pas d'endpoint dédié | - | `UserController.updateUser` en théorie | `UserServiceImpl.updateUser` ignorait `activated` | ADMIN/SUPER_ADMIN | - | **Corrigé le 2026-08-09** (ADR-0021 §Vague 1) : `POST /v1/users/{id}/activate|deactivate` |

**Réponses aux 13 questions posées initialement** :
1. Le backend génère lui-même le JWT (`JwtService`, HS256, JJWT).
2. Pas de Keycloak au moment de l'audit — reprise décidée depuis (ADR-0011).
3. Refresh géré par `SessionServiceImpl` + table `usersession`, token opaque hashé.
4. Oui, `/auth/refresh`. 5. Oui, `/auth/logout`. 6. Oui, au niveau session. 7. C'est la session qui
   est révoquée. 8. Oui, table `usersession`. 9. Access : claim JWT `exp` ; session : `UserSession.expiresAt`.
10. Via `SecurityContextHolder` → principal `UserEntity`, lu par `CurrentUserProvider` (interne).
11. Résolu à la demande par `CurrentTenantProviderImpl` (`OrganizationMembershipRepository.findByUserId`).
12. `UserEntity.getAuthorities()` ajoute `"ROLE_" + authority.getName()`.
13. Les `Permission` du rôle sont aussi ajoutées comme `GrantedAuthority`, utilisées via
    `@PreAuthorize`/`hasAnyRole`/`hasAuthority`.

---

## 2. USERS

| Fonctionnalité | État à l'audit | Endpoint | Remarques |
|---|---|---|---|
| Liste (complète) | OUI | `GET /v1/users/s` | ADMIN/SUPER_ADMIN |
| Liste (paginée) | OUI | `GET /v1/users` | `page`/`size` obligatoires |
| Détail | OUI | `GET /v1/users/{id}` | - |
| Création | OUI | `POST /v1/users` | Mot de passe obligatoire, haché ; `userId` client neutralisé |
| Modification | OUI, partielle | `PUT /v1/users/{id}` | Ignorait `activated` (bug, corrigé) |
| Suppression | OUI, dure | `DELETE /v1/users/{id}` | Pas de corbeille |
| Activation | PARTIEL | `/auth/activation` | Self-service uniquement |
| Désactivation | **corrigé depuis** | `POST /v1/users/{id}/deactivate` | ADR-0021 §Vague 1 |
| Recherche/filtres | **Corrigé le 2026-08-10** | `GET /v1/users?q=` | Terme cherché sur email/prénom/nom, insensible à la casse, en plus de `page`/`size` déjà existants |
| Pagination | OUI | - | Params obligatoires |
| Utilisateurs par agence | NON | - | Traité par ADR-0020 (`GET /v1/organizations/{id}/members`) |
| Affectation user → agence | NON exposé | - | Traité par ADR-0020 |

**Modèle réel** : une seule entité `UserEntity` (implémente `UserDetails`), rôle unique
(`@ManyToOne AuthorityEntity`, `nullable=false`).

---

## 3. AGENCES / ORGANISATIONS / TENANTS

Vocabulaire réel : **`Organization`** (module `tenant`), pas Agency/Institution.

**Constat à l'audit : zéro endpoint.** `OrganizationRepository`/`OrganizationMembershipRepository`
existent, aucun controller. **Depuis corrigé** (2026-08-09, en parallèle de cet audit — voir
`docs/TENANT_ORGANIZATIONS_PLAN.md`) : `OrganizationController` expose `/v1/organizations*` derrière
la permission `MANAGE_ORGANIZATIONS` (SUPER_ADMIN uniquement). Le discriminant `organizationId` sur
les agrégats métier et le filtre Hibernate restent non livrés — traités par
[ADR-0020](adr/0020-cloisonnement-multi-tenant-et-api-organisation.md) §2-4.

La chaîne `Utilisateur → Compte → Agence → Tenant → Rôles → Permissions` n'existe pas telle quelle :
Utilisateur = Compte (un seul objet) ; `UserEntity → OrganizationMembership → Organization` existe en
base (contrainte d'unicité, une organisation par utilisateur) mais était un angle mort côté API.

---

## 4. ROLES (= `AuthorityEntity`)

| Fonctionnalité | État à l'audit | Endpoint | Remarques |
|---|---|---|---|
| Liste/Détail/Création/Suppression | OUI | `/v1/authorities*` | `MANAGE_ROLE`, soft-delete |
| Modification | **PARTIEL (bug)** | `PUT /v1/authorities/{id}` | `permissions` envoyées ignorées — **corrigé le 2026-08-09** (ADR-0021 §Vague 1) |
| Attribution à un user | OUI, indirect | `PUT /v1/users/{id}` (champ `authority`) | - |
| Retrait d'un rôle | N/A structurel | - | `authorityId` non-null : on remplace, jamais on ne retire |

Risque déjà connu, non traité par ce plan (décision produit, pas un bug) : `MANAGE_ROLE` semé sur
`ADMIN` **et** `SUPER_ADMIN` — un `ADMIN` peut s'auto-élever `SUPER_ADMIN`.

---

## 5. PERMISSIONS

| Fonctionnalité | État à l'audit | Remarque |
|---|---|---|
| Catalogue exposé | **Corrigé** | `GET /v1/permissions` (ADR-0021 §Vague 2, livré le 2026-08-09) |
| Permissions d'un rôle | PARTIEL | Visible via `GET /v1/authorities/{id}` en entier |
| Attribution/retrait | **PARTIEL (bug)** | Seulement à la création — **corrigé le 2026-08-09** pour la modification |
| Vérification côté client | **Corrigé le 2026-08-10** | `GET /v1/permissions/mine` — permissions du compte authentifié, ouvert à tout authentifié (le catalogue complet, lui, reste réservé `MANAGE_ROLE`) |

Catalogue réel (`Permission` enum, 10 valeurs seedées) : `USER_VIEW, ACCESS_ADMIN, MANAGE_ROLE,
CREATE_USER, ACCESS_MY_ACTIVITIES, VIEW_COLLECTION_ROUTE, DECLARE_COLLECTION, VIEW_SUPERVISION,
MANAGE_DEVICES, SEND_AWARENESS`.

---

## 6. Système d'autorisation réel

RBAC pur, pas de Keycloak à l'audit (reprise décidée depuis), pas de groupes. Règles d'URL
(`SecurityConfiguration`, ordre significatif) + `@PreAuthorize` niveau méthode. Piège documenté dans
le code : la chaîne de filtres HTTP est évaluée **avant** la sécurité de méthode.

---

## Verdict à la date de l'audit (2026-08-09, avant correctifs)

### BACKEND API INCOMPLETE

Raisons : aucune API Agences/Organisations/Tenants malgré un modèle de données en place ;
changement/reset de mot de passe totalement absents ; désactivation de compte présente en apparence
mais inopérante (bug) ; système de permissions inutilisable pour piloter un frontend (pas de
catalogue, pas dans le JWT).

**Socle solide identifié** : login/refresh/logout/activation, CRUD utilisateur de base, CRUD rôle
(create/read/delete), RBAC vérifié par tests d'intégration (`AdministrationAuthorizationTest`).

**Suite donnée** : voir `docs/PLAN_IDENTITE_TENANT_RBAC.md` pour le détail d'exécution des trois ADR
listés en tête de ce document.

## Mise à jour du verdict (2026-08-10, après correctifs)

Tous les manques listés au verdict initial sont fermés : API `/v1/organizations*` (tenant),
changement/reset de mot de passe (`/auth/change-password`, `/auth/password-reset/*`),
désactivation de compte réparée (`POST /v1/users/{id}/deactivate`), catalogue de permissions
(`GET /v1/permissions`) et permissions du compte courant (`GET /v1/permissions/mine`). S'y ajoutent
trois gaps mineurs relevés par ce même audit et fermés dans la foulée : profil du compte authentifié
(`GET /auth/me`), recherche sur la liste des utilisateurs (`GET /v1/users?q=`), et le cloisonnement
multi-tenant réel (discriminant `organizationId` + filtre Hibernate, ADR-0020).

### BACKEND API COMPLETE (pour le périmètre audité)

Restent hors périmètre, par décision explicite et non par oubli : Keycloak (bloqué, pas de démon
Docker dans cet environnement), le multi-rôle par utilisateur (hors scope ADR-0003), et un test
bout-en-bout multi-collectivités réel pour le filtre tenant (comportement vérifié unitairement).
