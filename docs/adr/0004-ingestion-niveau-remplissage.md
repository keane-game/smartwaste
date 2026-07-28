# ADR-0004 — Architecture d'ingestion du niveau de remplissage (cœur IoT)

- Statut : **Accepté — NON implémenté**. C'est le cœur du produit et il n'existe pas.
> ⚠️ **Périmètre à revoir** : le mémoire (cas d'usage administrateur) demande de configurer des seuils de **température et d'humidité** en plus du remplissage — le capteur DHT11 est prévu à cet effet. Cet ADR ne modélise que le remplissage. Un ADR-0004bis est nécessaire avant implémentation.
- Date : 2026-07-11
- Priorité : P0-5, P0-6

## Contexte

La raison d'être du produit — « détecter le niveau de remplissage » et « envoyer des alertes automatiques » — **n'existe pas** dans le code : aucune occurrence de `niveau/remplissage/capteur/seuil/sensor/threshold`. `DepotoirEntity` (point de collecte) n'a pas de champ de remplissage ; `AlertEntity` est un CRUD manuel non relié à un dépotoir. Cible produit : plusieurs collectivités, **milliers de points**, intégration IoT, temps réel.

## Décision

Introduire une **chaîne d'ingestion découplée** en trois maillons :

1. **Mesure** — nouvelle entité `MeasurementEntity` (`id`, `depotoir` *→1*, `fillLevel` %, `measuredAt`, `source` {IOT, MANUAL}, `batteryLevel?`). `DepotoirEntity` reçoit `fillLevel` + `lastMeasuredAt` (dénormalisation du dernier état pour la carte/les listes).
2. **Ingestion** — endpoint `POST /v1/measurements` (auth par clé d'API device en en-tête, distincte du JWT utilisateur), validé, idempotent. Un service applique la mesure au dépotoir et publie un **événement applicatif** (`ApplicationEventPublisher`).
3. **Évaluation de seuil** — un `ThresholdEvaluator` écoute l'événement ; si `fillLevel ≥ seuil` (configurable par type de dépotoir), il crée une `Alert` reliée au dépotoir (cf. ADR-0005) et déclenche la notification.

**Transport** : REST/HTTP synchrone **pour la v1** (simple, testable, aligné sur les clients existants). L'usage d'événements applicatifs internes prépare une bascule ultérieure vers un **broker (MQTT/Kafka)** sans réécrire la logique métier, quand le volume l'exigera.

## Conséquences

- **+** La boucle « mesurer → seuiller → alerter » devient réelle et testable unitairement (evaluator isolé).
- **+** Le découplage par événements permet d'ajouter MQTT/Kafka plus tard sans toucher aux règles de seuil.
- **+** Le `fillLevel` dénormalisé rend la carte/les listes performantes (pas d'agrégation à la volée).
- **−** Nouvelle entité + endpoint + gestion de clés device à sécuriser et à versionner (Liquibase).
- **−** Risque de rafales d'écritures à l'échelle → prévoir index sur `(depotoir, measuredAt)` et, à terme, rétention/agrégation.

## Alternatives considérées

- **Stocker le remplissage uniquement sur `Depotoir`** (sans historique de mesures) : rejeté — perd l'historique nécessaire aux tableaux de bord et à l'analyse de tournées.
- **MQTT/broker dès la v1** : rejeté à court terme (surcoût d'infra) mais **anticipé** par le design événementiel.
- **Polling depuis le backend vers les capteurs** : rejeté (ne passe pas à l'échelle, dépendance réseau inversée).
