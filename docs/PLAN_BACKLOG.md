# Plan d'implémentation du backlog fonctionnel

> Écarts et justifications : `BACKLOG_FONCTIONNEL.md`. État réel du code : `IMPLEMENTATION_LOG.md`.
> Établi le **2026-07-30**, après vérification de la boucle métier de bout en bout contre PostgreSQL.

## Principe de séquencement

Trois contraintes ordonnent ce plan, dans cet ordre de force :

1. **Une dépendance de données prime sur une dépendance de code.** G4 (prédictif) est techniquement faisable demain et ne doit pourtant pas être lancé : 2 capteurs pour 71 points ne produisent aucune tendance. On construirait un modèle qui apprend sur du vide.
2. **G1 débloque quatre autres écarts** (G5, G6, G8, G10). Il passe avant tout ce qui en dépend, quelle que soit sa taille.
3. **Une leçon de la journée du 2026-07-30 :** ce qui n'a jamais été exécuté ne fonctionne pas. L'import a livré deux défauts latents au premier lancement réel, et le tri géographique un troisième — invisible en test unitaire, révélé par les 24 points de Mbao. **Chaque lot ci-dessous se termine par une exécution contre PostgreSQL avec des données réelles**, pas par un build vert.

---

## Lot 1 — Santé du parc de capteurs (G7)

**Pourquoi d'abord.** Taille S, aucune dépendance, et une valeur immédiate : le parc va grandir, et un capteur mort rend son point invisible sans que personne ne l'apprenne. Le faire maintenant, tant que le parc compte 2 capteurs, coûte une journée ; le faire à 200 capteurs, c'est déjà avoir perdu des données.

**Travail.**
- Planificateur périodique comparant `Sensor.lastSeenAt` à un seuil de silence configurable (`sonaged.iot.silence-threshold-hours`).
- Alerte de **code distinct** du débordement — un capteur muet est un problème de maintenance, pas de collecte, et les confondre brouillerait la tournée.
- Anti-répétition : une alerte par capteur et par épisode de silence, close au retour des mesures.
- Endpoint de parc trié par ancienneté de dernier contact.

**Vérification.** Contre PostgreSQL : enrôler un capteur, émettre, avancer le seuil, constater l'alerte ; réémettre, constater la clôture ; vérifier qu'un second cycle ne crée pas de doublon.

**Attention.** `AlertCode` est un enum en base (`VARCHAR`) — ajouter une valeur demande un changeset si une contrainte l'énumère. Vérifier avant, pas au démarrage.

---

## Lot 2 — L'agent de collecte et la clôture de la boucle (G1)

**Pourquoi ici.** C'est le maillon manquant de « détecter → alerter → **collecter** → constater ». Sans lui, quatre autres écarts sont incalculables, et le produit ne sait jamais ce qu'il a accompli.

**Travail, dans cet ordre.**
1. **Rôle `AGENT`** : changeset de semis sur le modèle de `2.1.0-7`, permissions associées, règles d'autorisation dans `SecurityConfiguration` (l'agent lit sa tournée, écrit ses passages, ne touche pas au référentiel).
2. **Affectation territoriale** : un agent est rattaché à des communes. Sans cela, tout agent voit tout — ce qui n'a pas de sens sur 12 communes et rend l'écran inexploitable.
3. **Passage enregistré** : `POST /v1/collection-routes/stops/{depotoirId}/collected` — horodate, remet `fillLevelPercent` à 0, pose `lastMeasuredAt`, **clôt l'alerte ouverte** du point. C'est le geste central de tout le lot.
4. **Point inaccessible** : même geste avec un motif ; le point **reste** dans la tournée du lendemain. Un obstacle n'est pas une collecte, et le confondre avec un oubli fausserait tout indicateur.
5. **Taux de réalisation** de la tournée du jour.

**Décisions à trancher — chacune mérite une ligne d'ADR.**
- **Qui fait autorité sur le niveau, l'agent ou le capteur ?** Si une mesure arrive après un passage, elle doit primer (le capteur voit l'état réel). Si aucune mesure n'arrive, la déclaration de l'agent tient. À écrire explicitement, sinon les deux sources se contrediront en silence.
- **Où vit le passage ?** Le contexte `waste` possède le point de collecte ; l'identité possède l'agent. Référence **par identifiant** (ADR-0012), jamais d'association.
- **Un agent peut-il déclarer un passage hors tournée ?** Oui en pratique (il passe devant un bac plein) — mais alors la tournée n'est plus la seule porte d'entrée.

**Vérification.** Contre PostgreSQL, avec un vrai compte `AGENT` : tournée sur Mbao → marquer un point collecté → le niveau retombe, l'alerte se clôt, le point sort de `DEBORDEMENT` → le taux de réalisation bouge. Puis un point inaccessible → il réapparaît le lendemain.

**Piège identifié.** `CollectionRouteServiceImpl` classe en `ETAT_INCONNU` tout point dont la mesure est périmée (24 h). Un point collecté hier et non mesuré depuis y retombera — et remontera donc en tête de tournée. Il faut décider si un **passage** rafraîchit l'information au même titre qu'une mesure. Sans cela, les agents reverront chaque jour les points qu'ils viennent de vider.

---

## Lot 3 — Notifications push (G2)

