/**
 * Registre déclaratif des entités exposées par l'API backend.
 *
 * Plutôt que de dupliquer un composant « liste » + « formulaire » par entité (12 entités ×
 * 3 écrans), chaque ressource est décrite une seule fois ici et rendue par les composants
 * génériques `EntityListComponent` / `EntityFormComponent`.
 *
 * Les chemins reflètent fidèlement le backend, y compris ses irrégularités :
 *  - certaines ressources exposent la liste complète sur un chemin à double « s »
 *    (`/communess`, `/quartierss`, `/depotoirss`, `/userss`, `/alertss`) tandis que le
 *    chemin simple renvoie une `Page` ;
 *  - `Region` n'expose ni PUT ni DELETE ;
 *  - `Coordinate` a un chemin de suppression non standard.
 */

export type FieldType = 'text' | 'number' | 'textarea' | 'checkbox';

export interface FieldConfig {
  /** Nom de la propriété dans le DTO backend. */
  name: string;
  label: string;
  type: FieldType;
  required?: boolean;
  /** Longueur maximale appliquée côté formulaire. */
  maxLength?: number;
  /** Affiché en lecture seule (identifiants techniques, références). */
  readonlyOnEdit?: boolean;
  hint?: string;
}

export interface EntityConfig {
  /** Clé de route, ex. `communes` → /entities/communes. */
  key: string;
  /** Libellé singulier, utilisé dans les titres et messages. */
  label: string;
  labelPlural: string;
  /** Chemin CRUD de base (POST/PUT/DELETE/GET by id), relatif à environment.apiUrl. */
  basePath: string;
  /**
   * Chemin renvoyant la liste complète (tableau JSON). Diffère de `basePath` quand le
   * backend réserve le chemin simple à une réponse paginée.
   */
  listPath: string;
  /** Nom du champ identifiant dans le DTO. */
  idField: string;
  /** Colonnes affichées dans la liste. */
  columns: string[];
  fields: FieldConfig[];
  canCreate: boolean;
  canUpdate: boolean;
  canDelete: boolean;
  /** La ressource expose /deletions et /{id}/restore (corbeille 30 j). */
  softDelete?: boolean;
  /**
   * Clé de ressource attendue par `/v1/deletions/{resource}`.
   *
   * Le backend la dérive du nom du bean repository (`communeRepository` -> `commune`), elle
   * ne coïncide donc PAS avec la clé de route (`communes`, `circuit-collects`...). Sans ce
   * champ, la corbeille répondrait 404.
   */
  deletionResource: string;
  /** Chemin de suppression non standard (sinon `${basePath}/${id}`). */
  deletePathTemplate?: string;
  /** Raison affichée quand une opération n'est pas disponible côté backend. */
  notes?: string;
}

const text = (name: string, label: string, opts: Partial<FieldConfig> = {}): FieldConfig =>
  ({ name, label, type: 'text', ...opts });

