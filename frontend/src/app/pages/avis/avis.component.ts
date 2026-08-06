import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { environment } from '../../../environments/environment';
import { API_PATHS } from '../../shared/constants/api-endpoints';

/**
 * Formulaire « Donner mon avis » — `POST /avis`.
 *
 * <p>Le backend fixe lui-même l'auteur (utilisateur authentifié) et le statut ; le client
 * n'envoie que le message. La réponse est du texte, pas du JSON — d'où `responseType: 'text'`,
 * sans quoi Angular échoue au parsing sur une réponse pourtant réussie.
 *
 * <p><b>Attention au préfixe :</b> `/avis` est monté à la <b>racine du serveur</b>, hors de
 * `/v1`. C'est la seule ressource métier dans ce cas avec `/auth` et `/data`.
 */
@Component({
  selector: 'app-avis',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './avis.component.html',
  styleUrls: ['./avis.component.scss']
})
export class AvisComponent {

  private readonly host = environment.apiUrl.replace(/\/v1\/?$/, '');

  loading = false;
  sent = false;
  error = '';

  form = this.fb.group({
    message: ['', [Validators.required, Validators.minLength(5), Validators.maxLength(1000)]]
  });

  constructor(private fb: FormBuilder, private http: HttpClient) { }

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
}
