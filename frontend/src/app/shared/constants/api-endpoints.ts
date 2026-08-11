/**
 * Registre des chemins réellement exposés par le backend SONAGED.
 *
 * <p><b>Pourquoi ce fichier existe.</b> Onze composants de ce front affectaient un chemin au
 * <i>singulier</i> (`/commune`, `/department`, `/quartier`, `/depotoir`, `/user`, `/authority`,
 * `/typedepotoir`, `/alert`) alors que le backend n'expose que le pluriel — plus un
 * `/regions/all` qui n'a jamais existé. Créer une commune, un quartier ou un dépotoir, ou
 * modifier un utilisateur, était donc impossible. Centraliser les chemins ici évite que la
 * faute se reproduise composant par composant.
 *
 * <p><b>Vérifié contre les 36 `@RestController` du backend</b> (2026-08-06) : les 147 endpoints
 * réels sont tous représentés ici, soit comme `ResourceEndpoint` (CRUD), soit comme `API_PATHS`
 * (le reste). La plupart n'ont encore aucune interface — cf. `docs/FRONTEND_AUDIT.md` §7 pour ce
 * qui reste à construire ; ce fichier garantit seulement que les chemins seront corrects le jour
 * où l'écran existera.
 *
 * <p><b>Le piège des sous-chemins `/s`.</b> Cinq ressources réservent le chemin simple à une
 * réponse `Page` — avec les paramètres `page` et `size` <b>obligatoires</b>, sans valeur par
 * défaut : les appeler sans paramètres donne un <b>400</b>, pas un 404. Ces cinq-là servent la
 * liste complète sur un sous-chemin `/s` : `/communes/s`, `/quartiers/s`, `/depotoirs/s`,
 * `/users/s`, `/alerts/s`.
 *
 * <p>Le backend déclare `@GetMapping("s")` sous `@RequestMapping("/v1/communes")`, ce qui se lit
 * comme une concaténation mais n'en est pas une : `PathPattern.combine` insère un séparateur.
 * `/v1/communess` n'a jamais existé et ne peut pas exister — un chemin frère est hors de portée
 * du préfixe de classe.
 *
 * <p>Tous les chemins sont relatifs à `environment.apiUrl` (qui inclut déjà `/v1`).
 * L'authentification (`/auth/**`) et l'import de données (`/data/**`) ont leurs propres bases,
 * `environment.authUrl` et `environment.dataUrl`.
 */

export interface ResourceEndpoint {
  /** Chemin CRUD de base : POST, et `${basePath}/${id}` pour GET par id, PUT, DELETE. */
  basePath: string;
  /**
   * Chemin renvoyant la liste complète sous forme de tableau JSON.
   * Diffère de `basePath` quand le backend réserve le chemin simple à une réponse paginée.
   */
  listPath: string;
  /** Nom du champ identifiant dans le DTO. */
  idField: string;
  /**
   * Type réel de l'identifiant côté backend, vérifié `@PathVariable` par `@PathVariable`.
   * ADR-0012 était **partielle** jusqu'au commit `4c25e8c` (2026-08-07, "finish ADR-0012 UUID
   * migration") : `depotoirs`, `typedepotoirs`, `alerts`, `circuits`, `circuit-collects`,
   * `circuit-balayages` et `moblier-urbains` restaient en `Long` auto-incrémenté jusque-là.
   * Depuis ce commit, **toutes** les ressources de ce registre sont en UUID v7. `Avis.id` (hors
   * registre : `/avis` est monté hors `/v1`, cf. `API_PATHS.avis*`) est passé en UUID v7 à son
   * tour le 2026-08-08 (changelog `2.24.0`) — plus aucune ressource `int`/`Long` dans l'API. Le
   * champ `idType` n'a donc plus qu'une valeur possible ici, conservé pour tracer l'historique et
   * détecter si une ressource future revient en arrière.
   */
  idType: 'uuid' | 'long';
  /**
   * Clé attendue par `/v1/deletions/{resource}` (corbeille 30 j) — **absente** quand la
   * ressource n'a pas de suppression logique (son repository n'étend pas `SoftDeleteRepository`
   * côté backend, ex. `vehicles`, `collection-schedules`, `alert-thresholds` : leur DELETE est
   * une désactivation permanente, pas un passage en corbeille).
   *
   * Quand elle existe, le backend la dérive du nom du bean repository (`communeRepository` →
   * `commune`), elle ne coïncide donc PAS avec le segment de chemin (`communes`,
   * `circuit-collects`…). Sans elle, la corbeille répond 404.
   */
  deletionResource?: string;
  /** Chemin de suppression non standard (sinon `${basePath}/${id}`). */
  deletePathTemplate?: string;
  /**
   * Chemin de lecture par id non standard (sinon `${basePath}/${id}`).
   * Seul `circuit-balayages` en a besoin : son `GET` par id vit sur un sous-chemin frère
   * (`/circuit-balayages/circuit-balayage/{id}`), pas sur `${basePath}/{id}` comme son PUT/DELETE.
   */
  getByIdPathTemplate?: (id: string) => string;
  /** Limite connue de l'API, à afficher plutôt qu'à découvrir en production. */
  notes?: string;
}

