import { HttpErrorResponse, HttpEvent, HttpHandlerFn, HttpInterceptorFn, HttpRequest } from '@angular/common/http';
import { inject } from '@angular/core';
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
 * rafraîchissement est donc en vol, les autres requêtes l'attendent. L'état du verrou vit au
 * niveau du module (et non d'une instance de classe) : un intercepteur fonctionnel est une
 * fonction simple, réinvoquée à chaque requête, sans instance propre où le poser.
 *
 * <p><b>Pourquoi une fonction plutôt qu'une classe `HTTP_INTERCEPTORS`.</b> Cette classe
 * injectait `AuthService` au constructeur ; `AuthService` injecte `HttpClient` — cycle : la
 * construction de `HttpClient` réclame `HTTP_INTERCEPTORS`, qui réclame cet intercepteur, qui
 * réclame `AuthService`, qui réclame `HttpClient` (`NG0200`, jamais détecté par `tsc`/`ng build`
 * puisque c'est un cycle du graphe d'injection à l'exécution, pas une erreur de type). Un
 * intercepteur fonctionnel (`withInterceptors`) résout ses dépendances via `inject()` au moment
 * de la requête, après que `HttpClient` existe déjà — le cycle ne se forme jamais.
 */

let refreshing = false;
const refreshed$ = new BehaviorSubject<string | null>(null);

export const ErrorInterceptor: HttpInterceptorFn = (request, next) => {
  const authenticationService = inject(AuthService);

  // Les appels d'authentification eux-mêmes ne sont jamais rejoués : un 401 sur
  // `/auth/authenticate`, c'est un mot de passe faux, pas un jeton périmé.
  const isAuthCall = request.url.includes('/auth/');

  return next(request).pipe(catchError((err: HttpErrorResponse) => {
    if (err.status === 401 && !isAuthCall && authenticationService.getRefreshToken()) {
      return handleUnauthorized(request, next, authenticationService);
    }
    if (err.status === 401 && !isAuthCall) {
      authenticationService.logout();
      return throwError(() => err);
    }

    notify(err);
    return throwError(() => err);
  }));
};

function handleUnauthorized(
  request: HttpRequest<any>,
  next: HttpHandlerFn,
  authenticationService: AuthService
): Observable<HttpEvent<any>> {
  if (refreshing) {
    // Un rafraîchissement est déjà en vol : on attend son jeton plutôt que d'en demander
    // un second, qui invaliderait le premier.
    return refreshed$.pipe(
      filter((token): token is string => token !== null),
      take(1),
      switchMap(token => next(withToken(request, token)))
    );
  }

  refreshing = true;
  refreshed$.next(null);

  return authenticationService.refreshToken().pipe(
    switchMap(user => {
      refreshing = false;
      refreshed$.next(user.bearer);
      return next(withToken(request, user.bearer));
    }),
    catchError(refreshError => {
      // Session réellement close (révoquée, expirée, ou jeton déjà rejoué) : il n'y a
      // plus rien à tenter.
      refreshing = false;
      authenticationService.logout();
      return throwError(() => refreshError);
    })
  );
}

function withToken(request: HttpRequest<any>, token: string): HttpRequest<any> {
  return request.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
}

/** Message à l'utilisateur. Ne décide de rien : l'erreur continue sa route vers l'appelant. */
function notify(error: HttpErrorResponse): void {
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
