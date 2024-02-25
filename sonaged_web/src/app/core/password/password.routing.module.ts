import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { RestPasswordComponent } from './rest-password/rest-password.component';
import { PasswordComponent } from './password.component';

const routes: Routes = [
    { path: '', component: RestPasswordComponent },
    { path: '', component: PasswordComponent }
    ];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class PasswordRoutingModule { }
