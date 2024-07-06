import { Injectable } from '@angular/core';
import {  Router,  CanMatch, Route, UrlSegment, ActivatedRouteSnapshot, CanActivate, RouterStateSnapshot } from '@angular/router';
import { Observable } from 'rxjs';
import { AuthService } from '../services/auth.service';

@Injectable()
 export class AuthGuard implements CanActivate {

//     constructor(private router: Router, private userContextService: UserContextService) { }

//     canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {
//         const user = this.userContextService.user$.getValue();
//         if (user != null) {
//             // logged in so return true
//             return true;
//         }

//         // not logged in so redirect to login page with the return url and return false
//         this.router.navigate(['/login'], { queryParams: { returnUrl: state.url } });
//         return false;
//     }
// }

// export class AuthGuard implements CanMatch {

//     constructor(private authService: AuthService, private router: Router) {}
  
//     canMatch(
//       route: Route,
//       segments: UrlSegment[]
//     ): boolean | Observable<boolean> | Promise<boolean> {
//       const isAuthenticated = this.authService.isLoggedIn();
//       if (!isAuthenticated) {
//         this.router.navigate(['/login']);
//       }
//       return isAuthenticated;
//     }
constructor(private authService: AuthService, private router: Router) {}

canActivate(
  route: ActivatedRouteSnapshot,
  state: RouterStateSnapshot): boolean {
  if (this.authService.isAuthenticated()) {
    return true;
  } else {
    this.router.navigate(['/login']);
    return false;
  }
}
  }
