import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { JwtHelperService } from '@auth0/angular-jwt';

import { AuthService } from '../services/auth.service';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {
  hide = true;
  form: any = {};
  isLoggedIn = false;
  isLoginFailed = false;
  error!: { 'errorTitle': any; 'errorDesc': any; };




  constructor(private authService: AuthService, private router: Router) {

  }

  ngOnInit(): void {
    // console.log(this.authService.getAuthToken());

  }

  onSubmit(): any {
    this.isLoggedIn = true;
    this.isLoginFailed = false;

    this.authService.login(this.form)
      .subscribe(
        userData => {
          // tslint:disable-next-line: no-string-literal
          if (!userData || !userData['token']) {
            this.isLoggedIn = false;
            this.isLoginFailed = true;
            return;
          }
          // Le tableau de bord est le point d'entrée commun après connexion.
          // (Le JWT SONAGED porte un claim « role » unique et informatif ; l'autorisation
          // effective est gérée côté backend via les authorities de l'utilisateur.)
          this.isLoginFailed = false;
          this.router.navigate(['/dashboard']);
        },
        error => {
          this.isLoggedIn = false;
          this.isLoginFailed = true;
          console.log(error);
        });
  }

}
