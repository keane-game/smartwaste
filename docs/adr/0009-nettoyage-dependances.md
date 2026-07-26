# ADR-0009 — Nettoyage des dépendances backend

- Statut : Proposé
- Date : 2026-07-11
- Priorité : P1-1

## Contexte

Le `pom.xml` accumule des dépendances redondantes et incompatibles :
- **Deux** stacks de documentation OpenAPI : `springdoc-openapi-starter-webmvc-ui` **et** `springfox-swagger-ui` / `springfox-data-rest` 3.0.0. Springfox est **abandonné** et **incompatible avec Spring Boot 3 / Jakarta** (basé sur `javax`) → risque de conflit de classpath / d'échec au démarrage.
- **Deux** bibliothèques JWT : `jjwt` (utilisée par `JwtService`) **et** Auth0 `java-jwt`.
- `spring.main.allow-circular-references=true` masque une dépendance circulaire (`JwtFilter ↔ UserService ↔ JwtService`) au lieu de la corriger.

## Décision

1. **Retirer springfox** (toutes ses dépendances) ; conserver **springdoc** comme unique source OpenAPI/Swagger.
2. **Conserver `jjwt`** (déjà utilisé) et **retirer Auth0 `java-jwt`**.
3. Supprimer le code mort associé (`ENCRIPTION_KEY`/`getKey()` dans `JwtService`, imports inutiles).
4. Résoudre la dépendance circulaire (introduire une abstraction / `@Lazy` ciblé) puis **retirer** `allow-circular-references=true`.

## Conséquences

- **+** Build plus léger, démarrage plus sûr, moins de surface de vulnérabilités.
- **+** Le graphe de dépendances Spring redevient sain (couplage réduit).
- **−** Vérifier qu'aucune annotation springfox (`@Api`, `@ApiOperation`) ne subsiste ; migrer vers les annotations `io.swagger.v3` (springdoc) le cas échéant.
- **−** Casser le cycle peut demander un léger remaniement de l'injection dans la sécurité.

## Alternatives considérées

- **Garder les deux libs OpenAPI** : rejeté (conflit Jakarta, dette).
- **Basculer tout sur springfox** : impossible (incompatible Boot 3).
