
import { HttpInterceptorFn } from '@angular/common/http';

export const AuthInterceptor: HttpInterceptorFn = (req, next) => {
  const authToken = 'YOUR_AUTH_TOKEN_HERE';
  const currentUser = JSON.parse(localStorage.getItem('currentUser') || '{}');
  // Clone the request and add the authorization header
  const authReq = req.clone({
    setHeaders: {
      Authorization: `Bearer ${currentUser.bearer}`
    }
  });

  // Pass the cloned request with the updated header to the next handler
  return next(authReq);
};