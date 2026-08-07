
 	Pole Sciences Économiques, Juridique et de l’Administration (SEJA) 
Remerciements
Nous rendons grâce à Allah, le Tout Miséricordieux, le Très Miséricordieux de nous avoir donné la santé et l’aptitude de pouvoir faire nos études et après notre stage jusqu’à ce point et que tout ne Lui est forcé mais seulement fait pour montrer sa Générosité hors Paire ;
Que le salut soit sur son Prophète Muhammad (SWT), sa famille et ses fidèles serviteurs.
Notre gratitude va d’abord à nos encadreurs Dr Ndéye Arame DIAGO et Dr Marie Hélène MBALLO, pour leur confiance, leurs conseils, leur disponibilité, et leurs orientations durant tout le travail. 
Tout le personnel enseignant et administratif de l’Université numérique Cheikh Hamidou KANE(UNCHK) ;
Tous nos promotionnaires.   
















Dédicaces
Nous dédions ce modeste travail :
À nos très chers parents, la raison de ce que nous devenons aujourd’hui ;
A nos vaillantes-courageuses-charmantes mamans
A nos défunts pères, que leurs âmes reposent en paix éternellement ;
À nos frères et sœurs, que le bon Dieu vous procure la bonne santé, la joie, la luminescence et la réussite dans votre vie.
Nos tuteurs, nos amis et promotionnaires.
Nous vous en sommes très reconnaissants.
Nous vous souhaitons tout le bonheur et le succès dans votre vie familiale et professionnelle
A toute autre personne que nous n’avons pas citée et que nous devions faire.

















Table des matières
Remerciements	1
Dédicaces	2
Table des matières	3
Sigles et Abréviations	6
Avant-propos	8
Liste des Figures	9
Liste des tableaux	11
Introduction Générale	1
CHAPITRE 1 :	Présentation générale	3
Introduction	3
1.1	Présentation de l’unité de Coordination de la Gestion des Déchets solides	3
1.2	Organigramme de l’UCG	3
1.3	Présentation du sujet	4
1.3.1	Contexte	4
1.3.2	Problématique	5
1.3.3	Objectifs	5
1.4	Contexte de la gestion des déchets au Sénégal	6
1.4.1	Définition	6
1.4.2	Causes des déchets	7
1.4.3	Impacts des déchets	7
1.4.4	Historique de la gestion des déchets au Sénégal	8
1.4.5	Analyse des applications existantes de gestion des déchets	10
1.4.6	Problèmes de la gestion des déchets	13
Conclusion	14
CHAPITRE 2 :	Les concepts d’internet des objets son importance	15
Introduction	15
2.1	L’architecture de l’internet des objets	16
2.2	Les technologies de communication dans l’IdO	18
2.2.1	Les technologies de communication à courte portée	18
2.2.2	Les technologies de communication longue portée	19
2.3	Secteurs d’applications des objets connectés	21
2.3.1	L’Internet des objets dans la domotique	22
2.3.2	L’Internet des objets dans le domaine de la santé	23
2.3.3	L’Internet des Objets dans le domaine de l’agriculture	23
2.3.4	L’Internet des Objets dans le domaine de l’industrie	24
2.4	Importance de l'IdO dans la gestion des déchets	25
2.5	Étude et exemples d’application de gestion de déchets avec l'IdO	25
Conclusion	32
CHAPITRE 3 :	Analyse et conception	33
Introduction	33
3.1	Analyse des besoins	33
3.1.1	Nature du projet	33
3.1.2	La méthodologie utilisée	34
3.1.3	Modélisation	37
3.1.4	Besoins non fonctionnels	38
3.1.5	Besoins fonctionnels	38
3.1.6	Diagramme des cas d’utilisation	40
3.1.7	Diagramme de séquence	44
3.1.8	Diagramme de classe	49
3.2	Conception des besoins	50
3.2.1	Choix des outils et technologies utilisés	50
Conclusion	73
CHAPITRE 4 :	Implémentation	74
Introduction	74
4.1	Architectures du système	74
4.1.1	Architecture matérielle du système	74
4.1.2	Architecture logicielle du système	74
4.1.3	Description de l’architecture du système	75
4.1.4	Architecture de communication du système	78
4.2	Exemple de montage des composants IdO	78
4.2.1	Système d’envoi/réception du niveau de déchet	79
4.2.2	Mesure de la température et de l’humidité	80
4.2.3	Système d'ouverture /fermeture	81
4.2.4	Système de géolocalisation des poubelles	82
4.3	Implémentation des applications Web/Mobile & API	83
4.3.1	Les maquettes (figma)	83
4.3.2	L’API (SpringBoot)	85
4.3.3	L’Application web (angular)	89
4.3.4	L’Application mobile (flutter)	92
Conclusion	95
Conclusion Générale	97
Bibliographie	98
Sigles et Abréviations
Abréviations 	Signification
5G	Cinquième génération 
AECID	Agence Espagnol pour la Coopération Internationale au 
Développement
AGETIP	Agence d’Exécution des Travaux d’Intérêt Public 
APROSEN	Agence pour la propreté du Sénégal
Cadak-Car	Communauté des Agglomérations de Dakar et la Communauté des 
Communauté des Agglomérations de Rufisque 
CAMCUD 	Coordination des Associations et de Mouvements de jeunes de la 
Communauté Urbaine de Dakar 
eMBB	enhanced Mobile Broadband 
GPRS	General Packet Radio Service. 
GPS	Global Positioning System
GSM	Global System for Mobile
IDE 	Integrated development environment
IdO	Internet des Objets
IEEE	Institute of Electrical and Electronics Engineers
IOE	Internet of Everything
IoT	Internet of thing 
LM	Lettre de missions
LoRa	Long Range 
LoRaWAN	Low Range Wide Area Network
LPWAN	Low Power Wide Area Network
MCD 	Modèle Conceptuel de Données
MCT 	Modèle Conceptuel des Traitements
mMTC	Massive Machine Type Communications 
MOT 	Modèle organisationnel des traitements
Nb-IoT	Internet des objets à bande étroite 
NIST	Institut national des normes et de la technologie 
OMG	Object Management Group
OMT 	Object Modeling Technique
OOSE	Object-Oriented Software Engineering
PNGD	Programme National de Gestion de Déchets
PNR	Point de Regroupement Normalisé 
PROMOGED	Projet de Promotion de la Gestion intégrée et de l’Économie des 
Déchets
RFID	Radio Fréquence Identification
SGBD 	Système de gestion de bases de données.
SIAS 	Société Industrielle d’Aménagement Urbain du Sénégal
SIG	Systèmes d’informations géographique
SOADIP 	Société Africaine de Développement Industriel et de Promotion
SONAGED 	Société nationale de gestion intégré des déchets
TIC	Technologie de l’information et de communication 
UIT	Union Internationale des Télécommunications 
UML 	Unified Modeling Language
UP 	Unified Process
URL	Uniform Resource Locator
Wifi	Wireless Fidelity 
MULHP	Ministère de l'Urbanisme, du Logement et de l'hHgiène Publique
 UCG	l'Unité de Coordination de la Gestion des déchets solides



















Avant-propos
L’Université numérique Cheikh Hamidou KANE(UNCHK), créée par décret N° 2013-1294 est une mise en œuvre de la décision 02 du Conseil Présidentiel du 14 août 2013 qui consiste à "mettre les Technologies de l’Information et de la Communication (TIC) au cœur du développement de l’enseignement supérieur et de la recherche pour améliorer l’accès à l’enseignement supérieur et l’efficacité du système”.
Elle est la première université numérique du Sénégal et est un établissement public à caractère éducatif et professionnel.
En tant qu’établissement d’enseignement supérieur, elle a également pour mission de contribuer à la production des connaissances, ce qui implique de sa part une politique active en matière de recherche et de vulgarisation scientifique.
Pour terminer et valider le cycle du master en Ingénierie Logicielle (IL), l’étudiant arrivé à la fin de celui-ci doit faire un mémoire d'étude.
C’est dans cette option que nous avons été chargés de mener un projet informatique dont la teneur est consignée dans ce mémoire. 
C’est dans ce dessein que nous avons choisi la gestion des déchets en partenariat avec l'Unité de Coordination de la Gestion des déchets solides (UCG) du département de Pikine pour mener un projet informatique dont la teneur est consignée dans ce mémoire.









 Liste des Figures
Figure 1:Organigramme de l’UCG	4
Figure 2:Point de regroupement normalisé des déchets par l'UCG	10
Figure 3:Allô Gravats	11
Figure 4:programme allo déchets	12
Figure 5:Cleaning Day – Besup Setal	13
Figure 6:Architecture réseau IdO	18
Figure 7:Secteurs d’application de l'IdO	22
Figure 8:Internet des objets dans la domotique	22
Figure 9:Internet des objets dans le domaine de la santé	23
Figure 10:Internet des Objets dans le domaine de l’agriculture	24
Figure 11:Internet des Objets dans le domaine de l’industrie	24
Figure 12: Smart Waste Management and Bin	26
Figure 13: WasteHero (Danemark)	28
Figure 14:Bigbelly Smart Waste & Recycling System (États-Unis)	29
Figure 15:Bin-e (Pologne)	30
Figure 16:CleanCUBE (South Korea)	32
Figure 17:Framework scrum	36
Figure 18:UML logo	38
Figure 19:Diagramme de cas d’utilisation d’administration	41
Figure 20:Diagramme de cas d’utilisation d’un utilisateur	43
Figure 21:diagramme des séquences de l'authentification	45
Figure 22:Diagramme de séquence d’ajouter une alerte	46
Figure 23:Diagramme de séquence de localisation de poubelles	48
Figure 24:diagramme des classes	50
Figure 25::  carte Arduino Uno	51
Figure 26:Module ESP8266	52
Figure 27:Capteur ultrason HC-SR04	53
Figure 28:: module GPS NEO 6M	54
Figure 29:module LoRa RYLR998 de Reyax	55
Figure 30:capteur de température et d'humidité	55
Figure 31:servomoteur 9g	56
Figure 32:Architecture matérielle du système	74
Figure 33:Architecture logicielle du système	75
Figure 34:Architecture de communication du système	78
Figure 35:Connexion du capteur ultrason avec la carte Esp8266	79
Figure 36:mesure du niveau de remplissage	80
Figure 37:montage du dht11 avec l'ESP8266	81
Figure 38:montage du HCSR04 sur la carte ESP8266	81
Figure 39:connexion du servomoteur avec la carte ESP8266	82
Figure 40:code pour le contrôle du servomoteur	82
Figure 41:connexion du module GPS sur le module Esp8266	83
Figure 42:maquette interface mobile	84
Figure 43:maquette interface mobile localisation	84
Figure 44:maquette interface web connexion	85
Figure 45:architecture  de  l'API	86
Figure 46:Fichier de configuration de l’API	87
Figure 47:contrôleur de l’utilisateur	88
Figure 48:documentation des endpoints avec swagger-ui	89
Figure 49:liste des utilisateurs dans la BD	89
Figure 50:architecture de l’application web	90
Figure 51:code	91
Figure 52:affichage de la liste utilisateurs	91
Figure 53:formulaire de création de collaborateur	91
Figure 54:architecture de application mobile	92
Figure 55:Interface de  bienvenu	93
Figure 56:interface de connexion	94
Figure 57:interface de création de compte utilisateur	95



















Liste des tableaux
Tableau 1: Fiche descriptive du cas d'utilisation d’Administration du système	42
Tableau 2:Fiche descriptive du cas d'utilisation d’un citoyens (ménage)	44
Tableau 3:Fiche descriptive du scénario de l’authentification	46
Tableau 4:Fiche descriptive du scénario d’ajouter une alerte	47
Tableau 5:Fiche descriptive du scénario de localisation de poubelles	49
Tableau 6: Comparaison des technologies mobile	64
Tableau 7:Comparaison des technologiques frontend	67
Tableau 8:Comparaison des technologiques backend	70
Tableau 9:Comparaison du SQL et du NoSQL	71







 










Introduction Générale
Dans le contexte dynamique de l'amélioration continue de la santé humaine et de la préservation de l'environnement, la gestion des déchets se dresse comme un défi prédominant pour les nations en développement, avec une acuité particulière au Sénégal. L'insalubrité des espaces publics a atteint des niveaux alarmants, où les rues sont jonchées de détritus, les sachets plastiques s'amoncellent, et les systèmes de collecte et de gestion des déchets se révèlent inefficaces, en raison notamment du manque crucial d'informations disponibles.
Cette situation impose un fardeau financier substantiel à l'État, notamment en ce qui concerne la surveillance du personnel chargé de la gestion des déchets. En effet, la nécessité de maintenir un contrôle constant sur les activités liées à la collecte et au traitement des déchets représente un investissement financier considérable pour les autorités gouvernementales.
Face à l'urgence de la situation, le gouvernement sénégalais a pris des mesures proactives en lançant le projet de Promotion de la Gestion intégrée et de l’Économie des 
Déchets (PROMOGED) [01], dans le cadre de la phase 2 du Programme National de Gestion des Déchets (PNGD), avec le soutien de la Banque Mondiale (BM) et de l'Agence Espagnole pour la Coopération Internationale au Développement (AECID). Ce projet novateur vise à promouvoir la gestion intégrée et l'économie des déchets solides au Sénégal [02].
Le principal but poursuivi par PROMOGED est l'amélioration des conditions de santé publique et du bien-être de la population, tout en réduisant l'impact néfaste de la prolifération des déchets sur l'environnement. Pour assurer la mise en œuvre efficace de ce projet d'envergure, la responsabilité a été confiée à l’UCG [03].
Cependant le constat d'une certaine insuffisance dans l'arsenal technique mis en place par le gouvernement pour la gestion des déchets révèle des lacunes majeures dans le processus de pré-collecte, notamment en raison d'une approche abstraite et périodique. Cette inefficacité entraîne des problèmes tels que le débordement des poubelles et une gestion non optimale des tournées de vidage. De plus, le manque de connaissance précise de l'état de remplissage des conteneurs et leurs localisations contribue à une gestion imprécise. Cette situation conduit parfois les citoyens à jeter leurs déchets dans les rues, aggravant ainsi le problème d'insalubrité urbaine. Pour remédier à cette situation, Nous déployons un système automatisé de gestion des déchets, permettant un suivi en temps réel de l'état des poubelles, la détection des niveaux de remplissage et la proposition d'itinéraires optimaux pour les véhicules de collecte.
Dans cette perspective, UCG du département de Pikine a entrepris des initiatives visant à améliorer la gestion des déchets. Cela comprend la mise en place d'une plateforme de gestion des déchets et d'une application mobile destinée aux citoyens (ménages) et aux collecteurs. Ces efforts reflètent une approche proactive dans la recherche de solutions innovantes pour optimiser la gestion des déchets, réduire l'impact sur l'environnement et améliorer la qualité de vie des citoyens.
Pour mieux appréhender le sujet, nous avons scindé le travail en quatre (4) chapitres.
Le premier chapitre, intitulé "Présentation Générale", établit le cadre de notre travail en mettant en lumière notre collaboration étroite avec l'UCG dans le cadre de notre sujet. Cette collaboration se concentre sur les besoins spécifiques auxquels notre solution de gestion des déchets doit répondre, ainsi que les contraintes auxquelles elle est soumise.
Le deuxième chapitre, intitulé "Les concepts d’internet des objets et l’importance de l'IdO", offre une vue d'ensemble de l'Internet des objets (IdO). Il définit l'IdO, retrace son évolution, décrit ses composants clés comme les capteurs et les technologies de connectivité, et examine son architecture et la sécurité des données. Le chapitre explore également certains domaines les applications de l’IdO.
Le troisième chapitre, intitulé "Analyse et conception", effectue une étude approfondie des besoins du système à développer. Il examine certains besoins spécifiques auxquels la solution doit répondre ainsi que les divers outils et technologies qui seront utilisés pour atteindre nos objectifs.
Le quatrième et dernier chapitre, intitulé "Implémentation", détaille notre démarche pour établir un environnement de travail propice à la réalisation du projet, ainsi que les réalisations accomplies dans ce cadre. CHAPITRE 1 : 	Présentation générale
Introduction 
Ce chapitre sert d'introduction essentielle à notre étude, en fournissant une présentation simplifiée de l'Unité de Coordination de la Gestion des Déchets (UCG) ainsi qu'un aperçu clair du contexte, du problème et des objectifs de notre sujet de recherche.
1.1	Présentation de l’unité de Coordination de la Gestion des Déchets solides
L'UCG, résultat de la fusion en 2011 entre l’Agence pour la Propreté du Sénégal (APROSEN) et l’Entente de la Communauté d'Agglomérations de Dakar et Communauté d'Agglomérations de Rufisque (Cadak-Car) [04], opère sous l'égide du Ministère de l'Urbanisme, du Logement et de l'Hygiène Publique(MULHP). Sa mission principale consiste à accompagner les collectivités locales dans la gestion efficace de leurs compétences relatives aux déchets solides. Cette démarche vise à améliorer le cadre de vie en instaurant des infrastructures conformes aux normes, en supervisant le balayage, la collecte et le transport des déchets, ainsi qu'en favorisant la mobilisation sociale.
En tant qu'organisme, nous assumons également la responsabilité de concevoir la stratégie nationale de gestion des déchets, tout en renforçant les capacités des municipalités par la mise en œuvre de projets et programmes spécifiques. Notre engagement s'articule autour de la création d'une gestion intégrée des déchets, alignée sur les normes, afin de promouvoir un environnement sain et d'améliorer la qualité de vie au niveau local.
1.2	Organigramme de l’UCG
 
