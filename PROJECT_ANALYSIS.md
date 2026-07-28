# PROJECT_ANALYSIS.md

> 🟠 **ARCHIVE — GELÉE AU 2026-07-11.**
>
> L'analyse métier (§1 vision, acteurs, flux cibles) et les **risques R1–R8** restent pertinents et
> sont référencés partout ailleurs. En revanche l'état technique décrit (§2, §3, §5, §6) est dépassé :
> voir `docs/IMPLEMENTATION_LOG.md` pour le réel et `docs/KNOWLEDGE_MAP.md` pour la synthèse.


> Rapport d'analyse de reprise — rôle : Senior Software Engineer / Architecte.
> Date : 2026-07-11 · Aucune modification de code effectuée.
> Complément opérationnel : voir `PROJECT_STATUS.md` (état des fonctionnalités) et `ROADMAP.md` (plan priorisé). Décisions : `docs/adr/`.

---

## 1. Vision métier

Système professionnel de **gestion intelligente des déchets** pour la zone de Pikine (Sénégal), porté par la SONAGED. Le produit doit :
surveiller les **points de collecte** (dépotoirs), **détecter leur niveau de remplissage**, **déclencher des alertes automatiques**, **optimiser les tournées** (circuits de collecte/balayage) et **superviser sur carte**.

### Acteurs du système
- **Gestionnaire / superviseur** (SONAGED) : supervise points, circuits, alertes, tableaux de bord.
- **Agent de collecte / balayage** : rattaché à un circuit (matin/soir/nuit).
- **Citoyen** : émet des avis/signalements (`Avis`), destinataire potentiel d'un système d'alerte (cf. enquête CSV).
- **Système / capteur (à venir)** : émetteur des mesures de remplissage (vision IoT).

### Flux cibles
1. Capteur → mesure de remplissage → seuil dépassé → **alerte** → notification gestionnaire → tournée.
2. Gestionnaire → visualisation carte (dépotoirs, circuits, alertes) → décision.
3. Utilisateur → inscription → activation e-mail → authentification JWT → back-office.

> **Écart majeur** : les étapes « capteur → mesure → seuil → alerte automatique » n'existent pas encore (cf. §5, §6).

---

## 2. Architecture actuelle

```
   Angular (angular/ :4200) ─┐
   sonaged_web/ (doublon)    ├─HTTP/JWT─► ucgBackend (Spring Boot 3.2.4 :8089)
   Flutter (mobileFlutter/) ─┘             /v1 · /auth · /v1/maps · /sonaged-docs
                                                │           │
                                          PostgreSQL     SMTP (smtp4dev / Gmail)
                                          :5433 sonaged
   datas/*.json (GeoJSON Pikine) — chargement en base non automatisé
```

- **Backend en couches** : `controller → service → service.impl → repository → model`, DTO + MapStruct (`XxxMapper.UMP`), AOP (`aspects/`, `@Notifiable`). Circular refs forcées.
- **Sécurité** : JWT stateless HS256 (sujet = e-mail), `JwtFilter` + `SecurityConfiguration`, BCrypt, activation par code.
- **Multi-clients** : 1 backend, **3 fronts web** (dont 2 en trop) + 1 mobile.

---

## 3. Stack technique

| Couche | Technologies |
|---|---|
| Backend | Java 17, Spring Boot 3.2.4 (web, data-jpa, security, mail, validation), MapStruct, Lombok, jjwt **+** auth0 java-jwt (redondant), springdoc **+** springfox (conflit), Liquibase (déclaré) |
| BD | PostgreSQL (`ddl-auto=update` **et** Liquibase — incohérent) |
| Web | Angular 16, Bootstrap, apexcharts/echarts/tinymce/quill, express (`server.js`) pour servir le build |
| Mobile | Flutter 3 (Dart ≥3.1.5), Riverpod, freezed, dio, go_router, Google Maps |
| Infra | Docker (smtp4dev uniquement), pas de CI/CD |

---

## 4. Modules existants & modèle de données

### Domaine (relations JPA réelles — bien modélisées)
```
Region 1─* Department 1─* Commune 1─* { Depotoir, CircuitCollect, CircuitBalayage }
Each geo entity 1─1 Geometry (forme GeoJSON)
Depotoir *─1 TypeDepotoir · *─1 Commune   (lien *─1 Quartier commenté/désactivé)
User *─1 Authority 1─* Permission (enum, table authorityPermission)
Alert 1─1 Coordinate · 1─1 Image · + displayPicture BLOB
Avis *─1 (…)
```
- Entités robustes : `equals/hashCode` compatibles proxy Hibernate, `@Enumerated(STRING)` (AlertCode, CircuitShift, Permission).
- `UserEntity implements UserDetails`, e-mail unique, **un seul rôle** par utilisateur (ManyToOne Authority).

