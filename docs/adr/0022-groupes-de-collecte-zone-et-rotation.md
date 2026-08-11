# ADR-0022 — Groupes de collecte : équipe, zone et rotation

- Statut : **Proposé** — cadrage seul, aucune ligne de code écrite. Ouvre le triptyque
  [ADR-0022](0022-groupes-de-collecte-zone-et-rotation.md) (le groupe) /
  [ADR-0023](0023-affectations-datees-et-desaffectation.md) (les affectations) /
  [ADR-0024](0024-evenements-d-affectation-et-acyclicite.md) (notifications et acyclicité).
- Date : 2026-08-11
- Priorité : **G6 étendu** (`docs/BACKLOG_FONCTIONNEL.md`) — G6 ne couvrait que le balayage ;
  la présente décision porte sur la **collecte**, restée sans organisation humaine du tout.

## Contexte

### Ce que le modèle porte aujourd'hui

Le constat est mesuré sur la base réelle, pas déduit du code :

| Élément | État constaté |
|---|---|
| `Vehicle` | Existe (`registration`, `label`, `active`, position). **0 ligne en base.** |
| `Vehicle.circuitCollectId` | Colonne unique et mutable — l'affectation d'un véhicule à un circuit |
| Groupe / équipe de collecte | **N'existe pas** |
| Chauffeur | **N'existe pas** — aucun lien personne ↔ véhicule |
| `CircuitCollect.rotation` | `String`, valeur `"R1"` sur **les 52 circuits** — champ dégénéré |
| `CircuitCollect.sectection` | `String`, 7 valeurs qui sont des **noms de communes** (Thiaroye, Mbao, Dalifort…) — un rattachement territorial en texte libre, doublon de `communeId` |
| Rôles `AGENT`, `SUPERVISEUR` | Semés et attribuables, mais **aucune règle opérationnelle ne s'y adosse** |
| `CircuitShift` | Enum `MATIN` / `SOIR` / `NUIT` déjà défini, porté par le balayage seul |

### Ce que le métier décrit

Les notes de terrain UCG relevées en G6 — « **CHAQUE CIRCUIT A UN SUPERVISEUR** / **CHAQUE SUP A
25 AGENTS** / RESPONSABLE DE LA COLLECTE / **UN BALAYAGE MATIN, SOIR ET NUIT** » — décrivent une
organisation par **équipes encadrées**, affectées à un territoire et à une plage horaire. Le
modèle actuel ne sait exprimer aucun des trois termes.

G6 posait « **préalable : cadrage métier**, ne pas modéliser sans validation UCG ». Ce préalable
est levé sur le périmètre *collecte* par la demande du 2026-08-11, qui énonce explicitement les
objets attendus : groupes, affectation de véhicule à un groupe, zone du groupe et du véhicule,
chauffeur affecté à un véhicule, historiques de rotation et d'affectation, notification,
désaffectation. Les points **encore ouverts** sont listés en §6 et ne bloquent pas la structure.

### Pourquoi ce n'est pas un simple CRUD de plus

Trois questions que le modèle actuel ne peut pas honorer, et qui ne sont pas des raffinements :

1. « Quel véhicule était sur le circuit 12 mardi dernier ? » — `Vehicle.circuitCollectId` ne
   porte que l'état courant ; la réponse d'hier a été écrasée.
2. « Qui conduisait ce véhicule au moment de l'incident ? » — la question n'a aucun support.
3. « Ce groupe a-t-il couvert sa zone sur la plage du matin ? » — ni groupe, ni plage côté collecte.

Ce sont des questions d'**exploitation** et de **responsabilité**, pas de confort d'écran.

## Décision

### 1. Le groupe de collecte est un agrégat du contexte `waste`, pas un nouveau module

`CollectionGroup` (« groupe de collecte ») rejoint `sn.smartwaste.collect.waste.domain.model`,
aux côtés de `Vehicle` et `CircuitCollectEntity`.

