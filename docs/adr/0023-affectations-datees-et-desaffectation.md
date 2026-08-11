# ADR-0023 — Affectations datées : véhicule, chauffeur, rotation — et désaffectation

- Statut : **Proposé** — cadrage seul, aucune ligne de code écrite. Volet « affectations » du
  triptyque ouvert par [ADR-0022](0022-groupes-de-collecte-zone-et-rotation.md).
- Date : 2026-08-11
- Priorité : **G6 étendu**. Corrige au passage un défaut de conception d'`AgentAssignment`
  (identity), livré sous ADR-0020/G1.

## Contexte

La demande énumère quatre objets qui sont **le même objet** vu quatre fois :

| Demande | Ressource | Cible |
|---|---|---|
| affectation de véhicule de collecte à un groupe | véhicule | groupe |
| chauffeur affecté à tel véhicule | personne | véhicule |
| zone de collecte du groupe | groupe | circuits |
| groupes de rotation | groupe | zone + plage |

Elle énumère aussi, pour deux d'entre eux, un **historique**, et pour tous, une **désaffectation**.
Ces deux exigences ne sont pas des fonctionnalités supplémentaires : elles déterminent la forme du
modèle, et le modèle actuel les rend impossibles.

### Le défaut de forme : une clé étrangère n'a pas de passé

`Vehicle.circuitCollectId` est une colonne d'**état courant**. Trois conséquences, toutes vérifiables
sur le schéma tel qu'il est :

1. Réaffecter écrase. La valeur d'hier n'existe plus nulle part.
2. Désaffecter met à `null` — c'est-à-dire **efface le fait** qu'il y a eu une affectation.
   « Ce véhicule n'a jamais servi » et « ce véhicule a servi puis a été retiré » deviennent
   indiscernables.
3. Aucune date. Impossible de répondre « depuis quand », « jusqu'à quand », « qui, ce jour-là ».

Ajouter une table `*_history` alimentée par déclencheur ou par code applicatif ne corrige pas la
forme : elle crée une seconde source de vérité qu'il faut maintenir cohérente avec la première, et
qui diverge au premier chemin d'écriture oublié.

### Le précédent à ne pas reproduire : `AgentAssignment`

