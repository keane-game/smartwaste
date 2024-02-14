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
    return this.http.delete(`${environment.apiUrl}${this.url}/${id}`).pipe(
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