**Pourquoi pas un nouveau contexte `fleet` ou `operations`.** Un module supplémentaire devrait
dépendre de `waste` (les circuits), d'`identity` (les personnes) et de `territory` (les communes) —
soit trois arêtes nouvelles dans le graphe de modules pour n'isoler aucune règle qui ne soit déjà
du domaine déchet. `waste` référence **déjà** `userId`, `communeId` et `quartierId` par identifiant
(ADR-0012, `waste/package-info.java`) : le groupe n'introduit donc **aucune dépendance nouvelle
entre contextes**. Le décompte de Spring Modulith reste à 10 modules, et
`SmartWasteModularityTests` n'a rien de nouveau à autoriser.

**Composition.** Le groupe porte un nom, un code, l'`organizationId` de cloisonnement (ADR-0020),
un superviseur (`supervisorUserId`) et des membres (`userId`). Toutes les références de personne
sont **des identifiants nus** : jamais d'association JPA vers `UserEntity`, qui appartient à
`identity` et qu'une association ferait franchir à une clé étrangère (`modules.verify()` échouerait,
et ADR-0012 l'interdit explicitement).

### 2. La zone est portée par le groupe et **dérivée** pour le véhicule

La demande énonce « zone de collecte du groupe **et du véhicule** ». La décision est de **ne pas
stocker de zone sur le véhicule**.

Une zone posée des deux côtés est une zone qui se contredit : rien n'empêcherait un véhicule d'être
déclaré sur Mbao alors que son groupe travaille Thiaroye, et aucune des deux valeurs ne serait plus
légitime que l'autre. La zone d'un véhicule est donc **une question, pas une colonne** : c'est la
zone du groupe auquel il est affecté à l'instant considéré (ADR-0023). Un véhicule sans groupe n'a
pas de zone — et c'est l'information juste, pas une lacune à combler.

**Ce qu'est une zone.** Un ensemble de `circuitCollectId`. Le circuit est déjà l'unité de travail
réelle (52 en base, avec géométrie et `communeId` posés par ADR-0016 / ADR-0018) ; une zone en
communes serait plus grossière que le terrain et obligerait à redécouper. La commune reste
**dérivable** du circuit, ce qui suffit aux écrans de synthèse et à l'habilitation (ADR-0024 §3).

**Conséquence sur `sectection`.** Le champ texte qui duplique le nom de commune devient
redondant avec `CircuitCollectEntity.communeId`. Il n'est **pas supprimé par cette décision** —
toute suppression exige une validation explicite (accord de travail du projet) — mais il est
déclaré *déprécié* : aucune règle nouvelle ne doit s'y adosser.

### 3. `Vehicle.circuitCollectId` est remplacé, pas complété

Le véhicule cesse d'être affecté directement à un circuit. Il est affecté à un **groupe**, et c'est
le groupe qui porte la zone. La chaîne devient :

```
Chauffeur ──affecté à──▶ Véhicule ──affecté à──▶ Groupe ──couvre──▶ Zone (circuits)
```

Un seul chemin relie une personne à un territoire. Conserver en parallèle l'ancien lien direct
véhicule → circuit offrirait un second chemin, libre de diverger du premier : c'est exactement le
défaut que §2 refuse pour la zone.

La colonne `circuitCollectId` survit en base le temps de la migration (ADR-0023 §5) puis est
retirée par un changeset dédié, `Vehicle` étant à ce jour **vide de toute donnée** — la migration
ne transporte rien.

### 4. La rotation est un **tour de service planifié**, pas une étiquette

`CircuitCollect.rotation` vaut `"R1"` sur les 52 circuits : le champ ne distingue rien et ne peut
donc rien historiser. Il est déclaré déprécié au même titre que `sectection`.

La rotation devient un objet à part entière : **quel groupe travaille quelle zone sur quelle plage,
pendant quelle période.** Elle réutilise `CircuitShift` (`MATIN` / `SOIR` / `NUIT`), déjà défini et
aujourd'hui limité au balayage — l'enum est levé au rang de vocabulaire commun aux deux métiers
plutôt que dupliqué.

« L'historique des groupes de rotation » demandé est alors la **suite des périodes de rotation
révolues**, obtenue par le même mécanisme daté que les affectations (ADR-0023), et non par une
table d'archive séparée qu'il faudrait maintenir cohérente avec la table vivante.

### 5. Le groupe ne remplace pas l'habilitation, il l'alimente

