import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { CreateUserComponent } from './create/create-user.component';
import { ListUserComponent } from './list/list-user.component';

const routes: Routes = [

    {  path: '', component: CreateUserComponent },
    {  path: '', component: ListUserComponent },
    {  path: '', component: CreateUserComponent },


];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class UsersRoutingModule { }
