# ADR-0011 — Keycloak comme fournisseur d'identité (OIDC)

- Statut : **Accepté — REPRISE DÉCIDÉE** (2026-08-09). L'ajournement du 2026-07-29 est levé ; voir
  « Décision de reprise » ci-dessous. Chantier **non commencé** : c'est une décision de cible et de
  séquencement, pas encore une bascule de code.
- Date : 2026-07-11 (reprise : 2026-08-09)
- Priorité : P0 (sécurité) — **remplace la partie « JWT + mot de passe » de [ADR-0003](0003-authentification-et-jwt.md)**

## Décision de reprise (2026-08-09)

> **Ce qui a changé depuis l'ajournement.** L'audit `docs/API_AUDIT_AUTH_USERS_TENANT_RBAC` (2026-08-09,
> voir le journal des correctifs dans `docs/PLAN_IDENTITE_TENANT_RBAC.md`) constate que l'auth maison,
> livrée seule, **n'atteint pas la complétude qu'un frontend attend** : ni changement de mot de passe,
> ni réinitialisation, ni catalogue de permissions exploitable côté client. Corriger chacun de ces
> manques dans le code maison reviendrait à reconstruire, pièce par pièce, ce que Keycloak fournit
> déjà — avec le risque de le faire moins bien (reset par email est une surface de sécurité en soi).
> Décision explicite du mainteneur : **ne pas continuer d'investir dans l'auth maison au-delà d'un
> pont minimal**, et engager la bascule Keycloak comme le chantier structurant qu'elle était déjà
> désignée être.
>
> **Ce qui n'a PAS changé, et reste un risque à porter, pas à cacher.** Le blocage opérationnel
> d'origine — « aucun moyen d'exécuter Keycloak dans l'environnement de développement actuel » —
> **est désormais confirmé, pas seulement suspecté**. Vérifié le 2026-08-09 : `docker` est introuvable
> à la fois en CLI (Bash et PowerShell) et comme service Windows dans cet environnement.
> `backend-api/src/main/resources/docker-compose.yml` déclare des services (`postgres`, `smtp4dev`,
> `minio`), mais leur existence ne dit rien de la disponibilité d'un démon pour les exécuter — ce
> que `docs/IMPLEMENTATION_LOG.md` (2026-08-06) suspectait déjà (« pas de démon Docker ici »). La
> base PostgreSQL réelle, elle, est joignable **hors Docker** (`jdbc:postgresql://localhost:5432/smartwaste`,
> confirmé le 2026-08-09) — ce blocage vise spécifiquement Keycloak, pas la persistance. **Avant
> toute bascule réelle**, un poste ou une CI équipée d'un démon Docker doit d'abord être identifié —
> ce n'est pas une tâche à refaire dans cet environnement-ci.
>
> **Séquencement retenu** (détaillé dans `docs/PLAN_IDENTITE_TENANT_RBAC.md`) :
> 1. **Pont immédiat** (ADR-0021, code livré cette session/les suivantes) : combler les manques
>    critiques de l'auth maison (reset/changement de mot de passe, désactivation de compte réparée)
>    — pour ne pas laisser le produit sans ces fonctions pendant tout le temps que prendra la bascule
>    Keycloak (chantier L, non instantané). Ce pont est **explicitement temporaire** : chaque pièce
>    posée ici (endpoint de reset, endpoint de changement de mot de passe) est un candidat direct à la
>    suppression une fois Keycloak en service (§4 ci-dessous).
> 2. **Vérification d'infrastructure** : confirmer la disponibilité d'un démon Docker, lancer
>    Keycloak localement, exécuter effectivement le blueprint `docs/keycloak-migration.md` (jusqu'ici
>    non compilé/non vérifié) avant d'écrire le moindre code d'intégration définitif.
> 3. **Bascule** : suivre `docs/keycloak-migration.md` (mis à jour en conséquence).
>
> ⚠️ Le blocage §4 déjà documenté reste vrai et n'est pas résolu par cette reprise : `JwtService`,
> `JwtFilter` et `SecurityConstants` portent aujourd'hui les **sessions révocables**, qui restent la
> solution en vigueur jusqu'à ce que le resource server Keycloak soit effectivement validé en
> environnement réel — les supprimer avant cette validation retirerait la révocation sans rien mettre
> à la place. La bascule remplace ces classes, elle ne les précède pas.
>
> ⚠️ Tant que la bascule n'est pas terminée, **la rotation des secrets d'ADR-0002 reste due** : c'est
> Keycloak qui devait la rendre sans objet, et ce n'est toujours pas fait.

