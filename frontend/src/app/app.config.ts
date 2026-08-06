import { ApplicationConfig, importProvidersFrom } from '@angular/core';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { HTTP_INTERCEPTORS, HttpClient, provideHttpClient, withInterceptors, withInterceptorsFromDi } from '@angular/common/http';
import { TranslateModule, TranslateLoader } from '@ngx-translate/core';
import { TranslateHttpLoader } from '@ngx-translate/http-loader';
import { AuthInterceptor } from './core/helpers/auth.interceptor';
import { ErrorInterceptor } from './core/helpers/error.inerceptor';


export function HttpLoaderFactory(http: HttpClient) {
  return new TranslateHttpLoader(http);
}


export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideAnimationsAsync(),
  
   
    // `AuthInterceptor` (fonctionnel) pose l'en-tête, puis `ErrorInterceptor` (classe, donc
    // fourni par le DI car il injecte `AuthService`) traite les 401 et rejoue la requête avec le
    // jeton renouvelé. L'ordre compte : le rejeu se fait en aval, sa propre en-tête l'emporte.
    provideHttpClient(
      withInterceptors([
        AuthInterceptor
      ]),
      withInterceptorsFromDi(),
    ),
    { provide: HTTP_INTERCEPTORS, useClass: ErrorInterceptor, multi: true },
    importProvidersFrom(TranslateModule.forRoot({
      defaultLanguage: 'en',
      loader: {
        provide: TranslateLoader,
        deps: [HttpClient],
        useFactory: HttpLoaderFactory
      }
    })),
  ]
};
