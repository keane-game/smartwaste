# CAHIER DES CHARGES — SMART COLLECT V2

## TOME 2 — ARCHITECTURE FONCTIONNELLE

---

### 1. Vue globale

Smart Collect repose sur une architecture en couches, du capteur physique jusqu'à l'utilisateur final, structurée ainsi :

```text
Capteurs (ESP32 + capteurs de niveau)
        ↓
    Broker MQTT
        ↓
Backend Spring Boot 3.5 (DDD + Spring Modulith)
        ↓
    Modules métier
        ↓
    API REST + SSE
        ↓
   ┌─────────┴─────────┐
Angular (Web)      Flutter (Mobile)
   ↓                    ↓
Superviseurs/       Agents de terrain
Décideurs/Admin     (balayeurs, collecteurs)
```

Cette architecture garantit une séparation claire entre :
- la **couche perception** (capteurs, remontée d'événements physiques) ;
- la **couche métier** (modules DDD, règles de gestion, orchestration) ;
- la **couche exposition** (API REST pour les opérations, SSE pour le temps réel) ;
- la **couche présentation** (Angular pour le pilotage, Flutter pour le terrain).

---

### 2. Principes d'architecture

- **Domain-Driven Design (DDD)** : chaque module encapsule son propre langage métier, ses agrégats et ses règles, avec des frontières explicites entre modules.
- **Spring Modulith** : les modules coexistent dans un même déploiement (monolithe modulaire) mais communiquent par événements et interfaces publiques, ce qui permet une extraction future en microservices si le passage à l'échelle SaaS multi-tenant l'exige.
- **Communication asynchrone interne** : les modules publient des événements de domaine (ex. `PointCollecteSeuilAtteint`) plutôt que de s'appeler directement, réduisant le couplage.
- **Sécurité transversale** : chaque module expose ses propres règles d'autorisation (RBAC via Keycloak), mais la vérification est centralisée au niveau de la couche API.
- **Multi-tenant natif** : le module Tenant isole les données par municipalité/opérateur dès la conception, pour permettre l'évolution vers un modèle SaaS décrite dans le Tome 1 (vision long terme).

---

### 3. Architecture logique — Modules

| Module | Rôle en une phrase |
|---|---|
| **Identity** | Gestion des utilisateurs, rôles et authentification |
| **Territory** | Référentiel géographique : communes, quartiers, points, circuits |
| **Waste** | Cœur métier de la collecte : tournées, validations, historique |
| **IoT** | Ingestion et traitement des données capteurs |
| **Platform** | Alertes, notifications, orchestration temps réel |
| **Tenant** | Isolation multi-municipalité / multi-opérateur |
| **Analytics** | Statistiques, rapports, indicateurs de performance |
| **Administration** | Paramétrage, configuration fonctionnelle |
| **Shared** | Briques transverses réutilisables (value objects, utilitaires) |
| **Config** | Configuration technique, environnements, secrets |

Chaque module ci-dessous est détaillé selon : **responsabilités**, **dépendances**, **événements**, **ports**, **cas d'utilisation représentatifs**, **API**, **sécurité**.

---

### 4. Module Identity

**Responsabilités**
- Authentification des utilisateurs (délégation à Keycloak).
- Gestion des profils : Super Administrateur, Administrateur, Superviseur, Agent, Technicien IoT, Citoyen.
- Attribution et révocation des rôles et permissions.

**Dépendances**
- Aucune dépendance métier entrante ; consommé par tous les autres modules pour la résolution d'identité et de permissions.

**Événements publiés**
- `UtilisateurCree`, `RoleModifie`, `UtilisateurDesactive`

**Ports**
- Port entrant : API REST d'administration des comptes.
- Port sortant : synchronisation avec Keycloak (Admin REST API).

**Cas d'utilisation représentatifs**
- UC-001 Connexion
- UC-002 Créer un agent
- UC Attribuer un rôle Superviseur

**API (extrait)**
- `POST /api/identity/users`
- `PATCH /api/identity/users/{id}/roles`
- `GET /api/identity/me`

**Sécurité**
- Toute création de compte à privilège (Administrateur, Superviseur) nécessite une permission `IDENTITY_MANAGE`.
- Jetons JWT émis par Keycloak, validés à chaque appel API.

---

### 5. Module Territory

**Responsabilités**
- Référentiel des communes et quartiers (import initial, ex. données SONAGED de Pikine).
- Gestion des points de collecte (points propres, bacs de rue) et de leur position géographique.
- Définition des circuits (66 circuits identifiés sur Pikine) et des axes de balayage (segments de 500 m par balayeur).

**Dépendances**
- Fournit des données de référence à Waste, IoT, Analytics, Platform.

**Événements publiés**
- `PointCollecteCree`, `CircuitModifie`, `CommuneImportee`

**Ports**
- Port entrant : import CSV/API des référentiels territoriaux.
- Port sortant : notification aux modules Waste et IoT lors de la création d'un nouveau point.

**Cas d'utilisation représentatifs**
- UC-003 Importer les communes
- UC-004 Importer les points
- UC Définir un axe de balayage

**API (extrait)**
- `GET /api/territory/communes`
- `POST /api/territory/points`
- `GET /api/territory/circuits/{id}`

**Sécurité**
- Modification du référentiel réservée aux rôles Administrateur et Super Administrateur.
- Lecture ouverte aux Superviseurs et Agents dans le périmètre de leur circuit.

---

### 6. Module Waste

**Responsabilités**
- Cœur métier de la collecte : planification des tournées, ordonnancement, validation de passage.
- Historisation des collectes (qui, quand, quel point, quel volume estimé).
- Gestion des trois vacations de balayage (matin, soir, nuit).

**Dépendances**
- Consomme les données de Territory (points, circuits) et d'IoT (niveau de remplissage) pour ordonnancer les tournées.
- Publie vers Analytics pour le calcul des statistiques de performance.

**Événements publiés**
- `TourneePlanifiee`, `CollecteValidee`, `CircuitCloture`

**Ports**
- Port entrant : validation de collecte depuis l'application mobile Flutter.
- Port sortant : notification à Platform pour la clôture d'alerte associée.

**Cas d'utilisation représentatifs**
- UC-005 Créer une tournée
- UC Valider une collecte sur le terrain
- UC Consulter l'historique d'un circuit

**API (extrait)**
- `POST /api/waste/tours`
- `POST /api/waste/collections/{pointId}/validate`
- `GET /api/waste/circuits/{id}/history`

**Sécurité**
- Validation de collecte réservée aux Agents affectés au circuit concerné.
- Planification réservée aux Superviseurs et Administrateurs.

---

### 7. Module IoT

**Responsabilités**
- Ingestion des mesures des capteurs de niveau de remplissage via MQTT.
- Gestion du cycle de vie des capteurs (installation, calibration, état, santé).
- Détection de seuil et transmission de l'information à Platform pour déclenchement d'alerte.

**Dépendances**
- Dépend de Territory pour associer un capteur à un point de collecte.
- Publie vers Platform pour le déclenchement des alertes.

**Événements publiés**
- `MesureRecue`, `SeuilAtteint`, `CapteurHorsService`

**Ports**
- Port entrant : abonnement au broker MQTT (topics par capteur/point).
- Port sortant : publication d'événements de domaine vers Platform.

**Cas d'utilisation représentatifs**
- UC Enregistrer un nouveau capteur
- UC Recevoir et traiter une mesure
- UC Détecter un capteur défaillant (absence de mesure au-delà d'un délai)

**API (extrait)**
- `POST /api/iot/sensors`
- `GET /api/iot/sensors/{id}/measurements`
- `GET /api/iot/sensors/{id}/health`

**Sécurité**
- Authentification des capteurs par identifiants MQTT dédiés (par device), distincts des comptes utilisateurs.
- Accès aux données capteurs réservé aux Techniciens IoT, Superviseurs et Administrateurs.

---

### 8. Module Platform

**Responsabilités**
- Gestion du cycle de vie complet des alertes : création, propagation, notification, clôture, historique.
- Diffusion des mises à jour en temps réel vers les interfaces (SSE).
- Orchestration des notifications (push mobile, notifications web).

**Dépendances**
- Consomme les événements d'IoT (`SeuilAtteint`) et de Waste (`CollecteValidee` pour clôturer une alerte).

**Événements publiés**
- `AlerteCreee`, `AlertePropagee`, `AlerteCloturee`

**Ports**
- Port entrant : événements internes (IoT, Waste).
- Port sortant : flux SSE vers Angular/Flutter, notifications push.

**Cas d'utilisation représentatifs**
- UC Créer une alerte automatique (seuil atteint)
- UC Notifier le superviseur d'un circuit
- UC Clôturer une alerte après collecte

**API (extrait)**
- `GET /api/platform/alerts` (liste + filtres)
- `GET /api/platform/alerts/stream` (SSE)
- `PATCH /api/platform/alerts/{id}/close`

**Sécurité**
- Diffusion des alertes filtrée par circuit/territoire selon le périmètre de l'utilisateur connecté.

---

### 9. Module Tenant

**Responsabilités**
- Isolation des données par municipalité ou opérateur (préparation à l'évolution SaaS multi-tenant décrite en Tome 1).
- Gestion de la configuration propre à chaque tenant (branding, paramètres de collecte).

**Dépendances**
- Transverse : consulté par tous les modules pour le filtrage contextuel des données.

**Événements publiés**
- `TenantCree`, `TenantConfigure`

**Ports**
- Port entrant : API de provisioning de nouveaux tenants (municipalités).

**Cas d'utilisation représentatifs**
- UC Provisionner une nouvelle municipalité
- UC Configurer les paramètres d'un tenant

**API (extrait)**
- `POST /api/tenants`
- `GET /api/tenants/{id}/settings`

**Sécurité**
- Opérations réservées au Super Administrateur (niveau plateforme, au-dessus des Administrateurs municipaux).

---

### 10. Module Analytics

**Responsabilités**
- Agrégation des données de collecte, d'alertes et de performance des circuits.
- Génération de rapports (PDF, Excel, CSV) et de graphiques pour les décideurs.
- Calcul d'indicateurs (taux de remplissage moyen, délai moyen de réponse à une alerte, performance par circuit/agent).

**Dépendances**
- Consomme les données historisées de Waste, IoT et Platform.

**Événements publiés**
- `RapportGenere`

**Ports**
- Port entrant : requêtes de génération de rapport.
- Port sortant : export de fichiers (PDF/Excel/CSV).

**Cas d'utilisation représentatifs**
- UC Générer un rapport mensuel par circuit
- UC Consulter le tableau de bord de performance

**API (extrait)**
- `GET /api/analytics/dashboard`
- `POST /api/analytics/reports`

**Sécurité**
- Accès aux rapports globaux réservé aux Administrateurs et décideurs ; les Superviseurs n'accèdent qu'aux données de leur périmètre.

---

### 11. Module Administration

**Responsabilités**
- Paramétrage fonctionnel global (seuils d'alerte par défaut, fréquences de balayage, règles métier configurables).
- Gestion des campagnes de sensibilisation citoyenne (module transverse rattaché fonctionnellement ici).

**Dépendances**
- Fournit des paramètres consommés par IoT (seuils) et Waste (fréquences).

**Événements publiés**
- `ParametreModifie`, `CampagneCreee`

**Ports**
- Port entrant : interface d'administration (Angular).

**Cas d'utilisation représentatifs**
- UC Modifier le seuil d'alerte global
- UC Créer une campagne de sensibilisation

**API (extrait)**
- `PUT /api/administration/settings`
- `POST /api/administration/campaigns`

**Sécurité**
- Réservé aux Administrateurs et Super Administrateurs.

---

### 12. Module Shared

**Responsabilités**
- Fournit les objets de valeur, exceptions communes, utilitaires de mapping et conventions transverses (formats de date, pagination, gestion d'erreurs standardisée) utilisés par l'ensemble des modules.

**Dépendances**
- Aucune dépendance métier ; dépendance technique uniquement (bibliothèque interne).

**Événements publiés**
- Aucun (module de support, pas de logique métier propre).

**Ports**
- N/A — bibliothèque partagée, pas d'exposition API directe.

**Sécurité**
- N/A.

---

### 13. Module Config

**Responsabilités**
- Gestion de la configuration technique (profils Spring, variables d'environnement, secrets), en lien avec le Tome 14 (Déploiement).

**Dépendances**
- Utilisé au démarrage par l'ensemble des modules.

**Ports**
- Port sortant : intégration avec le système de gestion de secrets (variables d'environnement Docker/CI).

**Sécurité**
- Accès aux secrets restreint à l'infrastructure de déploiement (jamais exposé via l'API applicative).

---

### 14. Communication inter-modules — synthèse

```text
IoT ──(SeuilAtteint)──▶ Platform ──(AlerteCreee)──▶ SSE ──▶ Angular / Flutter
Territory ──(référentiel)──▶ Waste, IoT, Analytics
Waste ──(CollecteValidee)──▶ Platform (clôture alerte) + Analytics (historique)
Tenant ──(filtrage contextuel)──▶ tous les modules
Identity ──(résolution d'identité/permissions)──▶ tous les modules
```

Ce mode de communication événementiel garantit que chaque module reste autonome et testable indépendamment, tout en assurant une cohérence globale du système — condition nécessaire à l'objectif technique d'évolutivité posé dans le Tome 1.

---

*Fin du Tome 2 — Architecture Fonctionnelle. Ce tome sert de socle au Tome 3 (Cas d'utilisation détaillés) et au Tome 4 (Modules Fonctionnels détaillés par écran/fonctionnalité).*
