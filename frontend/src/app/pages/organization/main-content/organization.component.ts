import { Component, OnInit, inject } from '@angular/core';
import { HttpClient, HttpContext, HttpErrorResponse } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { headerTitleService } from '../../../services/headerTitle.service';
import { API_ENDPOINTS } from '../../../shared/constants/api-endpoints';
import { errorAlert, succesAlert } from '../../../services/alert.service';
import { HANDLES_OWN_ERRORS } from '../../../core/helpers/error.inerceptor';
import { OrganizationScopeService } from '../../../core/services/organization-scope.service';

interface Organization {
  organizationId: string;
  name: string;
  code: string;
  status: 'ACTIVE' | 'SUSPENDED';
}

interface Membership {
  membershipId: string;
  userId: string;
  organizationId: string;
}

interface UserSummary {
  userId: string;
  userFirstname: string;
  userLastname: string;
  userEmail: string;
  authority?: { name: string };
}

/**
 * Gestion des collectivités (`/v1/organizations/**`, permission `MANAGE_ORGANIZATIONS`).
 *
 * <p>La collectivité est l'unité de cloisonnement du produit (ADR-0020) : un ADMIN ne voit que la
 * sienne, un SUPER_ADMIN voit tout et peut observer l'une d'elles via le sélecteur d'en-tête.
 * L'API existait depuis le 2026-08-10 sans aucune interface — les collectivités et leurs
 * rattachements ne se créaient qu'en appelant le serveur à la main.
 *
 * <p><b>Une collectivité ne se supprime pas, elle se suspend.</b> Le serveur n'expose aucun
 * endpoint de suppression, et c'est délibéré : ses dépotoirs, circuits, alertes et comptes portent
 * son identifiant. La faire disparaître laisserait des données orphelines qu'aucun périmètre ne
 * pourrait plus atteindre. L'écran propose donc « Suspendre » / « Réactiver », jamais « Supprimer ».
 *
 * <p><b>Le code est immuable après création</b> (contrainte serveur) : le champ est désactivé en
 * modification plutôt que masqué, pour que la règle se voie au lieu de se deviner.
 */
@Component({
    selector: 'app-organization',
    templateUrl: './organization.component.html',
    styleUrls: ['./organization.component.scss'],
    standalone: false
})
export class OrganizationComponent implements OnInit {

  private readonly http = inject(HttpClient);
  private readonly headerTitle = inject(headerTitleService);
  private readonly scopeService = inject(OrganizationScopeService);
  private readonly baseUrl = environment.apiUrl;

  /**
   * Les appels dont cet écran explique lui-même l'échec — sans ce marqueur, `ErrorInterceptor`
   * affiche « Une erreur est survenue » à la place du message utile (code déjà pris, compte déjà
   * rattaché ailleurs).
   */
  private readonly ownErrors = { context: new HttpContext().set(HANDLES_OWN_ERRORS, true) };

  organizations: Organization[] = [];
  loading = false;

  /** Collectivité dont on consulte les membres. `null` = aucun panneau ouvert. */
  selected: Organization | null = null;
  members: UserSummary[] = [];
  loadingMembers = false;

  /** Comptes rattachables : tous ceux qui ne sont pas déjà membres de la collectivité ouverte. */
  allUsers: UserSummary[] = [];
  userToAttach: string | null = null;
  attaching = false;

  form: { organizationId: string | null; name: string; code: string } | null = null;
  saving = false;

  ngOnInit(): void {
    this.headerTitle.setTitle('Collectivités');
    this.load();
    this.http.get<UserSummary[]>(`${this.baseUrl}${API_ENDPOINTS.users.listPath}`)
      .subscribe({ next: users => this.allUsers = users ?? [] });
  }

  load(): void {
    this.loading = true;
    this.http.get<Organization[]>(`${this.baseUrl}${API_ENDPOINTS.organizations.listPath}`).subscribe({
      next: orgs => { this.organizations = orgs ?? []; this.loading = false; },
      error: () => { this.loading = false; errorAlert('Impossible de charger les collectivités.'); },
    });
  }

  get activeCount(): number {
    return this.organizations.filter(o => o.status === 'ACTIVE').length;
  }

  // ---------------------------------------------------------------- Création / modification

  openCreate(): void {
    this.form = { organizationId: null, name: '', code: '' };
  }

  openEdit(org: Organization): void {
    this.form = { organizationId: org.organizationId, name: org.name, code: org.code };
  }

  closeForm(): void {
    this.form = null;
  }

  get isEdit(): boolean {
    return this.form?.organizationId != null;
  }

