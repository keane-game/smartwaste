import { Injectable } from '@angular/core';
import { HttpRequest, HttpHandler, HttpEvent, HttpInterceptor, HttpErrorResponse } from '@angular/common/http';
import { Observable, TimeoutError, of, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import swal from 'sweetalert';
import { AuthService } from '../services/auth.service';

@Injectable()
export class ErrorInterceptor implements HttpInterceptor {
    constructor(private authenticationService: AuthService) {}

    intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        return next.handle(request).pipe(catchError(err => {
            if (err.status === 401) {
                // auto logout if 401 response returned from api
                this.authenticationService.logout();
                location.reload();
            }

            const error = err.error.message || err.statusText;
            return this.serviceErrorHandler<any>(error);
        }));
    }

    serviceErrorHandler<T>(error: HttpErrorResponse): Observable<T> {
        console.log(error)
        if (error instanceof TimeoutError) {
          swal('Erreur', 'Voutre connexion internet est instable. Veuillez verifier votre connexion internet', 'error');
          return of({} as T);
        }
    
        if (error instanceof HttpErrorResponse) {
          if (!navigator.onLine) {
            // Handle offline error
            console.log('offline bro');
            swal('Erreur', 'Vous etes hors connexion. Veuillez verifier votre connexion internet', 'error')
    
          } else {
            switch (error.status) {
            
                case 401:
                    this.authenticationService.logout();
                    location.reload();
                    break;
                case 400:
                    swal('Error', error.error.message, 'error')
                    break;
                case 403:
                    swal('Error', error.error.message, 'error')
                    break;
                case 404:
                    swal('Error', error.error.message, 'error')
                    break;
                case 500:
                    swal('Error', 'Verifiez votre connexion internet et contacter votre editeur de logiciel si le soucis persiste', 'error')
                    break;
                default:
                    swal('Error', 'Verifiez votre connexion internet et contacter votre editeur de logiciel si le soucis persiste', 'error')
                    break;
    
            }
          }
        } else {
          // Handle Client Error (Angular Error, ReferenceError...)
          console.log('angular error');
          // console.log(error);
        }
        return of({} as T);
      }
}


