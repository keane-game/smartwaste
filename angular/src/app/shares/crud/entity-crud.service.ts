import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { EntityConfig } from './entity-config';

/**
 * Client HTTP générique pour les ressources décrites dans `ENTITY_CONFIGS`.
 *
 * Volontairement distinct de `SharedService` : ce dernier porte l'URL courante dans un champ
 * mutable (`sharedService.url = '...'` avant chaque appel), ce qui devient incorrect dès que
 * deux requêtes concurrentes visent des ressources différentes — la seconde écrase l'URL de la
 * première. Ici, le chemin est passé à chaque appel, donc sans état partagé.
 *
 * `SharedService` reste en place et inchangé pour les écrans existants.
 */
@Injectable({ providedIn: 'root' })
export class EntityCrudService {

  constructor(private http: HttpClient) { }

  /** Liste complète (tableau JSON) — utilise `listPath`, qui peut différer de `basePath`. */
  list(config: EntityConfig): Observable<any[]> {
    return this.http
      .get<any[]>(`${environment.apiUrl}${config.listPath}`, { headers: { Accept: 'application/json' } })
      .pipe(catchError(this.handleError));
  }

  /** Page (endpoints qui renvoient un `Page<T>` Spring Data). */
  page(config: EntityConfig, page: number, size: number): Observable<any> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http
      .get<any>(`${environment.apiUrl}${config.basePath}`, { params })
      .pipe(catchError(this.handleError));
  }

  getById(config: EntityConfig, id: string | number): Observable<any> {
    return this.http
      .get<any>(`${environment.apiUrl}${config.basePath}/${id}`)
      .pipe(catchError(this.handleError));
  }

  create(config: EntityConfig, payload: any): Observable<any> {
    return this.http
      .post<any>(`${environment.apiUrl}${config.basePath}`, payload)
      .pipe(catchError(this.handleError));
  }

  update(config: EntityConfig, id: string | number, payload: any): Observable<any> {
    return this.http
      .put<any>(`${environment.apiUrl}${config.basePath}/${id}`, payload)
      .pipe(catchError(this.handleError));
  }

  /**
   * Suppression. Les endpoints renvoient du texte brut, pas du JSON, d'où `responseType: 'text'`
   * (sans quoi Angular échoue au parsing malgré un HTTP 200).
   */
  remove(config: EntityConfig, id: string | number): Observable<any> {
    const path = config.deletePathTemplate
      ? config.deletePathTemplate.replace('{id}', String(id))
      : `${config.basePath}/${id}`;
    return this.http
      .delete(`${environment.apiUrl}${path}`, { responseType: 'text' })
      .pipe(catchError(this.handleError));
  }

  /** Corbeille générique : `GET /v1/deletions/{resource}`. */
  pendingDeletions(resource: string): Observable<any[]> {
    return this.http
      .get<any[]>(`${environment.apiUrl}/deletions/${resource}`)
      .pipe(catchError(this.handleError));
  }

  /** Restauration générique : `POST /v1/deletions/{resource}/{id}/restore`. */
  restore(resource: string, id: string | number): Observable<any> {
    return this.http
      .post(`${environment.apiUrl}/deletions/${resource}/${id}/restore`, {}, { responseType: 'text' })
      .pipe(catchError(this.handleError));
  }

  /**
   * Transforme l'erreur HTTP en message lisible. Le backend renvoie selon les cas un corps
   * JSON (`{message}` / `{error}`), du texte brut, ou rien — les trois sont couverts.
   */
  private handleError(error: HttpErrorResponse): Observable<never> {
    let message: string;
    if (error.status === 0) {
      message = "Backend injoignable. Vérifiez que l'API tourne sur " + environment.apiUrl + '.';
    } else if (error.status === 401) {
      message = 'Session expirée ou non authentifiée.';
    } else if (error.status === 403) {
      message = "Vous n'avez pas les droits pour cette opération.";
    } else if (error.status === 404) {
      message = 'Ressource introuvable.';
    } else if (error.status === 409) {
      message = typeof error.error === 'string' && error.error
        ? error.error
        : 'Opération en conflit (délai de restauration dépassé ?).';
    } else {
      const body: any = error.error;
      message = (body && (body.message || body.error))
        || (typeof body === 'string' && body)
        || `Erreur ${error.status}.`;
    }
    return throwError(() => new Error(message));
  }
}
