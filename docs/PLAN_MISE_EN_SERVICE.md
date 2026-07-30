# Plan de mise en service — besoins effectifs du système

> Établi le **2026-07-30**, au lendemain du premier démarrage réel contre PostgreSQL.
> Décisions associées : [ADR-0014](adr/0014-amorcage-du-systeme.md) (amorçage),
> [ADR-0015](adr/0015-referentiel-geographique-projection-et-perimetre.md) (référentiel géographique).
> Ce plan ne remplace pas `../ROADMAP.md` (le plan de reprise, P0→P2). Il répond à une autre
> question : **que manque-t-il pour que ce qui est déjà construit fonctionne réellement ?**

## Le constat qui motive ce plan

Jusqu'au 2026-07-30, l'application n'avait **jamais démarré** contre PostgreSQL. Le build était vert,
les 140 tests passaient, le contexte Spring se câblait sur H2 — et `ddl-auto: validate` n'avait
jamais confronté les entités au schéma Liquibase. Ce démarrage a eu lieu : il a échoué sur quatre
colonnes manquantes (changelog `2.10.0`), puis a réussi.

L'état constaté immédiatement après est le suivant :

| Table | Lignes |
|---|---|
| `authority`, `authoritypermission`, `organization`, `alertthreshold` | 3, 9, 1, 1 |
| `users` | **0** |
| `region`, `departement`, `commune`, `quartier`, `geometry`, `typedepotoir` | **0** |
| `depotoir`, `circuitcollect`, `circuitbalayage`, `alert`, `measurement` | **0** |

Le moteur de seuils, les read-models de carte, la priorisation des tournées, le flux SSE, la
supervision, le signalement citoyen : tout cela est écrit et testé, et **tout tourne à vide**. Le
besoin effectif du système n'est plus d'ajouter des fonctions, c'est de lui donner de quoi
fonctionner.

Les besoins ci-dessous sont ordonnés par **dépendance stricte** : chacun est bloqué par le précédent.

---

## B1 · Une racine territoriale

**Manque.** `region = 0`. Le changelog `2.0.0_territory_uuid` purge le référentiel pour convertir les
identifiants en `uuid` ; `data/region.sql`, qui semait une région en `BIGINT`, n'a jamais été rejoué
sous la nouvelle forme — alors que les rôles, eux, l'ont été (`2.1.0-7`).

**Conséquence.** `UploadFileServiceImpl:60` renvoie `"Region not found"` et **toute la chaîne d'import
s'arrête en cascade, sans erreur** : département, commune, quartier, circuits, dépotoirs. L'import ne
échoue pas, il ne fait rien.

**Travail.** Un changeset Liquibase semant la région Dakar, identifiant UUID fixe de forme v7,
précondition d'idempotence, `rollback` explicite — sur le modèle de `2.1.0-7`, `2.2.0-3`, `2.8.0-3`.

**Vérification.** `select count(*) from region` = 1 après démarrage ; deux démarrages successifs ne
créent pas deux régions.

---

## B2 · Un compte administrable

**Manque.** `users = 0`. Le changeset `2.1.0-3` vide la table avant conversion ; rien ne l'a
repeuplée. Aucun chemin ne mène à un compte `ADMIN` : `/auth/register` est public mais impose
`DEFAULT_REGISTRATION_ROLE = "USER"`, et l'activation passe par un code envoyé par courriel.

**Conséquence.** Personne ne peut se connecter. Toute la surface d'administration durcie le
2026-07-30 est inatteignable — y compris `POST /v1/admin/import/geojson`, donc B3 par la voie HTTP.

**Travail.** `AdminBootstrapRunner` (module `administration`) selon [ADR-0014](adr/0014-amorcage-du-systeme.md) :
identifiants lus dans `SONAGED_ADMIN_EMAIL` / `SONAGED_ADMIN_PASSWORD`, **aucune valeur par défaut**,
idempotent, compte créé activé en `SUPER_ADMIN`, mot de passe haché par le `BCryptPasswordEncoder` de
l'application. Absence de configuration → aucune action + avertissement nommant les variables
attendues. Interrupteur `sonaged.bootstrap.admin.enabled`.

**Vérification.** Sans variables : démarrage normal, avertissement au journal, `users = 0`. Avec
variables : le compte existe, `POST /auth/` retourne un jeton, `GET /v1/users/s` répond 200. Second
démarrage : le mot de passe n'est pas réécrit.

---

## B3 · Des coordonnées exploitables

**Manque.** Les 13 fichiers de `datas/` déclarent `spatialReference: {wkid: 32628}` — UTM zone 28N,
en mètres. L'import stocke `x` dans `latitude` et `y` dans `longitude`
(`UploadFileServiceImpl:194-196` → `ImportedFeature.GeoPoint(latitude, longitude)` →
`TerritoryImportAdapter:195`). Deux erreurs superposées : l'unité et l'ordre des axes. Aucune
bibliothèque de projection n'est présente dans le projet.

