# ADR-0024 — Événements d'affectation, notifications et acyclicité des modules

- Statut : **Proposé** — cadrage seul, aucune ligne de code écrite. Volet « notification et
  architecture » du triptyque ouvert par
  [ADR-0022](0022-groupes-de-collecte-zone-et-rotation.md).
- Date : 2026-08-11
- Priorité : **G6 étendu**. Prolonge [ADR-0007](0007-notifications-temps-reel.md) (SSE) et
  [ADR-0012](0012-decouplage-entites-references-par-id.md) (références par identifiant).

## Contexte

La demande ajoute « notification » aux affectations : prévenir quand un véhicule change de groupe,
quand un chauffeur est affecté, quand une désaffectation intervient. Elle demande explicitement
d'**éviter les références circulaires**.

Ce n'est pas une précaution de principe. Le graphe de dépendances entre modules, relevé sur le code
actuel, contient une arête qui rend la question urgente.

### Le graphe réel

Mesuré par les imports croisés, contexte par contexte :

| Module | Dépend de |
|---|---|
| `identity` | **rien** |
| `tenant` | `identity` |
| `territory` | `tenant` |
| `waste` | `identity`, `tenant`, `territory` |
| `platform` | `identity`, **`waste`** |
| `iot` | `tenant`, `waste` |
| `analytics` | `iot`, `platform`, `territory`, `waste` |
| `administration` | `identity`, `territory`, `waste` |
| `shared` | **rien** (ne dépend que de lui-même) |

L'ordre topologique existe — `identity → tenant → territory → waste → {platform, iot} → analytics` —
le graphe est donc acyclique aujourd'hui, et `SmartWasteModularityTests` le vérifie à chaque build.

### La conséquence directe

**`platform → waste` existe déjà.** Donc `waste` ne peut appeler aucun service de `platform` :
la moindre injection d'un `NotificationService` dans un service d'affectation fermerait le cycle
`waste ↔ platform`, et `modules.verify()` casserait le build.

Le réflexe naturel — « le service d'affectation appelle le service de notification » — est donc
interdit ici, pas par convention mais par la structure existante.

### Le précédent qui règle la question

Le système sait déjà faire. `waste.AlertServiceImpl` publie `AlertRaisedEvent` ;
`platform.AlertBroadcaster` l'écoute et diffuse en SSE. Aucun appel de `waste` vers `platform` :
l'événement vit dans `shared.domain.event`, qui ne dépend de rien.

Mieux, `AlertRaisedEvent` documente déjà la règle de charge utile qui rend ce découplage
tenable — le code d'alerte y est transporté **en `String` et non en énumération**, « le shared
kernel n'a pas à connaître le vocabulaire métier du contexte Déchets ».

## Décision

### 1. Les affectations notifient par événement de domaine, jamais par appel direct

Tout changement d'affectation publie un événement dans `shared.domain.event`. `waste` publie ;
`platform` écoute et choisit le canal (SSE, e-mail, push). `waste` ignore qui écoute, et n'acquiert
aucune dépendance nouvelle.

Catalogue proposé, aligné sur ADR-0023 :

| Événement | Émis quand |
|---|---|
| `VehicleAssignedToGroup` | une période véhicule↔groupe s'ouvre |
| `VehicleUnassignedFromGroup` | une période véhicule↔groupe se clôt |
| `DriverAssignedToVehicle` | une période chauffeur↔véhicule s'ouvre |
| `DriverUnassignedFromVehicle` | une période chauffeur↔véhicule se clôt |
| `CollectionGroupZoneChanged` | la zone d'un groupe change |
| `RotationPlanned` / `RotationEnded` | une période de rotation s'ouvre / se clôt |

Un **transfert** (ADR-0023 §3) émet la paire *clôture puis ouverture*, dans cet ordre : les abonnés
n'ont pas à deviner qu'une affectation en a remplacé une autre.

### 2. Les événements portent un instantané suffisant, pas seulement des identifiants

Un événement réduit à `(vehicleId, groupId)` obligerait `platform` à relire `waste` pour composer
« Véhicule DK-1234-AB affecté au groupe Thiaroye Nord ». Deux défauts : cela renforce l'arête
`platform → waste`, et surtout **la relecture ne donne pas l'état d'alors** — au moment où la
notification part, une seconde affectation a pu passer.

Les événements transportent donc les libellés nécessaires au message : immatriculation, nom du
groupe, nom de la personne, horodatage, motif de fin. Exactement la règle qu'`AlertRaisedEvent`
applique déjà avec son `RaisedAlert`.

Contrainte de forme, héritée du même précédent : **types primitifs, `UUID`, `String`, `Instant` et
records du shared kernel uniquement**. Aucune entité, aucun DTO, aucune énumération d'un contexte
métier. Un `CircuitShift` transporté tel quel ferait dépendre `shared` de `waste` et détruirait la
propriété qui fait tout marcher — `shared` ne dépend de rien.

### 3. L'habilitation territoriale devient une projection, alimentée dans le sens `waste → identity`

ADR-0022 §5 pose le problème : l'appartenance à un groupe (planification, dans `waste`) et la
couverture territoriale d'un agent (autorisation, dans `identity` via `AgentAssignment` /
`AgentDirectory`) décrivent le même terrain et peuvent diverger.

Faire lire le groupe par `identity` créerait `identity → waste`. Or `identity` **ne dépend de rien
aujourd'hui** : c'est la racine du graphe, et cette position est ce qui permet à tous les autres de
s'appuyer dessus. L'arête créerait immédiatement le cycle `waste → identity → waste`.

