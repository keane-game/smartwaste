# ADR-0014 — Amorçage du système : racine territoriale et compte d'administration

- Statut : **Proposé**
- Date : 2026-07-30
- Priorité : P0 — bloquant pour l'exploitation ; prérequis de [ADR-0015](0015-referentiel-geographique-projection-et-perimetre.md)
- Complète [ADR-0001](0001-gestion-du-schema-liquibase.md) (Liquibase source unique) et [ADR-0002](0002-gestion-des-secrets.md) (aucun secret versionné)

## Contexte

Le 2026-07-30, l'application a démarré **pour la première fois** contre PostgreSQL (`ddl-auto: validate` a enfin confronté les entités au schéma Liquibase, révélant quatre colonnes manquantes, corrigées par le changelog `2.10.0`). L'état de la base a alors pu être constaté pour ce qu'il est :

| Table | Lignes |
|---|---|
| `authority`, `authoritypermission`, `organization`, `alertthreshold` | 3, 9, 1, 1 — **semées** |
| `users` | **0** |
| `region`, `departement`, `commune`, `quartier`, `geometry`, `typedepotoir` | **0** |
| `depotoir`, `circuitcollect`, `circuitbalayage`, `alert`, `measurement` | **0** |

Le système démarre, expose son API, valide son schéma — et **ne peut être utilisé par personne, sur aucune donnée**. Deux causes indépendantes, toutes deux héritées des migrations UUID :

**1. Aucun compte n'existe.** Le changeset `2.1.0-3` vide la table (`<delete tableName="users"/>`) avant de convertir `userid` en `uuid`. Le seed d'origine (`data/user.sql`, identifiants `BIGINT`, exécuté par `1.0.0`) n'a jamais été rejoué sous forme UUID, alors que les rôles, eux, l'ont été (`2.1.0-7-seed-authorities`). Il n'existe donc **aucun chemin vers un compte `ADMIN`** :
- `/auth/register` est public mais impose `DEFAULT_REGISTRATION_ROLE = "USER"` (`AuthServiceImpl:41`) — délibérément, puisque le rôle arrivait auparavant du corps de la requête ;
- l'activation exige un code envoyé par courriel (`ValidationService`) ;
- toute la surface d'administration durcie le 2026-07-30 (`hasAnyRole(ADMIN, SUPER_ADMIN)`) est par conséquent **inatteignable**, y compris `POST /v1/admin/import/geojson`.

**2. Aucune racine territoriale.** Le changelog `2.0.0_territory_uuid` est destructif (purge du référentiel, `BIGINT` → `uuid`) ; `data/region.sql` avait semé une région sous l'ancien format et rien ne l'a réintroduite. Or l'import du référentiel commence par :

```java
// UploadFileServiceImpl:59-63
public String uploadDataDepartment(MultipartFile file) {
    if (!territory.hasRegion()) {
        return "Region not found";
    }
    ...
}
```

Sans région, l'import du département renvoie `"Region not found"`, et la chaîne entière — commune, quartier, circuits, dépotoirs — s'arrête en cascade **sans erreur** : chaque étape se contente d'un message dans la map de résultat. L'import n'échoue pas, il ne fait rien.

