import { Injectable } from '@angular/core';
import { User } from '../models/user.model';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { BehaviorSubject, Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';


@Injectable({ providedIn: 'root' })
export class AuthService {

  private currentUserSubject: BehaviorSubject<User>;
  public currentUser: Observable<User>;


  errorData: {} | undefined;
  redirectUrl: string | undefined;

  constructor(private http: HttpClient) {
    this.currentUserSubject = new BehaviorSubject<User>(JSON.parse(localStorage.getItem('currentUser') || '{}'));
    this.currentUser = this.currentUserSubject.asObservable();
  }

  public get currentUserValue(): User {
    return this.currentUserSubject.value;
  }

  getAuthToken(): any {
    const currentUser = JSON.parse(localStorage.getItem('currentUser') || '{}');
    return currentUser.token;
  }

  login(data: any): Observable<any> {
    return this.http.post<any>('http://localhost:8000/api/login', data)
    .pipe(map(user => {

      // store user details and jwt token in local storage to keep user logged in between page refreshes
      if (user) {
      localStorage.setItem('currentUser', JSON.stringify(user));
      this.currentUserSubject.next(user);
      return user;
      }

    }),
    catchError(this.handleError)
    );
  }

  logout(): void {
    // remove user from local storage and set current user to null
    localStorage.removeItem('currentUser');
    this.currentUserSubject.next(null as any);
  }
  isLoggedIn(): boolean {
    if (localStorage.getItem('currentUser')) {
      return true;
    }
    return false;
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
    this.errorData = {
      errorTitle: 'Oops! Request for document failed',
      errorDesc: 'Something bad happened. Please try again later.'
    };
    return throwError(this.errorData);
  }
}

