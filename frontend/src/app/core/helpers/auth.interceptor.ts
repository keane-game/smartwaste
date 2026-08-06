
import { HttpInterceptorFn } from '@angular/common/http';

/**
 * Pose l'en-tête `Authorization` sur les requêtes sortantes.
 *
 * <p><b>Pourquoi la garde.</b> La version précédente clonait toujours la requête, y compris
 * quand personne n'était connecté : elle envoyait alors littéralement
 * `Authorization: Bearer undefined`, sur <i>toutes</i> les requêtes — jusqu'aux fichiers de
 * traduction chargés par `TranslateHttpLoader`. Un en-tête absent et un en-tête invalide ne
 * produisent pas la même réponse serveur.
 */
export const AuthInterceptor: HttpInterceptorFn = (req, next) => {
  const currentUser = JSON.parse(localStorage.getItem('currentUser') || '{}');
  const bearer: string | undefined = currentUser.bearer;

  if (!bearer) {
    return next(req);
  }

  return next(req.clone({
    setHeaders: {
      Authorization: `Bearer ${bearer}`
    }
  }));
};
