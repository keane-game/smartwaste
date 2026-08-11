import { Component, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';

import { AuthService } from '../../../core/services/auth.service';
import { SessionService } from '../../../core/services/session.service';

export interface NavItem {
  label: string;
  icon: string;
  route: string;
  /** Sans role liste = visible par tout compte authentifie (ex. Profil). */
  roles?: string[];
  children?: NavItem[];
}

/**
 * Trois profils reels (mémoire du produit, section 11) : ADMIN-like (SUPER_ADMIN/ADMIN, plus
 * SUPERVISEUR/TECHNICIEN_IOT qui partagent l'essentiel de la meme surface), AGENT (terrain),
 * USER (citoyen). Corrige le vrai trou de la Phase 0 de l'audit : `sidebar.component.html`
 * affichait EXACTEMENT le meme menu a tout le monde, y compris les ecrans d'administration a
 * un compte AGENT ou USER — l'appel API echouait ensuite en 403, mais l'entree de menu restait
 * visible et menait a un ecran casse plutot que de ne jamais s'afficher.
 *
 * Ne reference que des routes qui existent reellement aujourd'hui (voir
 * docs/FRONTEND_UI_AUDIT.md) : pas de lien vers un ecran qui n'a pas encore ete construit.
 */
const ADMIN_ROLES = ['SUPER_ADMIN', 'ADMIN', 'SUPERVISEUR', 'TECHNICIEN_IOT'];

const NAV_ITEMS: NavItem[] = [
  { label: 'Tableau de bord', icon: 'bi-grid', route: '/', roles: ADMIN_ROLES },
  { label: 'Département Pikine', icon: 'bi-building', route: '/departements', roles: ADMIN_ROLES },
  {
    label: 'Gestion des ressources', icon: 'bi-folder2-open', route: '', roles: ADMIN_ROLES,
    children: [
      { label: 'Utilisateurs', icon: 'bi-circle', route: '/users', roles: ADMIN_ROLES },
      { label: 'Communes', icon: 'bi-circle', route: '/communes', roles: ADMIN_ROLES },
      { label: 'Quartiers', icon: 'bi-circle', route: '/quartiers', roles: ADMIN_ROLES },
      { label: 'Dépotoirs', icon: 'bi-circle', route: '/depotoirs', roles: ADMIN_ROLES },
      { label: 'Circuits de balayage', icon: 'bi-circle', route: '/circuit-balayages', roles: ADMIN_ROLES },
      { label: 'Circuits de collecte', icon: 'bi-circle', route: '/circuit-collects', roles: ADMIN_ROLES },
      { label: 'Mobiliers urbains', icon: 'bi-circle', route: '/moblier-urbains', roles: ADMIN_ROLES },
      { label: 'Rapports', icon: 'bi-circle', route: '/reports', roles: ADMIN_ROLES },
      { label: 'Flotte', icon: 'bi-circle', route: '/vehicles', roles: ADMIN_ROLES },
      { label: 'Capteurs & traceurs', icon: 'bi-circle', route: '/devices', roles: ['SUPER_ADMIN', 'ADMIN', 'TECHNICIEN_IOT'] },
    ],
  },
  { label: 'Carte', icon: 'bi-map', route: '/maps', roles: ADMIN_ROLES },
  { label: 'Alertes', icon: 'bi-exclamation-triangle', route: '/alerts', roles: ADMIN_ROLES },
  { label: 'Notifications', icon: 'bi-bell', route: '/notifications', roles: ADMIN_ROLES },

  { label: 'Ma tournée', icon: 'bi-signpost-2', route: '/collection-routes', roles: [...ADMIN_ROLES, 'AGENT'] },

  { label: 'Espace citoyen', icon: 'bi-megaphone', route: '/avis', roles: ['USER'] },

  { label: 'Profil', icon: 'bi-person', route: '/profil' },
];

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.scss'],
})
export class SidebarComponent {

  constructor(
    private authService: AuthService,
    private sessionService: SessionService,
  ) {}

  readonly items = computed<NavItem[]>(() => this.filterByRole(NAV_ITEMS));

  logout(): void {
    this.authService.logout();
  }

  private filterByRole(items: NavItem[]): NavItem[] {
    return items
      .filter(item => !item.roles || this.sessionService.hasAnyRole(...item.roles))
      .map(item => item.children ? { ...item, children: this.filterByRole(item.children) } : item)
      .filter(item => !item.children || item.children.length > 0);
  }
}
