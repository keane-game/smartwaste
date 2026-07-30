# ADR-0017 — Ordre de passage des tournées : l'urgence découpe, la géographie ordonne

- Statut : **Accepté — implémenté** (2026-07-30)
- Date : 2026-07-30
- Priorité : P1 — première fonctionnalité réellement neuve après la mise en service
- Dépend de [ADR-0016](0016-geometrie-des-entites-dechets.md) : sans position, rien de tout ceci n'est calculable

## Contexte

`CollectionRouteServiceImpl` porte trois règles solides, chacune corrigeant une erreur qu'on commet naturellement en écrivant ce tri : un point jamais mesuré n'est pas un point vide, une mesure ancienne vaut une absence de mesure, et à urgence égale le plus ancien passe devant (sans quoi un point attend indéfiniment — de la famine).

Il lui manquait la géographie. Le tri s'écrivait :

```java
.sorted(Comparator.comparing((RouteStop s) -> s.priority().ordinal())
        .thenComparing(s -> staleness(s, now), Comparator.reverseOrder()))
```

Aucune position n'entrait dans le calcul — les points n'en avaient d'ailleurs aucune avant l'ADR-0016. Le résultat était **une liste de priorités, pas une tournée** : deux points voisins pouvaient se retrouver aux deux extrémités du parcours et le camion traverser la commune en zigzag. `RouteStop` n'exposait pas non plus de coordonnées, donc aucun client ne pouvait tracer le trajet.

## Décision

**L'urgence découpe la tournée en tranches ; la géographie n'ordonne qu'à l'intérieur d'une tranche.**

1. Les points sont répartis par `StopPriority` (`DEBORDEMENT`, `ETAT_INCONNU`, `A_SURVEILLER`, `RIEN_A_FAIRE`), comme avant.
2. Dans chaque tranche, l'ordre suit le **plus proche voisin** (haversine, `GeoDistance`), en repartant du dernier point de la tranche précédente — sinon la tournée se téléporterait à chaque changement d'urgence.
3. `RouteStop` expose `latitude` / `longitude`, pour que le client puisse tracer le parcours.

**Pourquoi l'urgence garde la priorité absolue.** Un débordement à l'autre bout de la commune passe avant un point tiède qu'on a sous la main : une tournée optimisée qui laisse déborder n'a aucun sens. C'est le produit qui le dit, pas l'algorithme.

**Pourquoi le plus proche voisin et pas une optimisation réelle.** Le problème exact (voyageur de commerce) est NP-difficile, et le gain d'une solution optimale sur une heuristique gloutonne est de l'ordre de 10–15 % — sur des distances à vol d'oiseau, dans des rues dont on ignore le sens et l'état. Ce serait une précision fausse. Le plus proche voisin supprime le zigzag, ce qui est tout le gain disponible sans données routières.

**Pourquoi haversine et pas une distance ellipsoïdale.** À l'échelle d'une commune, l'écart se compte en mètres. On cherche un **ordre**, pas une distance.

### Le garde-fou anti-famine, et où il agit réellement

Le plus proche voisin, seul, affame les points isolés : il y a toujours quelqu'un de plus près. Un point dont l'information dépasse `sonaged.routing.max-staleness-hours` (72 h par défaut) repasse donc **en tête de sa tranche**, avant tout chaînage.

Il vaut la peine de dire **où** ce garde-fou mord, parce que ce n'est pas là où on l'attend. Au-delà de la validité d'une mesure (24 h), un point quitte de toute façon `DEBORDEMENT` ou `A_SURVEILLER` pour `ETAT_INCONNU` : la reclassification fait déjà une partie du travail. La famine ne peut donc s'installer que **dans `ETAT_INCONNU`**, où tous les points sont périmés et où seule la distance les départagerait. C'est là que le rattrapage des délaissés opère. Un test le formule explicitement, après qu'une première rédaction eut confondu les deux mécanismes.

### Points sans position

Ils ne peuvent pas être chaînés ; ils **ferment leur tranche**, par ancienneté décroissante. Les exclure les rendrait invisibles — et c'est exactement le genre de point qu'on finit par ne jamais collecter.

## Conséquences

- **+** La tournée devient un parcours et non une liste : le zigzag disparaît à l'intérieur de chaque niveau d'urgence.
- **+** Le client peut tracer le trajet (coordonnées exposées).
- **+** Les trois règles d'origine sont préservées, y compris la protection contre la famine — déplacée là où elle mord vraiment.
- **−** `RouteStop` gagne deux composants : signature modifiée, appelants à recompiler.
- **−** Distance à vol d'oiseau : deux points séparés par un canal ou une voie ferrée paraissent voisins. Corriger cela demanderait un graphe routier — hors de portée et probablement hors de propos ici.
- **−** Le plus proche voisin dépend du point de départ. La première tranche part du point le plus ancien, faute de mieux : à défaut de géographie, l'attente fait foi. Un vrai point de départ (dépôt du camion, position du véhicule) serait plus juste — `vehicle.lastlatitude` / `lastlongitude` existent déjà et rendraient ce raffinement facile.

## Alternatives considérées

- **Trier globalement par distance, urgence en second** : rejeté — laisse déborder des points pour économiser des kilomètres.
- **Optimisation exacte (TSP) ou 2-opt** : rejeté — coût sans rapport avec le gain sur des distances à vol d'oiseau ; précision fausse.
- **Distance routière via un service externe** (OSRM, Google Directions) : rejeté à ce stade — dépendance réseau, clé à gérer, coût par requête, et le produit n'a pas encore livré une seule tournée réelle. À reconsidérer quand l'usage sera établi.
- **Ignorer la famine** : rejeté — c'est le défaut qui fait abandonner ce type d'outil, et le service s'en prémunissait déjà avant cette évolution. La régression aurait été silencieuse.
