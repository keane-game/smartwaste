# ADR-0020 — Cloisonnement multi-tenant complet et API Organisation

- Statut : **Accepté — implémenté (XL)**. Opérationnalise le chapitre « Multi-tenant »
  d'[ADR-0008](0008-strategie-fetch-et-multitenant.md), resté au stade des fondations depuis
  2026-07-11. Détaillé en tâches dans `docs/PLAN_IDENTITE_TENANT_RBAC.md`.
  **§2-5 livrés le 2026-08-10** : discriminant `organizationId` sur 12 entités (changelog `2.27.0`),
  API `/v1/organizations*`, et filtre Hibernate activé (`TenantFilterActivationFilter`), avec
  rattachement automatique de tout nouveau compte à Pikine (`UserAccountCreated` →
  `DefaultOrganizationEnrollmentListener`) pour que le défaut fermé du filtre ne vide pas le
  référentiel pour un habitant qui vient de s'inscrire. 300 tests, 0 échec, vérifié contre
  PostgreSQL réel.
- Date : 2026-08-09
- Priorité : P2-3 (ROADMAP.md) — reclassé **structurant** par cette décision, plus seulement « à
  cadrer tôt, implémenter plus tard »

## Contexte

L'audit `docs/API_AUDIT_AUTH_USERS_TENANT_RBAC` (2026-08-09) constate que le module `tenant`
(`Organization`, `OrganizationMembership`, `CurrentTenantProvider`) existe en base et en code
depuis ADR-0008 mais :

- **n'a aucune surface API** — aucun controller, ni pour administrer les collectivités (`Organization`),
  ni pour rattacher un utilisateur à l'une d'elles (`OrganizationMembership`) ;
- **ne filtre aucune donnée** — `organizationId` n'est posé sur aucun agrégat métier ; deux
  collectivités partageraient aujourd'hui exactement les mêmes dépotoirs, alertes, véhicules et
  capteurs si une seconde était onboardée, sans qu'aucun mécanisme ne les distingue.

`CurrentTenantProvider.currentOrganizationId()` résout donc une information que **rien ne consomme
encore**. Le produit vise plusieurs collectivités (CLAUDE.md, cible produit) ; cet écart devient
bloquant dès qu'une deuxième ville est envisagée, pas seulement une amélioration de confort.

## Décision

### 1. Unité de cloisonnement : l'`Organization` est une collectivité, pas un simple label

Une `Organization` (ex. « Ville de Pikine ») possède un sous-ensemble du référentiel territorial —
concrètement, un ensemble de `Commune`. Une `Commune` appartient à **exactement une** organisation ;
tout ce qui est rattaché à une commune (quartiers, dépotoirs, circuits, véhicules, capteurs, alertes)
en hérite. C'est cohérent avec le domaine réel : deux collectivités sénégalaises ne se partagent pas
une commune.

### 2. Colonne dénormalisée, pas une dérivation par jointure

Un filtre Hibernate (`@FilterDef`/`@Filter`, activé par session) ne peut pas résoudre efficacement
« l'organisation du dépotoir » en remontant `Depotoir → Quartier → Commune → Organization` à chaque
ligne. Conformément à ADR-0008 (« colonne `organizationId` sur les entités métier, filtrée par un
filtre Hibernate »), **chaque entité soumise au cloisonnement porte sa propre colonne
`organizationId`**, posée à la création et jamais recalculée par jointure à la lecture.

**Entités concernées** (première vague, `waste` + `iot`) :
`Commune` (ancre — voir §1), `Depotoir`, `MoblierUrbain`, les trois `Circuit*` (Collecte, Balayage,
Pré-collecte), `Alert`, `Vehicle`, `Sensor`, `VehicleTracker`, `CollectionSchedule`,
`AlertThreshold`.

**Hors périmètre pour cette vague** : `Quartier`, `Region`, `Department` restent sans colonne
propre — leur usage applicatif passe systématiquement par leur `Commune` parente (ADR-0018), qui
porte déjà le discriminant ; ajouter la colonne partout où elle est dérivable sans jamais être filtrée
directement serait de la donnée qui peut diverger de sa source sans qu'on le remarque.

### 3. L'organisation vient du contexte de la requête, jamais du corps

Même règle que celle déjà appliquée à `userId` (`AuthServiceImpl.register`) et à `authority`
(élévation de privilège fermée) : `organizationId` est posé côté serveur, à la création, à partir de
`CurrentTenantProvider.currentOrganizationId()` — **jamais lu depuis le DTO entrant**. Un utilisateur
sans organisation courante (compte non rattaché) ne peut créer aucune ressource cloisonnée ; c'est un
409/403 explicite, pas une valeur par défaut devinée.

### 4. Filtrage à la lecture : Hibernate `@Filter`, activé après authentification

Un filtre de session, activé par un composant équivalent à `JwtFilter` (même point d'insertion dans
la chaîne, après résolution de l'utilisateur courant), pose le paramètre `organizationId` sur toutes
les entités listées au §2. Défaut : **fermé** — une requête dont l'utilisateur n'a pas d'organisation
courante ne voit aucune ligne cloisonnée plutôt que de les voir toutes (même logique de refus par
défaut que `SecurityConfiguration`, §« Écriture : refusée par défaut »).

**Exception explicite : `SUPER_ADMIN` voit toutes les organisations.** Le rôle distingue déjà
l'exploitant de la plateforme (SONAGED) de l'administration d'une collectivité (`ADMIN`, scopé à son
organisation). Le filtre n'est pas activé pour `SUPER_ADMIN` — un exploitant multi-collectivités doit
pouvoir superviser l'ensemble sans changer de compte. Ce point mérite sa propre vérification
d'autorisation (test d'intégration dédié, sur le modèle d'`AdministrationAuthorizationTest`) tant il
inverse la règle générale.

