import { Injectable } from '@angular/core';
import { HttpRequest, HttpHandler, HttpEvent, HttpInterceptor } from '@angular/common/http';
import { BehaviorSubject, Observable, throwError } from 'rxjs';
import { catchError, filter, switchMap, take } from 'rxjs/operators';

import { AuthService } from '../services/auth.service';

/**
 * Gestion des erreurs HTTP, et surtout du renouvellement transparent de l'accès.
 *
 * <p><b>Pourquoi ce n'est plus une simple déconnexion sur 401.</b> Le jeton d'accès est passé de
 * 10 jours à quelques minutes (le backend sait désormais révoquer une session). Sans
 * renouvellement, l'utilisateur serait éjecté toutes les 15 minutes. Sur 401, on tente donc un
 * `POST /auth/refresh` et on rejoue la requête **une seule fois** ; si le rafraîchissement échoue,
 * la session est réellement finie et on déconnecte.
 *
 * <p><b>Le verrou n'est pas décoratif.</b> Un écran déclenche facilement plusieurs appels
 * simultanés ; sans lui, chacun lancerait son propre rafraîchissement. Comme le backend **fait
 * tourner** le jeton à chaque appel, le premier invaliderait celui des autres et déconnecterait
 * l'utilisateur — précisément le bug que la rotation est censée détecter. Un seul rafraîchissement
 * est donc en vol, les autres requêtes l'attendent.
 */
@Injectable()
export class ErrorInterceptor implements HttpInterceptor {

    private refreshing = false;
    private readonly refreshed$ = new BehaviorSubject<string | null>(null);

    constructor(private authenticationService: AuthService) {}

    intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        return next.handle(request).pipe(catchError(err => {
            const isAuthCall = request.url.includes('/auth/');
            if (err.status === 401 && !isAuthCall && this.authenticationService.getRefreshToken()) {
                return this.handleUnauthorized(request, next);
            }
            if (err.status === 401) {
                this.authenticationService.logout();
                location.reload();
            }
            return throwError(() => err.error?.message || err.statusText || err);
        }));
    }

    private handleUnauthorized(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        if (this.refreshing) {
            // Un rafraîchissement est déjà en vol : on attend son jeton plutôt que d'en demander
            // un second, qui invaliderait le premier.
            return this.refreshed$.pipe(
                filter((token): token is string => token !== null),
                take(1),
                switchMap(token => next.handle(this.withToken(request, token)))
            );
        }

        this.refreshing = true;
        this.refreshed$.next(null);

        return this.authenticationService.refreshToken().pipe(
            switchMap(user => {
                this.refreshing = false;
                this.refreshed$.next(user.token);
                return next.handle(this.withToken(request, user.token));
            }),
            catchError(refreshError => {
                // Session réellement close (révoquée, expirée, ou jeton déjà rejoué) : il n'y a
                // plus rien à tenter.
                this.refreshing = false;
                this.authenticationService.logout();
                location.reload();
                return throwError(() => refreshError);
            })
        );
    }

    private withToken(request: HttpRequest<any>, token: string): HttpRequest<any> {
        return request.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
    }
}
