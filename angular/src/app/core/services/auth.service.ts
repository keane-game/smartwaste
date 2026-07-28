import { Injectable } from '@angular/core';
import { User } from '../../models/user.model';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { BehaviorSubject, Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';


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

  /** Jeton de rafraîchissement — opaque, à traiter comme un secret. */
  getRefreshToken(): string | null {
    const currentUser = JSON.parse(localStorage.getItem('currentUser') || '{}');
    return currentUser.refresh || null;
  }

  private get authBase(): string {
    return environment.apiUrl.replace(/\/v1\/?$/, '');
  }

  /**
   * Échange le jeton de rafraîchissement contre un nouveau couple.
   *
   * <p>Le backend fait tourner le jeton à chaque appel : celui qu'on vient d'utiliser ne resservira
   * pas. Il faut donc impérativement stocker celui qui revient, sinon la session est perdue au
   * rafraîchissement suivant.
   */
  refreshToken(): Observable<any> {
    const refresh = this.getRefreshToken();
    if (!refresh) {
      return throwError(() => new Error('Aucun jeton de rafraîchissement'));
    }
    return this.http.post<any>(`${this.authBase}/auth/refresh`, { refresh }).pipe(
      map(resp => {
        const stored = JSON.parse(localStorage.getItem('currentUser') || '{}');
        const user = { ...stored, token: resp.bearer, refresh: resp.refresh };
        localStorage.setItem('currentUser', JSON.stringify(user));
        this.currentUserSubject.next(user as User);
        return user;
      })
    );
  }

  login(data: any): Observable<any> {
    // Backend SONAGED : POST {host}/auth/authenticate (hors préfixe /v1) avec { username, password }.
    // La réponse est { bearer: <jwt> } ; on la normalise en { token } pour getAuthToken()/l'intercepteur.
    const authBase = environment.apiUrl.replace(/\/v1\/?$/, '');
    return this.http.post<any>(`${authBase}/auth/authenticate`, data)
    .pipe(map(resp => {
      const token = resp && (resp.bearer || resp.token);
      if (token) {
        // `refresh` est conservé tel quel : c'est lui qui permettra de renouveler l'accès
        // sans redemander le mot de passe, maintenant que le jeton d'accès est de courte durée.
        const user = { ...resp, token };
        // store user details and jwt token in local storage to keep user logged in between page refreshes
        localStorage.setItem('currentUser', JSON.stringify(user));
        this.currentUserSubject.next(user as User);
        return user;
      }
      return null;
    }),
    catchError(this.handleError)
    );
  }

  /** Inscription : POST {host}/auth/register (hors préfixe /v1) avec un objet User. */
  register(user: any): Observable<any> {
    const authBase = environment.apiUrl.replace(/\/v1\/?$/, '');
    return this.http.post(`${authBase}/auth/register`, user, { responseType: 'text' })
      .pipe(catchError(this.handleError));
  }

  /** Activation du compte via le code reçu par e-mail : POST {host}/auth/activation. */
  activate(code: string): Observable<any> {
    const authBase = environment.apiUrl.replace(/\/v1\/?$/, '');
    return this.http.post(`${authBase}/auth/activation`, { code }, { responseType: 'text' })
      .pipe(catchError(this.handleError));
  }

  /**
   * Déconnexion.
   *
   * <p>Vider le stockage local ne suffisait pas : le jeton restait parfaitement valide côté serveur
   * jusqu'à son expiration. On ferme donc d'abord la session (`POST /auth/logout`), puis on purge
   * localement. La purge a lieu quoi qu'il arrive — un serveur injoignable ne doit pas laisser
   * l'utilisateur « connecté » dans son navigateur.
   */
  logout(): void {
    const token = this.getAuthToken();
    const purge = () => {
      localStorage.removeItem('currentUser');
      this.currentUserSubject.next(null as any);
    };
    if (!token) { purge(); return; }
    this.http.post(`${this.authBase}/auth/logout`, {}).subscribe({ next: purge, error: purge });
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

