import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { ListUserComponent } from './users/list-user/list-user.component';

const routes: Routes = [


    {
        path: 'user-list', component: ListUserComponent,
        loadChildren: () => import('src/app/entity/users/user.module').then(m => m.UserModule)
    }
]

@NgModule({
    imports: [RouterModule.forChild(routes)],
exports: [RouterModule]
})
export class EntitiesRoutingModule { }
