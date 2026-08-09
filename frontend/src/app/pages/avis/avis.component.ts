import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { ReactiveFormsModule, FormsModule, FormBuilder, Validators } from '@angular/forms';
import { environment } from '../../../environments/environment';
import { API_ENDPOINTS, API_PATHS } from '../../shared/constants/api-endpoints';
import { succesAlert, errorAlert } from '../../services/alert.service';

interface AvisMine {
  id: string;
  message: string;
  statut: 'SIGNALE' | 'EN_COURS' | 'TRAITE' | 'REJETE';
  submittedAt: string;
}

interface Quartier {
  quartierId: string;
  name: string;
}

interface MessageRecu {
  title: string;
  body: string;
  sentAt: string;
}

const STATUT_LABELS: Record<AvisMine['statut'], string> = {
  SIGNALE: 'Signalé',
  EN_COURS: 'En cours de traitement',
  TRAITE: 'Traité',
  REJETE: 'Rejeté',
};

/**
 * Espace citoyen : déposer/suivre un signalement (`/avis`), s'abonner aux passages de collecte
 * d'un quartier (`/v1/collection-subscriptions`), et consulter les messages de sensibilisation
 * reçus (`/v1/awareness/mine`).
 *
 * <p><b>Attention aux préfixes :</b> `/avis` est monté à la <b>racine du serveur</b>, hors de
 * `/v1` — comme `/auth` et `/data`. Les abonnements et la sensibilisation, eux, sont sous `/v1`.
 */
@Component({
    selector: 'app-avis',
    imports: [CommonModule, ReactiveFormsModule, FormsModule],
    templateUrl: './avis.component.html',
    styleUrls: ['./avis.component.scss']
})
export class AvisComponent implements OnInit {

  private readonly host = environment.apiUrl.replace(/\/v1\/?$/, '');
  private readonly apiUrl = environment.apiUrl;

  readonly statutLabels = STATUT_LABELS;

  loading = false;
  sent = false;
  error = '';

  form = this.fb.group({
    message: ['', [Validators.required, Validators.minLength(5), Validators.maxLength(1000)]]
  });

  mesSignalements: AvisMine[] = [];

  quartiers: Quartier[] = [];
  mesAbonnements: string[] = [];
  quartierASuivre: string | null = null;
  abonnementEnCours = false;

  messagesSensibilisation: MessageRecu[] = [];

  /** Erreur de chargement d'une des sections « espace citoyen » — distincte de `error`, propre
   * au formulaire de dépôt. Ne bloque pas les autres sections : chargement indépendant par section. */
  sectionError = '';

  constructor(private fb: FormBuilder, private http: HttpClient) { }

  ngOnInit(): void {
    this.loadMesSignalements();
    this.loadAbonnements();
    this.loadSensibilisation();
    this.http.get<Quartier[]>(`${this.apiUrl}${API_ENDPOINTS.quartiers.listPath}`)
      .subscribe({
        next: (quartiers) => { this.quartiers = quartiers; },
        error: () => { this.sectionError = "Impossible de charger la liste des quartiers."; }
      });
  }

  get remaining(): number {
    return 1000 - (this.form.value.message?.length ?? 0);
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading = true;
    this.error = '';
    // Corps réduit au seul message : le serveur impose l'auteur et le statut.
    this.http.post(`${this.host}${API_PATHS.avis}`, { message: this.form.value.message }, { responseType: 'text' })
      .subscribe({
        next: () => {
          this.loading = false;
          this.sent = true;
          this.form.reset({ message: '' });
          this.loadMesSignalements();
        },
        error: () => {
          this.loading = false;
          this.error = "Impossible d'envoyer votre avis. Réessayez plus tard.";
        }
      });
  }

  another(): void {
    this.sent = false;
  }

  private loadMesSignalements(): void {
    this.http.get<AvisMine[]>(`${this.host}${API_PATHS.avisMine}`)
      .subscribe({
        next: (avis) => { this.mesSignalements = avis; },
        error: () => { this.sectionError = "Impossible de charger vos signalements."; }
      });
  }

  statutLabel(statut: AvisMine['statut']): string {
    return this.statutLabels[statut] ?? statut;
  }

  private loadAbonnements(): void {
    this.http.get<string[]>(`${this.apiUrl}${API_PATHS.collectionSubscriptions}`)
      .subscribe({
        next: (quartierIds) => { this.mesAbonnements = quartierIds; },
        error: () => { this.sectionError = "Impossible de charger vos abonnements."; }
      });
  }

  quartierName(quartierId: string): string {
    return this.quartiers.find(q => q.quartierId === quartierId)?.name ?? quartierId;
  }

  /** Quartiers pas encore suivis — inutile de proposer de se réabonner à un quartier déjà suivi. */
  get quartiersDisponibles(): Quartier[] {
    return this.quartiers.filter(q => !this.mesAbonnements.includes(q.quartierId));
  }

  sabonner(): void {
    if (!this.quartierASuivre) {
      return;
    }
    this.abonnementEnCours = true;
    this.http.post(`${this.apiUrl}${API_PATHS.collectionSubscriptions}`, { quartierId: this.quartierASuivre })
      .subscribe({
        next: () => {
          this.abonnementEnCours = false;
          this.quartierASuivre = null;
          succesAlert('Abonnement enregistré.');
          this.loadAbonnements();
        },
        error: () => {
          this.abonnementEnCours = false;
          errorAlert("Impossible de vous abonner à ce quartier. Réessayez plus tard.");
        }
      });
  }

  seDesabonner(quartierId: string): void {
    this.http.delete(`${this.apiUrl}${API_PATHS.unsubscribeFromQuartier(quartierId)}`)
      .subscribe({
        next: () => { succesAlert('Désabonnement effectué.'); this.loadAbonnements(); },
        error: () => errorAlert('Impossible de vous désabonner. Réessayez plus tard.')
      });
  }

  private loadSensibilisation(): void {
    this.http.get<MessageRecu[]>(`${this.apiUrl}${API_PATHS.awarenessMine}`)
      .subscribe({
        next: (messages) => { this.messagesSensibilisation = messages; },
        error: () => { this.sectionError = "Impossible de charger les messages de sensibilisation."; }
      });
  }
}
