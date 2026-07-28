# ADR-0006 — Consolidation vers un frontend web unique

- Statut : **Proposé — ⚠️ PRÉMISSE INVALIDÉE, à réexaminer avant toute action**.
> Cet ADR retient `angular/` au motif qu'il serait « le plus complet ». Mesure du 2026-07-28 : `angular/` = 164 fichiers `.ts` / ~9 900 lignes (Angular 17.0.7, NgModules) ; **`sonaged_web/` = 196 fichiers / ~16 900 lignes (Angular 17.3, composants standalone)**. Le second est plus gros et plus moderne. Le volume ne prouve pas la complétude, mais **appliquer cet ADR en l'état pourrait supprimer la meilleure base**. Comparaison fonctionnelle requise avant décision.
- Date : 2026-07-11
- Priorité : P1-4

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