`identity` porte déjà `AgentAssignment` (agent ↔ commune) et le publie via `AgentDirectory`,
consommé par `waste.TerritorialAccessGuard` pour vérifier qu'un agent déclare un passage sur une
commune qu'il couvre. C'est une décision d'**autorisation**, et elle reste dans `identity`.

L'appartenance à un groupe est une décision de **planification**, et elle reste dans `waste`.

Ces deux faits doivent cesser de diverger : un agent déplacé de groupe conserverait sinon
l'habilitation de son ancienne zone. La synchronisation se fait **par événement de domaine**, sens
`waste → identity` uniquement — le mécanisme, et la démonstration qu'il n'introduit aucun cycle,
sont traités en [ADR-0024](0024-evenements-d-affectation-et-acyclicite.md) §3.

### 6. Ce qui reste à trancher avec le métier

Ces points **n'empêchent pas** de poser la structure ci-dessus ; ils en fixent les cardinalités et
doivent être confirmés avant l'implémentation :

1. Un groupe conduit-il **un seul** véhicule à la fois, ou plusieurs ? (la structure supporte N ;
   la contrainte, elle, est une règle métier)
2. Une personne peut-elle appartenir à **deux groupes** simultanément (renfort, remplacement) ?
3. Un groupe couvre-t-il **une seule** zone à la fois, ou plusieurs circuits disjoints ?
4. « Rotation » désigne-t-il le **tour de service** (retenu ici) ou le **voyage aller-retour à la
   décharge** ? Les deux sens ont cours dans le métier des déchets ; la décision retenue est le
   premier, parce que c'est celui qui rend « historique des groupes de rotation » signifiant.
5. **52 circuits en base contre 66 dans les notes UCG** — écart déjà relevé en G6, non tranché.
   Toute règle « chaque circuit a un superviseur » se heurtera à cet écart.

## Conséquences

**Positives.**
- Les trois questions d'exploitation du §Contexte deviennent répondables.
- Aucune arête nouvelle dans le graphe de modules ; le décompte Modulith reste à 10.
- `CircuitShift`, `AgentDirectory`, `organizationId` et le générateur UUID v7 sont réutilisés :
  la décision consomme l'existant plutôt que de le doubler.
- Deux champs texte dégénérés (`rotation`, `sectection`) sont explicitement dépréciés au lieu de
  continuer à ressembler à des données.

**Coûts et risques.**
- La zone du véhicule devient une **jointure** et non une lecture de colonne : les écrans carte et
  la liste de flotte doivent la résoudre. Un modèle de lecture publié par `waste` est prévu à cet
  effet (ADR-0023 §4).
- `Vehicle.circuitCollectId` disparaît : tout consommateur futur doit passer par le groupe.
  Aucun consommateur actuel n'existe (0 véhicule en base), le coût est donc payé maintenant.
- La cohérence groupe ↔ habilitation repose sur un événement, donc sur une **cohérence à terme**
  et non immédiate. Conséquence assumée et bornée en ADR-0024 §3.
- Les cardinalités du §6 non tranchées : les poser à tort obligerait à une migration de contraintes,
  pas à une refonte du modèle.

## Alternatives écartées

**Poser `groupId` directement sur `Vehicle` et `userId`.** C'est le modèle actuel étendu : une
colonne d'état courant. Il ne répond à aucune des trois questions d'historique, et la
désaffectation y est une perte d'information (voir ADR-0023 §1).

**Un contexte `fleet` dédié.** Trois arêtes nouvelles entre modules pour isoler des règles qui sont
du domaine déchet. Le bénéfice d'extraction ultérieure ne compense pas, d'autant que `Vehicle` vit
déjà dans `waste`.

**Porter la zone sur le véhicule *et* le groupe, avec une règle de cohérence.** Deux sources pour un
même fait, plus un invariant à faire respecter partout. La dérivation supprime le problème au lieu
de le surveiller.

**Une table d'archive `rotation_history` séparée.** Duplique la donnée vivante et exige de garder
les deux cohérentes — c'est-à-dire le défaut que la modélisation datée d'ADR-0023 rend impossible
par construction.
