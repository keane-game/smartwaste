import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { first } from 'rxjs';

import { AuthService } from '../services/auth.service';
import { SessionService } from '../services/session.service';
import { errorAlert } from '../../services/alert.service';

/**
 * Connexion — reconstruite en Phase 2 de la refonte (docs/FRONTEND_UI_AUDIT.md).
 *
 * <p>Corrige un bug réel de l'ancienne version : elle traitait le claim JWT `role` comme un
 * tableau d'objets ({@code decodedToken.role[0].authority}), alors que le serveur émet une
 * chaîne unique ({@code "ROLE_SUPER_ADMIN"}) — vérifié sur un jeton réel. `role[0]` sur une
 * chaîne renvoie son premier caractère, pas un rôle : le `switch` qui suivait ne correspondait
 * donc jamais, et les trois branches (`ROLE_ADMIN`/`ROLE_USER`/`default`) faisaient de toute
 * façon la même navigation — du code mort qui donnait l'illusion d'un aiguillage par rôle.
 * `SessionService` décode maintenant le jeton une seule fois, correctement, pour toute l'app.
 */
@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'],
})
export class LoginComponent {

  readonly loading = signal(false);
  readonly showPassword = signal(false);

  form = this.fb.group({
    username: ['', [Validators.required, Validators.email]],
    password: ['', Validators.required],
  });

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private sessionService: SessionService,
    private router: Router,
    private route: ActivatedRoute,
  ) {}

  togglePasswordVisibility(): void {
    this.showPassword.update(v => !v);
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading.set(true);

    this.authService.login(this.form.value)
      .pipe(first())
      .subscribe({
        next: () => {
          this.loading.set(false);
          // AuthService.login() a deja ecrit le jeton en localStorage et notifie
          // currentUser ; SessionService s'y est abonne, mais on force une lecture
          // synchrone avant de decider ou naviguer, pour ne pas dependre de l'ordre
          // d'execution des abonnements RxJS.
          this.sessionService.refresh();
          const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl') ?? this.authService.redirectUrl;
          this.router.navigateByUrl(returnUrl || '/');
        },
        error: (error) => {
          this.loading.set(false);
          const message = error?.status === 401
            ? 'Identifiants incorrects.'
            : "Impossible de se connecter pour le moment. Réessayez plus tard.";
          errorAlert(message);
        },
      });
  }
}
