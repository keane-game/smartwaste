import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LayoutComponent } from './shares/layout/layout.component';
import { LoginComponent } from './core/login/login.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { PageNotFoundComponent } from './exception/page-not-found/page-not-found.component';
import { HomeComponent } from './pages/general/home/home.component';
import { NotFoundComponent } from './pages/general/not-found/not-found.component';
import { ListUserComponent } from './entity/users/list/list-user.component';

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
        path: 'supervision',
        loadComponent: () => import('./supervision/supervision-map.component').then(m => m.SupervisionMapComponent)
      },
      {
        path: 'avis',
        loadComponent: () => import('./avis/avis.component').then(m => m.AvisComponent)
      },
      {
        path: '',
        loadChildren: () => import('./entity/entities.module').then(m => m.EntitiesModule)
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
