# KNOWLEDGE_MAP.md — Cartographie documentaire exhaustive

> Reconstruction complète de la connaissance du projet à partir de **toutes** les sources du dépôt,
> suivies récursivement jusqu'à épuisement des références.
> Date : 2026-07-28 · Aucun développement effectué pendant cette phase.
> Sert de base au plan d'évolution — à lire avant `ROADMAP.md`.
>
> ⚠️ **Mise à jour 2026-08-06** : `docs/IMPLEMENTATION_LOG.md` a continué bien après cette date
> (jusqu'au 2026-08-06) et invalide plusieurs constats ci-dessous, en particulier tout le §4.2/§7.1
> et la ligne ADR-0004 du §3.1 — la chaîne capteur→mesure→seuil→alerte **est** implémentée et
> vérifiée en base depuis le 2026-08-04. Corrections ponctuelles apportées inline ; pour tout ce qui
> n'est pas explicitement corrigé ici, `IMPLEMENTATION_LOG.md` reste la source qui prime sur ce
> document en cas de désaccord.

---

## 0. Préalable : l'état réel de la « mémoire persistante »

**Ce projet n'a aucune mémoire persistante enregistrée.** Le répertoire attendu
(`~/.claude/projects/-mnt-c-…-master-ucg/memory/`) **n'existe pas** : on n'y trouve que des
transcriptions brutes de sessions (`*.jsonl`), qui ne sont pas de la connaissance curée. Le seul
répertoire `memory/` présent sur la machine appartient à un **autre projet** (`sama-card`) et n'a
pas été consulté.

**Conséquence directe** : toute la connaissance ci-dessous provient du dépôt. Ce qui n'a jamais été
écrit dans un fichier — arbitrages oraux, intentions, raisons d'un abandon — est **perdu**. C'est le
premier axe d'amélioration (§9).

---

## 1. Cartographie des documents consultés

### 1.1 Documents de pilotage (racine)
| Document | Date | Rôle | Fiabilité |
|---|---|---|---|
| `CLAUDE.md` | — | Instructions agent, conventions | 🔴 **périmé** (§8.1) |
| `README.md` | ~2026-07-26 | Porte d'entrée, démarrage, points sensibles | 🟢 bon, 2 écarts mineurs |
| `PROJECT_STATUS.md` | 2026-07-11 | Photographie de l'état des fonctionnalités | 🔴 **très périmé** (§8.2) |
| `PROJECT_ANALYSIS.md` | 2026-07-11 | Analyse de reprise, risques R1–R8 | 🟠 partiellement périmé |
| `ROADMAP.md` | ~2026-07-11+ | Plan priorisé P0/P1/P2 | 🟠 numérotation en conflit (§8.3) |

### 1.2 Documentation technique (`docs/`)
| Document | Rôle | Fiabilité |
|---|---|---|
| `docs/IMPLEMENTATION_LOG.md` | Journal d'implémentation daté, très détaillé | 🟢 **la source la plus fiable** |
| `docs/architecture-cible.md` | Découpage cible en modules | 🔴 **contredit l'ADR accepté** (§8.4) |
| `docs/keycloak-migration.md` | Blueprint P0-A, non exécuté | 🟢 cohérent, non appliqué |
| `docs/adr/README.md` | Index des 13 ADR | 🟠 statuts faux (§8.5) |
| `docs/adr/0001…0013` | 13 décisions d'architecture | voir §3 |
| `backend-api/endpoint.md` | Liste des endpoints, régénérée en P2-4 | 🟠 re-périmé depuis (§8.6) |

### 1.3 Sources métier (racine) — **la vraie spécification**
| Document | Contenu |
|---|---|
| `Gestion automatisée … UCG.txt` (1 221 l.) | **Mémoire de master complet** — extraction texte du `.docx`/`.pdf` homonymes. Acteurs, besoins fonctionnels/non fonctionnels, cas d'usage, diagrammes UML, architecture matérielle IoT, choix des composants, perspectives |
| `GESTION DE DECHET IoT.txt` (40 l.) | Notes de terrain : modèle Kenya/IBM Nairobi, **66 circuits de collecte**, balayage matin/soir/nuit, **1 superviseur = 25 agents**, 500 m par balayeur, mobilier urbain = points propres + bacs de rue. Contacts : M. Diop (dir. cartographie), Mansour Diallo (assistant technique) |
| `Formulaire d'enquête … .csv` / `.txt` (34 réponses) | **Enquête citoyenne** — voir §4.3 |
| `Presentation-memoire-master.pptx` | Soutenance (non extrait — binaire) |
| `GESTION DE DECHET.docx` | Notes métier (binaire) |

### 1.4 Données
`datas/` — **13 fichiers GeoJSON réels de Pikine** (et non 4 comme le laisse entendre `CLAUDE.md`) :
`QUARTIERS` (1,9 Mo), `circuit_balay`, `circuit_collect`, `commune`,
`LIMITE DELEGATION DEPARTEMENTALE DE PIKINE`, `depotoir`, `depotoir2`, `bac_rue`, `point_pp`,
`pp_pnr_pp-pnr`, `ppef_cp`, `CAISSES POLYBENNE`, `pre_collecte`.

### 1.5 Code et configuration lus comme documentation
`backend-api/pom.xml`, `application.yml`, `application-keycloak.properties`, `docker-compose.yml`
(smtp4dev + MinIO), `schema.sql` (⚠️ `DROP DATABASE`, neutralisé), `config/liquibase/master.xml`
+ 8 changelogs, `.gitignore`, `.env.example`, `angular|sonaged_web|ucgFrontend/package.json`,
`mobileFlutter/pubspec.yaml`, `.claude/skills/`.

### 1.6 Références **non atteignables** depuis cet environnement
Signalées pour honnêteté, elles peuvent contenir de la connaissance non couverte ici :
- **GitHub Issues / PR / Discussions / Projects** de `github.com/keane-kane/master-ucg` — `gh` n'est pas installé.
- **Figma** — maquettes citées (mémoire, fig. 42-44), aucun lien dans le dépôt.
- **Trello** — cité comme outil de gestion (mémoire §3.2.1), aucun lien.
- **diagrams.net / Fritzing** — diagrammes et schémas de montage cités, **sources absentes** du dépôt (seulement des images dans le `.docx`).
- Bibliographie du mémoire : ~33 liens externes (ucg.gouv.sn, ResearchGate, ARCEP…) — contexte académique, sans impact sur le code.

---

## 2. Relations entre documents (graphe)

```
                    Mémoire de master (.txt/.docx/.pdf)  ◄── LA spécification
                    Notes IoT terrain · Enquête citoyenne
                                    │ (analysés en 2026-07-11 par)
                                    ▼
                        PROJECT_ANALYSIS.md ──► risques R1..R8
                          │            │
                          ▼            ▼
                  PROJECT_STATUS.md   ROADMAP.md ──► P0/P1/P2
                          │            │  │
                          └────────────┘  └──► docs/adr/0001..0013
                                                │        │
                                architecture-cible.md ◄──┘ (ADR-0010, REMPLACÉ)
                                keycloak-migration.md ◄──── ADR-0011
                                                │
                                                ▼
                                    docs/IMPLEMENTATION_LOG.md  ◄── ce qui est VRAIMENT fait
                                                │
                                                ▼
                                    README.md · endpoint.md · CLAUDE.md
```

**Lecture du graphe** : la chaîne descendante est saine (spécification → analyse → plan → décisions),
mais la **remontée** ne se fait pas. `IMPLEMENTATION_LOG.md` enregistre fidèlement le réel ; ni les
statuts d'ADR, ni `PROJECT_STATUS.md`, ni `CLAUDE.md`, ni `architecture-cible.md` ne sont mis à jour
en retour. **Tous les écarts du §8 découlent de cette unique boucle manquante.**

---

## 3. Décisions d'architecture retrouvées

### 3.1 Les 13 ADR
| ADR | Décision | Statut déclaré | Réalité constatée |
|---|---|---|---|
| 0001 | Liquibase = source unique du schéma, JPA `validate` | Proposé | ✅ **fait** |
| 0002 | Externaliser + roter les secrets, `.gitignore` racine | Proposé | 🟠 externalisé, `.gitignore` créé — **rotation et purge d'historique NON faites** |
| 0003 | Hacher le mdp soumis · durcir JWT · TTL 1 h + refresh | Proposé, part. remplacé par 0011 | 🟠 `register` corrigé, JWT durci ; **TTL toujours à 10 j** ; refresh en cours |
| 0004 | Chaîne d'ingestion IoT : `Measurement` → seuil → alerte | Proposé | ✅ **implémenté et vérifié en base depuis 2026-08-04** (corrigé 2026-08-06 — voir §4.2) ; `FillLevelProjector` évalue remplissage, température **et** humidité indépendamment via `AlertThreshold` (par type ou seuil par défaut) — le manque signalé plus bas (§7.1, ancienne version) sur température/humidité est déjà comblé, aucun ADR-0004bis nécessaire |
| 0005 | `Alert → Depotoir` + images hors BLOB (MinIO) | Proposé | 🟠 MinIO fait ; **`Alert.depotoirId` : colonne créée, relation non exploitée** |
| 0006 | `angular/` = front canonique, retirer les 2 autres | Proposé | ❌ non tranché — **et la prémisse est fausse** (§8.7) |
| 0007 | Notifications temps réel SSE (+ FCM mobile) | Proposé | ✅ SSE fait · ❌ FCM non fait |
| 0008 | LAZY par défaut, pas de cascade cross-contexte, multi-tenant par discriminant | Proposé | ✅ LAZY/cascades faits · 🟠 multi-tenant : **fondations posées, discriminant non appliqué** |
| 0009 | Retirer springfox + auth0-jwt + code mort + refs circulaires | Proposé | 🟠 springfox ✅, auth0 ✅, code mort ✅ (2026-07-28) — **`allow-circular-references: true` toujours actif** |
| 0010 | Monolithe modulaire en 5 modules sous `sonaged.ucg` | ⚠️ **Remplacé** | ⛔ abandonné |
| 0011 | Keycloak OIDC, backend = Resource Server | Proposé | ❌ non fait (blueprint prêt, dépendance ajoutée mais inerte) |
| 0012 | Références par identifiant entre contextes | Proposé | ✅ **fait** |
| 0013 | DDD `sn.smartwaste.collect`, 8 contextes, Clean Architecture, SaaS | **Accepté** | 🟢 **en cours, très avancé** |

### 3.2 Décisions structurantes prises hors ADR (dans le journal)
- **UUID v7** pour les identifiants (générateur maison, sans dépendance) — motif : localité d'insertion en index B-tree.
- **Module `administration`** (3ᵉ non-contexte, après `shared` et `config`) pour la corbeille et l'import.
- **Ports applicatifs plutôt que repositories exposés** (`WasteReadModel`, `CurrentUserProvider`, `TerritoryReadModel`, `AlertStreamMetrics`, `CurrentTenantProvider`).
- **Rattachement utilisateur↔organisation porté par `tenant`**, pas par `identity`.
- **Soft-delete généralisé** + purge planifiée à 30 j + corbeille REST.
> Aucune de ces décisions n'a d'ADR. Elles ne vivent que dans `IMPLEMENTATION_LOG.md`.

---

## 4. Fonctionnalités identifiées

### 4.1 Implémentées et vérifiées
Authentification JWT + inscription + activation e-mail · CRUD utilisateurs/rôles · CRUD des ~9 ressources
métier (dépotoirs, types, mobilier urbain, circuits ×3, alertes, images, historique) · référentiel
territorial complet (région→département→commune→quartier + géométries) · cartographie
(`/v1/maps/**`) · tableau de bord + statistiques de supervision · upload d'images MinIO · import
GeoJSON · avis citoyens (géolocalisés, statut piloté) · **SSE temps réel des alertes** · soft-delete
+ corbeille + purge · écrans CRUD génériques Angular · écrans mobiles (auth, dashboard, carte,
live tracking).
**Ajouté depuis 2026-08-04, non listé ici avant le 2026-08-06** : ingestion de mesures capteur +
seuil + alerte automatique (§4.2), provisioning/santé des capteurs et véhicules, suivi de position
véhicule, journal par point de collecte, tournées agent priorisées par état réel, rapports de
performance (export CSV), messages de sensibilisation programmés par quartier, permissions
réellement appliquées (plus de `SecurityRule` mortes), notifications push par appareil (transport
loggué, pas encore de FCM réel).

### 4.2 Le cœur métier — ~~absent~~ **implémenté (corrigé 2026-08-06)**
La chaîne **capteur → mesure → seuil → alerte automatique → notification** — raison d'être du
produit, énoncée dans le titre même du mémoire (« mise en place d'un système d'alerte d'un point
de collecte ») — **existe et a été vérifiée contre PostgreSQL** (`docs/IMPLEMENTATION_LOG.md`,
2026-08-04/05) : `iot.Sensor`/`Measurement`, `waste.FillLevelProjector`/`ThresholdResolver`
déclenchent une `Alert` liée au `Depotoir` sur dépassement de seuil, avec résolution et journal par
point (`PointJournalController`). `Alert` n'est plus une ressource saisie à la main uniquement.
Ce qui reste hors périmètre d'ADR-0004 : les seuils **température/humidité** que le mémoire
demande également (§7.1) ne sont pas évalués, seul le remplissage l'est.

### 4.3 Ce que dit l'enquête citoyenne (34 réponses) — **jamais traduit en backlog**
| Question | Résultat |
|---|---|
| Pour un **système d'alerte pour sortir vos ordures** | **~29/34 OUI** |
| Pour un **bac à ordures modernisé** | ~31/34 OUI |
| Triez-vous vos ordures ? | **~32/34 NON** |
| Problèmes cités | « les voitures ne passent pas souvent », « pas de point de collecte dans ma zone » |
| Klaxon des camions | dérange une large majorité |

**Le besoin n°1 exprimé par les citoyens est l'inverse de celui que la ROADMAP prépare** : le backlog
construit l'alerte *bac plein → superviseur* ; les habitants demandent *camion imminent → citoyen*
(et le klaxon, qui remplit ce rôle aujourd'hui, les dérange). **Aucune ligne de la ROADMAP ne couvre
ce cas.** Voir §7.

---

## 5. Idées en attente (formulées, non planifiées)

- **Optimisation des tournées** — citée dans l'objectif produit, `PROJECT_STATUS` P1-4 et
  `architecture-cible` (`Tournee`, futur). Aucun algorithme, aucune entité.
- **Multi-tenant complet** (discriminant `organizationId` sur les agrégats) — ADR-0008/P2-3, XL.
- **Bus partagé Redis** pour le SSE multi-instances — ADR-0007, conditionné à la montée en charge.
- **Broker MQTT/Kafka** — anticipé par le design événementiel (ADR-0004/0010), non introduit.
- **Rapports de performance** — cas d'usage administrateur du mémoire, `/v1/reports` dans `architecture-cible`.
- **Perspectives du mémoire** : capteurs de remplissage plus fins, **compactage des déchets**, GPRS
  pour zones sans Internet, **capteurs olfactifs**, **IA de détection de présence humaine** à l'ouverture.
- **Multi-rôles par utilisateur** — explicitement repoussé par ADR-0003 (modèle mono-rôle conservé).

---

## 6. Éléments abandonnés

| Élément | Statut | Trace |
|---|---|---|
| **ADR-0010** — 5 modules `sonaged.ucg`, découpage par couches | Remplacé par ADR-0013 | ADR-0010 conservé pour historique |
| **`sonaged.ucg.*`** — échafaudage de modules (12 fichiers marqueurs) | Mort depuis ADR-0013 | Encore présent + `UcgModularityTests` qui le teste à vide |
| **`ucgFrontend/`** — scaffold Angular 16 | Mort | 71 fichiers, 5 720 l. conservés |
| **`displayPicture byte[]`** (BLOB en table ALERT) | Remplacé par MinIO | ADR-0005 |
| **`schema.sql`** (`DROP DATABASE`) | Neutralisé, jamais réactivable | `spring.sql.init.mode: never` |
| **`table utilisateur`** + `avis.utilisateur_id` | Vestige d'un modèle antérieur | Toujours dans le baseline Liquibase |
| **`DataNotifierAspect`** | Mort de fait | Pointcut sur `com.worldline.tapandgo.user.annotations.Notifiable` — **projet étranger** |
| **Enum `Permission`** — ~20 valeurs (`ACCESS_PRODUCT`, `DISTRIBUTE_PRODUCT`, `ACCESS_TERMINAL_INFO`, `VALIDATE_PAYMENT_MEAN`…) | Sans rapport avec les déchets | Même origine `worldline/tapandgo` |
| **Beans `SecurityRule`** (13 règles d'autorisation) | Jamais appliqués | `configure()` n'est appelé par personne |
| **`HistoryEntity`** | Coquille vide (`@Id` seul) | Traîne DTO, mapper, repository, service, contrôleur |
| **Restes de template Flutter** | Mort | `spoonacular`, `product_model`, `app_env.dart` « Q Flutter TDD » |
| **`SonagedApplicationTests`** | Redondant depuis `ApplicationContextLoadsTest` | `@Disabled` |

> Aucun de ces éléments n'a été supprimé : la règle projet impose une validation explicite.

---

## 7. Fonctionnalités prévues mais non implémentées

### 7.1 Chaîne IoT — spécifiée en détail, absente à 100 %
Le mémoire (ch. 4) décrit le **matériel choisi** : Arduino UNO/Mega, **ESP8266** (WiFi),
**HC-SR04 ×2** (niveau de remplissage + détection de présence), **NEO-6M** (GPS),
**LoRa RYLR998**, **DHT11** (température/humidité), servomoteur (ouverture automatique).
Communication hybride **LoRa + WiFi (+5G)**, protocoles **TCP/UDP/MQTT**, **passerelle IoT**
agrégeant vers le cloud.

~~Côté backend, **rien** : ni `Capteur`, ni `Measurement`, ni endpoint d'ingestion, ni évaluateur de
seuil, ni `fillLevel` sur `Depotoir`.~~ — **corrigé 2026-08-06** : tout ceci existe désormais
(`iot.Sensor`, `iot.Measurement`, `MeasurementIngestionController`, `ThresholdResolver`,
`Depotoir.fillLevel`/`lastMeasuredAt`), voir §4.2. Ce qui manque encore du matériel décrit ci-dessus
reste réel : GPS embarqué sur les bacs (géométrie toujours statique), ouverture automatique du bac,
et LoRa/passerelle IoT (l'ingestion actuelle est REST + clé API device, pas LoRa/MQTT).

> ✅ **Écart de périmètre comblé (corrigé 2026-08-06)** : le cas d'usage administrateur du mémoire
> demande de « configurer les seuils de **température, d'humidité** et de niveau de remplissage ».
> Une lecture précédente de ce document disait qu'ADR-0004 ne modélisait que le remplissage — faux
> à la vérification du code : `AlertThreshold` porte les trois grandeurs (`fillLevelPercent`,
> `temperatureCelsius`, `humidityPercent`, chacune configurable par `TypeDepotoir` ou en seuil par
> défaut, `null` = grandeur non surveillée), et `FillLevelProjector.on(MeasurementRecorded)` les
> évalue **indépendamment** (un bac peut déborder et fermenter, ce sont deux alertes distinctes,
> `AlertCode.DANGER` pour température/humidité). Aucun ADR-0004bis n'est nécessaire.

### 7.2 Autres fonctionnalités spécifiées et manquantes
| Fonctionnalité | Source | État |
|---|---|---|
| **Alerte citoyen « sortez vos ordures »** / horaires de collecte par zone | Enquête + cas d'usage citoyen | ❌ absent **et non planifié** |
| Signalement de dépôt sauvage par le citoyen | Cas d'usage citoyen | 🟠 `Avis` existe, sans géolocalisation ni photo ni statut de traitement |
| Localisation des **véhicules** de collecte | Besoin « Localisation » du mémoire | ❌ (écran mobile « live tracking » sans backend) |
| Notifications de **sensibilisation** | Besoin « Notifications et Alertes » | ❌ |
| Push mobile **FCM** | ADR-0007 | ❌ |
| Ouverture automatique du bac (servomoteur) | Mémoire §4.2.3 | ❌ (hors backend, mais partie de la promesse) |
| GPS embarqué sur les bacs | Mémoire §4.2.4 | ❌ (géométrie statique importée par GeoJSON) |
| Optimisation des tournées | Objectif produit | ❌ |
| Rapports de performance | Cas d'usage administrateur | ❌ |
| `GET /v1/me` | `architecture-cible` | ❌ |
| Keycloak (SSO/MFA/reset) | ADR-0011 | ❌ blueprint seul |
| CI/CD | P2-2 | ✅ validé en local, jamais exécuté sur GitHub (branche non poussée) |
| Dockerfile applicatif | P2-2 | ✅ (`backend-api/Dockerfile`, multi-étage, Actuator `/actuator/health` seul ouvert) |
| Conteneur PostgreSQL en dev | P2-2 | ❌ (`docker-compose.yml` ne fournit que smtp4dev et MinIO ; Postgres reste une install locale sur `:5433`) |

### 7.3 Rôles métier non modélisés
Le mémoire définit **3 acteurs** (Administrateur, **Agent de collecte**, **Citoyen**) et le terrain en
ajoute (superviseur de circuit, contrôleur de balayage). Le backend ne connaît que
`SUPER_ADMIN / ADMIN / USER`. Ni l'agent, ni le citoyen, ni le superviseur n'existent comme rôle —
alors que les circuits, eux, sont modélisés.

---

## 8. Incohérences entre documents

### 8.1 🔴 `CLAUDE.md` est périmé sur des points qui orientent le travail
| Affirmation | Réalité |
|---|---|
| « Java 17, Spring Boot 3.2.4 » | **Java 21, Boot 3.5.3** |
| « Angular 16 » (`angular/`) | **Angular 17.0.7** |
| « no root `.gitignore` » | **Il existe** |
| « `AuthServiceImpl.register` hardcodes `Sonaged@123` » | **Corrigé** — subsiste dans `UserServiceImpl.createUser` |
| « `MapsController` serves `dto/maps` » | Déplacé vers `analytics` + `*.application.api` |
| Architecture « layered `controller → service → …` » | Vrai pour le legacy résiduel ; **le reste est en DDD/Clean Architecture** |
| « ~20 entities forming the geo/collection hierarchy » | Vrai, mais réparties en 6 contextes bornés |

### 8.2 🔴 `PROJECT_STATUS.md` (2026-07-11) décrit un état largement dépassé
Y sont encore listés comme à faire ou cassés : `.gitignore` absent, `ddl-auto=update`, springfox,
token loggué, 500 sur JWT expiré, README vide, `endpoint.md` erroné, import GeoJSON absent —
**tous résolus**. Le document se présente pourtant comme « la photographie de l'état actuel », et
`CLAUDE.md` demande de **le lire avant de planifier**.

### 8.3 🟠 Deux numérotations P0 concurrentes
`PROJECT_STATUS.md` : P0-3 = cœur IoT, P0-4 = notification d'alerte, P0-5 = robustesse JWT.
`ROADMAP.md` : P0-4 = Liquibase, P0-5/P0-6 = cœur IoT.
**« P0-4 » ne désigne pas la même chose selon le fichier lu.**

### 8.4 🔴 `docs/architecture-cible.md` contredit l'ADR accepté
Il décrit les **5 modules d'ADR-0010**, explicitement **remplacé** par ADR-0013 (8 contextes).
Or `README.md` le présente comme l'architecture cible et `ROADMAP.md` P1-7 y renvoie. Un lecteur
suivant les liens depuis le README construit une image **fausse** de la cible.
Il porte en outre une décision jamais appliquée : `Geometry`/`Coordinate` comme **embeddables du
shared kernel** — l'implémentation en a fait des **entités du contexte `territory`** avec FK.

### 8.5 🟠 Statuts d'ADR faux, et règle projet contredite
12 ADR sur 13 sont marqués **« Proposé »**, et l'index affirme :
> « aucune implémentation n'est lancée avant passage à "Accepté" ».

Or 0001, 0005 (partiel), 0007, 0008 (partiel), 0009 (partiel) et 0012 **sont implémentés**. Soit la
règle a été enfreinte, soit les statuts n'ont jamais été mis à jour — dans les deux cas, **l'index
des ADR ne peut plus servir à savoir ce qui est décidé**. (Accessoirement, `README.md` annonce
« 12 ADR » ; il y en a 13.)

### 8.6 🟠 `endpoint.md` re-périmé
Régénéré en P2-4, il ignore déjà : `POST /auth/refresh`, `POST /auth/logout` (en cours), et le
déplacement de `MapsController`. Un document généré à la main se re-périme à chaque itération —
il devrait être **produit depuis l'OpenAPI**.

### 8.7 🔴 ADR-0006 repose sur une prémisse factuellement fausse
ADR-0006 retient `angular/` parce qu'il serait « le plus complet » et propose de **supprimer
`sonaged_web/`**. Mesure objective :

| Front | Fichiers `.ts` | Lignes | Angular | Architecture |
|---|---|---|---|---|
| `angular/` | 164 | 9 902 | 17.0.7 | NgModules |
| **`sonaged_web/`** | **196** | **16 851** | **17.3.0** | **standalone (`app.config.ts`, `app.routes.ts`)** |
| `ucgFrontend/` | 71 | 5 720 | 16 | mort |

`sonaged_web/` est **plus gros et plus moderne**. Volume ≠ complétude, mais **exécuter ADR-0006 en
l'état reviendrait peut-être à supprimer la meilleure base**. À réexaminer avant toute suppression.

### 8.8 🟠 Le blueprint Keycloak et le travail en cours se contredisent
`keycloak-migration.md` §4 prévoit de **supprimer** `JwtService`, `JwtFilter` et `SecurityConstants`.
Le chantier « sessions » en cours **étend précisément ces trois classes**. Les deux directions sont
défendables, mais elles doivent être arbitrées, pas menées en parallèle.

### 8.9 🟡 Écarts mineurs
- `CLAUDE.md` : « `datas/` (quartiers, circuits, dépotoirs, bacs) » → **13 fichiers**, dont
  `pre_collecte`, `ppef_cp`, `point_pp`, `CAISSES POLYBENNE`.
- `PROJECT_ANALYSIS.md` cite `Alert 1─1 Image` **et** `displayPicture BLOB` — le BLOB est retiré.
- `README.md` : `sonaged_web` « Angular » sans version (17.3.0).

---

## 9. Axes d'amélioration

### 9.1 Gouvernance documentaire — la cause racine
1. **Fermer la boucle de retour.** Toute itération qui implémente un ADR doit en changer le statut.
   Sans cela, l'index des ADR restera un document de fiction.
2. **Réduire le nombre de sources d'état.** `PROJECT_STATUS.md`, `PROJECT_ANALYSIS.md` et
   `IMPLEMENTATION_LOG.md` décrivent le même objet à des dates différentes. Un seul doit faire foi
   (le journal) ; les deux autres devraient être **datés comme archives** ou fusionnés.
3. **Corriger `CLAUDE.md`** en priorité : c'est le seul document chargé automatiquement à chaque
   session, donc celui dont les erreurs se propagent le plus.
4. **Réconcilier ou archiver `architecture-cible.md`** avec l'ADR-0013.
5. **Unifier la numérotation P0/P1/P2** sur `ROADMAP.md`.
6. **Générer `endpoint.md` depuis l'OpenAPI** au lieu de le maintenir à la main.
7. **Créer la mémoire persistante du projet** et y consigner les invariants (règle de non-suppression,
   base jetable, environnement sans Docker, arbitrages produits).
8. **Écrire les ADR manquants** pour les décisions structurantes prises hors ADR (§3.2) : UUID v7,
   module `administration`, ports applicatifs, propriétaire du rattachement tenant.

### 9.2 Produit — l'écart le plus coûteux
9. ~~**Trancher le périmètre du cœur IoT** avant de le construire : ADR-0004 ne couvre que le
   remplissage... Un ADR-0004bis est nécessaire.~~ — **fait, sans ADR dédié (corrigé 2026-08-06)** :
   `AlertThreshold`/`FillLevelProjector` couvrent déjà remplissage, température et humidité (voir
   §3.1/§7.1). Reste un point mineur pour la gouvernance documentaire : consigner cette extension
   de périmètre dans ADR-0004 lui-même (mise à jour de son statut) plutôt que la laisser implicite.
10. **Instruire le besoin citoyen** (§4.3) : c'est la demande n°1 de la seule étude utilisateur du
    projet, et elle est absente du backlog. Elle est peu coûteuse comparée à la chaîne IoT
    (horaires de collecte + notification) et donnerait une valeur perçue immédiate.
11. **Modéliser les rôles réels** (agent, superviseur de circuit, citoyen) : les circuits existent,
    les gens qui les parcourent n'existent pas dans le modèle.
12. **Enrichir `Avis`** (géolocalisation, photo, statut) pour couvrir le signalement de dépôt sauvage.

### 9.3 Technique
13. **Roter les secrets et purger l'historique Git** — ADR-0002 §4-5, jamais fait. Les secrets restent
    exploitables tant que l'historique existe, quelle que soit la configuration actuelle.
14. **Démarrer l'application contre PostgreSQL.** Elle ne l'a **jamais** été. `ddl-auto: validate`
    confrontera pour la première fois les entités au schéma Liquibase ; c'est là que se révéleront
    les écarts que ni le compilateur, ni `JpaMappingBootstrapTest`, ni le test de contexte H2 ne
    peuvent voir.
15. **Retirer `allow-circular-references: true`** (ADR-0009 §4, seul point non traité).
16. **Statuer sur les vestiges `worldline/tapandgo`** (`DataNotifierAspect`, enum `Permission`) :
    ils suggèrent que le squelette du projet vient d'une autre base de code.
17. **Câbler ou retirer les `SecurityRule`** : 13 règles d'autorisation écrites et jamais appliquées
    donnent une **fausse impression de contrôle d'accès**.
18. **Tests métier** : la couverture progresse (42 tests) mais reste concentrée sur ce qui a été
    touché récemment. `UploadFileServiceImpl` (~550 l.), l'import GeoJSON et MinIO restent nus.
19. **CI** : aucune. Tout le filet de sécurité repose sur une exécution manuelle de `mvnw verify`.

---

## 10. Ce qui manque à cette cartographie

Par honnêteté sur les limites de la recherche :
- **GitHub Issues / PR / Discussions / Projects** non consultés (`gh` absent).
- **Figma, Trello** cités sans lien — contenus inconnus.
- **Sources des diagrammes** (diagrams.net, Fritzing) absentes du dépôt : seules subsistent des
  images intégrées aux documents binaires.
- `.pptx` et `.docx` non extraits (le `.txt` du mémoire couvre l'essentiel du `.pdf`/`.docx` homonyme,
  mais `GESTION DE DECHET.docx` et la présentation n'ont pas d'équivalent texte).
- **18 commits non poussés** sur `chore/backend-finalisation` + le travail non commité de la session :
  l'état de `origin/main` ne reflète pas ce document.
