import { Component, ElementRef, OnDestroy, OnInit, Renderer2 } from '@angular/core';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';
import { AlertStreamService } from '../../services/alert-stream.service';
declare var $: any;

/** Une alerte poussée par le flux temps réel, telle qu'affichée dans la cloche. */
interface HeaderAlert {
  object: string;
  message: string;
  code: string;
  at: Date;
}

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent implements OnInit, OnDestroy {

  /** Identité de l'utilisateur connecté, décodée du JWT (claims firstname/lastname/role/sub). */
  fullName = 'Utilisateur';
  role = '';
  initials = 'U';

  /** File des dernières alertes reçues via `GET /v1/alerts/stream` (les plus récentes en tête). */
  alerts: HeaderAlert[] = [];
  private sub?: Subscription;

  constructor(
    private elRef: ElementRef,
    private renderer: Renderer2,
    private authService: AuthService,
    private alertStream: AlertStreamService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.resolveIdentity();

    this.alertStream.connect();
    this.sub = this.alertStream.alerts.subscribe(a => {
      if (!a) { return; }
      this.alerts = [{
        object: a.object || a.objet || 'Alerte',
        message: a.message || '',
        code: (a.code || 'INFO').toUpperCase(),
        at: new Date()
      }, ...this.alerts].slice(0, 8);
    });
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
    this.alertStream.disconnect();
  }

  /** Déconnexion : purge le jeton puis retour à l'écran de login. */
  logout(event: Event): void {
    event.preventDefault();
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  /** Classe d'icône Bootstrap selon la sévérité de l'alerte. */
  iconFor(code: string): string {
    switch (code) {
      case 'DANGER':  return 'bi-x-circle text-danger';
      case 'WARNING': return 'bi-exclamation-circle text-warning';
      default:        return 'bi-info-circle text-primary';
    }
  }

  /** Décode les claims du JWT stocké pour afficher le vrai nom / rôle de l'utilisateur. */
  private resolveIdentity(): void {
    const claims = this.decodeToken(this.authService.getAuthToken());
    if (!claims) { return; }
    const first = claims.firstname || '';
    const last = claims.lastname || '';
    const name = `${first} ${last}`.trim();
    this.fullName = name || claims.sub || 'Utilisateur';
    this.role = claims.role || '';
    this.initials = ((first[0] || this.fullName[0] || 'U') + (last[0] || '')).toUpperCase();
  }

  /** Décode la charge utile (payload) base64url d'un JWT sans dépendance externe. */
  private decodeToken(token: string | undefined): any {
    if (!token || token.split('.').length !== 3) { return null; }
    try {
      const payload = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
      return JSON.parse(decodeURIComponent(escape(atob(payload))));
    } catch {
      return null;
    }
  }

  jquery(event: any): void {
    const el = this.elRef.nativeElement.querySelector('#navigation');
    this.renderer.addClass(el, 'active');
    $('#sidebarCollapse').on('click', () => {
      $('#sidebar').toggleClass('active');
    });
  }
}