`identity` **écoute** donc `CollectionGroupZoneChanged` et les événements d'appartenance, et met à
jour `AgentAssignment` comme une **projection**. Écouter un record de `shared` ne crée aucune arête
vers `waste` — c'est précisément la raison d'être du shared kernel.

Deux conséquences assumées :

- **Cohérence à terme.** Entre le changement de groupe et la mise à jour de l'habilitation, un agent
  peut rester habilité sur son ancienne zone. La fenêtre est celle d'un écouteur synchrone, soit la
  même transaction (convention du projet, cf. `DefaultOrganizationEnrollmentListener` et
  `NotificationServiceImpl` : « la création du compte et son rattachement réussissent ou échouent
  ensemble »). Le risque est donc théorique tant que l'écoute reste synchrone — et devient réel le
  jour où elle passerait en asynchrone, ce qui devra être une décision consciente.
- `AgentAssignment` **cesse d'être saisi à la main** et devient dérivé. Sa migration vers le modèle
  daté d'ADR-0023 (§5) et ce basculement en projection forment le lot 6 du plan, sous validation
  explicite.

### 4. Aucune référence circulaire entre beans non plus

`spring.main.allow-circular-references=false` depuis 2026-07-30 (ADR-0009 §4), et
`ApplicationContextLoadsTest` démarre le contexte réel : un cycle de beans réintroduit casse le
build.

Le service d'affectation n'injecte donc **ni** un service de notification, **ni** le service de
groupe qui l'appellerait en retour. Il publie via `ApplicationEventPublisher`, comme
`AlertServiceImpl`. La règle est double : pas de cycle **entre modules** (§1), pas de cycle **entre
beans** à l'intérieur d'un module.

### 5. Ce qui reste dans `waste`, ce qui part dans `platform`

Frontière explicite, pour éviter que la notification ne remonte du mauvais côté :

- `waste` décide **qu'un fait a eu lieu** et le publie. Il ne connaît ni destinataire, ni canal,
  ni libellé final.
- `platform` décide **qui prévenir et comment** : superviseur du groupe, chauffeur concerné,
  administrateur de la collectivité ; SSE, e-mail, push. Il résout l'adresse via `UserDirectory`
  (port déjà publié par `identity`, déjà consommé par `tenant`).

Le cloisonnement multi-tenant s'applique à la diffusion : une notification ne franchit pas la
frontière d'une `organizationId` (ADR-0020). L'`organizationId` figure donc dans chaque événement.

> ⚠️ Le filtre de cloisonnement est aujourd'hui **inopérant** — mesuré le 2026-08-11 : un ADMIN
> d'une autre collectivité voit 100 % des données de Pikine. Tant que ce défaut n'est pas corrigé,
> l'`organizationId` transporté ici est une intention, pas une garantie. À traiter avant le lot 5.

## Conséquences

**Positives.**
- Le graphe de modules reste acyclique sans exception ni dérogation à déclarer.
- `waste` n'apprend rien de `platform` : ajouter un canal (push FCM, SMS) ne touche pas au métier.
- Les notifications restent justes même sous réaffectation rapide, l'instantané étant capturé à
  l'émission.
- `identity` conserve sa position de racine du graphe, ce dont dépend tout le reste.

**Coûts et risques.**
- Les événements dupliquent des libellés (immatriculation, nom de groupe). Duplication **voulue**,
  bornée à la charge utile, et déjà pratiquée par `AlertRaisedEvent`.
- Un événement de plus par geste d'affectation : le catalogue de `shared.domain.event` passe de 8 à
  14 records. Il reste lisible ; au-delà, il faudra le regrouper par contexte émetteur.
- L'écoute synchrone lie la réussite de l'affectation à celle de la notification. C'est la
  convention en vigueur dans le projet ; si un canal lent (e-mail, push) devait la mettre en défaut,
  la parade est `@ApplicationModuleListener` (asynchrone, transactionnel) — au prix de la cohérence
  immédiate décrite en §3.
- La projection d'habilitation introduit un chemin d'écriture non manuel sur `AgentAssignment` :
  une reprise de données mal ordonnée pourrait la vider. Le lot 6 doit prévoir une reconstruction
  complète, pas seulement l'écoute incrémentale.

## Alternatives écartées

**Appel direct `waste → platform.NotificationService`.** Ferme le cycle `waste ↔ platform`,
`modules.verify()` casse le build. Écarté par la structure, pas par préférence.

**Inverser l'arête : `platform` interroge `waste` périodiquement.** Remplace un cycle par du
sondage, retarde la notification, et ne capte pas les états transitoires (une affectation ouverte et
close entre deux sondages est invisible).

**Événements réduits aux seuls identifiants.** Force `platform` à relire `waste`, renforce l'arête
et expose au décalage décrit en §2. Écarté pour la même raison qu'`AlertRaisedEvent` l'avait déjà
été.

**Un module `notification` distinct.** N'ajoute qu'un nœud : le problème n'est pas où vit le canal,
mais dans quel sens va l'arête. `platform` remplit déjà ce rôle et son `package-info` le dit —
« notifications sortantes […] pilotées par les événements des autres contextes ».

**Supprimer `AgentAssignment` et faire porter l'habilitation par le groupe.** Ferait dépendre
l'autorisation de `waste`, alors que `TerritorialAccessGuard` (dans `waste`) l'interroge déjà :
`waste → identity → waste`. C'est le cycle que §3 évite.
