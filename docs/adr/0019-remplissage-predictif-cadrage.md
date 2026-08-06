# ADR-0019 — Remplissage prédictif : cadrage (G4)

- Statut : 🔵 **Proposé — cadrage seulement, aucun code**. Condition d'entrée (voir §Décision) non remplie : le parc annoncé (« capteurs bientôt équipés », 2026-08-06) n'a pas encore produit d'historique.
  ⚠️ **§4 non implémentable en l'état** — voir « Complément (2026-08-06) » en bas de page : un
  cycle de modules bloque `waste → iot`.
- Date : 2026-08-06
- Priorité : Lot 8 / **G4** (`docs/PLAN_BACKLOG.md`)

## Contexte

Le mémoire (cas d'usage administrateur) demande une projection : à quelle date un point de collecte donné saturera-t-il, pour l'intégrer comme niveau d'urgence dans la tournée. `docs/PLAN_BACKLOG.md` (Lot 8) posait une **condition d'entrée non négociable** : disposer de plusieurs semaines de mesures sur un nombre significatif de points. Au 2026-07-30, le parc réel comptait 2 capteurs pour 71 points — lancer le lot aurait entraîné un modèle sur du vide.

L'UCG prévoit maintenant d'équiper des capteurs. Cet ADR **cadre** la fonctionnalité pendant que le parc grandit, pour que son implémentation, une fois l'historique disponible, soit un petit changement bien délimité plutôt qu'une conception improvisée sous pression. Il ne lève pas la condition d'entrée : il la rend **vérifiable par le code, point par point**, plutôt que par une estimation globale du parc.

Trois erreurs déjà commises ailleurs dans ce projet cadrent ce qu'il faut éviter ici :
- « Jamais mesuré n'est pas vide » (`CollectionRouteServiceImpl`) — l'absence de donnée ne doit jamais se traduire par une valeur inventée.
- Le module `iot` **n'interprète rien** (`iot/package-info.java`) — il reçoit et conserve des mesures, il ne décide d'aucune règle métier.
- Une tournée qui rebascule sans cesse à cause d'une info éphémère fait abandonner l'outil (`ADR-0017`) — une projection ne doit pas faire clignoter la tournée.

## Décision

### 1. La condition d'entrée se vérifie par point, pas globalement

Un parc « globalement suffisant » ne dit rien d'un point précis fraîchement instrumenté. La règle est donc locale : un point ne reçoit une projection **que si**, pour ce point précis :

- au moins `sonaged.predictif.min-measurements` mesures de niveau (défaut : **20**), **et**
- ces mesures s'étalent sur au moins `sonaged.predictif.min-window-days` jours (défaut : **14**).

Les deux conditions sont nécessaires : un capteur qui spamme 20 mesures en une heure ne dit rien d'une tendance ; un capteur qui mesure une fois par semaine depuis deux mois n'a pas assez de points pour une régression fiable. Un point qui ne satisfait pas les deux reste **explicitement sans projection** — jamais une tendance approximative construite sur 3 mesures. Même contrat que « jamais mesuré n'est pas vide » : l'absence de certitude s'exprime par une absence de valeur, pas par une valeur fragile.

Conséquence directe : la fonctionnalité s'active **progressivement, point par point**, à mesure que le parc grandit. Aucun interrupteur global ne peut l'activer prématurément sur un point sans historique.

### 2. Algorithme : régression linéaire, pas de modèle prédictif

Régression linéaire simple (moindres carrés) du niveau (%) en fonction du temps, sur une fenêtre glissante de `sonaged.predictif.window-days` (défaut : **30**) jours. La pente (`%/jour`) donne :
- **pente ≤ 0** (le point se vide ou stagne) → aucune date de saturation. Ne pas en produire une négative ou infinie serait la même erreur que « jamais mesuré = vide » à l'envers.
- **pente > 0** → date projetée = `maintenant + (100 − niveau_actuel) / pente` jours.

Rejeté : un modèle entraîné (ML) — la spec (§Alternatives) explique pourquoi. La régression linéaire est lisible, vérifiable à la main sur un tableur, et suffisante pour une courbe de remplissage qui n'a aucune raison d'être autre chose que globalement monotone entre deux collectes.

### 3. Fenêtre glissante bornée par le dernier passage constaté

**Piège identifié** : un passage `COLLECTED` (G1, `CollectionPassageServiceImpl`) remet `fillLevelPercent` à 0 directement sur `Depotoir`, **sans** créer de ligne `Measurement` côté `iot`. Une régression naïve sur les 30 derniers jours de mesures IoT continuerait donc, juste après une collecte, à voir les valeurs hautes d'avant le passage — et projeterait une saturation dans 2 jours pour un bac qu'on vient de vider.

Décision : la fenêtre glissante démarre au plus tard entre `{maintenant − window-days}` et `{date du dernier passage COLLECTED sur ce point}`. Un point tout juste vidé repart donc sans historique exploitable — ce qui déclenche naturellement la règle du §1 (pas assez de mesures dans la fenêtre → pas de projection), au lieu d'halluciner une tendance sur un état qui n'existe plus.

### 4. Où vit le calcul : `waste`, pas `iot`

`iot` expose déjà `IngestionMetrics.measurementsFor(depotoirId, from, to)` — un historique brut, en lecture seule, sans franchissement d'entité (`docs/adr/0004-…`, `IngestionMetrics.java`). C'est le seul point d'entrée nécessaire : **aucun nouveau port côté `iot`**. La régression elle-même — « cette pente justifie une visite anticipée » — est une règle métier déchets, exactement comme le seuil d'alerte (`ThresholdResolver`) ou le franchissement (`FillLevelProjector`) : elle vit donc dans `waste`, dans un nouveau composant pair de ceux-ci (nom de travail : `FillTrendEstimator`), et non dans `iot` qui « n'interprète rien ».

Contrat explicite (même logique que `RouteStop`, qui documente déjà pourquoi un `null` est un fait et pas un défaut) :

```java
Optional<FillTrend> estimate(Long depotoirId, Instant asOf);

record FillTrend(double percentPerDay, Instant projectedSaturationAt,
                  int measurementsUsed, Instant windowStart) { }
```

`Optional.empty()` est la réponse normale et attendue pour l'immense majorité des points tant que le parc est jeune — pas une erreur, pas un cas limite à logger en warning.

### 5. Intégration dans la tournée : un niveau d'urgence, pas un remplacement

Nouvelle valeur d'énumération dans `CollectionRouteService.StopPriority`, insérée entre les deux existantes qui l'encadrent logiquement :

```
DEBORDEMENT           // constaté, certain
SATURATION_PROJETEE   // <- nouveau : inféré, mais concret et actionnable
ETAT_INCONNU          // aucune visibilité — reste plus urgent qu'une simple surveillance
A_SURVEILLER
RIEN_A_FAIRE
```

Un débordement **mesuré** prime toujours sur une projection : la certitude bat l'inférence, même défavorable. `ETAT_INCONNU` reste au-dessus : ne rien savoir du tout demeure pire que savoir qu'un point va probablement déborder — cohérent avec la raison d'être de cette priorité (`CollectionRouteServiceImpl`, commentaire de classe).

Un point n'entre dans `SATURATION_PROJETEE` que si la date projetée tombe dans un horizon de routage `sonaged.predictif.horizon-hours` (défaut : **48**). Au-delà, la projection reste une donnée de reporting (Lot 4 / G5) mais ne doit pas faire bouger une tournée du jour — reprendre l'avertissement déjà écrit pour l'anti-famine (`ADR-0017`) : une tournée qui se réordonne sur un horizon lointain et incertain perd la confiance de l'agent qui la suit.

### 6. Rien n'est persisté

La projection se calcule à la demande, à chaque appel de planification de tournée — une requête sur l'index existant `(depotoirid, measuredat)` plus une régression en mémoire sur ≤ 30 points, coût négligeable. Aucun nouveau champ dénormalisé sur `Depotoir`, contrairement à `fillLevelPercent`/`lastMeasuredAt` : ces deux-là doivent survivre entre deux mesures, une projection non — la recalculer à chaque lecture évite un champ de plus à tenir cohérent, et évite qu'une projection devienne silencieusement périmée comme l'a déjà été une mesure trop ancienne.

## Conséquences

- **+** Condition d'entrée vérifiable automatiquement, par point : aucun risque d'activer la fonctionnalité prématurément même si le lot est mergé avant que le parc soit prêt.
- **+** Aucune dépendance nouvelle, aucun nouveau port `iot`, aucune entité supplémentaire — le lot 8, une fois le gate ouvert, se limite à `FillTrendEstimator` + un branchement dans `CollectionRouteServiceImpl` + le nouveau `StopPriority`.
- **+** Piège du passage-sans-mesure documenté et neutralisé avant d'être découvert en production (même style que la découverte du 2026-07-30 sur le tri géographique).
- **−** Recalcul à la demande à chaque planification : coûte une requête + une régression par point instrumenté et par appel. Négligeable au parc actuel (71 points) ; à revisiter si le parc atteint plusieurs milliers de points (mise en cache à évaluer alors, pas avant).
- **−** La régression linéaire suppose un remplissage globalement monotone entre deux vidages ; un point à comportement erratique (dépôts sauvages ponctuels) produira une pente peu fiable. Accepté : la certitude affichée (`measurementsUsed`, `windowStart`) permet à un rapport (G5) de la relativiser ; le mémoire ne demande pas mieux qu'une tendance simple.

## Alternatives considérées

- **Modèle entraîné (régression non linéaire, ML)** : rejeté. Aucun historique suffisant pour entraîner quoi que ce soit avant longtemps, et un modèle opaque serait plus difficile à expliquer à un agent de terrain qu'une pente en `%/jour`. Réévaluer seulement si la régression linéaire se révèle insuffisante en pratique, jamais par anticipation.
- **Lissage exponentiel** : rejeté au profit de la régression — pas plus simple à implémenter, moins lisible (pas de « date de saturation » directe), et sans avantage démontré sur un remplissage supposé monotone.
- **Interrupteur global unique** (`sonaged.predictif.enabled=true/false` sans gate par point) : rejeté — masquerait la vraie condition d'entrée. Un administrateur activant le lot le jour où « assez » de capteurs sont posés produirait des projections fantaisistes sur les points tout juste instrumentés. Le gate par point rend cet interrupteur global inutile : la fonctionnalité s'active elle-même, point par point, quand les données le permettent.
- **Persister la projection sur `Depotoir`** : rejeté — voir §6.

## Complément (2026-08-06) — §4 contredit un cycle de modules existant

Tentative d'implémentation du seul gate d'éligibilité (§1), avant même `FillTrendEstimator` :
`SmartWasteModularityTests` échoue avec un **cycle** `iot ↔ waste`.

`iot` dépend déjà de `waste` : `DeviceProvisioningServiceImpl` (enrôlement d'un capteur) appelle
`WasteReadModel.collectionPointExists(...)` / `existingCollectionPoints(...)` pour vérifier qu'un
`depotoirId` désigne un point réel. Cette dépendance est légitime et antérieure à cet ADR. Le §4
fait dépendre `waste` de `iot.IngestionMetrics` dans l'autre sens — Spring Modulith refuse les deux
directions à la fois (`modules.verify()`, garde-fou documenté dans `CLAUDE.md`).

**Non résolu ici.** Deux pistes, ni l'une ni l'autre tranchée :
- inverser la dépendance de `DeviceProvisioningServiceImpl` (ex. valider l'existence du point via
  un événement plutôt qu'un appel synchrone à `WasteReadModel`) — un refactor d'un chemin qui
  fonctionne aujourd'hui, donc à valider avant d'y toucher ;
- reconsidérer où vit le gate/l'estimateur, en assumant le coût que le §4 écartait explicitement.

Le gate d'éligibilité n'a pas été livré : le construire dans `waste` casse le build tant que ce
point n'est pas tranché. Voir `docs/IMPLEMENTATION_LOG.md`.
