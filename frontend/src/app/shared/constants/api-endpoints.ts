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
  /** Nom du champ identifiant dans le DTO — un UUID v7, jamais un entier (ADR-0012). */
  idField: string;
  /**
   * Clé attendue par `/v1/deletions/{resource}` (corbeille 30 j).
   *
   * Le backend la dérive du nom du bean repository (`communeRepository` → `commune`), elle ne
   * coïncide donc PAS avec le segment de chemin (`communes`, `circuit-collects`…). Sans elle,
   * la corbeille répond 404.
   */
  deletionResource: string;
  /** Chemin de suppression non standard (sinon `${basePath}/${id}`). */
  deletePathTemplate?: string;
  /** Limite connue de l'API, à afficher plutôt qu'à découvrir en production. */
  notes?: string;
}

export const API_ENDPOINTS = {
  regions: {
    basePath: '/regions', listPath: '/regions', idField: 'regionId',
    deletionResource: 'region',
    notes: "Le backend n'expose ni PUT ni DELETE sur /v1/regions. Il n'existe pas de /regions/all."
  },
  departments: {
    basePath: '/departments', listPath: '/departments', idField: 'departmentId',
    deletionResource: 'department'
  },
  communes: {
    basePath: '/communes', listPath: '/communes/s', idField: 'communeId',
    deletionResource: 'commune'
  },
  quartiers: {
    basePath: '/quartiers', listPath: '/quartiers/s', idField: 'quartierId',
    deletionResource: 'quartier'
  },
  depotoirs: {
    basePath: '/depotoirs', listPath: '/depotoirs/s', idField: 'depotoirId',
    deletionResource: 'depotoir',
    notes: 'Seule ressource à exposer aussi /depotoirs/deletions et /depotoirs/{id}/restore.'
  },
  typedepotoirs: {
    basePath: '/typedepotoirs', listPath: '/typedepotoirs', idField: 'typeDepotoirId',
    deletionResource: 'typedepotoir'
  },
  users: {
    basePath: '/users', listPath: '/users/s', idField: 'userId',
    deletionResource: 'user',
    notes: "Le DTO expose `userId`, pas `id`."
  },
  authorities: {
    basePath: '/authorities', listPath: '/authorities', idField: 'authorityId',
    deletionResource: 'authority',
    notes: "Tout le contrôleur exige l'autorité MANAGE_ROLE."
  },
  alerts: {
    basePath: '/alerts', listPath: '/alerts/s', idField: 'alertId',
    deletionResource: 'alert',
    notes: 'Création en POST sur /alerts ET sur /alerts/s ; multipart accepté. Flux SSE sur /alerts/stream.'
  },
  circuits: {
    basePath: '/circuits', listPath: '/circuits', idField: 'circuitId',
    deletionResource: 'circuit'
  },
  'circuit-collects': {
    basePath: '/circuit-collects', listPath: '/circuit-collects', idField: 'circuitcollectId',
    deletionResource: 'circuitcollect'
  },
  'circuit-balayages': {
    basePath: '/circuit-balayages', listPath: '/circuit-balayages', idField: 'circuitbalayageId',
    deletionResource: 'circuitbalayage'
  },
  'moblier-urbains': {
    basePath: '/moblier-urbains', listPath: '/moblier-urbains', idField: 'moblierUrbainId',
    deletionResource: 'moblierurbain'
  },
  coordinates: {
    basePath: '/coordinates', listPath: '/coordinates', idField: 'coordinateId',
    deletionResource: 'coordinate',
    deletePathTemplate: '/coordinates/delete/coordinate/{id}',
    notes: 'Chemin de suppression non standard côté backend.'
  },
  geometries: {
    basePath: '/geometries', listPath: '/geometries', idField: 'geometryId',
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
  pointJournal: (depotoirId: string) => `/supervision/points/${depotoirId}/journal`,

  /** Tournée de l'agent de collecte. */
  collectionRoutes: '/collection-routes',
  collectionRouteCompletion: '/collection-routes/completion',
  stopCollected: (depotoirId: string) => `/collection-routes/stops/${depotoirId}/collected`,
  stopInaccessible: (depotoirId: string) => `/collection-routes/stops/${depotoirId}/inaccessible`,

  /** Planification et seuils. */
  collectionSchedules: '/collection-schedules',
  alertThresholds: '/alert-thresholds',

  /** Flotte et IoT. */
  vehicles: '/vehicles',
  vehiclePositions: '/vehicle-positions',
  measurements: '/measurements',
  sensors: '/devices/sensors',
  vehicleTrackers: '/devices/vehicle-trackers',

  /** Citoyen. */
  collectionSubscriptions: '/collection-subscriptions',
  deviceTokens: '/device-tokens',
  awareness: '/awareness',
  awarenessMine: '/awareness/mine',

  /** Temps réel. */
  alertStream: '/alerts/stream',

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

  /** Hors préfixe /v1 — racine du serveur. */
  avis: '/avis',
  avisMine: '/avis/mine',
  avisMap: '/avis/map'
} as const;
