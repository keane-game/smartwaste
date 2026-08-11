import { HttpErrorResponse, HttpEvent, HttpHandlerFn, HttpRequest, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { BehaviorSubject, Observable, throwError } from 'rxjs';
import { catchError, filter, switchMap, take } from 'rxjs/operators';
import swal from 'sweetalert';
import { AuthService } from '../services/auth.service';

/**
 * Gestion des erreurs HTTP, et surtout du renouvellement transparent de l'accès.
 *
 * <p><b>Pourquoi ce n'est pas une simple déconnexion sur 401.</b> Le jeton d'accès dure quelques
 * minutes (le backend sait révoquer une session). Sans renouvellement, l'utilisateur serait éjecté
 * en permanence. Sur 401, on tente donc un `POST /auth/refresh` et on rejoue la requête <b>une
 * seule fois</b> ; si le rafraîchissement échoue, la session est réellement finie.
 *
 * <p><b>Le verrou n'est pas décoratif.</b> Un écran déclenche facilement plusieurs appels
 * simultanés ; sans lui, chacun lancerait son propre rafraîchissement. Comme le backend
 * <b>fait tourner</b> le jeton à chaque appel, le premier invaliderait celui des autres.
 *
 * <p><b>Pourquoi une fonction plutôt qu'une classe `HTTP_INTERCEPTORS`.</b> Cette classe injectait
 * `AuthService`, qui injecte `HttpClient` — cycle de DI (`NG0200`). Un intercepteur fonctionnel
 * résout ses dépendances via `inject()` au moment de la requête, après que `HttpClient` existe.
 */

let refreshing = false;
const refreshed$ = new BehaviorSubject<string | null>(null);

/**
 * Session définitivement close (rafraîchissement refusé). Sans ce drapeau, les appels périodiques
 * qui tournent en fond — sondage des véhicules du tableau de bord toutes les 30 s, reconnexion du
 * flux SSE — relançaient CHACUN un rafraîchissement condamné, et chaque échec affichait une popup
 * d'erreur : d'où les popups « à tout instant » signalées en usage réel une fois le jeton de
 * rafraîchissement périmé. On ne tente plus rien tant qu'une nouvelle authentification n'a pas eu
 * lieu, et `AuthService.login` remet le drapeau à zéro.
 */
let sessionEnded = false;

export function resetSessionEndedFlag(): void {
  sessionEnded = false;
}

export const ErrorInterceptor: HttpInterceptorFn = (request, next) => {
  const authenticationService = inject(AuthService);

  // Les appels d'authentification eux-mêmes ne sont jamais rejoués : un 401 sur
  // `/auth/authenticate`, c'est un mot de passe faux, pas un jeton périmé.
  const isAuthCall = request.url.includes('/auth/');

  return next(request).pipe(catchError((err: HttpErrorResponse) => {
    // Un échec sur `/auth/**` ne doit JAMAIS produire de popup ici : soit c'est le formulaire de
    // connexion, qui affiche lui-même son message, soit c'est un rafraîchissement/déconnexion,
    // dont l'échec est une fin de session normale — pas un incident à signaler bruyamment.
    if (isAuthCall) {
      return throwError(() => err);
    }

    if (err.status === 401 && !sessionEnded && authenticationService.getRefreshToken()) {
      return handleUnauthorized(request, next, authenticationService);
    }
    if (err.status === 401) {
      endSession(authenticationService);
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
      // Session réellement close (révoquée, expirée, ou jeton déjà rejoué) : plus rien à tenter.
      // Le backend répond 404 sur un jeton de rafraîchissement inconnu (ResourceNotFoundException)
      // et non 401 : on ne teste donc pas le code, tout échec ici clôt la session.
      refreshing = false;
      endSession(authenticationService);
      return throwError(() => refreshError);
    })
  );
}

/** Ferme la session une seule fois, sans popup — les appels en fond peuvent échouer en rafale. */
function endSession(authenticationService: AuthService): void {
  if (sessionEnded) {
    return;
  }
  sessionEnded = true;
  authenticationService.logout();
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
