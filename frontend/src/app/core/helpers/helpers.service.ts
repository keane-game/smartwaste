import { HttpClient } from "@angular/common/http";
import { TranslateLoader, TranslateModule } from "@ngx-translate/core";
import { TranslateHttpLoader } from "@ngx-translate/http-loader";

export function httpLoaderFactory(http: HttpClient) {
    return new TranslateHttpLoader(http);
  }
  
  export function tokenGetter() {
    return localStorage.getItem("access_token");
  }
  
  export function providersFrom(){

    return [ TranslateModule.forRoot({
            defaultLanguage: 'en',
            loader: {
            provide: TranslateLoader,
            deps: [HttpClient],
            useFactory: httpLoaderFactory
            }
        }), 
        // JwtModule.forRoot({
        //     config: {
        //         tokenGetter: tokenGetter,
        //         allowedDomains: ["localhost:5000"],
        //         disallowedRoutes: ["http://example.com/examplebadroute/"],
        //     },
        // }),
    ]
   
  }