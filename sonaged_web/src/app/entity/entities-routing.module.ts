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
        path: 'profil',
        loadChildren: () => import('./profil/profil.module').then(m => m.ProfilModule)
    },
    { path: 'region', loadChildren: () => import('./region/region.module').then(m => m.RegionModule) },
    {
        path: 'departements',
        loadChildren: () => import('./department/department.module').then(m => m.DepartmentModule)
    },

    { path: 'communes', loadChildren: () => import('./commune/commune.module').then(m => m.CommuneModule) },


    { path: 'quartiers', loadChildren: () => import('./quartier/quartier.module').then(m => m.QuartierModule) },

    {
        path: 'depotoirs', component:ListDepotoirComponent,
        loadChildren: () => import('./depotoir/depotoir.module').then(m => m.DepotoirModule) 
    },
    {
        path: 'circuits',
        loadChildren: () => import('./circuit/circuit.module').then(m => m.CircuitModule)
    },


   

]

@NgModule({
    imports: [RouterModule.forChild(routes)],
exports: [RouterModule]
})
export class EntitiesRoutingModule { }
