import { Component } from '@angular/core';
import { AuthService } from '../../core/services/auth.service';
import { NgForm } from '@angular/forms';
import { Router } from '@angular/router';
import { JwtHelperService } from '@auth0/angular-jwt';
import { errorAlert } from '../../services/alert.service';
import { first } from 'rxjs';

@Component({
    selector: 'app-login',
    templateUrl: './login.component.html',
    styleUrl: './login.component.scss',
    standalone: false
})
export class LoginComponent {

  imageUrl: string = '../assets/images/loginImg1.png';
  form: any = {
    username: '',
    password: ''
  };
  isLoggedIn = false;
  isLoading: boolean = false;
  errorMessage = '';
  errorFields: any = {};

  constructor(
    private authService: AuthService,
    private router: Router,
    private jwtHelper: JwtHelperService
  ) { }


  onSubmit(f: NgForm): void {
  
   this.errorMessage = '';
   this.errorFields = {};
  
   if (!this.form.username || !this.form.password) {
     this.errorMessage = 'Veuillez remplir tous les champs';
     errorAlert( this.errorMessage);
     this.errorFields = {
       username: !this.form.username,
       password: !this.form.password
     };
     return;
   }
   this.isLoading = true;
   this.isLoggedIn = true;  
    console.log('terr')
    this.authService.login(this.form)
    .pipe(first())
    .subscribe({
      next:  (userData) =>{
        const decodedToken = this.jwtHelper.decodeToken(userData['bearer']);
        if (decodedToken && decodedToken.role && decodedToken.role[0]) {
          const expirationDate = decodedToken.exp;
          const isExpired = decodedToken.sub;
          console.log(decodedToken.role[0]);
          this.isLoading = false;
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
          errorAlert('Votre authentification a échouée!')
          this.isLoading = false;
          // Ajoutez ici la gestion de l'erreur, par exemple afficher un message d'erreur à l'utilisateur
      }
        
      },
      error: (error) => {
        this.errorMessage = error.error.message;
        this.errorFields = {
          username: true,
          password: true
        };
        errorAlert('Erreur: ' + error.error.message)
       
      }
  });
  
  }
 


  onFocus(field: string): void {
    if (this.errorFields[field]) {
      this.errorFields[field] = false;
    }
  }
}
