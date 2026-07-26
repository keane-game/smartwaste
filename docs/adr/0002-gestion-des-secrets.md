# ADR-0002 — Externalisation et rotation des secrets

- Statut : Proposé
- Date : 2026-07-11
- Priorité : P0-2

## Contexte

Des secrets sont **écrits en dur et versionnés** dans le dépôt :
- secret de signature JWT : `SecurityConstants.SECRET` (+ `ENCRIPTION_KEY` mort dans `JwtService`) ;
- mot de passe PostgreSQL : `application.properties` (`keane`) ;
- identifiants SMTP (compte Gmail personnel) ;
- clé API Google : `mobileFlutter/android/app/google-services.json`.

Il n'existe **pas de `.gitignore` racine** ; ~2 100 fichiers sont suivis. N'importe qui ayant accès au dépôt peut forger des JWT valides et se connecter à la base. Les secrets sont aussi présents dans l'historique Git.

## Décision

1. **Externaliser** toute donnée sensible via variables d'environnement, injectées par la configuration Spring :
   ```properties
   spring.datasource.password=${DB_PASSWORD}
   security.jwt.secret=${JWT_SECRET}
   spring.mail.password=${MAIL_PASSWORD}
   ```
   Le secret JWT est lu depuis la config (`@Value`/`@ConfigurationProperties`), plus depuis une constante.
2. Fournir un `.env.example` (sans valeurs) et un `application.yml` par profil (`dev`, `prod`).
3. Ajouter un **`.gitignore` racine** (`.env`, `*.local`, `target/`, `node_modules/`, `dist/`, `build/`, `/build/`, secrets).
4. **Roter** immédiatement les secrets exposés (JWT, mot de passe BD, clé Google) ; restreindre la clé Google (package + empreinte SHA).
5. Purger les secrets de l'historique (git-filter-repo / BFG) — opération sensible, à planifier avec l'équipe.

## Conséquences

- **+** Les secrets ne transitent plus par le code ; rotation possible sans recompiler.
- **+** Base saine pour un futur secret manager (Vault, AWS SM…).
- **−** Nécessite de documenter les variables requises et d'ajuster CI/CD et postes de dev.
- **−** La purge d'historique réécrit les commits → coordination requise (force-push, clones à refaire).

## Alternatives considérées

- **Laisser en dur** : rejeté (faille critique).
- **Secret manager dès maintenant** : sur-dimensionné pour l'étape actuelle ; l'externalisation par env est le prérequis et suffit à court terme.
