import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { SidebarComponent } from './share/sidebar/sidebar.component';
import { LayoutComponent } from './share/layout/layout.component';
import { LoginComponent } from './core/login/login.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { UserComponent } from './entity/user/user.component';
import { PageNotFoundComponent } from './exception/page-not-found/page-not-found.component';

const routes: Routes = [

  {path:'', redirectTo: 'ucg', pathMatch: 'full'},
  {path:'login', component: LoginComponent},
  {path:'ucg', component: LayoutComponent,
  children: [
    {path:'', component: DashboardComponent},

  ]
},

{
  path: 'error',
  component: PageNotFoundComponent,
  // loadChildren: () => import('src/app/shared/error/error.module').then(m => m.ErrorModule)
},
{
  path: '',
  redirectTo: 'login',
  pathMatch: 'full'
}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
