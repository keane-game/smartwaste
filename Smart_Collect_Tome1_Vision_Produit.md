# CAHIER DES CHARGES — SMART COLLECT V2

## TOME 1 — VISION PRODUIT

---

### 1. Présentation

#### 1.1 Contexte SONAGED

La Société Nationale de Gestion des Déchets (SONAGED) est l'établissement public sénégalais chargé de la coordination et de la supervision de la gestion des déchets solides ménagers sur l'ensemble du territoire national. Elle opère à travers un réseau de circuits de collecte, de points de regroupement et d'équipes de terrain (balayeurs, superviseurs, agents de collecte, contrôleurs).

Dans la zone de Pikine, prise ici comme périmètre pilote, le dispositif existant repose sur une organisation humaine structurée :

- Un mobilier urbain composé de **points propres** et de **bacs de rue** répartis sur le territoire ;
- Des **camions de collecte** effectuant des passages horaires selon des circuits prédéfinis ;
- Des **axes de balayage**, chacun confié à un balayeur en charge d'environ **500 mètres linéaires** ;
- Une hiérarchie de terrain : chaque **superviseur** encadre **25 agents**, et chaque agent est responsable d'une portion de la collecte ;
- Un **contrôleur de balayage**, dont la fonction reste externe au système numérique actuel ;
- **66 circuits de collecte** actifs, avec une ambition d'organiser le balayage en trois vacations : **matin, soir et nuit**.

Ce dispositif fonctionne aujourd'hui sur une base essentiellement humaine et déclarative : les remontées de terrain (bac plein, dépôt sauvage, dysfonctionnement d'un circuit) dépendent de la vigilance et de la disponibilité des agents, sans mécanisme de détection ou d'alerte automatisé.

#### 1.2 Problématique actuelle

L'analyse du terrain fait ressortir plusieurs constats :

- **Absence de système d'alerte automatisé** : à ce jour, il n'existe pas de dispositif permettant de signaler en temps réel qu'un point de collecte a atteint sa capacité, ce qui conduit soit à des collectes trop précoces (gaspillage de ressources), soit trop tardives (débordement, insalubrité, nuisances).
- **Pilotage réactif plutôt que prédictif** : les tournées sont planifiées selon des horaires fixes plutôt que selon le niveau réel de remplissage des points de collecte.
- **Manque de visibilité centralisée** : les superviseurs et décideurs ne disposent pas d'une vue d'ensemble en temps réel de l'état des 66 circuits, des bacs et du mobilier urbain.
- **Traçabilité limitée** : il est difficile de mesurer objectivement la performance d'un circuit, d'un agent ou d'une zone, faute de données historisées et exploitables.
- **Sensibilisation insuffisante des populations** aux pratiques de gestion des déchets, ce qui aggrave les phénomènes de dépôts sauvages et complique le travail des équipes de collecte.
- **Usage excessif de plastiques non biodégradables**, qui alourdit la charge de travail des circuits et pose un problème environnemental structurel indépendant de l'organisation logistique elle-même.

#### 1.3 Limites des systèmes existants

Les initiatives comparables observées dans la sous-région, notamment au **Kenya**, montrent la voie mais ne sont pas directement transposables telles quelles :

- Au Kenya, des entreprises ont déployé des capteurs de niveau de remplissage connectés via cartes SIM IoT, déclenchant des alertes automatiques lorsque les bacs atteignent un seuil critique, avec une réduction des coûts de gestion des déchets pouvant atteindre **40 %**.
- Dans le comté de Nairobi, un partenariat avec **IBM** a permis d'équiper la flotte de collecte de capteurs intelligents, offrant un suivi en temps réel des camions, une cartographie numérique des rues, ainsi que des fonctionnalités connexes (surveillance du comportement des conducteurs, détection de nids-de-poule et de dos d'âne, suivi de la consommation de carburant).

Ces exemples démontrent la faisabilité technique et la rentabilité d'une approche IoT appliquée à la gestion des déchets, mais ils restent conçus pour des contextes d'infrastructure et de financement différents. Le Sénégal, et plus particulièrement le périmètre SONAGED/Pikine, ne dispose à ce jour d'aucun système d'alerte équivalent, ce qui constitue à la fois un constat de retard et une opportunité de conception d'une solution adaptée aux réalités locales (organisation en circuits, hiérarchie superviseur/agent, contraintes de connectivité et de coût).

#### 1.4 Opportunité Smart City

Le projet **Smart Collect** s'inscrit dans une dynamique plus large de **ville intelligente (Smart City)**, où la donnée de terrain — remplissage des bacs, état des circuits, activité des équipes — devient un levier de pilotage pour les municipalités et les opérateurs publics. En automatisant la détection et l'alerte, Smart Collect vise à transformer un système de collecte réactif en un système **prédictif et piloté par la donnée**, réplicable au-delà de Pikine à l'échelle d'autres municipalités sénégalaises, puis à d'autres pays de la sous-région.

#### 1.5 Objectifs du projet

Smart Collect a pour ambition de :

