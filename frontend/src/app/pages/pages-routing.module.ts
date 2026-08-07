import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

const routes: Routes = [

    {
        path: 'users',
        loadChildren: () => import('./users/users.module').then(m => m.UserModule)
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
        path: 'depotoirs',
        loadChildren: () => import('./depotoir/depotoir.module').then(m => m.DepotoirModule) 
    },
    {
        path: 'circuit-collects',
        loadChildren: () => import('./circuit-collect/circuit-collect.module').then(m => m.CircuitCollectModule)
    },
    {
        path: 'circuit-balayages',
        loadChildren: () => import('./circuit-balayage/circuit-balayage.module').then(m => m.CircuitBalayageModule)
    },

    { path: 'notifications', loadChildren: () => import('./notification/notifcation.module').then(m => m.NotifcationModule) },

    { path: 'alerts', loadChildren: () => import('./alert/alert.module').then(m => m.AlertModule) },

    { path: 'maps', loadChildren: () => import('./maps/maps.module').then(m => m.MapsModule) },

    {
        path: 'collection-routes',
        loadChildren: () => import('./collection-route/collection-route.module').then(m => m.CollectionRouteModule)
    },

    {
        path: 'reports',
        loadChildren: () => import('./reports/reports.module').then(m => m.ReportsModule)
    },

    {
        path: 'moblier-urbains',
        loadChildren: () => import('./moblier-urbain/moblier-urbain.module').then(m => m.MoblierUrbainModule)
    },

    {
        path: 'vehicles',
        loadChildren: () => import('./vehicle/vehicle.module').then(m => m.VehicleModule)
    },

    {
        path: 'devices',
        loadChildren: () => import('./device/device.module').then(m => m.DeviceModule)
    },

]

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class PagesRoutingModule { }