**Pourquoi après G1.** Non par dépendance technique, mais parce que le lot 2 crée le second usage du canal (prévenir un agent d'une alerte sur sa zone) : construire le canal pour un seul usage puis le reprendre serait du travail refait.

**Travail.**
- Enregistrement d'un jeton d'appareil par compte, avec révocation.
- Adaptateur d'envoi derrière un **port** — l'ADR-0007 prévoyait FCM, et le remplacer un jour ne doit pas toucher les appelants.
- Branchement sur les deux émetteurs existants : `CollectionReminderScheduler` et le changement d'état d'un signalement.
- Échec d'envoi **journalisé et non bloquant** : un jeton périmé ne doit pas faire échouer un rappel de collecte pour tout un quartier.

**Préalable d'infrastructure, à ne pas contourner.** Compte Firebase et clé de service. **La clé Google est déjà dans l'historique Git** (ADR-0002 §4-5, rotation jamais faite) : la roter **avant** ce lot, et ne pas en ajouter une seconde. Elle passe par l'environnement, comme `SONAGED_ADMIN_PASSWORD`.

**Vérification.** Envoi réel vers un appareil de test, application fermée.

---

## Lot 4 — Rapports de performance (G5)

**Pourquoi maintenant.** Le lot 2 rend enfin l'efficacité mesurable : sans « collecté », il n'y a pas de délai alerte → collecte, et un rapport n'aurait que des dénominateurs.

**Travail.** Sélection période + territoire ; délai moyen alerte → collecte ; taux de réalisation des tournées ; classement des points chroniquement en débordement ; export.

**Attention.** Ce sont des agrégats sur des volumes croissants. Le contexte `analytics` publie déjà des read-models ; ne pas y faire remonter des entités d'autres contextes (`modules.verify()` refuse — il l'a déjà fait pour l'amorçage du compte). Les index nécessaires demanderont un changeset.

---

## Lot 5 — Sensibilisation (G3)

**Travail.** Message ciblé par commune ou quartier, programmable, diffusé par le canal du lot 3 ; historique consultable par le citoyen ; traçabilité (auteur, date, nombre de destinataires).

**Garde-fou.** Une fonction d'envoi de masse est aussi une fonction de nuisance. Réserver la rédaction à l'administration, plafonner la fréquence, et rendre le désabonnement effectif — un citoyen qui ne peut pas se taire désinstalle l'application.

---

## Lot 6 — Traçabilité des collectes (G8)

**Travail.** Journal par point : mesures, alertes, passages. Alimenté par les lots 1 et 2.

**Décision préalable.** `HistoryEntity` est une coquille vide (un `@Id`) qui traîne DTO, mapper, repository, service et contrôleur. Deux voies : la remplir, ou la supprimer et créer une entité dédiée. **La suppression exige une validation explicite** (règle projet). Trancher avant d'écrire.

---

## Lot 7 — Organisation du balayage (G6)

**Ne pas démarrer sans cadrage métier.** La source est un fragment d'entretien — « CHAQUE BALAYEUR A UN 500M », « CHAQUE SUP A 25 AGENTS », « 66 circuits de collecte » — pas une spécification. Et la base contredit déjà les notes : **52 circuits de collecte et 156 de balayage** importés, contre « 66 circuits » annoncés. Modéliser sur cette base produirait un modèle faux et coûteux à défaire.

**Questions à poser à l'UCG avant toute ligne de code.** Que compte-t-on dans les 66 ? Un segment de 500 m est-il une entité stable ou un découpage indicatif ? Les trois plages sont-elles un objectif ou l'existant ? Un agent est-il affecté à un segment, à un circuit, ou à un superviseur ?

**Travail, une fois cadré.** Agent de terrain, affectation, segment, plages ; couverture par circuit et par plage.

---

## Lot 8 — Prédictif (G4)

**Condition d'entrée, non négociable : disposer de plusieurs semaines de mesures sur un nombre significatif de points.** Aujourd'hui : 2 capteurs de test pour 71 points. Lancer ce lot avant équipement produirait un modèle entraîné sur du vide, dont personne ne saurait dire s'il se trompe.

**Travail.** Vitesse de remplissage par point sur fenêtre glissante ; date de saturation projetée ; intégration comme niveau d'urgence dans la tournée.

**Exigence de conception.** Le comportement quand l'historique est trop court doit être **explicite** : ne pas produire de tendance plutôt qu'en inventer une. C'est la même règle que « un point jamais mesuré n'est pas un point vide », qui a déjà coûté un défaut aujourd'hui — l'ancienneté conventionnelle d'un point jamais mesuré faisait basculer toute la tournée dans le rattrapage anti-famine.

---

## Lot 9 — Mode hors ligne mobile (G10)

À rouvrir quand le lot 2 aura mis des agents sur le terrain et que l'usage réel dira si le réseau manque. File d'attente locale des passages déclarés, synchronisation au retour du réseau, résolution des conflits avec les mesures capteur.

---

## Hors périmètre

**G9 — télémétrie de flotte étendue** (carburant, comportement du conducteur, temps en trafic). C'est un exemple d'état de l'art (Nairobi/IBM) cité dans les notes, pas une exigence du mémoire. À reconsidérer si l'UCG le demande, une fois la boucle de base réellement exploitée.

---

## Récapitulatif

| Lot | Écart | Taille | Condition d'entrée |
|---|---|---|---|
| 1 | G7 santé des capteurs | S | — |
| 2 | G1 agent + collecte effectuée | L | — |
| 3 | G2 notifications push | M | Firebase + **rotation de la clé Google** |
| 4 | G5 rapports | M | lot 2 |
| 5 | G3 sensibilisation | M | lot 3 |
| 6 | G8 traçabilité | M | lot 2 + décision sur `HistoryEntity` |
| 7 | G6 balayage | L | lot 2 + **cadrage UCG** |
| 8 | G4 prédictif | L | **capteurs déployés, historique réel** |
| 9 | G10 hors ligne | M | lot 2 + usage constaté |

Les lots 1 et 2 sont exécutables immédiatement. Les lots 3, 7 et 8 attendent quelque chose qui ne dépend pas du code : une clé rotée, une réponse du métier, des capteurs posés.