Ces deux manques sont de nature différente : l'un est une **donnée de référence** (la région existe objectivement, son identifiant doit être stable d'un environnement à l'autre), l'autre est un **secret** (un mot de passe d'administration).

## Décision

Traiter les deux séparément, selon la nature de ce qui est amorcé.

### 1. La racine territoriale est semée par Liquibase

Un changeset ajoute la région **Dakar** avec un identifiant fixe, sur le modèle exact des seeds déjà en place (`2.1.0-7` pour les rôles, `2.2.0-3` pour l'organisation, `2.8.0-3` pour le seuil par défaut) : UUID littéral respectant la forme v7 (nibble de version = 7, variante = `0b10`) pour ne pas fausser l'ordonnancement des index, précondition `sqlCheck` d'idempotence, `rollback` explicite.

**Motif** : une donnée de référence n'est pas un secret. Son identifiant doit être **reconnaissable et identique partout** — c'est ce qui permet à un import, à un jeu de tests ou à un environnement de recette de s'y référer sans découverte préalable. C'est déjà la convention du projet ; s'en écarter ici créerait une exception sans raison.

### 2. Le compte d'administration est amorcé par l'application, à partir de l'environnement

Un `ApplicationRunner` dédié (`AdminBootstrapRunner`, module `administration` — le module non-contexte des opérations d'exploitation) crée le compte au démarrage, avec ce contrat :

- **Les identifiants viennent de l'environnement** : `SONAGED_ADMIN_EMAIL`, `SONAGED_ADMIN_PASSWORD`. Aucune valeur par défaut, aucun mot de passe ni empreinte dans le dépôt.
- **Absence de configuration = aucune action**, et un avertissement explicite au journal indiquant que le système restera sans compte administrable. Le démarrage n'échoue pas : un environnement peut légitimement gérer ses comptes autrement (recette restaurée, Keycloak à terme).
- **Idempotent** : si un compte porte déjà cette adresse, le runner ne fait rien — il ne réécrit pas le mot de passe, ne réactive pas un compte désactivé, ne réattribue pas le rôle. Un amorçage ne doit pas pouvoir servir de porte dérobée de réinitialisation.
- Le compte est créé **activé** (`activated = true`), avec le rôle `SUPER_ADMIN`, et le mot de passe haché par le `BCryptPasswordEncoder` de l'application — le même que `AuthServiceImpl.register`.
- Désactivable par `sonaged.bootstrap.admin.enabled=false`, sur le modèle de `sonaged.import.geojson.on-startup`.

**Motif du choix « runner » contre « changeset Liquibase »** — un compte semé par changelog supposerait de committer une **empreinte bcrypt d'un mot de passe connu**. Ce serait :
- un secret versionné de plus, dans un dépôt dont [ADR-0002](0002-gestion-des-secrets.md) §4-5 constate que la rotation et la purge d'historique **n'ont jamais été faites** — on aggraverait précisément la dette qu'on n'a pas encore payée ;
- **irrévocable en pratique** : la somme de contrôle d'un changeset appliqué est figée, on ne corrige pas un changeset, on en ajoute un autre (leçon du changelog `2.10.0`) ;
- **partagé par tous les déploiements** issus du même dépôt — le défaut exact que `UserServiceImpl.createUser` a mis des mois à perdre avec son `Sonaged@123` codé en dur.

Le runner évite les trois : le secret ne transite que par l'environnement, il est rotable comme n'importe quel mot de passe, et il diffère par déploiement.

## Conséquences

- **+** Le système devient utilisable : un compte permet de se connecter, donc d'atteindre la surface d'administration, donc de déclencher l'import par l'API.
- **+** Aucun secret nouveau dans le dépôt — cohérent avec ADR-0002, et compatible en l'état avec un déploiement réel.
- **+** L'amorçage du compte est **neutre vis-à-vis d'ADR-0011** : le jour où Keycloak devient le fournisseur d'identité, on désactive le runner par configuration au lieu de démêler un seed de changelog.
- **−** Un développeur doit poser deux variables d'environnement avant son premier démarrage, sans quoi il obtient un système sans compte. Le message d'avertissement doit donc nommer les variables attendues, pas seulement signaler l'absence.
- **−** Deux mécanismes d'amorçage coexistent (Liquibase pour les données de référence, runner pour le compte). La frontière doit rester lisible : **donnée de référence → changelog ; secret → environnement**.
- **−** Le runner écrit en base au démarrage, donc après Liquibase et hors de son suivi : cette création n'apparaît pas dans `databasechangelog`. Assumé — c'est le prix de ne pas versionner le secret.

## Alternatives considérées

- **Semer le compte dans un changeset Liquibase** (avec empreinte bcrypt) : rejeté — secret versionné, non rotable, partagé entre déploiements. Détaillé ci-dessus.
- **Élever le premier compte inscrit au rang d'administrateur** : rejeté — `/auth/register` est public ; la règle transformerait une course à l'inscription en prise de contrôle, sur une API déjà exposée.
- **Procédure manuelle documentée** (`INSERT` SQL à la main, ou `UPDATE` du rôle après inscription) : rejeté comme mécanisme principal — non reproductible, non testable, et l'expérience du projet est que ce qui n'est pas exécuté par le code n'est pas exécuté du tout. Reste acceptable comme dépannage ponctuel.
- **Ne rien amorcer et attendre Keycloak (ADR-0011)** : rejeté — ADR-0011 est marqué **non implémenté** et suppose une infrastructure absente ; le système resterait inutilisable pour une durée indéterminée.