export const ENTITY_CONFIGS: EntityConfig[] = [
  {
    key: 'regions', deletionResource: 'region',
    label: 'Région', labelPlural: 'Régions',
    basePath: '/regions', listPath: '/regions', idField: 'regionId',
    columns: ['regionId', 'name', 'code'],
    fields: [text('name', 'Nom', { required: true, maxLength: 120 }), text('code', 'Code', { maxLength: 60 })],
    canCreate: true, canUpdate: false, canDelete: false,
    notes: "Le backend n'expose ni PUT ni DELETE sur /v1/regions."
  },
  {
    key: 'departments', deletionResource: 'department',
    label: 'Département', labelPlural: 'Départements',
    basePath: '/departments', listPath: '/departments', idField: 'departmentId',
    columns: ['departmentId', 'name', 'code'],
    fields: [text('name', 'Nom', { required: true, maxLength: 120 }), text('code', 'Code', { maxLength: 60 })],
    canCreate: true, canUpdate: true, canDelete: true
  },
  {
    key: 'communes', deletionResource: 'commune',
    label: 'Commune', labelPlural: 'Communes',
    basePath: '/communes', listPath: '/communess', idField: 'communeId',
    columns: ['communeId', 'name', 'code', 'total', 'area'],
    fields: [
      text('name', 'Nom', { required: true, maxLength: 120 }),
      text('code', 'Code', { maxLength: 60 }),
      text('total', 'Population totale'),
      text('women', 'Femmes'),
      text('men', 'Hommes'),
      text('length', 'Périmètre'),
      text('area', 'Superficie')
    ],
    canCreate: true, canUpdate: true, canDelete: true
  },
  {
    key: 'quartiers', deletionResource: 'quartier',
    label: 'Quartier', labelPlural: 'Quartiers',
    basePath: '/quartiers', listPath: '/quartierss', idField: 'quartierId',
    columns: ['quartierId', 'name', 'code', 'cav', 'length'],
    fields: [
      text('name', 'Nom', { required: true, maxLength: 120 }),
      text('code', 'Code', { maxLength: 60 }),
      text('cav', 'CAV'),
      text('codeCav', 'Code CAV'),
      text('cCrca', 'CCRCA'),
      text('codeCcrca', 'Code CCRCA'),
      text('numerozr', 'Numéro ZR'),
      text('codeSzr', 'Code SZR'),
      text('zoneCoron', 'Zone Coron'),
      text('poucentage', 'Pourcentage'),
      text('length', 'Périmètre')
    ],
    canCreate: true, canUpdate: true, canDelete: true
  },
  {
    key: 'circuits', deletionResource: 'circuit',
    label: 'Circuit', labelPlural: 'Circuits',
    basePath: '/circuits', listPath: '/circuits', idField: 'circuitId',
    columns: ['circuitId', 'name', 'code'],
    fields: [text('name', 'Nom', { required: true, maxLength: 120 }), text('code', 'Code', { maxLength: 60 })],
    canCreate: true, canUpdate: true, canDelete: true
  },
  {
    key: 'circuit-collects', deletionResource: 'circuitcollect',
    label: 'Circuit de collecte', labelPlural: 'Circuits de collecte',
    basePath: '/circuit-collects', listPath: '/circuit-collects', idField: 'circuitcollectId',
    columns: ['circuitcollectId', 'name', 'code', 'frequency', 'type'],
    fields: [
      text('name', 'Nom', { required: true, maxLength: 120 }),
      text('code', 'Code', { maxLength: 60 }),
      text('length', 'Longueur'),
      text('frequency', 'Fréquence'),
      text('type', 'Type'),
      text('cat', 'Catégorie'),
      text('rotation', 'Rotation'),
      { name: 'communeId', label: 'Commune (identifiant)', type: 'number',
        hint: 'Référence par identifiant (ADR-0012) — voir la liste des communes.' }
    ],
    canCreate: true, canUpdate: true, canDelete: true
  },
  {
    key: 'circuit-balayages', deletionResource: 'circuitbalayage',
    label: 'Circuit de balayage', labelPlural: 'Circuits de balayage',
    basePath: '/circuit-balayages', listPath: '/circuit-balayages', idField: 'circuitbalayageId',
    columns: ['circuitbalayageId', 'name', 'code', 'shift', 'length'],
    fields: [
      text('name', 'Nom', { required: true, maxLength: 120 }),
      text('code', 'Code', { maxLength: 60 }),
      text('shift', 'Shift', { hint: 'Valeur attendue par le backend (énumération CircuitShift).' }),
      text('length', 'Longueur'),
      { name: 'communeId', label: 'Commune (identifiant)', type: 'number',
        hint: 'Référence par identifiant (ADR-0012).' }
    ],
    canCreate: true, canUpdate: true, canDelete: true
  },
  {
    key: 'moblier-urbains', deletionResource: 'moblierurbain',
    label: 'Mobilier urbain', labelPlural: 'Mobiliers urbains',
    basePath: '/moblier-urbains', listPath: '/moblier-urbains', idField: 'moblierUrbainId',
    columns: ['moblierUrbainId', 'name', 'code'],
    fields: [text('name', 'Nom', { required: true, maxLength: 120 }), text('code', 'Code', { maxLength: 60 })],
    canCreate: true, canUpdate: true, canDelete: true
  },
  {
    key: 'typedepotoirs', deletionResource: 'typedepotoir',
    label: 'Type de dépotoir', labelPlural: 'Types de dépotoir',
    basePath: '/typedepotoirs', listPath: '/typedepotoirs', idField: 'typeDepotoirId',
    columns: ['typeDepotoirId', 'name'],
    fields: [text('name', 'Nom', { required: true, maxLength: 120 })],
    canCreate: true, canUpdate: true, canDelete: true
  },
  {
    key: 'authorities', deletionResource: 'authority',
    label: 'Rôle', labelPlural: 'Rôles',
    basePath: '/authorities', listPath: '/authorities', idField: 'authorityId',
    columns: ['authorityId', 'name', 'code'],
    fields: [text('name', 'Nom', { required: true, maxLength: 120 }), text('code', 'Code', { maxLength: 60 })],
    canCreate: true, canUpdate: true, canDelete: true
  },
  {
    key: 'coordinates', deletionResource: 'coordinate',
    label: 'Coordonnée', labelPlural: 'Coordonnées',
    basePath: '/coordinates', listPath: '/coordinates', idField: 'coordinateId',
    columns: ['coordinateId', 'latitude', 'longitude', 'altitude'],
    fields: [
      text('latitude', 'Latitude', { required: true }),
      text('longitude', 'Longitude', { required: true }),
      text('altitude', 'Altitude')
    ],
    canCreate: true, canUpdate: true, canDelete: true,
    deletePathTemplate: '/coordinates/delete/coordinate/{id}',
    notes: 'Chemin de suppression non standard côté backend.'
  },
  {
    key: 'geometries', deletionResource: 'geometry',
    label: 'Géométrie', labelPlural: 'Géométries',
    basePath: '/geometries', listPath: '/geometries', idField: 'geometryId',
    columns: ['geometryId', 'type', 'spatialReference'],
    fields: [
      text('type', 'Type', { required: true }),
      text('spatialReference', 'Référence spatiale'),
      { name: 'ring', label: 'Ring', type: 'textarea' }
    ],
    canCreate: true, canUpdate: true, canDelete: true
  }
];

export function findEntityConfig(key: string): EntityConfig | undefined {
  return ENTITY_CONFIGS.find(c => c.key === key);
}