export const API_ENDPOINTS = {
  regions: {
    // `/regions` (chemin nu) exige desormais page&size (P1-2, meme patron que Commune/Quartier/
    // Department) ; `/regions/s` reste la liste complete pour peupler un select. Sans page/size,
    // le chemin nu renvoie une erreur — trouve en verifiant le formulaire de creation departement.
    basePath: '/regions', listPath: '/regions/s', idField: 'regionId', idType: 'uuid',
    deletionResource: 'region',
    notes: "Le backend n'expose ni PUT ni DELETE sur /v1/regions."
  },
  departments: {
    // `/departments` (chemin nu) exige desormais page&size lui aussi ; `/departments/s` est la
    // liste complete. Meme piege que regions ci-dessus, meme correctif.
    basePath: '/departments', listPath: '/departments/s', idField: 'departmentId', idType: 'uuid',
    deletionResource: 'department'
  },
  communes: {
    basePath: '/communes', listPath: '/communes/s', idField: 'communeId', idType: 'uuid',
    deletionResource: 'commune'
  },
  quartiers: {
    basePath: '/quartiers', listPath: '/quartiers/s', idField: 'quartierId', idType: 'uuid',
    deletionResource: 'quartier'
  },
  depotoirs: {
    basePath: '/depotoirs', listPath: '/depotoirs/s', idField: 'depotoirId', idType: 'uuid',
    deletionResource: 'depotoir',
    notes: 'Seule ressource à exposer aussi /depotoirs/deletions et /depotoirs/{id}/restore.'
  },
  typedepotoirs: {
    // `/s` pour la liste complète : le chemin nu est paginé et répond 500 sans page&size
    // (vérifié). C'est cette liste qui alimente le menu « type » du formulaire Point de collecte.
    basePath: '/typedepotoirs', listPath: '/typedepotoirs/s', idField: 'typeDepotoirId', idType: 'uuid',
    deletionResource: 'typedepotoir'
  },
  users: {
    basePath: '/users', listPath: '/users/s', idField: 'userId', idType: 'uuid',
    deletionResource: 'user',
    notes: "Le DTO expose `userId`, pas `id`."
  },
  authorities: {
    basePath: '/authorities', listPath: '/authorities', idField: 'authorityId', idType: 'uuid',
    deletionResource: 'authority',
    notes: "Tout le contrôleur exige l'autorité MANAGE_ROLE."
  },
  alerts: {
    basePath: '/alerts', listPath: '/alerts/s', idField: 'alertId', idType: 'uuid',
    deletionResource: 'alert',
    notes: 'Création en POST sur /alerts ET sur /alerts/s ; multipart accepté. Flux SSE sur /alerts/stream.'
  },
  circuits: {
    basePath: '/circuits', listPath: '/circuits', idField: 'circuitId', idType: 'uuid',
    deletionResource: 'circuit'
  },
  'circuit-collects': {
    // `/s` pour la liste complète : le chemin nu est paginé et exige page&size (même patron que
    // communes/quartiers/departments/regions).
    basePath: '/circuit-collects', listPath: '/circuit-collects/s', idField: 'circuitcollectId', idType: 'uuid',
    deletionResource: 'circuitcollect'
  },
  'circuit-balayages': {
    basePath: '/circuit-balayages', listPath: '/circuit-balayages/s', idField: 'circuitbalayageId', idType: 'uuid',
    deletionResource: 'circuitbalayage',
    getByIdPathTemplate: (id: string) => `/circuit-balayages/circuit-balayage/${id}`,
    notes: "GET par id sur /circuit-balayages/circuit-balayage/{id} (pas ${basePath}/{id})."
  },
  'moblier-urbains': {
    // `/s` pour la liste complète : le chemin nu est paginé et répond 500 sans page&size
    // (« Required request parameter 'page' ... is not present ») — l'écran Mobiliers urbains
    // était donc en erreur au chargement.
    basePath: '/moblier-urbains', listPath: '/moblier-urbains/s', idField: 'moblierUrbainId', idType: 'uuid',
    deletionResource: 'moblierurbain'
  },
  vehicles: {
    basePath: '/vehicles', listPath: '/vehicles', idField: 'vehicleId', idType: 'uuid',
    notes: "Reserve a ADMIN/SUPER_ADMIN (@PreAuthorize sur tout le controleur). DELETE = deactivate() (retrait de circulation, pas une suppression) ; pas de corbeille."
  },
  'collection-schedules': {
    basePath: '/collection-schedules', listPath: '/collection-schedules', idField: 'scheduleId', idType: 'uuid',
    notes: "GET accepte un parametre optionnel ?quartierId=<uuid> pour filtrer. DELETE = deactivate() ; pas de corbeille."
  },
  'alert-thresholds': {
    basePath: '/alert-thresholds', listPath: '/alert-thresholds', idField: 'thresholdId', idType: 'uuid',
    notes: "Reserve a ADMIN/SUPER_ADMIN. DELETE = deactivate() (retour au seuil par defaut) ; pas de corbeille."
  },
  coordinates: {
    basePath: '/coordinates', listPath: '/coordinates', idField: 'coordinateId', idType: 'uuid',
    deletionResource: 'coordinate',
    deletePathTemplate: '/coordinates/delete/coordinate/{id}',
    notes: 'Chemin de suppression non standard côté backend.'
  },
  geometries: {
    basePath: '/geometries', listPath: '/geometries', idField: 'geometryId', idType: 'uuid',
    deletionResource: 'geometry'
  }
} as const satisfies Record<string, ResourceEndpoint>;