Figure 1:Organigramme de l’UCG
1.3	Présentation du sujet
1.3.1	Contexte
Au Sénégal, la gestion des déchets constitue un défi majeur en raison de la croissance démographique, de l'urbanisation rapide et des pratiques de consommation en évolution. Les centres urbains, tels que Dakar, connaissent une pression accrue sur leurs infrastructures de gestion des déchets en raison de l'augmentation de la production de déchets ménagers et industriels.
La situation est complexe en raison de plusieurs facteurs, notamment :
❖	Croissance urbaine rapide : 
Les centres urbains, en particulier Dakar, connaissent une urbanisation rapide, entraînant une augmentation de la production de déchets ménagers et industriels.
❖	Infrastructures insuffisantes :
Les infrastructures de gestion des déchets sont souvent insuffisantes pour faire face à cette croissance, conduisant à des problèmes tels que l'accumulation de déchets dans les rues, les décharges sauvages et la pollution.
❖	Problèmes environnementaux et de santé publique : 
Les pratiques de gestion informelles, telles que la combustion de déchets, contribuent à la pollution atmosphérique et posent des risques pour la santé publique, nécessitant une approche plus durable
	Manque de sensibilisation des populations : 
Une sensibilisation insuffisante des populations aux pratiques de gestion des déchets entrave les efforts de réduction et de recyclage des déchets.
	Utilisation excessive de plastiques :
L'usage généralisé et excessif de produits plastiques non biodégradables contribue de manière significative à la pollution et complique les efforts de gestion des déchets.
1.3.2	Problématique
La croissance urbaine rapide à engendrer une augmentation importante de la production de déchets, posant ainsi des défis pressants en matière de gestion des 
Les infrastructures actuelles de gestion des déchets au Sénégal s'avèrent souvent insuffisantes pour faire face à cette croissance, entraînant des problèmes tels que l'accumulation de déchets dans les rues et la pollution.
De nombreuses zones urbaines et périurbaines du pays manquent d'un système formel de collecte des déchets, ce qui conduit souvent à des pratiques de gestion informelles, telles que le dépôt sauvage ou la combustion des déchets, entraînant des problèmes environnementaux et de santé publique.
1.3.3	Objectifs
Notre objectif est, de prime abord, de permettre aux infrastructures actuelles de gestion des déchets d’être plus efficaces et de pouvoir surmonter l’ensemble des défis.
Pour y parvenir nous allons mettre en place un système automatisé de gestion des déchets permettant : 
❖	Une Surveillance en temps réel : Mettre en place dans un premier temps un système d’alerte faisant intervenir l’humain et dans un second temps un système de surveillance en temps réel qui utilise des capteurs connectés pour suivre le niveau de remplissage des bacs à déchets dans des zones spécifiques.
❖	Collecte de données : Les capteurs enverraient des données sur le niveau de remplissage des bacs à un centre de contrôle central, permettant une collecte de données précise et en temps réel sur la production de déchets dans différentes zones.
❖	Algorithmes prédictifs : Utilisez des algorithmes prédictifs pour anticiper les besoins en collecte de déchets, en tenant compte des tendances de production de déchets dans chaque zone, des jours de la semaine, des événements spéciaux, etc.
❖	Alertes automatisées : Lorsque le niveau de remplissage d'un bac atteint un seuil critique, le système envoie des alertes automatiques aux services de collecte de déchets, optimisant ainsi la planification des itinéraires de collecte.
❖	Engagement communautaire : Intégrez des fonctionnalités interactives pour impliquer la communauté, permettant aux résidents de signaler des problèmes ou de fournir des informations sur les déchets, favorisant ainsi une approche participative.
Nous avons pour ambition d'exploiter les avancées technologiques de l'Internet des objets (IdO) afin de mettre en œuvre une solution innovante pour optimiser la gestion des déchets dans notre pays. Cette solution intelligente sera basée sur différents systèmes de détection permettant de surveiller à distance l'état de remplissage des poubelles, de déclencher des alertes lorsque celles-ci atteignent leur capacité maximale, de proposer des itinéraires adaptés pour les véhicules de collecte des déchets, et de calculer de manière précise le pourcentage de déchets contenus dans les poubelles.
1.4	Contexte de la gestion des déchets au Sénégal
1.4.1	Définition
La notion de déchet est complexe et sujette à des interprétations variées en fonction des contextes culturels, juridiques et économiques. Selon la loi sénégalaise du 15 Janvier 2001 portant code de l’environnement qui appuie peut-être la loi française définit un déchet comme étant : « Toute substance solide, liquide, gazeuse, ou résidu d’un processus de production, de transformation, ou d’utilisation de toutes autres substances éliminées, destinées à être éliminées ou devant être éliminées en vertu des lois et règlements en vigueur » [05]. Cette définition inclut donc tout élément qui est jeté ou abandonné par son détenteur.
Il est important de noter que tous les éléments considérés comme des déchets ne sont pas nécessairement inutilisables. Certains peuvent être réutilisés, recyclés ou transformés en nouvelles matières premières. Cependant, les déchets ultimes, qui ne peuvent pas être recyclés ou valorisés, doivent être stockés de manière appropriée pour éviter toute pollution de l'environnement.
Cette définition souligne l'importance de la gestion responsable des déchets, en encourageant la réduction à la source, le recyclage et la valorisation des matériaux, tout en veillant à limiter l'impact environnemental des déchets ultimes.
1.4.2	Causes des déchets
Les activités humaines ont toujours engendré des déchets, mais l'avènement de l'urbanisation et de l'industrialisation a considérablement amplifié ce phénomène. Autrefois, les déchets étaient souvent jetés dans la nature sans véritable conscience des risques environnementaux qu'ils posaient. La collecte et le traitement des déchets étaient rares, et les pratiques telles que le don des déchets alimentaires aux animaux ou la combustion des déchets étaient courantes. À cette époque, la valeur des biens matériels était appréciée, et leur remplacement coûtait souvent cher, incitant ainsi à entretenir et à prolonger leur durée de vie.
Cependant, avec l'avènement de la société de consommation moderne, cette dynamique a radicalement changé. Les produits commercialisés sont désormais largement emballés pour des raisons de protection, de promotion et de distribution. Une fois achetés, ces emballages deviennent rapidement des déchets, contribuant ainsi à l'essor de ce que certains ont qualifié de 'civilisation du déchet'. De plus, l'importation de produits emballés de l'extérieur aggrave encore cette situation en introduisant davantage de déchets dans le circuit local de gestion des déchets.
1.4.3	Impacts des déchets
Les déchets constituent une menace significative pour l'environnement, la santé humaine et l'économie, nécessitant une gestion efficace et durable pour atténuer leurs effets néfastes.
❖	Impact sur l'environnement :
La mauvaise gestion des déchets contribue au changement climatique et à la pollution atmosphérique, ce qui compromet directement la santé des écosystèmes et des espèces. Les décharges, souvent considérées comme la dernière option dans la hiérarchie des déchets, émettent du méthane, un gaz à effet de serre potentiellement plus dommageable que le dioxyde de carbone. De plus, les décharges peuvent contaminer le sol et les eaux souterraines, menaçant ainsi les ressources naturelles et la biodiversité.
Une gestion adéquate des déchets, telle que l'incinération et le recyclage, peut contribuer à réduire les émissions de gaz à effet de serre et à prévenir la pollution de l'air, du sol et de l'eau. Par exemple, l'incinération des déchets peut être utilisée pour produire de la chaleur ou de l'électricité, tandis que le recyclage permet de réduire la demande de nouvelles matières premières et d'économiser de l'énergie.
❖	Impact sur la santé humaine :
Les déchets affectent directement la santé humaine en contaminant les sources d'eau potable, en polluant l'air et en exposant les populations à des substances toxiques. Les écosystèmes marins et côtiers sont particulièrement vulnérables aux déchets, mettant en danger de nombreuses espèces marines et la sécurité alimentaire des populations qui en dépendent.
Indirectement, les déchets contribuent également à la propagation de maladies infectieuses et à l'augmentation des taux de morbidité dans les populations exposées à des environnements contaminés par les déchets.
❖	Perte économique et coûts de gestion :
La gestion des déchets représente une charge financière importante pour les gouvernements et les communautés locales. La collecte, le transport, le traitement et l'élimination des déchets nécessitent des investissements considérables en termes d'infrastructure, de main-d'œuvre et de technologie. Cependant, ces coûts peuvent être compensés par les bénéfices économiques potentiels du recyclage et de la valorisation des déchets, qui peuvent générer des revenus supplémentaires et créer des emplois dans l'économie circulaire.
En outre, la gestion inadéquate des déchets peut entraîner des pertes économiques indirectes, telles que la dégradation des écosystèmes naturels, la diminution de la productivité agricole et touristique, et la réduction de la qualité de vie des populations affectées.
1.4.4	Historique de la gestion des déchets au Sénégal
L'histoire de la gestion des déchets solides au Sénégal a connu plusieurs étapes distinctes [06]. Depuis l'indépendance en 1960, les autorités sénégalaises ont tenté divers programmes de gestion des déchets, mais les résultats ont été mitigés.
De 1960 à 1971, les services municipaux étaient responsables du balayage des rues, du dessablage, de la collecte et de l'élimination des ordures ménagères. Cependant, ce système était entravé par des équipements insuffisants, une couverture territoriale incomplète, une gestion du personnel et des équipements peu rigoureux, des pressions politiques conduisant à un recrutement excessif et un manque de suivi et de mécanismes d'évaluation des activités.
En 1971, la société privée Société Africaine de Développement Industriel et de Promotion (SOADIP) a repris l'ensemble des services jusqu'alors assurés par les services municipaux. Cependant, des difficultés avec la SOADIP ont commencé à apparaître à partir de 1981, notamment en raison du manque d'entretien et de renouvellement des équipements et d'un personnel pléthorique.
En 1984, avec la cessation des activités de la SOADIP, la gestion communale reprend le contrôle avec l'intervention de la direction des services communaux. L'absence de capacité technique et de ressources de la commune a perduré jusqu'à la création d'une nouvelle société de nettoyage à économie mixte, la Société Industrielle d'Aménagement Urbain du Sénégal (SIAS). La défaillance du service public de l'Etat et l'incapacité de la mairie à le remplacer avaient plongé Dakar dans une saleté répugnante.
Cependant, la SIAS, créée en 1985 pour remplacer la SOADIP, subira le même sort que cette dernière une décennie plus tard et fermera ses portes en 1995. Depuis la dissolution de la SIAS, une relève est apportée par l’Agence pour l'Exécution des Travaux d'Intérêt Public(AGETIP), qui exerce sa mission en concertation avec la Coordination des Associations et des Mouvements de Jeunes de la Communauté Urbaine de Dakar (CAMCUD).
A partir de 2006, le Ministère de l'Environnement et la convention CADAK-CAR ont pris en charge la gestion des déchets dans la région de Dakar et ont travaillé avec la société parisienne VEOLIA [07] et les sociétés concessionnaires. La CADAK-CAR est une émanation des collectivités territoriales de la région de Dakar et est composée de la CADAK, qui regroupe les villes de Dakar, Pikine, et Guédiawaye, et de la CAR, qui regroupe la ville de Rufisque, les communes de Bargny, Diamniadio, et Sébikhotane, et les communautés rurales de Sangalkam et Yenne. Elle a été créée par une convention signée par leurs deux présidents et assure la maîtrise d'ouvrage du programme de gestion des déchets solides urbains de la région de Dakar suite au décret n° 2006-05 du 9 janvier 2006 portant transfert dudit programme. 
En 2011, l’UCG a été créée pour accompagner les collectivités locales dans la gestion des déchets. L'UCG est chargée de mettre en place des infrastructures conformes, de gérer la collecte et le transport des déchets, et de renforcer les capacités des municipalités.
Actuellement, l'UCG met en œuvre le programme "Zéro Déchet" pour améliorer la santé publique, le bien-être des populations et réduire l'impact environnemental des déchets. Ce programme comprend des opérations régulières de nettoyage des voiries et l'installation de zones de stockage des déchets surveillées en permanence.
Enfin, le projet de loi 06/2022 prévoit la création de la Société Nationale de Gestion intégrée des Déchets (SONAGED), qui remplacera l'UCG. La SONAGED vise à favoriser l'intercommunalité, à développer des infrastructures de valorisation des déchets et à promouvoir l'économie circulaire.
1.4.5	Analyse des applications existantes de gestion des déchets
1.4.5.1	Les points de regroupement normalisés (PRN)
« Au total plus 93 PNR ont été créés. En effet, 63 sont à Dakar et les 30 dans les autres régions. Leur mise en place permet de faire une pré collecte des déchets solides dans les différents quartier ». Cela permet de résoudre le problème de l’entassement des ordures dans les quartiers avant l’arrivée des camions poubelles.
Les PNR permettent également le tri et le recyclage. Ils sont situés à des endroits stratégiques, pour faciliter leur utilisation par les habitants.
 
Figure 2:Point de regroupement normalisé des déchets par l'UCG

1.4.5.2	Allô Gravats
ALLÔ GRAVATS intervient auprès des entreprises, des collectivités, et des particuliers. ALLÔ GRAVATS est une entreprise de débarras, et une entreprise de nettoyage dans les domaines suivants : débarras maison, débarras appartement, débarras cave, enlèvement déchets verts, enlèvement gravats…

	Figure 3:Allô Gravats
1.4.5.3	Le programme allo déchets
UCG a mis en place ce système pour répondre au besoin des citoyens. C’est un service de plainte mis à la disposition de la population dans le besoin de remonter des alertes liées à l’insalubrité.
Les villes sont confrontées à un fléau de déchets : décharges à ciel ouvert dans les lieux publics, déversement d’ordures sur les routes et les trottoirs, etc. Afin de remédier à cette situation, UCG a décidé de lancer le projet « Allo Déchets ». Cette initiative a pour objectif majeur d’éradiquer tout dépôt d’ordures dans ces points.
  
Figure 4:programme allo déchets
1.4.5.4	Cleaning Day-Besup Setal
Dans le cadre d’une nouvelle initiative lancée par le chef de l’État le 4 janvier 2020 pour un Sénégal propre. L’UCG s’est associé aux journées mensuelles de nettoyage
Ce Cleaning Day – Besup Setal est prévu pour chaque premier samedi du mois
Le problème de la gestion des ordures concerne les plusieurs acteurs, la population qui la produit et les techniciens de surface qui les gèrent. Étant un peu difficile de gérer les ordures ménagères nous proposons une solution qui peut faciliter à la fois la collecte des ordures aux techniciens de surface et de pouvoir signaler les déchets en cas de besoin.
 
