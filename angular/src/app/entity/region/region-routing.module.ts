import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { crudRoutes } from '../../shares/crud/crud.module';

/**
 * Routes de la ressource « regions » : liste, création et édition sont rendues par les
 * composants CRUD génériques, configurés par `ENTITY_CONFIGS` (clé « regions »).
 */
@NgModule({
  imports: [RouterModule.forChild(crudRoutes('regions'))],
  exports: [RouterModule]
})
export class RegionRoutingModule { }