### Fonctionnalités livrées (voir `PROJECT_STATUS.md` pour emplacements)
Auth JWT + activation e-mail, CRUD Users/Alerts (+ upload image)/entités géo, cartographie (`MapsController`), dashboard, upload de fichiers ; front web CRUD Users + login ; écrans mobile auth/dashboard/carte + live tracking.

---

## 5. Fonctionnalités incomplètes

- **Cœur métier IoT** : pas de modèle de mesure, pas d'ingestion, pas de seuil, pas de déclenchement automatique. `Alert` = CRUD manuel, **non relié à `Depotoir`**.
- **Notifications** : n'envoient que des codes d'activation (pas d'alertes de collecte).
- **Optimisation des tournées** : circuits stockés, aucun algorithme.
- **Front web** : dépotoirs et pages générales à l'état de squelette ; 3 fronts non consolidés.
- **Mobile** : écrans faits, branchement API réel partiel, restes de template (spoonacular).
- **Import GeoJSON** : `datas/*.json` non chargés automatiquement.

---

## 6. Dette technique

| Domaine | Dette |
|---|---|
| Sécurité | Secret JWT et mots de passe **versionnés** ; mot de passe d'inscription **codé en dur** ; token loggué ; pas de gestion d'exception JWT ; pas de `.gitignore` racine |
| Build | springfox + springdoc ; 2 libs JWT ; Liquibase **et** `ddl-auto` ; `allow-circular-references=true` |
| Modèle JPA | `FetchType.EAGER` massif (Depotoir→Commune→Department→Region ; Geometry) ; `CascadeType.ALL` sur des ManyToOne vers types **partagés** (`TypeDepotoir`) → risque de suppression en cascade ; images stockées en **BLOB** dans la table `ALERT` |
| Frontends | 3 dépôts Angular (`angular`, `sonaged_web`, `ucgFrontend` mort) ; `services/user.service.ts` vide |
| Docs | README vides ; `endpoint.md` erroné ; notes perso versionnées |
| Tests | ~2 fichiers de test ; pas de CI |
| Nommage | UCG vs SONAGED incohérent |

---

## 7. Risques

| # | Risque | Gravité | Nature |
|---|---|---|---|
| R1 | Compromission de comptes (mdp unique `Sonaged@123`, secret JWT public) | 🔴 Critique | Sécurité |
| R2 | Fuite de secrets présents dans l'historique Git | 🔴 Critique | Sécurité |
| R3 | Effondrement des perfs à l'échelle (EAGER + N+1 + BLOB) sur « milliers de points » | 🟠 Élevé | Scalabilité |
| R4 | Suppression accidentelle de données de référence via `CascadeType.ALL` | 🟠 Élevé | Intégrité BD |
| R5 | Corruption/écrasement de schéma (Liquibase vs Hibernate) | 🟠 Élevé | BD/Déploiement |
| R6 | Instabilité de build (springfox incompatible Boot 3) | 🟡 Moyen | Build |
| R7 | Divergence des 3 fronts (double maintenance) | 🟡 Moyen | Maintenabilité |
| R8 | Erreurs 500 non maîtrisées (JWT expiré, `roles.get(0)`) | 🟡 Moyen | Robustesse |

---

## 8. Recommandations (synthèse)

1. **Sécuriser d'abord** (R1, R2, R8) : hacher le mot de passe soumis, externaliser+roter les secrets, durcir le JWT. → ADR-0002, ADR-0003.
2. **Trancher la gestion de schéma** (R5) : Liquibase = source unique, JPA `validate`. → ADR-0001.
3. **Construire le cœur IoT** : modèle `Measurement` + `Depotoir.fillLevel`, endpoint d'ingestion, moteur de seuils, `Alert` relié à `Depotoir`. → ADR-0004, ADR-0005.
4. **Préparer l'échelle** (R3, R4) : `LAZY` par défaut + projections DTO, retirer `CascadeType.ALL` fautif, sortir les images du BLOB. → ADR-0005, ADR-0008.
5. **Assainir build & fronts** (R6, R7) : nettoyer `pom.xml`, choisir un front unique. → ADR-0006, ADR-0009.
6. **Qualité** : tests + CI/CD + docs.

> Détail actionnable et estimations : `ROADMAP.md`. Décisions argumentées : `docs/adr/`.