**Conséquence.** Chaque point importé se retrouverait à « latitude 239 504, longitude 1 631 116 » —
hors de la Terre pour tout consommateur WGS84 : carte web, application Flutter, read-models
`DepotoirMaps` / `DepartmentMaps`. Le défaut n'a jamais été visible parce que l'import n'a jamais
tourné.

**Travail.** `CoordinateProjector` (module `administration`) selon
[ADR-0015](adr/0015-referentiel-geographique-projection-et-perimetre.md) : conversion
EPSG:32628 → EPSG:4326 à l'import, rétablissement de l'ordre des axes, seul composant du code
connaissant une projection. `GeometryEntity.spatialReference` conserve le `wkid` d'origine.

**Vérification.** Points de contrôle connus convertis avec une tolérance documentée ; après import,
toute latitude est dans `[14.6, 14.9]` et toute longitude dans `[-17.5, -17.1]` — l'emprise de
Pikine. Une assertion de plage vaut ici mieux qu'une comparaison exacte : elle échoue bruyamment si
la projection saute.

---

## B4 · Le référentiel réel chargé

**Manque.** Rien n'est importé. L'import lit 6 des 13 fichiers ; les 6 autres fichiers de points
(112 entrées) ne sont dans aucune liste, alors qu'ils portent **le même schéma d'attributs** que
`depotoir.json` et sont déjà discriminés par `Type_de_Mo`.

**Complication.** Les fichiers se recouvrent : **132 entrées pour 71 coordonnées distinctes**, dont 49
présentes dans plusieurs fichiers. Et les libellés de commune divergent (`Diamagueune` /
`Diamaguene` / `Diamagueune Sicap Mbao`, `Guinaw rail` / `Guinaw rails`, `Pikine EST` / `Pikine Est`),
alors que le rattachement se fait par correspondance de nom.

**Travail.** Étendre la liste des fichiers de points au chemin d'import existant (`importDepotoir`,
qui type déjà via `resolveOrCreateType`) ; dédoublonner sur la coordonnée arrondie + type ;
rapprocher les communes sur nom normalisé ; rapporter explicitement ignorés et non rattachés.
`pre_collecte.json` reste hors périmètre (schéma distinct). `MoblierUrbain` n'est pas retenu comme
cible — statuer sur son retrait est une suppression de code, **validation requise**.

**Vérification.** 71 dépôts distincts, aucun doublon de coordonnée, rapport d'import listant les
ignorés et les points sans commune ; un second import ne crée rien.

---

## B5 · Une vérification d'exécution réelle

**Manque.** Aucun chemin fonctionnel n'a jamais été exercé contre PostgreSQL avec des données. Le
2026-07-30 prouve que l'application **démarre** ; il ne prouve rien sur ce qu'elle fait ensuite. Toute
la validation du projet repose sur des tests unitaires, un contexte H2 et un schéma dérivé des
entités — trois choses qui, par construction, ne pouvaient pas révéler B1 à B4.

**Travail.** Un parcours de fumée documenté, exécuté dans cet ordre : connexion → lecture du
référentiel (`/v1/communes/s`, `/v1/depotoirs/s`) → read-models de carte → tableau de bord et
supervision → dépôt d'une mesure (`POST /v1/measurements`) → franchissement de seuil → alerte créée →
réception sur le flux SSE. Chaque étape consigne ce qui a réellement répondu.

**Vérification.** Le parcours va jusqu'au bout, ou l'endroit exact où il casse est nommé. C'est le
premier test de bout en bout de l'histoire du projet ; il est probable qu'il trouve quelque chose.

---

## Ce que ce plan ne traite pas

Décisions ouvertes, hors de ce périmètre, à ne pas confondre avec les besoins ci-dessus :

- **Rotation des secrets et purge d'historique** ([ADR-0002](adr/0002-gestion-des-secrets.md) §4-5) —
  jamais faites. Le mot de passe de base, le secret JWT et la clé Google restent dans l'historique
  Git. Le défaut `${DB_PASSWORD:keqne}` réintroduit dans `application.yml` va dans le sens inverse de
  cette dette.
- **Intégration continue** (P2-2) — aucun workflow. Les 140 tests ne tournent que localement, et le
  test de correspondance schéma↔entités exige une base PostgreSQL dans le pipeline.
- **Consolidation des frontends** (P1-4, [ADR-0006](adr/0006-consolidation-frontend.md) dont la
  prémisse est invalidée) — suppression de dépôts, **validation explicite requise**.
- **Keycloak** ([ADR-0011](adr/0011-keycloak-identity-provider.md)) — non implémenté. B2 est conçu
  pour s'effacer devant lui par simple configuration.
- **`pre_collecte.json`** — 6 entrées de schéma distinct, cadrage propre à faire.
