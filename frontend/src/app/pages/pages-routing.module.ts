import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { roleGuard } from '../core/guards/role.guard';

/**
 * Ferme le trou trouve en Phase 0 de l'audit : `AuthGuard` ne verifiait que la connexion, jamais
 * le role — un AGENT ou un USER pouvait naviguer vers n'importe lequel de ces ecrans
 * d'administration (l'appel API echouait ensuite en 403, mais l'ecran restait accessible et
 * rendait une page cassee plutot que de ne jamais s'afficher). `profil` reste volontairement
 * SANS garde de role : tout compte authentifie doit pouvoir consulter le sien.
 */
const ADMIN_ROLES = ['SUPER_ADMIN', 'ADMIN', 'SUPERVISEUR', 'TECHNICIEN_IOT'];

const routes: Routes = [

    {
        path: 'users',
        loadChildren: () => import('./users/users.module').then(m => m.UserModule),
        canActivate: [roleGuard], data: { roles: ADMIN_ROLES },
    },
    {
        path: 'profil',
        loadChildren: () => import('./profil/profil.module').then(m => m.ProfilModule)
    },
    {
        path: 'region',
        loadChildren: () => import('./region/region.module').then(m => m.RegionModule),
        canActivate: [roleGuard], data: { roles: ADMIN_ROLES },
    },
    {
        path: 'departements',
        loadChildren: () => import('./department/department.module').then(m => m.DepartmentModule),
        canActivate: [roleGuard], data: { roles: ADMIN_ROLES },
    },

    {
        path: 'communes',
        loadChildren: () => import('./commune/commune.module').then(m => m.CommuneModule),
        canActivate: [roleGuard], data: { roles: ADMIN_ROLES },
    },

    {
        path: 'quartiers',
        loadChildren: () => import('./quartier/quartier.module').then(m => m.QuartierModule),
        canActivate: [roleGuard], data: { roles: ADMIN_ROLES },
    },

    {
        path: 'depotoirs',
        loadChildren: () => import('./depotoir/depotoir.module').then(m => m.DepotoirModule),
        canActivate: [roleGuard], data: { roles: ADMIN_ROLES },
    },
    {
        path: 'circuit-collects',
        loadChildren: () => import('./circuit-collect/circuit-collect.module').then(m => m.CircuitCollectModule),
        canActivate: [roleGuard], data: { roles: ADMIN_ROLES },
    },
    {
        path: 'circuit-balayages',
        loadChildren: () => import('./circuit-balayage/circuit-balayage.module').then(m => m.CircuitBalayageModule),
        canActivate: [roleGuard], data: { roles: ADMIN_ROLES },
    },

    {
        path: 'notifications',
        loadChildren: () => import('./notification/notifcation.module').then(m => m.NotifcationModule),
        canActivate: [roleGuard], data: { roles: ADMIN_ROLES },
    },

    {
        path: 'alerts',
        loadChildren: () => import('./alert/alert.module').then(m => m.AlertModule),
        canActivate: [roleGuard], data: { roles: ADMIN_ROLES },
    },

    {
        path: 'maps',
        loadChildren: () => import('./maps/maps.module').then(m => m.MapsModule),
        canActivate: [roleGuard], data: { roles: ADMIN_ROLES },
    },

    {
        path: 'collection-routes',
        loadChildren: () => import('./collection-route/collection-route.module').then(m => m.CollectionRouteModule),
        // Seul ecran partage entre l'administration et le terrain (Tournee agent, section 18).
        canActivate: [roleGuard], data: { roles: [...ADMIN_ROLES, 'AGENT'] },
    },

    {
        path: 'reports',
        loadChildren: () => import('./reports/reports.module').then(m => m.ReportsModule),
        canActivate: [roleGuard], data: { roles: ADMIN_ROLES },
    },

    {
        path: 'moblier-urbains',
        loadChildren: () => import('./moblier-urbain/moblier-urbain.module').then(m => m.MoblierUrbainModule),
        canActivate: [roleGuard], data: { roles: ADMIN_ROLES },
    },

    {
        path: 'vehicles',
        loadChildren: () => import('./vehicle/vehicle.module').then(m => m.VehicleModule),
        canActivate: [roleGuard], data: { roles: ADMIN_ROLES },
    },

    {
        path: 'devices',
        loadChildren: () => import('./device/device.module').then(m => m.DeviceModule),
        canActivate: [roleGuard], data: { roles: [...ADMIN_ROLES] },
    },

]

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class PagesRoutingModule { }
