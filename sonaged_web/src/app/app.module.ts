import { ApplicationConfig, CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA } from '@angular/core';
import { provideRouter } from '@angular/router';



import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule } from '@angular/common/http';
import { AppRoutingModule } from './app-routing.module';
import { IonicModule } from '@ionic/angular';
import { MaterialsModule } from './materials/material.module';
import { JwtModule } from "@auth0/angular-jwt";
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

import { ReactiveFormsModule, FormsModule } from '@angular/forms';


import { NgMultiSelectDropDownModule } from 'ng-multiselect-dropdown'
import { LeafletModule } from '@asymmetrik/ngx-leaflet';


export function tokenGetter() {
  return localStorage.getItem("access_token");
}

@NgModule({
  declarations: [
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    MaterialsModule,
    IonicModule.forRoot(),
    BrowserAnimationsModule,
    HttpClientModule,
    ReactiveFormsModule, 
    FormsModule,

    NgMultiSelectDropDownModule.forRoot(),
    JwtModule.forRoot({
      config: {
        tokenGetter: tokenGetter,
        allowedDomains: ["example.com"],
        disallowedRoutes: ["http://example.com/examplebadroute/"],
      },
    }),
    LeafletModule,

    MaterialsModule,


  ],
  schemas:[CUSTOM_ELEMENTS_SCHEMA],
  providers: [AppRoutingModule],
  bootstrap: [],
})

export class AppModule { }
