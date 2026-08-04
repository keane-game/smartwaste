# ADR-0018 — Rattacher un point à sa commune par sa position, non par son libellé

- Statut : **Accepté — implémenté** (2026-08-04)
- Date : 2026-08-04
- Priorité : P0 — corrige une donnée fausse dans tout ce qui est territorial
- **Remplace [ADR-0015](0015-referentiel-geographique-projection-et-perimetre.md) §4**, dont le remède était inefficace
- Rendu possible par [ADR-0016](0016-geometrie-des-entites-dechets.md) : sans position sur les points, rien de ceci n'est calculable

## Contexte

L'import rattache chaque point de collecte à sa commune en comparant des **libellés** : celui du fichier SIG (`Commune`) et ceux du référentiel, issus de `commune.json`. Les deux ne coïncident pas.

L'ADR-0015 §4 l'avait anticipé et prescrit un « rapprochement par nom normalisé (casse repliée, accents retirés, espaces réduits) ». **Cette décision n'a jamais été implémentée** — le code s'en tenait à `findByNameIgnoreCase` puis `findByNameContainingIgnoreCase`. Et en la mesurant avant de l'implémenter, il est apparu qu'elle **n'aurait rien réglé** : les écarts ne sont ni de casse ni d'accent, mais d'orthographe.

| Libellé du fichier | Référentiel | Écart |
|---|---|---|
| `Dalifort` | `daliford` | lettre finale |
| `Diamagueune` | `diamaguene sicap mbao` | un `u` de trop |
| `Guinaw rails` | `guinaw rail nord` / `guinaw rail sud` | un `s` de trop, **et deux cibles possibles** |
| `Keur Mbaye fall` | *absent* | commune inconnue du référentiel |

Aucune normalisation typographique ne corrige cela.

**Le pire n'était pas là.** La correspondance partielle retenait « la première » commune candidate, avec un simple avertissement au journal. Or `Pikine` correspond à *trois* communes (`pikine est`, `pikine nord (sud)`, `pikine ouest`), `Thiaroye` à trois, `Guinaw rail` à deux. Bilan mesuré sur les 71 points réels :

| | Points |
|---|---|
| Rattachement certain | 31 |
| **Rattachement arbitraire** (plusieurs candidats, le premier gagne) | **25** |
| Aucun rattachement | 15 |

Les 25 sont plus graves que les 15. Un point sans commune est *visiblement* absent des tournées et des rapports ; un point rattaché au hasard est *invisiblement faux*, et corrompt à la fois la commune qui l'accueille à tort et celle qui le perd. La tournée, le taux de réalisation et le rapport d'efficacité livrés les jours précédents reposaient donc, pour 35 % des points, sur un tirage au sort.

## Décision

**La commune d'une entité est déterminée par sa position géographique.** Le libellé n'est plus qu'un repli.

1. Le référentiel territorial publie les **contours** des communes (`TerritoryImportPort.communeBoundaries()`), en WGS84.
2. L'import situe chaque entité par **lancer de rayon** (`PolygonContainment`) : un point est dans la commune dont le contour l'entoure.
3. Si la position place l'entité dans **exactement une** commune, c'est celle-là. Zéro ou plusieurs → on ne conclut pas.
4. Le repli par libellé ne tranche **plus** entre plusieurs candidats : ambigu = non rattaché.

**Pourquoi c'est le bon critère.** Un libellé est une convention d'écriture, qui varie d'un export à l'autre. Une position est un fait. « Pikine » ne désigne pas une commune ; le point situé à 14,7413 N / 17,4195 O en désigne une seule.

**Pourquoi pas un rapprochement approximatif** (distance d'édition, phonétique). Il aurait « corrigé » `Guinaw rails` en le rattachant à `guinaw rail nord` **ou** `sud` — une chance sur deux, c'est-à-dire le défaut qu'on corrige, avec un vernis d'algorithme. On ne remplace pas un tirage au sort par un tirage au sort mieux habillé.

**Pourquoi le lancer de rayon et non un rectangle englobant.** Les communes de Pikine ne sont pas rectangulaires ; un englobant rattacherait au territoire des points situés dans ses concavités.

### Un instantané, et non une résolution point par point

La première version publiait `findCommuneIdAt(latitude, longitude)`. Élégante à l'appel, elle rechargeait les 12 communes, leur géométrie et leurs coordonnées **à chaque entité** — une vingtaine de requêtes multipliées par 279 entités. L'import ne terminait plus ; il a fallu l'interrompre.

Les contours ne changent pas pendant un import : ils se lisent **une fois**, et l'appelant garde l'instantané. Rendre cet instantané plutôt que le mémoriser dans le service évite aussi un cache de singleton qui se périmerait en silence à la première modification de commune.

## Conséquences

- **+** Sur les 71 points réels : **70 rattachés**, tous par la position, contre 56 dont 25 arbitraires. Un seul reste sans commune, et il le reste honnêtement.
- **+** `guinaw rail nord` (7 points) et `guinaw rail sud` (4) sont enfin **distingués** — le libellé `Guinaw rail` les confondait. `daliford` récupère ses 11 points, invisibles jusque-là.
- **+** Tournées, taux de réalisation et rapports d'efficacité portent désormais sur le bon territoire.
- **+** Les circuits en bénéficient : 52/52 et 146/156 rattachés.
- **−** Le rattachement dépend de la qualité des contours. Deux communes qui se chevauchent produiraient « plusieurs candidats » et donc aucun rattachement — un défaut visible, ce qui est l'intention.
- **−** Une entité sans géométrie retombe sur le libellé, avec ses limites. C'est le cas des 10 circuits de balayage non rattachés.
- **−** Le point de départ d'un circuit sert à le situer : un tracé qui traverse deux communes est rattaché à celle de son premier point. Acceptable pour un rattachement administratif, discutable pour un calcul de charge — à revoir si l'usage l'exige.

## Alternatives considérées

- **Implémenter la normalisation d'ADR-0015 §4** : rejeté après mesure — elle ne corrige aucun des cas réels, qui sont des variantes d'orthographe.
- **Table d'alias curée à la main** (`Dalifort` → `daliford`) : exacte et relisible, mais elle laisse entière la question de `Guinaw rails`, qui n'a pas de réponse dans le libellé. La géométrie répond à tout d'un coup.
- **Rapprochement approximatif** : rejeté, cf. ci-dessus.
- **Laisser le tirage au sort et signaler l'ambiguïté** : rejeté — un avertissement dans un journal n'a empêché personne d'exploiter 25 rattachements faux.