1. Mettre en place un **système d'alerte automatisé** basé sur des capteurs IoT installés sur les points de collecte et bacs de rue ;
2. Offrir une **plateforme centralisée** (web et mobile) permettant à la SONAGED et aux municipalités de superviser en temps réel l'état des 66 circuits, des points propres et des équipes de terrain ;
3. **Optimiser les tournées de collecte** en fonction du niveau réel de remplissage plutôt que d'horaires fixes ;
4. Fournir aux agents de terrain (superviseurs, balayeurs, agents de collecte) une **application mobile** de suivi, de validation de collecte et de remontée d'incidents, y compris en mode hors-ligne ;
5. Produire des **statistiques et rapports** exploitables par les décideurs pour mesurer la performance et orienter les investissements ;
6. Intégrer un volet de **sensibilisation citoyenne** afin d'agir également sur la cause comportementale du problème, au-delà de la seule réponse logistique.

#### 1.6 Vision long terme

À terme, Smart Collect ambitionne de devenir une **plateforme souveraine, modulaire et multi-tenant**, capable de servir plusieurs municipalités et opérateurs de gestion des déchets à travers une architecture unique (Spring Boot 3.5, DDD, Spring Modulith, Angular, Flutter, MQTT, PostgreSQL, Keycloak, SSE, Docker). La feuille de route prévoit une évolution progressive : d'un système d'alerte pilote sur Pikine, vers une solution SaaS multi-municipalités, puis vers une offre exportable à l'échelle du continent africain, en cohérence avec les enjeux identifiés dans les retours d'expérience kényans (rentabilité économique, impact environnemental, adoption technologique).

---

### 2. Parties prenantes

| Partie prenante | Rôle dans le système |
|---|---|
| **SONAGED** | Maître d'ouvrage institutionnel ; supervision nationale de la gestion des déchets |
| **Municipalités** | Bénéficiaires opérationnelles locales (ex. Pikine) ; pilotage des circuits sur leur territoire |
| **Administration** | Paramétrage général, gestion des référentiels (communes, quartiers, circuits) |
| **Administrateurs / Super Administrateurs** | Gestion des comptes, des permissions et de la configuration de la plateforme |
| **Superviseurs** | Encadrement de 25 agents chacun ; pilotage d'un circuit et de ses balayeurs |
| **Agents de collecte / Balayeurs** | Exécution de terrain ; utilisateurs principaux de l'application mobile |
| **Contrôleurs de balayage** | Acteurs de contrôle qualité, aujourd'hui hors du système numérique — cible d'intégration future |
| **Techniciens IoT** | Installation, calibration et maintenance des capteurs |
| **Citoyens** | Utilisateurs finaux du service, cibles des campagnes de sensibilisation |
| **Décideurs** | Utilisateurs des rapports et statistiques pour l'aide à la décision |

---

### 3. Objectifs

#### 3.1 Objectifs fonctionnels
- Détection automatique du niveau de remplissage des points de collecte et déclenchement d'alertes en temps réel.
- Cartographie interactive des communes, quartiers, points de collecte et circuits.
- Planification et suivi des tournées, avec historique des collectes.
- Application mobile de terrain pour les agents (navigation, validation, remontée d'incidents, mode hors-ligne).
- Génération de rapports et statistiques exportables (PDF, Excel, CSV).
- Module de sensibilisation citoyenne (campagnes, contenus, quiz).

#### 3.2 Objectifs techniques
- Architecture modulaire (DDD, Spring Modulith) garantissant l'évolutivité et la maintenabilité.
- Communication capteurs → plateforme via protocole **MQTT**.
- Mise à jour temps réel de l'interface via **SSE (Server-Sent Events)**.
- Authentification et autorisation centralisées via **Keycloak** (RBAC).
- Conteneurisation et déploiement reproductible via **Docker**.

#### 3.3 Objectifs économiques
- Réduction des coûts opérationnels de collecte en évitant les passages inutiles, sur le modèle des gains observés au Kenya (jusqu'à 40 % de réduction des coûts de gestion).
- Optimisation de l'allocation des ressources humaines et matérielles (camions, agents) en fonction de la demande réelle.
- Création d'une base technologique réutilisable et commercialisable auprès d'autres municipalités (modèle SaaS).

#### 3.4 Objectifs environnementaux
- Réduction des débordements de bacs et des dépôts sauvages liés aux retards de collecte.
- Contribution à la lutte contre la pollution plastique par une meilleure maîtrise du cycle de collecte.
- Appui à une gestion plus durable des déchets à l'échelle municipale.

#### 3.5 Objectifs sociaux
- Amélioration des conditions de travail des agents de terrain grâce à des outils numériques adaptés (application mobile, remontée d'incidents facilitée).
- Renforcement de la sensibilisation des citoyens aux bonnes pratiques de gestion des déchets.
- Meilleure salubrité urbaine pour les populations riveraines des points de collecte.

#### 3.6 Objectifs académiques
- Constituer le socle technique et documentaire d'un mémoire de Master en Génie Logiciel.
- Démontrer une maîtrise de bout en bout d'un projet IoT/Smart City : architecture logicielle, temps réel, mobilité, sécurité et déploiement.
- Servir de support de présentation auprès de la SONAGED et des municipalités partenaires potentielles.

---

*Fin du Tome 1 — Vision Produit. Ce tome sert de socle aux Tomes suivants (Architecture Fonctionnelle, Cas d'utilisation, Modules Fonctionnels, etc.) et devra rester cohérent avec eux au fil de la rédaction.*
