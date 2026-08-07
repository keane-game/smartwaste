import { ApplicationConfig, importProvidersFrom } from '@angular/core';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
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
  
   
    // `AuthInterceptor` pose l'en-tête, puis `ErrorInterceptor` traite les 401 et rejoue la
    // requête avec le jeton renouvelé. L'ordre compte : le rejeu se fait en aval, sa propre
    // en-tête l'emporte. Les deux sont fonctionnels (`inject()` résolu à la requête) plutôt que
    // fournis via `HTTP_INTERCEPTORS` : `ErrorInterceptor` a besoin d'`AuthService`, qui a besoin
    // de `HttpClient` — en `HTTP_INTERCEPTORS` classique cela forme un cycle de DI (`NG0200`)
    // puisque construire `HttpClient` réclame alors l'intercepteur avant que `HttpClient`
    // n'existe. `withInterceptors` évite le cycle : `inject()` s'exécute au moment de la requête.
    provideHttpClient(
      withInterceptors([
        AuthInterceptor,
        ErrorInterceptor,
      ]),
    ),
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