Figure 5:Cleaning Day – Besup Setal
1.4.6	Problèmes de la gestion des déchets
L'histoire de la gestion des déchets au Sénégal met en lumière les multiples tentatives de résolution du problème des déchets à travers la mise en place de différentes sociétés de gestion, telles que la SOADIP et la SIAS. Malheureusement, ces entreprises ont toutes échoué malgré les contrats lucratifs obtenus, laissant derrière elles des travailleurs souvent non rémunérés pendant plusieurs mois et confrontés à des conditions difficiles.
Concernant la gestion des déchets, plusieurs problèmes persistent dans le pays. Parmi ceux-ci, on peut citer :
❖	Le débordement des poubelles sans surveillance,
❖	Le comportement des populations non éduquées ou non impliquées qui jettent les ordures par terre lorsque les poubelles sont pleines,
❖	La pollution sonore lors des collectes,
❖	Les itinéraires de collecte non optimisés,
❖	Le gaspillage de carburant et de temps lors de collectes inutiles,
❖	Le travail désorganisé et sporadique des équipes de ramassage,
❖	La non-séparation des déchets,
❖	L'absence de filières formelles de gestion des déchets,
❖	Le manque de sensibilisation de la population sur la gestion des déchets etc.
Conclusion
Notre objectif est de résoudre efficacement et de manière optimale le problème majeur que constitue la gestion des déchets dans notre pays. En tirant parti des technologies de l'IdO, nous visons à transformer le processus de collecte des déchets en un système plus intelligent et efficace, contribuant ainsi à réduire les impacts environnementaux tout en améliorant la qualité de vie de nos concitoyens.















CHAPITRE 2 : 	Les concepts d’internet des objets son importance
Introduction 
Le concept d'un réseau d'appareils intelligents a été discuté dès 1982, avec une machine à Coca modifiée à l'Université Carnegie Mellon devenant le premier appareil connecté à Internet, capable de signaler son inventaire et si les boissons nouvellement chargées étaient froides [08]. Plus tard, Kevin Ashton (né en 1968), un pionnier britannique de la technologie, est connu pour avoir inventé le terme « Internet des objets (IdO) » pour décrire un système où Internet est connecté au monde physique via des capteurs omniprésents. Il a d'abord proposé le concept d'IdO en 1999, et il a qualifié l'IdO d'objets connectés identifiables de manière unique avec la technologie d'identification par radiofréquence (SIG). [09]
 Cependant, la définition exacte de l'IdO est encore dans le processus de formation qui est soumis aux perspectives prises. Selon l'Union Internationale des Télécommunications (UIT), l'Internet des objets (IdO) est une « infrastructure mondiale pour la société de l'information, qui permet de disposer de services évolués en interconnectant des objets (physiques ou virtuels) grâce aux technologies de l'information et de la communication interopérables existantes ou en évolution ». [10]
 Une autre définition de l’IdO est donnée dans la littérature : L'Internet des objets (IdO) est le réseau d'objets physiques - appareils, instruments, véhicules, bâtiments et autres éléments intégrés à l'électronique, aux circuits, aux logiciels, aux capteurs et à la connectivité réseau qui permet à ces objets de collecter et d'échanger des données. Il permet aux objets d'être détectés et contrôlés à distance sur l'infrastructure réseau existante, créant des opportunités pour une intégration plus directe du monde physique dans les systèmes informatiques, et résultant en une efficacité et une précision améliorée. [11]
Certaines applications IdO préliminaires ont déjà été développées dans les secteurs de la santé, des transports et de l'automobile [12]. Les technologies IdO en sont à leurs balbutiements ; cependant, de nombreux nouveaux développements se sont produits dans l'intégration d'objets avec des capteurs dans Internet.

2.1	L’architecture de l’internet des objets
Une architecture réseau IdO est un ensemble de briques de systèmes IdO qui communiquent entre eux pour relier le monde tangible des objets au monde virtuel des réseaux et du cloud remplissant des fonctions spécifiques. La structure en cinq couches est largement acceptée et comprend généralement :
❖	La couche de perception (ou capteur) [13] : Egalement appelée couche d'objet, constitue la première couche de l'IdO. Elle représente les objets physiques de l'IdO et est chargée de collecter, traiter les informations de base, et de fournir diverses fonctionnalités telles que la position physique, la température, le poids, et le mouvement. Cette couche numérise les données d'un environnement donné et les transmet à la couche supérieure via des canaux sécurisés. Elle comprend les appareils physiques équipés de capteurs pour collecter des données sur l'environnement, comme des capteurs de température, des caméras, et des actionneurs.
	La couche réseau [13] : Ou la couche de communication est responsable du transport des données vers le centre de traitement de l'information, en utilisant des moyens de transmission filaires ou sans fil tels que la 3G, le Wi-Fi, le ZigBee et le LoRa. Elle gère les protocoles de communication comme 6LoWPAN, nécessaires pour l'adressage et le routage des données de millions d'objets connectés, et assure la connectivité des appareils IdO au réseau, permettant aux dispositifs de transmettre les données collectées.
❖	La couche de traitement [13] : Egalement appelée couche Middleware, gère les services offerts par chaque objet IdO. Elle lie les informations collectées à des bases de données et applique des traitements pour permettre des décisions automatiques. Cette couche permet aux développeurs d'accéder à des services sans se soucier de l'interopérabilité des objets ou des plateformes matérielles. Elle est responsable de collecter, stocker, traiter et gérer les données des dispositifs IdO, impliquant des bases de données, des systèmes de gestion de flux de données et des plateformes de cloud computing.
	La couche d'application [13] : Elle est la dernière couche de l'architecture IdO. Elle permet aux utilisateurs finaux de bénéficier des services fournis par les couches inférieures. Cette couche est responsable de l'interface utilisateur, de l'expérience utilisateur, de la sécurité et de la gestion des données. Elle représente les applications et les services qui utilisent les données IdO pour offrir des fonctionnalités spécifiques, telles que des applications mobiles, des interfaces web, des systèmes de contrôle industriels et des systèmes de gestion de l'énergie.
❖	La couche de sécurité [13] : Cette couche est souvent représentée de manière implicite et constitue même de l’architecture. Elle est essentielle pour protéger les données, les dispositifs et l'ensemble du système contre les menaces et les attaques. 
•	Sécurisation des Dispositifs : Protection des capteurs et actuateurs avec des techniques telles que l'authentification et le chiffrement.
•	Sécurisation des Passerelles : Utilisation de firewalls et de mécanismes d'authentification pour sécuriser les passerelles.
•	Sécurisation des Communications : Cryptage des données en transit et utilisation de protocoles sécurisés.
•	Sécurisation du Cloud/Serveurs : Chiffrement des données au repos et contrôle d'accès strict sur les serveurs et le cloud.
•	Surveillance et Gestion des Incidents : Mise en place de systèmes pour la surveillance en temps réel et la gestion proactive des incidents de sécurité.
•	Sécurisation des Applications et des Interfaces Utilisateur : Assurer que les applications web et mobiles, ainsi que les interfaces utilisateur, sont sécurisées contre les attaques comme l'injection SQL, le cross-site scripting (XSS)
Chaque couche de cette architecture joue un rôle crucial dans le fonctionnement global du système IdO, en assurant la collecte, le transfert, le traitement et l'utilisation efficaces des données générées par les appareils connectés.

 
Figure 6:Architecture réseau IdO
2.2	Les technologies de communication dans l’IdO
Dans l'IdO, les technologies de communication sont essentielles pour permettre aux objets connectés de transmettre des données de manière efficace et fiable. Ces technologies varient en fonction de la portée de communication requise, qu'il s'agisse d'une courte portée pour la communication entre des appareils à proximité ou d'une longue portée pour la connectivité sur de plus grandes distances. Voici quelques-unes des technologies les plus couramment utilisées dans l'IdO pour la communication à courte et à longue portée :
2.2.1	Les technologies de communication à courte portée
❖	Bluetooth 
Le Bluetooth est une technologie sans fil à courte portée largement utilisée pour la communication entre les appareils à proximité les uns des autres, comme les smartphones, les écouteurs sans fil, les montres connectées, etc. Bluetooth Low Energy (BLE) est particulièrement populaire dans les applications IdO en raison de sa faible consommation d'énergie [14].
❖	Zigbee 
Le Zigbee est un protocole de communication sans fil basse consommation conçu spécifiquement pour les réseaux de capteurs et les applications IdO. Il fonctionne sur une courte portée et est idéal pour les environnements où de nombreux appareils doivent communiquer entre eux de manière fiable.
❖	Z-Wave 
Le Z-Wave est un autre protocole sans fil à courte portée utilisé dans l'IdO, principalement pour les applications de domotique. Il offre une bonne portée et une faible consommation d'énergie, ce qui le rend adapté aux environnements résidentiels et commerciaux.
2.2.2	Les technologies de communication longue portée
❖	LPWAN
Le LPWAN « Low Power Wide Area Network » signifie réseau étendu à faible consommation d’énergie. Faible puissance, la technologie sans fil (LPWAN) est la réponse à la nécessité d'avoir un système qui connecte à internet des objets à faible consommation d’énergies, pour la saisie de données à faible débit. Les objets connectés n'envoient souvent pas de données en continu et sont en phase quasi dormante lorsqu’une action n’est pas nécessaire et sont alimentés souvent par batterie. Ces technologies utilisent les ondes radio très efficaces dans les zones rurales. Cependant, les zones urbaines avec des immeubles de grandes hauteurs, des arbres et des maisons sont plus difficiles à parcourir par les ondes radio. En ce qui concerne les aspects réseau, les réseaux LPWAN sont généralement répartis en topologie en étoile.
❖	Wi-Fi
 Le Wi-Fi est largement utilisé pour la connectivité à longue portée dans les environnements IdO où une bande passante élevée est requise. Bien que le Wi-Fi soit souvent associé à une communication à courte portée, ses versions plus récentes comme le Wi-Fi HaLow offrent une meilleure portée pour les applications IdO.
❖	LTE-M
 3GPP a créé la communication de type machine LTE (LTE-M) la norme. Lte-m transmet dans la bande subghz sous licence, avec des fréquences allant de 700 à 900 MHz. Les débits de données en liaison montante et descendante sont d'environ 1 Mbps. Cette approche à faible consommation d'énergie peut aider à prolonger la durée de vie des terminaux alimentés par batterie jusqu'à 10 à 20 années. Lte-m utilise également l'infrastructure sans fil cellulaire existante pour la rendre plus robuste et sécurisée pour les services avec des exigences de qualité élevées [15].
