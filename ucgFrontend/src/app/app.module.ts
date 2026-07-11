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
import { LoginComponent } from './core/login/login.component';
import { LayoutComponent } from './shares/layout/layout.component';
import { SidebarComponent } from './shares/sidebar/sidebar.component';
import { FooterComponent } from './shares/footer/footer.component';
import { HeaderComponent } from './shares/header/header.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { HeaderBreadcrumbComponent } from './shares/header-breadcrumb/header-breadcrumb.component';

import { TapbarWidget } from './shares/widget/tapbar.widget';
import { PuPopWidget } from './shares/widget/pupop.widget'
import { EntitiesModule } from './entity/entities.module';


export function tokenGetter() {
  return localStorage.getItem("access_token");
}

@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    LayoutComponent,
    SidebarComponent,
    FooterComponent,
    HeaderComponent,
    DashboardComponent,
    HeaderBreadcrumbComponent,
    TapbarWidget,
    PuPopWidget
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    MaterialsModule,
    IonicModule.forRoot(),
    BrowserAnimationsModule,
    HttpClientModule,
    EntitiesModule,
    ReactiveFormsModule, 
    FormsModule,
    JwtModule.forRoot({
      config: {
        tokenGetter: tokenGetter,
        allowedDomains: ["example.com"],
        disallowedRoutes: ["http://example.com/examplebadroute/"],
      },
    }),
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
