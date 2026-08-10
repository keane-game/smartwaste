# ADR-0021 — Complétion de l'API Identité & Accès (pont avant Keycloak)

- Statut : **Accepté — implémenté (vagues 1 et 2)**, 2026-08-09. Vague 1 (corrections de bugs) et
  vague 2 (`change-password`, `password-reset/*`, `GET /v1/permissions`) livrées le même jour.
  Changelog `2.26.0_password_reset_token.xml` appliqué contre la base réelle
  (`jdbc:postgresql://localhost:5432/smartwaste`, joignable dans cet environnement — contrairement
  au blocage Docker qui vise spécifiquement Keycloak, ADR-0011). Suite complète : 295 tests, 0 échec.
- Date : 2026-08-09
- Priorité : P0 (vague 1) / P1 (vague 2)

## Contexte

[ADR-0011](0011-keycloak-identity-provider.md) reprend la bascule vers Keycloak, mais c'est un
chantier L : vérification d'infrastructure, conversion de trois clients, migration des comptes.
L'audit `docs/API_AUDIT_AUTH_USERS_TENANT_RBAC` (2026-08-09) relève entre-temps des manques et un
bug que le produit ne peut pas se permettre d'attendre :

- **Aucun changement ni réinitialisation de mot de passe** — un utilisateur qui oublie son mot de
  passe n'a aucun recours.
- **La désactivation de compte ne fonctionne pas** : le champ `activated` existe (`UserEntity`, DTO
  `User`), mais `UserServiceImpl.updateUser` ne le recopie jamais depuis le DTO reçu — un bug, pas un
  choix délibéré (contrairement au retrait volontaire de `userId`/`authority` ailleurs dans la même
  classe, qui, eux, sont commentés).
- **`SessionService.revokeAllForUser` est du code mort** : implémenté, testé au niveau service, mais
  n'a aucun appelant.
- **`AuthorityServiceImpl.updateAuthority` ignore les `permissions` envoyées** : seul `name` est mis à
  jour, silencieusement — un administrateur qui modifie les droits d'un rôle existant depuis son
  formulaire n'a aucun effet, sans erreur pour le signaler.
- **Aucun catalogue de permissions exposé** : l'énumération `Permission` n'existe que côté code.

Corriger ces points dans l'auth maison **n'est pas un investissement perdu même si Keycloak arrive** :
la bascule est un chantier de plusieurs semaines a minima (vérification Docker, PKCE sur trois
clients, migration de comptes) ; laisser un bug de désactivation ouvert et aucun moyen de reset
pendant tout ce temps n'est pas acceptable. Chaque pièce posée ici est explicitement un **pont**,
pas une extension pérenne de l'auth maison.

## Décision

### Vague 1 — corrections de bugs (livrée avec cet ADR, aucun nouveau endpoint)

1. **Désactivation/réactivation de compte.** Nouveaux endpoints dédiés plutôt qu'un champ silencieux
   dans `PUT /v1/users/{id}` (cohérent avec le style déjà en usage : `VehicleController.deactivate`,
   `DeviceProvisioningController.deactivateSensor`) :
   ```
   POST /v1/users/{id}/activate     — ADMIN/SUPER_ADMIN
   POST /v1/users/{id}/deactivate   — ADMIN/SUPER_ADMIN
   ```
   La désactivation appelle **immédiatement** `SessionService.revokeAllForUser` — un compte désactivé
   dont les sessions ouvertes restent valides jusqu'à expiration serait une désactivation incomplète.
   C'est le premier appelant réel de cette méthode, qui cesse d'être du code mort.

2. **Permissions d'un rôle réellement modifiables.** `AuthorityServiceImpl.updateAuthority` applique
   désormais `permissions` quand le corps de la requête en fournit, au même titre que `name`.

### Vague 2 — nouveaux endpoints — ✅ livrée le 2026-08-09

3. **Changement de mot de passe (self-service, authentifié).**
   ```
   POST /auth/change-password   { currentPassword, newPassword }
   ```
   Vérifie `currentPassword` contre le hash existant avant d'accepter `newPassword` — un jeton
   d'accès volé ne suffit pas à lui seul à prendre le compte définitivement. Révoque **toutes** les
   sessions de l'utilisateur, y compris celle de l'appel courant : un changement de mot de passe
   déconnecte partout, l'utilisateur se reconnecte avec le nouveau. Plus simple et plus sûr qu'une
   exception « sauf la session courante », et cohérent avec le comportement déjà documenté de
   `revokeAllForUser`.