### 5. API `tenant` — implémentée (2026-08-09, `OrganizationController`)

```
GET    /v1/organizations                       — lister les collectivités (actives et suspendues)
GET    /v1/organizations/{id}                  — détail
POST   /v1/organizations                       — créer (statut forcé ACTIVE côté serveur)
PUT    /v1/organizations/{id}                  — renommer (code immuable après création)
POST   /v1/organizations/{id}/suspend          — suspendre (coupe l'accès des membres rattachés)
POST   /v1/organizations/{id}/reactivate       — réactiver
DELETE /v1/organizations/{id}                  — suppression logique (corbeille /v1/deletions)
GET    /v1/organizations/{id}/members          — lister les membres
POST   /v1/organizations/{id}/members          — rattacher un utilisateur (409 si déjà rattaché)
DELETE /v1/organizations/{id}/members/{membershipId} — détacher
```

**Garde réellement posée : `@PreAuthorize("hasAuthority('MANAGE_ORGANIZATIONS')")`** — une permission
dédiée plutôt que `hasAnyRole('SUPER_ADMIN')` en dur, seedée uniquement sur `SUPER_ADMIN`
(changelog `2.25.0_manage_organizations_permission.xml`), cohérente avec le modèle rôle→permissions
déjà en vigueur pour `MANAGE_ROLE` (`AuthorityController`). C'est un choix plus fin que celui
esquissé au brouillon de cet ADR (`hasAnyRole`) : une permission peut être retirée d'un rôle sans
livraison, un rôle en dur dans `@PreAuthorize` non. **Adopté tel quel** — pas de raison de revenir à
un rôle en dur.

**Non livré par ce même travail, à ne pas confondre** : la lecture/gestion des membres par un `ADMIN`
de sa propre organisation (contrôle applicatif décrit plus bas) n'existe pas encore — `MANAGE_ORGANIZATIONS`
est actuellement la seule garde, réservée SUPER_ADMIN pour l'ensemble du controller. Onboarder une
collectivité reste, comme prévu, un geste plateforme : c'est SONAGED qui contractualise avec une
nouvelle ville, pas la ville elle-même.

### 6. Contrainte actuelle « un utilisateur, une organisation » : conservée

`OrganizationMembership` porte déjà une contrainte d'unicité sur `userId` (voir le code : commentaire
explicite sur le choix). Cette décision ne la lève pas — un utilisateur intervenant sur plusieurs
collectivités reste hors périmètre, à instruire séparément si le besoin apparaît (lever la contrainte
suffira alors, la table est déjà conçue pour).

### 7. Indépendance vis-à-vis de la bascule Keycloak (ADR-0011)

`OrganizationMembership.userId` référence l'identifiant **local** (`UserEntity.userId`), pas un
identifiant Keycloak. Le blueprint Keycloak (`docs/keycloak-migration.md` §5) prévoit justement de
conserver un `UserEntity` profil local, résolu depuis le `sub` du token — cette table n'a donc
**rien à changer** quand la bascule aura lieu. Les deux chantiers (ADR-0011, ADR-0020) sont
séquençables dans n'importe quel ordre relatif ; ni l'un ne bloque l'autre.

## Conséquences

- **+** Onboarder une deuxième collectivité devient une opération de données (créer l'`Organization`,
  rattacher ses communes et ses utilisateurs), plus une refonte de schéma.
- **+** Le filtre par défaut fermé empêche par construction la fuite de données déjà identifiée comme
  risque dans ADR-0008 (« toutes les données sont globales »).
- **−** Migration de données non triviale : chaque table listée au §2 reçoit une colonne
  `nullable=false` à terme, ce qui impose un changeset en deux temps (colonne nullable + backfill vers
  une organisation de démarrage « Ville de Pikine », puis `NOT NULL`) — même logique que les autres
  colonnes non-nullables ajoutées après coup dans ce projet.
- **−** Toute requête qui listait jusqu'ici sans notion de tenant (dashboards, exports, carte) doit
  être auditée une par une pour confirmer qu'elle passe bien par une session filtrée — un oubli serait
  silencieux, comme le rappelle `SecurityConfiguration` sur un tout autre risque.
- **−** `SUPER_ADMIN` non filtré est une exception large : elle doit être testée explicitement
  (§4) pour ne jamais devenir, par erreur de configuration future, le comportement par défaut de
  `ADMIN`.

## Alternatives considérées

- **Filtrer par jointure vers `Commune` à la lecture, sans dénormaliser** : rejeté — plus simple à
  tenir cohérent (une seule source de vérité), mais incompatible avec un `@Filter` Hibernate
  performant sur des listes, et ADR-0008 avait déjà tranché pour la colonne directe.
- **Base ou schéma séparé par collectivité** : rejeté, comme dans ADR-0008 — trop lourd à exploiter
  pour de nombreuses petites collectivités comparé au discriminant partagé.
- **Autoriser un `ADMIN` à créer sa propre organisation (self-service)** : rejeté — l'onboarding
  d'une collectivité est un engagement contractuel de la plateforme, pas un geste utilisateur ;
  laisser n'importe quel `ADMIN` en créer une casserait le modèle « SONAGED opère, les collectivités
  utilisent ».
- **Lever tout de suite la contrainte un-utilisateur-une-organisation** : rejeté — aucun besoin
  observé aujourd'hui ; l'ajouter par anticipation ajouterait de la complexité (quelle organisation
  « courante » choisir pour un utilisateur qui en a plusieurs ?) sans cas d'usage réel à valider
  contre.
