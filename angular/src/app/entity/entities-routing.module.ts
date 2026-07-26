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

    {
        path: 'departments',
        loadChildren: () => import('./department/department.module').then(m => m.DepartmentModule)
    },

    {
        path: 'communes',
        loadChildren: () => import('./commune/commune.module').then(m => m.CommuneModule)
    },

    {
        path: 'quartiers',
        loadChildren: () => import('./quartier/quartier.module').then(m => m.QuartierModule)
    },

    {
        path: 'regions',
        loadChildren: () => import('./region/region.module').then(m => m.RegionModule)
    },

    {
        path: 'circuits',
        loadChildren: () => import('./circuit/circuit.module').then(m => m.CircuitModule)
    },

    {
        path: 'circuit-collects',
        loadChildren: () => import('./circuit-collect/circuit-collect.module').then(m => m.CircuitCollectModule)
    },

    {
        path: 'circuit-balayages',
        loadChildren: () => import('./circuit-balayage/circuit-balayage.module').then(m => m.CircuitBalayageModule)
    },

    {
        path: 'moblier-urbains',
        loadChildren: () => import('./moblier-urbain/moblier-urbain.module').then(m => m.MoblierUrbainModule)
    },

    {
        path: 'typedepotoirs',
        loadChildren: () => import('./type-depotoir/type-depotoir.module').then(m => m.TypeDepotoirModule)
    },

    {
        path: 'alerts',
        loadChildren: () => import('./alert/alert.module').then(m => m.AlertModule)
    },

    {
        path: 'authorities',
        loadChildren: () => import('./authority/authority.module').then(m => m.AuthorityModule)
    },

    {
        path: 'import',
        loadChildren: () => import('./data-import/data-import.module').then(m => m.DataImportModule)
    },

    // Ressources exposées par le backend mais sans écran jusqu'ici.
    {
        path: 'coordinates',
        loadChildren: () => import('./coordinate/coordinate.module').then(m => m.CoordinateModule)
    },

    {
        path: 'geometries',
        loadChildren: () => import('./geometry/geometry.module').then(m => m.GeometryModule)
    },

]

@NgModule({
    imports: [RouterModule.forChild(routes)],
exports: [RouterModule]
})
export class EntitiesRoutingModule { }
