import { ApplicationConfig, importProvidersFrom } from '@angular/core';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import {  providersFrom } from './core/helpers/helpers.service';
import { AuthGuard } from './core/gaurds/auth.gaurd';




export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideAnimationsAsync(),
    
    provideHttpClient(
      withInterceptorsFromDi(), // tell httpClient to use interceptors from DI
    ),

    importProvidersFrom(
      ...providersFrom()
    ),
    //...httpInterceptorProviders
    AuthGuard
  ]
};
