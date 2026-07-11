import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { SidebarComponent } from './shares/sidebar/sidebar.component';
import { LayoutComponent } from './shares/layout/layout.component';
import { LoginComponent } from './core/login/login.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { ListUserComponent } from './entity/users/list-user/list-user.component';
import { PageNotFoundComponent } from './exception/page-not-found/page-not-found.component';

const routes: Routes = [

  {path:'', redirectTo: 'ucg', pathMatch: 'full'},
  {path:'login', component: LoginComponent},
  {path:'ucg', component: LayoutComponent,
  children: [
    {path:'', component: DashboardComponent},
    {path:'users', component: ListUserComponent },
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
