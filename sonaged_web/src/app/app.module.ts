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

import { AppComponent } from './app.component';
import { LayoutComponent } from './shares/layout/layout.component';
import { SidebarComponent } from './shares/sidebar/sidebar.component';
import { FooterComponent } from './shares/footer/footer.component';
import { HeaderComponent } from './shares/header/header.component';

import { NgMultiSelectDropDownModule } from 'ng-multiselect-dropdown'
import { DragDropDirective } from './directives/drag-drop.directive';
import { FileUploadComponent } from './shares/file-upload/file-upload.component';
import { LeafletModule } from '@asymmetrik/ngx-leaflet';


export function tokenGetter() {
  return localStorage.getItem("access_token");
}

@NgModule({
  declarations: [
    AppComponent,
    LayoutComponent,
    SidebarComponent,
    FooterComponent,
    HeaderComponent,
    FileUploadComponent,
    DragDropDirective,
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


  ],schemas:[CUSTOM_ELEMENTS_SCHEMA],
  providers: [AppRoutingModule],
  bootstrap: [AppComponent],
})

export class AppModule { }
