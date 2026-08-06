import { Injectable } from '@angular/core';
import { HttpRequest, HttpHandler, HttpEvent, HttpInterceptor, HttpErrorResponse } from '@angular/common/http';
import { BehaviorSubject, Observable, throwError } from 'rxjs';
import { catchError, filter, switchMap, take } from 'rxjs/operators';
import swal from 'sweetalert';
import { AuthService } from '../services/auth.service';

/**
 * Gestion des erreurs HTTP, et surtout du renouvellement transparent de l'accès.
 *
 * <p><b>Pourquoi ce n'est plus une simple déconnexion sur 401.</b> Le jeton d'accès est passé de
 * dix jours à quelques minutes (le backend sait désormais révoquer une session). Sans
 * renouvellement, l'utilisateur serait éjecté toutes les quelques minutes. Sur 401, on tente donc
 * un `POST /auth/refresh` et on rejoue la requête <b>une seule fois</b> ; si le rafraîchissement
 * échoue, la session est réellement finie et on déconnecte.
 *
 * <p><b>Le verrou n'est pas décoratif.</b> Un écran déclenche facilement plusieurs appels
 * simultanés ; sans lui, chacun lancerait son propre rafraîchissement. Comme le backend
 * <b>fait tourner</b> le jeton à chaque appel, le premier invaliderait celui des autres et
 * déconnecterait l'utilisateur — précisément le bug que la rotation est censée détecter. Un seul
 * rafraîchissement est donc en vol, les autres requêtes l'attendent.
 *
 * <p><b>Ce que la version précédente faisait de travers.</b> Elle lisait `err.error.message` sans
 * garde (une coupure réseau donne `err.error === null`, donc une exception dans le gestionnaire
 * d'exceptions) ; elle passait une <i>chaîne</i> à un gestionnaire qui testait
 * `instanceof HttpErrorResponse`, ce qui rendait tout son `switch` inatteignable ; et elle
 * retournait `of({})`, ce qui <b>avalait</b> chaque erreur — les appelants ne voyaient jamais
 * l'échec et affichaient un écran vide au lieu d'un message.
 */
@Injectable()
export class ErrorInterceptor implements HttpInterceptor {

    private refreshing = false;
    private readonly refreshed$ = new BehaviorSubject<string | null>(null);

    constructor(private authenticationService: AuthService) {}

    intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        return next.handle(request).pipe(catchError((err: HttpErrorResponse) => {
            // Les appels d'authentification eux-mêmes ne sont jamais rejoués : un 401 sur
            // `/auth/authenticate`, c'est un mot de passe faux, pas un jeton périmé.
            const isAuthCall = request.url.includes('/auth/');

            if (err.status === 401 && !isAuthCall && this.authenticationService.getRefreshToken()) {
                return this.handleUnauthorized(request, next);
            }
            if (err.status === 401 && !isAuthCall) {
                this.authenticationService.logout();
                return throwError(() => err);
            }

            this.notify(err);
            return throwError(() => err);
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
                this.refreshed$.next(user.bearer);
                return next.handle(this.withToken(request, user.bearer));
            }),
            catchError(refreshError => {
                // Session réellement close (révoquée, expirée, ou jeton déjà rejoué) : il n'y a
                // plus rien à tenter.
                this.refreshing = false;
                this.authenticationService.logout();
                return throwError(() => refreshError);
            })
        );
    }

    private withToken(request: HttpRequest<any>, token: string): HttpRequest<any> {
        return request.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
    }

    /** Message à l'utilisateur. Ne décide de rien : l'erreur continue sa route vers l'appelant. */
    private notify(error: HttpErrorResponse): void {
        if (!navigator.onLine) {
            swal('Erreur', 'Vous êtes hors connexion. Veuillez vérifier votre connexion internet.', 'error');
            return;
        }

        // `error.error` vaut `null` sur une coupure réseau et peut être une chaîne (nos endpoints
        // de suppression renvoient du texte) : on ne déréférence jamais à l'aveugle.
        const body: any = error.error;
        const detail: string =
            (body && typeof body === 'object' && body.message)
            || (typeof body === 'string' && body)
            || error.statusText
            || '';

        switch (error.status) {
            case 0:
                swal('Erreur', 'Serveur injoignable. Vérifiez votre connexion internet.', 'error');
                break;
            case 400:
            case 403:
            case 404:
                swal('Erreur', detail || 'Requête refusée par le serveur.', 'error');
                break;
            default:
                swal('Erreur', 'Une erreur est survenue. Contactez votre éditeur si le problème persiste.', 'error');
                break;
        }
    }
}
