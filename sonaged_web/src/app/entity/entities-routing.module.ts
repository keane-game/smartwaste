import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { ListUserComponent } from './users/list/list-user.component';
import { CreateUserComponent } from './users/create/create-user.component';
import { ListDepotoirComponent } from './depotoir/list/list-depotoir.component';

const routes: Routes = [

    {
        path: 'users', component: ListUserComponent,
        loadChildren: () => import('../entity/users/user.module').then(m => m.UserModule)
    },


    {
        path: 'depotoirs', component:ListDepotoirComponent,
        loadChildren: () => import('./depotoir/depotoir.module').then(m => m.DepotoirModule) 
    },

]

@NgModule({
    imports: [RouterModule.forChild(routes)],
exports: [RouterModule]
})
export class EntitiesRoutingModule { }
