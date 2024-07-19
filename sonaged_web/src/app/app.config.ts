import { ApplicationConfig, importProvidersFrom } from '@angular/core';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { AuthGuard } from './core/gaurds/auth.gaurd';
import { TranslateModule, TranslateLoader } from '@ngx-translate/core';
import { TranslateHttpLoader } from '@ngx-translate/http-loader';
import { AuthInterceptor } from './core/helpers/auth.interceptor';


export function HttpLoaderFactory(http: HttpClient) {
  return new TranslateHttpLoader(http);
}


export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideAnimationsAsync(),
  
   
    provideHttpClient(
      withInterceptors([
        AuthInterceptor
      ]),
      //withInterceptorsFromDi(),
    ),
    importProvidersFrom(TranslateModule.forRoot({
      defaultLanguage: 'en',
      loader: {
        provide: TranslateLoader,
        deps: [HttpClient],
        useFactory: HttpLoaderFactory
      }
    })),
    AuthGuard
  ]
};
