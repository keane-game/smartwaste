# Backlog fonctionnel — écarts entre la spécification et le système

> Établi le **2026-07-30** par relecture intégrale de deux sources :
> - `Gestion automatisée des ordures ménagères mise en place d'un système alerte d'un point de collecte.md` (le mémoire — acteurs, cas d'utilisation, objectifs) ;
> - `GESTION DE DECHET IoT.md` (notes de terrain UCG : entretiens M. Diop, cartographie ; M. Diallo, assistance technique — plus l'étude de cas Kenya/IBM).
>
> Plan d'exécution : `PLAN_BACKLOG.md`. État réel du code : `IMPLEMENTATION_LOG.md`.

## Méthode

Chaque exigence des deux documents a été confrontée au code (contrôleurs, entités, rôles semés, dépendances). Ce backlog ne liste que ce qui **n'est pas couvert**. Ce qui l'est déjà figure d'abord, pour que les écarts se lisent sur fond de ce qui existe.

## Ce qui est déjà couvert

| Exigence (source) | Où |
|---|---|
| S'authentifier · créer un compte (mémoire §3.1.5.2) | `auth/**`, JWT, sessions révocables |
| Surveillance temps réel du remplissage (§1.3.3, §3.1.5.2) | `Measurement`, `FillLevelProjector`, `POST /v1/measurements` |
| Alertes automatiques au seuil (§1.3.3) | `AlertThreshold`, vérifié de bout en bout le 2026-07-30 |
| Seuils configurables remplissage / température / humidité (§3.1.6.1) | `/v1/alert-thresholds` |
| Localiser poubelles, dépôts, véhicules (§3.1.5.2) | `/v1/maps/**`, `/v1/vehicle-positions` |
| Signaler un dépôt sauvage (§3.1.6.2) | `Avis` (+ latitude/longitude, cycle de vie) |
| Horaires de collecte (§3.1.6.2) | `/v1/collection-schedules` |
| Itinéraires de collecte (§1.3.3) | `/v1/collection-routes` (ordre géographique depuis ADR-0017) |
| Notification temps réel superviseur (§3.1.5.2) | SSE `/v1/alerts/stream` |
| Administration du référentiel et des équipements (§3.1.6.1) | CRUD complet, `/v1/devices` |

---

## G1 · L'acteur « Agent de collecte » n'existe pas — **critique**

**Source.** Mémoire §3.1.5.1 : le système compte **trois** acteurs — Administrateur, **Agent de collecte**, Citoyen. L'agent est défini comme celui qui « assure la collecte et le transport des déchets » et dont la tâche est « l'intervention immédiate dans le cas de recevoir une alerte de collecte ». Notes UCG : « RESPONSABLE DE LA COLLECTE ».

**Constat.** Les rôles semés sont `SUPER_ADMIN`, `ADMIN`, `USER` — il n'y a **pas de rôle agent**. Aucun écran, aucun endpoint ne s'adresse à lui. Surtout, `grep -rilE "collecteEffectue|markCollected|vidage|emptied|servedAt"` ne renvoie **rien** : **aucun moyen d'enregistrer qu'un point a été collecté**.

**Pourquoi c'est le plus grave.** La boucle métier du produit est « détecter → alerter → **collecter** → constater ». Les trois premiers maillons fonctionnent depuis aujourd'hui ; le quatrième n'existe pas. Conséquences en chaîne :
- le niveau de remplissage ne retombe que si un capteur le dit — or 71 points sur 71 n'ont pas de capteur ;
- une tournée ne peut pas être clôturée, donc le système ne sait jamais ce qui a été fait ;
- aucun indicateur d'efficacité n'est calculable (délai alerte → collecte), ce qui vide de sens le G5 ;
- l'alerte reste ouverte indéfiniment.

**Récits.**
- *En tant qu'agent de collecte, je vois la tournée du jour sur ma commune, ordonnée, pour savoir où aller.*
- *En tant qu'agent, je marque un point « collecté » (avec horodatage et position), pour que le système sache qu'il est vide.*
- *En tant qu'agent, je signale un point inaccessible (voie barrée, bac absent) avec un motif, pour que ce ne soit pas confondu avec un oubli.*
- *En tant que superviseur, je vois le taux de réalisation de la tournée du jour.*

**Critères d'acceptation.** Rôle `AGENT` semé et habilité ; `POST /v1/collection-routes/stops/{id}/collected` remet le niveau à 0, horodate et clôt l'alerte associée ; un point marqué inaccessible reste dans la tournée du lendemain avec son motif ; l'agent ne peut agir que sur les communes qui lui sont affectées.

**Taille : L.** Dépend de : rien. **Bloque : G5, G8.**

---

## G2 · Le citoyen ne reçoit aucune notification hors application ouverte — **critique**

**Source.** Mémoire §3.1.5.2 : « l'envoi et la réception d'alertes **en temps réel** concernant le niveau de remplissage des poubelles, les horaires de collecte, les anomalies détectées ». §3.1.5.1 : le citoyen « reçoit les mises à jour […] à travers une application Smartphone ».

**Constat.** La seule diffusion est **SSE** (`/v1/alerts/stream`), qui exige une connexion HTTP ouverte et un jeton : cela convient à un poste de supervision, pas à un téléphone. Aucune trace de Firebase / FCM dans `src/main` ni dans `pom.xml`. ADR-0007 le reconnaît : « SSE fait, FCM non fait ».

**Effet.** Le rappel « sortez vos ordures » (`CollectionReminderScheduler`) et les alertes n'atteignent jamais un citoyen dont l'application est fermée — c'est-à-dire presque toujours. La fonctionnalité existe côté serveur et ne produit aucun effet observable côté usager.

**Récits.**
- *En tant que citoyen abonné à mon quartier, je reçois une notification sur mon téléphone la veille du passage, même application fermée.*
- *En tant que citoyen, je reçois un accusé quand mon signalement change d'état.*

**Critères.** Jeton d'appareil enregistré par compte ; envoi FCM sur rappel de collecte et sur changement d'état d'un signalement ; échec d'envoi journalisé et non bloquant ; désabonnement effectif.

**Taille : M.** Dépend de : compte Firebase (infra). **Note :** la clé Google est déjà dans l'historique Git (ADR-0002) — à roter avant toute mise en service.

---

## G3 · Aucune notification de sensibilisation

**Source.** Mémoire §3.1.5.2, liste des fonctionnalités du système : « […] et des **notifications de sensibilisation** ». Le manque de sensibilisation est par ailleurs cité comme **cause du problème** (§1.4, notes IoT : « Une sensibilisation insuffisante des populations aux pratiques de gestion des déchets entrave les efforts de réduction et de recyclage »).

**Constat.** Rien. Aucune entité, aucun endpoint, aucun planificateur.

**Récits.**
- *En tant qu'administrateur, je rédige un message de sensibilisation et je le programme vers un quartier ou une commune.*
- *En tant que citoyen, je consulte l'historique des messages reçus.*

**Critères.** Message ciblé par territoire, programmable, diffusé par le même canal que G2 ; consultable ; traçable (qui, quand, combien de destinataires).

**Taille : M.** Dépend de : **G2** (sans canal, un message ne part pas).

---

## G4 · Aucun algorithme prédictif

**Source.** Mémoire §1.3.3, objectif explicite : « Utilisez des **algorithmes prédictifs** pour anticiper les besoins en collecte de déchets, en tenant compte des tendances de production de déchets dans chaque zone, des jours de la semaine, des événements spéciaux ».

**Constat.** `Measurement` accumule un historique horodaté — la matière première existe — mais rien ne l'exploite. La priorisation des tournées est purement réactive : elle regarde l'état courant, jamais la tendance.

**Récits.**
- *En tant que superviseur, je vois la date probable de saturation d'un point, pour passer avant le débordement plutôt qu'après.*
- *En tant que superviseur, je vois les points dont la vitesse de remplissage a changé (événement, marché, fête).*

**Critères.** Vitesse de remplissage estimée par point sur fenêtre glissante ; date de saturation projetée ; intégration comme niveau d'urgence supplémentaire dans la tournée ; comportement défini quand l'historique est trop court (ne pas inventer de tendance).

**Taille : L.** Dépend de : un historique réel, donc du **déploiement de capteurs** — aujourd'hui 2 capteurs de test pour 71 points. **À ne pas lancer avant d'avoir des données.**

---

## G5 · Aucun rapport de performance

**Source.** Mémoire §3.1.6.1, cas d'utilisation Administrateur : « L'administrateur peut **générer des rapports de performance** pour surveiller l'efficacité du système de gestion des déchets » et « accéder aux données collectées par les capteurs et les **analyser** ».

**Constat.** `/v1/supervision/stats` rend des indicateurs **instantanés**. Il n'existe ni période, ni comparaison, ni export, ni notion d'efficacité (délai entre alerte et collecte, taux de réalisation des tournées, points chroniquement en débordement).

**Récits.**
- *En tant qu'administrateur, j'édite un rapport sur une période et une commune, et je l'exporte.*
- *En tant qu'administrateur, je compare deux périodes pour savoir si la situation s'améliore.*

**Critères.** Sélection période + territoire ; délai moyen alerte → collecte ; taux de réalisation ; points les plus problématiques ; export CSV ou PDF.

**Taille : M.** Dépend de : **G1** (sans « collecté », aucun délai n'est calculable — le rapport n'aurait que des dénominateurs).

---

## G6 · L'organisation réelle du balayage est absente du modèle

**Source.** Notes de terrain UCG, exclusivement : « ILS ONT DES AXES DE BALAYAGES ; **CHAQUE CIRCUIT A UN SUPERVISEUR** / **CHAQUE BALAYEUR A UN 500M** / **CHAQUE SUP A 25 AGENTS** / RESPONSABLE DE LA COLLECTE / **UN BALAYAGE MATIN, SOIR ET NUIT** / **66 circuits de collecte** ».

**Constat.** `CircuitBalayageEntity` porte un `shift` (`CircuitShift`) et une longueur — c'est tout. Aucun superviseur, aucun agent, aucune affectation, aucun découpage en segments de 500 m. Le balayage, qui est une part entière du métier UCG, n'est modélisé que comme un tracé sur une carte. À noter : la base contient **156 circuits de balayage et 52 de collecte**, quand les notes parlent de **66 circuits de collecte** — l'écart mérite d'être tranché avec le métier avant de construire dessus.

**Récits.**
- *En tant que superviseur, je vois mes circuits et les agents qui y sont affectés.*
- *En tant que responsable, j'affecte un balayeur à un segment et à une plage (matin / soir / nuit).*
- *En tant que superviseur, je constate la couverture d'un circuit sur une plage donnée.*

**Critères.** Notion d'agent de terrain et d'affectation ; segment de circuit ; les trois plages ; vue de couverture par circuit et par plage.

**Taille : L.** Dépend de : **G1** (le rôle agent). **Préalable : cadrage métier** — les notes sont des bribes d'entretien, pas une spécification. Ne pas modéliser sans validation UCG.

---

## G7 · Un capteur muet ne déclenche rien

**Source.** Mémoire §3.1.6.1 (l'administrateur « effectue des maintenances pour assurer le bon fonctionnement du système ») ; §3.2.1 mentionne l'autonomie et la batterie des modules.

**Constat.** `Sensor` porte `lastSeenAt` et `active`, `IngestionMetrics` expose la santé de l'ingestion — mais **aucune alerte** n'est levée quand un capteur cesse d'émettre. Le point devient silencieusement « état inconnu » : la tournée le traite correctement (c'est le mérite de `ETAT_INCONNU`), mais **personne n'est prévenu que le capteur est mort**. Un parc de capteurs se dégrade sans que quiconque le sache.

**Récits.**
- *En tant qu'administrateur, je suis alerté quand un capteur n'a rien émis depuis N heures.*
- *En tant qu'administrateur, je vois le parc trié par ancienneté de dernier contact.*

**Critères.** Seuil de silence configurable ; alerte dédiée (code distinct du débordement) ; pas de répétition à chaque cycle ; retour à la normale constaté.

**Taille : S.** Dépend de : rien. **Meilleur rapport valeur / effort du backlog.**

---

## G8 · Aucune traçabilité des collectes

**Source.** Mémoire §3.1.6.1 (« accéder aux données collectées et les analyser »). Implicite dans tout rapport.

**Constat.** `HistoryEntity` est une **coquille vide** — un seul `@Id` — qui traîne néanmoins un DTO, un mapper, un repository, un service et un contrôleur (déjà signalé dans `CLAUDE.md`). Aucun événement métier n'est historisé.

**Récits.**
- *En tant qu'administrateur, je consulte l'historique d'un point : mesures, alertes, collectes.*

**Critères.** Journal des événements par point ; consultable ; alimenté par les collectes (G1) et les alertes ; statuer sur le sort de `HistoryEntity` (**suppression → validation explicite requise**).

**Taille : M.** Dépend de : **G1**.

---

## G9 · Télémétrie de flotte réduite à la position

**Source.** `GESTION DE DECHET IoT.md`, étude de cas Nairobi/IBM : « surveillance du **comportement des conducteurs**, détection des dos d'âne et des nids-de-poule, vérification de la **consommation de carburant** », « temps passé dans le **trafic** et temps consacré à la collecte ».

**Constat.** `VehicleTracker` et `/v1/vehicle-positions` ne transportent qu'une position.

**Appréciation.** C'est un **exemple étranger cité en état de l'art**, pas une exigence du mémoire pour ce système. À traiter comme une ambition, non comme un manque.

**Taille : XL.** **Recommandation : hors périmètre** tant que la boucle de base n'est pas exploitée.

---

## G10 · Aucun fonctionnement hors ligne côté mobile

**Source.** Non exprimé explicitement ; déduit du contexte (§1.4, couverture réseau) et du choix de LoRa « pour les environnements où la connexion Internet est limitée ou indisponible » (§3.2.1).

**Constat.** L'application Flutter suppose le réseau.

**Appréciation.** Réel pour un agent en tournée dans Pikine, spéculatif pour un citoyen. À rouvrir quand G1 mettra des agents sur le terrain.

**Taille : M.** Dépend de : **G1**.

---

## Explicitement hors périmètre

- **Recyclage et tri** — 16 occurrences, toutes en contexte ou en définition ; aucun cas d'utilisation. Le mémoire décrit le recyclage comme un enjeu, pas comme une fonction du système.
- **Servomoteur, couvercle automatique, drone** — matériel du prototype (§3.2.1) ; sans objet côté logiciel.
- **Pesée des déchets** — une occurrence, aucune exigence associée.
- **Contrôleur de balayage** — les notes UCG le disent elles-mêmes : « QUI NE FAIT PAS PARTI DU SYSTEME ».
- **`MoblierUrbain`** — CRUD complet jamais alimenté. Les notes UCG (« mobilier urbain : point propre et bacs de rue ») décrivent ce que l'import charge déjà comme `Depotoir` typé. Redondant ; son retrait est une **suppression de code → validation requise** (cf. ADR-0015).

## Ordre de valeur

| # | Écart | Taille | Bloqué par |
|---|---|---|---|
| 1 | **G7** capteur muet | S | — |
| 2 | **G1** agent + collecte effectuée | L | — |
| 3 | **G2** notifications push | M | infra Firebase |
| 4 | **G5** rapports de performance | M | G1 |
| 5 | **G3** sensibilisation | M | G2 |
| 6 | **G8** traçabilité | M | G1 |
| 7 | **G6** organisation du balayage | L | G1 + cadrage UCG |
| 8 | **G4** prédictif | L | données réelles |
| 9 | **G10** hors ligne | M | G1 |
| — | **G9** télémétrie étendue | XL | hors périmètre |
