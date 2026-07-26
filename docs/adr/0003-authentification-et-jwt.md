# ADR-0003 — Authentification, mot de passe et durcissement JWT

- Statut : Proposé — **partiellement remplacé par [ADR-0011](0011-keycloak-identity-provider.md)** (le minting JWT et la gestion des mots de passe passent à Keycloak ; le durcissement des erreurs 401/403 reste pertinent, désormais assuré nativement par le resource server)
- Date : 2026-07-11
- Priorité : P0-1, P0-3

## Contexte

`AuthServiceImpl.register()` ignore le mot de passe soumis et fait :
```java
String pwdCrypt = this.passwordEncoder.encode("Sonaged@123");
```
→ **tous les comptes ont le même mot de passe** → n'importe qui peut se connecter à n'importe quel compte.

Par ailleurs, le pipeline JWT est fragile :
- `JwtFilter` appelle `isTokenExpired`/`extractUsername` **sans try/catch** → un token expiré/malformé lève `ExpiredJwtException`/`JwtException` non gérée → **HTTP 500** au lieu de 401 ;
- `JwtService.generateJwt` fait `roles.get(0)` → **crash** si l'utilisateur n'a aucun rôle ;
- `JwtFilter` **journalise le token** à chaque requête (`logger.error(request.getHeader("Authorization"))`) → fuite.

Le modèle : `UserEntity implements UserDetails`, e-mail unique = identifiant, **un seul rôle** (`ManyToOne Authority`), sujet du token = e-mail.

## Décision

1. **Inscription** : hacher le mot de passe **fourni par l'utilisateur** (validé : longueur/robustesse), jamais une constante.
2. **JWT robuste** : encapsuler la validation dans un bloc qui, sur exception (expiré/invalide/signature), n'authentifie pas et laisse la chaîne répondre **401** ; retirer toute journalisation du token (au plus un id de corrélation).
3. **Rôles** : sécuriser l'accès (`isEmpty()` → 403/authorité par défaut) ; conserver le modèle **mono-rôle** actuel (cohérent avec `roles.get(0)`) et documenter que le passage multi-rôles est une évolution séparée.
4. **Durée de vie** : réduire `EXPIRATION_TIME` (actuellement 10 jours) à une valeur raisonnable (ex. 1 h) et prévoir un refresh token en P2.

## Conséquences

- **+** Authentification réellement sûre ; erreurs HTTP correctes ; logs sans secret.
- **−** Les comptes créés avec le mot de passe codé en dur doivent être réinitialisés/invalidés.
- **−** Une durée de token courte impose d'implémenter le refresh (planifié P2) pour l'UX.

## Alternatives considérées

- **Garder un mot de passe par défaut + forcer changement à la 1re connexion** : rejeté (fenêtre d'exposition, complexité inutile ; l'inscription doit simplement prendre le mot de passe de l'utilisateur).
- **Multi-rôles immédiat** : hors périmètre P0 ; introduit des changements de schéma et de token non nécessaires à la correction de sécurité.
