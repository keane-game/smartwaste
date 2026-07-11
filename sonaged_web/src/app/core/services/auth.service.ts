import { Injectable } from '@angular/core';
import { User } from '../../models/user.model';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { BehaviorSubject, Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { TranslateService } from '@ngx-translate/core';
import { Router } from '@angular/router';


@Injectable({ providedIn: 'root' })
export class AuthService {

  private currentUserSubject: BehaviorSubject<User>;
  public currentUser: Observable<User>;


  errorData: {} | undefined;
  redirectUrl: string | undefined;
  
  baseUrl = environment.authUrl;

  constructor(
    private http: HttpClient, 
    private translate: TranslateService,
    private router: Router) {
    this.currentUserSubject = new BehaviorSubject<User>(JSON.parse(localStorage.getItem('currentUser') || '{}'));
    this.currentUser = this.currentUserSubject.asObservable();
  }

  public get currentUserValue(): User {
    return this.currentUserSubject.value;
  }

  getAuthToken(): any {
    const currentUser = JSON.parse(localStorage.getItem('currentUser') || '{}');
    return currentUser.bearer;
  }


  register(data: User): Observable<User>{
    return this.http.post<User>(`${this.baseUrl}/register`, data)
     .pipe( catchError(this.handleError.bind(this)));
  }

  login(data: any): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/authenticate`, data)
    .pipe(map(user => {

      // store user details and jwt token in local storage to keep user logged in between page refreshes
      if (user) {
      localStorage.setItem('currentUser', JSON.stringify(user));
      this.currentUserSubject.next(user);
      return user;
      }

    }),
    //catchError(this.handleErrors)
    //catchError(error => this.handleError(error))
    );
  }

  logout(): void {
    // remove user from local storage and set current user to null
    localStorage.removeItem('currentUser');
    this.currentUserSubject.next(null as any);

    this.router.navigate(['/login']);
  }


  isLoggedIn(): boolean {
    if (localStorage.getItem('currentUser')) {
      return true;
    }
    return false;
  }


  isAuthenticated(): boolean {
    // Replace this logic with actual authentication check
    const token = localStorage.getItem('token');
    return !!token;
  }
  private handleError(error: HttpErrorResponse): Observable<never> {
    let errorMessage = this.translate.instant('ERROR.INTERNAL_ERROR');
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
    return throwError(() => new Error(errorMessage));
  }


  private handleErrort(error: HttpErrorResponse): Observable<never> {
    console.error('errorMessage');
    let errorMessage =  this.translate.instant('ERROR.INTERNAL_ERROR');
    console.error(errorMessage);
    if (error.error instanceof ErrorEvent) {
      // A client-side or network error occurred.
      this.translate.get('ERROR.CLIENT_SIDE').subscribe((translation: string) => {
        errorMessage = `${translation}: ${error.error.message}`;
        console.error(errorMessage);
      });
    } else {
      // The backend returned an unsuccessful response code.
      this.translate.get('ERROR.SERVER_SIDE').subscribe((translation: string) => {
        errorMessage = `${translation}: ${error.status}, ${error.message}`;
        console.error(errorMessage);
      });
    }
    console.error(errorMessage);
    return throwError(() => new Error(error.error.message));
  }

}

