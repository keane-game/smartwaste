# ADR-0011 — Keycloak comme fournisseur d'identité (OIDC)

- Statut : **Accepté sur le principe — AJOURNÉ** (2026-07-29). Reste la cible ; n'est pas le chantier courant.

> **Décision d'ajournement.** Keycloak supprimerait d'un coup l'auth maison, les secrets versionnés
> et la gestion de mot de passe, et apporterait SSO/MFA/reset. Mais son adoption **maintenant** est
> un mauvais séquencement : complexité L, trois clients à convertir au flux PKCE, une brique
> opérationnelle à héberger — et, dans l'environnement de développement actuel, **aucun moyen de
> l'exécuter** (pas de Docker), donc une configuration qu'on écrirait sans jamais la vérifier.
>
> Entre-temps, les **sessions révocables** (2026-07-28) ont fermé le défaut le plus grave : la
> déconnexion n'existait pas côté serveur. Elles sont testées et opérationnelles. C'est donc la
> solution **en vigueur**, et ce document cesse d'être en tension avec elle.
>
> **Conditions de reprise** — au premier des trois : un besoin de SSO ou de MFA ; l'extraction d'un
> second service (l'auth centralisée devient alors structurante, cf. ADR-0013) ; ou une mise en
> service réelle, qui rend la gestion des mots de passe maison inacceptable.
>
> ⚠️ Tant que Keycloak n'est pas adopté, **la rotation des secrets d'ADR-0002 reste due** : c'est
> Keycloak qui devait la rendre sans objet.
> ⚠️ **Tension à arbitrer** : le blueprint prévoit de supprimer `JwtService`, `JwtFilter` et `SecurityConstants`, alors que le chantier « sessions révocables » en cours étend précisément ces classes.
- Date : 2026-07-11
- Priorité : P0 (sécurité) — **remplace la partie « JWT + mot de passe » de [ADR-0003](0003-authentification-et-jwt.md)**

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
