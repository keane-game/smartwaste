import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LayoutComponent } from './shared/components/layout/layout.component';
import { DashboardComponent } from './pages/dashboard/dashboard.component';
import { HomeComponent } from './pages/general/home/home.component';
import { NotFoundComponent } from './pages/general/not-found/not-found.component';

const routes: Routes = [

  { 
    path:'', component: LayoutComponent, 
    children: [
      {
        path: '',component:DashboardComponent,
      },
      {
        path: 'dashboard',component:DashboardComponent,
      },
      {
        path: '',
        loadChildren: () => import('./pages/pages.module').then(m => m.PagesModule)
      },
      {
        path: 'password',
        loadChildren: () => import('./core/password/password.module').then(m => m.PasswordModule)
      },

    ]
  },


 { path: 'home', component: HomeComponent, },

  {
    path: 'login',
    loadChildren: () => import('./core/login/login.module')
      .then(mod => mod.LoginModule)
  },
  {
    path: 'signup',
    loadChildren: () => import('./pages/general/signup/signup.module')
      .then(mod => mod.SignupModule)
  },
  {
    path: 'contact',
    loadChildren: () => import('./pages/general/contact/contact.module')
      .then(mod => mod.ContactModule)
  },
 

  {
    path: 'about',
    loadChildren: () => import('./pages/general/about/about.routes').then(routes => routes.routes)
  },


 
  { path: '**', component: NotFoundComponent }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
