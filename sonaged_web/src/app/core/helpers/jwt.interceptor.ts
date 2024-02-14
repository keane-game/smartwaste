import { Injectable } from '@angular/core';
import { HttpRequest, HttpHandler, HttpEvent, HttpInterceptor } from '@angular/common/http';
import { Observable } from 'rxjs';

import { AuthService } from '../services/auth.service';

@Injectable()
export class JwtInterceptor implements HttpInterceptor {
    constructor(private authService: AuthService) {}

    intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        // add authorization header with jwt token if available
        const currentUser = this.authService.currentUserValue;
        if (currentUser && this.authService.getAuthToken()) {
            request = request.clone({
                setHeaders: {
                    Authorization: `Bearer ${this.authService.getAuthToken()}`
                }
            });
        }

        return next.handle(request);
    }
}
