import { Routes } from '@angular/router';

import { HomeComponent } from './pages/general/home/home.component';
import { NotFoundComponent } from './pages/general/not-found/not-found.component';
import { DashboardComponent } from './pages/dashboard/dashboard.component';
import { LayoutComponent } from './shared/components/layout/layout.component';
import { AuthGuard } from './core/helpers/auth.guard';
import { EsriComponent } from './pages/maps/esri/esri.component';

export const routes: Routes = [

  {
    path:'', component: LayoutComponent,
    canActivate: [AuthGuard],
    canActivateChild: [AuthGuard],
    children: [
      {
        path: '',component:DashboardComponent,
      },
      {
        path: 'dashboard',component:DashboardComponent,
      },
      {
        // Récupéré depuis `angular/` : seul écran citoyen existant du dépôt, sans équivalent ici.
        // Déclaré AVANT l'entrée à chemin vide ci-dessous : une route `path: ''` portant un
        // `loadChildren` capture tout ce qui suit, et Angular ne revient pas en arrière une fois
        // le module paresseux chargé sans correspondance.
        path: 'avis',
        loadComponent: () => import('./pages/avis/avis.component').then(m => m.AvisComponent)
      },
      {
        path: '',
        loadChildren: () => import('./pages/pages.module').then(m => m.PagesModule)
      },

    ]
  },

  {
    // Hors garde : un mot de passe se réinitialise précisément quand on n'est pas connecté.
    path: 'password',
    loadChildren: () => import('./core/password/password.module').then(m => m.PasswordModule)
  },

  { path: 'home', component: HomeComponent, },
  { path: 'map', component: EsriComponent },

  {
    // Seule route de connexion : c'est celle-ci que `AuthGuard` et `AuthService.logout()`
    // ciblent. `pages/login/` (routé jadis sur `/logins`) est un doublon jamais référencé
    // ailleurs dans l'app — retiré du routage, fichiers conservés (suppression = validation).
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