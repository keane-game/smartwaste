import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpHeaders, HttpParams } from '@angular/common/http';
import { throwError, Observable, BehaviorSubject } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';
import { environment } from '../../environments/environment';

import { TranslateService } from '@ngx-translate/core';

@Injectable({
  providedIn: 'root'
})
export class SharedService {

  public url = '';
  baseUrl = environment.apiUrl;
  dataUrl = environment.dataUrl
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

  getDepartmentState(): Observable<any>{
    return this.http
     .get(`${this.dataUrl}${this.url}`, { headers: { Accept: 'application/json'} })
       .pipe(
          map(data => data),
          catchError(this.handleError.bind(this))
        );
  }

  fetchDatas(noPage: number = 0, size: number = 5): void {
    this.http.get<any[]>(`${this.baseUrl}${this.url}?noPage=${noPage}&size=${size}`).pipe(
      tap((data) => this.dataSubject.next(data)),
      catchError(this.handleError.bind(this))
    ).subscribe();
  }

  getResources(page: number = 0, size: number = 10): Observable<any> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())

    return this.http
      .get<any>(`${this.baseUrl}${this.url}`, { params, ...this.httpOptions })
      .pipe(
        map(data => data),
        //catchError(this.handleError.bind(this))
      );
  }

  getById(id: number): Observable<any>{
    return this.http.get(`${this.baseUrl}${this.url}/${id}`);
  }

  create(data: any): Observable<any>{
    return this.http.post(`${this.baseUrl}${this.url}`, data)
     .pipe( catchError(this.handleError.bind(this)));
  }

  update(data: any, id: number): Observable<any>{
    return this.http.put(`${this.baseUrl}${this.url}/${id}`, data).pipe(
      catchError(this.handleError.bind(this))
    );
  }

  delete(id: number): Observable<any>{
    return this.http.delete(`${this.baseUrl}${this.url}/${id}`, { responseType: 'text' }).pipe(
      catchError(this.handleError.bind(this))
    );
  }

  private handleErrors(error: HttpErrorResponse): Observable<never> {
    const errorMessage = error.error instanceof ErrorEvent
      ? `Client-side error: ${error.error.message}`
      : `Backend error: ${error.status}, body: ${error.message}`;
  
    console.error(errorMessage);
  
    return throwError(() => new Error(""));
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

  private ipApiUrl = 'https://api.ipify.org?format=json';
  getIpAddress(): Observable<any> {
    return this.http.get<any>(this.ipApiUrl);
  }
  
}