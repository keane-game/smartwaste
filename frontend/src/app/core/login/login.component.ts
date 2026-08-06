import { AfterViewInit, Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { JwtHelperService } from '@auth0/angular-jwt';

import { AuthService } from '../services/auth.service';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { first } from 'rxjs';
import { succesAlert, errorAlert } from '../../services/alert.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit, AfterViewInit {
  hide = true;
  form: any = {};
  isLoggedIn = false;
  isLoginFailed = false;
  error!: { 'errorTitle': any; 'errorDesc': any; };
  imageUrl: string = '../assets/images/loginImg1.png';
  email : any;
  password : any;
  connexion_success = false;
  display_error = false;
  verified_users : any;
  verified_user_simulation = [{
    "email": "mouhamed.dieng@atos.net",
    "password" : "password"
  }]



  constructor(private authService: AuthService, private router: Router) {

  }

  ngOnInit(): void {
    // console.log(this.authService.getAuthToken());

  }


  onSubmit(): any {
    this.isLoggedIn = true;

    this.authService.login(this.form)
    .pipe(first())
        .subscribe({
          next:  (userData) => {
            succesAlert("La mise à jour a bien réussie")
          
              const helper = new JwtHelperService();
              console.log(userData);

              // Décodage du token
              const decodedToken = helper.decodeToken(userData['bearer']);
              if (decodedToken && decodedToken.role && decodedToken.role[0]) {
                  const expirationDate = decodedToken.exp;
                  const isExpired = decodedToken.sub;
                  console.log(decodedToken.role[0]);

                  // Gestion des rôles
                  const role = decodedToken.role[0].authority || decodedToken.role[0];
                  switch (role) {
                      case 'ROLE_ADMIN':
                          console.log(decodedToken);
                          this.router.navigate(['/']);
                          break;
                      case 'ROLE_USER':
                          this.router.navigate(['/']);
                          break;
                      default:
                          this.router.navigate(['/']);
                          break;
                  }
              } else {
                  console.error('Token décodé invalide');
                  // Ajoutez ici la gestion de l'erreur, par exemple afficher un message d'erreur à l'utilisateur
              }
          
          },
          error: (error) => {
            console.log(error)
            errorAlert('Erreur ' + error.error.message)
          },
            
        });
}







  ngAfterViewInit(): void {
    // Get the elements
    const eye = document.getElementById('eye');
    const eyeoff = document.getElementById('eyeoff');
    const passwordField = document.getElementById('passwordField') as HTMLInputElement;

    if (eye && eyeoff && passwordField) {
      // Event listeners
      eye.addEventListener('click', () => {
        eye.style.display = 'none';
        eyeoff.style.display = 'block';
        passwordField.type = 'text';
      });

      eyeoff.addEventListener('click', () => {
        eye.style.display = 'block';
        eyeoff.style.display = 'none';
        passwordField.type = 'password';
      });
    }
  }

 
}