export type ResourceKey = keyof typeof API_ENDPOINTS;

/**
 * Chemins hors du registre CRUD, qui n'ont pas d'équivalent « ressource ».
 * Relatifs à `environment.apiUrl` sauf mention contraire.
 */
export const API_PATHS = {
  /** Cartographie de supervision. */
  mapsDepartments: '/maps/departments',
  mapsDepotoirs: '/maps/depotoirs',
  mapsVehicles: '/maps/vehicles',

  /** Supervision et rapports. */
  supervisionStats: '/supervision/stats',
  supervisionReports: '/supervision/reports',
  supervisionReportsCsv: '/supervision/reports/csv',
  // `depotoirId` est un UUID v7 depuis le commit `4c25e8c` (2026-08-07, "finish ADR-0012 UUID
  // migration") — `Depotoir` était un `Long` avant cela.
  pointJournal: (depotoirId: string) => `/supervision/points/${depotoirId}/journal`,

  /**
   * Tournée de l'agent de collecte.
   * `collectionRoutes` exige la permission `VIEW_COLLECTION_ROUTE` ; les deux `stop*` exigent
   * AGENT/ADMIN/SUPER_ADMIN + `DECLARE_COLLECTION`.
   */
  collectionRoutes: '/collection-routes',
  collectionRouteCompletion: '/collection-routes/completion',
  stopCollected: (depotoirId: string) => `/collection-routes/stops/${depotoirId}/collected`,
  stopInaccessible: (depotoirId: string) => `/collection-routes/stops/${depotoirId}/inaccessible`,

  /**
   * Flotte et IoT. Planification (`collection-schedules`), seuils (`alert-thresholds`) et
   * flotte (`vehicles`) ont un CRUD complet : voir `API_ENDPOINTS`, pas ce registre-ci.
   * Positions et mesures IoT restent des chemins à part : écriture seule, aucune lecture exposée.
   */
  vehiclePositions: '/vehicle-positions',
  measurements: '/measurements',

  /**
   * Provisioning IoT (`/v1/devices/**`, permission `MANAGE_DEVICES`).
   * La clé d'API rendue par `enroll*`/`rotate*Key` n'est plus jamais lisible ensuite — à afficher
   * une seule fois côté UI.
   */
  devices: {
    sensors: '/devices/sensors',
    enrollSensor: '/devices/sensors',
    rotateSensorKey: (sensorId: string) => `/devices/sensors/${sensorId}/key`,
    deactivateSensor: (sensorId: string) => `/devices/sensors/${sensorId}`,
    vehicleTrackers: '/devices/vehicle-trackers',
    enrollVehicleTracker: '/devices/vehicle-trackers',
    rotateVehicleTrackerKey: (trackerId: string) => `/devices/vehicle-trackers/${trackerId}/key`,
    deactivateVehicleTracker: (trackerId: string) => `/devices/vehicle-trackers/${trackerId}`,
  },

  /** Citoyen. */
  collectionSubscriptions: '/collection-subscriptions',
  unsubscribeFromQuartier: (quartierId: string) => `/collection-subscriptions/${quartierId}`,
  deviceTokens: '/device-tokens',
  awareness: '/awareness',
  awarenessMine: '/awareness/mine',

  /** Temps réel. */
  alertStream: '/alerts/stream',

  /** Import GeoJSON (admin, authentifié) — cf. `POST /v1/admin/import/geojson`. */
  adminImportGeojson: '/admin/import/geojson',

  /** Corbeille transverse. */
  deletions: (resource: string) => `/deletions/${resource}`,
  restore: (resource: string, id: string) => `/deletions/${resource}/${id}/restore`,

  /** Hors préfixe /v1 — base `environment.authUrl`. */
  auth: {
    authenticate: '/authenticate',
    register: '/register',
    activation: '/activation',
    refresh: '/refresh',
    logout: '/logout'
  },

  /**
   * Hors préfixe /v1 — racine du serveur.
   * `avis` sert à la fois `POST` (créer, tout compte authentifié) et `GET` (file de traitement
   * par statut via `?statut=SIGNALE|EN_COURS|TRAITE|REJETE`, réservé ADMIN/SUPER_ADMIN).
   */
  avis: '/avis',
  avisMine: '/avis/mine',
  avisMap: '/avis/map',
  /** SIGNALE -> EN_COURS -> TRAITE|REJETE ; une transition interdite répond 409. ADMIN/SUPER_ADMIN. */
  avisUpdateStatus: (avisId: string, statut: string) => `/avis/${avisId}/statut/${statut}`,
} as const;