  save(): void {
    if (!this.form || !this.form.name.trim() || (!this.isEdit && !this.form.code.trim())) {
      return;
    }
    this.saving = true;

    // En modification, seul le nom est transmis : le code est immuable côté serveur et le statut
    // se pilote par les actions dédiées — l'envoyer ici le remettrait silencieusement à ACTIVE.
    const done = () => {
      this.saving = false;
      this.closeForm();
      this.load();
      // Le sélecteur d'en-tête tient sa propre liste : sans cela, une collectivité créée ou
      // renommée n'y apparaîtrait qu'au prochain rechargement de page.
      this.scopeService.reload();
    };
    const fail = (err: HttpErrorResponse) => {
      this.saving = false;
      errorAlert(err.status === 409
        ? 'Ce code est déjà utilisé par une autre collectivité.'
        : "Échec de l'enregistrement.");
    };

    if (this.isEdit) {
      this.http.put(`${this.baseUrl}${API_ENDPOINTS.organizations.basePath}/${this.form.organizationId}`,
        { name: this.form.name.trim() }, this.ownErrors)
        .subscribe({ next: () => { succesAlert('Collectivité mise à jour'); done(); }, error: fail });
    } else {
      this.http.post(`${this.baseUrl}${API_ENDPOINTS.organizations.basePath}`,
        { name: this.form.name.trim(), code: this.form.code.trim().toUpperCase() }, this.ownErrors)
        .subscribe({ next: () => { succesAlert('Collectivité créée'); done(); }, error: fail });
    }
  }

  // ---------------------------------------------------------------- Suspension / réactivation

  setStatus(org: Organization, status: 'ACTIVE' | 'SUSPENDED'): void {
    const verbe = status === 'SUSPENDED' ? 'Suspendre' : 'Réactiver';
    if (!confirm(`${verbe} « ${org.name} » ?`)) {
      return;
    }
    this.http.put(`${this.baseUrl}${API_ENDPOINTS.organizations.basePath}/${org.organizationId}`, { status })
      .subscribe({
        next: () => {
          succesAlert(status === 'SUSPENDED' ? 'Collectivité suspendue' : 'Collectivité réactivée');
          this.load();
          this.scopeService.reload();
        },
        error: () => errorAlert("Échec du changement de statut."),
      });
  }

  // ---------------------------------------------------------------- Membres

  openMembers(org: Organization): void {
    this.selected = org;
    this.userToAttach = null;
    this.loadMembers();
  }

  closeMembers(): void {
    this.selected = null;
    this.members = [];
  }

  /**
   * Les membres sont rendus comme des rattachements (`userId` nu) : le contexte `tenant` ne peut
   * pas lire les comptes d'`identity` autrement que par identifiant (ADR-0012). Les noms sont donc
   * résolus ici, à partir de la liste des utilisateurs déjà chargée — sinon l'écran afficherait une
   * colonne d'UUID, inexploitable pour décider qui détacher.
   */
  private loadMembers(): void {
    if (!this.selected) { return; }
    this.loadingMembers = true;
    this.http.get<Membership[]>(
      `${this.baseUrl}${API_ENDPOINTS.organizations.basePath}/${this.selected.organizationId}/members`)
      .subscribe({
        next: memberships => {
          const ids = new Set((memberships ?? []).map(m => m.userId));
          this.members = this.allUsers.filter(u => ids.has(u.userId));
          this.loadingMembers = false;
        },
        error: () => { this.loadingMembers = false; errorAlert('Impossible de charger les membres.'); },
      });
  }

  /** Comptes proposables au rattachement : ceux qui ne sont pas déjà membres de cette collectivité. */
  get attachableUsers(): UserSummary[] {
    const memberIds = new Set(this.members.map(m => m.userId));
    return this.allUsers.filter(u => !memberIds.has(u.userId));
  }

  attach(): void {
    if (!this.selected || !this.userToAttach) { return; }
    this.attaching = true;
    this.http.post(
      `${this.baseUrl}${API_ENDPOINTS.organizations.basePath}/${this.selected.organizationId}/members`,
      { userId: this.userToAttach }, this.ownErrors)
      .subscribe({
        next: () => { this.attaching = false; this.userToAttach = null; this.loadMembers(); },
        error: (err: HttpErrorResponse) => {
          this.attaching = false;
          // 409 = déjà rattaché ailleurs. Une personne n'appartient qu'à UNE collectivité, et tout
          // compte créé est inscrit d'office à Pikine : le rattacher ailleurs suppose donc de l'en
          // détacher d'abord. Le dire, plutôt que « échec ».
          errorAlert(err.status === 409
            ? 'Ce compte est déjà rattaché à une collectivité. Détachez-le de celle-ci avant.'
            : 'Échec du rattachement.');
        },
      });
  }

  detach(user: UserSummary): void {
    if (!this.selected) { return; }
    if (!confirm(`Détacher ${this.fullName(user)} de « ${this.selected.name} » ?`)) {
      return;
    }
    this.http.delete(
      `${this.baseUrl}${API_ENDPOINTS.organizations.basePath}/${this.selected.organizationId}/members/${user.userId}`)
      .subscribe({
        next: () => this.loadMembers(),
        error: () => errorAlert('Échec du détachement.'),
      });
  }

  fullName(user: UserSummary): string {
    return `${user.userFirstname ?? ''} ${user.userLastname ?? ''}`.trim() || user.userEmail;
  }
}
