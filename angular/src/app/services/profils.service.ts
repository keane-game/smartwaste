import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class ProfilsService {

  private url = '/api/admin/profils';
  constructor(private http: HttpClient) { }

  getProfils(): any {
    return this.http.get(this.url).pipe(
      catchError(this.handleError)
    );
  }

  getProfil(id: number): any {
    return this.http.get(this.url + id).pipe(
      catchError(this.handleError)
    );
  }

  createProfil(profil): any {
    return this.http.post<any>(this.url, profil)
    .pipe(
      catchError(this.handleError)
    );
  }

  updateProfil(profil, id: number): any {
    return this.http.post<any>(this.url + id, profil)
    .pipe(
      catchError(this.handleError)
    );
  }

  deleteBlog(id: number): any {
    return this.http.delete(this.url + id).pipe(
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

      console.error(`Backend returned code ${error.status}, ` + `body was: ${error.error}`);
    }

    // return an observable with a user-facing error message

    return throwError('Something bad happened. Please try again later.');
  }
}
