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
    // console.log(this.form);

    this.authService.login(this.form)
      .subscribe(
        userData => {

          const helper = new JwtHelperService();

          // tslint:disable-next-line: no-string-literal
          const decodedToken = helper.decodeToken(userData['token']);
          const expirationDate = decodedToken.exp;
          const isExpired = decodedToken.iat;

          switch (decodedToken.roles[0]){
            case 'ROLE_ADMIN' : {
              console.log(decodedToken);
              this.router.navigate(['/admin']);
              break;
            }
            case 'ROLE_CM' : {
              this.router.navigate(['/cm']);
              break;
            }
            case 'ROLE_APPRENANT' : {
              this.router.navigate(['/apprenant']);
              break;
            }
            case 'ROLE_FORMATEUR' : {
              this.router.navigate(['/formateur']);
              break;
            }


          }

        },
        error => {
        console.log(error);
        });
  }

}
