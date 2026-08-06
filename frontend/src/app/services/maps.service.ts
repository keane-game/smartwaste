import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpHeaders, HttpParams } from '@angular/common/http';
import { throwError, Observable, BehaviorSubject } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';
import { environment } from '../../environments/environment';

import { TranslateService } from '@ngx-translate/core';
import { API_PATHS } from '../shared/constants/api-endpoints';

/**
 * Point géographique tel que le backend le sérialise.
 *
 * <p><b>Le piège :</b> `latitude` et `longitude` sont des <b>chaînes</b>, pas des nombres. Leaflet
 * attend des `number` — il faut passer par `parseFloat`, faute de quoi les marqueurs se posent
 * silencieusement en `NaN` et la carte apparaît vide sans lever d'erreur.
 */
export interface GeoCoordinate {
  coordinateId?: string;
  latitude: string;
  longitude: string;
  altitude?: string;
}

/** Contour du département de référence — `GET /v1/maps/departments`. */
export interface DepartmentMap {
  name: string;
  code: string;
  typeGeo: string;
  coordinates: GeoCoordinate[];
}

/** Point de collecte géolocalisé — `GET /v1/maps/depotoirs`. */
export interface DepotoirMap {
  address: string;
  typeDepot: string;
  typeGeo: string;
  coordinates: GeoCoordinate[];
  /** `null` si jamais mesuré — pas de capteur sur ce point. */
  fillLevelPercent: number | null;
  lastMeasuredAt: string | null;
}

/** Tranche de remplissage, pour la couleur du marqueur — jamais mesuré compte à part. */
export type FillLevelBucket = 'unknown' | 'ok' | 'warning' | 'danger';

export function fillLevelBucket(fillLevelPercent: number | null | undefined): FillLevelBucket {
  if (fillLevelPercent === null || fillLevelPercent === undefined) {
    return 'unknown';
  }
  if (fillLevelPercent >= 80) {
    return 'danger';
  }
  if (fillLevelPercent >= 50) {
    return 'warning';
  }
  return 'ok';
}

export const FILL_LEVEL_COLORS: Record<FillLevelBucket, string> = {
  unknown: '#6c757d',
  ok: '#2eca6a',
  warning: '#ff771d',
  danger: '#dc3545',
};

/**
 * Véhicule en circulation — `GET /v1/maps/vehicles`.
 *
 * <p>Ici les coordonnées sont bien des `number` : ce read-model est plus récent que les deux
 * autres. La fraîcheur (« en circulation ») est décidée par le backend, pas par un paramètre.
 */
export interface VehicleOnMap {
  vehicleId: string;
  registration: string;
  label: string;
  latitude: number;
  longitude: number;
  lastPositionAt: string;
}

@Injectable({
  providedIn: 'root'
})
export class MapsService {

  public url = '';
  baseUrl = environment.apiUrl + "/maps";
  private dataSubject = new BehaviorSubject<any[]>([]); // Replace 'any[]' with your actual data type

  datas$ = this.dataSubject.asObservable();
  constructor(private http: HttpClient, private translate: TranslateService) { }
   httpOptions = {
    headers: new HttpHeaders({
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    })
  };
  
  getAll(): Observable<any>{
    return this.http
     .get(`${this.baseUrl}${this.url}`, { headers: { Accept: 'application/json'} })
       .pipe(
          map(data => data),
          catchError(this.handleError.bind(this))
        );
  }

  // ---- Accès typés aux endpoints cartographiques (`/v1/maps/**`) ----
  // Ces trois endpoints n'étaient exploités par aucun front. `/maps/vehicles` ne l'était nulle
  // part du tout.

  /** Contour du département de référence. */
  getDepartment(): Observable<DepartmentMap> {
    return this.http.get<DepartmentMap>(`${environment.apiUrl}${API_PATHS.mapsDepartments}`)
      .pipe(catchError(this.handleError.bind(this)));
  }

  /** Points de collecte géolocalisés. */
  getDepotoirs(): Observable<DepotoirMap[]> {
    return this.http.get<DepotoirMap[]>(`${environment.apiUrl}${API_PATHS.mapsDepotoirs}`)
      .pipe(catchError(this.handleError.bind(this)));
  }

  /** Flotte en circulation, avec la date de dernière position. */
  getVehicles(): Observable<VehicleOnMap[]> {
    return this.http.get<VehicleOnMap[]>(`${environment.apiUrl}${API_PATHS.mapsVehicles}`)
      .pipe(catchError(this.handleError.bind(this)));
  }

  /** Convertit les coordonnées chaînes du backend en couples `[lat, lng]` utilisables par Leaflet. */
  static toLatLng(coordinates: GeoCoordinate[] | null | undefined): [number, number][] {
    return (coordinates ?? [])
      .map(c => [parseFloat(c?.latitude), parseFloat(c?.longitude)] as [number, number])
      .filter(([lat, lng]) => !isNaN(lat) && !isNaN(lng));
  }


  private handleError(error: HttpErrorResponse): Observable<never> {
    let errorMessage =  this.translate.instant('ERROR.INTERNAL_ERROR');
    if (error.error instanceof ErrorEvent) {
      // A client-side or network error occurred.
      this.translate.get('ERROR.CLIENT_SIDE').subscribe((translation: string) => {
        errorMessage = `${translation}: ${error.error.message}`;
      });
    } else {
      // The backend returned an unsuccessful response code.
      this.translate.get('ERROR.SERVER_SIDE').subscribe((translation: string) => {
        errorMessage = `${translation}: ${error.status}, ${error.message}`;
      });
    }
    console.error(errorMessage);
    return throwError(() => new Error(this.translate.instant('ERROR.GENERIC')));
  }
  
}