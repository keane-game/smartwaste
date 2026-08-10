import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { AuthService } from '../services/auth.service';

/**
 * Garde fonctionnelle (Angular 20) : remplace `core/helpers/auth.guard.ts` (classe), qui ne
 * verifiait deja que la connexion — comportement conserve a l'identique ici, seule la forme
 * change pour s'aligner sur le style standalone du reste de la refonte.
 */
export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isLoggedIn()) {
    return true;
  }

  authService.redirectUrl = state.url;
  router.navigate(['/login'], { queryParams: { returnUrl: state.url } });
  return false;
};
