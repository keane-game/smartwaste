import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { LoginComponent } from './core/login/login.component';
import { UserComponent } from './entity/user/user.component';
import { DepotComponent } from './entity/depot/depot.component';
import { LayoutComponent } from './share/layout/layout.component';
import { SidebarComponent } from './share/sidebar/sidebar.component';
import { FooterComponent } from './share/footer/footer.component';
import { HeaderComponent } from './share/header/header.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { IonicModule } from '@ionic/angular';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

import { TapbarWidget } from './share/widget/tapbar.widget';

import { HttpClientModule, HttpClient } from '@angular/common/http';
import { HeaderBreadcrumbComponent } from './share/header-breadcrumb/header-breadcrumb.component';
import { MaterialsModule } from './materials/material.module';
import { JwtModule } from "@auth0/angular-jwt";
import { AppCommonModule } from './app.common.module';

export function tokenGetter() {
  return localStorage.getItem("access_token");
}

@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    UserComponent,
    DepotComponent,
    LayoutComponent,
    SidebarComponent,
    FooterComponent,
    HeaderComponent,
    DashboardComponent,
    HeaderBreadcrumbComponent,
    TapbarWidget
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    MaterialsModule,
    IonicModule.forRoot(),
    BrowserAnimationsModule,
    HttpClientModule,
    AppCommonModule,
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
