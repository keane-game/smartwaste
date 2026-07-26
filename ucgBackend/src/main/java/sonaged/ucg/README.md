# Squelette Spring Modulith — `sonaged.ucg`

Structure **cible** du monolithe modulaire (cf. `docs/architecture-cible.md`, ADR-0010/0011/0012).
**Additif** : n'altère pas l'application existante `sonaged.collecte.master` (qui reste l'app en cours d'exécution).

## Modules
| Package | Module | État |
|---|---|---|
| `identiteacces` | Identité & Accès (Keycloak + profils) | à peupler |
| `referentiel` | Référentiel Territorial | à peupler |
| `collecte` | Gestion des Collectes (points, alertes, circuits) | à peupler |
| `ingestioniot` | Ingestion IoT | ⚠️ **RÉSERVÉ — non implémenté, module inerte** |
| `communication` | Avis + notifications (e-mail/SSE/push) | à peupler |
| `supervision` | Dashboard / stats / historique / audit | à peupler |
| `shared` | Shared kernel (module ouvert) | à peupler |

`UcgModulith` est l'**ancre d'analyse** (annotée `@Modulithic`) — pas une application Spring Boot, pas de `main`.

## Vérifier / documenter
`maven-surefire-plugin` a `skipTests=true` ; lancer explicitement :
```bash
./mvnw test -DskipTests=false -Dtest=UcgModularityTests
```
- `verifiesModularStructure` → vérifie l'absence de cycles et le respect des frontières.
- `writesDocumentation` → génère les diagrammes sous `target/spring-modulith-docs/`.

## Migration (P1-7)
Déplacer progressivement le code de `sonaged.collecte.master` vers ces modules, en remplaçant les
associations JPA **cross-module** par des références par identifiant (ADR-0012). L'IoT (`ingestioniot`)
ne se peuple qu'une fois la fonctionnalité décidée (ROADMAP P0-5/P0-6).
