import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { CreateUserComponent } from './create-user/create-user.component';
import { UserComponent } from './main-content/user.component';

const routes: Routes = [

    {  path: '', component: UserComponent },
    {  path: '', component: CreateUserComponent },


];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class UsersRoutingModule { }
