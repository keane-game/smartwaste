import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { SessionService } from '../../core/services/session.service';
import { headerTitleService } from '../../services/headerTitle.service';

/**
 * Mon profil.
 *
 * <p>Cet écran était un gabarit statique livré avec le thème : il affichait « Kevin Anderson »,
 * « Web Designer », une adresse à New York, un numéro de téléphone et des liens Twitter/Facebook —
 * des données entièrement inventées, présentées comme celles du compte connecté, et une image
 * (`assets/img/profile-img.jpg`) absente du dépôt donc en 404. Aucun champ n'était relié à quoi
 * que ce soit, et les trois onglets « Edit Profile » / « Settings » / « Change Password »
 * n'enregistraient rien.
 *
 * <p>Remplacé par ce que l'application connaît réellement de l'utilisateur : les claims du jeton
 * (nom, e-mail, rôle, identifiant de session). Le changement de mot de passe renvoie vers le
 * parcours existant `/password`, seul mécanisme réellement implémenté.
 */
@Component({
    selector: 'app-profil',
    standalone: true,
    imports: [CommonModule, RouterLink],
    templateUrl: './profil.component.html',
    styleUrl: './profil.component.scss'
})
export class ProfilComponent {

  readonly user = this.sessionService.user;

  constructor(
    private sessionService: SessionService,
    private headerTitleService: headerTitleService,
  ) {
    this.headerTitleService.setTitle('Mon profil');
  }

  get displayName(): string {
    const u = this.user();
    if (!u) { return ''; }
    return `${u.firstname} ${u.lastname}`.trim() || u.email;
  }

  get displayRole(): string {
    return (this.user()?.role ?? '').replace(/^ROLE_/, '');
  }

  /** Initiales pour l'avatar — évite de dépendre d'une image de profil, que l'API ne fournit pas. */
  get initials(): string {
    const u = this.user();
    if (!u) { return '?'; }
    const first = (u.firstname || u.email || '?').charAt(0);
    const last = (u.lastname || '').charAt(0);
    return (first + last).toUpperCase();
  }
}