`identity.domain.model.AgentAssignment` (agent ↔ commune) est déjà une table d'affectation. Elle
n'a **aucune période de validité** : la désaffectation s'y fait par **suppression logique**
(`deletionStatus`, hérité d'`AbstractAuditingEntity`).

C'est un défaut, et il est mesurable. `DeletionPurgeScheduler` injecte **tous** les beans
`SoftDeleteRepository` et appelle `purgeExpired` chaque nuit à 03:00 ; la rétention par défaut est
`DELETION_RETENTION_DAYS:30`. Une affectation désaffectée est donc **physiquement détruite au bout
de 30 jours** — l'historique d'affectation s'auto-efface, silencieusement, sans que personne l'ait
décidé.

S'y ajoute une confusion de sens : `deletionStatus` répond à « cet enregistrement a-t-il été
supprimé ? », pas à « cette affectation est-elle encore en vigueur ? ». Une désaffectation est un
**fait métier normal**, pas une suppression ; les traiter par le même mécanisme rend indiscernables
une équipe réorganisée et une ligne saisie par erreur.

## Décision

### 1. Une affectation est une entité à période de validité, pas une clé étrangère

Toute affectation — véhicule↔groupe, chauffeur↔véhicule, groupe↔zone, rotation — est portée par un
enregistrement dédié qui décrit **un intervalle de temps** :

```
(organizationId, ressource, cible, effectiveFrom, effectiveTo, assignedBy, endedBy, endReason)
```

- `effectiveFrom` : instant de prise d'effet, jamais nul.
- `effectiveTo` : **nul tant que l'affectation court**. C'est la seule marque du « courant ».
- `assignedBy` / `endedBy` : `userId` de l'auteur — qui a décidé, pas seulement quand.
- `endReason` : motif de fin, énuméré (voir §3).

**« L'affectation courante » cesse d'être une colonne et devient une requête** : `effectiveTo IS
NULL`. L'historique n'est plus une table à part : c'est le **même jeu de lignes**, celles dont
`effectiveTo` est renseigné. Il n'y a donc rien à garder cohérent entre un état et son archive,
puisqu'il n'y a qu'une table.

### 2. La désaffectation clôt une période, elle ne supprime jamais une ligne

Désaffecter = poser `effectiveTo`, `endedBy` et `endReason` sur la ligne ouverte. Aucun `DELETE`,
aucune suppression logique.

**Conséquence directe et voulue** : ces entités **n'étendent pas** le mécanisme de suppression
logique et leurs repositories **n'implémentent pas** `SoftDeleteRepository`. Elles échappent donc
par construction à `DeletionPurgeScheduler` — le piège des 30 jours décrit au §Contexte ne peut pas
se refermer sur l'historique.

Une saisie erronée relève d'un autre geste : une **correction**, tracée comme telle
(`endReason = SAISIE_ERRONEE`), pas d'un effacement. L'audit reste lisible.

### 3. Invariants, et le seul qui compte vraiment

**Non-recouvrement.** Pour une ressource donnée, deux périodes ouvertes simultanément sont
interdites : un chauffeur ne conduit pas deux véhicules à la même heure, un véhicule n'appartient
pas à deux groupes. La contrainte est posée **en base** — index unique partiel sur
`(ressource, organizationId) WHERE effectiveTo IS NULL` — et pas seulement en Java : un contrôle
applicatif seul se fait battre par deux requêtes concurrentes, et c'est précisément le genre
d'incohérence qu'un historique rend visible pour toujours.

Affecter une ressource déjà affectée ne lève donc pas une erreur : c'est un **transfert**, qui clôt
la période en cours (`endReason = TRANSFERT`) et en ouvre une nouvelle dans la même transaction.
C'est le geste réel du terrain — on ne désaffecte pas un chauffeur pour le réaffecter dix secondes
plus tard.

**Motifs de fin** (`endReason`) : `TRANSFERT`, `FIN_DE_MISSION`, `MAINTENANCE`, `INDISPONIBILITE`,
`SAISIE_ERRONEE`. Énumérés plutôt que libres, parce qu'ils servent à filtrer les rapports.

**Antidatage.** `effectiveFrom` peut être antérieur à maintenant (une affectation constatée après
coup), mais jamais postérieur : une affectation future est une **planification**, objet distinct
qu'on n'introduit pas ici.

### 4. La zone du véhicule est un modèle de lecture publié, pas une jointure recopiée partout

ADR-0022 §2 fait de la zone d'un véhicule une dérivation : `véhicule → groupe courant → zone du
groupe`. Deux ou trois jointures que les écrans carte, flotte et supervision referaient chacun de
leur côté.

`waste` publie donc un port de lecture — dans `waste.application.api`, aux côtés de
`WasteReadModel`, `DepotoirMaps` et `CollectionPerformance` — qui répond aux questions composées :
zone courante d'un véhicule, véhicules d'un groupe, chauffeur courant d'un véhicule. Les autres
contextes (`analytics` pour le tableau de bord, `platform` pour libeller une notification) passent
par ce port, **jamais par les repositories de `waste`** : `modules.verify()` échoue sur un accès
direct, et c'est la règle du projet, pas une préférence.

### 5. Migration : rien à transporter

`Vehicle` compte **0 ligne** en base. La bascule ne migre donc aucune donnée :

1. Changeset créant les tables d'affectation et leurs index uniques partiels.
2. `Vehicle.circuitCollectId` **conservée mais inutilisée** le temps que le frontend bascule.
3. Changeset de retrait de la colonne, une fois plus aucun lecteur.

Le schéma appartient exclusivement à Liquibase (ADR-0001) ; Hibernate reste en `ddl-auto=validate`.

`AgentAssignment` n'est **pas** migrée par cette décision. Son alignement sur le même modèle daté
est justifié (même défaut, même purge à 30 jours) mais touche à `identity` et à une table déjà
peuplée : il fait l'objet d'un point de plan distinct (§Plan, lot 4) et d'une validation explicite,
conformément à l'accord de travail du projet.

## Plan d'exécution

Découpage en lots livrables séparément, chacun vérifiable contre PostgreSQL réel.

| Lot | Contenu | Dépend de | Taille |
|---|---|---|---|
| **1** | `CollectionGroup` + membres + zone (circuits). CRUD et écran de gestion. | — | M |
| **2** | Affectation datée **véhicule ↔ groupe** : modèle, invariant de non-recouvrement, transfert, désaffectation, historique consultable. | 1 | M |
| **3** | Affectation datée **chauffeur ↔ véhicule**. Même mécanique, cible différente. | 2 | S |
| **4** | Rotation : groupe × zone × `CircuitShift` × période. Historique par le même mécanisme. | 1 | M |
| **5** | Événements d'affectation + notifications (ADR-0024). | 2, 3 | S |
| **6** | Projection d'habilitation `waste → identity` (ADR-0024 §3) ; alignement d'`AgentAssignment` sur le modèle daté — **validation explicite requise**. | 5 | M |
| **7** | Écrans : flotte, groupes, rotation, historiques ; zone dérivée sur la carte. | 2, 3, 4 | L |

Les lots 1 à 3 forment le socle : ils rendent répondables les trois questions d'exploitation
d'ADR-0022. Les lots 4 à 7 sont indépendants entre eux.

## Conséquences

**Positives.**
- Historique et désaffectation cessent d'être des fonctionnalités à construire : ils **tombent** de
  la forme du modèle. Aucun code d'archivage à écrire ni à maintenir cohérent.
- L'historique est à l'abri de la purge par construction, pas par une exception à ne pas oublier.
- Le non-recouvrement est garanti en base, donc résistant à la concurrence.
- Qui a décidé quoi, et pourquoi, est tracé (`assignedBy` / `endedBy` / `endReason`).

**Coûts et risques.**
- Toute lecture de « l'état courant » porte un prédicat `effectiveTo IS NULL`. Oublié, il fait
  remonter l'historique entier — c'est le piège caractéristique de ce modèle. Il se neutralise en
  n'exposant **que** des méthodes de repository nommées explicitement (`findCurrent…`) plutôt qu'un
  `findAll` générique.
- Les tables croissent indéfiniment : elles n'ont pas vocation à être purgées. Volumétrie sans
  commune mesure avec celle des mesures capteurs, non préoccupante.
- Un index unique **partiel** est une construction PostgreSQL ; elle lie un peu plus le schéma au
  moteur. Le projet est déjà sur PostgreSQL exclusivement (ADR-0001), le coût est théorique.
- `AgentAssignment` reste dans son état actuel jusqu'au lot 6 : l'historique d'habilitation
  continue d'être purgé à 30 jours d'ici là. Défaut **connu et daté**, pas ignoré.

## Alternatives écartées

**Table d'historique séparée alimentée par déclencheur.** Deux sources de vérité, cohérence à
garantir, et une logique métier qui descend dans la base où elle est invisible au test.

**Suppression logique comme désaffectation** (le choix d'`AgentAssignment`). Purge à 30 jours,
et confusion entre « supprimé » et « terminé ». Écartée pour les raisons mesurées au §Contexte.

**Versionnement bitemporel** (période de validité *et* période de saisie). Répond en plus à « que
croyait-on savoir le 3 mars ? ». Aucune exigence du domaine ne le demande, et le coût de
compréhension est réel. Écarté comme sur-conception ; le modèle retenu en est le sous-ensemble
usuel et pourra être étendu sans être défait.

**Event sourcing sur les affectations.** Cohérent avec l'orientation événementielle du système,
mais impose un modèle de lecture à reconstruire et une infrastructure de journal que le projet n'a
pas. Sans commune mesure avec le besoin.
