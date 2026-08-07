import { Injectable } from '@angular/core';
import { User } from '../../models/user.model';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { BehaviorSubject, Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { Router } from '@angular/router';


@Injectable({ providedIn: 'root' })
export class AuthService {

  private currentUserSubject: BehaviorSubject<User>;
  public currentUser: Observable<User>;


  errorData: {} | undefined;
  redirectUrl: string | undefined;

  baseUrl = environment.authUrl;

  // Pas de `TranslateService` ici : il tire `HttpClient` (chargement du fichier de traduction),
  // qui repasse par les intercepteurs, dont `ErrorInterceptor` demande `AuthService` — un aller-
  // retour inutile qui a déjà causé un `NG0200` (dépendance circulaire) au bootstrap. Les
  // messages restent en dur, en français (langue par défaut de l'app).
  constructor(
    private http: HttpClient,
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

  /** Jeton de rafraîchissement — opaque, à traiter comme un secret. */
  getRefreshToken(): string | null {
    const currentUser = JSON.parse(localStorage.getItem('currentUser') || '{}');
    return currentUser.refresh || null;
  }

  /**
   * Échange le jeton de rafraîchissement contre un nouveau couple (`POST /auth/refresh`).
   *
   * <p><b>Pourquoi c'est indispensable ici.</b> Le jeton d'accès est passé de dix jours à
   * quelques minutes, le backend sachant désormais révoquer une session. Sans renouvellement,
   * l'utilisateur est éjecté au bout de quelques minutes, à chaque session.
   *
   * <p>Le backend <b>fait tourner</b> le jeton à chaque appel : celui qu'on vient d'utiliser ne
   * resservira pas. Il faut donc impérativement stocker celui qui revient, sinon la session est
   * perdue au rafraîchissement suivant.
   */
  refreshToken(): Observable<any> {
    const refresh = this.getRefreshToken();
    if (!refresh) {
      return throwError(() => new Error('Aucun jeton de rafraîchissement'));
    }
    return this.http.post<any>(`${this.baseUrl}/refresh`, { refresh }).pipe(
      map(resp => {
        const stored = JSON.parse(localStorage.getItem('currentUser') || '{}');
        // La forme de stockage locale est celle de la réponse d'authentification : `bearer`
        // porte l'accès, `refresh` le renouvellement. `getAuthToken()` lit `bearer`.
        const user = { ...stored, bearer: resp.bearer, refresh: resp.refresh };
        localStorage.setItem('currentUser', JSON.stringify(user));
        this.currentUserSubject.next(user as User);
        return user;
      })
    );
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

  /**
   * Déconnexion.
   *
   * <p>Vider le stockage local ne suffisait pas : le jeton restait parfaitement valide côté
   * serveur jusqu'à son expiration. On ferme donc d'abord la session (`POST /auth/logout`), puis
   * on purge localement. La purge a lieu <b>quoi qu'il arrive</b> — un serveur injoignable ne
   * doit pas laisser l'utilisateur « connecté » dans son navigateur.
   */
  logout(): void {
    const token = this.getAuthToken();
    const purge = () => {
      localStorage.removeItem('currentUser');
      this.currentUserSubject.next(null as any);
      this.router.navigate(['/login']);
    };
    if (!token) { purge(); return; }
    this.http.post(`${this.baseUrl}/logout`, {}).subscribe({ next: purge, error: purge });
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
    const errorMessage = error.error instanceof ErrorEvent
      ? `Erreur côté client : ${error.error.message}`
      : `Erreur serveur : ${error.status}, ${error.message}`;
    console.error(errorMessage);
    return throwError(() => new Error(errorMessage));
  }

}

