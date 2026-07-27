# ADR-0013 — Architecture DDD `sn.smartwaste.collect` : 8 contextes, Clean Architecture, SaaS multi-tenant

- Statut : **Accepté** — **remplace [ADR-0010](0010-evolution-microservices-monolithe-modulaire.md)** (découpage en 5 modules) et **précise [ADR-0012](0012-decouplage-entites-references-par-id.md)**
- Date : 2026-07-27
- Priorité : structurant (cadre P1-7, P2-3, et l'ingestion IoT d'ADR-0004)

## Contexte

L'ADR-0010 actait un monolithe modulaire en **5 modules** sous `sonaged.ucg`, avec un découpage
interne **par couches** hérité de `sonaged.collecte.master`. Deux migrations ont été menées sur
cette base (`supervision`, `communication`).

Le cadrage produit a depuis évolué : le système est positionné comme un **SaaS multi-collectivités**
(« SmartWaste »), avec facturation par abonnement, isolation par organisation, et une chaîne IoT
(MQTT / LoRaWAN) destinée à monter en charge indépendamment. Trois manques de l'ADR-0010 deviennent
alors bloquants :

1. **Aucun contexte « tenant »** — l'appartenance des données à une collectivité n'existe nulle part,
   alors que c'est la clé d'isolation de tout le SaaS. L'ADR-0008 renvoyait le multi-tenant à
   « plus tard » ; or l'ajouter après coup obligerait à reprendre chaque agrégat.
2. **`collecte` était un module fourre-tout** (points, alertes, circuits, véhicules) doublé de
   sous-domaines internes non matérialisés — une frontière qu'on ne peut pas extraire.
3. **Le découpage par couches ne porte aucune information métier** : `controller/`, `service/`,
   `repository/` décrivent la technique, pas le domaine. Il rend invisible la logique de domaine et
   couple celle-ci à JPA et à Spring MVC, ce qui interdit l'extraction ultérieure.

## Décision

### 1. Racine et contextes

Nouvelle racine **`sn.smartwaste.collect`**, organisée en **8 contextes bornés** :

| Contexte | Responsabilité | Service futur |
|---|---|---|
| `identity` | utilisateurs, rôles, permissions, JWT | `identity-service` |
| `tenant` | organisations, clients SaaS, abonnements, configuration | `tenant-service` |
| `territory` | régions, départements, communes, quartiers, géométries | `territory-service` |
| `waste` | points de collecte, bacs, opérations, tournées, véhicules, alertes métier | `waste-service` |
| `iot` | capteurs, devices, MQTT, LoRaWAN, télémétrie, événements device | `iot-service` |
| `platform` | notifications, e-mails, SSE, journal d'audit, intégrations externes | `platform-service` |
| `analytics` | tableaux de bord, statistiques, rapports (read-side) | `analytics-service` |
| `shared` | noyau partagé (module ouvert) — pas un contexte |

`config` regroupe l'amorçage applicatif ; ce n'est pas non plus un contexte borné.

**8 et non davantage** : le découpage vise de **gros domaines métier extractibles**, pas des
micro-modules. L'ADR-0010 avait consolidé 9 modules en 5 par crainte de la granularité ; on remonte
à 8 parce que `tenant` et `iot` sont des frontières d'extraction et de scalabilité réelles
(l'ingestion IoT est le premier candidat à l'extraction), et parce que `waste` regroupe désormais
ce qui était éclaté.

### 2. Structure interne — Clean Architecture

Chaque contexte suit le même découpage, **par responsabilité et non par couche technique** :

```
<contexte>/
├── domain/          model, valueobject, event, repository (interfaces), service, exception
├── application/     usecase, command, query, dto, mapper
├── infrastructure/  persistence, repository (impl), messaging, mqtt, security, configuration
└── presentation/    controller, request, response, exception
```

Le sens des dépendances est **toujours vers l'intérieur** : `presentation → application → domain`,
`infrastructure → domain`. Le domaine ne connaît ni Spring MVC, ni JPA, ni le broker.

> **Conséquence Spring Modulith** : tout sous-package d'un module est **interne**. Aucune de ces
> couches n'est visible depuis un autre contexte sans `@NamedInterface` explicite. L'encapsulation
> devient effective et non déclarative.

### 3. Communication inter-contexte

Interdits : accès à un repository d'un autre contexte, association JPA cross-contexte, dépendance
sur un package interne, logique métier dans un contrôleur.

Autorisés : **interfaces publiques** exposées (`@NamedInterface`), **services applicatifs**, et
surtout **événements de domaine**. La chaîne cible :

```
Sensor ──TelemetryReceived──▶ waste ──AlertRaised──▶ platform (notification)
```

Les événements portent des **charges utiles autonomes** (jamais d'entité JPA), ce qui rend le
passage à un broker mécanique. Deux événements existent déjà sur ce modèle :
`ActivationCodeIssued` (identity → platform) et `AlertRaisedEvent` (waste → platform/SSE).

### 4. Fondations SaaS multi-tenant

L'architecture doit **porter** l'isolation avant qu'elle ne soit exploitée : contexte de tenant
propagé par requête, propriété des données par organisation, auditabilité, RBAC, et une notion
d'abonnement **découplée de tout fournisseur de facturation** (aucune dépendance Stripe/Paddle dans
le domaine — un port, une implémentation d'infrastructure).

### 5. Stratégie de migration

Migration **progressive, un contexte à la fois**, build vert après chaque étape. Pendant la
transition, `sonaged.**` et `sn.smartwaste.collect.**` coexistent et sont tous deux scannés par
`SonagedApplication`. Le code hérité a le droit de dépendre des nouveaux modules ; l'inverse doit
rester exceptionnel et temporaire.

## Conséquences

- **+** Frontières alignées sur le métier et sur les axes de scalabilité (IoT, tenant).
- **+** Domaine isolé de l'infrastructure → testable sans base ni broker.
- **+** Extraction en microservices réduite à : remplacer l'appel in-process par du réseau et
  l'événement interne par un message de broker.
- **−** Refactoring **massif** (~187 classes) et arborescence plus profonde.
- **−** `@NamedInterface` devient obligatoire pour toute exposition : plus verbeux, volontairement.
- **−** ⚠️ **Réalisé sans filet de test** : la couverture métier est quasi nulle (cf. journal
  d'implémentation — les tests d'`AuthorityServiceImplTest` sont vides) et l'application n'a jamais
  été démarrée contre une base. Le compilateur et Spring Modulith sont les seuls garde-fous ; une
  régression de câblage Spring ne serait visible qu'au démarrage.

## Alternatives considérées

- **Conserver les 5 modules d'ADR-0010** : rejeté — pas de contexte `tenant`, `collecte`
  fourre-tout, et l'IoT non extractible.
- **Garder le découpage par couches à l'intérieur des modules** : rejeté — ne dit rien du métier et
  maintient le couplage du domaine à JPA/Spring MVC.
- **Passer directement aux microservices** : rejeté (inchangé depuis ADR-0010) — surcoût
  opérationnel sans bénéfice tant que le cœur métier n'est pas stabilisé.
