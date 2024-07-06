import { AfterViewInit, Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { JwtHelperService } from '@auth0/angular-jwt';

import { AuthService } from '../services/auth.service';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';

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
    // console.log(this.form);

    this.authService.login(this.form)
      .subscribe(
        userData => {

          const helper = new JwtHelperService();
console.log(userData);

          // tslint:disable-next-line: no-string-literal
          const decodedToken = helper.decodeToken(userData['bearer']);
          const expirationDate = decodedToken.exp;
          const isExpired = decodedToken.sub;
          console.log(decodedToken.role[0]);
          
          //  this.router.navigate(['/dashboard']);
          let role = decodedToken.role[0].authority
          switch (role){
            case 'ROLE_ADMIN' : {
              console.log(decodedToken);
              this.router.navigate(['/']);
              break;
            }
            case 'ROLE_USER' : {
              this.router.navigate(['/']);
              break;
            }
            default :
            this.router.navigate(['/']);
            

          }

        },
        error => {
        console.log(error);
        });
  }






  onLogin_simulation() {
    //this.router.navigate(['/home']);
    // this function will be executed when the user log
    // we have already grap the email and password
    this.verified_user_simulation.forEach(user => {
      if(user.email == this.email && user.password == this.password)
      {
        // alors c'est bon // on redirige vers kpi
        this.router.navigateByUrl("/dashboard")
        this.display_error = false
        this.connexion_success = true;
      }
      // else
      // {
      //   // on reste ici
      //   // on affiche un message d'erreur
      //   // on style les input par animation
      //   this.display_error = true;
      //   console.log("veu")
      // }
    })

    if(!this.connexion_success)
    {
         this.display_error = true;
         console.log("veu")
    }


  }
  onLogin()
  {
    // first we get all user exist in the database
  
    // second we iterate in this verified user
    this.verified_users.forEach((user: { email: any; password: any; }) => {
      if(user.email == this.email && user.password == this.password)
      {
        // alors c'est bon // on redirige vers kpi
        this.router.navigateByUrl("/dashboard")
        this.display_error = false
        this.connexion_success = true;
      }
      
    })

    if(!this.connexion_success)
    {
         this.display_error = true;
         console.log("veu")
    }

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