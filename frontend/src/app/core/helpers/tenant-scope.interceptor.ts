import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { environment } from '../../../environments/environment';
import { OrganizationScopeService } from '../services/organization-scope.service';

/**
 * Pose l'en-tête de périmètre `X-Organization-Id` quand un SUPER_ADMIN observe une collectivité.
 *
 * <p>Rien n'est posé en vue plateforme (aucun périmètre choisi) ni pour les autres rôles : le
 * serveur ignore l'en-tête hors SUPER_ADMIN, et l'envoyer quand même donnerait l'illusion d'un
 * choix qui n'en est pas un.
 *
 * <p><b>Restreint aux appels d'API.</b> Le fichier de traduction et les tuiles de carte passent par
 * le même {@code HttpClient} ; leur ajouter un en-tête applicatif provoquerait une requête
 * préalable CORS vers un tiers, pour rien. Même précaution que la garde d'{@code AuthInterceptor},
 * qui envoyait auparavant `Bearer undefined` jusque sur les traductions.
 */
export const TenantScopeInterceptor: HttpInterceptorFn = (req, next) => {
  if (!req.url.startsWith(environment.apiUrl)) {
    return next(req);
  }

  const scope = inject(OrganizationScopeService).scopeHeader();
  if (!scope) {
    return next(req);
  }

  return next(req.clone({
    setHeaders: { [OrganizationScopeService.SCOPE_HEADER]: scope },
  }));
};