### Historique — décision d'ajournement (2026-07-29, levée ci-dessus)

> Keycloak supprimerait d'un coup l'auth maison, les secrets versionnés et la gestion de mot de
> passe, et apporterait SSO/MFA/reset. Mais son adoption **immédiate** était jugée un mauvais
> séquencement : complexité L, trois clients à convertir au flux PKCE, une brique opérationnelle à
> héberger — et, dans l'environnement de développement de l'époque, aucun moyen vérifié de
> l'exécuter (pas de démon Docker), donc une configuration qu'on aurait écrite sans jamais la
> vérifier. Entre-temps, les sessions révocables (2026-07-28) avaient fermé le défaut le plus grave
> (pas de déconnexion serveur), ce qui justifiait d'attendre une des trois conditions de reprise
> (SSO/MFA, extraction d'un second service, mise en service réelle) avant de rouvrir ce chantier.
> L'audit du 2026-08-09 constate que l'écart entre l'auth maison et un frontend complet est plus
> large que prévu (§Décision de reprise ci-dessus) — c'est ce constat, pas l'une des trois conditions
> initiales au sens strict, qui motive la reprise.

## Contexte

L'authentification actuelle est **maison** et vulnérable : secret JWT versionné (`SecurityConstants`), tokens fabriqués par `JwtService`, mot de passe d'inscription codé en dur (`AuthServiceImpl`), pas de SSO/MFA/reset. Dans une cible **microservices** (ADR-0010), chaque service devra vérifier l'identité : réimplémenter cela partout serait une dette de sécurité majeure. Il faut une **autorité d'identité centralisée et standard**.

## Décision

Adopter **Keycloak** comme fournisseur d'identité **OIDC / OAuth2**.

1. **Backend = OAuth2 Resource Server** (`spring-boot-starter-oauth2-resource-server`) : il **valide** les JWT signés par Keycloak via JWKS. Il ne fabrique plus de token, ne stocke plus de mot de passe.
2. **Suppression** du code d'auth maison : `SecurityConstants`, `JwtService` (minting), `JwtFilter` custom, la gestion mot de passe de `AuthServiceImpl` → remplacés par la configuration resource server (validation issuer/audience, mapping des rôles).
3. **Gestion des comptes déléguée à Keycloak** : inscription, **activation e-mail**, réinitialisation, MFA, politiques de mot de passe. `NotificationServiceImpl` n'a plus à envoyer de codes d'activation.
4. **Rôles** : *realm/client roles* Keycloak → `GrantedAuthority` via un converter. On conserve un profil `UserEntity` **local minimal** (données métier) référencé par le **`sub` Keycloak** (identifiant stable) — l'identité et le domaine restent **découplés** (cf. ADR-0012).
5. **Clients** : Angular via *Authorization Code + PKCE* (`angular-oauth2-oidc`/`keycloak-js`) ; Flutter via *AppAuth + PKCE*. Le même token est accepté par **tous** les futurs microservices → auth centralisée du maillage.
6. Keycloak **auto-hébergé** (Docker/K8s), configuration réaliste par `realm` exporté et versionné.

## Conséquences

- **+** Élimine d'un coup les 3 failles d'auth ; SSO, MFA, reset, politiques de mot de passe « gratuits ».
- **+** Standard OIDC → prêt pour les microservices et l'intégration de clients tiers.
- **+** Plus de secret de signature dans le dépôt (clés gérées par Keycloak).
- **−** Nouvelle brique opérationnelle à héberger et sécuriser (HA, sauvegardes).
- **−** **Migration** des comptes existants vers Keycloak ; adaptation des 3 clients (web + mobile) au flux OIDC.
- **−** Dépendance de disponibilité à Keycloak (mitigée par HA + cache JWKS).

## Impact sur la ROADMAP

Remplace le travail « JWT maison » : **P0-1** (mot de passe) et le minting de **P0-3** deviennent sans objet (gérés par Keycloak). Le durcissement des erreurs (401 vs 500) devient **natif** au resource server. Nouvelle tâche P0 : « Mettre en place Keycloak + resource server ».

## Alternatives considérées

- **Garder le JWT maison (corrigé)** : rejeté — dette de sécurité, pas de SSO/MFA, à répliquer dans chaque microservice.
- **SaaS (Auth0/Cognito/Firebase Auth)** : rejeté à ce stade — coût récurrent et **souveraineté des données** (Keycloak auto-hébergeable localement).
- **Serveur OAuth2 Spring Authorization Server maison** : rejeté — réimplémente ce que Keycloak fournit clé en main (UI admin, fédération, MFA).
