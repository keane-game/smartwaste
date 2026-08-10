import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { SessionService } from '../services/session.service';

/**
 * Garde de role, pilotee par la config de route plutot que par une garde dediee par role :
 * `{ path: 'users', canActivate: [roleGuard], data: { roles: ['ADMIN', 'SUPER_ADMIN'] } }`.
 *
 * Ferme un vrai trou trouve en Phase 0 de l'audit : `AuthGuard` ne verifiait que la connexion,
 * jamais le role — un AGENT ou un USER pouvait naviguer vers n'importe quel ecran d'administration
 * (l'appel API echouait ensuite en 403, mais l'ecran restait accessible et rendait une page cassee
 * plutot que de ne jamais s'afficher).
 *
 * Redirige vers `/` plutot que `/login` : contrairement a `authGuard`, l'utilisateur EST
 * authentifie, il n'a simplement pas le role attendu — le renvoyer au login serait faux et
 * boucherait la session en cours pour rien.
 */
export const roleGuard: CanActivateFn = (route) => {
  const sessionService = inject(SessionService);
  const router = inject(Router);

  const allowedRoles = (route.data?.['roles'] as string[] | undefined) ?? [];
  if (allowedRoles.length === 0 || sessionService.hasAnyRole(...allowedRoles)) {
    return true;
  }

  router.navigate(['/']);
  return false;
};