❖	Lora
LoRa (Long Range) est une technologie sans fil brevetée développée par Cycleo de Grenoble et acquise par Semtech en 2012. Cette technologie se caractérise notamment par sa grande portée et sa faible consommation d'énergie, ce qui la rend particulièrement adaptée aux objets connectés qui doivent être autonomes en énergie pendant plusieurs années. 
Le réseau LoRa permet des transmissions à longue portée (jusqu'à plus de 15 km en zones rurales) avec une faible consommation d'énergie, ce qui en fait une solution idéale pour les applications IdO (Internet des objets) qui nécessitent une connectivité longue portée et une faible consommation d’énergie [16].
❖	LoRaWAN
 LoRaWAN est un protocole de communication à longue portée conçu spécifiquement pour les applications IdO à faible consommation d'énergie. Il utilise la technologie LoRa pour une communication sans fil à bas débit sur de longues distances, ce qui le rend idéal pour les applications de suivi, de surveillance et d'automatisation à distance.
	Internet des objets à bande étroite (NB-IoT) 
Internet des objets à bande étroite (NB-iot), également connu sous le nom de LTE Cat NB1, est un autre dérivé de la norme LTE. Il est basé sur une communication à bande étroite et utilise une bande passante de 180khz. Par conséquent, les débits de données sont considérablement réduits (à-propos 250 KBPS pour la liaison descendante et 20 KBPS pour la liaison montante), ce qui rend les mises à jour difficiles à mettre en œuvre avec NB-iot. Nb-iot peut utiliser 3 différents modes : bande de garde LTE, autonome et intra bande. Le mode intra bande utilise la bande de fréquence LTE, la bande de fréquence protégée utilise la partie inutilisée de la bande de fréquence LTE, et la bande de fréquence indépendante utilise la bande de fréquence dédiée (comme la bande de fréquence GSM). Nb-iot ne prend pas en charge le transfert et ne vaut pas la peine d'être envisagé pour les applications IdO mobiles [15].
❖	La 5G
La cinquième génération de technologie mobile, caractérisée par la vitesse de transmission des données et une réduction de la latence de bout en bout. La 3GPP a proposé trois catégories de technologies principales [15] : eMBB - enhanced Mobile Broadband, Haut débit mobile amélioré qui permet une livraison plus dynamique et adaptative de la capacité en temps réel et prend en charge sans effort de nouveaux services tels que la réalité virtuelle et la réalité augmentée. Des débits de données extérieurs allant jusqu’à 2 Gbps et à l’intérieur jusqu’à 20 Gbps sont envisagés. mMTC - massive Machine Type Communications, Communications de type machine massive. L'objectif de cette catégorie est de fournir une très grande densité de connectivité offrant une connectivité globale pour plus d'un million de périphériques par kilomètre carré au niveau du réseau. Cette catégorie propose de nombreuses applications telles que les villes intelligentes, les réseaux électriques intelligents etc. uRLLC - Ultra Reliable Low Latency Communications, Communications ultra-fiables et à faible temps de latence. Cette technologie ouvre une nouvelle dimension à l’application de réseaux sans fil tels que les interventions d’urgence, la robotique collaborative, la cyber santé, les drones etc.
Ces technologies de communication à courte et longue portée offrent une gamme d'options aux développeurs d'applications IdO, leur permettant de choisir la solution la mieux adaptée aux besoins spécifiques de leurs projets en termes de portée, de bande passante, de consommation d'énergie et de coût.
2.3	 Secteurs d’applications des objets connectés
L’IdO est applicable dans plusieurs domaines de la vie. Elle permet ainsi de rendre ces secteurs plus intelligents dans leurs interactions. La domotique, la santé, l’élevage ainsi que l’industrie restent des exemples parfaits pour témoigner son intégration facile dans le monde actuel.

 
Figure 7:Secteurs d’application de l'IdO
2.3.1	L’Internet des objets dans la domotique
La domotique est une spécialité du bâtiment regroupant les technologies de l’électronique, de l’information et des télécommunications utilisées pour contrôler, automatiser et programmer des équipements de l’habitat. Elle vise à assurer les fonctions de sécurité, de confort, de gestion d’énergie et de communication qu’on peut retrouver dans une maison.
Si on prend par exemple la solution de Pluzzy de Toshiba, celle-ci est plutôt centrée sur la gestion de l’énergie et du chauffage [17]. D’autres sont plutôt axées sur la sécurité, comme par exemple MyFox. [18]. D’autres cumulent toutes les fonctions, mais moins poussées que des boxes spécialisés. Etc...
 
Figure 8:Internet des objets dans la domotique

2.3.2	L’Internet des objets dans le domaine de la santé
L'Internet des Objets permet en effet de dépasser les situations de déserts médicaux, notamment grâce aux outils de télédiagnostic : les bracelets connectés enregistrent les informations de santé (vaccins, pathologies, allergies, etc.) et permettent un suivi régulier. Alors, on parle d’Internet des Objets Médicaux (en anglais, Internet of Medical Things - IoMT). Les applications mobiles de télémédecine permettent de réaliser des diagnostics à distance via un simple téléphone connecté à Internet. C'est par exemple le cas de la startup Health Q en Afrique du Sud qui   afin d’aider ou d’envoyer une notification urgente au docteur, tuteur et/ou un proche [19].
 
Figure 9:Internet des objets dans le domaine de la santé
2.3.3	L’Internet des Objets dans le domaine de l’agriculture
L'Internet des objets (IdO) transforme l'industrie agricole et permet aux agriculteurs de faire face aux énormes défis auxquels ils sont confrontés. Alors, on parle d’agriculture intelligente (en anglais, smart agriculture ou smart farming). Ainsi, de nouvelles applications IdO innovantes répondent aux problèmes de l'agriculture en augmentant la qualité, la quantité, la durabilité et la rentabilité de la production agricole. Parmi les nombreux avantages que l'IdO apporte, sa capacité à innover dans le paysage des méthodes agricoles actuelles est absolument révolutionnaire. Les capteurs IdO capables de fournir aux agriculteurs des informations sur les rendements des cultures, les précipitations, les infestations de ravageurs et la nutrition des sols sont inestimables pour la production et offrent des données précises qui peuvent être utilisées pour améliorer les techniques agricoles au fil du temps [20].

 
Figure 10:Internet des Objets dans le domaine de l’agriculture
2.3.4	L’Internet des Objets dans le domaine de l’industrie
Tout comme dans les autres secteurs, les problématiques rencontrées dans celui de l’industrie trouvent leur résolution dans l’utilisation des objets connectés. En effet l’utilisation de ceux-ci permet aux entreprises de rester dans la compétitivité leur facilitant ainsi :
•	L’optimisation des coûts de maintenance ;
•	L’optimisation des coûts d’exploitation (logistique, production, etc.) ;
•	L’offre de nouveaux services et de nouveaux modèles économiques ;
•	La traçabilité et la sécurité.
L’utilisation des objets connectés permet toutefois à certaines industries qui étaient laissées aux oubliettes d’améliorer leurs services ou même d’innover pour être compétitives sur le marché. [20] La figure 4 ci-dessous donne une illustration des différentes applications IdO.
 
Figure 11:Internet des Objets dans le domaine de l’industrie
2.4	Importance de l'IdO dans la gestion des déchets
La problématique de ce mémoire porte sur l'intérêt de la gestion intelligente des déchets au Sénégal, en tant que politique et système relativement nouveau dans le pays, qui vise à résoudre les problèmes de collecte inefficace des déchets, de suivi de la localisation et du niveau de remplissage des poubelles, ainsi que de sensibilisation des résidents à l'importance de la gestion des déchets. Cette problématique soulève des questions telles que :
●	Quels sont les défis et les enjeux de la gestion des déchets au Sénégal ?
●	Comment la gestion intelligente des déchets peut-elle aider à résoudre ces problèmes ?
●	Quels sont les avantages et les limites de la gestion intelligente des déchets par rapport aux approches traditionnelles de gestion des déchets ?
●	Comment les autorités sénégalaises peuvent-elles encourager et soutenir la mise en œuvre de la gestion intelligente des déchets ?
●	Comment mesurer l'impact de la gestion intelligente des déchets sur l'environnement, la santé publique et l'économie ?
L'intérêt de cette problématique réside dans sa pertinence pour les travailleurs et les autorités sénégalaises, qui cherchent des solutions innovantes pour résoudre les problèmes de gestion des déchets, ainsi que pour la recherche scientifique, qui peut apporter des contributions importantes à la compréhension des enjeux liés à la gestion des déchets dans les pays en développement.

2.5	Étude et exemples d’application de gestion de déchets avec l'IdO
•	Smart Waste Management and Bin
Mohammad Aazam et ses co-auteurs [21] soulignent que l'augmentation constante de la population, l'urbanisation, les migrations et le changement de mode de vie entraînent une hausse significative de la production de déchets solides municipaux. La gestion des déchets devient ainsi un défi majeur, tant pour les pays en développement que pour les pays développés. En 2016, ils proposent une gestion intelligente des déchets basée sur le cloud pour les villes intelligentes, impliquant trois types principaux d’entités : les utilisateurs générant les déchets, les collecteurs/administrateurs de la ville et les parties prenantes. Ce système affecte positivement le mode de vie, les soins de santé, l'environnement, le recyclage et plusieurs autres industries.
Les auteurs constatent que les tendances actuelles de gestion des déchets ne sont pas suffisamment sophistiquées pour garantir un mécanisme robuste et efficace. Une gestion intelligente des déchets est cruciale pour notifier en temps opportun le statut des déchets et informer toutes les parties prenantes. Cela permettrait non seulement d'attirer et d'identifier les parties prenantes, mais aussi de créer des méthodes plus efficaces de recyclage et de réduction des déchets, rendant ainsi la gestion globale des déchets plus efficace.
Aazam et ses co-auteurs proposent un mécanisme de gestion intelligente des déchets basé sur le cloud, où les poubelles sont équipées de capteurs capables de notifier leur niveau de remplissage et de télécharger ces données sur le cloud. Les parties prenantes peuvent accéder aux données depuis le cloud, permettant à l'administration municipale et à la gestion des déchets d'optimiser les itinéraires et de sélectionner le chemin de collecte des déchets en fonction du statut des poubelles dans une métropole. Cela contribue à l'efficacité énergétique et temporelle.
 
  
Figure 12: Smart Waste Management and Bin





•	Systèmes de gestion des déchets utilisant l'IdO (Kenya)
Gestion efficace et rentable
Au Kenya, l'IdO est utilisé pour gérer les déchets de manière efficace et économique. Certaines entreprises ont mis au point des systèmes basés sur des capteurs pour surveiller la quantité de déchets dans les bacs à travers le pays. Ces capteurs transmettent des données via des cartes SIM IdO à un système centralisé, qui déclenche des alertes lorsque les bacs atteignent un certain niveau. Ces alertes permettent aux entreprises de gestion des déchets de collecter et d'éliminer rapidement les déchets excédentaires.
Avantages économiques et environnementaux
Ce système permet de réduire les coûts de gestion des déchets jusqu'à 40%, ce qui en fait une solution efficace et respectueuse de l'environnement au Kenya.
Collaboration avec IBM dans le comté de Nairobi
Dans le comté de Nairobi, IBM a contribué à l'introduction d'une solution de gestion des déchets basée sur l'IdO. Cette solution implique l'installation de capteurs intelligents dans la flotte de collecte des déchets, permettant de surveiller les camions en temps réel et de créer une carte numérique des rues de Nairobi. Ces capteurs vérifient également les décharges, suivent l'emplacement des camions, le temps passé dans le trafic et le temps consacré à la collecte des déchets.
Avantages élargis
La solution offre également des avantages supplémentaires, comme la surveillance du comportement des conducteurs, la détection des dos d'âne et des nids-de-poule, et la vérification de la consommation de carburant. Pendant la période d'essai, le volume des déchets collectés a considérablement augmenté grâce aux capteurs intelligents, soulignant les avantages et l'efficacité de l'adoption de la technologie IdO dans la gestion des déchets [22]



•	WasteHero (Danemark)
WasteHero déploie des capteurs IdO pour surveiller les niveaux de remplissage des conteneurs à déchets en temps réel. Ces capteurs envoient des données à une plateforme centrale qui analyse les informations pour optimiser les itinéraires de collecte. Grâce à cette technologie, les trajets inutiles sont réduits, ce qui diminue les émissions de CO₂ et améliore l'efficacité opérationnelle. En outre, les capteurs permettent de signaler immédiatement les conteneurs endommagés ou débordants, assurant ainsi un service plus réactif et fiable [23].

  
Figure 13: WasteHero (Danemark)



•	Bigbelly Smart Waste & Recycling System (États-Unis)
Bigbelly utilise des bacs intelligents pour le compost et le recyclage, principalement dans les zones résidentielles multifamiliales. Les bacs sont équipés de capteurs IoT qui envoient des alertes lorsque les bacs sont pleins. L'application mobile Smart Compost permet aux résidents de participer facilement au programme de compostage. Grâce à l'optimisation des itinéraires de collecte, le système Bigbelly réduit les déplacements inutiles, améliorant ainsi l'efficacité et réduisant les coûts opérationnels. [23]
  
 
Figure 14:Bigbelly Smart Waste & Recycling System (États-Unis)

•	Bin-e (Pologne)
Bin-e est une poubelle intelligente basée sur l'IA, conçue pour simplifier le recyclage dans les lieux publics. Elle trie et compresse automatiquement les déchets, contrôle le niveau de remplissage et traite les données pour une gestion pratique des déchets. Ce conteneur intelligent classe les déchets selon les règles locales, facilitant ainsi le recyclage et la valorisation des déchets dans une économie circulaire. En tant que dispositif IoT, il collecte des données en temps réel sur le taux de remplissage et le type de déchets, permettant aux gestionnaires de locaux d'optimiser les collectes. Bin-e améliore significativement le taux de tri, réduisant les erreurs humaines de 70% à moins de 8%, et contribue à diminuer les émissions de CO2 en évitant les trajets de collecte inutiles. Facile à intégrer dans les environnements de bureau, Bin-e utilise la reconnaissance photo pour trier les déchets efficacement. [25]
 
 
Figure 15:Bin-e (Pologne)
•	CleanCUBE (South Korea)
CleanCUBE est une solution de gestion des déchets alimentée par l'énergie solaire, conçue pour les espaces publics. Il augmente la capacité de stockage des déchets jusqu'à cinq fois par rapport aux poubelles traditionnelles, réduisant ainsi la fréquence des collectes jusqu'à 80%. CleanCUBE communique en temps réel les données collectées à travers une transmission sans fil à la plateforme de surveillance CleanCityNetworks. Il offre des fonctionnalités optionnelles telles que des habillages graphiques personnalisés et un routeur WiFi pour répondre aux besoins spécifiques des utilisateurs. CleanCUBE contribue à réduire les coûts opérationnels et l'empreinte carbone en optimisant la gestion des déchets urbains. Cette solution innovante améliore l'efficacité de la collecte des déchets, assurant des espaces publics plus propres et plus durables [26]. 

 
 

Figure 16:CleanCUBE (South Korea)
Conclusion
Dans ce chapitre, nous avons abordé deux parties principales : la première partie traitant le contexte de la gestion des déchets au Sénégal et la seconde le concept de l’Internet des objets. 
Le chapitre suivant présente l’analyse et la conception des besoins de notre projet.






 
CHAPITRE 3 : 	Analyse et conception 
Introduction
Le chapitre d'analyse et de conception de ce projet de gestion des déchets met en lumière l'importance de comprendre les besoins de la structure du projet, incluant l'identification des acteurs et la définition des fonctionnalités du système, avec la création de diagrammes de cas d'utilisation. 
Cette phase d'analyse répond à la question "Que faut-il faire ?", en offrant une vision claire du problème à résoudre et du système à réaliser, tout en cernant les défis et les objectifs du projet.
Quant à la conception, elle répond à la question "Comment ?", en décrivant de manière précise et sans ambiguïté le système à venir, utilisant un langage de modélisation approprié. L'accent est mis sur la création d'une solution conceptuelle qui répond de façon adéquate aux exigences identifiées lors de l'analyse.
En synthèse, l'analyse permet d'identifier le problème et les besoins du projet, tandis que la conception décrit les étapes et les procédures nécessaires pour développer, produire et fournir les fonctionnalités du système à venir.
3.1	Analyse des besoins
3.1.1	Nature du projet
La détermination des caractéristiques d'un projet vise à mettre en lumière ses aspects distinctifs. Dans le cas de notre projet, nous examinons les points suivants :
❖	Nature du Projet : Il se révèle être de complexité modérée. Notre objectif est de développer une application destinée à être utilisée par les citoyens (ménages) et certains employés de l’UCG en plus d’une plateforme de suivi et d'administration, tout en étant administrée par cette dernière. Le scénario suggère une interaction entre ces deux acteurs, justifiant ainsi une complexité moyenne.
❖	Taille du Projet : La gestion des ordures au Sénégal constitue un projet de taille substantielle et complexe, dicté par la croissance urbaine rapide, l'insuffisance des infrastructures, les impacts environnementaux et de santé publique, ainsi que l'évolution constante des besoins. Les centres urbains, en particulier Dakar, sont confrontés à une urbanisation accélérée, entraînant une augmentation significative des déchets. Les défis incluent également des infrastructures inadéquates, des pratiques informelles nuisibles à l'environnement et à la santé, et des besoins en constante évolution. Pour relever ces défis, le projet requiert une approche exhaustive intégrant des technologies avancées, des systèmes de surveillance en temps réel et une collaboration avec les collectivités locales. La taille du projet reflète la nécessité d'une réponse proactive pour améliorer la qualité de vie, préserver l'environnement et favoriser la santé publique.
❖	Son importance et sa sensibilité :  elles résident dans son impact étendu sur l'environnement, la santé publique, la qualité de vie, le développement économique et la durabilité. La gestion efficace des déchets contribue à la préservation de l'écosystème, à la réduction des risques sanitaires, à l'amélioration de la qualité de vie des résidents, à la stimulation du développement économique et à la promotion de pratiques durables. Ce projet revêt ainsi une dimension globale, alliant des aspects environnementaux, sociaux et économiques pour favoriser un changement positif dans la vie des communautés locales.
3.1.2	La méthodologie utilisée
Un processus de développement définit un ensemble d’activités et leur enchaînement. Une activité comprend des tâches, des contraintes, des ressources et une démarche. L’utilisation d’un processus de développement vise à mener à terme le projet en respectant les délais, la qualité et le budget alloué. De ce fait, choisir une méthodologie pour conduire un projet permet à tous les acteurs de travailler efficacement ensemble, en suivant des règles clairement définies. 
3.1.2.1	Méthodes Agiles
Les méthodes agiles sont des groupes de pratiques de pilotage et de réalisation de projets. Elles ont pour origine le manifeste Agile, rédigé en 2001, qui consacre le terme « agile » pour référencer de multiples méthodes existantes. Ce manifeste agile est un texte rédigé par 17 experts du développement d'applications informatiques sous la forme de plusieurs méthodes dites agiles [27] Ces experts estimaient que le traditionnel cycle de développement en cascade ne correspondait plus aux contraintes et aux exigences des organisations en évolution rapide. Ce manifeste agile définit quatre valeurs :
❖	Les individus et leurs interactions avant le processus et les outils
❖	Des fonctionnalités opérationnelles avant la documentation 
❖	Collaboration avec le client plutôt que contractualisation des relations 
❖	Acceptation du changement plutôt que conformité aux plans.
Les méthodes agiles utilisent un principe de développement itératif qui consiste à découper le projet un certain nombre de cycles, ou itérations. Ces itérations sont en fait des mini-projets définis avec le client en détaillant les différentes fonctionnalités qui seront développées en fonction de leur priorité. Le chef de projet établit alors un macro planning correspondant aux tâches nécessaires pour le développement de ces fonctionnalités.
Cela évite bien entendu de vouloir tout prévoir en sachant qu’il pourrait avoir des imprévus ou des complications pendant les différentes phases du projet (Conception et Développement). 
3.1.2.2	Description de la méthode SCRUM
Les méthodes agiles sont des groupes de pratiques de pilotage et de réalisation de projets. Elles ont pour origine le manifeste Agile, rédigé en 2001, qui consacre le terme d’ «agile » pour référencer de multiples méthodes existantes ; parmi elles, SCRUM. A l’instar de toute méthode agile, SCRUM se veut plus pragmatique que les méthodes traditionnelles, implique au maximum le demandeur (client) et permet une grande réactivité à ses demandes. Elle repose sur un cycle de développement itératif, incrémental et adaptatif. En effet, SCRUM s’appuie sur le découpage d'un projet en itérations, nommées "sprints", ainsi que sur l'auto-organisation de l'équipe avec des rôles bien définis. Chaque sprint itération d’une semaine à un mois maximum commence par une estimation des tâches, appelées "stories", suivie d'une planification opérationnelle, et se termine par une démonstration de ce qui a été achevé.
 
Figure 17:Framework scrum
Il existe trois rôles principaux à « pourvoir » : le responsable produit, le scrum master, et les membres de l’équipe.
Le product owner ou responsable produit : Ce dernier définit les spécifications fonctionnelles et communique la vision globale du produit à l’équipe. Il établit la priorité des fonctionnalités à développer ou à corriger et valider les fonctionnalités développées. Il se doit de jouer le rôle client final, se mettre à sa place et donc de prioriser ses besoins. Celui qui tient ce rôle est celui qui a le plus de responsabilités et d’autorité. Le responsable (produit) est en effet celui qui est en première ligne lorsque quelque chose se passe mal ; ce qui nécessite de trouver le juste équilibre entre autorité – responsabilité et engagement.
Le scrum master : Ce dernier agit en tant que facilitateur entre le responsable produit et l’équipe. Son rôle principal est d’éliminer tous les obstacles qui peuvent empêcher l’équipe d’atteindre les objectifs fixés pour chaque sprint de travail. Il s’assure que les principes et les valeurs Scrum sont respectés. Il facilite la communication au sein de l’équipe et cherche à améliorer la productivité et le savoir-faire de son équipe. Le Scrum Master conseille aussi le responsable produit sur la façon de maximiser le Return On Investment général de l’équipe.
L’équipe de dev : Dans la méthode SCRUM, l’équipe est responsable de la réalisation opérationnelle des tâches. L’équipe est d’ailleurs généralement composée de 6 à 10 personnes mais pouvant aller jusqu'à 200 personnes. C’est toute l’équipe qui est responsable du résultat final de chaque sprint. La manière dont sont exécutées les tâches est très libre mais cette liberté doit être néanmoins cadrée par l’obligation de répondre aux objectifs du sprint.
3.1.3	Modélisation
En ingénierie, une méthode de modélisation ou d'analyse et de conception est un procédé permettant de construire un modèle aussi correct et efficace que possible. Il a pour objectif de permettre de formaliser les étapes préliminaires du développement d'un système afin de rendre ce développement plus fidèle aux besoins du client. Pour ce faire, on part d'un énoncé informel (le besoin tel qu'il est exprimé par le client, complété par des recherches d'informations auprès des experts du domaine fonctionnel, comme les futurs utilisateurs d'un logiciel), ainsi que de l'analyse de l'existant éventuel (c'est-à-dire la manière dont les processus à traiter par le système se déroulent actuellement chez le client). 
La phase d'analyse permet de lister les résultats attendus, en termes de fonctionnalités, de performance, de robustesse, de maintenance, de sécurité, d'extensibilité, etc.
La phase de conception permet de décrire le plus souvent en utilisant un langage de modélisation, le fonctionnement futur du système, afin d'en faciliter la réalisation.
3.1.3.1	UML
L'UML est une synthèse de langages de modélisation objet antérieurs : Booch, OMT, OOSE.
Principalement issu des travaux de Grady Booch, James Rumbaugh et Ivar Jacobson, UML est à présent un standard adopté par l’OMG. UML 1.0 a été normalisé en janvier 1997 ; UML 2.0 a été adopté par l'OMG en juillet 2005. La dernière version de la spécification validée par l'OMG est UML 2.5.1 (2017)
L'UML (Unified Modeling Language) est un langage de modélisation graphique utilisé en ingénierie logicielle pour représenter visuellement la structure, le comportement et les interactions des systèmes informatiques. Il se compose de plusieurs types de diagrammes, dont les plus courants sont les diagrammes de classes, de séquence, d'activité et de composants. L'UML facilite la communication, la documentation et la conception de systèmes logiciels complexes. Il est utilisé dans divers domaines de l'informatique et a évolué au fil des ans pour rester pertinent dans l'industrie.
 
Figure 18:UML logo
3.1.4	Besoins non fonctionnels
Un besoin non fonctionnel est une contrainte qui pèse sur un service du système telles que les contraintes liées à l’environnement et à l’implémentation (langage de programmation, SGBD, système d’exploitation, ...) et les exigences en matière de performances, les dépendances du projet, de facilité de maintenance, d’extensibilité et de fiabilité. Ses exigences techniques sont souvent exprimées sous forme d’objectifs spécifiques que doit atteindre le système.
❖	Sécurité : tous les accès des utilisateurs (étudiant, vendeur, portier, administrateur) doivent être protégés par un login et un mot de passe pour parvenir à la sécurité de la plateforme.
❖	Efficacité : l'application doit être fonctionnelle indépendamment de toutes circonstances pouvant entourer l'utilisateur.
❖	Validité : réaliser exactement les tâches définies dans la spécification
❖	Performance : temps de réponse court.
❖	Fiabilité : les données fournies par l'application doivent êtres fiables et la solution doit rendre des résultats corrects
❖	Stockage : l'accès à la base de données doit être souple et rapide
❖	Extensibilité : l'application doit être extensible, c'est-à-dire qu'il pourra y avoir une possibilité d'ajouter ou de modifier de nouvelles fonctionnalités.
3.1.5	Besoins fonctionnels
Il s'agit des fonctionnalités du système. Ce sont les besoins spécifiques d'un comportement d'entrée / sortie du système. L’ensemble des fonctionnalités doivent être mises en relation avec un ensemble de besoins utilisateurs. Ces derniers définissent les services que les utilisateurs attendent de voir fournir par cette application.
En d’autres termes, c’est la description des exigences fonctionnelles des acteurs de l’application.
3.1.5.1	Identification des acteurs
Un acteur désigne un rôle externe joué par une personne, un processus, un matériel, un logiciel ou une chose qui interagit avec le système. En d’autres termes, un acteur est l’idéalisation d’un rôle joué par une personne... qui interagit avec le système. Ils représentent les rôles joués dans le système.
Voici ci-après la description des acteurs et leurs rôles vis-à-vis du système : 
❖	Administrateur (Responsable de département)
 L'administrateur est l'acteur responsable de tous les privilèges d'accès. Il contrôle le système en modifiant, configurant, consultant, maintenant, vérifiant et prenant des décisions liées au système et au travail. L'administrateur autorise les utilisateurs à effectuer leurs tâches, ainsi qu'à traiter et mettre à jour la base de données pour effectuer plusieurs tâches, telles que la gestion des profils et le suivi de leur travail à travers les statistiques, entre autres.
❖	Agent de collecte 
C’est l’acteur qui assure la collecte et le transport des déchets. La tâche et le travail de cet acteur pour ce projet, sont l’intervention immédiate dans le cas de recevoir une alerte de collecte provenant de l‘administrateur ou des citoyens à travers une application mobile.
❖	Citoyens (ménage)
Ce sont les producteurs de déchets, la tâche et le travail de cet acteur pour ce projet, une intervention immédiate dans le cas de recevoir les mises à jour contenant des informations relatives à l'état de la poubelle et les itinéraires de collecte à travers une application Smartphone. 
3.1.5.2	Identification des fonctionnalités générales du système
Pour la bonne compréhension du système mise en place, nous allons décrire chaque fonctionnalité de la plateforme. Cette description des fonctionnalités va permettre d’élaborer au mieux les cas d’utilisation de l’application.
❖	Notifications et Alertes
Le système d'alerte et de notification permet l'envoi et la réception d’alertes en temps réel concernant le niveau de remplissage des poubelles, les horaires de collecte, les anomalies détectées et des notifications de sensibilisation.
❖	Surveillance en Temps Réel et Visualisation des Données
Cette fonctionnalité permet de surveiller en temps réel le niveau de remplissage des poubelles et de visualiser les données collectées sur une interface graphique
❖	Localisation
L'utilisateur final et/ou l’opérateur de collecte doivent pouvoir localiser des poubelles, des dépôts sauvages et des véhicules de collecte.
❖	S’authentifier
L’authentification permet à un utilisateur d'accéder à la plateforme avec un niveau d'accès dépendant de son profil. L'utilisateur doit d'abord avoir un compte valide et actif.
❖	Création de Compte
L'utilisateur final doit pouvoir créer un compte sur la plateforme pour accéder aux services de gestion des déchets.
3.1.6	Diagramme des cas d’utilisation
Un cas d’utilisation spécifie une fonction offerte par l’application à son environnement. Un cas d’utilisation est spécifié uniquement par un intitulé. L’intitulé du cas d’utilisation respecte le pattern « verbe + compléments ». Le verbe de l’intitulé permet de spécifier la nature de la fonctionnalité offerte par l’application, tandis que les compléments permettent de spécifier les données d’entrée ou de sortie de la fonctionnalité.
3.1.6.1	Diagramme de cas d’utilisation de l’administrateur
L’administrateur est responsable de la gestion des départements.
 
Figure 19:Diagramme de cas d’utilisation d’administration

❖	Fiche descriptive du cas d'utilisation de l’administrateur
Cas d’utilisation	Administration du système
Acteur	Administrateur
Objectifs	Permettre à l’administrateur du système de gérer et de configurer le système
Préconditions	L'administrateur doit être authentifié et avoir les droits d'administration du système
Post-condition	Les configurations effectuées par l'administrateur sont enregistrées et prennent effet immédiatement
Scénario nominale	L'administrateur se connecte à l'interface d'administration du système à l'aide de ses identifiants et mots de passe.
 L'administrateur accède à la page d'accueil de l'interface d'administration et voit les différentes options disponibles pour gérer le système.
 L'administrateur peut ajouter, modifier ou supprimer des poubelles, des camions de collecte de déchets, des drones ou tout autre système utilisé pour la collecte des déchets.
 L'administrateur peut configurer les seuils de température, d'humidité et de niveau de remplissage des poubelles pour déclencher les notifications de collecte. 
L'administrateur peut configurer les horaires de collecte de déchets et les routes de collecte pour optimiser l'efficacité de la collecte.
L'administrateur peut accéder aux données collectées par les capteurs et les analyser pour améliorer les opérations de collecte de déchets.
L'administrateur peut générer des rapports de performance pour surveiller l'efficacité du système de gestion des déchets. 
L'administrateur peut effectuer des mises à jour logicielles et des maintenances pour assurer le bon fonctionnement du système.
Tableau 1: Fiche descriptive du cas d'utilisation d’Administration du système
3.1.6.2	Diagramme de cas d’utilisation d’Agent de collecte et du citoyen

 
Figure 20:Diagramme de cas d’utilisation d’un utilisateur

❖	Fiche descriptive du cas d'utilisation du citoyen et de l'agent de collecte
Cas d’utilisation	Citoyens
Acteur	Citoyens, Agent
Objectifs	Permettre à l'utilisateur du système de recevoir des notifications de sensibilisation, envoyer des alertes, suivre l’état de ses poubelles , voir les données des poubelles situées dans sa zone, localiser une poubelle, voir les horaires de collecte dans la zone…
Préconditions	Le Mobilier urbain doit être authentifié et avoir les droits appropries
Post-condition	L’utilisateur a consulté les données collectées par les capteurs et à une vue précise de l'état des poubelles dans son interface.
Exceptions :
 • Si l'utilisateur ne parvient pas à se connecter à l'interface de consultation des données, un message d'erreur s'affiche et le Mobilier urbain doit vérifier ses identifiants et mots de passe.
 • Si le système rencontre des problèmes techniques, l'utilisateur peut contacter l'assistance technique pour résoudre les problèmes.
Scénario nominale	L'utilisateur se connecte à l'interface de consultation des données du système à l'aide de ses identifiants et mots de passe. 
L'utilisateur accède à la page d'accueil de l'interface de consultation des données et voit les différentes options disponibles pour consulter les données. 
L'utilisateur peut voir les poubelles enregistrées dans le système et dans sa zone et peut sélectionner une poubelle pour voir les données collectées par les capteurs.
L'utilisateur peut envoyer une alerte pour signer un dépôt sauvage et peux consulter les horaires de collecte …

Tableau 2:Fiche descriptive du cas d'utilisation d’un citoyens (ménage)
3.1.7	Diagramme de séquence
Un diagramme de séquence est un type de diagramme de modélisation UML utilisé pour représenter les interactions entre les objets ou les éléments d'un système dans le temps. Il décrit les séquences d'actions qui se produisent entre différents acteurs et composants du système.
3.1.7.1	Diagramme des séquences de l’authentification

 
Figure 21:diagramme des séquences de l'authentification
❖	Fiche descriptive du scénario de l’authentification
Cas authentification	Utilisateur
Acteur	Utilisateur
Objectifs	Permettre à l'utilisateur d’accéder aux différentes services des applications
Préconditions	Avoir une bonne connexion, Accéder au site par url
Post-condition	Accès à l’espace utilisateur
Scénario nominale	1. L’utilisateur demande de se connecter ; 
2. Le système demande les paramètres ;
 3. L’utilisateur fournit les paramètres ; 
4. Le système vérifie la validité des paramètres ; 
5. Le système autorise l’accès
Exception : 
●	Motif de l’exception : invalidité des paramètres ;
●	Traitement : le système demande la fourniture de nouveaux paramètres valides.
Tableau 3:Fiche descriptive du scénario de l’authentification
3.1.7.2	Diagramme de séquence d’ajouter une alerte
 
Figure 22:Diagramme de séquence d’ajouter une alerte
❖	Fiche descriptive du scénario d’ajouter une alerte
Cas authentification	Utilisateur
Acteur	Utilisateur
Objectifs	Permettre à l'utilisateur d’accéder aux différentes services des applications
Préconditions	Avoir une bonne connexion, Accéder au site par url
Post-condition	Accès à l’espace utilisateur
Scénario nominale	1. L’utilisateur demande le formulaire d’ajout d’alert; 
1.1. Le système renvoie le formulaire d'alerte 
 2. L'utilisateur entre les paramètres de l'alerte
2.1. Le système vérifie la validité des paramètres ; 
2.4. Le système traite la demande et prépare l'enregistrement de l'alerte.
3.  Le système renvoi les résultats de l’enregistrement d’ajout d’alert
3.1 Affichage : Confirmer à l'utilisateur que l'alerte a été ajoutée avec succès.
3.2 Exception : affiche les motifs de l’exception
Tableau 4:Fiche descriptive du scénario d’ajouter une alerte
3.1.7.3	Diagramme de séquence de localisation de poubelles
 
Figure 23:Diagramme de séquence de localisation de poubelles
❖	Fiche descriptive du scénario de localisation de poubelles
Cas authentification	Utilisateur
Acteur	Utilisateur
Objectifs	Permettre à l'utilisateur d’accéder aux différentes services des applications
Préconditions	Avoir une bonne connexion, Accéder au site par url
Post-condition	Accès à l’espace utilisateur
Scénario nominale	1. L’utilisateur demande à localiser des poubelles ; 
1.1. Le système afficher la carte avec poubelles ;
 2. L’utilisateur fournit les paramètres de recherche ; 
3.  Le système renvoi les résultats de la recherche ; 
3.1 Afficher poubelles dans la carte
3.2 Exception : affiche les motifs de l’exception
Tableau 5:Fiche descriptive du scénario de localisation de poubelles
3.1.8	Diagramme de classe
Un diagramme de classe est un type de diagramme UML qui décrit la structure d'un système en utilisant des classes, des attributs, des méthodes et des relations entre ces éléments. Les classes représentent des concepts ou des objets du système, et les attributs et les méthodes possèdent les propriétés et les comportements de ces classes.
 
Figure 24:diagramme des classes
3.2	Conception des besoins
3.2.1	Choix des outils et technologies utilisés
Pour réaliser un projet d’application web ou mobile, la combinaison ou l’utilisation de nombreuses technologies est possible ; que ça soit dans l’environnement de travail, la programmation, la production de documents, etc. Il existe plusieurs moyens permettant de réaliser chaque étape du projet, donc un choix de technologie est imposé. Alors, sous la base de critères sélectifs et l’adaptabilité par rapport au projet que certaines technologies sont privilégiées parmi tant d’autres sur ce projet.
 Les objets connectés utilisés
❖	Une carte arduino
Dans le cadre de la réalisation du prototype, nous avons opté pour l'utilisation d'une carte Arduino, plus précisément la carte Arduino UNO. Cette carte, largement reconnue pour son caractère open source et sa simplicité d'utilisation, s'est avérée être le choix idéal pour atteindre efficacement et à moindre coût les divers objectifs de notre projet [28].
Il est important de remarquer que la carte Arduino UNO est basée sur le microcontrôleur ATMega328. Arduino Uno est une carte largement répandue et utilisée parmi toutes celles de la famille Arduino.
 
Figure 25::  carte Arduino Uno
❖	Le module Wifi ESP8266
L'ESP8266 est un microcontrôleur et un module Wi-Fi à faible coût et à faible consommation d'énergie, largement utilisé dans le domaine de l'Internet des Objets (IdO) et de l'électronique embarquée. Il est produit par Espressif Systems, une société chinoise spécialisée dans les semi-conducteurs [29].
Ce module combine un microcontrôleur 32 bits avec une pile TCP/IP intégrée, ce qui lui permet de se connecter à un réseau Wi-Fi et de communiquer via Internet. L'ESP8266 est populaire en raison de sa polyvalence, de sa facilité d'utilisation et de son prix abordable.
Grâce à ses capacités de connectivité Wi-Fi, l'ESP8266 peut être utilisé dans une variété de projets, tels que la domotique, la surveillance à distance, les capteurs IdO, les objets connectés, et bien plus encore. Il est souvent programmé en utilisant le langage de programmation Arduino, ce qui le rend accessible même pour les débutants en électronique et en programmation.
 
Figure 26:Module ESP8266
❖	Capteur de distance à Ultrason (HC-SR04)
Le capteur à ultrasons HC-SR04 est un composant largement utilisé dans de nombreux projets électroniques et systèmes embarqués. Il fonctionne en émettant des signaux ultrasoniques et en mesurant le temps nécessaire pour que ces signaux rebondissent sur un objet et reviennent au capteur. Avec une portée de mesure allant de quelques centimètres à plusieurs mètres, il est facile à utiliser et compatible avec des plateformes telles que Arduino. Le HC-SR04 trouve des applications dans la détection d'obstacles pour les robots, la surveillance à distance, la domotique et bien d'autres domaines. En résumé, le HC-SR04 est un capteur abordable, précis et polyvalent pour la mesure de distance à ultrason [30].

 
Figure 27:Capteur ultrason HC-SR04
❖	Module GPS NEO-6M
Un module GPS (Global Positioning System) est intégré pour fournir des données de géolocalisation sur les poubelles et mobilier urbain. Cette fonctionnalité permet de visualiser la position exacte des poubelles sur une carte et de planifier les trajets de collecte en fonction de la distance et de la proximité des poubelles [31]. 
Le GPS fonctionne en tandem avec une carte pour déterminer la localisation précise d'un point donné. Son principe de fonctionnement repose sur la triangulation des signaux émis par plusieurs satellites en orbite autour de la Terre. Ces satellites, disposés de manière à ce que 4 à 8 d'entre eux soient toujours visibles depuis n'importe quel endroit sur la planète, émettent des signaux captés par le récepteur GPS. En mesurant le temps mis par ces signaux pour parcourir la distance entre le satellite et le récepteur, le GPS peut calculer avec précision la position du récepteur sur la surface terrestre. Cette méthode permet d'obtenir des données de géolocalisation extrêmement précises pour guider efficacement les opérations de collecte des déchets.
   
Figure 28:: module GPS NEO 6M
❖	Module LoRa
LoRa, abréviation de "Long Range", est un protocole de transmission sans fil qui offre une portée de transmission étendue tout en consommant très peu d'énergie. Contrairement au WiFi et au Bluetooth, dont la portée est limitée à quelques mètres, LoRa permet de transmettre des données sur une distance d'environ 10 km [32].
Dans notre projet, chaque poubelle est équipé d'un module émetteur-récepteur LoRa afin de transmettre des données sur de longues distances tout en utilisant une puissance minimale. Les modules LoRa sont compacts et disponibles à un prix abordable, ce qui les rend particulièrement attractifs pour notre application.
Ces modules sont particulièrement adaptés aux environnements où la connexion Internet est limitée ou indisponible. Dans notre mémoire, nous avons choisi d'utiliser le module LoRa RYLR998 de Reyax, qui peut être facilement contrôlé à l'aide de commandes AT. Cela nous permet d'intégrer facilement et efficacement la technologie LoRa dans notre système de gestion des déchets.
 
Figure 29:module LoRa RYLR998 de Reyax
❖	Le capteur DHT11
Le capteur DHT11 est conçu pour mesurer la température et l'humidité. Avec ses 4 broches, il est souvent monté sur une carte support à 3 broches pour une utilisation simplifiée. Il communique avec la carte via l'une de ses entrées numériques, tandis que les 2 autres broches sont dédiées à son alimentation (5V) et à la masse (GND) [33].
Dans le cadre des poubelles intelligentes, le capteur DHT11 peut être utilisé pour surveiller la température et l'humidité à l'intérieur des poubelles. Cette surveillance est cruciale pour améliorer la gestion des déchets en réduisant les odeurs et en prévenant la croissance bactérienne. En surveillant l'humidité, en détectant la température et en agissant en conséquence, ce capteur contribue à maintenir un environnement sain et hygiénique, ce qui est essentiel pour un système de gestion des déchets efficace.
 
Figure 30:capteur de température et d'humidité

❖	Le servomoteur
Les servomoteurs, souvent appelés simplement "servos" par les utilisateurs, sont des moteurs d'un type particulier appréciés pour leur capacité à faire tourner un mécanisme jusqu'à une position précise et à la maintenir jusqu'à réception d'une nouvelle instruction [34]. Ils sont largement utilisés dans le modélisme, tels que la direction des voitures télécommandées ou l'ouverture automatique des couvercles, mais également dans des domaines tels que la robotique et l'industrie, notamment pour réguler les flux de liquides dans des vannes. Leur forme se présente généralement sous celle d'un petit rectangle, avec deux rebords sur les côtés pour une fixation solide et un axe décentré permettant d'attacher des bras interchangeables pour assurer la liaison mécanique avec la pièce à déplacer. Bien qu'il existe des servomoteurs à rotation continue, la plupart des modèles sont conçus pour un mouvement limité à 180 degrés.
 
Figure 31:servomoteur 9g
3.2.1.1	Les outils de développements utilisés

DIAGRAMS.NET 

Diagrams.net (anciennement appelé draw.io) est un outil de dessin en ligne gratuit et open-source pour la création de diagrammes et de schémas. Il offre une interface utilisateur intuitive pour la création de différents types de diagrammes, tels que des diagrammes de flux, des organigrammes, des diagrammes de réseau, des diagrammes de processus, des diagrammes UML et bien d'autres.
Diagrams.net propose également une grande variété de formes et d'icônes pour la création de diagrammes professionnels et personnalisés, ainsi que des fonctionnalités avancées telles que la possibilité d'importer et d'exporter des fichiers, la collaboration en temps réel, la sauvegarde dans le cloud et l'intégration avec d'autres applications telles que Google Drive, Microsoft OneDrive et GitHub.
En résumé, diagrams.net est un outil de création de diagrammes simple à utiliser et flexible, qui convient à la fois aux utilisateurs débutants et avancés. Il est particulièrement utile pour les développeurs de logiciels, les chefs de projet, les concepteurs, les étudiants et toute personne qui doit créer des diagrammes pour visualiser des idées, des processus ou des données.

INTELLIJ

IntelliJ IDEA, développé par JetBrains, est un environnement de développement intégré (IDE) polyvalent prisé par les développeurs. Il offre une prise en charge étendue de langages tels que Java, Kotlin, Groovy, Scala, JavaScript, etc. L'IDE se distingue par des fonctionnalités avancées pour le développement Java, notamment la complétion intelligente, le refactoring automatique, la détection d'erreurs en temps réel, et une intégration transparente avec les frameworks Java. Sa polyvalence s'étend au développement web, avec des outils avancés pour JavaScript et une intégration harmonieuse avec Git pour la gestion de version. IntelliJ propose un éditeur de code puissant, des fonctionnalités de débogage avancées, une interface utilisateur conviviale et une flexibilité accrue grâce à une variété de plugins. En résumé, IntelliJ IDEA est reconnu pour sa robustesse, son support étendu des langages, ses outils de productivité et son interface utilisateur intuitive, faisant de lui un choix populaire parmi les développeurs professionnels.



ANDROID STUDIO

Android Studio, développé par Google, est un environnement de développement intégré (IDE) complet pour la création d'applications Android. Outre son éditeur de code, ses outils de débogage et son support pour plusieurs langages dont Java et Kotlin, Android Studio intègre un émulateur puissant. Cet émulateur permet aux développeurs de tester leurs applications sur une variété de périphériques virtuels, simulant différentes configurations matérielles et versions d'Android. Avec des fonctionnalités telles que l'accélération matérielle et la reproduction réaliste du comportement des appareils physiques, l'émulateur facilite le test et le débogage des applications de manière efficace. Il s'intègre harmonieusement avec les autres outils de l'IDE, offrant ainsi une solution complète pour le développement Android. En résumé, Android Studio, avec son émulateur intégré, simplifie le processus de développement en offrant des fonctionnalités avancées et une expérience de test réaliste sur divers appareils virtuels.
FRITZING


Fritzing est une initiative matérielle open source qui rend l’électronique accessible en tant que matériau créatif pour tous. Il propose un outil logiciel, un site web communautaire et des services dans l’esprit du Traitement et d’Arduino, favoriser un écosystème créatif qui permet aux utilisateurs de documenter leurs prototypes, de les partager avec d’autres, d’enseigner l’électronique dans une salle de classe, et de mettre en page et fabriquer des PCB professionnels





Arduino IDE

 Le logiciel open source Arduino Software facilite l’écriture de code et son téléchargement sur la carte. Ce logiciel peut être utilisé avec n’importe quelle carte Arduino. La nouvelle version majeure de l’IDE Arduino version 2.0.3 est plus rapide et encore plus puissante. En plus d’un éditeur plus moderne et d’une interface plus réactive, il propose l’auto-complétion, la navigation dans le code et même un débogueur en direct [54].
VSCODE

VSCode (Visual Studio Code) est un éditeur de code source gratuit et open-source développé par Microsoft. Il est disponible pour les systèmes d'exploitation Windows, macOS et Linux.
VSCode est connu pour être un éditeur de code source très performant, qui prend en charge de nombreux langages de programmation et frameworks, notamment JavaScript, TypeScript, Python, PHP, Java, C++, Go, Ruby, Rust, etc. Il propose également une grande variété de fonctionnalités utiles pour les développeurs, telles que la complétion automatique de code, la correction orthographique, la refactorisation de code, le débogage, la gestion de version, la coloration syntaxique, la sélection multiple, etc.
VSCode peut également être étendu grâce à des extensions, qui permettent d'ajouter des fonctionnalités supplémentaires pour des langages de programmation spécifiques ou des outils de développement tiers. Il est également souvent utilisé pour des projets de développement collaboratif grâce à ses nombreuses fonctionnalités d'intégration avec des outils de développement, tels que Git, GitHub, Azure, etc.
En résumé, VSCode est un éditeur de code source populaire et polyvalent qui convient à une grande variété de projets de développement, de la simple édition de fichiers à la gestion de projets complexes.


GITHUB

GitHub est une plateforme basée sur Git, offrant un système de contrôle de versions pour le suivi des changements dans le code source. Les projets sont organisés en dépôts qui regroupent fichiers, code source et documentation. GitHub facilite la collaboration en permettant des contributions simultanées, la gestion des changements via des Pull Requests et un suivi des problèmes. Les Actions GitHub automatisent des workflows personnalisés, comme la construction automatique. La plateforme inclut des fonctionnalités telles que des wikis, des pages pour la documentation, et des outils d'intégration avec CI/CD. GitHub joue un rôle clé dans la communauté du développement en permettant la découverte de projets, la contribution à des projets open source et la collaboration. Il offre une intégration avec divers outils de développement et favorise la création de communautés open source. En résumé, GitHub est une plateforme complète facilitant la gestion de projets, la collaboration, l'automatisation des workflows et la construction de communautés autour du développement logiciel.
FIGMA

Figma est un outil d'UI design en ligne gratuit pour créer, collaborer, construire des prototypes et livrer des produits. En d’autre terme, Figma est une application Web collaborative pour la conception d'interfaces, avec des fonctionnalités hors ligne supplémentaires activées par les applications de bureau pour MacOS et Windows. L'ensemble des fonctionnalités de Figma se concentre sur la conception de l'interface utilisateur et de l'expérience utilisateur, en mettant l'accent sur la collaboration en temps réel, en utilisant une variété d'éditeurs de graphiques vectoriels et d'outils de prototypage. L'application mobile Figma pour Android et iOS permet de visualiser et d'interagir avec les prototypes Figma en temps réel sur les appareils mobiles et tablettes.

TRELLO

Trello est une plateforme de gestion de projet en ligne qui utilise des tableaux Kanban pour organiser les tâches sous forme de cartes déplaçables entre différentes listes. Offrant une flexibilité considérable, Trello permet la personnalisation des tableaux pour répondre aux besoins spécifiques du projet. La collaboration en temps réel, les détails des cartes, les intégrations avec d'autres applications, et les applications mobiles contribuent à une expérience utilisateur complète. Trello est largement adopté grâce à sa simplicité, son accessibilité gratuite, et sa capacité à s'adapter à des équipes de toutes tailles, en faisant un outil populaire dans le domaine de la gestion de projets. En résumé, Trello offre une approche visuelle et intuitive pour la gestion de projet, favorisant la collaboration, la transparence et la personnalisation.
3.2.1.2	Les technologies utilisées
❖	Notion Frameworks 
Le choix de la technologie pour développer une application est une étape cruciale, car la performance, la durabilité, la sécurité et l'efficacité dans la réalisation de l'application en dépendent. Conscients de cela, nous avons opté pour l'utilisation de frameworks pour développer le projet.
Les frameworks sont des outils construits autour de différents langages de programmation tels que PHP, JavaScript, Java, Python, Ruby, etc. Ils offrent une gamme variée de fonctionnalités, et peuvent être classés en front-end et back-end. Un framework, littéralement un "cadre de travail", fournit une base sur laquelle les développeurs peuvent construire leur application en tirant parti des fonctionnalités préexistantes.
L'utilisation d'un framework lors du développement permet de gagner du temps, et est aujourd'hui considérée comme un standard dans la construction de projets web ou mobiles. Ils sont souvent comparés à une boîte à outils, offrant aux développeurs une série d'éléments prêts à être utilisés selon les besoins du projet.
En effet, un framework peut être analogiquement comparé à une usine de voitures. L'usine fournit tous les éléments nécessaires à la construction d'une voiture, tels que les robots, les postes de travail, les composants (comme le volant ou les roues), etc. Les développeurs peuvent alors choisir les éléments nécessaires à partir de cette "usine" pour construire leur application ou leur logiciel. Une fois le squelette de l'application ou du logiciel (le framework) créé, les autres développeurs n'ont plus besoin de réinventer la roue à chaque nouveau projet.
	Les Frameworks Mobile 
•	FLUTTER

Flutter, développé par Google, est un framework open source de développement d'applications multiplateformes utilisant le langage Dart. Il se distingue par l'utilisation de widgets réutilisables pour la création d'interfaces utilisateur riches et interactives. La fonctionnalité de "Hot Reload" permet aux développeurs de voir instantanément les changements apportés au code, simplifiant le processus de développement. Flutter offre une approche single codebase, multiplateforme, permettant de développer des applications pour iOS, Android, le web et le bureau. Grâce à son moteur graphique Skia, Flutter garantit des performances élevées et une expérience utilisateur fluide. L'écosystème de Flutter est enrichi par une grande variété de packages, facilitant l'intégration de fonctionnalités. Il permet une personnalisation avancée de l'interface utilisateur avec des designs flexibles et des animations fluides. Soutenu par une communauté active et adopté par de nombreuses entreprises, Flutter se positionne comme une technologie de choix pour le développement d'applications multiplateformes.
•	IONIC 


Ionic est une plateforme de développement d'applications hybrides qui repose initialement sur AngularJS et Apache Cordova. Elle permet aux développeurs de créer des applications en utilisant des technologies web telles que HTML, CSS et JavaScript. Grâce à Ionic, il est possible de générer des applications pour diverses plateformes telles que iOS, Android, Chrome, Windows Phone, et bien d'autres encore




•	REACT NATIVE

React Native est un framework créé par Facebook après le succès de leur bibliothèque JavaScript, ReactJs. Il permet aux développeurs de tirer parti de la puissance de JavaScript pour développer des applications natives pour Android et iOS en même temps, en créant une sorte de « pont » entre les composants natifs et le code JavaScript. En d’autres termes, grâce à un moteur JavaScript, le développeur contrôle une UI native avec un code JavaScript.

Framework	Flutter	Ionic	React native
Créateurs	Google	Drafty Co.	Facebook
Langage programmation	Dart	HTML5, CSS, and JavaScript	JavaScript, Swift, Objective C ou Java
Performance	Haute	Modéré	Proche du natif
Interface graphique	Utilise les Widgets	HTML, CSS	Utilise interface native
Cas d'utilisation	Toutes les applications	Les applications simples	Toutes les applications
Code Réutilisabilité	50-90%	98%	90%
Licence	Open source	Open source avec une partie payant	Open source
Plateformes prise en charge	Android, IOS, Google Fuchsia et Desktop et Web	Android, IOS, Web	Android, IOS, UWP
Marché & Communauté soutenue	Très fort	Fort	Très fort
Applications populaires	Hamilton, Alibaba et Google Ads	JustWatch, Sworkit et Nationwide	Facebook, Instagram, et Airbnb
Tableau 6: Comparaison des technologies mobile
	Les Frameworks Front-end 
•	ANGULAR

Angular est un framework de développement d'applications web open-source créé et maintenu par Google. Il permet de créer des applications web dynamiques et réactives en utilisant le langage TypeScript.
Angular utilise une architecture de composants pour organiser l'interface utilisateur de l'application. Les composants peuvent contenir du code HTML, CSS et TypeScript pour créer des vues interactives et dynamiques. Angular prend également en charge la liaison de données, la validation de formulaire, la manipulation d'événements, la gestion de la navigation, la communication avec les API REST et bien d'autres fonctionnalités.
Angular est souvent utilisé dans les grandes applications d'entreprise, car il offre une structure organisée et modulaire pour le développement. Il est également largement utilisé pour la création de Progressive Web Apps (PWA), qui sont des applications web qui offrent une expérience utilisateur similaire à celle d'une application native.
•	VueJs

 Vue.js est un framework JavaScript open-source apprécié pour sa simplicité, sa flexibilité et sa réactivité. Basé sur une architecture de composants, il facilite la création d'interfaces utilisateur interactives. Vue.js propose une gestion de l'état interne avec VueX et prend en charge le rendu côté serveur via des solutions tierces comme Nuxt.js. Avec une communauté active et une documentation complète, Vue.js est largement utilisé pour développer une gamme d'applications web, offrant une alternative populaire à des frameworks comme Angular et React.

•	ReactJS
  React.js est une bibliothèque JavaScript open-source développée par Facebook, utilisée pour créer des interfaces utilisateur dynamiques et réactives pour les applications web. Basé sur une architecture de composants et utilisant le Virtual DOM, React permet d'écrire du code JSX intuitif et favorise un flux de données unidirectionnel pour une gestion des données plus prévisible. Avec un écosystème riche et diversifié, React est largement utilisé pour développer des applications web de toutes tailles.
Framework	Avantages	Inconvénients
ANGULAR
	Opinionné : Angular fournit une structure et une organisation claires pour le développement d'applications, ce qui peut être bénéfique pour les grandes équipes.
TypeScript :Angular est construit avec TypeScript, offrant ainsi une vérification statique des types et une meilleure sécurité.
Performance : L'approche d'AOT (Ahead-of-Time) compilation améliore les performances de l'application.
Écosystème complet : Angular offre un écosystème complet comprenant des outils de développement, des bibliothèques et des solutions prêtes à l'emploi.
	Complexité : La courbe d'apprentissage peut être raide en raison de la complexité de l'architecture et des concepts avancés comme les observables.
Taille : Angular est généralement plus lourd que d'autres frameworks, ce qui peut entraîner un temps de chargement plus long.
Rapidité des mises à jour : Les mises à jour majeures d'Angular peuvent nécessiter des modifications importantes dans les applications existantes.

ReactJS
	Performances : La virtualisation du DOM et l'approche de mise à jour sélective permettent d'améliorer les performances de l'application.
Flexibilité : React peut être utilisé avec d'autres bibliothèques et frameworks, offrant ainsi une grande flexibilité pour développer des applications web.
Grande communauté : React bénéficie d'une large adoption et d'une communauté active, offrant ainsi un support, des ressources et des composants prêts à l'emploi.
Réutilisabilité des composants : L'architecture basée sur des composants permet une réutilisabilité accrue du code et une maintenance simplifiée.
JSX : JSX permet d'écrire du code HTML directement dans JavaScript, ce qui simplifie la création d'interfaces utilisateur.
	Courbe d'apprentissage : Pour les débutants, la syntaxe JSX et le concept de composants peuvent nécessiter une certaine période d'adaptation.
Gestion de l'état : Bien que flexible, la gestion de l'état peut devenir complexe dans les grandes applications sans une bonne organisation.
Mises à jour fréquentes : les mises à jour fréquentes de React peuvent nécessiter des mises à jour régulières dans les projets existants.

VueJS
	Simplicité : Vue.js est réputé pour sa simplicité et sa facilité d'apprentissage, ce qui en fait un excellent choix pour les débutants et les petites équipes.
Taille : Vue.js a une empreinte plus légère que d'autres frameworks, ce qui permet un temps de chargement plus rapide.
Flexibilité : Vue.js offre une approche progressive, ce qui signifie qu'il peut être intégré progressivement dans une application existante sans perturber le fonctionnement actuel.
Réactivité : Le système de réactivité de Vue.js permet de créer facilement des interfaces utilisateur dynamiques et réactives.
	Écosystème moins mature : Bien que croissante, la communauté et l'écosystème de Vue.js sont moins matures que ceux de React.js et Angular.
Moins d'outils :  Vue.js dispose de moins d'outils et de bibliothèques tierces par rapport à React et Angular, bien que cela puisse changer avec le temps.
Support d'entreprise : Malgré son adoption croissante, Vue.js peut être perçu comme ayant moins de soutien de la part des grandes entreprises par rapport à React et Angular.

Tableau 7:Comparaison des technologiques frontend
●	SPRING BOOT

Spring Boot est un Framework open-source pour le développement d'applications Java. Il est basé sur le Framework Spring, qui est un des Framework les plus populaires pour le développement d'applications Java.
Spring Boot est conçu pour simplifier le développement d'applications Java en fournissant une configuration automatique, des starter-packs et une structure de projet prédéfinie. Cela permet aux développeurs de se concentrer sur la logique métier de leur application plutôt que sur la configuration et la mise en place de l'environnement de développement.
L’auto-configuration (permet de se concentrer sur le code métier au lieu de passer un temps fou à configurer le Framework qu’il utilise). Conteneur « léger » (les classes n'ont pas besoin d'implémenter une quelconque interface pour être prises en charge par le Framework). Communauté Java est une communauté mature et florissante
Utilisation élevée de la mémoire Difficile à déboguer en raison du code passe-partout Les dépendances inutilisées peuvent entraîner le déploiement de fichiers binaires de grande taille
Spring Boot intègre également de nombreux outils et fonctionnalités pour faciliter le développement d'applications Java, tels que :
●	Une configuration automatique en fonction des dépendances ajoutées au projet ;
●	Un système de gestion des dépendances avancé ;
●	Des fonctionnalités pour la création de web services RESTful ;
●	Une intégration facile avec des bases de données relationnelles, des serveurs de messagerie et des systèmes de gestion de files d'attente ;
●	Des outils pour la création de tests unitaires et de tests d'intégration ;
●	Une interface de ligne de commande pour la création rapide de projets.
Spring Boot est largement utilisé pour le développement d'applications Java, en particulier pour les applications web, les services web RESTful, les applications de traitement de données, les applications mobiles et les applications d’entreprise.
•	DJANGO

 Django est un framework web open-source, écrit en Python, qui simplifie le développement d'applications web complexes et robustes. Avec son architecture MTV (Model-Template-View) et son ORM puissant, Django permet de séparer la logique métier, la présentation et la logique d'affichage de manière claire. Il offre également une interface d'administration automatique, des fonctionnalités de sécurité intégrées et une grande extensibilité grâce à son écosystème de packages tiers. La documentation complète de Django en fait un choix convivial pour les débutants tout en fournissant suffisamment de ressources pour les développeurs expérimentés. En résumé, Django est un framework populaire et efficace pour le développement d'applications web de toutes tailles.
•	SYMFONY

Symfony est un ensemble de composants PHP ainsi qu'un framework MVC libre écrit en PHP. Il fournit des fonctionnalités modulables et adaptables qui permettent de faciliter et d’accélérer le développement d'un site web.
L'agence web française SensioLabs est à l'origine du framework. À force de toujours recréer les mêmes fonctionnalités comme la gestion d’utilisateurs, la gestion d’ORM etc. elle a développé ce framework pour ses propres besoins. Le code a été par la suite partagé avec la communauté des développeurs PHP comme ces problématiques étaient souvent les mêmes pour d'autres développeurs. Ainsi le projet est alors devenu Symfony (conformément à la volonté du créateur de conserver les initiales S et F de Sensio Framework). Symfony fournit ainsi des fonctionnalités modulables et adaptables qui permettent de faciliter et d’accélérer le développement d'un site web. En effet, dans son "package" le plus courant,
Symfony propose entre autres :
·	Une séparation du code en trois couches, selon le modèle MVC, pour une plus grande maintenabilité et évolutivité ;
·	Des performances optimisées et un système de cache afin d'assurer des temps de réponse optimaux ;
·	Une gestion des URL parlante, permettant à une page d'avoir une URL distincte de sa position dans l'arborescence ;
·	Un système de configuration en cascade utilisant pleinement le langage YAML ;
·	Un générateur de back-office et un lanceur de module (scaffolding) ;
·	L'internationalisation native ;
·	 Le support d'AJAX ;
·	 Une architecture extensible permettant créations et utilisations de plugins.
Symfony fournit une interface en ligne de commande pour améliorer la productivité en créant un code de base modifiable à volonté.
En outre, Symfony suggère l’intégration de composants logiciels tels que Doctrine pour requêter la base de données, Twig pour le rendu de ses templates, SwiftMailer pour l'envoi d'e-mails, etc.
Framework	Avantages	Inconvénients
Spring Boot	L’auto-configuration (permet de se concentrer sur le code métier au lieu de passer un temps fou à configurer le framework qu’il utilise). Conteneur « léger » (les classes n'ont pas besoin d'implémenter une quelconque interface pour être prises en charge par le framework). Communauté Java est une communauté mature et florissante	Utilisation élevée de la mémoire Difficile à déboguer en raison du code passe-partout Les dépendances inutilisées peuvent entraîner le déploiement de fichiers binaires de grande taille
Django	Accélère le développement d’applications Web Scalable de manière à pouvoir gérer tout type de matériel L’approche des « Batteries-included » aide les développeurs à réduire considérablement le temps de développement.	Performance réduit pour les petites applications web Le processus n’a pas la capacité de gérer plusieurs requêtes simultanément Très peu de documentation officielle, pas de moteur pour injection de dépendance
Symfony	Une flexibilité inégalée grâce aux bundles et aux composants qu'il contient. Une documentation très détaillée, propre et correctement structurée pour aider les développeurs dans leurs efforts de développement. Communauté très forte	Une courbe d'apprentissage vraiment raide (plus difficile à apprendre que le langage sur lequel il fonctionne) Pas performant pour les applications qui nécessitent une capacité de chargement en temps réel plus rapide.
Tableau 8:Comparaison des technologiques backend

❖	Système de Gestion de Base de Données
Une base de données est un ensemble d'informations qui est organisé de manière à être facilement accessible, géré et mis à jour. Elle est utilisée par les organisations comme méthode de stockage, de gestion et de récupération de l’information. Les données sont organisées en lignes, colonnes et tableaux et sont indexées pour faciliter la recherche d'informations. Les données sont mises à jour, complétées ou encore supprimées au fur et à mesure que de nouvelles informations sont ajoutées. De nos jours, Il existe deux types de base de données dont SQL et NoSQL dont la comparaison suivante

Base de données	SQL		NoSQL
Définition	Le SQL, qui signifie Structured Query Language, est un langage informatique normalisé permettant de communiquer avec une base de données relationnelle.		NoSQL signifie à la fois « Not only SQL », car certains langages NoSQL comprennent le langage SQL en plus de leur propre capacité, et « Non relationnel » parce qu’il ne peut pas stocker facilement des données relationnelles.
Caractéristiques	Le SQL permet uniquement la scalabilité verticale		Le NoSQL autorise à la fois la scalabilité verticale et horizontale, car il est distribué
Cas d’usage	Les données sont structurées et que leurs relations sont fondamentales.		Lorsque l’on manipule de très larges volumes de données dont les relations entre celles-ci ne sont pas particulièrement importantes.
Tableau 9:Comparaison du SQL et du NoSQL
Pour assurer une gestion et un stockage efficaces des données dans une base de données, l'utilisation d'un système de gestion de base de données (SGBD) est indispensable. Ces systèmes se déclinent en plusieurs types, notamment les SGBD relationnels (SQL) et les SGBD NoSQL. Parmi les SGBD relationnels les plus connus, on trouve Oracle, MySQL, PostgreSQL et SQL Server. En revanche, pour les SGBD NoSQL, les solutions les plus populaires incluent MongoDB, Redis, Cassandra et HBase.
Le théorème de Brewer, également connu sous le nom de théorème CAP, formulé en 2000 par Eric A. Brewer, s'intéresse à trois propriétés fondamentales des bases de données :
●	Cohérence (Consistency en anglais) : garantit qu'à un instant donné, les données ont un seul état visible, quel que soit le nombre de copies.
●	Disponibilité (Availability en anglais) : assure que les données sont disponibles tant que le système est opérationnel.
●	Tolérance aux partitions (Partition tolerance en anglais) : garantit qu'une requête fournit un résultat correct, indépendamment du nombre de serveurs impliqués.
 Dans le cadre de notre système, nous avons choisi d'utiliser une base de données NoSQL pour stocker les données provenant des capteurs, en raison du volume important de données généré. Pour le système lui-même, nous avons opté pour une base de données SQL. Cette approche est en accord avec le couple CP selon le théorème de CAP.

PostgreSQL 
PostgreSQL est un système de gestion de base de données relationnelle orienté objet. Il combine les avantages des bases de données SQL et NoSQL, offrant ainsi une solution hybride. PostgreSQL est gratuit et open source, ce qui permet une utilisation et une modification libre par la communauté
	Caractéristiques Principales :
•	Compatibilité étendue : PostgreSQL fonctionne sur une large gamme de systèmes d'exploitation, y compris Linux, Windows, et macOS.
•	Communauté Active : La base de données bénéficie d'une communauté active de développeurs et d'utilisateurs, assurant un support continu, des mises à jour régulières, et une abondance de ressources en ligne.
•	Fournisseurs de Services Tiers : De nombreux fournisseurs de services tiers offrent des solutions de support, d'hébergement, et de gestion pour PostgreSQL, facilitant son adoption par les entreprises.
•	Haute Conformité ACID : PostgreSQL garantit la conformité aux propriétés ACID (Atomicité, Cohérence, Isolation, Durabilité), assurant des transactions fiables et sécurisées.
•	Utilisation du SQL Pur : PostgreSQL utilise SQL (Structured Query Language) pur pour les interactions avec la base de données, facilitant son apprentissage et son utilisation pour ceux qui ont déjà des connaissances en SQL.
	Avantages :
●	Performance et Évolutivité : PostgreSQL est conçu pour gérer des charges de travail allant des petites applications aux grandes applications complexes avec des millions de transactions par seconde.
●	Extensibilité : Grâce à son architecture modulaire, PostgreSQL permet l'ajout de nouvelles fonctionnalités via des modules et des extensions.
●	Sécurité : PostgreSQL offre des fonctionnalités de sécurité avancées telles que l'authentification basée sur les rôles, le chiffrement des données, et le contrôle d'accès détaillé.
	Résumé
En résumé, nous avons sélectionné Flutter pour l'application mobile et AngularJS pour le frontend de l'application web. Pour le backend, nous avons préféré Spring Boot pour sa solidité et ses fonctionnalités étendues. En tant que framework complet pour le développement d'applications web en Java, Spring Boot offre les outils nécessaires tels que l'authentification, la gestion des bases de données et la prise en charge des API REST, parfaitement adaptés à notre projet d'envergure.
Par ailleurs, nous avons opté pour une base de données NoSQL pour stocker les données des capteurs en raison du volume important de données généré, tandis que pour le système lui-même, nous avons choisi une base de données SQL. Cette approche est conforme au couple CP du théorème de CAP, assurant ainsi une gestion efficace des données dans notre système.
Conclusion
Le chapitre d'analyse et de conception de ce projet offre une vision détaillée de la méthodologie adoptée pour élaborer le système. À travers une étude minutieuse, les besoins fonctionnels ont été identifiés et schématisés à l'aide de diagrammes de cas d'utilisation.  
Cette analyse a été approfondie par des diagrammes de séquence, permettant de décomposer les processus et de clarifier les interactions entre les différents acteurs du système.
En utilisant une approche méthodique, ce chapitre a jeté les bases d'une conception claire et structurée du projet. En comprenant à la fois "ce qui doit être fait" et "comment le faire", nous sommes en mesure d'avancer vers une implémentation efficace et cohérente du système de gestion des déchets.
CHAPITRE 4 : 	Implémentation 
Introduction
L'implémentation se réfère à la réalisation concrète d'un plan, d'une méthode, d'un concept ou d'une idée dans le but de parvenir à un objectif spécifique. Dans ce chapitre, nous détaillerons la mise en œuvre de notre système en commençant par l'assemblage des composants matériels (éléments électroniques), puis en configurant notre serveur cloud, et enfin en procédant à l'implémentation des applications qui simuleront le fonctionnement du système. Ce processus nous permettra de concrétiser les aspects théoriques et conceptuels de notre projet, en le transformant en un système fonctionnel et opérationnel.
4.1	Architectures du système
L'architecture du système de gestion des déchets est conçue pour intégrer divers composants technologiques afin de surveiller, collecter et traiter les déchets de manière efficace.
4.1.1	Architecture matérielle du système

 
Figure 32:Architecture matérielle du système

4.1.2	Architecture logicielle du système

 
Figure 33:Architecture logicielle du système

4.1.3	Description de l’architecture du système
L'architecture présentée se compose de plusieurs couches, chacune ayant des rôles et des responsabilités spécifiques pour la gestion et la communication des données des capteurs. Voici une description détaillée de chaque couche et des composants inclus :
La couche physique 
La couche physique est constituée de capteurs IdO, de modules GPS ainsi que des microcontrôleurs.
●	Capteurs : Incluant divers types de capteurs pour la collecte de données, tels que des capteurs de température, d'humidité, de niveau de remplissage, etc.
●	Microcontrôleur : Unité de traitement embarquée qui contrôle les capteurs et effectue des opérations de base avant la transmission des données.
●	GPS : Module de localisation qui fournit les coordonnées géographiques des capteurs.
La couche de communication 
Le réseau de communication est un aspect essentiel de notre système IdO. Nous utilisons un réseau de communication hybride combinant LoRa et Wi-Fi. LoRa est particulièrement adapté pour la transmission de données sur de longues distances avec une faible consommation d'énergie, ce qui est idéal pour les capteurs de poubelles qui peuvent être dispersés dans des zones urbaines étendues. 
●	Protocole : Utilise plusieurs protocoles de communication pour la transmission des données et le contrôle, notamment :
○	TCP (Transmission Control Protocol) : Protocole de communication fiable pour la transmission de données.
○	UDP (User Datagram Protocol) : Protocole de communication moins fiable mais plus rapide, utile pour des applications où la vitesse est plus critique que la fiabilité.
○	MQTT (Message Queuing Telemetry Transport) : Protocole léger de messagerie basé sur le modèle publish/subscribe, adapté aux environnements à faible bande passante.
●	Technologies de Communication : Utilisation de diverses technologies pour la transmission sans fil :
○	WiFi : Technologie de réseau local sans fil pour des connexions à courte portée.
○	LoRa : Technologie de réseau étendu à faible puissance (LPWAN) pour des communications à longue portée avec une faible consommation d'énergie.
○	5G : Technologie de réseau mobile de cinquième génération offrant des vitesses de transmission élevées et une faible latence.

●	Passerelle IdO
La passerelle IdO joue un rôle crucial en tant qu'agrégateur de données. Elle collecte les données des capteurs de poubelles via les réseaux LoRa et Wi-Fi, puis les relaie vers le serveur cloud. La passerelle sert de pont entre les dispositifs IdO sur le terrain et les infrastructures de traitement des données, assurant une transmission fluide et sécurisée des informations collectées.
La couche applicative 
Elle est constituée de l'ensemble des applications d’analyse, de traitement et de présentations des données issu de capteurs et sont composés :
●	Acquisition des Données : Processus de collecte des données brutes des capteurs via les microcontrôleurs.
●	Analyse des Données : Traitement et analyse des données collectées pour extraire des informations utiles et détecter des anomalies.
●	Présentation des Données : Visualisation des données analysées sous forme de tableaux de bord, graphiques, rapports, etc., pour une interprétation facile par les utilisateurs finaux.
Flux de Données et Contrôle
●	Flux de Données : Les données sont collectées par les capteurs, traitées par les microcontrôleurs, puis transmises via les technologies de communication et les protocoles spécifiés vers la couche applicative où elles sont analysées et présentées.
●	Flux de Contrôle : Des commandes et des configurations peuvent être envoyées de la couche applicative vers les capteurs pour ajuster les paramètres de collecte de données ou effectuer des actions spécifiques.
Fonctionnement Global
Les capteurs mesurent différents paramètres environnementaux et envoient les données au microcontrôleur. Le microcontrôleur utilise des protocoles comme TCP, UDP ou MQTT pour transmettre les données via des technologies de communication comme WiFi, LoRa ou 5G. L'acquisition et l'analyse des données reçues par la couche applicative permettent de détecter des tendances ou des anomalies. Les résultats de l'analyse sont présentés sous une forme compréhensible pour les utilisateurs, facilitant la prise de décision. Cette architecture 


4.1.4	Architecture de communication du système
Cette architecture montre comment nos différents services et composants communiquent entre eux
 
Figure 34:Architecture de communication du système
4.2	Exemple de montage des composants IdO
Une fois les composants sélectionnés, les actionneurs électriques et le logiciel de programmation Arduino, cette étape implique la conception et la construction d'une structure ou d'un châssis pour les maintenir ensemble. Le microcontrôleur ESP8266, considéré comme le "cerveau" et l'Arduino Méga servent de point central où tous les autres composants se connectent. Ces cartes sont responsables du traitement des informations et de la coordination des différents composants.
Nous allons détailler comment chaque composant est connecté dans les étapes suivantes de la conception. Ce processus garantit une intégration cohérente et efficace de tous les éléments nécessaires au bon fonctionnement du système.
4.2.1	Système d’envoi/réception du niveau de déchet
La carte Esp8266 reçoit les informations du capteur ultrason 01 puis les envoie à la plateforme IdO et aux applications Web/Mobile.
❖	Connexion du capteur ultrason avec la carte Esp8266
Nous avons connecté les ports Capteur ultrasons 01 HC-SR04 avec les ports de l’esp comme suit :


 
Figure 35:Connexion du capteur ultrason avec la carte Esp8266
❖	Le code source :
 
Figure 36:mesure du niveau de remplissage

4.2.2	Mesure de la température et de l’humidité

Le capteur DHT11 est utilisé dans une poubelle intelligente pour surveiller la température et l'humidité à l'intérieur des poubelles.
 
Figure 37:montage du dht11 avec l'ESP8266

4.2.3	Système d'ouverture /fermeture

Le capteur à ultrasons détecte le mouvement, donc la porte s'ouvre après 3 secondes, et la porte reste ouverte pendant un temps donné avant de se fermer.
❖	Connexion du deuxième capteur à ultrasons avec la carte Esp8266
 
Figure 38:montage du HCSR04 sur la carte ESP8266


❖	Connexion du servomoteur avec la carte ESP8266

 
Figure 39:connexion du servomoteur avec la carte ESP8266

❖	Le code source :
 Nous utilisons le code source pour contrôler le servomoteur avec le capteur ultrason 02 HC SR04.
 
Figure 40:code pour le contrôle du servomoteur

4.2.4	Système de géolocalisation des poubelles

❖	Connexion du module GPS sur le module Esp8266


 
Figure 41:connexion du module GPS sur le module Esp8266


4.3	Implémentation des applications Web/Mobile & API

4.3.1	Les maquettes (figma)

 
Figure 42:maquette interface mobile
  
Figure 43:maquette interface mobile localisation

 
Figure 44:maquette interface web connexion

4.3.2	L’API (SpringBoot)
❖	L’architecture de l’API

 
Figure 45:architecture  de  l'API

❖	Aperçu du fichier de configuration de l’API
 
Figure 46:Fichier de configuration de l’API

❖	Aperçu du contrôleur de l’utilisateur
 
Figure 47:contrôleur de l’utilisateur
❖	Documentation des endpoints avec swagger-ui 
 
 
Figure 48:documentation des endpoints avec swagger-ui
❖	Liste des utilisateurs dans la base de données
 
Figure 49:liste des utilisateurs dans la BD

4.3.3	L’Application web (angular)
❖	L’architecture de l’application web

 
Figure 50:architecture de l’application web


 
Figure 51:code
 
Figure 52:affichage de la liste utilisateurs
 
Figure 53:formulaire de création de collaborateur

4.3.4	L’Application mobile (flutter)
 
 
Figure 54:architecture de application mobile


 
Figure 55:Interface de  bienvenu





 
Figure 56:interface de connexion

 
Figure 57:interface de création de compte utilisateur 
Conclusion
Dans ce chapitre, nous avons exploré les grandes lignes de l'implémentation de notre système de gestion intelligente des déchets. Nous avons présenté les outils utilisés ainsi qu'une partie du code développé pour concrétiser notre solution. En détaillant l'implémentation du prototype matériel et des applications associées, ce chapitre offre un aperçu complet de la mise en œuvre pratique de notre projet.
En conclusion, l'implémentation de notre système de gestion intelligente des déchets revêt une importance cruciale pour améliorer la gestion des déchets au sein des communautés. Elle garantit une collecte efficace et opportune des poubelles, tout en offrant aux utilisateurs une application conviviale pour planifier leurs itinéraires de collecte des déchets. Conclusion Générale
Le mémoire présenté a détaillé le développement d'un système novateur de gestion intelligente des déchets au Sénégal, fondé sur un réseau de communication hybride. Ce système vise à améliorer la gestion des déchets solides et l'hygiène publique dans le pays. 
L'architecture du réseau hybride proposée offre une approche complète pour la gestion efficace des poubelles dans les espaces publics et résidentiels, en intégrant des composants tels que les nœuds d'extrémité, la transmission de données à longue portée via LoRa, la connectivité Wi-Fi, le stockage de données et la visualisation du niveau de remplissage des poubelles.
Ce système offre des fonctionnalités avancées telles que l'ouverture automatique des poubelles en présence d'utilisateurs, la surveillance en temps réel du niveau de remplissage et la transmission des données au serveur cloud. Son implémentation vise à résoudre les problèmes courants tels que les débordements et les odeurs désagréables associées aux poubelles à ordures.
Bien que le système présenté soit un premier pas vers une meilleure gestion des déchets, il est susceptible d'évoluer et de s'améliorer. Des suggestions d'amélioration comprennent l'utilisation de capteurs plus avancés pour la détection du niveau de remplissage, l'ajout de mécanismes de compactage des déchets pour prolonger les intervalles de collecte, et l'intégration de modules supplémentaires tels que le GPRS pour les zones sans Internet et des capteurs olfactifs pour détecter les odeurs indésirables.
En outre, l'intégration de l'intelligence artificielle pour garantir la présence humaine lors de l'ouverture des poubelles constitue une piste prometteuse pour améliorer encore davantage l'efficacité et la convivialité du système. En combinant les progrès technologiques avec une perspective humanitaire, ce système peut contribuer significativement à l'amélioration de l'environnement et à la valorisation du travail des agents de propreté dans la société sénégalaise.
 Bibliographie
[01] https://www.ucg.gouv.sn/docsucg/projets.php  
[02] https://www.ucg.gouv.sn/docsucg/presentation_ucg 
[03] https://www.insee.fr/fr/metadonnees/definition/c1644 
[04] https://www.ucg.gouv.sn/docsucg/projets.php 
[04]http://www.droit-afrique.com/upload/doc/senegal/Senegal-Code-2001environnement.pdf 
[05]https://www.spivds.org/medias/publications/les_dechets_definition_gestion_collecte_traitement_responsabilites_police_speciale.pdf 
[06]https://www.researchgate.net/publication/312422329_Cloud-based_smart_waste_management_for_smart_cities 
[07]  https://www.seneweb.com/news/Economie/contrat-veolia-mairie-de-dakar-assainissement-signature-d-un-contrat-l-onin-v-olia-fait-son-beurre-dans-les-poubelles-dakaroises_n_6749.html 
[08]  Daniel Mikelsten, «Automatisation et technologies émergentes», Cambridge Stanford 
Books, vol. 150p, 2020.  
[09]  Mansaf Alam, Kashish Ara Shakil et Samiya Khan, «Internet of Things (IoT) », 
Springer Nature, p.6, 2020. https://doi.org/10.1007/978-3-030-37468-6 
[10]  Autorité de régulation des communications électroniques et des postes(arcep), «PROJET 
DE LIVRE BLANC – PREPARER LA REVOLUTION DE L’INTERNET DES OBJETS», p.8, 2016.  
[11]  P. Dayaker, «Internet des objets: Une approche pratique»,Editions Notre Savoir, p.124, 
2022.  
[12]  Serigne Modou Kara Mbengue a. al, «“Internet of Medical Things : Remote» e-Health 
Symposium of the 16th IEEE International Conference on Wireless Communications 
and Mobile Computing., 2020.  
[13  Architecture d'un réseau IoT: https://www.objetconnecte.com/architecture-reseau-iot 
[14]https://web.maths.unsw.edu.au/~lafaye/CCM/bluetooth/bluetoothintro.html 
[15] 11F. Kuan, « Technologie de communication sans fil à courte portée vs technologie de communication sans fil à longue portée », MOKOSmart #1 Solution d’appareil intelligent en Chine, 8 septembre 2022. https://www.mokosmart.com/short-range-wireless-communication-technology-vs-long-range-wireless-communication-technology/ 
[16] https://www.objetconnecte.com/tout-savoir-reseau-lora-bouygues/ 
[17] Toshiba, «News Releases : Toshiba to Expand Home Solutions Business in Europe», 5 
Septembre 2013. Available: 
[18] Jenny McGrath, «Myfox Home Alarm and Security Camera Review», 7 Octobre 2015. 
Available: https://www.digitaltrends.com/home/myfox-smart-home-security-system-review  
[19]  HEALTHQ, «Sexual Health Clinic in Lawrence, Beverly, and Haverhill, MA». 
Available: https://healthq.org 
[20]  LesLeudis, «10 applications de l’Internet des Objets qui révolutionnent la société», 3 
Juillet 2018. Available: https://blog.lesjeudis.com/10-applications-de-l-internet-des-objets-qui-revolutionnent-la-societe 
[21]https://www.researchgate.net/publication/312422329_Cloud-based_smart_waste_management_for_smart_cities 
[22]https://caburntelecom.com/iot-in-africa/ 
[23] https://wastehero.io/industries/municipal-waste-management/ 
[24] https://bigbelly.com/products 
[25] https://bine.world/ 
[26] https://www.ecubelabs.com/fr/ 
[27] https://www.reussirsesprojets.com/manifeste-agile-actualite/ 
[28]https://arduino-france.site/description-arduino-uno/ 
[29]https://www.hwlibre.com/fr/espXNUMX 
[30]https://www.raspberryme.com/utilisation-dun-capteur-de-distance-raspberry-pi-capteur-a-ultrasons-hc-sr04/ 
[31] Guide to NEO-6M GPS Module Arduino | Random Nerd Tutorials
[32]https://www.technolabcreation.com/reyax-rylr998-lora-module-with-arduino/ 
[33] https://tutoduino.fr/debuter/capteur-temperature 
[34]https://www.a-m-c.com/fr/servomoteur/ 

