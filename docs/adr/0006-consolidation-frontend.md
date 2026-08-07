# ADR-0006 — Consolidation vers un frontend web unique

- Statut : **Exécuté le 2026-08-05, mais avec la décision INVERSÉE** — le front retenu est
  `sonaged_web/`, pas `angular/`. Voir « Décision effective » ci-dessous.
- Date : 2026-07-11 · Exécution : 2026-08-05
- Priorité : P1-4

## Décision effective (2026-08-05) — remplace la décision d'origine

**Front retenu : `sonaged_web/`.** `angular/` et `ucgFrontend/` ont été supprimés
(1 054 fichiers suivis).

Cet ADR retenait `angular/` au motif qu'il serait « le plus complet ». L'audit du 2026-08-05
(`docs/FRONTEND_AUDIT.md`) a montré que les deux candidats étaient en avance sur des axes
**opposés** :

- `angular/` était le seul **à jour de l'API** — sous-chemins `/s`, renouvellement de jeton,
  déconnexion serveur, client SSE, registre CRUD sur 12 entités ;
- `sonaged_web/` a la meilleure **fondation technique** — Leaflet + proj4 + esri-leaflet, i18n,
  papaparse, dépendances Angular 17.3 cohérentes, deux fois plus de code — mais son contrat
  d'API avait deux ans de retard : 11 chemins au singulier inexistants, listes en 400, aucun
  rafraîchissement de jeton.

L'arbitrage s'est fait sur ce qui est **coûteux à refaire**. La pile SIG et l'i18n sont du
travail ; la fraîcheur de l'API est de la connaissance, transférable. Elle l'a été avant
suppression : renouvellement de jeton, intercepteur 401, client SSE, registre des chemins
(`shared/constants/api-endpoints.ts`), accès typés `/v1/maps/**` et écran « avis ».

Correction au dossier : `sonaged_web` n'est **pas** « à composants standalone » — 16 fichiers
sur 199 le sont, contre 44 NgModules. Le bootstrap est standalone, pas l'architecture.

---

## Décision d'origine (2026-07-11) — non retenue

> ⚠️ Ce qui suit est conservé pour la trace du raisonnement. La mesure du 2026-07-28 avait déjà
> signalé que la prémisse était fausse : `angular/` = 164 fichiers `.ts` / ~9 900 lignes ;
> `sonaged_web/` = 196 fichiers / ~16 900 lignes.

## Contexte

Le dépôt contient **trois** applications web Angular :
- `angular/` — la plus complète (login, guards, intercepteurs JWT, CRUD utilisateurs, layout) ;
- `sonaged_web/` — quasi-doublon (mêmes scripts npm, nom `sonaged`) au rôle non clarifié ;
- `ucgFrontend/` — scaffold hérité **mort** (composants « works! », services vides), vestige de l'état initial du projet.

Trois fronts = triple maintenance, divergence de code, confusion pour l'onboarding. De plus, l'URL API d'`angular` est erronée (`:8089/api` alors que le backend expose `/v1` et `/auth`).

## Décision

1. Retenir **`angular/` comme frontend canonique** (le plus avancé).
2. Corriger sa configuration API (`environment*.ts` → `/v1`, `/auth`, port 8089) et distinguer réellement dev/prod.
3. **Archiver puis retirer** `ucgFrontend/` (mort) et `sonaged_web/` (doublon) — **après validation explicite** et récupération d'éventuels éléments utiles (styles, écrans) vers `angular/`.

## Conséquences

- **+** Une seule base web à faire évoluer et tester ; intégration API correcte.
- **−** Suppression de dépôts = action irréversible → **validation requise** (conforme à la règle projet « ne jamais supprimer sans analyse »).
- **−** Vérifier au préalable qu'aucun script de build/déploiement ne cible `sonaged_web/`/`ucgFrontend/`.

## Alternatives considérées

- **Garder `sonaged_web` comme cible** : possible si une raison (SSR, refonte) le justifie ; à ce stade `angular/` est plus complet → moindre effort.
- **Conserver les trois** : rejeté (coût de maintenance injustifié).
