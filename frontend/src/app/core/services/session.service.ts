import { Injectable, computed, signal } from '@angular/core';
import { JwtHelperService } from '@auth0/angular-jwt';

import { AuthService } from './auth.service';

/**
 * Utilisateur de la session courante, tel que porté par le JWT.
 *
 * Le claim `role` est une CHAINE UNIQUE (`"ROLE_SUPER_ADMIN"`), pas un tableau — vérifié
 * directement sur un jeton réel émis par POST /auth/authenticate. Le code précédent
 * (layout.component.ts, login.component.ts) le traitait tantôt comme un tableau d'objets
 * ({@code role[0].authority}), tantôt comme un tableau de chaines ({@code roles.some(...)}) :
 * les deux échouaient silencieusement à l'exécution (une chaine n'a pas de méthode `.some`, et
 * `role[0]` sur une chaine renvoie son premier caractère, pas un rôle). Conséquence réelle,
 * confirmée en lisant le code : le flux SSE d'alertes ne s'est jamais connecté pour personne,
 * y compris les comptes SUPER_ADMIN — `isAdmin()` levait une exception silencieusement rattrapée
 * et renvoyait toujours `false`.
 */
export interface SessionUser {
  email: string;
  firstname: string;
  lastname: string;
  /** Avec le prefixe ROLE_, tel qu'emis par le serveur (UserEntity.getAuthorities()). */
  role: string;
  sessionId: string | null;
}

@Injectable({ providedIn: 'root' })
export class SessionService {

  private readonly jwtHelper = new JwtHelperService();
  private readonly userSignal = signal<SessionUser | null>(null);

  readonly user = this.userSignal.asReadonly();
  readonly isAuthenticated = computed(() => this.userSignal() !== null);
  readonly role = computed(() => this.userSignal()?.role ?? null);

  constructor(private authService: AuthService) {
    this.refresh();
    // Se resynchronise a chaque connexion/deconnexion/rafraichissement de jeton : AuthService
    // est la seule source qui ecrit dans localStorage, la session ne doit jamais la dupliquer.
    this.authService.currentUser.subscribe(() => this.refresh());
  }

  /** Redecode le jeton courant. A appeler apres toute operation qui pourrait l'avoir change. */
  refresh(): void {
    this.userSignal.set(this.decode());
  }

  /**
   * Vrai si l'utilisateur courant porte l'un de ces roles — avec ou sans le prefixe ROLE_,
   * pour ne pas obliger chaque appelant a connaitre cette convention serveur.
   */
  hasAnyRole(...roles: string[]): boolean {
    const current = this.role();
    if (!current) {
      return false;
    }
    return roles.some(r => current === r || current === `ROLE_${r}`);
  }

  private decode(): SessionUser | null {
    const token = this.authService.getAuthToken();
    if (!token) {
      return null;
    }
    try {
      if (this.jwtHelper.isTokenExpired(token)) {
        return null;
      }
      const decoded = this.jwtHelper.decodeToken(token);
      if (!decoded?.role || !decoded?.sub) {
        return null;
      }
      return {
        email: decoded.sub,
        firstname: decoded.firstname ?? '',
        lastname: decoded.lastname ?? '',
        role: decoded.role,
        sessionId: decoded.sid ?? null,
      };
    } catch {
      return null;
    }
  }
}