4. **Réinitialisation de mot de passe (mot de passe oublié, non authentifié).**
   ```
   POST /auth/password-reset/request   { email }   — toujours 204, que l'email existe ou non
   POST /auth/password-reset/confirm   { token, newPassword }
   ```
   **Table dédiée, pas une réutilisation de `Validation`.** `Validation` sert aujourd'hui
   l'activation de compte avec une durée de vie de ~972 jours (valeur historique conservée,
   `ValidationServiceImpl`) — adaptée à un e-mail qu'on peut ouvrir n'importe quand après
   inscription, inadaptée à un secret de réinitialisation qui doit expirer vite (retenu : **1h**,
   `sonaged.security.password-reset.ttl-minutes`). Mélanger les deux dans la même table sous un
   même espace de codes créerait un risque de confusion fonctionnelle (un code d'activation ne doit
   jamais pouvoir réinitialiser un mot de passe, et réciproquement) pour économiser une table. Un
   `PasswordResetToken` séparé (même schéma que `UserSession` : secret aléatoire 256 bits, stocké
   **hashé**, à usage unique, `expiresAt`) élimine le risque par construction plutôt que par
   discipline de code.
   `request` répond toujours 204 sans jamais indiquer si l'email est connu — même raisonnement que
   `SessionServiceImpl.refresh` (« distinguer inconnu de expiré renseignerait un attaquant »).
   `confirm` invalide le token à l'usage (succès ou échec de validation du mot de passe) et révoque
   toutes les sessions existantes de l'utilisateur, comme le changement de mot de passe.

5. **Catalogue de permissions.**
   ```
   GET /v1/permissions   — ADMIN/SUPER_ADMIN (même garde que /v1/authorities)
   ```
   Retourne les valeurs de l'enum `Permission` — lecture seule, aucune donnée sensible, sert
   uniquement à construire un écran d'édition de rôle côté frontend sans dupliquer l'énumération
   côté client.

### Explicitement hors périmètre de cet ADR

- **Multi-rôle par utilisateur** : question déjà tranchée hors-périmètre par ADR-0003 §Alternatives,
  non rouverte ici.
- **API tenant/organisation** : traitée par [ADR-0020](0020-cloisonnement-multi-tenant-et-api-organisation.md).
- **Tout code Keycloak** : traité par [ADR-0011](0011-keycloak-identity-provider.md).

### Ce que la bascule Keycloak rendra caduc

Le §Vague 2 est un pont : `POST /auth/change-password`, `POST /auth/password-reset/*` et l'activation
existante (`POST /auth/activation`) sont **candidats directs à la suppression** une fois le resource
server Keycloak validé (ADR-0011 §Décision de reprise, étape 3) — Keycloak gère nativement ces trois
flux. Ne pas sur-investir ici (pas de politique de complexité de mot de passe avancée, pas de
verrouillage après N échecs) : ce serait dupliquer un effort que Keycloak fournit clé en main pour un
code voué à disparaître.

## Conséquences

- **+** Ferme un bug de sécurité opérationnelle réel (désactivation inopérante) sans attendre la
  bascule Keycloak.
- **+** `revokeAllForUser` cesse d'être du code mort, et le devient à un endroit où son usage est
  évident (désactivation, changement/reset de mot de passe).
- **+** Un administrateur peut enfin faire ce que l'écran de gestion des rôles laisse croire possible
  (modifier les permissions).
- **−** La vague 2 ajoute une table (`PasswordResetToken`) et un canal e-mail transactionnel de plus
  (déjà en place pour l'activation via `smtp4dev`/`ActivationCodeIssued`) — surface de code
  explicitement temporaire, à retirer avec discipline plutôt qu'à laisser traîner après la bascule
  Keycloak.
- **−** Réserver `GET /v1/permissions` à ADMIN/SUPER_ADMIN plutôt que de l'ouvrir à tout authentifié
  est un choix conservateur : un rôle de portée plus large qui aurait besoin de connaître le
  catalogue (ex. pour griser un bouton côté UI) devra le redemander explicitement plutôt que
  d'hériter d'un accès large par défaut.

## Alternatives considérées

- **Ajouter `activated` à la liste des champs recopiés par `PUT /v1/users/{id}`** au lieu d'un
  endpoint dédié : rejeté — mélangerait un changement d'état sensible (qui doit révoquer les
  sessions) avec une mise à jour de champs de confort (téléphone, adresse), au risque qu'un futur
  appel omette la révocation en pensant faire une simple mise à jour de profil.
- **Réutiliser `Validation` pour le reset de mot de passe avec un champ `purpose`** : rejeté — voir
  §4, le risque de confusion entre deux espaces de codes de nature différente (durée de vie, usage)
  l'emporte sur l'économie d'une table.
- **Attendre Keycloak pour tout traiter d'un coup** : rejeté — le délai (vérification Docker, PKCE
  sur trois clients, migration de comptes) laisserait un bug de désactivation et une absence de reset
  en production le temps du chantier, ce qui n'est pas acceptable pour un produit qui vise une mise
  en service réelle.
