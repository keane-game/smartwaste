import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { throwError, Observable } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { environment } from '../../environments/environment';


@Injectable({
  providedIn: 'root'
})
export class SharedService {

  public url = '';
  constructor(private http: HttpClient) { }

  getAll(): Observable<any>{
    return this.http
     .get(environment.apiUrl + this.url, {headers: {Accept: 'application/json'} })
       .pipe(
        map(data => {
            // const datas: any[] = [];
            // for (const key in data){
            //   if (data.hasOwnProperty(key)){
            //     datas.push({...data[key], id: +key + 1});
            //   }
            // }
            return data;
          },
           catchError(this.handleError)));
  }

  getById(id: number): Observable<any>{
    return this.http.get(`${environment.apiUrl}${this.url}/${id}`);
  }

  create(data: any): Observable<any>{
    return this.http.post(`${environment.apiUrl}${this.url}`, data)
     .pipe( catchError(this.handleError));
  }

  update(data: any, id: number): Observable<any>{
    return this.http.put(`${environment.apiUrl}${this.url}/${id}`, data).pipe(
      catchError(this.handleError));
  }

  delete(id: number): Observable<any>{
    // Les endpoints de suppression renvoient un message texte (pas du JSON).
    return this.http.delete(`${environment.apiUrl}${this.url}/${id}`, {responseType: 'text'}).pipe(
    catchError(this.handleError)
    );
  }

  // ---- Soft-delete (suppression logique, rétention 30 j, restauration) ----

  /** Liste des éléments en attente de suppression (avec date de purge prévue). */
  getDeletions(): Observable<any>{
    return this.http
      .get(`${environment.apiUrl}${this.url}/deletions`, {headers: {Accept: 'application/json'}})
      .pipe(catchError(this.handleError));
  }

  /** Restaure un élément en attente de suppression (si le délai n'est pas dépassé). */
  restore(id: number): Observable<any>{
    return this.http.post(`${environment.apiUrl}${this.url}/${id}/restore`, {}).pipe(
      catchError(this.handleError)
    );
  }

  // ---- Variante générique (endpoints transverses /v1/deletions/{resource}) ----
  // Utilisable par n'importe quelle ressource sans configurer `url`. La réponse enveloppe
  // l'entité : { item, deletionRequestedAt, purgeDueAt }.

  /** Éléments en attente de suppression pour une ressource (clé = nom d'entité, ex. 'depotoir'). */
  getPendingDeletions(resource: string): Observable<any>{
    return this.http
      .get(`${environment.apiUrl}/deletions/${resource}`, {headers: {Accept: 'application/json'}})
      .pipe(catchError(this.handleError));
  }

  /** Restaure un élément d'une ressource donnée. */
  restoreResource(resource: string, id: number): Observable<any>{
    return this.http.post(`${environment.apiUrl}/deletions/${resource}/${id}/restore`, {}).pipe(
      catchError(this.handleError)
    );
  }


  private handleError(error: HttpErrorResponse): any {
    if (error.error instanceof ErrorEvent) {

      // A client-side or network error occurred. Handle it accordingly.

      console.error('An error occurred:', error.error.message);
    } else {

      // The backend returned an unsuccessful response code.

      // The response body may contain clues as to what went wrong.

      console.error(`Backend returned code ${error}, ` + `body was: ${error}`);
    }

    // return an observable with a user-facing error message

    return throwError('Something bad happened. Please try again later.');
  }
}
