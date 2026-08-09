# TENANT_ORGANIZATIONS_PLAN.md — Gestion des organisations (SUPER_ADMIN)

> ⚠️ **Superseded le 2026-08-09** par `docs/adr/0020-cloisonnement-multi-tenant-et-api-organisation.md`
> (§5), écrit et accepté par une session concurrente pendant la rédaction de ce document — même
> périmètre, contrat légèrement différent (un seul `PUT` statut+nom au lieu de verbes
> `suspend`/`reactivate` dédiés, pas de suppression, lecture ouverte à l'ADMIN de sa propre
> collectivité). **`OrganizationController` est implémenté selon ADR-0020, pas selon la section 2
> ci-dessous** — cette section reste comme trace de la proposition initiale, ADR-0020 fait foi en
> cas de désaccord. Testé : `OrganizationControllerTest` (9 cas), suite complète 283/283.

## 1. Ce qui existe réellement côté backend (vérifié dans le code, 2026-08-09)

Contexte `tenant` (`sn.smartwaste.collect.tenant`) — fondations uniquement, ADR-0013 §4 :
- **`Organization`** : `organizationId`, `name`, `code` (unique), `status`
  (`ACTIVE`\|`SUSPENDED`), soft-delete complet (`SoftDeleteRepository` — corbeille/restauration
  déjà génériques via `/v1/deletions` une fois l'API posée, pas besoin de la réinventer).
- **`OrganizationMembership`** : `userId` ↔ `organizationId`, **un seul rattachement par
  utilisateur aujourd'hui** (contrainte unique sur `userId` — passer au multi-collectivités par
  personne ne demandera qu'à lever cette contrainte, pas une réécriture).
- **`CurrentTenantProvider`** : résout la collectivité de l'utilisateur courant, déjà utilisable
  par n'importe quel service qui voudrait cloisonner — mais **rien ne l'utilise encore**.
- **1 collectivité semée** : « Ville de Pikine » (`PIKINE`). **0 rattachement semé** — aucun
  utilisateur, y compris l'admin, n'est aujourd'hui rattaché à une organisation.
- **Aucun agrégat métier ne porte de `tenantId`** (dépotoirs, alertes, etc. restent globaux) —
  décision volontairement différée, complexité XL, à trancher agrégat par agrégat plus tard.
- **Zéro contrôleur REST.** Ni lecture, ni écriture, ni rattachement — rien n'est appelable.

**Conséquence pour ce plan** : tant que l'API n'existe pas, cette section reste une spécification.
Et tant que `tenantId` n'est posé sur aucun agrégat métier, l'écran décrit ici gère la **relation**
utilisateur↔organisation, pas un vrai cloisonnement des données Pikine par collectivité — ce sera
une évolution séparée (P2-3), pas une conséquence automatique de cet écran.

## 2. Contrat API proposé (à valider avant écriture, cohérent avec les patrons déjà en place)

Modèle le plus proche dans le code actuel : `AuthorityController` (`/v1/authorities`) — CRUD
simple, `@PreAuthorize` de classe sur une permission dédiée plutôt qu'un rôle générique.

### `OrganizationController` — proposé : `/v1/organizations`
Permission proposée : **nouvelle valeur d'enum `MANAGE_ORGANIZATIONS`**, réservée `SUPER_ADMIN`
uniquement — **pas** `ADMIN`. Justification : un `ADMIN` opère *pour le compte d'* une
collectivité (via son rattachement), il n'a pas de raison légitime de créer ou d'administrer
d'*autres* collectivités — c'est une capacité de plateforme, un cran au-dessus de
l'administration d'une seule collectivité. À la différence de `MANAGE_ROLE`
(délibérément donné à `ADMIN` aussi), ce n'est pas cohérent de la partager.

| Méthode + chemin | Requête | Réponse | Notes |
|---|---|---|---|
| `GET /v1/organizations` | — | `List<OrganizationDto>` | Toutes les collectivités, actives et suspendues |
| `GET /v1/organizations/{id}` | — | `OrganizationDto` avec le compte de membres | |
| `POST /v1/organizations` | `{name, code}` | `OrganizationDto`, 201 | `status` forcé à `ACTIVE` côté serveur, jamais fourni par le client |
| `PUT /v1/organizations/{id}` | `{name}` | `OrganizationDto` | `code` immuable après création (clé d'intégration stable, même logique que le `code` d'`Authority`) |
| `POST /v1/organizations/{id}/suspend` | — | `OrganizationDto`, 204 | Verbe dédié plutôt qu'un `PATCH status` générique — même raison que `AlertThreshold.deactivate` : un changement de statut est un acte métier, pas juste une écriture de champ |
| `POST /v1/organizations/{id}/reactivate` | — | `OrganizationDto`, 204 | |
| `DELETE /v1/organizations/{id}` | — | 204 | Soft-delete (le repository le supporte déjà) — passe par le mécanisme générique `/v1/deletions/organization/**` une fois posé, pas un endpoint dédié |

### Rattachement — proposé : sous `/v1/organizations/{id}/members` (pas un contrôleur séparé)
| Méthode + chemin | Requête | Réponse | Notes |
|---|---|---|---|
| `GET /v1/organizations/{id}/members` | — | `List<MembershipDto{membershipId, userId, userFullName, userEmail}>` | Jointure légère vers l'identité pour l'affichage — **par identifiant**, pas par association JPA (ADR-0012), donc un appel serveur à `CurrentUserProvider`/`UserRepository` en interne, pas un port nouveau |
| `POST /v1/organizations/{id}/members` | `{userId}` | `MembershipDto`, 201 | 409 si l'utilisateur a déjà un rattachement (contrainte unique actuelle) |
| `DELETE /v1/organizations/{id}/members/{membershipId}` | — | 204 | Détache, ne supprime pas l'utilisateur |

## 3. Écrans frontend proposés (SUPER_ADMIN uniquement — absent du menu ADMIN)

Positionnement dans la nouvelle navigation par rôle (Phase 2, app shell) : section distincte
**« Plateforme »**, séparée de la navigation opérationnelle (dépotoirs, alertes, tournées…) — un
SUPER_ADMIN gère la plateforme, un ADMIN gère sa collectivité, la distinction doit être visible
dans le menu, pas seulement dans les permissions.

1. **Liste des organisations** (`/plateforme/organisations`)
   Tableau : nom, code, statut (badge ACTIVE vert / SUSPENDED gris), nombre de membres. Action
   rapide : suspendre/réactiver directement depuis la ligne (avec confirmation — c'est un acte qui
   coupe l'accès à tous les membres rattachés).
2. **Créer une organisation** — formulaire modal ou page dédiée : nom, code (validation : format
   court, unicité vérifiée côté serveur, message clair si le code existe déjà).
3. **Détail organisation** (`/plateforme/organisations/{id}`)
   - En-tête : nom, code, statut, date de création.
   - Section « Membres » : liste des utilisateurs rattachés (nom, email, rôle) + action « Rattacher
     un utilisateur » (recherche par email parmi les comptes existants, pas de création de compte
     depuis cet écran — la gestion des comptes reste dans `/v1/users`).
   - Détacher un membre : confirmation, effet immédiat sur son accès si/quand le cloisonnement par
     `tenantId` existera — **à ce stade, effet purement déclaratif**, à documenter clairement dans
     l'UI (« ce rattachement n'affecte pas encore l'accès aux données ») pour ne pas laisser croire
     à un cloisonnement qui n'existe pas.
4. **Suspendre/réactiver** — confirmation renforcée (comme la modification de rôle vers
   SUPER_ADMIN) : action qui affecte tous les membres rattachés d'un coup.

## 4. Explicitement hors scope de cet écran

- Cloisonnement réel des données métier par collectivité (P2-3, XL) — cet écran gère la relation,
  pas le filtrage.
- Facturation/abonnement — l'entité `Organization` l'exclut délibérément (voir commentaire de
  classe), aucun champ à prévoir côté UI non plus.
- Multi-rattachement (un utilisateur dans plusieurs organisations) — la contrainte backend actuelle
  l'interdit ; l'écran de rattachement doit donc désactiver/expliquer l'action si l'utilisateur
  ciblé a déjà un rattachement, pas échouer silencieusement sur le 409.

## 5. Prochaine étape

Ce document sert de base à valider avant d'écrire quoi que ce soit. Une fois validé : (1) backend —
`Permission.MANAGE_ORGANIZATIONS` + `OrganizationController` + tests, sur le patron exact
d'`AuthorityController` ; (2) frontend — les 3 écrans ci-dessus, une fois l'API réellement
appelable.
