import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpHeaders, HttpParams } from '@angular/common/http';
import { throwError, Observable, BehaviorSubject } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';
import { environment } from '../../environments/environment';

import { TranslateService } from '@ngx-translate/core';

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