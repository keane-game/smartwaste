import { Component, ChangeDetectorRef, ElementRef, EventEmitter, AfterContentChecked, HostListener, OnInit, Output, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { headerTitleService } from '../../../services/headerTitle.service';
import { SessionService } from '../../../core/services/session.service';
import { OrganizationScopeService } from '../../../core/services/organization-scope.service';

@Component({
    selector: 'app-header',
    standalone: true,
    imports: [RouterLink],
    templateUrl: './header.component.html',
    styleUrls: ['./header.component.scss'],
})
export class HeaderComponent implements AfterContentChecked, OnInit {

  private readonly scopeService = inject(OrganizationScopeService);
  private readonly router = inject(Router);
  private readonly host = inject(ElementRef<HTMLElement>);

  /** Sélecteur de périmètre — affiché pour le seul SUPER_ADMIN (voir OrganizationScopeService). */
  readonly canSelectScope = this.scopeService.canSelectScope;
  readonly organizations = this.scopeService.organizations;
  readonly selectedScopeId = this.scopeService.selectedId;
  readonly scopeLabel = this.scopeService.label;
  scopeMenuOpen = false;

  ngOnInit(): void {
    this.scopeService.load();
  }

  toggleScopeMenu(): void {
    this.scopeMenuOpen = !this.scopeMenuOpen;
  }

  /**
   * Referme le menu sur un clic hors de l'en-tête.
   *
   * <p>Sans cela, le seul moyen de le fermer serait de choisir un périmètre — c'est-à-dire qu'on
   * ne pourrait pas l'ouvrir puis renoncer.
   */
  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    if (this.scopeMenuOpen && !this.host.nativeElement.contains(event.target as Node)) {
      this.scopeMenuOpen = false;
    }
  }

  @HostListener('document:keydown.escape')
  onEscape(): void {
    this.scopeMenuOpen = false;
  }

  /**
   * Change le périmètre observé et recharge l'écran courant.
   *
   * <p>Le rechargement n'est pas un raccourci : les listes chargent leurs données dans
   * `ngOnInit`, aucune ne réagit à un changement de périmètre. Sans re-création du composant,
   * l'utilisateur verrait le sélecteur annoncer une collectivité et le tableau afficher celle
   * d'avant. Le passage par une URL intermédiaire force Angular à reconstruire la route au lieu
   * de la réutiliser.
   */
  selectScope(organizationId: string | null): void {
    this.scopeMenuOpen = false;
    if (organizationId === this.selectedScopeId()) {
      return;
    }
    this.scopeService.select(organizationId);
    const current = this.router.url;
    this.router.navigateByUrl('/', { skipLocationChange: true })
      .then(() => this.router.navigateByUrl(current));
  }


  pageTitle = 'Tableau de bord';

  /** Utilisateur réel de la session — l'en-tête affichait « Admingh / Admin » en dur. */
  readonly user = this.sessionService.user;

  @Output() childEvent = new EventEmitter<any>();

  constructor(
    private headerTitleService: headerTitleService,
    private changeDetector: ChangeDetectorRef,
    private sessionService: SessionService,
  ) { }

  triggerParentFunction(): void {
    this.childEvent.emit();
  }

  /** Nom affiché : prénom + nom du jeton, à défaut l'e-mail. */
  get displayName(): string {
    const u = this.user();
    if (!u) { return ''; }
    const full = `${u.firstname} ${u.lastname}`.trim();
    return full || u.email;
  }

  /** Rôle sans le préfixe `ROLE_` que porte le jeton. */
  get displayRole(): string {
    return (this.user()?.role ?? '').replace(/^ROLE_/, '');
  }

  /** Initiales de l'avatar — l'API ne fournit pas de photo de profil. */
  get initials(): string {
    const u = this.user();
    if (!u) { return '?'; }
    return ((u.firstname || u.email || '?').charAt(0) + (u.lastname || '').charAt(0)).toUpperCase();
  }

  ngAfterContentChecked(): void {
    this.changeDetector.detectChanges();
  }

  ngAfterViewInit(): void {
    Promise.resolve().then(() => {
      this.headerTitleService.title.subscribe(updatedTitle => {
        this.pageTitle = updatedTitle;
      });
    });
  }
}
