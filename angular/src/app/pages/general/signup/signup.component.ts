import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';

/**
 * Inscription en deux étapes, adossée au backend SONAGED :
 *  1. `POST /auth/register` avec les informations du compte ;
 *  2. `POST /auth/activation` avec le code reçu par e-mail.
 * L'utilisateur activé est ensuite redirigé vers l'écran de connexion.
 */
@Component({
  selector: 'app-signup',
  templateUrl: './signup.component.html',
  styleUrl: './signup.component.css'
})
export class SignupComponent {

  /** Étape courante : `account` (formulaire) ou `activation` (saisie du code). */
  step: 'account' | 'activation' = 'account';

  loading = false;
  error = '';
  registeredEmail = '';

  form: FormGroup = this.fb.group({
    userFirstname: ['', Validators.required],
    userLastname: ['', Validators.required],
    userEmail: ['', [Validators.required, Validators.email]],
    userPhone: [''],
    userAddress: [''],
    userPassword: ['', [Validators.required, Validators.minLength(6)]]
  });

  activationForm: FormGroup = this.fb.group({
    code: ['', Validators.required]
  });

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) { }

  submitAccount(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading = true;
    this.error = '';
    this.authService.register(this.form.value).subscribe({
      next: () => {
        this.loading = false;
        this.registeredEmail = this.form.value.userEmail;
        this.step = 'activation';
      },
      error: () => {
        this.loading = false;
        this.error = "Échec de l'inscription. Vérifiez les informations et réessayez.";
      }
    });
  }

  submitActivation(): void {
    if (this.activationForm.invalid) {
      this.activationForm.markAllAsTouched();
      return;
    }
    this.loading = true;
    this.error = '';
    this.authService.activate(this.activationForm.value.code).subscribe({
      next: () => {
        this.loading = false;
        this.router.navigate(['/login']);
      },
      error: () => {
        this.loading = false;
        this.error = 'Code invalide ou expiré.';
      }
    });
  }
}
