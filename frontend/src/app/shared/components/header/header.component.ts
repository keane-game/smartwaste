import { Component, ChangeDetectorRef, EventEmitter, AfterContentChecked, Output } from '@angular/core';
import { RouterLink } from '@angular/router';
import { headerTitleService } from '../../../services/headerTitle.service';
import { SessionService } from '../../../core/services/session.service';

@Component({
    selector: 'app-header',
    standalone: true,
    imports: [RouterLink],
    templateUrl: './header.component.html',
    styleUrls: ['./header.component.scss'],
})
export class HeaderComponent implements AfterContentChecked {

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
